// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import datadog.trace.api.Trace;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceChord;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Key;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Value;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.EntityScorePicker;
import io.xj.nexus.fabricator.FabricationWrapperImpl;
import io.xj.nexus.fabricator.Fabricator;

import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MacroMainCraftImpl extends FabricationWrapperImpl implements MacroMainCraft {
  private static final double SCORE_MATCH = 1000;
  private static final double SCORE_AVOID = -SCORE_MATCH * 2;
  private static final double SCORE_DIRECT = 10 * SCORE_MATCH;
  private static final double SCORE_MACRO_ENTROPY = 0.5;
  private static final double SCORE_MAIN_ENTROPY = 0.5;
  private static final long NANOS_PER_SECOND = 1_000_000_000;
  private final ApiUrlProvider apiUrlProvider;

  @Inject
  public MacroMainCraftImpl(
    @Assisted("basis") Fabricator fabricator,
    ApiUrlProvider apiUrlProvider
  ) {
    super(fabricator);
    this.apiUrlProvider = apiUrlProvider;
  }

  @Override
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "doWork")
  public void doWork() throws NexusException {
    var macroProgram = chooseNextMacroProgram()
      .orElseThrow(() -> new NexusException("Failed to choose a Macro-program by any means!"));

    Long macroSequenceBindingOffset = computeMacroProgramSequenceBindingOffset();
    var macroSequenceBinding = fabricator.randomlySelectSequenceBindingAtOffset(macroProgram, macroSequenceBindingOffset)
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to determine sequence binding offset for macro Program \"%s\" %s",
        macroProgram.getName(),
        apiUrlProvider.getAppUrl(String.format("/programs/%s", macroProgram.getId()))
      )));
    var macroSequence = fabricator.getSourceMaterial().getProgramSequence(macroSequenceBinding);
    fabricator.add(
      SegmentChoice.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(fabricator.getSegment().getId())
        .setProgramId(macroProgram.getId())
        .setProgramType(Program.Type.Macro)
        .setProgramSequenceBindingId(macroSequenceBinding.getId())
        .build());

    // 2. Main
    Program mainProgram = chooseMainProgram()
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to choose main program based on macro Program \"%s\" at offset %s %s",
        macroProgram.getName(),
        macroSequenceBindingOffset,
        apiUrlProvider.getAppUrl(String.format("/programs/%s", macroProgram.getId()))
      )));
    Long mainSequenceBindingOffset = computeMainProgramSequenceBindingOffset();
    var mainSequenceBinding = fabricator.randomlySelectSequenceBindingAtOffset(mainProgram, mainSequenceBindingOffset)
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to determine sequence binding offset for main Program \"%s\" %s",
        mainProgram.getName(),
        apiUrlProvider.getAppUrl(String.format("/programs/%s", mainProgram.getId()))
      )));
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
    if (mainSequence.isPresent())
      for (ProgramSequenceChord sequenceChord : fabricator.getProgramSequenceChords(mainSequence.get())) {
        // [#154090557] don't of chord past end of Segment
        String name;
        if (sequenceChord.getPosition() < mainSequence.get().getTotal()) {
          // delta the chord name
          name = new Chord(sequenceChord.getName()).getFullDescription();
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
        }
      }

    // 4. Memes
    for (SegmentMeme segmentMeme : segmentMemes())
      fabricator.add(segmentMeme);

    // Update the segment with fabricated content
    if (macroSequence.isPresent() && mainSequence.isPresent())
      fabricator.updateSegment(fabricator.getSegment().toBuilder()
        .setOutputEncoder(fabricator.getChainConfig().getOutputContainer())
        .setDensity(computeSegmentDensity(macroSequence.get(), mainSequence.get()))
        .setTempo(computeSegmentTempo(macroSequence.get(), mainSequence.get()))
        .setKey(computeSegmentKey(mainSequence.get()).strip())
        .setTotal(mainSequence.get().getTotal())
        .build());

    // then, set the end-at time.
    if (mainSequence.isPresent())
      fabricator.updateSegment(fabricator.getSegment().toBuilder()
        .setEndAt(Value.formatIso8601UTC(segmentEndInstant(mainSequence.get())))
        .build());

    // done
    fabricator.done();
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
  private Long computeMacroProgramSequenceBindingOffset() throws NexusException {
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
        throw new NexusException(String.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
    }
  }

  /**
   compute the mainSequenceBindingOffset

   @return mainSequenceBindingOffset
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "computeMainProgramSequenceBindingOffset")
  private Long computeMainProgramSequenceBindingOffset() throws NexusException {
    switch (fabricator.getType()) {

      case Initial:
      case NextMain:
      case NextMacro:
        return 0L;

      case Continue:
        var previousMainChoice = fabricator.getPreviousMainChoice();
        if (previousMainChoice.isEmpty())
          throw new NexusException("Cannot get retrieve previous main choice");
        return fabricator.getNextSequenceBindingOffset(previousMainChoice.get());

      default:
        throw new NexusException(String.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
    }

  }

  /**
   Choose macro program

   @return macro-type program
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "chooseMacroProgram")
  public Optional<Program> chooseNextMacroProgram() {
    if (fabricator.isInitialSegment()) return chooseRandomMacroProgram();

    // if continuing the macro program, use the same one
    var previousMacroChoice = fabricator.getPreviousMacroChoice();
    if (fabricator.continuesMacroProgram() && previousMacroChoice.isPresent())
      return fabricator.getProgram(previousMacroChoice.get());

    // will rank all possibilities, and choose the next macro program
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    // (1) retrieve programs bound to chain and
    // (3) score each source program
    for (Program program : fabricator.getSourceMaterial().getProgramsOfType(Program.Type.Macro))
      superEntityScorePicker.add(program, scoreMacro(program));

    // (3b) Avoid previous macro program
    if (previousMacroChoice.isPresent()) {
      var program = fabricator.getProgram(previousMacroChoice.get());
      program.ifPresent(value -> superEntityScorePicker.score(value.getId(), SCORE_AVOID));
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
  public Optional<Program> chooseRandomMacroProgram() {
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    for (Program program : fabricator.getSourceMaterial().getProgramsOfType(Program.Type.Macro))
      superEntityScorePicker.add(program, Chance.normallyAround(0, SCORE_MACRO_ENTROPY));

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
    for (Program program : fabricator.getSourceMaterial().getProgramsOfType(Program.Type.Main))
      superEntityScorePicker.add(program, scoreMain(program));

    // report
    fabricator.putReport("mainChoice", superEntityScorePicker.report());

    // (4) return the top choice
    return superEntityScorePicker.getTop();
  }

  /**
   Score a candidate for next macro program, given current fabricator

   @param program to score
   @return score, including +/- entropy
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "scoreMacro")
  private double scoreMacro(Program program) {
    double score = Chance.normallyAround(0, SCORE_MACRO_ENTROPY);

    if (fabricator.isInitialSegment()) {
      return score;
    }

    // Score includes matching memes to previous segment's macro-program's next pattern
    score += fabricator.getMemeIsometryOfNextSequenceInPreviousMacro()
      .score(fabricator.getSourceMaterial().getMemesAtBeginning(program)) * SCORE_MATCH;

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECT;

    return score;
  }

  /**
   Score a candidate for next main program, given current fabricator

   @param program to score
   @return score, including +/- entropy
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "scoreMain")
  private double scoreMain(Program program) {
    double score = Chance.normallyAround(0, SCORE_MAIN_ENTROPY);

    if (!fabricator.isInitialSegment()) {
      var previousMainChoice = fabricator.getPreviousMainChoice();

      // Avoid previous main program
      if (previousMainChoice.isPresent()) {
        var previousMainProgram = fabricator.getProgram(previousMainChoice.get());
        if (previousMainProgram.isPresent())
          if (Objects.equals(program.getId(), previousMainProgram.get().getId()))
            score += SCORE_AVOID;
      }
    }

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECT;

    // Score includes matching memes, previous segment to macro program first pattern
    score += fabricator.getMemeIsometryOfCurrentMacro()
      .score(fabricator.getSourceMaterial().getMemesAtBeginning(program)) * SCORE_MATCH;

    return score;
  }

  /**
   all memes of all choices for the segment.
   cache results in fabricator, to avoid race condition causing [#153888310] During craft, instruments should be chosen based on combined memes of all chosen sequences for that segment.

   @return map of meme name to SegmentMeme entity
   */
  @Trace(resourceName = "nexus/craft/macro_main", operationName = "segmentMemes")
  private Collection<SegmentMeme> segmentMemes() throws NexusException {
    Multiset<String> uniqueResults = ConcurrentHashMultiset.create();
    for (SegmentChoice choice : fabricator.getChoices())
      for (SegmentMeme meme : fabricator.getMemesOfChoice(choice))
        uniqueResults.add(meme.getName());
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
   @throws NexusException on failure
   */
  private long segmentLengthNanos(ProgramSequence mainSequence) throws NexusException {
    return (long) (fabricator.computeSecondsAtPosition(mainSequence.getTotal()) * NANOS_PER_SECOND);
  }

  /**
   Get Segment End Timestamp
   Segment Length Time = Segment Tempo (time per Beat) * Segment Length (# Beats)

   @param mainSequence of which to compute segment length
   @return end timestamp
   @throws NexusException on failure
   */
  private Instant segmentEndInstant(ProgramSequence mainSequence) throws NexusException {
    return Instant.parse(fabricator.getSegment().getBeginAt()).plusNanos(segmentLengthNanos(mainSequence));
  }
}
