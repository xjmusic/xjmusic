// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro.impl;

import io.xj.core.exception.CraftException;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.entity.EntityRank;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.util.Chance;
import io.xj.core.util.Value;
import io.xj.craft.basis.Basis;
import io.xj.craft.macro.MacroMainCraft;
import io.xj.music.Key;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MacroMainCraftImpl implements MacroMainCraft {
  private static final double SCORE_MATCHED_KEY_MODE = 2;
  private static final double SCORE_MATCHED_MEMES = 10;
  private static final double SCORE_AVOID_PREVIOUS = 5;
  private static final double SCORE_MACRO_ENTROPY = 0.5;
  private static final double SCORE_MAIN_ENTROPY = 0.5;
  private static final long NANOS_PER_SECOND = 1_000_000_000;
  private final Logger log = LoggerFactory.getLogger(MacroMainCraftImpl.class);
  private final Basis basis;
  private Sequence _macroSequence;
  private Sequence _mainSequence;
  private BigInteger _macroPatternOffset;
  private BigInteger _mainPatternOffset;

  @Inject
  public MacroMainCraftImpl(
    @Assisted("basis") Basis basis
    /*-*/) {
    this.basis = basis;
  }

  @Override
  public void doWork() throws Exception {
    try {
      craftMacro();
      craftMain();
      craftMemes();
      basis.updateSegment(basis.segment()
        .setDensity(segmentDensity())
        .setTempo(segmentTempo())
        .setKey(segmentKey())
        .setTotal(segmentTotal())
        .setEndAtTimestamp(segmentEndTimestamp()));
      craftChords();
      report();

    } catch (CraftException e) {
      throw e;
    } catch (Exception e) {
      throw new CraftException(
        String.format("Failed to do %s-type MacroMainCraft for segment #%s",
          basis.type(), basis.segment().getId()), e);
    }
  }

  /**
   Report
   */
  private void report() {
    // future: basis.report() anything else interesting from the craft operation
  }

  /**
   Make Macro-type Sequence Choice
   add macro-sequence choice to segment

   @throws Exception on any failure
   */
  private void craftMacro() throws Exception {
    basis.create(new Choice()
      .setSegmentId(basis.segment().getId())
      .setType(SequenceType.Macro.toString())
      .setSequenceId(macroSequence().getId())
      .setTranspose(macroTranspose())
      .setSequencePatternOffset(macroPatternOffset()));
  }

  /**
   Make Main-type Sequence Choice
   add macro-sequence choice to segment

   @throws Exception on any failure
   */
  private void craftMain() throws Exception {
    basis.create(new Choice()
      .setSegmentId(basis.segment().getId())
      .setType(SequenceType.Main.toString())
      .setSequenceId(mainSequence().getId())
      .setTranspose(mainTranspose())
      .setSequencePatternOffset(mainPatternOffset()));
  }

  /**
   Make Memes
   add all memes to segment

   @throws Exception on any failure
   */
  private void craftMemes() throws Exception {
    segmentMemes().forEach((segmentMeme) -> {
      try {
        basis.create(segmentMeme);

      } catch (Exception e) {
        log.warn("failed to create segment meme {}", segmentMeme.getName(), e);
      }
    });
  }

  /**
   Make Chords
   Segment Chords = Main Sequence Pattern Chords, transposed according to to main sequence choice
   [#154090557] don't create chord past end of Segment

   @throws Exception on any failure
   */
  private void craftChords() throws Exception {
    basis.ingest().patternChords(mainPattern().getId())
      .forEach(patternChord -> {

        if (patternChord.getPosition() < basis.segment().getTotal()) {
          String name = "NaN";
          try {
            // delta the chord name
            name = patternChord.toMusical().transpose(mainTranspose()).getFullDescription();
            // create the transposed chord
            basis.create(new SegmentChord()
              .setSegmentId(basis.segment().getId())
              .setName(name)
              .setPosition(patternChord.getPosition()));

          } catch (Exception e) {
            log.warn("failed to create transposed segment chord {}@{}",
              String.valueOf(name), patternChord.getPosition(), e);
          }
        }
      });
  }

  /**
   compute (and cache) the chosen macro sequence

   @return macro-type sequence
   @throws Exception on failure
   */
  private Sequence macroSequence() throws Exception {
    if (Objects.isNull(_macroSequence))
      switch (basis.type()) {

        case Initial:
          _macroSequence = chooseMacro();
          break;

        case Continue:
        case NextMain:
          Choice previousChoice = basis.previousMacroChoice();
          if (Objects.isNull(previousChoice))
            throw new CraftException(String.format("No macro-type sequence chosen in segment previous to segment #%s!", basis.segment().getId()));
          _macroSequence = basis.ingest().sequence(previousChoice.getSequenceId());
          break;

        case NextMacro:
          _macroSequence = chooseMacro();
      }
    return _macroSequence;
  }

  /**
   compute (and cache) the mainSequence

   @return mainSequence
   */
  private Sequence mainSequence() throws Exception {
    if (Objects.isNull(_mainSequence))
      switch (basis.type()) {

        case Continue:
          Choice previousChoice = basis.previousMainChoice();
          if (Objects.isNull(previousChoice))
            throw new CraftException(String.format("No main-type sequence chosen in segment previous to segment #%s!", basis.segment().getId()));
          _mainSequence = basis.ingest().sequence(previousChoice.getSequenceId());
          break;

        case Initial:
        case NextMain:
        case NextMacro:
          _mainSequence = chooseMain();
      }


    return _mainSequence;
  }

  /**
   compute (and cache) the macroTranspose

   @return macroTranspose
   */
  private Integer macroTranspose() throws Exception {
    switch (basis.type()) {

      case Initial:
        return 0;

      case Continue:
      case NextMain:
        return basis.previousMacroChoice().getTranspose();

      case NextMacro:
        return Key.delta(macroSequence().getKey(),
          basis.previousMacroNextPattern().getKey(),
          basis.previousMacroChoice().getTranspose());

      default:
        throw new CraftException(String.format("unable to determine macro-type sequence transposition for segment #%s!", basis.segment().getId()));
    }
  }

  /**
   compute (and cache) Transpose Main-Sequence to the transposed key of the current macro pattern

   @return mainTranspose
   */
  private Integer mainTranspose() throws Exception {
    return Key.delta(mainSequence().getKey(),
      Value.eitherOr(macroPattern().getKey(), macroSequence().getKey()),
      macroTranspose());
  }

  /**
   compute (and cache) the macroPatternOffset

   @return macroPatternOffset
   */
  private BigInteger macroPatternOffset() throws Exception {
    if (Objects.isNull(_macroPatternOffset))
      switch (basis.type()) {

        case Initial:
          _macroPatternOffset = BigInteger.valueOf(0);
          break;

        case Continue:
          _macroPatternOffset = basis.previousMacroChoice().getSequencePatternOffset();
          break;

        case NextMain:
          _macroPatternOffset = basis.previousMacroChoice().nextPatternOffset();
          break;

        case NextMacro:
          _macroPatternOffset = BigInteger.valueOf(0);
      }

    return _macroPatternOffset;
  }

  /**
   compute (and cache) the mainPatternOffset

   @return mainPatternOffset
   */
  private BigInteger mainPatternOffset() throws Exception {
    if (Objects.isNull(_mainPatternOffset))
      switch (basis.type()) {

        case Initial:
          _mainPatternOffset = BigInteger.valueOf(0);
          break;

        case Continue:
          _mainPatternOffset = basis.previousMainChoice().nextPatternOffset();
          break;

        case NextMain:
          _mainPatternOffset = BigInteger.valueOf(0);
          break;

        case NextMacro:
          _mainPatternOffset = BigInteger.valueOf(0);
      }

    return _mainPatternOffset;
  }

  /**
   Fetch current pattern of macro-type sequence

   @return pattern
   @throws Exception on failure
   */
  private Pattern macroPattern() throws Exception {
    Pattern pattern = basis.ingest().patternAtOffset(macroSequence().getId(), macroPatternOffset(), PatternType.Macro);

    if (Objects.isNull(pattern))
      throw new CraftException(String.format("failed to determine at least one candidate %s-type pattern in sequence #%s chosen for segment #%s!",
        PatternType.Macro, macroSequence().getId(), basis.segment().getId()));

    return pattern;
  }

  /**
   Fetch current pattern of main-type sequence

   @return pattern
   @throws Exception on failure
   */
  private Pattern mainPattern() throws Exception {
    Pattern pattern = basis.ingest().patternAtOffset(mainSequence().getId(), mainPatternOffset(), PatternType.Main);

    if (Objects.isNull(pattern))
      throw new CraftException(String.format("failed to determine at least one candidate %s-type pattern in sequence #%s chosen for segment #%s!",
        PatternType.Main, mainSequence().getId(), basis.segment().getId()));

    return pattern;
  }

  /**
   Choose macro sequence

   @return macro-type sequence
   @throws Exception on failure
   */
  private Sequence chooseMacro() throws Exception {
    EntityRank<Sequence> entityRank = new EntityRank<>();

    // (1a) retrieve sequences bound directly to chain
    Collection<Sequence> sourceSequences = basis.ingest().sequences(SequenceType.Macro);

    // (1b) only if none were found in the previous transpose, retrieve sequences bound to chain library
    if (sourceSequences.isEmpty())
      sourceSequences = basis.libraryIngest().sequences(SequenceType.Macro);

    // (3) score each source sequence
    sourceSequences.forEach((sequence -> {
      try {
        entityRank.add(sequence, scoreMacro(sequence));
      } catch (Exception e) {
        log.warn("while scoring macro sequences", e);
      }
    }));

    // (3b) Avoid previous macro sequence
    if (!basis.isInitialSegment())
      entityRank.score(basis.previousMacroChoice().getSequenceId(), -SCORE_AVOID_PREVIOUS);

    // report
    basis.report("macroChoice", entityRank.report());

    // (4) return the top choice
    Sequence sequence = entityRank.getTop();
    if (Objects.nonNull(sequence))
      return sequence;
    else
      throw new CraftException("Found no macro-type sequence bound to Chain!");
  }

  /**
   Choose main sequence

   @return main-type Sequence
   @throws Exception on failure
   <p>
   future: don't we need to pass in the current pattern of the macro sequence?
   */
  private Sequence chooseMain() throws Exception {
    EntityRank<Sequence> entityRank = new EntityRank<>();

    // future: only choose major sequences for major keys, minor for minor! [#223] Key of first Pattern of chosen Main-Sequence must match the `minor` or `major` with the Key of the current Segment.

    // (2a) retrieve sequences bound directly to chain
    Collection<Sequence> sourceSequences = basis.ingest().sequences(SequenceType.Main);

    // (2b) only if none were found in the previous transpose, retrieve sequences bound to chain library
    if (sourceSequences.isEmpty())
      sourceSequences = basis.libraryIngest().sequences(SequenceType.Main);

    // (3) score each source sequence based on meme isometry
    sourceSequences.forEach((sequence -> {
      try {
        entityRank.add(sequence, scoreMain(sequence));
      } catch (Exception e) {
        log.warn("while scoring main sequences", e);
      }
    }));

    // report
    basis.report("mainChoice", entityRank.report());

    // (4) return the top choice
    Sequence sequence = entityRank.getTop();
    if (Objects.nonNull(sequence))
      return sequence;
    else
      throw new CraftException("Found no main-type sequence bound to Chain!");
  }

  /**
   Score a candidate for next macro sequence, given current basis

   @param sequence to score
   @return score, including +/- entropy
   @throws Exception on failure
   */
  private double scoreMacro(Sequence sequence) throws Exception {
    double score = Chance.normallyAround(0, SCORE_MACRO_ENTROPY);

    if (basis.isInitialSegment()) {
      return score;
    }

    // Score includes matching memes to previous segment's macro-sequence's next pattern (major/minor)
    score += basis.previousMacroNextPatternMemeIsometry().score(
      basis.ingest().sequenceAndPatternMemes(sequence.getId(), BigInteger.valueOf(0), PatternType.Macro))
      * SCORE_MATCHED_MEMES;

    // Score includes matching mode to previous segment's macro-sequence's next pattern (major/minor)
    Pattern patternAtOffset = basis.ingest().patternAtOffset(sequence.getId(), BigInteger.valueOf(0), PatternType.Macro);
    if (Objects.nonNull(patternAtOffset) && Key.isSameMode(basis.previousMacroNextPattern().getKey(), patternAtOffset.getKey())) {
      score += SCORE_MATCHED_KEY_MODE;
    }

    return score;
  }

  /**
   Score a candidate for next main sequence, given current basis

   @param sequence to score
   @return score, including +/- entropy
   @throws Exception on failure
   */
  private double scoreMain(Sequence sequence) throws Exception {
    double score = Chance.normallyAround(0, SCORE_MAIN_ENTROPY);

    if (!basis.isInitialSegment()) {

      // Avoid previous main sequence
      if (Objects.equals(sequence.getId(), basis.previousMainChoice().getSequenceId())) {
        score -= SCORE_AVOID_PREVIOUS;
      }

      // Score includes matching mode, previous segment to macro sequence first pattern (major/minor)
      if (Key.isSameMode(basis.currentMacroPattern().getKey(), sequence.getKey())) {
        score += SCORE_MATCHED_KEY_MODE;
      }
    }

    // Score includes matching memes, previous segment to macro sequence first pattern
    score += basis.currentMacroMemeIsometry().score(
      basis.ingest().sequenceAndPatternMemes(sequence.getId(), BigInteger.valueOf(0), PatternType.Main))
      * SCORE_MATCHED_MEMES;

    return score;
  }

  /**
   prepare map of final segment memes

   @return map of meme name to SegmentMeme entity
   */
  private Collection<SegmentMeme> segmentMemes() throws Exception {
    Map<String, SegmentMeme> uniqueResults = Maps.newHashMap();

    basis.ingest().sequenceMemes(macroSequence().getId())
      .forEach(meme -> uniqueResults.put(
        meme.getName(), SegmentMeme.of(basis.segment().getId(), meme.getName())));

    basis.ingest().patternMemes(macroPattern().getId())
      .forEach(meme -> uniqueResults.put(
        meme.getName(), SegmentMeme.of(basis.segment().getId(), meme.getName())));

    basis.ingest().sequenceMemes(mainSequence().getId())
      .forEach(meme -> uniqueResults.put(
        meme.getName(), SegmentMeme.of(basis.segment().getId(), meme.getName())));

    basis.ingest().patternMemes(mainPattern().getId())
      .forEach(meme -> uniqueResults.put(
        meme.getName(), SegmentMeme.of(basis.segment().getId(), meme.getName())));

    Collection<SegmentMeme> result = Lists.newArrayList();
    uniqueResults.forEach((key, val) -> result.add(val));

    // cache results in basis, to avoid race condition causing [#153888310] During craft, instruments should be chosen based on combined memes of all chosen sequences for that segment.
    basis.setSegmentMemes(result);

    return result;
  }

  /**
   Get Segment length, in nanoseconds
   If a previous segment exists, the tempo is averaged with its tempo, because the tempo will increase at a linear rate from start to finish.

   @return segment length, in nanoseconds
   @throws Exception on failure
   */
  private long segmentLengthNanos() throws Exception {
    return (long) (basis.secondsAtPosition(segmentTotal()) * NANOS_PER_SECOND);
  }

  /**
   Get Segment End Timestamp
   Segment Length Time = Segment Tempo (time per Beat) * Segment Length (# Beats)

   @return end timestamp
   @throws CraftException on failure
   */
  private Timestamp segmentEndTimestamp() throws Exception {
    return Timestamp.from(basis.segmentBeginAt().toInstant().plusNanos(segmentLengthNanos()));
  }

  /**
   Compute the total # of beats of the current segment
   Segment Total (# Beats) = from current Pattern of Main-Sequence

   @return # beats total
   @throws Exception on failure
   */
  private Integer segmentTotal() throws Exception {
    return mainPattern().getTotal();
  }

  /**
   Compute the final key of the current segment
   Segment Key is the transposed key of the current main pattern

   @return key
   @throws Exception on failure
   */
  private String segmentKey() throws Exception {
    String mainKey = mainPattern().getKey();
    if (null == mainKey || mainKey.isEmpty()) {
      mainKey = mainSequence().getKey();
    }
    return Key.of(mainKey).transpose(mainTranspose()).getFullDescription();
  }

  /**
   Compute the final tempo of the current segment

   @return tempo
   @throws Exception on failure
   */
  private double segmentTempo() throws Exception {
    return (Value.eitherOr(macroPattern().getTempo(), macroSequence().getTempo()) +
      Value.eitherOr(mainPattern().getTempo(), mainSequence().getTempo())) / 2;
  }

  /**
   Compute the final density of the current segment
   future: Segment Density = average of macro and main-sequence patterns

   @return density
   @throws Exception on failure
   */
  private Double segmentDensity() throws Exception {
    return (Value.eitherOr(macroPattern().getDensity(), macroSequence().getDensity()) +
      Value.eitherOr(mainPattern().getDensity(), mainSequence().getDensity())) / 2;
  }

}
