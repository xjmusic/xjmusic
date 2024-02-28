// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.background;


import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;

import java.util.Collection;
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
      reportMissing(Instrument.class, "Background-type instrument");
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

    for (InstrumentAudio audio : pickAudiosForInstrument(instrument)) {
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

  /**
   Choose drum instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @param instrument for which to pick audio
   @return drum-type Instrument
   */
  Collection<InstrumentAudio> pickAudiosForInstrument(Instrument instrument) throws NexusException {
    var previousPicksOfInstrument = fabricator.retrospective().getPreviousPicksForInstrument(instrument.getId());
    if (fabricator.getInstrumentConfig(instrument).isAudioSelectionPersistent() && !previousPicksOfInstrument.isEmpty()) {
      return previousPicksOfInstrument.stream()
          .map(pick -> fabricator.sourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId()))
          .filter(Optional::isPresent)
          .map(Optional::get)
          .toList();
    }

    return pickAudioIntensityLayers(
        fabricator.sourceMaterial().getAudiosOfInstrument(instrument.getId()),
        fabricator.getTemplateConfig().getIntensityLayers(InstrumentType.Background)
    );
  }
}
