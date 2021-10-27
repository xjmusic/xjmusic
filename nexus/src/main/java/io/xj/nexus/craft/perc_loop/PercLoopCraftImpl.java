// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.perc_loop;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.*;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.util.Chance;
import io.xj.lib.util.TremendouslyRandom;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.rhythm.RhythmCraftImpl;
import io.xj.nexus.fabricator.EntityScorePicker;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.MemeIsometry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 PercLoop craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 <p>
 [#176625174] PercLoopCraftImpl extends DetailCraftImpl to leverage all detail craft enhancements
 */
public class PercLoopCraftImpl extends RhythmCraftImpl implements PercLoopCraft {
  private final Logger log = LoggerFactory.getLogger(PercLoopCraftImpl.class);

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

    var msg = new SegmentMessage();
    msg.setId(UUID.randomUUID());
    msg.setSegmentId(fabricator.getSegment().getId());
    msg.setType(SegmentMessageType.INFO);
    msg.setBody(String.format("Targeting %d layers of percussion loop", targetLayers));
    fabricator.add(msg);

    if (instrumentIds.size() < targetLayers)
      instrumentIds = withIdsAdded(instrumentIds, targetLayers - instrumentIds.size());
    else if (instrumentIds.size() > targetLayers)
      instrumentIds = withIdsRemoved(instrumentIds, instrumentIds.size() - targetLayers);

    for (UUID percLoopId : instrumentIds)
      craftPercLoop(percLoopId);

    // Finally, update the segment with the crafted content
    fabricator.done();
  }

  /**
   Craft percussion loop

   @param instrumentId of percussion loop instrument to craft
   */
  private void craftPercLoop(UUID instrumentId) throws NexusException {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setInstrumentType(InstrumentType.PercLoop.toString());
    choice.setInstrumentId(instrumentId);
    fabricator.add(choice);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.add(arrangement);

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
      pick.setName("PERCLOOP");
      pick.setInstrumentAudioId(audio.get().getId());
      fabricator.add(pick);

      pos += audio.get().getTotalBeats();
    }
  }

  /**
   Add some number of ids to the list

   @param fromIds to begin with
   @param count   number of ids to add
   @return list including added ids
   */
  public List<UUID> withIdsAdded(List<UUID> fromIds, int count) {
    var ids = new ArrayList<>(fromIds);
    for (int i = 0; i < count; i++)
      chooseFreshPercLoopInstrument(ids)
        .ifPresent((instrument) -> ids.add(instrument.getId()));
    return ids;
  }

  /**
   Choose drum instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @return drum-type Instrument
   */
  private Optional<Instrument> chooseFreshPercLoopInstrument(List<UUID> avoidInstrumentIds) {
    EntityScorePicker<Instrument> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> sourceInstruments = fabricator.sourceMaterial().getInstrumentsOfType(InstrumentType.PercLoop);

    // future: [#258] Instrument selection is based on Text Isometry between the voice name and the instrument name
    log.debug("[segId={}] not currently in use", fabricator.getSegment().getId());

    // (3) score each source instrument based on meme isometry
    MemeIsometry percussiveIsometry = fabricator.getMemeIsometryOfSegment();
    for (Instrument instrument : sourceInstruments)
      if (!avoidInstrumentIds.contains(instrument.getId()))
        superEntityScorePicker.add(instrument, scorePercussive(instrument, percussiveIsometry));

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
