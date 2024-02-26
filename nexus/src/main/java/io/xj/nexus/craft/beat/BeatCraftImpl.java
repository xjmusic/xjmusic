// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.beat;


import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.util.CsvUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 Beat craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 <p>
 BeatCraftImpl extends DetailCraftImpl to leverage all detail craft enhancements https://www.pivotaltracker.com/story/show/176625174
 */
public class BeatCraftImpl extends CraftImpl implements BeatCraft {

  public BeatCraftImpl(
    Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    Optional<SegmentChoice> priorBeatChoice = fabricator.getChoicesIfContinued(ProgramType.Beat).stream().findFirst();

    // Program is from prior choice, or freshly chosen
    Optional<Program> program = priorBeatChoice.isPresent() ?
      fabricator.sourceMaterial().getProgram(priorBeatChoice.get().getProgramId()) :
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
      CsvUtils.split(fabricator.getTemplateConfig().getIntensityAutoCrescendoBeatLayersToPrioritize()),
      fabricator.getTemplateConfig().getIntensityAutoCrescendoBeatLayersIncoming()
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

      for (ProgramVoice voice : voices) {
        var choice = new SegmentChoice();
        choice.setId(UUID.randomUUID());
        choice.setSegmentId(fabricator.getSegment().getId());
        choice.setMute(computeMute(voice.getType()));
        choice.setProgramType(fabricator.sourceMaterial().getProgram(voice.getProgramId()).orElseThrow(() -> new NexusException("Can't get program for voice")).getType());
        choice.setInstrumentType(voice.getType());
        choice.setProgramId(voice.getProgramId());
        choice.setProgramSequenceId(sequence.get().getId());
        choice.setProgramVoiceId(voice.getId());

        // Whether there is a prior choice for this voice
        Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(voice);

        if (priorChoice.isPresent()) {
          // If there is a prior choice, then we should continue it
          choice.setDeltaIn(priorChoice.get().getDeltaIn());
          choice.setDeltaOut(priorChoice.get().getDeltaOut());
          choice.setInstrumentId(priorChoice.get().getInstrumentId());
          choice.setInstrumentMode(priorChoice.get().getInstrumentMode());
          this.craftNoteEventArrangements(fabricator.getTempo(), fabricator.put(choice, false), true);
        } else {
          // If there is no prior choice, then we should choose a fresh instrument
          var instrument = chooseFreshInstrument(InstrumentType.Drum, List.of(), voice.getName(), fabricator.sourceMaterial().getTrackNamesOfVoice(voice));
          if (instrument.isEmpty()) {
            reportMissing(Instrument.class, String.format("for voice[%s]", voice.getId()));
            continue;
          }
          choice.setDeltaIn(computeDeltaIn(choice));
          choice.setDeltaOut(computeDeltaOut(choice));
          choice.setInstrumentId(instrument.get().getId());
          choice.setInstrumentMode(instrument.get().getMode());
          this.craftNoteEventArrangements(fabricator.getTempo(), fabricator.put(choice, false), true);
        }
      }
    }
  }
}
