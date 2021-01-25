// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.macro_main;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.lib.entity.EntityException;
import io.xj.lib.music.Key;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.client.HubClientException;
import io.xj.service.nexus.craft.CraftException;
import io.xj.service.nexus.fabricator.EntityScorePicker;
import io.xj.service.nexus.fabricator.FabricationException;
import io.xj.service.nexus.fabricator.FabricationWrapperImpl;
import io.xj.service.nexus.fabricator.Fabricator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MacroMainCraftImpl extends FabricationWrapperImpl implements MacroMainCraft {
  private static final double SCORE_MATCHED_KEY_MODE = 2;
  private static final double SCORE_MATCHED_MEMES = 10;
  private static final double SCORE_AVOID_PREVIOUS = -5;
  private static final double SCORE_DIRECTLY_BOUND = 100;
  private static final double SCORE_MACRO_ENTROPY = 0.5;
  private static final double SCORE_MAIN_ENTROPY = 0.5;
  private static final long NANOS_PER_SECOND = 1_000_000_000;
  private final Logger log = LoggerFactory.getLogger(MacroMainCraftImpl.class);
  private final Collection<Segment.Type> typesContinueMacro = ImmutableList.of(Segment.Type.Continue, Segment.Type.NextMain);

  @Inject
  public MacroMainCraftImpl(
    @Assisted("basis") Fabricator fabricator
    /*-*/) {
    this.fabricator = fabricator;
  }

  /**
   compute Transpose Main-Program to the transposed key of the current macro pattern
   <p>
   [#175548549] Program and Instrument parameters to turn off transposition and tonality.

   @param macroProgram   of which to compute transpose of main program
   @param macroTranspose of which to compute transpose of main program
   @param mainProgram    of which to compute transpose
   @param macroSequence  of which to compute transpose
   @return mainTranspose
   */
  private Integer computeMainTranspose(Program macroProgram, int macroTranspose, Program mainProgram, ProgramSequence macroSequence) {
    try {
      return
        fabricator.getProgramConfig(mainProgram).doTranspose() ?
          Key.delta(mainProgram.getKey(),
            Value.eitherOr(macroSequence.getKey(), macroProgram.getKey()),
            macroTranspose) :
          0;

    } catch (ValueException e) {
      log.error("Failed to compute main transpose; will skip transposition.", e);
    }
    return 0;
  }

  /**
   Compute the final key of the current segment
   Segment Key is the transposed key of the current main pattern

   @param mainSequence  of which to compute key
   @param mainTranspose semitones
   @return key
   */
  private static String computeSegmentKey(ProgramSequence mainSequence, int mainTranspose) {
    String mainKey = mainSequence.getKey();
    if (null == mainKey || mainKey.isEmpty()) {
      mainKey = mainSequence.getKey();
    }
    return Key.of(mainKey).transpose(mainTranspose).getFullDescription();
  }

  /**
   Compute the final tempo of the current segment

   @param macroSequence of which to compute segment tempo
   @param mainSequence  of which to compute segment tempo
   @return tempo
   */
  private static double computeSegmentTempo(ProgramSequence macroSequence, ProgramSequence mainSequence) {
    return (Value.eitherOr(macroSequence.getTempo(), macroSequence.getTempo()) +
      Value.eitherOr(mainSequence.getTempo(), mainSequence.getTempo())) / 2;
  }

  /**
   Compute the final density of the current segment
   future: Segment Density = average of macro and main-sequence patterns

   @param macroSequence of which to compute segment tempo
   @param mainSequence  of which to compute segment tempo
   @return density
   */
  private static Double computeSegmentDensity(ProgramSequence macroSequence, ProgramSequence mainSequence) {
    return (Value.eitherOr(macroSequence.getDensity(), macroSequence.getDensity()) +
      Value.eitherOr(mainSequence.getDensity(), mainSequence.getDensity())) / 2;
  }

  @Override
  public void doWork() throws CraftException {
    try {
      // 1. Macro Program chosen based on previous if possible
      // [#176375076] MacroMainCraft should tolerate and retry zero entities
      // When these conditions are encountered, log the error in a Segment Message, and broaden the query parameters. Worst case, pick completely at random from the library.
      Optional<ProgramSequence> nextSequenceOfPreviousMacroProgram = chooseNextSequenceOfPreviousMacroProgram();
      Program macroProgram;
      if (nextSequenceOfPreviousMacroProgram.isEmpty())
        macroProgram = chooseMacroProgram();
      else try {
        macroProgram = chooseMacroProgram(nextSequenceOfPreviousMacroProgram.get());
      } catch (CraftException e) {
        reportMissing(Program.class,
          String.format("Macro-type failed to choose based on previous because %s", e.getMessage()));
        macroProgram = chooseMacroProgram();
      }

      Long macroSequenceBindingOffset = computeMacroProgramSequenceBindingOffset();
      var macroSequenceBinding = fabricator.randomlySelectSequenceBindingAtOffset(macroProgram, macroSequenceBindingOffset);
      var macroSequence = fabricator.getSourceMaterial().getProgramSequence(macroSequenceBinding);
      int macroTranspose = nextSequenceOfPreviousMacroProgram.isPresent()
        ? computeMacroTranspose(macroProgram, nextSequenceOfPreviousMacroProgram.get())
        : 0;
      fabricator.add(
        SegmentChoice.newBuilder()
          .setId(UUID.randomUUID().toString())
          .setSegmentId(fabricator.getSegment().getId())
          .setProgramId(macroProgram.getId())
          .setProgramType(Program.Type.Macro)
          .setTranspose(macroTranspose)
          .setProgramSequenceBindingId(macroSequenceBinding.getId())
          .build());

      // 2. Main
      Program mainProgram = chooseMainProgram();
      Long mainSequenceBindingOffset = computeMainProgramSequenceBindingOffset();
      var mainSequenceBinding = fabricator.randomlySelectSequenceBindingAtOffset(mainProgram, mainSequenceBindingOffset);
      var mainSequence = fabricator.getSourceMaterial().getProgramSequence(mainSequenceBinding);
      int mainTranspose = computeMainTranspose(macroProgram, macroTranspose, mainProgram, macroSequence);
      fabricator.add(
        SegmentChoice.newBuilder()
          .setId(UUID.randomUUID().toString())
          .setSegmentId(fabricator.getSegment().getId())
          .setProgramId(mainProgram.getId())
          .setProgramType(Program.Type.Main)
          .setTranspose(mainTranspose)
          .setProgramSequenceBindingId(mainSequenceBinding.getId())
          .build());

      // 3. Chords and voicings
      fabricator.getSourceMaterial().getChords(mainSequence).forEach(sequenceChord -> {
        // [#154090557] don't of chord past end of Segment
        String name = "NaN";
        if (sequenceChord.getPosition() < mainSequence.getTotal()) try {
          // delta the chord name
          name = new io.xj.lib.music.Chord(sequenceChord.getName())
            .transpose(mainTranspose).getFullDescription();
          // of the transposed chord
          SegmentChord chord = fabricator.add(SegmentChord.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setSegmentId(fabricator.getSegment().getId())
            .setPosition(sequenceChord.getPosition())
            .setName(name)
            .build());
          for (var voicing : fabricator.getSourceMaterial().getVoicings(sequenceChord))
            fabricator.add(SegmentChordVoicing.newBuilder()
              .setId(UUID.randomUUID().toString())
              .setSegmentId(fabricator.getSegment().getId())
              .setSegmentChordId(chord.getId())
              .setType(voicing.getType())
              .setNotes(voicing.getNotes())
              .build());


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

      // Update the segment with fabricated content
      fabricator.updateSegment(fabricator.getSegment().toBuilder()
        .setOutputEncoder(fabricator.getChainConfig().getOutputContainer())
        .setDensity(computeSegmentDensity(macroSequence, mainSequence))
        .setTempo(computeSegmentTempo(macroSequence, mainSequence))
        .setKey(computeSegmentKey(mainSequence, mainTranspose))
        .setTotal(mainSequence.getTotal())
        .build());
      // then, set the end-at time.
      fabricator.updateSegment(fabricator.getSegment().toBuilder()
        .setEndAt(Value.formatIso8601UTC(segmentEndInstant(mainSequence)))
        .build());
      fabricator.done();

    } catch (FabricationException e) {
      throw exception("Failed to do Macro-Main-Craft Work", e);

    } catch (Exception e) {
      throw exception("Bad failure", e);
    }
  }

  /**
   compute the macroTranspose
   <p>
   [#175548549] Program and Instrument parameters to turn off transposition and tonality.

   @param macroProgram      to compute transpose of
   @param macroNextSequence to base choice on (never actually used, because next macro first sequence overlaps it)
   @return macroTranspose
   */
  private Integer computeMacroTranspose(Program macroProgram, ProgramSequence macroNextSequence) throws CraftException {
    try {
      if (!fabricator.getProgramConfig(macroProgram).doTranspose()) return 0;

    } catch (ValueException e) {
      log.error("Failed to compute macro transpose; will skip transposition.", e);
      return 0;
    }

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

    } catch (FabricationException e) {
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

    } catch (FabricationException e) {
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

    } catch (FabricationException e) {
      throw exception("Failed to get Main Pattern Offset", e);
    }
  }

  /**
   Choose the next sequence for the previous segment's macro choice, which we use to base the current macro choice on

   @return next sequence in previous segment's macro choice, or null if none exists
   */
  private Optional<ProgramSequence> chooseNextSequenceOfPreviousMacroProgram() {
    try {
      SegmentChoice previousMacroChoice = fabricator.getPreviousMacroChoice();
      Program previousMacroProgram = fabricator.getProgram(previousMacroChoice);
      if (fabricator.hasOneMoreSequenceBindingOffset(previousMacroChoice))
        return Optional.of(fabricator.getSourceMaterial().getProgramSequence(
          fabricator.randomlySelectSequenceBindingAtOffset(
            previousMacroProgram,
            fabricator.getNextSequenceBindingOffset(previousMacroChoice))));
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
  private Program chooseMacroProgram(ProgramSequence macroNextSequence) throws CraftException {
    // if continuing the macro program, use the same one
    try {
      if (typesContinueMacro.contains(fabricator.getType()))
        return fabricator.getProgram(fabricator.getPreviousMacroChoice());
    } catch (FabricationException e) {
      throw exception("Failed to get Macro Program", e);
    }

    // will rank all possibilities, and choose the next macro program
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    // (1) retrieve programs bound to chain and
    // (3) score each source program
    try {
      for (Program program : fabricator.getSourceMaterial().getProgramsOfType(Program.Type.Macro))
        superEntityScorePicker.add(program, scoreMacro(program, macroNextSequence));
    } catch (Exception e) {
      log.warn("while scoring macro programs", e);
    }

    // (3b) Avoid previous macro program
    if (!fabricator.isInitialSegment())
      try {
        superEntityScorePicker.score(fabricator.getProgram(fabricator.getPreviousMacroChoice()).getId(), SCORE_AVOID_PREVIOUS);
      } catch (FabricationException e) {
        throw exception("Failed to get program create previous Macro choice, in order to choose next Macro", e);
      }

    // report
    fabricator.putReport("macroChoice", superEntityScorePicker.report());

    // (4) return the top choice
    try {
      return superEntityScorePicker.getTop();
    } catch (FabricationException e) {
      throw exception("Found no macro-type program bound to Chain!", e);
    }
  }

  /**
   Choose first macro program, completely at random

   @return macro-type program
   @throws CraftException on failure
   */
  private Program chooseMacroProgram() throws CraftException {
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    try {
      for (Program program : fabricator.getSourceMaterial().getProgramsOfType(Program.Type.Macro))
        superEntityScorePicker.add(program, Chance.normallyAround(0, SCORE_MACRO_ENTROPY));
    } catch (HubClientException e) {
      throw exception("score macro entropy", e);
    }

    // (3b) Avoid previous macro program
    if (!fabricator.isInitialSegment()) try {
      superEntityScorePicker.score(fabricator.getProgram(fabricator.getPreviousMacroChoice()).getId(), SCORE_AVOID_PREVIOUS);
    } catch (FabricationException e) {
      throw exception("Failed to get program create previous Macro choice, in order to choose next Macro", e);
    }

    try {
      return superEntityScorePicker.getTop();
    } catch (FabricationException e) {
      throw exception("Found no macro-type program bound to Chain!", e);
    }
  }

  /**
   Choose main program
   <p>
   ONLY CHOOSES ONCE, then returns that choice every time

   @return main-type Program
   @throws CraftException on failure
   <p>
   future: don't we need to pass in the current pattern of the macro program?
   */
  private Program chooseMainProgram() throws CraftException {
    // if continuing the macro program, use the same one
    try {
      if (Segment.Type.Continue == fabricator.getType())
        return fabricator.getProgram(fabricator.getPreviousMainChoice());
    } catch (FabricationException e) {
      throw exception("Failed to get Macro Program", e);
    }

    // will rank all possibilities, and choose the next main program
    // future: only choose major programs for major keys, minor for minor! [#223] Key of first Pattern of chosen Main-Program must match the `minor` or `major` with the Key of the current Segment.
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve programs bound to chain and
    // (3) score each source program based on meme isometry
    try {
      for (Program program : fabricator.getSourceMaterial().getProgramsOfType(Program.Type.Main))
        superEntityScorePicker.add(program, scoreMain(program));
    } catch (Exception e) {
      log.warn("while scoring main programs", e);
    }

    // report
    fabricator.putReport("mainChoice", superEntityScorePicker.report());

    // (4) return the top choice
    try {
      return superEntityScorePicker.getTop();
    } catch (FabricationException e) {
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
  private double scoreMacro(Program program, ProgramSequence macroNextSequence) throws CraftException {
    double score = Chance.normallyAround(0, SCORE_MACRO_ENTROPY);

    if (fabricator.isInitialSegment()) {
      return score;
    }

    // Score includes matching memes to previous segment's macro-program's next pattern
    try {
      score += fabricator.getMemeIsometryOfNextSequenceInPreviousMacro()
        .score(fabricator.getSourceMaterial().getMemesAtBeginning(program)) * SCORE_MATCHED_MEMES;
    } catch (FabricationException | HubClientException | EntityException e) {
      throw exception("Failed to get source material for scoring Macro", e);
    }

    // Score includes matching mode (major/minor) to previous segment's macro-program's next pattern
    if (Objects.nonNull(macroNextSequence) && Key.isSameMode(macroNextSequence.getKey(), program.getKey()))
      score += SCORE_MATCHED_KEY_MODE;

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECTLY_BOUND;

    return score;
  }

  /**
   Score a candidate for next main program, given current fabricator

   @param program to score
   @return score, including +/- entropy
   @throws CraftException on failure
   */
  private double scoreMain(Program program) throws CraftException {
    double score = Chance.normallyAround(0, SCORE_MAIN_ENTROPY);

    if (!fabricator.isInitialSegment()) {

      // Avoid previous main program
      try {
        if (Objects.equals(program.getId(), fabricator.getProgram(fabricator.getPreviousMainChoice()).getId())) {
          score += SCORE_AVOID_PREVIOUS;
        }
      } catch (FabricationException e) {
        throw exception("Failed to get previous main choice, in order to score next Main choice", e);
      }

      // Score includes matching mode, previous segment to macro program first pattern (major/minor)
      try {
        if (Key.isSameMode(fabricator.getKeyForChoice(fabricator.getPreviousMainChoice()), Key.of(program.getKey()))) {
          score += SCORE_MATCHED_KEY_MODE;
        }
      } catch (FabricationException e) {
        throw exception("Failed to get current macro offset, in order to score next Main choice", e);
      }
    }

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECTLY_BOUND;

    // Score includes matching memes, previous segment to macro program first pattern
    try {
      score += fabricator.getMemeIsometryOfCurrentMacro()
        .score(fabricator.getSourceMaterial().getMemesAtBeginning(program)) * SCORE_MATCHED_MEMES;

    } catch (FabricationException | HubClientException | EntityException e) {
      throw exception("Failed to get memes at beginning create program, in order to score next Main choice", e);
    }

    return score;
  }

  /**
   all memes of all choices for the segment.
   cache results in fabricator, to avoid race condition causing [#153888310] During craft, instruments should be chosen based on combined memes of all chosen sequences for that segment.

   @return map of meme name to SegmentMeme entity
   */
  private Collection<SegmentMeme> segmentMemes() throws FabricationException {
    Multiset<String> uniqueResults = ConcurrentHashMultiset.create();
    for (SegmentChoice choice : fabricator.getChoices()) {
      try {
        for (SegmentMeme meme : fabricator.getMemesOfChoice(choice)) {
          uniqueResults.add(meme.getName());
        }
      } catch (FabricationException e) {
        log.warn("Failed to get memes create choice: {}", choice);
      }
    }
    Collection<SegmentMeme> result = Lists.newArrayList();
    uniqueResults.elementSet().forEach(memeName -> result.add(
      SegmentMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(fabricator.getSegment().getId())
        .setName(memeName)
        .build()));
    return result;
  }

  /**
   Get Segment length, in nanoseconds

   @param mainSequence the end of which marks the end of the segment
   @return segment length, in nanoseconds
   @throws CraftException on failure
   */
  private long segmentLengthNanos(ProgramSequence mainSequence) throws CraftException {
    try {
      return (long) (fabricator.computeSecondsAtPosition(mainSequence.getTotal()) * NANOS_PER_SECOND);
    } catch (FabricationException e) {
      throw exception("Failed to compute seconds at position", e);
    }
  }

  /**
   Get Segment End Timestamp
   Segment Length Time = Segment Tempo (time per Beat) * Segment Length (# Beats)

   @param mainSequence of which to compute segment length
   @return end timestamp
   @throws CraftException on failure
   */
  private Instant segmentEndInstant(ProgramSequence mainSequence) throws CraftException {
    return Instant.parse(fabricator.getSegment().getBeginAt()).plusNanos(segmentLengthNanos(mainSequence));
  }
}
