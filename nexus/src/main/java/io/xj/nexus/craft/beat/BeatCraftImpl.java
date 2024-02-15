// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.beat;


import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.util.CsvUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 Beat craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 <p>
 BeatCraftImpl extends DetailCraftImpl to leverage all detail craft enhancements https://www.pivotaltracker.com/story/show/176625174
 */
public class BeatCraftImpl extends DetailCraftImpl implements BeatCraft {

  public BeatCraftImpl(
    Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(ProgramType.Beat);

    // Program is from prior choice, or freshly chosen
    Optional<Program> program = priorChoice.isPresent() ?
      fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()):
      chooseFreshProgram(ProgramType.Beat, InstrumentType.Drum);

    // Should gracefully skip voicing type if unfulfilled by detail program https://www.pivotaltracker.com/story/show/176373977
    if (program.isEmpty()) {
      reportMissing(Program.class, "Beat-type program");
      return;
    }

    // Segments have intensity arcs; automate mixer layers in and out of each main program https://www.pivotaltracker.com/story/show/178240332
    ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) ->
      fabricator.sourceMaterial().getProgramVoice(choice.getProgramVoiceId())
        .map(ProgramVoice::getName)
        .orElse("Unknown");
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> ProgramType.Beat.equals(choice.getProgramType());
    var programNames = fabricator.sourceMaterial().getVoicesOfProgram(program.get()).stream()
      .map(ProgramVoice::getName)
      .collect(Collectors.toList());
    precomputeDeltas(
      choiceFilter,
      choiceIndexProvider,
      programNames,
      CsvUtils.split(fabricator.getTemplateConfig().getDeltaArcBeatLayersToPrioritize()),
      fabricator.getTemplateConfig().getDeltaArcBeatLayersIncoming()
    );

    // beat sequence is selected at random of the current program
    // FUTURE: Beat Program with multiple Sequences https://www.pivotaltracker.com/story/show/166855956
    var sequence = fabricator.getRandomlySelectedSequence(program.get());

    // voice arrangements
    if (sequence.isPresent()) {
      var voices = fabricator.sourceMaterial().getVoicesOfProgram(program.get());
      if (voices.isEmpty())
        reportMissing(ProgramVoice.class,
          String.format("in Beat-choice Program[%s]", program.get().getId()));

      craftNoteEvents(fabricator.getTempo(), sequence.get(), voices, voice -> chooseFreshInstrument(List.of(voice.getType()), List.of(InstrumentMode.Event), List.of(), voice.getName(), fabricator.sourceMaterial().getTrackNamesOfVoice(voice)), true);
    }
  }

}
