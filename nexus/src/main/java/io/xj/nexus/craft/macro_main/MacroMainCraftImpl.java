// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Key;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.EntityScorePicker;
import io.xj.nexus.fabricator.FabricationWrapperImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.MemeIsometry;
import io.xj.nexus.persistence.Segments;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static io.xj.lib.util.Values.NANOS_PER_SECOND;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MacroMainCraftImpl extends FabricationWrapperImpl implements MacroMainCraft {
  private static final double SCORE_MATCH = 1000;
  private static final double SCORE_AVOID = -SCORE_MATCH * 2;
  private static final double SCORE_DIRECT = 10 * SCORE_MATCH;
  private static final double SCORE_MACRO_ENTROPY = 1.0;
  private static final double SCORE_MAIN_ENTROPY = 1.0;
  private final ApiUrlProvider apiUrlProvider;

  @Inject
  public MacroMainCraftImpl(
    @Assisted("basis") Fabricator fabricator,
    ApiUrlProvider apiUrlProvider
  ) {
    super(fabricator);
    this.apiUrlProvider = apiUrlProvider;
  }

  /**
   Compute the final key of the current segment key, the key of the current main program sequence

   @param mainSequence of which to compute key
   @return key
   */
  private String computeSegmentKey(ProgramSequence mainSequence) {
    String mainKey = mainSequence.getKey();
    if (null == mainKey || mainKey.isEmpty())
      mainKey = fabricator.sourceMaterial().getProgram(mainSequence.getProgramId()).orElseThrow().getKey();
    return Key.of(mainKey).getFullDescription();
  }

  /**
   Compute the final tempo of the current segment

   @param macroSequence of which to compute segment tempo
   @param mainSequence  of which to compute segment tempo
   @return tempo
   */
  private double computeSegmentTempo(@Nullable ProgramSequence macroSequence, @Nullable ProgramSequence mainSequence) throws NexusException {
    @Nullable Float macroTempo =
      Objects.nonNull(macroSequence) ?
        (Objects.nonNull(macroSequence.getTempo()) ?
          macroSequence.getTempo()
          : fabricator.sourceMaterial().getProgram(macroSequence.getProgramId()).orElseThrow().getTempo())
        : null;
    @Nullable Float mainTempo =
      Objects.nonNull(mainSequence) ?
        (Objects.nonNull(mainSequence.getTempo()) ?
          mainSequence.getTempo()
          : fabricator.sourceMaterial().getProgram(mainSequence.getProgramId()).orElseThrow().getTempo())
        : null;
    if (Objects.nonNull(macroTempo) && Objects.nonNull(mainTempo))
      return (macroTempo + mainTempo) / 2;
    if (Objects.nonNull(macroTempo))
      return macroTempo;
    if (Objects.nonNull(mainTempo))
      return mainTempo;
    throw new NexusException("Failed to compute Segment Tempo!");
  }

  /**
   Compute the final density of the current segment
   future: Segment Density = average of macro and main-sequence patterns
   <p>
   Segment is assigned a density during macro-main craft. It's going to be used to determine a target # of perc loops
   Percussion Loops Alpha #179534065

   @param macroSequence of which to compute segment tempo
   @param mainSequence  of which to compute segment tempo
   @return density
   */
  private double computeSegmentDensity(Integer delta, @Nullable ProgramSequence macroSequence, @Nullable ProgramSequence mainSequence) throws NexusException {
    return Values.limitDecimalPrecision(Values.interpolate(
      fabricator.getTemplateConfig().getDensityFloor(),
      fabricator.getTemplateConfig().getDensityCeiling(),
      (double) delta / fabricator.getTemplateConfig().getMainProgramLengthMaxDelta(),
      computeDensity(macroSequence, mainSequence)
    ));
  }

  /**
   Compute the average density of the two given sequences

   @param macroSequence of which to compute segment tempo
   @param mainSequence  of which to compute segment tempo
   @return density
   */
  private Float computeDensity(@Nullable ProgramSequence macroSequence, @Nullable ProgramSequence mainSequence) throws NexusException {
    @Nullable Float macroDensity =
      Objects.nonNull(macroSequence) ?
        (Objects.nonNull(macroSequence.getDensity()) ?
          macroSequence.getDensity()
          : fabricator.sourceMaterial().getProgram(macroSequence.getProgramId()).orElseThrow().getDensity())
        : null;
    @Nullable Float mainDensity =
      Objects.nonNull(mainSequence) ?
        (Objects.nonNull(mainSequence.getDensity()) ?
          mainSequence.getDensity()
          : fabricator.sourceMaterial().getProgram(mainSequence.getProgramId()).orElseThrow().getDensity())
        : null;
    if (Objects.nonNull(macroDensity) && Objects.nonNull(mainDensity))
      return (macroDensity + mainDensity) / 2;
    if (Objects.nonNull(macroDensity))
      return macroDensity;
    if (Objects.nonNull(mainDensity))
      return mainDensity;
    throw new NexusException("Failed to compute Density!");
  }

  @Override
  public void doWork() throws NexusException {
    var macroProgram = fabricator.addMemes(chooseNextMacroProgram()
      .orElseThrow(() -> new NexusException("Failed to choose a Macro-program by any means!")));
    Integer macroSequenceBindingOffset = computeMacroProgramSequenceBindingOffset();
    var macroSequenceBinding = fabricator.addMemes(fabricator.getRandomlySelectedSequenceBindingAtOffset(macroProgram, macroSequenceBindingOffset)
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to determine sequence binding offset for macro Program \"%s\" %s",
        macroProgram.getName(),
        apiUrlProvider.getAppUrl(String.format("/programs/%s", macroProgram.getId()))
      ))));
    var macroSequence = fabricator.sourceMaterial().getProgramSequence(macroSequenceBinding);
    var macroChoice = new SegmentChoice();
    macroChoice.setId(UUID.randomUUID());
    macroChoice.setSegmentId(fabricator.getSegment().getId());
    macroChoice.setProgramSequenceId(macroSequence.orElseThrow().getId());
    macroChoice.setProgramId(macroProgram.getId());
    macroChoice.setDeltaIn(Segments.DELTA_UNLIMITED);
    macroChoice.setDeltaOut(Segments.DELTA_UNLIMITED);
    macroChoice.setProgramType(ProgramType.Macro.toString());
    macroChoice.setProgramSequenceBindingId(macroSequenceBinding.getId());
    fabricator.add(macroChoice);

    // 2. Main
    Program mainProgram = fabricator.addMemes(chooseMainProgram()
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to choose main program based on macro Program \"%s\" at offset %s %s",
        macroProgram.getName(),
        macroSequenceBindingOffset,
        apiUrlProvider.getAppUrl(String.format("/programs/%s", macroProgram.getId()))
      ))));
    fabricator.addMemes(macroProgram); // [#179078533] Straightforward meme logic
    Integer mainSequenceBindingOffset = computeMainProgramSequenceBindingOffset();
    var mainSequenceBinding = fabricator.addMemes(fabricator.getRandomlySelectedSequenceBindingAtOffset(mainProgram, mainSequenceBindingOffset)
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to determine sequence binding offset for main Program \"%s\" %s",
        mainProgram.getName(),
        apiUrlProvider.getAppUrl(String.format("/programs/%s", mainProgram.getId()))
      ))));
    var mainSequence = fabricator.sourceMaterial().getProgramSequence(mainSequenceBinding);
    var mainChoice = new SegmentChoice();
    mainChoice.setId(UUID.randomUUID());
    mainChoice.setSegmentId(fabricator.getSegment().getId());
    mainChoice.setProgramId(mainProgram.getId());
    mainChoice.setDeltaIn(Segments.DELTA_UNLIMITED);
    mainChoice.setDeltaOut(Segments.DELTA_UNLIMITED);
    mainChoice.setProgramType(ProgramType.Main.toString());
    mainChoice.setProgramSequenceBindingId(mainSequenceBinding.getId());
    fabricator.add(mainChoice);

    // 3. Chords and voicings
    if (mainSequence.isPresent())
      for (ProgramSequenceChord sequenceChord : fabricator.getProgramSequenceChords(mainSequence.get())) {
        // [#154090557] don't of chord past end of Segment
        String name;
        if (sequenceChord.getPosition() < mainSequence.get().getTotal()) {
          // delta the chord name
          name = new Chord(sequenceChord.getName()).getFullDescription();
          // of the final chord
          SegmentChord chord = new SegmentChord();
          chord.setId(UUID.randomUUID());
          chord.setSegmentId(fabricator.getSegment().getId());
          chord.setPosition(sequenceChord.getPosition());
          chord.setName(name);
          fabricator.add(chord);
          for (var voicing : fabricator.sourceMaterial().getVoicings(sequenceChord)) {
            var segmentChordVoicing = new SegmentChordVoicing();
            segmentChordVoicing.setId(UUID.randomUUID());
            segmentChordVoicing.setSegmentId(fabricator.getSegment().getId());
            segmentChordVoicing.segmentChordId(chord.getId());
            segmentChordVoicing.type(voicing.getType().toString());
            segmentChordVoicing.setNotes(voicing.getNotes());
            fabricator.add(segmentChordVoicing);
          }
        }
      }

    // Update the segment with fabricated content
    if (mainSequence.isPresent()) {
      var seg = fabricator.getSegment();
      seg.setType(fabricator.getType());
      seg.setOutputEncoder(fabricator.getTemplateConfig().getOutputContainer());
      seg.setTempo(computeSegmentTempo(macroSequence.get(), mainSequence.get()));
      seg.setKey(computeSegmentKey(mainSequence.get()).strip());
      seg.setTotal(Integer.valueOf(mainSequence.get().getTotal()));
      fabricator.updateSegment(seg);
    }

    // then, set the end-at time.
    if (mainSequence.isPresent())
      fabricator.updateSegment(fabricator.getSegment()
        .endAt(Values.formatIso8601UTC(segmentEndInstant(mainSequence.get()))));

    // If the type is not Continue, we will reset the offset main
    var segment = fabricator.getSegment();
    if (SegmentType.CONTINUE.equals(fabricator.getType()))
      segment.setDelta(fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal());
    else
      segment.setDelta(0);
    segment.density(computeSegmentDensity(segment.getDelta(), macroSequence.orElse(null), mainSequence.orElse(null)));
    fabricator.updateSegment(segment);

    // done
    fabricator.done();
  }

  /**
   compute the macroSequenceBindingOffset

   @return macroSequenceBindingOffset
   */
  private Integer computeMacroProgramSequenceBindingOffset() throws NexusException {
    var previousMacroChoice = fabricator.getMacroChoiceOfPreviousSegment();
    return switch (fabricator.getType()) {
      case INITIAL, NEXTMACRO -> 0;
      case CONTINUE -> previousMacroChoice.isPresent() ?
        fabricator.getSequenceBindingOffsetForChoice(previousMacroChoice.get()) : 0;
      case NEXTMAIN -> previousMacroChoice.isPresent() ?
        fabricator.getNextSequenceBindingOffset(previousMacroChoice.get()) : 0;
      default -> throw new NexusException(String.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
    };
  }

  /**
   compute the mainSequenceBindingOffset

   @return mainSequenceBindingOffset
   */
  private Integer computeMainProgramSequenceBindingOffset() throws NexusException {
    switch (fabricator.getType()) {

      case INITIAL:
      case NEXTMAIN:
      case NEXTMACRO:
        return 0;

      case CONTINUE:
        var previousMainChoice = fabricator.getMainChoiceOfPreviousSegment();
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
  public Optional<Program> chooseNextMacroProgram() throws NexusException {
    if (fabricator.isInitialSegment()) return chooseRandomMacroProgram();

    // if continuing the macro program, use the same one
    var previousMacroChoice = fabricator.getMacroChoiceOfPreviousSegment();
    if (fabricator.isContinuationOfMacroProgram() && previousMacroChoice.isPresent())
      return fabricator.getProgram(previousMacroChoice.get());

    // will rank all possibilities, and choose the next macro program
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    // (1) retrieve programs bound to chain and
    // (3) score each source program
    MemeIsometry iso = fabricator.getMemeIsometryOfNextSequenceInPreviousMacro();
    Collection<String> memes;
    for (Program program : fabricator.sourceMaterial().getProgramsOfType(ProgramType.Macro)) {
      memes = fabricator.sourceMaterial().getMemesAtBeginning(program);
      if (iso.isAllowed(memes))
        superEntityScorePicker.add(program, scoreMacro(iso, program, memes));
    }

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
  public Optional<Program> chooseRandomMacroProgram() {
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    for (Program program : fabricator.sourceMaterial().getProgramsOfType(ProgramType.Macro))
      superEntityScorePicker.add(program, Chance.normallyAround(0, SCORE_MACRO_ENTROPY));

    return superEntityScorePicker.getTop();
  }

  /**
   Choose main program
   <p>
   ONLY CHOOSES ONCE, then returns that choice every time

   @return main-type Program
   */
  private Optional<Program> chooseMainProgram() throws NexusException {
    // if continuing the macro program, use the same one
    var previousMainChoice = fabricator.getMainChoiceOfPreviousSegment();
    if (SegmentType.CONTINUE == fabricator.getType())
      if (previousMainChoice.isPresent())
        return fabricator.getProgram(previousMainChoice.get());

    // will rank all possibilities, and choose the next main program
    // future: only choose major programs for major keys, minor for minor! [#223] Key of first Pattern of chosen Main-Program must match the `minor` or `major` with the Key of the current Segment.
    EntityScorePicker<Program> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve programs bound to chain and
    // (3) score each source program based on meme isometry
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    Collection<String> memes;
    for (Program program : fabricator.sourceMaterial().getProgramsOfType(ProgramType.Main)) {
      memes = fabricator.sourceMaterial().getMemesAtBeginning(program);
      if (iso.isAllowed(memes))
        superEntityScorePicker.add(program, scoreMain(iso, program, memes));
    }

    // report
    fabricator.putReport("mainChoice", superEntityScorePicker.report());

    // (4) return the top choice
    return superEntityScorePicker.getTop();
  }

  /**
   Score a candidate for next macro program, given current fabricator

   @param iso     from which to score macro programs
   @param program to score
   @param memes   to score
   @return score, including +/- entropy
   */
  private double scoreMacro(MemeIsometry iso, Program program, Collection<String> memes) {
    double score = Chance.normallyAround(0, SCORE_MACRO_ENTROPY);

    if (fabricator.isInitialSegment()) {
      return score;
    }

    // Score includes matching memes to previous segment's macro-program's next pattern
    score += iso.score(memes) * SCORE_MATCH;

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      score += SCORE_DIRECT;
    else if (program.getState().equals(ProgramState.Draft))
      score += SCORE_UNPUBLISHED;

    return score;
  }

  /**
   Score a candidate for next main program, given current fabricator

   @param iso     from which to score main programs
   @param program to score
   @param memes   to score
   @return score, including +/- entropy
   */
  private double scoreMain(MemeIsometry iso, Program program, Collection<String> memes) {
    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(program))
      return SCORE_DIRECT;
    else if (program.getState().equals(ProgramState.Draft))
      return SCORE_UNPUBLISHED;

    // Score includes matching memes, previous segment to macro program first pattern
    AtomicReference<Double> score = new AtomicReference<>(
      Chance.normallyAround(0, SCORE_MAIN_ENTROPY) + SCORE_MATCH *
        iso.score(memes));

    // Avoid previous main program
    if (!fabricator.isInitialSegment())
      fabricator.getMainChoiceOfPreviousSegment()
        .flatMap(previousMainChoice -> fabricator.getProgram(previousMainChoice))
        .filter(previousMainProgram -> Objects.equals(program.getId(), previousMainProgram.getId()))
        .map(previousMainProgram -> score.updateAndGet(v -> v + SCORE_AVOID));

    return score.get();
  }

  /**
   Get Segment length, in nanoseconds

   @param mainSequence the end of which marks the end of the segment
   @return segment length, in nanoseconds
   @throws NexusException on failure
   */
  private long segmentLengthNanos(ProgramSequence mainSequence) throws NexusException {
    return (long) (fabricator.getSecondsAtPosition(mainSequence.getTotal()) * NANOS_PER_SECOND);
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
