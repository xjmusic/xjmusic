// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Key;
import io.xj.lib.util.MarbleBag;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.MemeIsometry;
import io.xj.nexus.persistence.Segments;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.*;

import static io.xj.lib.util.Values.NANOS_PER_SECOND;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MacroMainCraftImpl extends CraftImpl implements MacroMainCraft {
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

   @return tempo
   @param mainSequence  of which to compute segment tempo
   */
  private double computeSegmentTempo(@Nullable ProgramSequence mainSequence) throws NexusException {
    @Nullable Float mainTempo =
      Objects.nonNull(mainSequence) ?
        fabricator.sourceMaterial().getProgram(mainSequence.getProgramId()).orElseThrow().getTempo()
        : null;
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
    fabricator.put(macroChoice);

    // 2. Main
    Program mainProgram = fabricator.addMemes(chooseNextMainProgram()
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
    fabricator.put(mainChoice);

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
          fabricator.put(chord);
          for (var voicing : fabricator.sourceMaterial().getVoicings(sequenceChord)) {
            var segmentChordVoicing = new SegmentChordVoicing();
            segmentChordVoicing.setId(UUID.randomUUID());
            segmentChordVoicing.setSegmentId(fabricator.getSegment().getId());
            segmentChordVoicing.segmentChordId(chord.getId());
            segmentChordVoicing.type(voicing.getType().toString());
            segmentChordVoicing.setNotes(voicing.getNotes());
            fabricator.put(segmentChordVoicing);
          }
        }
      }

    // Update the segment with fabricated content
    if (mainSequence.isPresent()) {
      var seg = fabricator.getSegment();
      seg.setType(fabricator.getType());
      seg.setOutputEncoder(fabricator.getTemplateConfig().getOutputContainer());
      seg.setTempo(computeSegmentTempo(mainSequence.get()));
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
   will rank all possibilities, and choose the next macro program

   @return macro-type program
   */
  public Optional<Program> chooseNextMacroProgram() throws NexusException {
    var bag = MarbleBag.empty();
    var candidates = fabricator.sourceMaterial().getProgramsOfType(ProgramType.Macro);

    // initial segment is completely random
    if (fabricator.isInitialSegment()) return chooseRandomProgram(candidates, List.of());

    // if continuing the macro program, use the same one
    if (fabricator.isContinuationOfMacroProgram()
      && fabricator.getMacroChoiceOfPreviousSegment().isPresent())
      return fabricator.getProgram(fabricator.getMacroChoiceOfPreviousSegment().get());

    // add candidates to the bag
    MemeIsometry iso = fabricator.getMemeIsometryOfNextSequenceInPreviousMacro();
    var avoidProgramId = fabricator.getMacroChoiceOfPreviousSegment()
      .map(SegmentChoice::getProgramId);

    // Phase 1: Directly Bound Programs besides any that should be avoided, with a meme match
    // Phase 3: Any Directly Bound Programs besides any that should be avoided, meme match is a bonus
    // Phase 5: Any Directly Bound Programs
    for (Program program : programsDirectlyBound(candidates)) {
      bag.add(1, program.getId(), iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
      bag.add(3, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
      bag.add(5, program.getId());
    }

    // Phase 2: All Published Programs with a meme match, besides any that should be avoided
    // Phase 4: Any Published Programs, meme match is a bonus
    for (Program program : programsPublished(candidates)) {
      if (avoidProgramId.isEmpty() || !avoidProgramId.get().equals(program.getId())) {
        bag.add(2, program.getId(), iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
        bag.add(4, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
      }
      bag.add(6, program.getId());
    }

    // Phase 7: Literally Any Programs
    for (Program program : candidates)
      bag.add(5, program.getId());

    // if the bag is empty, problems
    if (bag.isEmpty())
      throw new NexusException("Failed to choose any next macro program. No candidates available!");

    // report and pick
    fabricator.putReport("macroChoice", bag.toString());
    return fabricator.sourceMaterial().getProgram(bag.pick());
  }

  /**
   Choose program completely at random

   @param programs all from which to choose
   @param avoid    to avoid
   @return program
   */
  public Optional<Program> chooseRandomProgram(Collection<Program> programs, List<UUID> avoid) {
    var bag = MarbleBag.empty();

    // Phase 1: Directly Bound Programs, besides those we should avoid
    // Phase 3: Any Directly Bound Programs
    for (Program program : programsDirectlyBound(programs)) {
      if (!avoid.contains(program.getId()))
        bag.add(1, program.getId());
      bag.add(3, program.getId());
    }

    // Phase 2: All Published Programs, besides those we should avoid
    // Phase 3: Any Published Programs
    for (Program program : programsPublished(programs)) {
      if (!avoid.contains(program.getId()))
        bag.add(2, program.getId());
      bag.add(4, program.getId());
    }

    // Phase 5: Any Program
    for (Program program : programs)
      bag.add(5, program.getId());

    if (bag.isPresent())
      return fabricator.sourceMaterial().getProgram(bag.pick());

    return Optional.empty();
  }

  /**
   Choose main program
   <p>
   ONLY CHOOSES ONCE, then returns that choice every time

   @return main-type Program
   */
  private Optional<Program> chooseNextMainProgram() throws NexusException {
    var bag = MarbleBag.empty();
    var candidates = fabricator.sourceMaterial().getProgramsOfType(ProgramType.Main);

    // if continuing the macro program, use the same one
    if (SegmentType.CONTINUE == fabricator.getType()
      && fabricator.getMainChoiceOfPreviousSegment().isPresent())
      return fabricator.getProgram(fabricator.getMainChoiceOfPreviousSegment().get());

    // add candidates to the bag
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    var avoidProgramId = fabricator.getMainChoiceOfPreviousSegment()
      .map(SegmentChoice::getProgramId);
    Collection<String> memes;

    // Phase 1: Directly Bound Programs, memes allowed, bonus for meme match, besides any that should be avoided
    // Phase 3: Any Directly Bound Programs, memes allowed, bonus for meme match
    for (Program program : programsDirectlyBound(candidates)) {
      memes = fabricator.sourceMaterial().getMemesAtBeginning(program);
      if (!iso.isAllowed(memes)) continue;
      bag.add(1, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
      bag.add(3, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
    }

    // Phase 2: All Published Programs, memes allowed, bonus for meme match, besides any that should be avoided
    // Phase 4: Any Published Programs, memes allowed, bonus for meme match
    for (Program program : programsPublished(candidates)) {
      memes = fabricator.sourceMaterial().getMemesAtBeginning(program);
      if (!iso.isAllowed(memes)) continue;
      if (avoidProgramId.isEmpty() || !avoidProgramId.get().equals(program.getId()))
        bag.add(2, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
      bag.add(4, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
    }

    // Phase 5: Literally Any Programs
    for (Program program : candidates)
      bag.add(5, program.getId());

    // if the bag is empty, problems
    if (bag.isEmpty())
      throw new NexusException("Failed to choose any next main program. No candidates available!");

    // report and pick
    fabricator.putReport("mainChoice", bag.toString());
    return fabricator.sourceMaterial().getProgram(bag.pick());
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
