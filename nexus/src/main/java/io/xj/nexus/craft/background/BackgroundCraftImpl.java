// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.background;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.*;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.entity.Entities;
import io.xj.lib.util.Chance;
import io.xj.lib.util.TremendouslyRandom;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.fabricator.EntityScorePicker;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.MemeIsometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 Background craft for the current segment
 <p>
 Background-type Instrument #180121388
 */
public class BackgroundCraftImpl extends DetailCraftImpl implements BackgroundCraft {
  private final Logger log = LoggerFactory.getLogger(BackgroundCraftImpl.class);

  @Inject
  public BackgroundCraftImpl(
    @Assisted("basis") Fabricator fabricator
  ) {
    super(fabricator);
  }

  /**
   Remove some number of ids from the list

   @param fromIds to begin with
   @param count   number of ids to add
   @return list including added ids
   */
  public static List<UUID> withIdsRemoved(List<UUID> fromIds, int count) {
    var ids = new ArrayList<>(fromIds);
    for (int i = 0; i < count; i++)
      ids.remove((int) TremendouslyRandom.zeroToLimit(ids.size()));
    return ids;
  }

  @Override
  public void doWork() throws NexusException {
    List<SegmentChoice> previousChoices = fabricator.retrospective().getPreviousChoicesOfType(InstrumentType.Background);
    List<UUID> instrumentIds = previousChoices.stream().map(SegmentChoice::getInstrumentId).collect(Collectors.toList());

    int targetLayers = (int) Math.floor(
      fabricator.getTemplateConfig().getBackgroundLayerMin() +
        fabricator.getSegment().getDensity() *
          (fabricator.getTemplateConfig().getBackgroundLayerMax() -
            fabricator.getTemplateConfig().getBackgroundLayerMin()));

    fabricator.addInfoMessage(String.format("Targeting %d layers of background", targetLayers));

    if (instrumentIds.size() > targetLayers)
      instrumentIds = withIdsRemoved(instrumentIds, instrumentIds.size() - targetLayers);

    for (UUID backgroundId : instrumentIds)
      craftBackground(backgroundId);

    Optional<Instrument> chosen;
    if (instrumentIds.size() < targetLayers)
      for (int i = 0; i < targetLayers - instrumentIds.size(); i++) {
        chosen = chooseFreshBackgroundInstrument(instrumentIds);
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
  private void craftBackground(UUID instrumentId) throws NexusException {
    fabricator.addMemes(fabricator.sourceMaterial().getInstrument(instrumentId)
      .orElseThrow(() -> new NexusException("Failed to get instrument!")));
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setInstrumentType(InstrumentType.Background.toString());
    choice.setInstrumentId(instrumentId);
    fabricator.add(choice);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.add(arrangement);

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
    pick.setName("BACKGROUND");
    pick.setInstrumentAudioId(audio.get().getId());
    fabricator.add(pick);
  }

  /**
   Choose drum instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @return drum-type Instrument
   */
  private Optional<Instrument> chooseFreshBackgroundInstrument(List<UUID> avoidInstrumentIds) {
    EntityScorePicker<Instrument> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> sourceInstruments =
      fabricator.sourceMaterial().getInstrumentsOfType(InstrumentType.Background)
        .stream()
        .filter(i -> !avoidInstrumentIds.contains(i.getId()))
        .toList();

    // future: [#258] Instrument selection is based on Text Isometry between the voice name and the instrument name
    log.debug("[segId={}] not currently in use", fabricator.getSegment().getId());

    // (3) score each source instrument based on meme isometry
    MemeIsometry iso = fabricator.getMemeIsometryOfSegment();
    Collection<String> memes;
    for (Instrument instrument : sourceInstruments) {
      memes = Entities.namesOf(fabricator.sourceMaterial().getMemes(instrument));
      if (iso.isAllowed(memes))
        superEntityScorePicker.add(instrument, score(iso, instrument, memes));
    }

    // report
    fabricator.putReport("percussiveChoice", superEntityScorePicker.report());

    // (4) return the top choice
    return superEntityScorePicker.getTop();
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

    EntityScorePicker<InstrumentAudio> superEntityScorePicker = new EntityScorePicker<>();

    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosForInstrumentId(instrumentId))
      superEntityScorePicker.add(audio, Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_INSTRUMENT));

    return superEntityScorePicker.getTop();
  }

}
