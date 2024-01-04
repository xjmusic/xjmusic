// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.macro_main;


import io.xj.hub.enums.ProgramType;
import io.xj.hub.music.Chord;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.util.MarbleBag;
import io.xj.hub.util.ValueUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.MemeIsometry;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentChordVoicing;
import io.xj.nexus.model.SegmentType;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MacroMainCraftImpl extends CraftImpl implements MacroMainCraft {

  @Nullable
  private final Program selectedMacroProgram;

  public MacroMainCraftImpl(
    Fabricator fabricator,
    @Nullable Program selectedMacroProgram
  ) {
    super(fabricator);
    this.selectedMacroProgram = selectedMacroProgram;
  }

  /**
   Compute the final key of the current segment key, the key of the current main program sequence

   @param mainSequence of which to compute key
   @return key
   */
  private String computeSegmentKey(ProgramSequence mainSequence) throws NexusException {
    String mainKey = mainSequence.getKey();
    if (null == mainKey || mainKey.isEmpty())
      mainKey = fabricator.sourceMaterial().getProgram(mainSequence.getProgramId())
        .orElseThrow(() -> new NexusException(String.format(
          "Unable to determine key for Main-Program[%s] %s",
          mainSequence.getName(),
          mainSequence.getProgramId())
        ))
        .getKey();
    return Chord.of(mainKey).getName();
  }

  /**
   Compute the final density of the current segment
   future: Segment Density = average of macro and main-sequence patterns
   <p>
   Segment is assigned a density during macro-main craft. It's going to be used to determine a target # of perc loops
   Percussion Loops Alpha https://www.pivotaltracker.com/story/show/179534065

   @param macroSequence of which to compute segment tempo
   @param mainSequence  of which to compute segment tempo
   @return density
   */
  double computeSegmentDensity(Integer delta, @Nullable ProgramSequence macroSequence, @Nullable ProgramSequence mainSequence) throws NexusException {
    return ValueUtils.limitDecimalPrecision(ValueUtils.interpolate(
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
  Float computeDensity(@Nullable ProgramSequence macroSequence, @Nullable ProgramSequence mainSequence) throws NexusException {
    @Nullable Float macroDensity =
      Objects.nonNull(macroSequence) ?
        (Objects.nonNull(macroSequence.getDensity()) ?
          macroSequence.getDensity()
          : fabricator.sourceMaterial().getProgram(macroSequence.getProgramId()).orElseThrow(() ->
          new NexusException(String.format(
            "Unable to determine density for Macro-Program[%s] %s",
            macroSequence.getName(),
            macroSequence.getProgramId())
          )).getDensity())
        : null;
    @Nullable Float mainDensity =
      Objects.nonNull(mainSequence) ?
        (Objects.nonNull(mainSequence.getDensity()) ?
          mainSequence.getDensity()
          : fabricator.sourceMaterial().getProgram(mainSequence.getProgramId()).orElseThrow(() ->
          new NexusException(String.format(
            "Unable to determine density for Main-Program[%s] %s",
            mainSequence.getName(),
            mainSequence.getProgramId())
          )).getDensity())
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
    var macroProgram = Objects.nonNull(selectedMacroProgram)
      ? selectedMacroProgram
      : chooseNextMacroProgram().orElseThrow(() -> new NexusException("Failed to choose a Macro-program by any means!"));
    Integer macroSequenceBindingOffset = Objects.nonNull(selectedMacroProgram)
      ? fabricator.getSecondMacroSequenceBindingOffset(selectedMacroProgram)
      : computeMacroSequenceBindingOffset();
    var macroSequenceBinding = fabricator.getRandomlySelectedSequenceBindingAtOffset(macroProgram, macroSequenceBindingOffset)
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to determine sequence binding offset for Macro-Program[%s] %s",
        macroProgram.getName(),
        macroProgram.getId())
      ));
    var macroSequence = fabricator.sourceMaterial().getProgramSequence(macroSequenceBinding)
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to determine sequence for Macro-Program[%s] %s",
        macroProgram.getName(),
        macroProgram.getId())
      ));
    var macroChoice = new SegmentChoice();
    macroChoice.setId(UUID.randomUUID());
    macroChoice.setSegmentId(fabricator.getSegment().getId());
    macroChoice.setProgramSequenceId(macroSequence.getId());
    macroChoice.setProgramId(macroProgram.getId());
    macroChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    macroChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    macroChoice.setProgramType(ProgramType.Macro);
    macroChoice.setProgramSequenceBindingId(macroSequenceBinding.getId());
    fabricator.put(macroChoice);

    // 2. Main
    Program mainProgram = chooseNextMainProgram()
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to choose main program based on Macro-Program[%s] at offset %s %s",
        macroProgram.getName(),
        macroSequenceBindingOffset,
        macroProgram.getId())
      ));
    Integer mainSequenceBindingOffset = computeMainProgramSequenceBindingOffset();
    var mainSequenceBinding = fabricator.getRandomlySelectedSequenceBindingAtOffset(mainProgram, mainSequenceBindingOffset)
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to determine sequence binding offset for Main-Program[%s] %s",
        mainProgram.getName(),
        mainProgram.getId()
      )));
    var mainSequence = fabricator.sourceMaterial().getProgramSequence(mainSequenceBinding)
      .orElseThrow(() -> new NexusException(String.format(
        "Unable to determine sequence for Main-Program[%s] %s",
        mainProgram.getName(),
        mainProgram.getId()
      )));
    var mainChoice = new SegmentChoice();
    mainChoice.setId(UUID.randomUUID());
    mainChoice.setSegmentId(fabricator.getSegment().getId());
    mainChoice.setProgramId(mainProgram.getId());
    mainChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    mainChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    mainChoice.setProgramType(ProgramType.Main);
    mainChoice.setProgramSequenceBindingId(mainSequenceBinding.getId());
    fabricator.put(mainChoice);

    // 3. Chords and voicings
    for (ProgramSequenceChord sequenceChord : fabricator.getProgramSequenceChords(mainSequence)) {
      // don't of chord past end of Segment https://www.pivotaltracker.com/story/show/154090557
      String name;
      if (sequenceChord.getPosition() < mainSequence.getTotal()) {
        // delta the chord name
        name = new Chord(sequenceChord.getName()).getName();
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
          segmentChordVoicing.type(fabricator.getProgramVoiceType(voicing).toString());
          segmentChordVoicing.setNotes(voicing.getNotes());
          fabricator.put(segmentChordVoicing);
        }
      }
    }

    var segment = fabricator.getSegment();

    // Update the segment with fabricated content
    segment.setType(fabricator.getType());
    segment.setTempo(Double.valueOf(mainProgram.getTempo()));
    segment.setKey(computeSegmentKey(mainSequence).strip());
    segment.setTotal(Integer.valueOf(mainSequence.getTotal()));
    segment.setDurationMicros(segmentLengthMicros(mainProgram, mainSequence));

    // If the type is not Continue, we will reset the offset main
    if (SegmentType.CONTINUE.equals(fabricator.getType()))
      segment.setDelta(fabricator.getSegment().getDelta() + fabricator.getSegment().getTotal());
    else
      segment.setDelta(0);

    // Set the density
    segment.setDensity(computeSegmentDensity(segment.getDelta(), macroSequence, mainSequence));

    // Finished
    fabricator.putSegment(segment);
    fabricator.done();
  }

  /**
   compute the macroSequenceBindingOffset

   @return macroSequenceBindingOffset
   */
  Integer computeMacroSequenceBindingOffset() throws NexusException {
    var previousMacroChoice = fabricator.getMacroChoiceOfPreviousSegment();
    return switch (fabricator.getType()) {
      case INITIAL, NEXT_MACRO -> 0;
      case CONTINUE -> previousMacroChoice.isPresent() ?
        fabricator.getSequenceBindingOffsetForChoice(previousMacroChoice.get()) : 0;
      case NEXT_MAIN -> previousMacroChoice.isPresent() ?
        fabricator.getNextSequenceBindingOffset(previousMacroChoice.get()) : 0;
      default ->
        throw new NexusException(String.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
    };
  }

  /**
   compute the mainSequenceBindingOffset

   @return mainSequenceBindingOffset
   */
  Integer computeMainProgramSequenceBindingOffset() throws NexusException {
    switch (fabricator.getType()) {
      case INITIAL, NEXT_MAIN, NEXT_MACRO -> {
        return 0;
      }
      case CONTINUE -> {
        var previousMainChoice = fabricator.getPreviousMainChoice();
        if (previousMainChoice.isEmpty())
          throw new NexusException("Cannot get retrieve previous main choice");
        return fabricator.getNextSequenceBindingOffset(previousMainChoice.get());
      }
      default ->
        throw new NexusException(String.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
    }
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
   will rank all possibilities, and choose the next macro program

   @return macro-type program
   */
  public Optional<Program> chooseNextMacroProgram() throws NexusException {
    var bag = MarbleBag.empty();
    var candidates = fabricator.sourceMaterial().getPrograms(ProgramType.Macro);

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
   Choose main program
   <p>
   ONLY CHOOSES ONCE, then returns that choice every time

   @return main-type Program
   */
  protected Optional<Program> chooseNextMainProgram() throws NexusException {
    var bag = MarbleBag.empty();
    var candidates = fabricator.sourceMaterial().getPrograms(ProgramType.Main);

    // if continuing the macro program, use the same one
    if (SegmentType.CONTINUE == fabricator.getType()
      && fabricator.getPreviousMainChoice().isPresent())
      return fabricator.getProgram(fabricator.getPreviousMainChoice().get());

    // add candidates to the bag
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    var avoidProgramId = fabricator.getPreviousMainChoice().map(SegmentChoice::getProgramId);

    // Phase 1: Directly Bound Programs, memes allowed, bonus for meme match, besides any that should be avoided
    for (Program program : programsDirectlyBound(candidates)) {
      if (!iso.isAllowed(fabricator.sourceMaterial().getMemesAtBeginning(program))) continue;
      bag.add(1, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
    }

    // Phase 2: All Published Programs, memes allowed, bonus for meme match, besides any that should be avoided
    // Phase 3: Any Published Programs, memes allowed, bonus for meme match
    var published = programsPublished(candidates);
    for (Program program : published) {
      if (!iso.isAllowed(fabricator.sourceMaterial().getMemesAtBeginning(program))) {
        continue;
      }
      if (avoidProgramId.isEmpty() || !avoidProgramId.get().equals(program.getId()))
        bag.add(2, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
      else
        bag.add(3, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
    }

    // if the bag is empty, problems
    if (bag.isEmpty()) {
      throw new NexusException("Failed to choose any next main program. No candidates available!");
    }


    // report and pick
    fabricator.putReport("mainChoice", bag.toString());
    return fabricator.sourceMaterial().getProgram(bag.pick());
  }

  /**
   Get Segment length, in nanoseconds

   @param mainProgram  from which to source tempo
   @param mainSequence the end of which marks the end of the segment
   @return segment length, in nanoseconds
   @throws NexusException on failure
   */
  long segmentLengthMicros(Program mainProgram, ProgramSequence mainSequence) throws NexusException {
    return fabricator.getSegmentMicrosAtPosition(mainProgram.getTempo(), mainSequence.getTotal());
  }

}
