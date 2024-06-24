// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/MacroMainCraft.h"

using namespace XJ;

MacroMainCraft::MacroMainCraft(
        Fabricator *fabricator,
        const std::optional<Program *> &overrideMacroProgram,
        const std::set<std::string> &overrideMemes) : FabricationWrapper(fabricator) {
  this->overrideMacroProgram = overrideMacroProgram;
  this->overrideMemes = overrideMemes;
}

  void doWork() {
    auto segment = fabricator.getSegment();

    // Prepare variables to hold result of macro and main choice
    // Depending on whether we have override memes, we may perform main then macro (override), or macro then main (auto)
    ProgramSequence macroSequence;
    ProgramSequence mainSequence;
    Program mainProgram;

    // If we are overriding memes, start by adding them to the workbench segment, and do main before macro
    if (Objects.nonNull(overrideMemes)) {
      for (std::string meme : overrideMemes) {
        auto segmentMeme = new SegmentMeme();
        segmentMeme.setId(EntityUtils::computeUniqueId());
        segmentMeme.setSegmentId(fabricator.getSegment().getId());
        segmentMeme.setName(meme);
        fabricator.put(segmentMeme, true);
      }
      // choose main then macro (override)
      mainSequence = doMainChoiceWork(segment);
      macroSequence = doMacroChoiceWork(segment);
    } else {
      // choose macro then main (auto)
      macroSequence = doMacroChoiceWork(segment);
      mainSequence = doMainChoiceWork(segment);
    }
    mainProgram = fabricator.sourceMaterial().getProgram(mainSequence.getProgramId())
      .orElseThrow(() -> new FabricationException(std::string.format("Unable to determine main program for Segment[%d]", segment.getId())));

    // 3. Chords and voicings
    for (ProgramSequenceChord sequenceChord : fabricator.getProgramSequenceChords(mainSequence)) {
      // don't of chord past end of Segment
      std::string name;
      if (sequenceChord.getPosition() < mainSequence.getTotal()) {
        // delta the chord name
        name = new Chord(sequenceChord.getName()).getName();
        // of the chord
        SegmentChord chord = new SegmentChord();
        chord.setId(EntityUtils::computeUniqueId());
        chord.setSegmentId(segment.getId());
        chord.setPosition(sequenceChord.getPosition());
        chord.setName(name);
        fabricator.put(chord, false);
        for (auto voicing : fabricator.sourceMaterial().getVoicingsOfChord(sequenceChord)) {
          auto segmentChordVoicing = new SegmentChordVoicing();
          segmentChordVoicing.setId(EntityUtils::computeUniqueId());
          segmentChordVoicing.setSegmentId(segment.getId());
          segmentChordVoicing.segmentChordId(chord.getId());
          segmentChordVoicing.type(fabricator.getProgramVoiceType(voicing).toString());
          segmentChordVoicing.setNotes(voicing.getNotes());
          fabricator.put(segmentChordVoicing, false);
        }
      }
    }

    // Update the segment with fabricated content
    segment.setType(fabricator.getType());
    segment.setTempo(Double.valueOf(mainProgram.getTempo()));
    segment.setKey(computeSegmentKey(mainSequence).strip());
    segment.setTotal(Integer.valueOf(mainSequence.getTotal()));
    segment.setDurationMicros(segmentLengthMicros(mainProgram, mainSequence));

    // If the type is not Continue, we will reset the offset main
    if (SegmentType.CONTINUE.equals(fabricator.getType()))
      segment.setDelta(segment.getDelta() + segment.getTotal());
    else
      segment.setDelta(0);

    // Set the intensity
    segment.setIntensity(computeSegmentIntensity(segment.getDelta(), macroSequence, mainSequence));

    // Finished
    fabricator.updateSegment(segment);
  }

  /**
   Do the macro-choice work.

   @param segment of which to compute main choice
   @return the macro sequence
   */
  private ProgramSequence doMacroChoiceWork(Segment segment) throws FabricationException {
    auto macroProgram = chooseMacroProgram();
    Integer macroSequenceBindingOffset = computeMacroSequenceBindingOffset();
    auto macroSequenceBinding = fabricator.getRandomlySelectedSequenceBindingAtOffset(macroProgram, macroSequenceBindingOffset)
      .orElseThrow(() -> new FabricationException(std::string.format("Unable to determine macro sequence binding for Segment[%d]", segment.getId())));
    auto macroSequence = fabricator.sourceMaterial().getSequenceOfBinding(macroSequenceBinding)
      .orElseThrow(() -> new FabricationException(std::string.format("Unable to determine macro sequence for Segment[%d]", segment.getId())));
    //
    auto macroChoice = new SegmentChoice();
    macroChoice.setId(EntityUtils::computeUniqueId());
    macroChoice.setSegmentId(segment.getId());
    macroChoice.setProgramSequenceId(macroSequence.getId());
    macroChoice.setProgramId(macroProgram.getId());
    macroChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    macroChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    macroChoice.setProgramType(Program::Type::Macro);
    macroChoice.setProgramSequenceBindingId(macroSequenceBinding.getId());
    fabricator.put(macroChoice, true); // force put, because fabrication cannot proceed without a macro choice

    return macroSequence;
  }

  /**
   Do the main-choice work.

   @param segment of which to compute main choice
   @return the main sequence
   */
  private ProgramSequence doMainChoiceWork(Segment segment) throws FabricationException {
    auto mainProgram = chooseMainProgram();
    Integer mainSequenceBindingOffset = computeMainProgramSequenceBindingOffset();
    auto mainSequenceBinding = fabricator.getRandomlySelectedSequenceBindingAtOffset(mainProgram, mainSequenceBindingOffset)
      .orElseThrow(() -> new FabricationException(std::string.format("Unable to determine main sequence binding for Segment[%d]", segment.getId())));
    auto mainSequence = fabricator.sourceMaterial().getSequenceOfBinding(mainSequenceBinding)
      .orElseThrow(() -> new FabricationException(std::string.format("Unable to determine main sequence for Segment[%d]", segment.getId())));
    //
    auto mainChoice = new SegmentChoice();
    mainChoice.setId(EntityUtils::computeUniqueId());
    mainChoice.setSegmentId(segment.getId());
    mainChoice.setProgramId(mainProgram.getId());
    mainChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    mainChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    mainChoice.setProgramType(Program::Type::Main);
    mainChoice.setProgramSequenceBindingId(mainSequenceBinding.getId());
    fabricator.put(mainChoice, true); // force put, because fabrication cannot proceed without a main choice

    return mainSequence;
  }

  /**
   Compute the key of the current segment key, the key of the current main program sequence

   @param mainSequence of which to compute key
   @return key
   */
  private std::string computeSegmentKey(ProgramSequence mainSequence) throws FabricationException {
    std::string mainKey = mainSequence.getKey();
    if (null == mainKey || mainKey.isEmpty())
      mainKey = fabricator.sourceMaterial().getProgram(mainSequence.getProgramId())
        .orElseThrow(() -> new FabricationException(std::string.format(
          "Unable to determine key for Main-Program[%s] %s",
          mainSequence.getName(),
          mainSequence.getProgramId())
        ))
        .getKey();
    return Chord.of(mainKey).getName();
  }

  /**
   Compute the intensity of the current segment
   future: Segment Intensity = average of macro and main-sequence patterns
   <p>
   Segment is assigned a intensity during macro-main craft. It's going to be used to determine a target # of perc loops
   Percussion Loops Alpha https://github.com/xjmusic/xjmusic/issues/261

   @param macroSequence of which to compute segment tempo
   @param mainSequence  of which to compute segment tempo
   @return intensity
   */
  private double computeSegmentIntensity(Integer delta, @Nullable ProgramSequence macroSequence, @Nullable ProgramSequence mainSequence) throws FabricationException {
    return fabricator.getTemplateConfig().isIntensityAutoCrescendoEnabled()
      ?
      ValueUtils.limitDecimalPrecision(ValueUtils.interpolate(
        fabricator.getTemplateConfig().getIntensityAutoCrescendoMinimum(),
        fabricator.getTemplateConfig().getIntensityAutoCrescendoMaximum(),
        (double) delta / fabricator.getTemplateConfig().getMainProgramLengthMaxDelta(),
        computeIntensity(macroSequence, mainSequence)
      ))
      :
      computeIntensity(macroSequence, mainSequence);
  }

  /**
   Compute the average intensity of the two given sequences

   @param macroSequence of which to compute segment tempo
   @param mainSequence  of which to compute segment tempo
   @return intensity
   */
  private float computeIntensity(@Nullable ProgramSequence macroSequence, @Nullable ProgramSequence mainSequence) throws FabricationException {
    @Nullable Float macroIntensity = Objects.nonNull(macroSequence) ? macroSequence.getIntensity() : null;
    @Nullable Float mainIntensity = Objects.nonNull(mainSequence) ? mainSequence.getIntensity() : null;
    if (Objects.nonNull(macroIntensity) && Objects.nonNull(mainIntensity))
      return (macroIntensity + mainIntensity) / 2;
    if (Objects.nonNull(macroIntensity))
      return macroIntensity;
    if (Objects.nonNull(mainIntensity))
      return mainIntensity;
    throw new FabricationException("Failed to compute Intensity!");
  }

  /**
   compute the macroSequenceBindingOffset

   @return macroSequenceBindingOffset
   */
  private Integer computeMacroSequenceBindingOffset() throws FabricationException {
    if (List.of(Segment::Type::Initial, SegmentType.NEXT_MACRO).contains(fabricator.getType()))
      return Objects.nonNull(overrideMacroProgram)
        ? fabricator.getSecondMacroSequenceBindingOffset(overrideMacroProgram)
        : 0;

    auto previousMacroChoice = fabricator.getMacroChoiceOfPreviousSegment();
    if (previousMacroChoice.isEmpty())
      return 0;

    if (SegmentType.CONTINUE == fabricator.getType())
      return fabricator.getSequenceBindingOffsetForChoice(previousMacroChoice.get());

    if (SegmentType.NEXT_MAIN == fabricator.getType())
      return fabricator.getNextSequenceBindingOffset(previousMacroChoice.get());

    throw new FabricationException(std::string.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
  }

  /**
   compute the mainSequenceBindingOffset

   @return mainSequenceBindingOffset
   */
  private int computeMainProgramSequenceBindingOffset() throws FabricationException {
    switch (fabricator.getType()) {
      case INITIAL, NEXT_MAIN, NEXT_MACRO -> {
        return 0;
      }
      case CONTINUE -> {
        auto previousMainChoice = fabricator.getPreviousMainChoice();
        if (previousMainChoice.isEmpty())
          throw new FabricationException("Cannot get retrieve previous main choice");
        return fabricator.getNextSequenceBindingOffset(previousMainChoice.get());
      }
      default ->
        throw new FabricationException(std::string.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
    }
  }

  /**
   Choose program completely at random

   @param programs all from which to choose
   @param avoid    to avoid
   @return program
   */
  protected Program chooseRandomProgram(Collection<Program> programs, List<UUID> avoid) throws FabricationException {
    auto bag = MarbleBag.empty();

    // Phase 1: Directly Bound Programs, besides those we should avoid
    // Phase 2: Any Directly Bound Programs
    for (Program program : programsDirectlyBound(programs)) {
      if (!avoid.contains(program.getId()))
        bag.add(1, program.getId());
      bag.add(2, program.getId());
    }

    // Phase 3: All Published Programs, besides those we should avoid
    // Phase 4: Any Published Programs
    for (Program program : programsPublished(programs)) {
      if (!avoid.contains(program.getId()))
        bag.add(3, program.getId());
      bag.add(4, program.getId());
    }

    // Phase 5: Any Program
    for (Program program : programs)
      bag.add(5, program.getId());

    // if the bag is empty, problems
    if (bag.isEmpty())
      throw new FabricationException("Failed to choose any random program. No candidates available!");

    auto program = fabricator.sourceMaterial().getProgram(bag.pick());
    if (program.isEmpty()) {
      auto message = std::string.format(
        "Unable to choose main program for Segment[%d]",
        fabricator.getSegment().getId()
      );
      fabricator.addErrorMessage(message);
      LOG.error(message);
      throw new FabricationException(message);
    }
    return program.get();
  }

  /**
   will rank all possibilities, and choose the next macro program

   @return macro-type program
   */
  protected Program chooseMacroProgram() throws FabricationException {
    if (Objects.nonNull(overrideMacroProgram))
      return overrideMacroProgram;

    auto bag = MarbleBag.empty();
    auto candidates = fabricator.sourceMaterial().getProgramsOfType(Program::Type::Macro);

    // initial segment is completely random
    if (fabricator.isInitialSegment()) return chooseRandomProgram(candidates, List.of());

    // if continuing the macro program, use the same one
    if (fabricator.isContinuationOfMacroProgram()
      && fabricator.getMacroChoiceOfPreviousSegment().isPresent()) {
      auto previousProgram = fabricator.getProgram(fabricator.getMacroChoiceOfPreviousSegment().get());
      if (previousProgram.isEmpty()) {
        auto message = std::string.format(
          "Unable to get previous macro program for Segment[%d]",
          fabricator.getSegment().getId()
        );
        fabricator.addErrorMessage(message);
        LOG.error(message);
        throw new FabricationException(message);
      }
      return previousProgram.get();
    }

    // Compute the meme isometry for use in selecting programs from the bag
    MemeIsometry iso =
      Objects.nonNull(overrideMemes) ?
        MemeIsometry.of(fabricator.getMemeTaxonomy(), overrideMemes)
        : fabricator.getMemeIsometryOfNextSequenceInPreviousMacro();

    // Compute any program id to avoid
    auto avoidProgramId = fabricator.getMacroChoiceOfPreviousSegment()
      .map(SegmentChoice::getProgramId);

    // Add candidates to the bag
    // Phase 1: Directly Bound Programs besides any that should be avoided, with a meme match
    // Phase 2: Any Directly Bound Programs besides any that should be avoided, meme match is a bonus
    // Phase 3: Any Directly Bound Programs
    for (Program program : programsDirectlyBound(candidates)) {
      bag.add(1, program.getId(), iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
      bag.add(2, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
      bag.add(3, program.getId());
    }

    // Add candidates to the bag
    // Phase 4: All Published Programs with a meme match, besides any that should be avoided
    // Phase 5: Any Published Programs, meme match is a bonus
    // Phase 6: Any Published Programs
    for (Program program : programsPublished(candidates)) {
      if (avoidProgramId.isEmpty() || !avoidProgramId.get().equals(program.getId())) {
        bag.add(4, program.getId(), iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
        bag.add(5, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
      }
      bag.add(6, program.getId());
    }

    // Add candidates to the bag
    // Phase 7: Literally Any Programs
    for (Program program : candidates)
      bag.add(7, program.getId());

    // if the bag is empty, problems
    if (bag.isEmpty())
      throw new FabricationException("Failed to choose any next macro program. No candidates available!");

    // report and pick
    fabricator.putReport("macroChoice", bag.toString());
    auto program = fabricator.sourceMaterial().getProgram(bag.pick());
    if (program.isEmpty()) {
      auto message = std::string.format(
        "Unable to choose macro program for Segment[%d]",
        fabricator.getSegment().getId()
      );
      fabricator.addErrorMessage(message);
      LOG.error(message);
      throw new FabricationException(message);
    }
    return program.get();
  }

  /**
   Choose main program
   <p>
   ONLY CHOOSES ONCE, then returns that choice every time

   @return main-type Program
   */
  protected Program chooseMainProgram() throws FabricationException {
    auto bag = MarbleBag.empty();
    auto candidates = fabricator.sourceMaterial().getProgramsOfType(Program::Type::Main);

    // if continuing the macro program, use the same one
    if (SegmentType.CONTINUE == fabricator.getType()
      && fabricator.getPreviousMainChoice().isPresent()) {
      auto previousProgram = fabricator.getProgram(fabricator.getPreviousMainChoice().get());
      if (previousProgram.isEmpty()) {
        auto message = std::string.format(
          "Unable to get previous main program for Segment[%d]",
          fabricator.getSegment().getId()
        );
        fabricator.addErrorMessage(message);
        LOG.error(message);
        throw new FabricationException(message);
      }
      return previousProgram.get();
    }


    // Compute the meme isometry for use in selecting programs from the bag
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();

    // Compute any program id to avoid
    auto avoidProgramId = fabricator.getPreviousMainChoice().map(SegmentChoice::getProgramId);

    // Add candidates to the bag
    // Phase 1: Directly Bound Programs, memes allowed, bonus for meme match, besides any that should be avoided
    for (Program program : programsDirectlyBound(candidates)) {
      if (!iso.isAllowed(fabricator.sourceMaterial().getMemesAtBeginning(program))) continue;
      bag.add(1, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
    }

    // Add candidates to the bag
    // Phase 2: All Published Programs, memes allowed, bonus for meme match, besides any that should be avoided
    // Phase 3: Any Published Programs, memes allowed, bonus for meme match
    auto published = programsPublished(candidates);
    for (Program program : published) {
      if (!iso.isAllowed(fabricator.sourceMaterial().getMemesAtBeginning(program))) {
        continue;
      }
      if (avoidProgramId.isEmpty() || !avoidProgramId.get().equals(program.getId()))
        bag.add(2, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
      else
        bag.add(3, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
    }

    // Add candidates to the bag
    // Phase 4: Literally Any Programs
    for (Program program : candidates)
      bag.add(4, program.getId());

    // if the bag is empty, problems
    if (bag.isEmpty()) {
      throw new FabricationException("Failed to choose any next main program. No candidates available!");
    }

    // report and pick
    fabricator.putReport("mainChoice", bag.toString());
    auto program = fabricator.sourceMaterial().getProgram(bag.pick());
    if (program.isEmpty()) {
      auto message = std::string.format(
        "Unable to choose main program for Segment[%d]",
        fabricator.getSegment().getId()
      );
      fabricator.addErrorMessage(message);
      LOG.error(message);
      throw new FabricationException(message);
    }
    return program.get();
  }

  /**
   Get Segment length, in nanoseconds

   @param mainProgram  from which to source tempo
   @param mainSequence the end of which marks the end of the segment
   @return segment length, in nanoseconds
   */
  long segmentLengthMicros(Program mainProgram, ProgramSequence mainSequence) {
    return fabricator.getSegmentMicrosAtPosition(mainProgram.getTempo(), mainSequence.getTotal());
  }
