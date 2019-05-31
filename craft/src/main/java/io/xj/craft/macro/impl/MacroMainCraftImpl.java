// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro.impl;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.entity.EntityRank;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.util.Chance;
import io.xj.core.util.Value;
import io.xj.craft.CraftImpl;
import io.xj.craft.exception.CraftException;
import io.xj.craft.macro.MacroMainCraft;
import io.xj.music.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MacroMainCraftImpl extends CraftImpl implements MacroMainCraft {
  private static final double SCORE_MATCHED_KEY_MODE = 2;
  private static final double SCORE_MATCHED_MEMES = 10;
  private static final double SCORE_AVOID_PREVIOUS = 5;
  private static final double SCORE_MACRO_ENTROPY = 0.5;
  private static final double SCORE_MAIN_ENTROPY = 0.5;
  private static final long NANOS_PER_SECOND = 1_000_000_000;
  private final Logger log = LoggerFactory.getLogger(MacroMainCraftImpl.class);
  private Pattern macroPattern;
  private Pattern mainPattern;
  private Sequence macroSequence;
  private Sequence mainSequence;

  @Inject
  public MacroMainCraftImpl(
    @Assisted("basis") Fabricator fabricator
    /*-*/) {
    setFabricator(fabricator);
  }

  @Override
  public void doWork() throws CraftException {
    doMacroCraft();
    doMainCraft();
    doMemeCraft();
    getFabricator().getSegment()
      .setDensity(segmentDensity())
      .setTempo(segmentTempo())
      .setKey(segmentKey())
      .setTotal(chooseMainPattern().getTotal())
      .setEndAtTimestamp(segmentEndTimestamp());
    try {
      getFabricator().updateSegment();
    } catch (CoreException e) {
      throw exception("Failed to update Segment before crafting chords", e);
    }
    doChordCraft();
    report();
    try {
      getFabricator().updateSegment();
    } catch (CoreException e) {
      throw exception("Failed to update Segment upon completion", e);
    }
  }

  /**
   Report
   */
  private void report() {
    // future: getFabricator().report() anything else interesting from the craft operation
  }

  /**
   Make Macro-type Sequence Choice
   add macro-sequence choice to segment

   @throws CraftException on any failure
   */
  private void doMacroCraft() throws CraftException {
    try {
      SequencePattern sequencePattern = getFabricator().getRandomSequencePatternAtOffset(getMacroSequence().getId(), getMacroPatternOffset());
      getFabricator().add(
        new Choice()
          .setSegmentId(getFabricator().getSegment().getId())
          .setType(SequenceType.Macro.toString())
          .setTranspose(getMacroTranspose())
          .setSequencePatternId(sequencePattern.getId()));

    } catch (CoreException e) {
      throw exception("Failed to do Macro craft", e);
    }
  }

  /**
   Make Main-type Sequence Choice
   add macro-sequence choice to segment

   @throws CraftException on any failure
   */
  private void doMainCraft() throws CraftException {
    try {
      SequencePattern sequencePattern = getFabricator().getRandomSequencePatternAtOffset(getMainSequence().getId(), getMainPatternOffset());
      getFabricator().add(
        new Choice()
          .setSegmentId(getFabricator().getSegment().getId())
          .setType(SequenceType.Main.toString())
          .setTranspose(getMainTranspose())
          .setSequencePatternId(sequencePattern.getId()));

    } catch (CoreException e) {
      throw exception("Failed to do Main craft", e);
    }
  }

  /**
   Make Memes
   add all memes to segment
   */
  private void doMemeCraft() {
    segmentMemes().forEach((segmentMeme) -> {
      try {
        getFabricator().add(segmentMeme);

      } catch (Exception e) {
        log.warn("Could not create segment meme {}", segmentMeme.getName(), e);
      }
    });
  }

  /**
   Make Chords
   Segment Chords = Main Sequence Pattern Chords, transposed according to to main sequence choice
   [#154090557] don't create chord past end of Segment

   @throws CraftException on any failure
   */
  private void doChordCraft() throws CraftException {
    try {
      getFabricator().getSourceMaterial().getChordsOfPattern(chooseMainPattern().getId())
        .forEach(patternChord -> {

          if (patternChord.getPosition() < getFabricator().getSegment().getTotal()) {
            String name = "NaN";
            try {
              // delta the chord name
              name = patternChord.toMusical().transpose(getMainTranspose()).getFullDescription();
              // create the transposed chord
              getFabricator().add(new SegmentChord()
                .setSegmentId(getFabricator().getSegment().getId())
                .setName(name)
                .setPosition(patternChord.getPosition()));

            } catch (Exception e) {
              log.warn("failed to create transposed segment chord {}@{}",
                name, patternChord.getPosition(), e);
            }
          }
        });

    } catch (CoreException e) {
      throw exception("Failed to do Chord craft", e);
    }
  }

  /**
   compute (and cache) the chosen macro sequence

   @return macro-type sequence
   @throws CraftException on failure
   */
  private Sequence getMacroSequence() throws CraftException {
    try {
      switch (getFabricator().getType()) {

        case Initial:
        case NextMacro:
          return chooseMacro();

        case Continue:
        case NextMain:
          return getFabricator().getSequenceOfChoice(getFabricator().getPreviousMacroChoice());

        default:
          throw exception(String.format("Cannot get Macro-type sequence for unknown fabricator type=", getFabricator().getType()));
      }

    } catch (CoreException e) {
      throw exception("Failed to get Macro Sequence", e);
    }
  }

  /**
   compute (and cache) the mainSequence

   @return mainSequence
   */
  private Sequence getMainSequence() throws CraftException {
    try {
      switch (getFabricator().getType()) {

        case Continue:
          return getFabricator().getSequenceOfChoice(getFabricator().getPreviousMainChoice());

        case Initial:
        case NextMain:
        case NextMacro:
          return chooseMain();

        default:
          throw exception(String.format("Cannot get Main-type sequence for unknown fabricator type=", getFabricator().getType()));
      }

    } catch (CoreException e) {
      throw exception("Failed to get Main Sequence", e);
    }
  }

  /**
   compute (and cache) the macroTranspose

   @return macroTranspose
   */
  private Integer getMacroTranspose() throws CraftException {
    try {
      switch (getFabricator().getType()) {

        case Initial:
          return 0;

        case Continue:
        case NextMain:
          return getFabricator().getPreviousMacroChoice().getTranspose();

        case NextMacro:
          return Key.delta(getMacroSequence().getKey(),
            getFabricator().getPreviousMacroNextOffset().getKey(),
            getFabricator().getPreviousMacroChoice().getTranspose());

        default:
          throw exception(String.format("unable to determine macro-type sequence transposition for segment #%s!", getFabricator().getSegment().getId()));
      }

    } catch (CoreException e) {
      throw exception("Failed to get Macro Transpose", e);
    }
  }

  /**
   compute (and cache) Transpose Main-Sequence to the transposed key of the current macro pattern

   @return mainTranspose
   */
  private Integer getMainTranspose() throws CraftException {
    return Key.delta(getMainSequence().getKey(),
      Value.eitherOr(chooseMacroPattern().getKey(), getMacroSequence().getKey()),
      getMacroTranspose());
  }

  /**
   compute (and cache) the macroPatternOffset

   @return macroPatternOffset
   */
  private BigInteger getMacroPatternOffset() throws CraftException {
    try {
      switch (getFabricator().getType()) {

        case Initial:
        case NextMacro:
          return BigInteger.valueOf(0);

        case Continue:
          return getFabricator().getSequencePatternOffsetForChoice(getFabricator().getPreviousMacroChoice());

        case NextMain:
          return getFabricator().getNextSequencePatternOffset(getFabricator().getPreviousMacroChoice());

        default:
          throw exception(String.format("Cannot get Macro-type sequence for known fabricator type=", getFabricator().getType()));
      }

    } catch (CoreException e) {
      throw exception("Failed to get Macro Pattern Offset", e);
    }
  }

  /**
   compute (and cache) the mainPatternOffset

   @return mainPatternOffset
   */
  private BigInteger getMainPatternOffset() throws CraftException {
    try {
      switch (getFabricator().getType()) {

        case Initial:
        case NextMain:
        case NextMacro:
          return BigInteger.valueOf(0);

        case Continue:
          return getFabricator().getNextSequencePatternOffset(getFabricator().getPreviousMainChoice());

        default:
          throw exception(String.format("Cannot get Macro-type sequence for known fabricator type=", getFabricator().getType()));
      }

    } catch (CoreException e) {
      throw exception("Failed to get Main Pattern Offset", e);
    }
  }

  /**
   Choose a pattern of macro-type sequence
   <p>
   *ONLY CHOOSES ONCE, then returns that choice every time.**

   @return pattern
   @throws CraftException on failure
   */
  private Pattern chooseMacroPattern() throws CraftException {
    if (Objects.isNull(macroPattern))
      try {
        macroPattern = getFabricator().getSourceMaterial().fetchOnePattern(
          getFabricator().getRandomSequencePatternAtOffset(getMacroSequence().getId(), getMacroPatternOffset()).getPatternId());

      } catch (CoreException e) {
        throw exception("Failed to get Macro Pattern", e);
      }

    return macroPattern;
  }

  /**
   Choose a pattern of main-type sequence
   <p>
   *ONLY CHOOSES ONCE, then returns that choice every time.**

   @return pattern
   @throws CraftException on failure
   */
  private Pattern chooseMainPattern() throws CraftException {
    if (Objects.isNull(mainPattern))
      try {
        mainPattern = getFabricator().getSourceMaterial().fetchOnePattern(
          getFabricator().getRandomSequencePatternAtOffset(getMainSequence().getId(), getMainPatternOffset()).getPatternId());

      } catch (CoreException e) {
        throw exception("Failed to get Main Pattern", e);
      }

    return mainPattern;
  }

  /**
   Choose macro sequence
   <p>
   *ONLY CHOOSES ONCE, then returns that choice every time.**

   @return macro-type sequence
   @throws CraftException on failure
   */
  private Sequence chooseMacro() throws CraftException {
    if (Objects.nonNull(macroSequence))
      return macroSequence;

    EntityRank<Sequence> entityRank = new EntityRank<>();

    // (1) retrieve sequences bound to chain
    Collection<Sequence> sourceSequences;
    try {
      sourceSequences = getFabricator().getSourceMaterial().getSequencesOfType(SequenceType.Macro);
    } catch (CoreException e) {
      throw exception("Failed to get source material for choosing Macro", e);
    }

    // (3) score each source sequence
    sourceSequences.forEach((sequence -> {
      try {
        entityRank.add(sequence, scoreMacro(sequence));
      } catch (Exception e) {
        log.warn("while scoring macro sequences", e);
      }
    }));

    // (3b) Avoid previous macro sequence
    if (!getFabricator().isInitialSegment()) try {
      entityRank.score(getFabricator().getSequenceOfChoice(getFabricator().getPreviousMacroChoice()).getId(), -SCORE_AVOID_PREVIOUS);
    } catch (CoreException e) {
      throw exception("Failed to get sequence of previous Macro choice, in order to choose next Macro", e);
    }

    // report
    getFabricator().putReport("macroChoice", entityRank.report());

    // (4) return the top choice
    try {
      macroSequence = entityRank.getTop();
    } catch (CoreException e) {
      throw exception("Found no macro-type sequence bound to Chain!", e);
    }

    return macroSequence;
  }

  /**
   Choose main sequence
   <p>
   *ONLY CHOOSES ONCE, then returns that choice every time.**

   @return main-type Sequence
   @throws CraftException on failure
   <p>
   future: don't we need to pass in the current pattern of the macro sequence?
   */
  private Sequence chooseMain() throws CraftException {
    if (Objects.nonNull(mainSequence))
      return mainSequence;

    EntityRank<Sequence> entityRank = new EntityRank<>();

    // future: only choose major sequences for major keys, minor for minor! [#223] Key of first Pattern of chosen Main-Sequence must match the `minor` or `major` with the Key of the current Segment.

    // (2) retrieve sequences bound to chain
    Collection<Sequence> sourceSequences;
    try {
      sourceSequences = getFabricator().getSourceMaterial().getSequencesOfType(SequenceType.Main);
    } catch (CoreException e) {
      throw exception("Failed to get source material for choosing Main", e);
    }

    // (3) score each source sequence based on meme isometry
    sourceSequences.forEach((sequence -> {
      try {
        entityRank.add(sequence, scoreMain(sequence));
      } catch (Exception e) {
        log.warn("while scoring main sequences", e);
      }
    }));

    // report
    getFabricator().putReport("mainChoice", entityRank.report());

    // (4) return the top choice
    try {
      mainSequence = entityRank.getTop();
    } catch (CoreException e) {
      throw exception("Found no main-type sequence bound to Chain!", e);
    }

    return mainSequence;
  }

  /**
   Score a candidate for next macro sequence, given current fabricator

   @param sequence to score
   @return score, including +/- entropy
   @throws CraftException on failure
   */
  private double scoreMacro(Sequence sequence) throws CraftException {
    double score = Chance.normallyAround(0, SCORE_MACRO_ENTROPY);

    if (getFabricator().isInitialSegment()) {
      return score;
    }

    // Score includes matching memes to previous segment's macro-sequence's next pattern
    try {
      score += getFabricator().getMemeIsometryOfNextPatternInPreviousMacro()
        .score(getFabricator().getSourceMaterial().getMemesAtBeginningOfSequence(sequence.getId())) * SCORE_MATCHED_MEMES;
    } catch (CoreException e) {
      throw exception("Failed to get source material for scoring Macro", e);
    }

    // Score includes matching mode (major/minor) to previous segment's macro-sequence's next pattern
    try {
      if (Key.isSameMode(getFabricator().getPreviousMacroNextOffset().getKey(), sequence.getKey())) {
        score += SCORE_MATCHED_KEY_MODE;
      }
    } catch (CoreException e) {
      throw exception("Failed to get previous macro at the next offset in order to score next Macro", e);
    }

    return score;
  }

  /**
   Score a candidate for next main sequence, given current fabricator

   @param sequence to score
   @return score, including +/- entropy
   @throws CraftException on failure
   */
  private double scoreMain(Sequence sequence) throws CraftException {
    double score = Chance.normallyAround(0, SCORE_MAIN_ENTROPY);

    if (!getFabricator().isInitialSegment()) {

      // Avoid previous main sequence
      try {
        if (Objects.equals(sequence.getId(), getFabricator().getSequenceOfChoice(getFabricator().getPreviousMainChoice()).getId())) {
          score -= SCORE_AVOID_PREVIOUS;
        }
      } catch (CoreException e) {
        throw exception("Failed to get previous main choice, in order to score next Main choice", e);
      }

      // Score includes matching mode, previous segment to macro sequence first pattern (major/minor)
      try {
        if (Key.isSameMode(getFabricator().getCurrentMacroOffset().getKey(), sequence.getKey())) {
          score += SCORE_MATCHED_KEY_MODE;
        }
      } catch (CoreException e) {
        throw exception("Failed to get current macro offset, in order to score next Main choice", e);
      }
    }

    // Score includes matching memes, previous segment to macro sequence first pattern
    try {
      score += getFabricator().getMemeIsometryOfCurrentMacro()
        .score(getFabricator().getSourceMaterial().getMemesAtBeginningOfSequence(sequence.getId())) * SCORE_MATCHED_MEMES;
    } catch (CoreException e) {
      throw exception("Failed to get memes at beginning of sequence, in order to score next Main choice", e);
    }

    return score;
  }

  /**
   all memes of all choices for the segment.
   cache results in fabricator, to avoid race condition causing [#153888310] During craft, instruments should be chosen based on combined memes of all chosen sequences for that segment.

   @return map of meme name to SegmentMeme entity
   */
  private Collection<SegmentMeme> segmentMemes() {
    Multiset<String> uniqueResults = ConcurrentHashMultiset.create();
    for (Choice choice : getFabricator().getSegment().getChoices()) {
      try {
        for (Meme meme : getFabricator().getMemesOfChoice(choice)) {
          uniqueResults.add(meme.getName());
        }
      } catch (CoreException e) {
        log.warn("Failed to get memes of choice: {}", choice);
      }
    }
    Collection<SegmentMeme> result = Lists.newArrayList();
    uniqueResults.elementSet().forEach(memeName -> result.add(SegmentMeme.of(memeName)));
    return result;
  }

  /**
   Get Segment length, in nanoseconds

   @return segment length, in nanoseconds
   @throws CraftException on failure
   */
  private long segmentLengthNanos() throws CraftException {
    try {
      return (long) (getFabricator().computeSecondsAtPosition(chooseMainPattern().getTotal()) * NANOS_PER_SECOND);
    } catch (CoreException e) {
      throw exception("Failed to compute seconds at position", e);
    }
  }

  /**
   Get Segment End Timestamp
   Segment Length Time = Segment Tempo (time per Beat) * Segment Length (# Beats)

   @return end timestamp
   @throws CraftException on failure
   */
  private Timestamp segmentEndTimestamp() throws CraftException {
    return Timestamp.from(getFabricator().getSegment().getBeginAt().toInstant().plusNanos(segmentLengthNanos()));
  }

  /**
   Compute the final key of the current segment
   Segment Key is the transposed key of the current main pattern

   @return key
   @throws CraftException on failure
   */
  private String segmentKey() throws CraftException {
    String mainKey = chooseMainPattern().getKey();
    if (null == mainKey || mainKey.isEmpty()) {
      mainKey = getMainSequence().getKey();
    }
    return Key.of(mainKey).transpose(getMainTranspose()).getFullDescription();
  }

  /**
   Compute the final tempo of the current segment

   @return tempo
   @throws CraftException on failure
   */
  private double segmentTempo() throws CraftException {
    return (Value.eitherOr(chooseMacroPattern().getTempo(), getMacroSequence().getTempo()) +
      Value.eitherOr(chooseMainPattern().getTempo(), getMainSequence().getTempo())) / 2;
  }

  /**
   Compute the final density of the current segment
   future: Segment Density = average of macro and main-sequence patterns

   @return density
   @throws CraftException on failure
   */
  private Double segmentDensity() throws CraftException {
    return (Value.eitherOr(chooseMacroPattern().getDensity(), getMacroSequence().getDensity()) +
      Value.eitherOr(chooseMainPattern().getDensity(), getMainSequence().getDensity())) / 2;
  }

}
