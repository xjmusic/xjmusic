// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.detail;


import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.CraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 Detail craft for the current segment
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class DetailCraftImpl extends CraftImpl implements DetailCraft {
  private static final Collection<InstrumentType> DETAIL_INSTRUMENT_TYPES = Set.of(
      InstrumentType.Bass,
      InstrumentType.Pad,
      InstrumentType.Sticky,
      InstrumentType.Stripe,
      InstrumentType.Stab,
      InstrumentType.Hook,
      InstrumentType.Percussion
  );

  public DetailCraftImpl(Fabricator fabricator) {
    super(fabricator);
  }

  @Override
  public void doWork() throws NexusException {
    // Segments have intensity arcs; automate mixer layers in and out of each main program https://www.pivotaltracker.com/story/show/178240332
    ChoiceIndexProvider choiceIndexProvider = (SegmentChoice choice) -> StringUtils.stringOrDefault(choice.getInstrumentType(), choice.getId().toString());
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> Objects.equals(ProgramType.Detail, choice.getProgramType());
    precomputeDeltas(choiceFilter, choiceIndexProvider, fabricator.getTemplateConfig().getDetailLayerOrder().stream().map(InstrumentType::toString).collect(Collectors.toList()), List.of(), fabricator.getTemplateConfig().getIntensityAutoCrescendoDetailLayersIncoming());

    // For each type of voicing present in the main sequence, choose instrument, then program if necessary
    for (InstrumentType instrumentType : DETAIL_INSTRUMENT_TYPES) {

      // TODO based on number of target layers, choose multiple instruments with a spread of available intensities
      var targetLayers = fabricator.getTemplateConfig().getIntensityLayers(instrumentType);

      // TODO get multiple choices re: total # of layers -- deal with each prior choice
      // Instrument is from prior choice, else freshly chosen
      Optional<SegmentChoice> priorChoice = fabricator.getChoiceIfContinued(instrumentType);

      // Instruments may be chosen without programs https://www.pivotaltracker.com/story/show/181290857
      Optional<Instrument> instrument = priorChoice.isPresent() ? fabricator.sourceMaterial().getInstrument(priorChoice.get().getInstrumentId()) : chooseFreshInstrument(List.of(instrumentType), List.of(), null, List.of());

      // Should gracefully skip voicing type if unfulfilled by detail instrument https://www.pivotaltracker.com/story/show/176373977
      if (instrument.isEmpty()) {
        reportMissing(Instrument.class, String.format("%s-type Instrument", instrumentType));
        continue;
      }

      // Instruments have InstrumentMode https://www.pivotaltracker.com/story/show/181134085
      switch (instrument.get().getMode()) {

        // Event instrument mode takes over legacy behavior https://www.pivotaltracker.com/story/show/181736854
        case Event -> {
          // Event Use prior chosen program or find a new one
          Optional<Program> program = priorChoice.isPresent() ? fabricator.sourceMaterial().getProgram(priorChoice.get().getProgramId()) : chooseFreshProgram(ProgramType.Detail, instrumentType);

          // Event Should gracefully skip voicing type if unfulfilled by detail program https://www.pivotaltracker.com/story/show/176373977
          if (program.isEmpty()) {
            reportMissing(Program.class, String.format("%s-type Program", instrumentType));
            continue;
          }
          craftEventParts(fabricator.getTempo(), instrument.get(), program.get());
        }

        // Chord instrument mode https://www.pivotaltracker.com/story/show/181631275
        case Chord -> craftChordParts(fabricator.getTempo(), instrument.get());

        case Loop -> craftLoop(instrument.get());

        // As-yet Unsupported Modes
        default ->
            fabricator.addWarningMessage(String.format("Cannot craft unsupported mode %s for Instrument[%s]", instrument.get().getMode(), instrument.get().getId()));
      }
    }
  }

  /**
   Craft for a loop-type instrument

   @param instrument to craft
   @throws NexusException on failure
   */
  private void craftLoop(Instrument instrument) throws NexusException {

    Collection<UUID> audioIds =
        SegmentType.CONTINUE.equals(fabricator.getType()) ?
            fabricator.retrospective().getPreviousPicksForInstrument(instrument.getId()).stream()
                .map(SegmentChoiceArrangementPick::getInstrumentAudioId)
                .collect(Collectors.toSet())
            : new ArrayList<>();

    int targetLayers = fabricator.getTemplateConfig().getIntensityLayers(instrument.getType());


    fabricator.addInfoMessage(String.format("Targeting %d layers of %s-type loop", targetLayers, instrument.getType()));

    if (audioIds.size() > targetLayers)
      audioIds = ValueUtils.withIdsRemoved(audioIds, audioIds.size() - targetLayers);

    else if (audioIds.size() < targetLayers)
      for (int i = 0; i < targetLayers - audioIds.size(); i++) {
        Optional<InstrumentAudio> chosen = chooseFreshInstrumentAudio(List.of(instrument.getType()), List.of(InstrumentMode.Loop), audioIds, computePreferredEvents(audioIds.size()));
        if (chosen.isPresent()) {
          audioIds.add(chosen.get().getId());
        }
      }

    for (InstrumentAudio audio : audioIds.stream()
        .flatMap(audioId -> fabricator.sourceMaterial().getInstrumentAudio(audioId).stream())
        .toList())
      craftLoop(fabricator.getTempo(), audio);
  }

  /**
   Loop-mode instrument audios are chosen in order of priority
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
   Craft loop

   @param tempo of main program
   @param audio for which to craft segment
   */
  @SuppressWarnings("DuplicatedCode")
  void craftLoop(double tempo, InstrumentAudio audio) throws NexusException {
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

    // Start at zero and keep laying down loops until we're out of here
    float beats = 0;
    while (beats < fabricator.getSegment().getTotal()) {

      // Pick attributes are expressed "rendered" as actual seconds
      long startAtSegmentMicros = fabricator.getSegmentMicrosAtPosition(tempo, beats);
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
      pick.setEvent("LOOP");
      pick.setInstrumentAudioId(audio.getId());
      fabricator.put(pick, false);

      beats += audio.getLoopBeats();
    }
  }



/*

TODO craft parts for hook type instruments

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

  **
   Craft hook loop

   @param tempo of main program
   @param instrument to craft
   @param audio      to craft
   @throws NexusException on failure
   *
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

  **
   Select a new random instrument audio

   @param instrument of which to score available audios, and make a selection
   @return matched new audio
   *
  Optional<InstrumentAudio> selectNewInstrumentAudio(
      Instrument instrument
  ) {
    var bag = MarbleBag.empty();

    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosOfInstrument(instrument.getId()))
      bag.add(1, audio.getId());

    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }
*/


}
