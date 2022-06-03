// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.background;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.util.MarbleBag;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.fabricator.Fabricator;

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

  @Inject
  public BackgroundCraftImpl(
    @Assisted("basis") Fabricator fabricator
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
      instrumentIds = Values.withIdsRemoved(instrumentIds, instrumentIds.size() - targetLayers);

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
  private void craftBackground(UUID instrumentId) throws NexusException {
    var choice = new SegmentChoice();
    var instrument = fabricator.sourceMaterial().getInstrument(instrumentId)
      .orElseThrow(() -> new NexusException("Can't get Instrument Audio!"));
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setInstrumentType(instrument.getType().toString());
    choice.setInstrumentMode(instrument.getMode().toString());
    choice.setInstrumentId(instrumentId);
    fabricator.put(choice);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement);

    // Start at zero and keep laying down perc loops until we're out of here
    var audio = pickAudioForInstrument(instrumentId);
    if (audio.isEmpty()) return;

    var pick = new SegmentChoiceArrangementPick();
    pick.setId(UUID.randomUUID());
    pick.setSegmentId(fabricator.getSegment().getId());
    pick.setSegmentChoiceArrangementId(arrangement.getId());
    pick.setStart(0.0);
    pick.setLength(fabricator.getTotalSeconds());
    pick.setAmplitude(1.0);
    pick.setEvent("BACKGROUND");
    pick.setInstrumentAudioId(audio.get().getId());
    fabricator.put(pick);
  }

  /**
   Choose drum instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @return drum-type Instrument
   */
  private Optional<InstrumentAudio> pickAudioForInstrument(UUID instrumentId) {
    var arr = fabricator.retrospective().getPreviousPicksForInstrument(instrumentId);
    if (!arr.isEmpty())
      return fabricator.sourceMaterial().getInstrumentAudio(arr.get(0).getInstrumentAudioId());

    var bag = MarbleBag.empty();

    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosForInstrumentId(instrumentId))
      bag.add(1, audio.getId());

    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }

}
