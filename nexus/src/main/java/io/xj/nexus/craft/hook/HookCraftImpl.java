// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.hook;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.lib.util.MarbleBag;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HookCraftImpl extends CraftImpl implements HookCraft {
  @Inject
  public HookCraftImpl(
    @Assisted("basis") Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    if (!fabricator.sourceMaterial().hasInstruments(InstrumentType.Hook, InstrumentMode.Loop)) return;

    // Instrument is from prior choice, else freshly chosen
    Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(InstrumentType.Hook, InstrumentMode.Loop);

    // Instruments may be chosen without programs https://www.pivotaltracker.com/story/show/181290857
    Optional<Instrument> instrument = priorChoice.isPresent() ?
      fabricator.sourceMaterial().getInstrument(priorChoice.get().getInstrumentId()) :
      chooseFreshInstrument(List.of(InstrumentType.Hook), List.of(InstrumentMode.Loop), List.of(), null, List.of());

    // instrument audio prior choice persists
    Optional<SegmentChoiceArrangementPick> priorPick = priorChoice.flatMap(pc ->
      fabricator.retrospective().getPreviousPicksForInstrument(pc.getInstrumentId()).stream().findAny());
    Optional<InstrumentAudio> priorAudio = priorPick.flatMap(pp ->
      fabricator.sourceMaterial().getInstrumentAudio(pp.getInstrumentAudioId()).stream().findAny());

    // Pick instrument audio
    Optional<InstrumentAudio> instrumentAudio =
      instrument.isPresent() && fabricator.getInstrumentConfig(instrument.get()).isAudioSelectionPersistent() && priorAudio.isPresent()
        ? priorAudio : (instrument.isPresent() ? selectNewInstrumentAudio(instrument.get()) : Optional.empty());

    // Loop instrument mode https://www.pivotaltracker.com/story/show/181815619
    // Should gracefully skip audio in unfulfilled by instrument https://www.pivotaltracker.com/story/show/176373977
    if (instrument.isPresent() && instrumentAudio.isPresent()) craftHook(instrument.get(), instrumentAudio.get());

    // Finally, update the segment with the crafted content
    fabricator.done();
  }

  /**
   Craft hook loop

   @param instrument to craft
   @param audio      to craft
   @throws NexusException on failure
   */
  private void craftHook(Instrument instrument, InstrumentAudio audio) throws NexusException {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType().toString());
    choice.setInstrumentMode(instrument.getMode().toString());
    choice.setInstrumentId(instrument.getId());
    fabricator.put(choice);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement);

    // Start at zero and keep laying down hook loops until we're out of here
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
      pick.setEvent("HOOK");
      pick.setInstrumentAudioId(audio.getId());
      fabricator.put(pick);

      pos += audio.getTotalBeats();
    }
  }

  /**
   Select a new random instrument audio

   @param instrument of which to score available audios, and make a selection
   @return matched new audio
   */
  private Optional<InstrumentAudio> selectNewInstrumentAudio(
    Instrument instrument
  ) {
    var bag = MarbleBag.empty();

    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosForInstrumentId(instrument.getId()))
      bag.add(1, audio.getId());

    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }
}
