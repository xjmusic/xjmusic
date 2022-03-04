// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.detail;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.SegmentChoice;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 Detail craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class DetailCraftImpl extends CraftImpl implements DetailCraft {
  public static final List<String> DETAIL_INSTRUMENT_TYPES =
    ImmutableList.of(
      InstrumentType.Bass,
      InstrumentType.Stripe,
      InstrumentType.Pad,
      InstrumentType.Sticky,
      InstrumentType.Stab
    ).stream().map(InstrumentType::toString).collect(Collectors.toList());

  @Inject
  public DetailCraftImpl(
    @Assisted("basis") Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    // [#178240332] Segments have intensity arcs; automate mixer layers in and out of each main program
    ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) -> Values.stringOrDefault(choice.getInstrumentType(), choice.getId().toString());
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> Objects.equals(ProgramType.Detail.toString(), choice.getProgramType());
    precomputeDeltas(
      choiceFilter,
      choiceIndexProvider,
      DETAIL_INSTRUMENT_TYPES,
      List.of(),
      fabricator.getTemplateConfig().getDeltaArcDetailLayersIncoming()
    );

    for (InstrumentType voicingType : fabricator.getDistinctChordVoicingTypes()) {
      Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(voicingType);

      // Program is from prior choice, or freshly chosen
      Optional<Program> program = priorChoice.isPresent() ?
        fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()) :
        chooseFreshProgram(ProgramType.Detail, voicingType);

      // [#176373977] Should gracefully skip voicing type if unfulfilled by detail program
      if (program.isEmpty()) {
        reportMissing(Program.class, String.format("Detail-type with voicing-type %s", voicingType));
        continue;
      }

      // detail sequence is selected at random of the current program
      // FUTURE: [#166855956] Detail Program with multiple Sequences
      var sequence = fabricator.getRandomlySelectedSequence(program.get());

      // voice arrangements
      if (sequence.isPresent()) {
        var voices = fabricator.sourceMaterial().getVoices(program.get());
        if (voices.isEmpty())
          reportMissing(ProgramVoice.class,
            String.format("in Detail-choice Program[%s]", program.get().getId()));
        craftChoices(sequence.get(), voices, voice -> chooseFreshInstrument(voice.getType(), List.of(), null, List.of()), false);
      }
    }

    // Finally, update the segment with the crafted content
    fabricator.done();
  }
}
