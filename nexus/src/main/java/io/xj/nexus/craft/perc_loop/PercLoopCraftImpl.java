// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.perc_loop;

import com.google.api.client.util.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentType;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.util.Text;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.beat.BeatCraftImpl;
import io.xj.nexus.fabricator.Fabricator;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Percussion-type Loop-mode craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 <p>
 https://www.pivotaltracker.com/story/show/176625174 PercLoopCraftImpl extends DetailCraftImpl to leverage all detail craft enhancements
 */
public class PercLoopCraftImpl extends BeatCraftImpl implements PercLoopCraft {
  @Inject
  public PercLoopCraftImpl(
    @Assisted("basis") Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    Collection<UUID> audioIds =
      SegmentType.CONTINUE.equals(fabricator.getType()) ?
        fabricator.retrospective().getPreviousChoicesOfTypeMode(InstrumentType.Percussion, InstrumentMode.Loop).stream()
          .flatMap(choice -> fabricator.retrospective().getPreviousPicksForInstrument(choice.getInstrumentId()).stream())
          .map(SegmentChoiceArrangementPick::getInstrumentAudioId)
          .collect(Collectors.toSet())
        : Lists.newArrayList();

    int targetLayers = (int) Math.floor(
      fabricator.getTemplateConfig().getPercLoopLayerMin() +
        fabricator.getSegment().getDensity() *
          (fabricator.getTemplateConfig().getPercLoopLayerMax() -
            fabricator.getTemplateConfig().getPercLoopLayerMin()));

    fabricator.addInfoMessage(String.format("Targeting %d layers of percussion loop", targetLayers));

    if (audioIds.size() > targetLayers)
      audioIds = Values.withIdsRemoved(audioIds, audioIds.size() - targetLayers);

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
      craftPercLoop(audio);

    // Finally, update the segment with the crafted content
    fabricator.done();
  }

  /**
   Percussion-type Loop-mode instrument audios are chosen in order of priority
   https://www.pivotaltracker.com/story/show/181262545

   @param after # of choices
   @return required event name
   */
  @Nullable
  private List<String> computePreferredEvents(int after) {
    return switch (after) {
      case 0 -> fabricator.getTemplateConfig().getEventNamesLarge().stream()
        .map(Text::toEvent)
        .toList();

      case 1 -> fabricator.getTemplateConfig().getEventNamesMedium().stream()
        .map(Text::toEvent)
        .toList();

      default -> fabricator.getTemplateConfig().getEventNamesSmall().stream()
        .map(Text::toEvent)
        .toList();
    };
  }

  /**
   Craft percussion loop

   @param audio for which to craft segment
   */
  @SuppressWarnings("DuplicatedCode")
  private void craftPercLoop(InstrumentAudio audio) throws NexusException {
    var choice = new SegmentChoice();
    var instrument = fabricator.sourceMaterial().getInstrument(audio.getInstrumentId())
      .orElseThrow(() -> new NexusException("Can't get Instrument Audio!"));
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType().toString());
    choice.setInstrumentMode(instrument.getMode().toString());
    choice.setInstrumentId(audio.getInstrumentId());
    fabricator.put(choice);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement);

    // Start at zero and keep laying down perc loops until we're out of here
    double pos = 0;
    while (pos < fabricator.getSegment().getTotal()) {

      // Pick attributes are expressed "rendered" as actual seconds
      double startSeconds = fabricator.getSecondsAtPosition(pos);
      double lengthSeconds = fabricator.getSecondsAtPosition(pos + audio.getTotalBeats()) - startSeconds;

      // of pick
      var pick = new SegmentChoiceArrangementPick();
      pick.setId(UUID.randomUUID());
      pick.setSegmentId(fabricator.getSegment().getId());
      pick.setSegmentChoiceArrangementId(arrangement.getId());
      pick.setStart(startSeconds);
      pick.setLength(lengthSeconds);
      pick.setAmplitude(1.0);
      pick.setEvent("PERCLOOP");
      pick.setInstrumentAudioId(audio.getId());
      fabricator.put(pick);

      pos += audio.getTotalBeats();
    }
  }
}
