// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.detail;


import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 Detail craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class DetailCraftImpl extends CraftImpl implements DetailCraft {
  public DetailCraftImpl(Fabricator fabricator) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    // Segments have intensity arcs; automate mixer layers in and out of each main program https://www.pivotaltracker.com/story/show/178240332
    ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) -> StringUtils.stringOrDefault(choice.getInstrumentType(), choice.getId().toString());
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> Objects.equals(ProgramType.Detail, choice.getProgramType());
    precomputeDeltas(choiceFilter, choiceIndexProvider, fabricator.getTemplateConfig().getDetailLayerOrder().stream().map(InstrumentType::toString).collect(Collectors.toList()), List.of(), fabricator.getTemplateConfig().getDeltaArcDetailLayersIncoming());

    // For each type of voicing present in the main sequence, choose instrument, then program if necessary
    for (InstrumentType voicingType : fabricator.getTemplateConfig().getDetailLayerOrder().stream().filter(fabricator.getDistinctChordVoicingTypes()::contains).toList()) {

      // Instrument is from prior choice, else freshly chosen
      Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(voicingType);

      // Instruments may be chosen without programs
      // https://www.pivotaltracker.com/story/show/181290857
      Optional<Instrument> instrument = priorChoice.isPresent() ? fabricator.sourceMaterial().getInstrument(priorChoice.get().getInstrumentId()) : chooseFreshInstrument(List.of(voicingType), List.of(), List.of(), null, List.of());

      // Should gracefully skip voicing type if unfulfilled by detail instrument
      // https://www.pivotaltracker.com/story/show/176373977
      if (instrument.isEmpty()) {
        reportMissing(Instrument.class, String.format("%s-type Instrument", voicingType));
        continue;
      }

      // Instruments have InstrumentMode
      // https://www.pivotaltracker.com/story/show/181134085
      switch (instrument.get().getMode()) {

        // Event instrument mode takes over legacy behavior
        // https://www.pivotaltracker.com/story/show/181736854
        case Event -> {
          // Event Use prior chosen program or find a new one
          Optional<Program> program = priorChoice.isPresent() ? fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()) : chooseFreshProgram(ProgramType.Detail, voicingType);

          // Event Should gracefully skip voicing type if unfulfilled by detail program
          // https://www.pivotaltracker.com/story/show/176373977
          if (program.isEmpty()) {
            reportMissing(Program.class, String.format("%s-type Program", voicingType));
            continue;
          }
          craftEventParts(instrument.get(), program.get());
        }

        // Chord instrument mode
        // https://www.pivotaltracker.com/story/show/181631275
        case Chord -> craftChordParts(instrument.get());

        // As-yet Unsupported Modes
        default ->
          fabricator.addWarningMessage(String.format("Cannot craft unsupported mode %s for Instrument[%s]", instrument.get().getMode(), instrument.get().getId()));
      }
    }

    // Finally, update the segment with the crafted content
    fabricator.done();
  }
}
