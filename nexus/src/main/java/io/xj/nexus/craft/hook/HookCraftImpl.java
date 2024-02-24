// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.hook;


import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.util.MarbleBag;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HookCraftImpl extends CraftImpl implements HookCraft {
  public HookCraftImpl(
    Fabricator fabricator
  ) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    if (!fabricator.sourceMaterial().hasInstrumentsOfTypeAndMode(InstrumentType.Hook, InstrumentMode.Loop)) return;

    // Instrument is from prior choice, else freshly chosen
    Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(InstrumentType.Hook, InstrumentMode.Loop);

    // Instruments may be chosen without programs https://www.pivotaltracker.com/story/show/181290857
    Optional<Instrument> instrument = priorChoice.isPresent() ?
      fabricator.sourceMaterial().getInstrument(priorChoice.get().getInstrumentId()) :
      chooseFreshInstrument(List.of(InstrumentType.Hook), List.of(), null, List.of());

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
    if (instrument.isPresent() && instrumentAudio.isPresent()) craftHook(fabricator.getTempo(), instrument.get(), instrumentAudio.get());
  }

  /**
   Craft hook loop

   @param tempo of main program
   @param instrument to craft
   @param audio      to craft
   @throws NexusException on failure
   */
  void craftHook(double tempo, Instrument instrument, InstrumentAudio audio) throws NexusException {
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

    // Start at zero and keep laying down hook loops until we're out of here
    float pos = 0;
    while (pos < fabricator.getSegment().getTotal()) {
      long startAtSegmentMicros = fabricator.getSegmentMicrosAtPosition(tempo, pos);
      long lengthMicros = Math.min(
        fabricator.getTotalSegmentMicros() - startAtSegmentMicros,
        (long) (audio.getLoopBeats() * fabricator.getMicrosPerBeat(tempo))
      );

      // of pick
      var pick = new SegmentChoiceArrangementPick();
      pick.setId(UUID.randomUUID());
      pick.setSegmentId(fabricator.getSegment().getId());
      pick.setSegmentChoiceArrangementId(arrangement.getId());
      pick.setStartAtSegmentMicros(startAtSegmentMicros);
      pick.setLengthMicros(lengthMicros);
      pick.setAmplitude(1.0f);
      pick.setEvent("HOOK");
      pick.setInstrumentAudioId(audio.getId());
      fabricator.put(pick, false);

      pos += audio.getLoopBeats();
    }
  }

  /**
   Select a new random instrument audio

   @param instrument of which to score available audios, and make a selection
   @return matched new audio
   */
  Optional<InstrumentAudio> selectNewInstrumentAudio(
    Instrument instrument
  ) {
    var bag = MarbleBag.empty();

    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosOfInstrument(instrument.getId()))
      bag.add(1, audio.getId());

    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }
}
