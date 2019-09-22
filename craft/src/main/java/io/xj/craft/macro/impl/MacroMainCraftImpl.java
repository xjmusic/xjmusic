// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro.impl;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.isometry.SuperEntityRank;
import io.xj.core.model.entity.MemeEntity;
import io.xj.core.model.program.Program;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.SegmentChord;
import io.xj.core.model.segment.sub.SegmentMeme;
import io.xj.core.util.Chance;
import io.xj.core.util.Value;
import io.xj.craft.CraftImpl;
import io.xj.craft.exception.CraftException;
import io.xj.craft.macro.MacroMainCraft;
import io.xj.music.Key;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

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
  private final Collection<FabricatorType> typesContinueMacro = ImmutableList.of(FabricatorType.Continue, FabricatorType.NextMain);

  @Inject
  public MacroMainCraftImpl(
    @Assisted("basis") Fabricator fabricator
    /*-*/) {
    this.fabricator = fabricator;
  }

  /**
   compute Transpose Main-Program to the transposed key of the current macro pattern

   @param macroProgram   from which to compute transpose of main program
   @param macroTranspose from which to compute transpose of main program
   @param mainProgram    from which to compute transpose
   @param macroSequence  from which to compute transpose
   @return mainTranspose
   */
  private static Integer computeMainTranspose(Program macroProgram, int macroTranspose, Program mainProgram, Sequence macroSequence) {
    return Key.delta(mainProgram.getKey(),
      Value.eitherOr(macroSequence.getKey(), macroProgram.getKey()),
      macroTranspose);
  }

  /**
   Compute the final key of the current segment
   Segment Key is the transposed key of the current main pattern

   @param mainSequence  from which to compute key
   @param mainTranspose semitones
   @return key
   */
  private static String computeSegmentKey(Sequence mainSequence, int mainTranspose) {
    String mainKey = mainSequence.getKey();
    if (null == mainKey || mainKey.isEmpty()) {
      mainKey = mainSequence.getKey();
    }
    return Key.of(mainKey).transpose(mainTranspose).getFullDescription();
  }

  /**
   Compute the final tempo of the current segment

   @param macroSequence from which to compute segment tempo
   @param mainSequence  from which to compute segment tempo
   @return tempo
   */
  private static double computeSegmentTempo(Sequence macroSequence, Sequence mainSequence) {
    return (Value.eitherOr(macroSequence.getTempo(), macroSequence.getTempo()) +
      Value.eitherOr(mainSequence.getTempo(), mainSequence.getTempo())) / 2;
  }

  /**
   Compute the final density of the current segment
   future: Segment Density = average of macro and main-sequence patterns

   @param macroSequence from which to compute segment tempo
   @param mainSequence  from which to compute segment tempo
   @return density
   */
  private static Double computeSegmentDensity(Sequence macroSequence, Sequence mainSequence) {
    return (Value.eitherOr(macroSequence.getDensity(), macroSequence.getDensity()) +
      Value.eitherOr(mainSequence.getDensity(), mainSequence.getDensity())) / 2;
  }

  @Override
  public void doWork() throws CraftException {
    try {
      // 1. Macro
      Optional<Sequence> nextSequenceOfPreviousMacroProgram = chooseNextSequenceOfPreviousMacroProgram();
      Program macroProgram = nextSequenceOfPreviousMacroProgram.isPresent()
        ? chooseMacroProgram(nextSequenceOfPreviousMacroProgram.get())
        : chooseMacroProgram();
      Long macroSequenceBindingOffset = computeMacroProgramSequenceBindingOffset();
      SequenceBinding macroSequenceBinding = macroProgram.randomlySelectSequenceBindingAtOffset(macroSequenceBindingOffset);
      Sequence macroSequence = macroProgram.getSequence(macroSequenceBinding.getSequenceId());
      int macroTranspose = nextSequenceOfPreviousMacroProgram.isPresent()
        ? computeMacroTranspose(macroProgram, nextSequenceOfPreviousMacroProgram.get())
        : 0;
      fabricator.add(
        new Choice()
          .setProgramId(macroProgram.getId())
          .setType(ProgramType.Macro.toString())
          .setTranspose(macroTranspose)
          .setSequenceBindingId(macroSequenceBinding.getId()));

      // 2. Main
      Program mainProgram = chooseMainProgram();
      Long mainSequenceBindingOffset = computeMainProgramSequenceBindingOffset();
      SequenceBinding mainSequenceBinding = mainProgram.randomlySelectSequenceBindingAtOffset(mainSequenceBindingOffset);
      Sequence mainSequence = mainProgram.getSequence(mainSequenceBinding.getSequenceId());
      int mainTranspose = computeMainTranspose(macroProgram, macroTranspose, mainProgram, macroSequence);
      fabricator.add(
        new Choice()
          .setProgramId(mainProgram.getId())
          .setType(ProgramType.Main.toString())
          .setTranspose(mainTranspose)
          .setSequenceBindingId(mainSequenceBinding.getId()));

      // 3. Chords
      mainProgram.getChordsOfSequence(mainSequence.getId()).forEach(sequenceChord -> {
        // [#154090557] don't create chord past end of Segment
        String name = "NaN";
        if (sequenceChord.getPosition() < mainSequence.getTotal()) try {
          // delta the chord name
          name = sequenceChord.toMusical().transpose(mainTranspose).getFullDescription();
          // create the transposed chord
          fabricator.add(new SegmentChord()
            .setName(name)
            .setPosition(sequenceChord.getPosition()));

        } catch (Exception e) {
          log.warn("failed to create transposed segment chord {}@{}",
            name, sequenceChord.getPosition(), e);
        }
      });

      // 4. Memes
      segmentMemes().forEach((segmentMeme) -> {
        try {
          fabricator.add(segmentMeme);

        } catch (Exception e) {
          log.warn("Could not create segment meme {}", segmentMeme.getName(), e);
        }
      });

      // Finally, update the segment with fabricated content
      fabricator.getSegment()
        .setDensity(computeSegmentDensity(macroSequence, mainSequence))
        .setTempo(computeSegmentTempo(macroSequence, mainSequence))
        .setKey(computeSegmentKey(mainSequence, mainTranspose))
        .setTotal(mainSequence.getTotal())
        .setEndAtInstant(segmentEndInstant(mainSequence));
      fabricator.updateSegment();

    } catch (CoreException e) {
      throw exception("Failed to do Macro+Main Craft work!", e);
    }
  }

  /**
   compute the macroTranspose

   @param macroProgram      to compute transpose of
   @param macroNextSequence to base choice on (never actually used, because next macro first sequence overlaps it)
   @return macroTranspose
   */
  private Integer computeMacroTranspose(Program macroProgram, Sequence macroNextSequence) throws CraftException {
    try {
      switch (fabricator.getType()) {

        case Initial:
          return 0;

        case Continue:
        case NextMain:
          return fabricator.getPreviousMacroChoice().getTranspose();

        case NextMacro:
          if (Objects.nonNull(macroNextSequence))
            return Key.delta(macroProgram.getKey(), macroNextSequence.getKey(),
              fabricator.getPreviousMacroChoice().getTranspose());
          else
            return Key.delta(macroProgram.getKey(), fabricator.getProgram(fabricator.getPreviousMacroChoice()).getKey(),
              fabricator.getPreviousMacroChoice().getTranspose());

        default:
          throw exception("unable to determine macro-type program transposition!");
      }

    } catch (CoreException e) {
      throw exception("Failed to get Macro Transpose", e);
    }
  }

  /**
   compute the macroSequenceBindingOffset

   @return macroSequenceBindingOffset
   */
  private Long computeMacroProgramSequenceBindingOffset() throws CraftException {
    try {
      switch (fabricator.getType()) {

        case Initial:
        case NextMacro:
          return 0L;

        case Continue:
          return fabricator.getSequenceBindingOffsetForChoice(fabricator.getPreviousMacroChoice());

        case NextMain:
          return fabricator.getNextSequenceBindingOffset(fabricator.getPreviousMacroChoice());

        default:
          throw exception(String.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
      }

    } catch (CoreException e) {
      throw exception("Failed to get Macro Pattern Offset", e);
    }
  }

  /**
   compute the mainSequenceBindingOffset

   @return mainSequenceBindingOffset
   */
  private Long computeMainProgramSequenceBindingOffset() throws CraftException {
    try {
      switch (fabricator.getType()) {

        case Initial:
        case NextMain:
        case NextMacro:
          return 0L;

        case Continue:
          return fabricator.getNextSequenceBindingOffset(fabricator.getPreviousMainChoice());

        default:
          throw exception(String.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
      }

    } catch (CoreException e) {
      throw exception("Failed to get Main Pattern Offset", e);
    }
  }

  /**
   Choose the next sequence for the previous segment's macro choice, which we use to base the current macro choice on

   @return next sequence in previous segment's macro choice, or null if none exists
   */
  private Optional<Sequence> chooseNextSequenceOfPreviousMacroProgram() {
    try {
      Choice previousMacroChoice = fabricator.getPreviousMacroChoice();
      Program previousMacroProgram = fabricator.getProgram(previousMacroChoice);
      if (fabricator.hasOneMoreSequenceBindingOffset(previousMacroChoice))
        return Optional.of(previousMacroProgram.getSequence(
          previousMacroProgram.randomlySelectSequenceBindingAtOffset(
            fabricator.getNextSequenceBindingOffset(previousMacroChoice)).getSequenceId()));
    } catch (Exception ignored) {
    }

    return Optional.empty();
  }

  /**
   Choose macro program

   @param macroNextSequence to base choice on (never actually used, because next macro first sequence overlaps it)
   @return macro-type program
   @throws CraftException on failure
   */
  private Program chooseMacroProgram(Sequence macroNextSequence) throws CraftException {
    // if continuing the macro program, use the same one
    try {
      if (typesContinueMacro.contains(fabricator.getType()))
        return fabricator.getProgram(fabricator.getPreviousMacroChoice());
    } catch (CoreException e) {
      throw exception("Failed to get Macro Program", e);
    }

    // will rank all possibilities, and choose the next macro program
    SuperEntityRank<Program> superEntityRank = new SuperEntityRank<>();

    // (1) retrieve programs bound to chain
    Collection<Program> sourcePrograms = fabricator.getSourceMaterial().getProgramsOfType(ProgramType.Macro);

    // (3) score each source program
    sourcePrograms.forEach((program -> {
      try {
        superEntityRank.add(program, scoreMacro(program, macroNextSequence));
      } catch (Exception e) {
        log.warn("while scoring macro programs", e);
      }
    }));

    // (3b) Avoid previous macro program
    if (!fabricator.isInitialSegment()) try {
      superEntityRank.score(fabricator.getProgram(fabricator.getPreviousMacroChoice()).getId(), -SCORE_AVOID_PREVIOUS);
    } catch (CoreException e) {
      throw exception("Failed to get program of previous Macro choice, in order to choose next Macro", e);
    }

    // report
    fabricator.putReport("macroChoice", superEntityRank.report());

    // (4) return the top choice
    try {
      return superEntityRank.getTop();
    } catch (CoreException e) {
      throw exception("Found no macro-type program bound to Chain!", e);
    }
  }

  /**
   Choose first macro program, completely at random\

   @return macro-type program
   @throws CraftException on failure
   */
  private Program chooseMacroProgram() throws CraftException {
    SuperEntityRank<Program> superEntityRank = new SuperEntityRank<>();

    fabricator.getSourceMaterial().getProgramsOfType(ProgramType.Macro).forEach(program -> superEntityRank.add(program, Chance.normallyAround(0, SCORE_MACRO_ENTROPY)));

    // (3b) Avoid previous macro program
    if (!fabricator.isInitialSegment()) try {
      superEntityRank.score(fabricator.getProgram(fabricator.getPreviousMacroChoice()).getId(), -SCORE_AVOID_PREVIOUS);
    } catch (CoreException e) {
      throw exception("Failed to get program of previous Macro choice, in order to choose next Macro", e);
    }

    try {
      return superEntityRank.getTop();
    } catch (CoreException e) {
      throw exception("Found no macro-type program bound to Chain!", e);
    }
  }

  /**
   Choose main program
   <p>
   ONLY CHOOSES ONCE, then returns that choice every time.**

   @return main-type Program
   @throws CraftException on failure
   <p>
   future: don't we need to pass in the current pattern of the macro program?
   */
  private Program chooseMainProgram() throws CraftException {
    // if continuing the macro program, use the same one
    try {
      if (FabricatorType.Continue == fabricator.getType())
        return fabricator.getProgram(fabricator.getPreviousMainChoice());
    } catch (CoreException e) {
      throw exception("Failed to get Macro Program", e);
    }

    // will rank all possibilities, and choose the next main program
    // future: only choose major programs for major keys, minor for minor! [#223] Key of first Pattern of chosen Main-Program must match the `minor` or `major` with the Key of the current Segment.
    SuperEntityRank<Program> superEntityRank = new SuperEntityRank<>();

    // (2) retrieve programs bound to chain
    Collection<Program> sourcePrograms = fabricator.getSourceMaterial().getProgramsOfType(ProgramType.Main);

    // (3) score each source program based on meme isometry
    sourcePrograms.forEach((program -> {
      try {
        superEntityRank.add(program, scoreMainProgram(program));
      } catch (Exception e) {
        log.warn("while scoring main programs", e);
      }
    }));

    // report
    fabricator.putReport("mainChoice", superEntityRank.report());

    // (4) return the top choice
    try {
      return superEntityRank.getTop();
    } catch (CoreException e) {
      throw exception("Found no main-type program bound to Chain!", e);
    }
  }

  /**
   Score a candidate for next macro program, given current fabricator

   @param program           to score
   @param macroNextSequence to base choice on (never actually used, because next macro first sequence overlaps it)
   @return score, including +/- entropy
   @throws CraftException on failure
   */
  private double scoreMacro(Program program, Sequence macroNextSequence) throws CraftException {
    double score = Chance.normallyAround(0, SCORE_MACRO_ENTROPY);

    if (fabricator.isInitialSegment()) {
      return score;
    }

    // Score includes matching memes to previous segment's macro-program's next pattern
    try {
      score += fabricator.getMemeIsometryOfNextSequenceInPreviousMacro()
        .score(program.getMemesAtBeginning()) * SCORE_MATCHED_MEMES;
    } catch (CoreException e) {
      throw exception("Failed to get source material for scoring Macro", e);
    }

    // Score includes matching mode (major/minor) to previous segment's macro-program's next pattern
    if (Objects.nonNull(macroNextSequence) && Key.isSameMode(macroNextSequence.getKey(), program.getKey())) {
      score += SCORE_MATCHED_KEY_MODE;
    }

    return score;
  }

  /**
   Score a candidate for next main program, given current fabricator

   @param program to score
   @return score, including +/- entropy
   @throws CraftException on failure
   */
  private double scoreMainProgram(Program program) throws CraftException {
    double score = Chance.normallyAround(0, SCORE_MAIN_ENTROPY);

    if (!fabricator.isInitialSegment()) {

      // Avoid previous main program
      try {
        if (Objects.equals(program.getId(), fabricator.getProgram(fabricator.getPreviousMainChoice()).getId())) {
          score -= SCORE_AVOID_PREVIOUS;
        }
      } catch (CoreException e) {
        throw exception("Failed to get previous main choice, in order to score next Main choice", e);
      }

      // Score includes matching mode, previous segment to macro program first pattern (major/minor)
      try {
        if (Key.isSameMode(fabricator.getKeyForChoice(fabricator.getPreviousMainChoice()), program.getKey())) {
          score += SCORE_MATCHED_KEY_MODE;
        }
      } catch (CoreException e) {
        throw exception("Failed to get current macro offset, in order to score next Main choice", e);
      }
    }

    // Score includes matching memes, previous segment to macro program first pattern
    try {
      score += fabricator.getMemeIsometryOfCurrentMacro()
        .score(program.getMemesAtBeginning()) * SCORE_MATCHED_MEMES;
    } catch (CoreException e) {
      throw exception("Failed to get memes at beginning of program, in order to score next Main choice", e);
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
    for (Choice choice : fabricator.getSegment().getChoices()) {
      try {
        for (MemeEntity meme : fabricator.getMemesOfChoice(choice)) {
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

   @param mainSequence the end of which marks the end of the segment
   @return segment length, in nanoseconds
   @throws CraftException on failure
   */
  private long segmentLengthNanos(Sequence mainSequence) throws CraftException {
    try {
      return (long) (fabricator.computeSecondsAtPosition(mainSequence.getTotal()) * NANOS_PER_SECOND);
    } catch (CoreException e) {
      throw exception("Failed to compute seconds at position", e);
    }
  }

  /**
   Get Segment End Timestamp
   Segment Length Time = Segment Tempo (time per Beat) * Segment Length (# Beats)

   @param mainSequence from which to compute segment length
   @return end timestamp
   @throws CraftException on failure
   */
  private Instant segmentEndInstant(Sequence mainSequence) throws CraftException {
    return fabricator.getSegment().getBeginAt().plusNanos(segmentLengthNanos(mainSequence));
  }

}
