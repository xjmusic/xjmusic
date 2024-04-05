// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.background;


import io.xj.hub.enums.InstrumentType;
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 Background craft for the current segment
 <p>
 Background-type Instrument https://www.pivotaltracker.com/story/show/180121388
 */
public class BackgroundCraftImpl extends CraftImpl implements BackgroundCraft {

  public BackgroundCraftImpl(
      Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
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
  void craftBackground(Instrument instrument) throws NexusException {
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
