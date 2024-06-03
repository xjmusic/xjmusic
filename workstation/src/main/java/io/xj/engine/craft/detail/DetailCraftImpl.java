// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.detail;


import io.xj.model.enums.InstrumentType;
import io.xj.model.enums.ProgramType;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.pojos.Program;
import io.xj.model.util.StringUtils;
import io.xj.engine.FabricationException;
import io.xj.engine.craft.CraftImpl;
import io.xj.engine.fabricator.Fabricator;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;

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
  public void doWork() throws FabricationException {
    // Segments have delta arcs; automate mixer layers in and out of each main program https://github.com/xjmusic/workstation/issues/233
    ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) -> StringUtils.stringOrDefault(choice.getInstrumentType(), choice.getId().toString());
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> Objects.equals(ProgramType.Detail, choice.getProgramType());
    precomputeDeltas(choiceFilter, choiceIndexProvider, fabricator.getTemplateConfig().getDetailLayerOrder().stream().map(InstrumentType::toString).collect(Collectors.toList()), List.of(), fabricator.getTemplateConfig().getDeltaArcBeatLayersIncoming());

    // For each type of detail instrument type, choose instrument, then program if necessary
    for (InstrumentType instrumentType : DETAIL_INSTRUMENT_TYPES) {

      // Instrument is from prior choice, else freshly chosen
      Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(instrumentType);

      // Instruments may be chosen without programs https://github.com/xjmusic/workstation/issues/234
      Optional<Instrument> instrument = priorChoice.isPresent() ? fabricator.sourceMaterial().getInstrument(priorChoice.get().getInstrumentId()) : chooseFreshInstrument(instrumentType, Set.of());

      // Should gracefully skip voicing type if unfulfilled by detail instrument https://github.com/xjmusic/workstation/issues/240
      if (instrument.isEmpty()) {
        continue;
      }

      // Instruments have InstrumentMode https://github.com/xjmusic/workstation/issues/260
      switch (instrument.get().getMode()) {

        // Event instrument mode takes over legacy behavior https://github.com/xjmusic/workstation/issues/234
        case Event -> {
          // Event Use prior chosen program or find a new one
          Optional<Program> program = priorChoice.isPresent() ? fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()) : chooseFreshProgram(ProgramType.Detail, instrumentType);

          // Event Should gracefully skip voicing type if unfulfilled by detail program https://github.com/xjmusic/workstation/issues/240
          if (program.isEmpty()) {
            continue;
          }
          craftEventParts(fabricator.getTempo(), instrument.get(), program.get());
        }

        // Chord instrument mode https://github.com/xjmusic/workstation/issues/235
        case Chord -> craftChordParts(fabricator.getTempo(), instrument.get());

        case Loop -> {
          craftLoopParts(fabricator.getTempo(), instrument.get());
        }

        // As-yet Unsupported Modes
        default ->
          fabricator.addWarningMessage(String.format("Cannot craft unsupported mode %s for Instrument[%s]", instrument.get().getMode(), instrument.get().getId()));
      }
    }

  }

  /**
   Craft loop parts

   @param tempo of main program
   @param instrument for which to craft
   */
  @SuppressWarnings("DuplicatedCode")
  void craftLoopParts(double tempo, Instrument instrument) throws FabricationException {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType());
    choice.setInstrumentMode(instrument.getMode());
    choice.setInstrumentId(instrument.getId());
    fabricator.put(choice, false);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement, false);

    for (InstrumentAudio audio : selectGeneralAudioIntensityLayers(instrument)) {

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

}
