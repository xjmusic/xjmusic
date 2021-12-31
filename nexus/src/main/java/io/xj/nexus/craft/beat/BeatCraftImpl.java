// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.beat;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.SegmentChoice;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.util.CSV;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.fabricator.Fabricator;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 Beat craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 <p>
 [#176625174] BeatCraftImpl extends DetailCraftImpl to leverage all detail craft enhancements
 */
public class BeatCraftImpl extends DetailCraftImpl implements BeatCraft {

  @Inject
  public BeatCraftImpl(
    @Assisted("basis") Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(ProgramType.Beat);

    // Program is from prior choice, or freshly chosen
    Optional<Program> program = priorChoice.isPresent() ?
      fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()) :
      chooseFreshProgram(ProgramType.Beat, InstrumentType.Drum);

    // [#176373977] Should gracefully skip voicing type if unfulfilled by detail program
    if (program.isEmpty()) {
      reportMissing(Program.class, "Beat-type program");
      return;
    }

    // [#178240332] Segments have intensity arcs; automate mixer layers in and out of each main program
    ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) ->
      fabricator.sourceMaterial().getProgramVoice(choice.getProgramVoiceId())
        .map(ProgramVoice::getName)
        .orElse("Unknown");
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> ProgramType.Beat.toString().equals(choice.getProgramType());
    var programNames = fabricator.sourceMaterial().getVoices(program.get()).stream()
      .map(ProgramVoice::getName)
      .collect(Collectors.toList());
    precomputeDeltas(
      choiceFilter,
      choiceIndexProvider,
      programNames,
      CSV.split(fabricator.getTemplateConfig().getDeltaArcBeatLayersToPrioritize()),
      fabricator.getTemplateConfig().getDeltaArcBeatLayersIncoming()
    );

    // beat sequence is selected at random of the current program
    // FUTURE: [#166855956] Beat Program with multiple Sequences
    var sequence = fabricator.getRandomlySelectedSequence(program.get());

    // voice arrangements
    if (sequence.isPresent()) {
      var voices = fabricator.sourceMaterial().getVoices(program.get());
      if (voices.isEmpty())
        reportMissing(ProgramVoice.class,
          String.format("in Beat-choice Program[%s]", program.get().getId()));

      craftChoices(sequence.get(), voices, voice -> chooseFreshInstrument(voice.getType(), List.of(), voice.getName()), true);
    }

    // add memes of program to segment in order to affect further choice
    // XJ should not add memes to Segment for program/instrument that was not successfully chosen #180468224
    fabricator.addMemes(program.get());

    // Finally, update the segment with the crafted content
    fabricator.done();
  }

}
