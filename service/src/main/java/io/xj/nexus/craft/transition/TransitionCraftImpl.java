// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.transition;


import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.music.Bar;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.MarbleBag;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.craft.detail.DetailCraftImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 * Transition craft for the current segment
 * <p>
 * Transition-type Instrument https://www.pivotaltracker.com/story/show/180059746
 */
public class TransitionCraftImpl extends DetailCraftImpl implements TransitionCraft {
  final List<String> smallNames;
  final List<String> mediumNames;
  final List<String> largeNames;

  public TransitionCraftImpl(
    Fabricator fabricator
  ) {
    super(fabricator);

    smallNames = fabricator.getTemplateConfig().getEventNamesSmall();
    mediumNames = fabricator.getTemplateConfig().getEventNamesMedium();
    largeNames = fabricator.getTemplateConfig().getEventNamesLarge();
  }

  @Override
  public void doWork() throws NexusException {
    var previousChoices = fabricator.retrospective().getPreviousChoicesOfMode(InstrumentMode.Transition);

    Collection<UUID> instrumentIds = previousChoices.stream()
      .map(SegmentChoice::getInstrumentId)
      .collect(Collectors.toList());

    double targetDensity = isBigTransitionSegment() ? fabricator.getTemplateConfig().getDensityCeiling() : fabricator.getSegment().getDensity();

    int targetLayers = (int) Math.floor(
      fabricator.getTemplateConfig().getTransitionLayerMin() +
        targetDensity *
          (fabricator.getTemplateConfig().getTransitionLayerMax() -
            fabricator.getTemplateConfig().getTransitionLayerMin()));

    fabricator.addInfoMessage(String.format("Targeting %d layers of transition", targetLayers));

    if (instrumentIds.size() > targetLayers)
      instrumentIds = ValueUtils.withIdsRemoved(instrumentIds, instrumentIds.size() - targetLayers);

    for (UUID id : instrumentIds) craftTransition(id);

    Optional<Instrument> chosen;
    if (instrumentIds.size() < targetLayers)
      for (int i = 0; i < targetLayers - instrumentIds.size(); i++) {
        chosen = chooseFreshInstrument(List.of(), List.of(InstrumentMode.Transition), instrumentIds, null, List.of());
        if (chosen.isPresent()) {
          instrumentIds.add(chosen.get().getId());
          craftTransition(chosen.get().getId());
        }
      }

    // Finally, update the segment with the crafted content
    fabricator.done();
  }

  /**
   * Is this a big-transition segment? (next main or next macro)
   *
   * @return true if it is a big transition segment
   */
  boolean isBigTransitionSegment() throws NexusException {
    return switch (fabricator.getType()) {
      case PENDING, CONTINUE -> false;
      case INITIAL, NEXTMAIN, NEXTMACRO -> true;
    };
  }

  /**
   * Is this a medium-transition segment? (not the same sequence as the previous segment)
   * <p>
   * Transition craft uses Small (instead of Medium) when a sequence repeats for more than 1 segment https://www.pivotaltracker.com/story/show/180921714
   *
   * @return true if it is a medium transition segment
   */
  boolean isMediumTransitionSegment() throws NexusException {
    return switch (fabricator.getType()) {
      case PENDING, INITIAL, NEXTMAIN, NEXTMACRO -> false;
      case CONTINUE -> !fabricator.getCurrentMainSequence().orElseThrow().getId()
        .equals(fabricator.getPreviousMainSequence().orElseThrow().getId());
    };
  }

  /**
   * Craft percussion loop
   *
   * @param instrumentId of percussion loop instrument to craft
   */
  @SuppressWarnings("DuplicatedCode")
  void craftTransition(UUID instrumentId) throws NexusException {
    var choice = new SegmentChoice();
    var instrument = fabricator.sourceMaterial().getInstrument(instrumentId)
      .orElseThrow(() -> new NexusException("Can't get Instrument Audio!"));
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType());
    choice.setInstrumentMode(instrument.getMode());
    choice.setInstrumentId(instrumentId);
    fabricator.put(choice);
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement);

    var small = pickAudioForInstrument(instrument, smallNames);
    var medium = pickAudioForInstrument(instrument, mediumNames);
    var big = pickAudioForInstrument(instrument, largeNames);

    if (small.isPresent())
      pickTransition(arrangement, small.get(), 0, fabricator.getTotalSegmentMicros(), smallNames.get(0));
    else if (isMediumTransitionSegment() && medium.isPresent())
      pickTransition(arrangement, medium.get(), 0, fabricator.getTotalSegmentMicros(), mediumNames.get(0));
    else if (isBigTransitionSegment() && big.isPresent())
      pickTransition(arrangement, big.get(), 0, fabricator.getTotalSegmentMicros(), largeNames.get(0));

    var deltaUnits = Bar.of(fabricator.getCurrentMainProgramConfig().getBarBeats()).computeSubsectionBeats(fabricator.getSegment().getTotal());
    var pos = deltaUnits;
    while (pos < fabricator.getSegment().getTotal()) {
      if (small.isPresent())
        pickTransition(arrangement, small.get(), fabricator.getSegmentMicrosAtPosition(pos), fabricator.getTotalSegmentMicros(), smallNames.get(0));
      pos += deltaUnits;
    }
  }

  /**
   * Pci the transition
   *
   * @param arrangement          to pick
   * @param audio                to pick
   * @param startAtSegmentMicros to pick
   * @param lengthMicros         to pick
   * @param name                 to pick
   * @throws NexusException on failure
   */
  @SuppressWarnings("DuplicatedCode")
  void pickTransition(SegmentChoiceArrangement arrangement, InstrumentAudio audio, long startAtSegmentMicros, long lengthMicros, String name) throws NexusException {
    var pick = new SegmentChoiceArrangementPick();
    pick.setId(UUID.randomUUID());
    pick.setSegmentId(fabricator.getSegment().getId());
    pick.setSegmentChoiceArrangementId(arrangement.getId());
    pick.setStartAtSegmentMicros(startAtSegmentMicros);
    pick.setLengthMicros(lengthMicros);
    pick.setAmplitude(1.0);
    pick.setEvent(name);
    pick.setInstrumentAudioId(audio.getId());
    fabricator.put(pick);
  }

  /**
   * Choose drum instrument
   * [#325] Possible to choose multiple instruments for different voices in the same program
   *
   * @return drum-type Instrument
   */
  Optional<InstrumentAudio> pickAudioForInstrument(Instrument instrument, List<String> names) {
    var previous =
      fabricator.retrospective().getPreviousPicksForInstrument(instrument.getId()).stream()
        .filter(pick -> names.contains(StringUtils.toMeme(pick.getEvent())))
        .findAny();

    if (fabricator.getInstrumentConfig(instrument).isAudioSelectionPersistent() && previous.isPresent())
      return fabricator.sourceMaterial().getInstrumentAudio(previous.get().getInstrumentAudioId());

    var bag = MarbleBag.empty();

    for (InstrumentAudio audio : fabricator.sourceMaterial().getAudiosForInstrumentId(instrument.getId())
      .stream().filter(instrumentAudio -> names.contains(StringUtils.toMeme(instrumentAudio.getEvent()))).toList())
      bag.add(1, audio.getId());

    if (bag.isEmpty()) return Optional.empty();
    return fabricator.sourceMaterial().getInstrumentAudio(bag.pick());
  }

}
