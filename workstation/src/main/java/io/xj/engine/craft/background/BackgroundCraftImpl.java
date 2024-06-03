// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.background;


import io.xj.model.enums.InstrumentType;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.engine.FabricationException;
import io.xj.engine.craft.CraftImpl;
import io.xj.engine.fabricator.Fabricator;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 Background craft for the current segment
 <p>
 Background-type Instrument https://github.com/xjmusic/workstation/issues/256
 */
public class BackgroundCraftImpl extends CraftImpl implements BackgroundCraft {

  public BackgroundCraftImpl(
      Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws FabricationException {
    Optional<SegmentChoice> previousChoice = fabricator.retrospective().getPreviousChoiceOfType(InstrumentType.Background);

    var instrument = previousChoice.isPresent() ?
        fabricator.sourceMaterial().getInstrument(previousChoice.get().getInstrumentId()) :
        chooseFreshInstrument(InstrumentType.Background, List.of());

    if (instrument.isEmpty()) {
      return;
    }

    craftBackground(instrument.get());
  }

  /**
   Craft percussion loop

   @param instrument for which to craft
   */
  @SuppressWarnings("DuplicatedCode")
  void craftBackground(Instrument instrument) throws FabricationException {
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
      var pick = new SegmentChoiceArrangementPick();
      pick.setId(UUID.randomUUID());
      pick.setSegmentId(fabricator.getSegment().getId());
      pick.setSegmentChoiceArrangementId(arrangement.getId());
      pick.setStartAtSegmentMicros(0L);
      pick.setLengthMicros(fabricator.getTotalSegmentMicros());
      pick.setAmplitude(1.0f);
      pick.setEvent("BACKGROUND");
      pick.setInstrumentAudioId(audio.getId());
      fabricator.put(pick, false);
    }
  }
}
