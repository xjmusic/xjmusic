// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.detail;


import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 Detail craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class DetailCraftImpl extends CraftImpl implements DetailCraft {
  private static final Collection<InstrumentType> DETAIL_INSTRUMENT_TYPES = Set.of(
      InstrumentType.Bass,
      InstrumentType.Pad,
      InstrumentType.Sticky,
      InstrumentType.Stripe,
      InstrumentType.Stab,
      InstrumentType.Hook,
      InstrumentType.Percussion
  );

  public DetailCraftImpl(Fabricator fabricator) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    // Segments have delta arcs; automate mixer layers in and out of each main program https://www.pivotaltracker.com/story/show/178240332
    ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) -> StringUtils.stringOrDefault(choice.getInstrumentType(), choice.getId().toString());
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> Objects.equals(ProgramType.Detail, choice.getProgramType());
    precomputeDeltas(choiceFilter, choiceIndexProvider, fabricator.getTemplateConfig().getDetailLayerOrder().stream().map(InstrumentType::toString).collect(Collectors.toList()), List.of(), fabricator.getTemplateConfig().getDeltaArcBeatLayersIncoming());

    // For each type of detail instrument type, choose instrument, then program if necessary
    for (InstrumentType instrumentType : DETAIL_INSTRUMENT_TYPES) {

      // Instrument is from prior choice, else freshly chosen
      Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(instrumentType);

      // Instruments may be chosen without programs https://www.pivotaltracker.com/story/show/181290857
      Optional<Instrument> instrument = priorChoice.isPresent() ? fabricator.sourceMaterial().getInstrument(priorChoice.get().getInstrumentId()) : chooseFreshInstrument(instrumentType, Set.of());

      // Should gracefully skip voicing type if unfulfilled by detail instrument https://www.pivotaltracker.com/story/show/176373977
      if (instrument.isEmpty()) {
        reportMissing(Instrument.class, String.format("%s-type Instrument", instrumentType));
        continue;
      }

      // Instruments have InstrumentMode https://www.pivotaltracker.com/story/show/181134085
      switch (instrument.get().getMode()) {

        // Event instrument mode takes over legacy behavior https://www.pivotaltracker.com/story/show/181736854
        case Event -> {
          // Event Use prior chosen program or find a new one
          Optional<Program> program = priorChoice.isPresent() ? fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()) : chooseFreshProgram(ProgramType.Detail, instrumentType);

          // Event Should gracefully skip voicing type if unfulfilled by detail program https://www.pivotaltracker.com/story/show/176373977
          if (program.isEmpty()) {
            reportMissing(Program.class, String.format("%s-type Program", instrumentType));
            continue;
          }
          craftEventParts(fabricator.getTempo(), instrument.get(), program.get());
        }

        // Chord instrument mode https://www.pivotaltracker.com/story/show/181631275
        case Chord -> craftChordParts(fabricator.getTempo(), instrument.get());

        case Loop -> {
          for (InstrumentAudio audio : selectGeneralAudioIntensityLayers(instrument.get())) {
            craftLoop(fabricator.getTempo(), audio);
          }
        }

        // As-yet Unsupported Modes
        default ->
            fabricator.addWarningMessage(String.format("Cannot craft unsupported mode %s for Instrument[%s]", instrument.get().getMode(), instrument.get().getId()));
      }
    }

  }

  /**
   Craft loop

   @param tempo of main program
   @param audio for which to craft segment
   */
  @SuppressWarnings("DuplicatedCode")
  void craftLoop(double tempo, InstrumentAudio audio) throws NexusException {
    var choice = new SegmentChoice();
    var instrument = fabricator.sourceMaterial().getInstrument(audio.getInstrumentId())
        .orElseThrow(() -> new NexusException("Can't get Instrument Audio!"));
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType());
    choice.setInstrumentMode(instrument.getMode());
    choice.setInstrumentId(audio.getInstrumentId());
    fabricator.put(choice, false);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement, false);

    // Start at zero and keep laying down loops until we're out of here
    float beats = 0;
    while (beats < fabricator.getSegment().getTotal()) {

      // Pick attributes are expressed "rendered" as actual seconds
      long startAtSegmentMicros = fabricator.getSegmentMicrosAtPosition(tempo, beats);
      long lengthMicros = Math.min(
          fabricator.getTotalSegmentMicros() - startAtSegmentMicros,
          (long) (audio.getLoopBeats() * fabricator.getMicrosPerBeat(tempo))
      );

      // of pick
      var pick = new SegmentChoiceArrangementPick();
      pick.setId(UUID.randomUUID());
      pick.setSegmentId(fabricator.getSegment().getId());
      pick.setSegmentChoiceArrangementId(arrangement.getId());
      pick.setStartAtSegmentMicros(startAtSegmentMicros);
      pick.setLengthMicros(lengthMicros);
      pick.setAmplitude(1.0f);
      pick.setEvent("LOOP");
      pick.setInstrumentAudioId(audio.getId());
      fabricator.put(pick, false);

      beats += audio.getLoopBeats();
    }
  }

}
