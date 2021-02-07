// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.macro_main;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import datadog.trace.api.Trace;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.lib.music.Key;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Value;
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
  private static final double SCORE_AVOID_PREVIOUS = -20; // must exceed SCORE_MATCHED_MEMES
  private static final double SCORE_DIRECTLY_BOUND = 100;
  private static final double SCORE_MACRO_ENTROPY = 0.5;
  private static final double SCORE_MAIN_ENTROPY = 0.5;
  private static final long NANOS_PER_SECOND = 1_000_000_000;
  private final Logger log = LoggerFactory.getLogger(MacroMainCraftImpl.class);

  @Inject
  public MacroMainCraftImpl(
    @Assisted("basis") Fabricator fabricator
    /*-*/) {
    this.fabricator = fabricator;
  }

  @Override
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "doWork")
  public void doWork() throws CraftException {
    try {
      // 1. Macro Program chosen based on previous if possible
      // [#176375076] MacroMainCraft should tolerate and retry zero entities
      // When these conditions are encountered, log the error in a Segment Message, and broaden the query parameters. Worst case, pick completely at random from the library.
      Optional<ProgramSequence> nextSequenceOfPreviousMacroProgram = chooseNextSequenceOfPreviousMacroProgram();

      Optional<Program> macroProgram =
        nextSequenceOfPreviousMacroProgram.isPresent() ?
          chooseNextMacroProgram(nextSequenceOfPreviousMacroProgram.get()) :
          chooseNextMacroProgram();

      if (macroProgram.isEmpty())
        macroProgram = chooseNextMacroProgram();

      if (macroProgram.isEmpty())
        throw exception("Failed to choose a Macro-program by any means!");

      Long macroSequenceBindingOffset = computeMacroProgramSequenceBindingOffset();
      var macroSequenceBinding = fabricator.randomlySelectSequenceBindingAtOffset(macroProgram.get(), macroSequenceBindingOffset)
        .orElseThrow(() -> exception("Unable to determine macro sequence binding"));
      var macroSequence = fabricator.getSourceMaterial().getProgramSequence(macroSequenceBinding);
      fabricator.add(
        SegmentChoice.newBuilder()
          .setId(UUID.randomUUID().toString())
          .setSegmentId(fabricator.getSegment().getId())
          .setProgramId(macroProgram.get().getId())
          .setProgramType(Program.Type.Macro)
          .setProgramSequenceBindingId(macroSequenceBinding.getId())
          .build());

      // 2. Main
      Program mainProgram = chooseMainProgram()
        .orElseThrow(() -> exception("Unable to choose main program"));
      Long mainSequenceBindingOffset = computeMainProgramSequenceBindingOffset();
      var mainSequenceBinding = fabricator.randomlySelectSequenceBindingAtOffset(mainProgram, mainSequenceBindingOffset)
        .orElseThrow(() -> exception("Unable to determine main sequence binding offset"));
      var mainSequence = fabricator.getSourceMaterial().getProgramSequence(mainSequenceBinding);
      fabricator.add(
        SegmentChoice.newBuilder()
          .setId(UUID.randomUUID().toString())
          .setSegmentId(fabricator.getSegment().getId())
          .setProgramId(mainProgram.getId())
          .setProgramType(Program.Type.Main)
          .setProgramSequenceBindingId(mainSequenceBinding.getId())
          .build());

      // 3. Chords and voicings
      mainSequence.ifPresent(programSequence ->
        fabricator.getSourceMaterial().getChords(programSequence).forEach(sequenceChord -> {
          // [#154090557] don't of chord past end of Segment
          String name = "NaN";
          if (sequenceChord.getPosition() < programSequence.getTotal()) try {
            // delta the chord name
            name = new io.xj.lib.music.Chord(sequenceChord.getName()).getFullDescription();
            // of the final chord
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
            log.warn("failed to create segment chord {}@{}",
              name, sequenceChord.getPosition(), e);
          }
        }));

      // 4. Memes
      segmentMemes().forEach((segmentMeme) -> {
        try {
          fabricator.add(segmentMeme);

        } catch (Exception e) {
          log.warn("Could not create segment meme {}", segmentMeme.getName(), e);
        }
      });

      // Update the segment with fabricated content
      if (macroSequence.isPresent() && mainSequence.isPresent())
        fabricator.updateSegment(fabricator.getSegment().toBuilder()
          .setOutputEncoder(fabricator.getChainConfig().getOutputContainer())
          .setDensity(computeSegmentDensity(macroSequence.get(), mainSequence.get()))
          .setTempo(computeSegmentTempo(macroSequence.get(), mainSequence.get()))
          .setKey(computeSegmentKey(mainSequence.get()))
          .setTotal(mainSequence.get().getTotal())
          .build());

      // then, set the end-at time.
      if (mainSequence.isPresent())
        fabricator.updateSegment(fabricator.getSegment().toBuilder()
          .setEndAt(Value.formatIso8601UTC(segmentEndInstant(mainSequence.get())))
          .build());

      // done
      fabricator.done();

    } catch (FabricationException e) {
      throw exception("Failed to do Macro-Main-Craft Work", e);

    } catch (Exception e) {
      throw exception("Bad failure", e);
    }
  }

  /**
   Compute the final key of the current segment
   Segment Key is the key of the current main program sequence

   @param mainSequence of which to compute key
   @return key
   */
  private static String computeSegmentKey(ProgramSequence mainSequence) {
    String mainKey = mainSequence.getKey();
    if (null == mainKey || mainKey.isEmpty()) {
      mainKey = mainSequence.getKey();
    }
    return Key.of(mainKey).getFullDescription();
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

  /**
   compute the macroSequenceBindingOffset

   @return macroSequenceBindingOffset
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "computeMacroProgramSequenceBindingOffset")
  private Long computeMacroProgramSequenceBindingOffset() throws CraftException {
    var previousMacroChoice = fabricator.getPreviousMacroChoice();
    switch (fabricator.getType()) {

      case Initial:
      case NextMacro:
        return 0L;

      case Continue:
        return previousMacroChoice.isPresent() ?
          fabricator.getSequenceBindingOffsetForChoice(previousMacroChoice.get()) : 0L;

      case NextMain:
        return previousMacroChoice.isPresent() ?
          fabricator.getNextSequenceBindingOffset(previousMacroChoice.get()) : 0L;

      default:
        throw exception(String.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
    }
  }

  /**
   compute the mainSequenceBindingOffset

   @return mainSequenceBindingOffset
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "computeMainProgramSequenceBindingOffset")
  private Long computeMainProgramSequenceBindingOffset() throws CraftException {
    switch (fabricator.getType()) {

      case Initial:
      case NextMain:
      case NextMacro:
        return 0L;

      case Continue:
        var previousMainChoice = fabricator.getPreviousMainChoice();
        if (previousMainChoice.isEmpty())
          throw exception("Cannot get retrieve previous main choice");
        return fabricator.getNextSequenceBindingOffset(previousMainChoice.get());

      default:
        throw exception(String.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
    }

  }

  /**
   Choose the next sequence for the previous segment's macro choice, which we use to base the current macro choice on

   @return next sequence in previous segment's macro choice, or null if none exists
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "chooseNextSequenceOfPreviousMacroProgram")
  private Optional<ProgramSequence> chooseNextSequenceOfPreviousMacroProgram() {
    var previousMacroChoice = fabricator.getPreviousMacroChoice();
    if (previousMacroChoice.isEmpty()) return Optional.empty();
    Optional<Program> previousMacroProgram = fabricator.getProgram(previousMacroChoice.get());
    if (previousMacroProgram.isEmpty())
      return Optional.empty();
    var psb = fabricator.randomlySelectSequenceBindingAtOffset(
      previousMacroProgram.get(),
      fabricator.getNextSequenceBindingOffset(previousMacroChoice.get()));
    if (psb.isPresent() && fabricator.hasOneMoreSequenceBindingOffset(previousMacroChoice.get())) {
      return fabricator.getSourceMaterial().getProgramSequence(psb.get());
    }

    return Optional.empty();
  }

  /**
   Choose macro program

   @param macroNextSequence to base choice on (never actually used, because next macro first sequence overlaps it)
   @return macro-type program
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "chooseMacroProgram")
  public Optional<Program> chooseNextMacroProgram(ProgramSequence macroNextSequence) {
    // if continuing the macro program, use the same one
    var previousMacroChoice = fabricator.getPreviousMacroChoice();
    if (fabricator.continuesMacroProgram() && previousMacroChoice.isPresent())
      return fabricator.getProgram(previousMacroChoice.get());

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
    if (!fabricator.isInitialSegment()) {
      if (previousMacroChoice.isPresent()) {
        var program = fabricator.getProgram(previousMacroChoice.get());
        program.ifPresent(value -> superEntityScorePicker.score(value.getId(), SCORE_AVOID_PREVIOUS));
      }
    }

    // report
    fabricator.putReport("macroChoice", superEntityScorePicker.report());

    // (4) return the top choice
    return superEntityScorePicker.getTop();
  }

  /**
   Choose first macro program, completely at random

   @return macro-type program
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "chooseMacroProgram")
  public Optional<Program> chooseNextMacroProgram() {
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    for (Program program : fabricator.getSourceMaterial().getProgramsOfType(Program.Type.Macro))
      superEntityScorePicker.add(program, Chance.normallyAround(0, SCORE_MACRO_ENTROPY));

    // (3b) Avoid previous macro program
    if (!fabricator.isInitialSegment()) {
      var previousMacroChoice = fabricator.getPreviousMacroChoice();
      if (previousMacroChoice.isPresent()) {
        var program = fabricator.getProgram(previousMacroChoice.get());
        program.ifPresent(value -> superEntityScorePicker.score(value.getId(), SCORE_AVOID_PREVIOUS));
      }
    }

    return superEntityScorePicker.getTop();
  }

  /**
   Choose main program
   <p>
   ONLY CHOOSES ONCE, then returns that choice every time

   @return main-type Program
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "chooseMainProgram")
  private Optional<Program> chooseMainProgram() {
    // if continuing the macro program, use the same one
    var previousMainChoice = fabricator.getPreviousMainChoice();
    if (Segment.Type.Continue == fabricator.getType())
      if (previousMainChoice.isPresent())
        return fabricator.getProgram(previousMainChoice.get());

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
    return superEntityScorePicker.getTop();
  }

  /**
   Score a candidate for next macro program, given current fabricator

   @param program           to score
   @param macroNextSequence to base choice on (never actually used, because next macro first sequence overlaps it)
   @return score, including +/- entropy
   @throws CraftException on failure
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "scoreMacro")
  private double scoreMacro(Program program, ProgramSequence macroNextSequence) throws CraftException {
    double score = Chance.normallyAround(0, SCORE_MACRO_ENTROPY);

    if (fabricator.isInitialSegment()) {
      return score;
    }

    // Score includes matching memes to previous segment's macro-program's next pattern
    try {
      score += fabricator.getMemeIsometryOfNextSequenceInPreviousMacro()
        .score(fabricator.getSourceMaterial().getMemesAtBeginning(program)) * SCORE_MATCHED_MEMES;
    } catch (HubClientException e) {
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
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "scoreMain")
  private double scoreMain(Program program) throws CraftException {
    double score = Chance.normallyAround(0, SCORE_MAIN_ENTROPY);

    if (!fabricator.isInitialSegment()) try {
      var previousMainChoice = fabricator.getPreviousMainChoice();

      // Avoid previous main program
      if (previousMainChoice.isPresent()) {
        var previousMainProgram = fabricator.getProgram(previousMainChoice.get());
        if (previousMainProgram.isPresent())
          if (Objects.equals(program.getId(), previousMainProgram.get().getId()))
            score += SCORE_AVOID_PREVIOUS;
      }

      // Score includes matching mode, previous segment to macro program first pattern (major/minor)
      if (previousMainChoice.isPresent() &&
        Key.isSameMode(fabricator.getKeyForChoice(previousMainChoice.get()), Key.of(program.getKey())))
        score += SCORE_MATCHED_KEY_MODE;

    } catch (FabricationException e) {
      throw exception("Failed to get current macro offset, in order to score next Main choice", e);
    }

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECTLY_BOUND;

    // Score includes matching memes, previous segment to macro program first pattern
    try {
      score += fabricator.getMemeIsometryOfCurrentMacro()
        .score(fabricator.getSourceMaterial().getMemesAtBeginning(program)) * SCORE_MATCHED_MEMES;

    } catch (HubClientException e) {
      throw exception("Failed to get memes at beginning create program, in order to score next Main choice", e);
    }

    return score;
  }

  /**
   all memes of all choices for the segment.
   cache results in fabricator, to avoid race condition causing [#153888310] During craft, instruments should be chosen based on combined memes of all chosen sequences for that segment.

   @return map of meme name to SegmentMeme entity
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "segmentMemes")
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
