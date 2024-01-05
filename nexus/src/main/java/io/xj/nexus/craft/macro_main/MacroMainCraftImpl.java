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
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentType;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class MacroMainCraftImpl extends CraftImpl implements MacroMainCraft {
  private static final Logger LOG = LoggerFactory.getLogger(MacroMainCraftImpl.class);

  @Nullable
  private final Program overrideMacroProgram;
  @Nullable
  private final Collection<String> overrideMemes;

  public MacroMainCraftImpl(
    Fabricator fabricator,
    @Nullable Program overrideMacroProgram,
    @Nullable Collection<String> overrideMemes
  ) {
    super(fabricator);
    this.overrideMacroProgram = overrideMacroProgram;
    this.overrideMemes = overrideMemes;
  }

  @Override
  public void doWork() throws NexusException {
    var segment = fabricator.getSegment();

    // If we are overriding memes, start by adding them to the workbench segment
    if (Objects.nonNull(overrideMemes))
      for (String meme : overrideMemes) {
        var segmentMeme = new SegmentMeme();
        segmentMeme.setId(UUID.randomUUID());
        segmentMeme.setSegmentId(fabricator.getSegment().getId());
        segmentMeme.setName(meme);
        fabricator.put(segmentMeme, true);
      }

    //
    // 1. Macro
    var macroProgram = Objects.nonNull(overrideMacroProgram)
      ? overrideMacroProgram
      : chooseMacroProgram();
    //
    Integer macroSequenceBindingOffset = Objects.nonNull(overrideMacroProgram)
      ? fabricator.getSecondMacroSequenceBindingOffset(overrideMacroProgram)
      : computeMacroSequenceBindingOffset();
    var macroSequenceBinding = fabricator.getRandomlySelectedSequenceBindingAtOffset(macroProgram, macroSequenceBindingOffset)
      .orElseThrow(() -> new NexusException(String.format("Unable to determine macro sequence binding for Segment[%d]", segment.getId())));
    //
    var macroSequence = fabricator.sourceMaterial().getProgramSequence(macroSequenceBinding)
      .orElseThrow(() -> new NexusException(String.format("Unable to determine macro sequence for Segment[%d]", segment.getId())));
    //
    var macroChoice = new SegmentChoice();
    macroChoice.setId(UUID.randomUUID());
    macroChoice.setSegmentId(segment.getId());
    macroChoice.setProgramSequenceId(macroSequence.getId());
    macroChoice.setProgramId(macroProgram.getId());
    macroChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    macroChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    macroChoice.setProgramType(ProgramType.Macro);
    macroChoice.setProgramSequenceBindingId(macroSequenceBinding.getId());
    fabricator.put(macroChoice, true); // force put, because fabrication cannot proceed without a macro choice

    //
    // 2. Main
    var mainProgram = chooseMainProgram();
    //
    Integer mainSequenceBindingOffset = computeMainProgramSequenceBindingOffset();
    var mainSequenceBinding = fabricator.getRandomlySelectedSequenceBindingAtOffset(mainProgram, mainSequenceBindingOffset)
      .orElseThrow(() -> new NexusException(String.format("Unable to determine main sequence binding for Segment[%d]", segment.getId())));
    //
    var mainSequence = fabricator.sourceMaterial().getProgramSequence(mainSequenceBinding)
      .orElseThrow(() -> new NexusException(String.format("Unable to determine main sequence for Segment[%d]", segment.getId())));

    var mainChoice = new SegmentChoice();
    mainChoice.setId(UUID.randomUUID());
    mainChoice.setSegmentId(segment.getId());
    mainChoice.setProgramId(mainProgram.getId());
    mainChoice.setDeltaIn(Segment.DELTA_UNLIMITED);
    mainChoice.setDeltaOut(Segment.DELTA_UNLIMITED);
    mainChoice.setProgramType(ProgramType.Main);
    mainChoice.setProgramSequenceBindingId(mainSequenceBinding.getId());
    fabricator.put(mainChoice, true); // force put, because fabrication cannot proceed without a main choice

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
        chord.setSegmentId(segment.getId());
        chord.setPosition(sequenceChord.getPosition());
        chord.setName(name);
        fabricator.put(chord, false);
        for (var voicing : fabricator.sourceMaterial().getVoicings(sequenceChord)) {
          var segmentChordVoicing = new SegmentChordVoicing();
          segmentChordVoicing.setId(UUID.randomUUID());
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

    // Set the density
    segment.setDensity(computeSegmentDensity(segment.getDelta(), macroSequence, mainSequence));

    // Finished
    fabricator.updateSegment(segment);
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
  private double computeSegmentDensity(Integer delta, @Nullable ProgramSequence macroSequence, @Nullable ProgramSequence mainSequence) throws NexusException {
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
  private float computeDensity(@Nullable ProgramSequence macroSequence, @Nullable ProgramSequence mainSequence) throws NexusException {
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

  /**
   compute the macroSequenceBindingOffset

   @return macroSequenceBindingOffset
   */
  private Integer computeMacroSequenceBindingOffset() throws NexusException {
    if (List.of(SegmentType.INITIAL, SegmentType.NEXT_MACRO).contains(fabricator.getType()))
      return 0;

    var previousMacroChoice = fabricator.getMacroChoiceOfPreviousSegment();
    if (previousMacroChoice.isEmpty())
      return 0;

    if (SegmentType.CONTINUE == fabricator.getType())
      return fabricator.getSequenceBindingOffsetForChoice(previousMacroChoice.get());

    if (SegmentType.NEXT_MAIN == fabricator.getType())
      return fabricator.getNextSequenceBindingOffset(previousMacroChoice.get());

    throw new NexusException(String.format("Cannot get Macro-type sequence for known fabricator type=%s", fabricator.getType()));
  }

  /**
   compute the mainSequenceBindingOffset

   @return mainSequenceBindingOffset
   */
  private int computeMainProgramSequenceBindingOffset() throws NexusException {
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
  protected Program chooseRandomProgram(Collection<Program> programs, List<UUID> avoid) throws NexusException {
    var bag = MarbleBag.empty();

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
      throw new NexusException("Failed to choose any random program. No candidates available!");

    var program = fabricator.sourceMaterial().getProgram(bag.pick());
    if (program.isEmpty()) {
      var message = String.format(
        "Unable to choose main program for Segment[%d]",
        fabricator.getSegment().getId()
      );
      fabricator.addErrorMessage(message);
      LOG.error(message);
      throw new NexusException(message);
    }
    return program.get();
  }

  /**
   will rank all possibilities, and choose the next macro program

   @return macro-type program
   */
  protected Program chooseMacroProgram() throws NexusException {
    if (Objects.nonNull(overrideMacroProgram))
      return overrideMacroProgram;

    var bag = MarbleBag.empty();
    var candidates = fabricator.sourceMaterial().getPrograms(ProgramType.Macro);

    // initial segment is completely random
    if (fabricator.isInitialSegment()) return chooseRandomProgram(candidates, List.of());

    // if continuing the macro program, use the same one
    if (fabricator.isContinuationOfMacroProgram()
      && fabricator.getMacroChoiceOfPreviousSegment().isPresent()) {
      var previousProgram = fabricator.getProgram(fabricator.getMacroChoiceOfPreviousSegment().get());
      if (previousProgram.isEmpty()) {
        var message = String.format(
          "Unable to get previous macro program for Segment[%d]",
          fabricator.getSegment().getId()
        );
        fabricator.addErrorMessage(message);
        LOG.error(message);
        throw new NexusException(message);
      }
      return previousProgram.get();
    }

    // Compute the meme isometry for use in selecting programs from the bag
    MemeIsometry iso =
      Objects.nonNull(overrideMemes) ?
        MemeIsometry.of(fabricator.getMemeTaxonomy(), overrideMemes)
        : fabricator.getMemeIsometryOfNextSequenceInPreviousMacro();

    // Compute any program id to avoid
    var avoidProgramId = fabricator.getMacroChoiceOfPreviousSegment()
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
      throw new NexusException("Failed to choose any next macro program. No candidates available!");

    // report and pick
    fabricator.putReport("macroChoice", bag.toString());
    var program = fabricator.sourceMaterial().getProgram(bag.pick());
    if (program.isEmpty()) {
      var message = String.format(
        "Unable to choose macro program for Segment[%d]",
        fabricator.getSegment().getId()
      );
      fabricator.addErrorMessage(message);
      LOG.error(message);
      throw new NexusException(message);
    }
    return program.get();
  }

  /**
   Choose main program
   <p>
   ONLY CHOOSES ONCE, then returns that choice every time

   @return main-type Program
   */
  protected Program chooseMainProgram() throws NexusException {
    var bag = MarbleBag.empty();
    var candidates = fabricator.sourceMaterial().getPrograms(ProgramType.Main);

    // if continuing the macro program, use the same one
    if (SegmentType.CONTINUE == fabricator.getType()
      && fabricator.getPreviousMainChoice().isPresent()) {
      var previousProgram = fabricator.getProgram(fabricator.getPreviousMainChoice().get());
      if (previousProgram.isEmpty()) {
        var message = String.format(
          "Unable to get previous main program for Segment[%d]",
          fabricator.getSegment().getId()
        );
        fabricator.addErrorMessage(message);
        LOG.error(message);
        throw new NexusException(message);
      }
      return previousProgram.get();
    }


    // Compute the meme isometry for use in selecting programs from the bag
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();

    // Compute any program id to avoid
    var avoidProgramId = fabricator.getPreviousMainChoice().map(SegmentChoice::getProgramId);

    // Add candidates to the bag
    // Phase 1: Directly Bound Programs, memes allowed, bonus for meme match, besides any that should be avoided
    for (Program program : programsDirectlyBound(candidates)) {
      if (!iso.isAllowed(fabricator.sourceMaterial().getMemesAtBeginning(program))) continue;
      bag.add(1, program.getId(), 1 + iso.score(fabricator.sourceMaterial().getMemesAtBeginning(program)));
    }

    // Add candidates to the bag
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

    // Add candidates to the bag
    // Phase 4: Literally Any Programs
    for (Program program : candidates)
      bag.add(4, program.getId());

    // if the bag is empty, problems
    if (bag.isEmpty()) {
      throw new NexusException("Failed to choose any next main program. No candidates available!");
    }

    // report and pick
    fabricator.putReport("mainChoice", bag.toString());
    var program = fabricator.sourceMaterial().getProgram(bag.pick());
    if (program.isEmpty()) {
      var message = String.format(
        "Unable to choose main program for Segment[%d]",
        fabricator.getSegment().getId()
      );
      fabricator.addErrorMessage(message);
      LOG.error(message);
      throw new NexusException(message);
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

}
