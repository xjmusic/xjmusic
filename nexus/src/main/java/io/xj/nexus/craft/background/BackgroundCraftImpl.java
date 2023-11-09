// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.background;


import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.MarbleBag;
import io.xj.hub.util.ValueUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Background craft for the current segment
 <p>
 Background-type Instrument https://www.pivotaltracker.com/story/show/180121388
 */
public class BackgroundCraftImpl extends DetailCraftImpl implements BackgroundCraft {

  public BackgroundCraftImpl(
    Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    List<SegmentChoice> previousChoices = fabricator.retrospective().getPreviousChoicesOfMode(InstrumentMode.Background);
    Collection<UUID> instrumentIds = previousChoices.stream().map(SegmentChoice::getInstrumentId).collect(Collectors.toList());

    int targetLayers = (int) Math.floor(
      fabricator.getTemplateConfig().getBackgroundLayerMin() +
        fabricator.getSegment().getDensity() *
          (fabricator.getTemplateConfig().getBackgroundLayerMax() -
            fabricator.getTemplateConfig().getBackgroundLayerMin()));

    fabricator.addInfoMessage(String.format("Targeting %d layers of background", targetLayers));

    if (instrumentIds.size() > targetLayers)
      instrumentIds = ValueUtils.withIdsRemoved(instrumentIds, instrumentIds.size() - targetLayers);

    for (UUID backgroundId : instrumentIds)
      craftBackground(backgroundId);

    Optional<Instrument> chosen;
    if (instrumentIds.size() < targetLayers)
      for (int i = 0; i < targetLayers - instrumentIds.size(); i++) {
        chosen = chooseFreshInstrument(List.of(), List.of(InstrumentMode.Background), instrumentIds, null, List.of());
        if (chosen.isPresent()) {
          instrumentIds.add(chosen.get().getId());
          craftBackground(chosen.get().getId());
        }
      }

    // Finally, update the segment with the crafted content
    fabricator.done();
  }

  /**
   Craft percussion loop

   @param instrumentId of percussion loop instrument to craft
   */
  @SuppressWarnings("DuplicatedCode")
  void craftBackground(UUID instrumentId) throws NexusException {
    var choice = new SegmentChoice();
    var instrument = fabricator.sourceMaterial().getInstrument(instrumentId)
      .orElseThrow(() -> new NexusException("Can't get Instrument Audio!"));
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType());
    choice.setInstrumentMode(instrument.getMode());
    choice.setInstrumentId(instrumentId);
    fabricator.put(choice);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement);

    // Start at zero and keep laying down perc loops until we're out of here
    var audio = pickAudioForInstrument(instrument);
    if (audio.isEmpty()) return;

    var pick = new SegmentChoiceArrangementPick();
    pick.setId(UUID.randomUUID());
    pick.setSegmentId(fabricator.getSegment().getId());
    pick.setSegmentChoiceArrangementId(arrangement.getId());
    pick.setStartAtSegmentMicros(0L);
    pick.setLengthMicros(fabricator.getTotalSegmentMicros());
    pick.setAmplitude(1.0f);
    pick.setEvent("BACKGROUND");
    pick.setInstrumentAudioId(audio.get().getId());
    fabricator.put(pick);
  }

  /**
   Choose drum instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @param instrument for which to pick audio
   @return drum-type Instrument
   */
  Optional<InstrumentAudio> pickAudioForInstrument(Instrument instrument) {
    var arr = fabricator.retrospective().getPreviousPicksForInstrument(instrument.getId());
    if (fabricator.getInstrumentConfig(instrument).isAudioSelectionPersistent() && !arr.isEmpty())
      return fabricator.sourceMaterial().getInstrumentAudio(arr.get(0).getInstrumentAudioId());

    var bag = MarbleBag.empty();

    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosForInstrumentId(instrument.getId()))
      bag.add(1, audio.getId());

    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }

}
