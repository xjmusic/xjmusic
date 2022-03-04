// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.perc_loop;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.util.MarbleBag;
import io.xj.lib.util.TremendouslyRandom;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.beat.BeatCraftImpl;
import io.xj.nexus.fabricator.Fabricator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 PercLoop craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 <p>
 [#176625174] PercLoopCraftImpl extends DetailCraftImpl to leverage all detail craft enhancements
 */
public class PercLoopCraftImpl extends BeatCraftImpl implements PercLoopCraft {
  @Inject
  public PercLoopCraftImpl(
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
    List<SegmentChoice> previousChoices = fabricator.retrospective().getPreviousChoicesOfType(InstrumentType.PercLoop);
    List<UUID> instrumentIds = previousChoices.stream().map(SegmentChoice::getInstrumentId).collect(Collectors.toList());

    int targetLayers = (int) Math.floor(
      fabricator.getTemplateConfig().getPercLoopLayerMin() +
        fabricator.getSegment().getDensity() *
          (fabricator.getTemplateConfig().getPercLoopLayerMax() -
            fabricator.getTemplateConfig().getPercLoopLayerMin()));

    fabricator.addInfoMessage(String.format("Targeting %d layers of percussion loop", targetLayers));

    if (instrumentIds.size() > targetLayers)
      instrumentIds = withIdsRemoved(instrumentIds, instrumentIds.size() - targetLayers);

    for (UUID percLoopId : instrumentIds)
      craftPercLoop(percLoopId);

    Optional<Instrument> chosen;
    if (instrumentIds.size() < targetLayers)
      for (int i = 0; i < targetLayers - instrumentIds.size(); i++) {
        chosen = chooseFreshInstrument(InstrumentType.PercLoop, instrumentIds, null, List.of());
        if (chosen.isPresent()) {
          instrumentIds.add(chosen.get().getId());
          craftPercLoop(chosen.get().getId());
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
  private void craftPercLoop(UUID instrumentId) throws NexusException {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setInstrumentType(InstrumentType.PercLoop.toString());
    choice.setInstrumentId(instrumentId);
    fabricator.put(choice);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement);

    // Start at zero and keep laying down perc loops until we're out of here
    double pos = 0;
    var audio = pickAudioForInstrument(instrumentId);
    while (pos < fabricator.getSegment().getTotal()) {

      // [#176373977] Should gracefully skip audio in unfulfilled by instrument
      if (audio.isEmpty()) return;

      // Pick attributes are expressed "rendered" as actual seconds
      double startSeconds = fabricator.getSecondsAtPosition(pos);
      double lengthSeconds = fabricator.getSecondsAtPosition(pos + audio.get().getTotalBeats()) - startSeconds;

      // of pick
      var pick = new SegmentChoiceArrangementPick();
      pick.setId(UUID.randomUUID());
      pick.setSegmentId(fabricator.getSegment().getId());
      pick.setSegmentChoiceArrangementId(arrangement.getId());
      pick.setStart(startSeconds);
      pick.setLength(lengthSeconds);
      pick.setAmplitude(1.0);
      pick.setEvent("PERCLOOP");
      pick.setInstrumentAudioId(audio.get().getId());
      fabricator.put(pick);

      pos += audio.get().getTotalBeats();
    }
  }

  /**
   Choose drum instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @return drum-type Instrument
   */
  private Optional<InstrumentAudio> pickAudioForInstrument(UUID instrumentId) {
    var pick = fabricator.retrospective().getPreviousPicksForInstrument(instrumentId).stream().findAny();
    if (pick.isPresent())
      return fabricator.sourceMaterial().getInstrumentAudio(pick.get().getInstrumentAudioId());

    var bag = MarbleBag.empty();

    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosForInstrumentId(instrumentId))
      bag.add(1, audio.getId());

    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }

}
