// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.perc_loop;

import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.beat.BeatCraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentType;

import java.util.*;
import java.util.stream.Collectors;

/**
 Percussion-type Loop-mode craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 <p>
 PercLoopCraftImpl extends DetailCraftImpl to leverage all detail craft enhancements https://www.pivotaltracker.com/story/show/176625174
 */
public class PercLoopCraftImpl extends BeatCraftImpl implements PercLoopCraft {
  public PercLoopCraftImpl(
    Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    var choices = fabricator.retrospective().getPreviousChoicesOfTypeMode(InstrumentType.Percussion, InstrumentMode.Loop);
    var instruments = choices.stream().flatMap(choice -> fabricator.sourceMaterial().getInstrument(choice.getInstrumentId()).stream()).toList();

    Collection<UUID> audioIds =
      SegmentType.CONTINUE.equals(fabricator.getType()) ?
        instruments.stream()
          .flatMap(instrument -> fabricator.retrospective().getPreviousPicksForInstrument(instrument.getId()).stream())
          .map(SegmentChoiceArrangementPick::getInstrumentAudioId)
          .collect(Collectors.toSet())
        : new ArrayList<>();

    int targetLayers = (int) Math.floor(
      fabricator.getTemplateConfig().getPercLoopLayerMin() +
        fabricator.getSegment().getDensity() *
          (fabricator.getTemplateConfig().getPercLoopLayerMax() -
            fabricator.getTemplateConfig().getPercLoopLayerMin()));

    fabricator.addInfoMessage(String.format("Targeting %d layers of percussion loop", targetLayers));

    if (audioIds.size() > targetLayers)
      audioIds = ValueUtils.withIdsRemoved(audioIds, audioIds.size() - targetLayers);

    else if (audioIds.size() < targetLayers)
      for (int i = 0; i < targetLayers - audioIds.size(); i++) {
        Optional<InstrumentAudio> chosen = chooseFreshInstrumentAudio(List.of(InstrumentType.Percussion), List.of(InstrumentMode.Loop), audioIds, computePreferredEvents(audioIds.size()));
        if (chosen.isPresent()) {
          audioIds.add(chosen.get().getId());
        }
      }

    for (InstrumentAudio audio : audioIds.stream()
      .flatMap(audioId -> fabricator.sourceMaterial().getInstrumentAudio(audioId).stream())
      .toList())
      craftPercLoop(fabricator.getTempo(), audio);
  }

  /**
   Percussion-type Loop-mode instrument audios are chosen in order of priority
   https://www.pivotaltracker.com/story/show/181262545

   @param after # of choices
   @return required event name
   */
  List<String> computePreferredEvents(int after) {
    return switch (after) {
      case 0 -> fabricator.getTemplateConfig().getEventNamesLarge().stream()
        .map(StringUtils::toEvent)
        .toList();

      case 1 -> fabricator.getTemplateConfig().getEventNamesMedium().stream()
        .map(StringUtils::toEvent)
        .toList();

      default -> fabricator.getTemplateConfig().getEventNamesSmall().stream()
        .map(StringUtils::toEvent)
        .toList();
    };
  }

  /**
   Craft percussion loop

   @param tempo of main program
   @param audio for which to craft segment
   */
  @SuppressWarnings("DuplicatedCode")
  void craftPercLoop(double tempo, InstrumentAudio audio) throws NexusException {
    var choice = new SegmentChoice();
    var instrument = fabricator.sourceMaterial().getInstrument(audio.getInstrumentId())
      .orElseThrow(() -> new NexusException("Can't get Instrument Audio!"));
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType());
    choice.setInstrumentMode(instrument.getMode());
    choice.setInstrumentId(audio.getInstrumentId());
    fabricator.put(choice, false);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement, false);

    // Start at zero and keep laying down perc loops until we're out of here
    float beats = 0;
    while (beats < fabricator.getSegment().getTotal()) {

      // Pick attributes are expressed "rendered" as actual seconds
      long startAtSegmentMicros = fabricator.getSegmentMicrosAtPosition(tempo, beats);
      long lengthMicros = Math.min(
        fabricator.getTotalSegmentMicros() - startAtSegmentMicros,
        (long) (audio.getTotalBeats() * fabricator.getMicrosPerBeat(tempo))
      );

      // of pick
      var pick = new SegmentChoiceArrangementPick();
      pick.setId(UUID.randomUUID());
      pick.setSegmentId(fabricator.getSegment().getId());
      pick.setSegmentChoiceArrangementId(arrangement.getId());
      pick.setStartAtSegmentMicros(startAtSegmentMicros);
      pick.setLengthMicros(lengthMicros);
      pick.setAmplitude(1.0f);
      pick.setEvent("PERCLOOP");
      pick.setInstrumentAudioId(audio.getId());
      fabricator.put(pick, false);

      beats += audio.getTotalBeats();
    }
  }
}
