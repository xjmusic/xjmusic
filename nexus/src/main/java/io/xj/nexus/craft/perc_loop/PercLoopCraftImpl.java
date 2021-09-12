// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.perc_loop;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.Instrument;
import io.xj.api.InstrumentAudio;
import io.xj.api.InstrumentState;
import io.xj.api.InstrumentType;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentMessage;
import io.xj.api.SegmentMessageType;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 PercLoop craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 <p>
 [#176625174] PercLoopCraftImpl extends DetailCraftImpl to leverage all detail craft enhancements
 */
public class PercLoopCraftImpl extends DetailCraftImpl implements PercLoopCraft {
  private final Logger log = LoggerFactory.getLogger(PercLoopCraftImpl.class);

  @Inject
  public PercLoopCraftImpl(
    @Assisted("basis") Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    List<SegmentChoice> previousChoices = fabricator.retrospective().getPreviousChoicesOfType(InstrumentType.PERCLOOP);
    List<UUID> instrumentIds = previousChoices.stream().map(SegmentChoice::getInstrumentId).collect(Collectors.toList());

    int targetLayers = (int) Math.floor(
      fabricator.getTemplateConfig().getPercLoopLayerMin() +
        fabricator.getSegment().getDensity() *
          (fabricator.getTemplateConfig().getPercLoopLayerMax() -
            fabricator.getTemplateConfig().getPercLoopLayerMin()));

    fabricator.add(new SegmentMessage()
      .id(UUID.randomUUID())
      .segmentId(fabricator.getSegment().getId())
      .type(SegmentMessageType.INFO)
      .body(String.format("Targeting %d layers of percussion loop", targetLayers)));

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
    var choice = fabricator.add(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(fabricator.getSegment().getId())
      .instrumentType(InstrumentType.PERCLOOP)
      .instrumentId(instrumentId));
    var arrangement = fabricator.add(new SegmentChoiceArrangement()
      .id(UUID.randomUUID())
      .segmentId(fabricator.getSegment().getId())
      .segmentChoiceId(choice.getId()));

    // Start at zero and keep laying down perc loops until we're outta here
    double pos = 0;
    while (pos < fabricator.getSegment().getTotal()) {
      var audio = pickAudioForInstrument(instrumentId);

      // [#176373977] Should gracefully skip audio in unfulfilled by instrument
      if (audio.isEmpty()) return;

      // Pick attributes are expressed "rendered" as actual seconds
      double startSeconds = fabricator.getSecondsAtPosition(pos);
      double lengthSeconds = fabricator.getSecondsAtPosition(pos + fabricator.getTemplateConfig().getPercLoopFixedSizeBeats()) - startSeconds;

      // of pick
      fabricator.add(new SegmentChoiceArrangementPick()
        .id(UUID.randomUUID())
        .segmentId(fabricator.getSegment().getId())
        .segmentChoiceArrangementId(arrangement.getId())
        .start(startSeconds)
        .length(lengthSeconds)
        .amplitude(1.0)
        .name("PERCLOOP")
        .instrumentAudioId(audio.get().getId()));

      pos += fabricator.getTemplateConfig().getPercLoopFixedSizeBeats();
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

  /**
   Choose drum instrument
   [#325] Possible to choose multiple instruments for different voices in the same program

   @return drum-type Instrument
   */
  private Optional<Instrument> chooseFreshPercLoopInstrument(List<UUID> avoidInstrumentIds) {
    EntityScorePicker<Instrument> superEntityScorePicker = new EntityScorePicker<>();

    // (2) retrieve instruments bound to chain
    Collection<Instrument> sourceInstruments = fabricator.sourceMaterial().getInstrumentsOfType(InstrumentType.PERCLOOP);

    // future: [#258] Instrument selection is based on Text Isometry between the voice name and the instrument name
    log.debug("[segId={}] not currently in use", fabricator.getSegment().getId());

    // (3) score each source instrument based on meme isometry
    MemeIsometry percussiveIsometry = fabricator.getMemeIsometryOfSegment();
    for (Instrument instrument : sourceInstruments)
      if (!avoidInstrumentIds.contains(instrument.getId()))
        superEntityScorePicker.add(instrument, scorePercLoop(instrument, percussiveIsometry));

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
    EntityScorePicker<InstrumentAudio> superEntityScorePicker = new EntityScorePicker<>();

    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosForInstrumentId(instrumentId))
      superEntityScorePicker.add(audio, Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_INSTRUMENT));

    return superEntityScorePicker.getTop();
  }

  /**
   Score a candidate for drum instrument, given current fabricator

   @param instrument         to score
   @param percussiveIsometry from which to score drum instruments
   @return score, including +/- entropy
   */
  private double scorePercLoop(Instrument instrument, MemeIsometry percussiveIsometry) {
    double score = Chance.normallyAround(0, SCORE_ENTROPY_CHOICE_INSTRUMENT);

    // Score includes matching memes, previous segment to macro instrument first pattern
    score += SCORE_MATCH_MEMES *
      percussiveIsometry.score(Entities.namesOf(fabricator.sourceMaterial().getMemes(instrument)));

    // [#174435421] Chain bindings specify Program & Instrument within Library
    if (fabricator.isDirectlyBound(instrument))
      score += SCORE_DIRECTLY_BOUND;
    else if (Objects.equals(instrument.getState(), InstrumentState.DRAFT))
      score += SCORE_UNPUBLISHED;

    return score;
  }

}
