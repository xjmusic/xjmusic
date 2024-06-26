// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.transition;


import io.xj.model.enums.InstrumentType;
import io.xj.model.music.Bar;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.util.StringUtils;
import io.xj.engine.FabricationException;
import io.xj.engine.craft.CraftImpl;
import io.xj.engine.fabricator.Fabricator;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


/**
 Transition craft for the current segment
 <p>
 Transition-type Instrument https://github.com/xjmusic/xjmusic/issues/262
 */
public class TransitionCraftImpl extends CraftImpl implements TransitionCraft {
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
  public void doWork() throws FabricationException {
    Optional<SegmentChoice> previousChoice = fabricator.retrospective().getPreviousChoiceOfType(InstrumentType.Transition);

    var instrument = previousChoice.isPresent() ?
      fabricator.sourceMaterial().getInstrument(previousChoice.get().getInstrumentId()) :
      chooseFreshInstrument(InstrumentType.Transition, List.of());

    if (instrument.isEmpty()) {
      return;
    }

    craftTransition(fabricator.getTempo(), instrument.get());
  }

  /**
   Is this a big-transition segment? (next main or next macro)

   @return true if it is a big transition segment
   */
  boolean isBigTransitionSegment() throws FabricationException {
    return switch (fabricator.getType()) {
      case PENDING, CONTINUE -> false;
      case INITIAL, NEXT_MAIN, NEXT_MACRO -> true;
    };
  }

  /**
   Is this a medium-transition segment? (not the same sequence as the previous segment)
   <p>
   Transition craft uses Small (instead of Medium) when a sequence repeats for more than 1 segment https://github.com/xjmusic/xjmusic/issues/264

   @return true if it is a medium transition segment
   */
  boolean isMediumTransitionSegment() throws FabricationException {
    return switch (fabricator.getType()) {
      case PENDING, INITIAL, NEXT_MAIN, NEXT_MACRO -> false;
      case CONTINUE -> !fabricator.getCurrentMainSequence()
        .orElseThrow(() -> new FabricationException("Can't get current main sequence"))
        .getId()
        .equals(fabricator.getPreviousMainSequence().orElseThrow(() ->
          new FabricationException("Can't get previous main sequence")).getId());
    };
  }

  /**
   Craft percussion loop

   @param tempo      of main program
   @param instrument of percussion loop instrument to craft
   */
  @SuppressWarnings("DuplicatedCode")
  void craftTransition(double tempo, Instrument instrument) throws FabricationException {
    var choice = new SegmentChoice();
    choice.setId(UUID.randomUUID());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType());
    choice.setInstrumentMode(instrument.getMode());
    choice.setInstrumentId(instrument.getId());
    if (fabricator.put(choice, false).isEmpty()) return;
    var arrangement = new SegmentChoiceArrangement();
    arrangement.setId(UUID.randomUUID());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement);

    var small = selectAudiosForInstrument(instrument, smallNames);
    var medium = selectAudiosForInstrument(instrument, mediumNames);
    var big = selectAudiosForInstrument(instrument, largeNames);

    if (isBigTransitionSegment() && !big.isEmpty())
      for (var bigAudio : big)
        pickInstrumentAudio(arrangement, bigAudio, 0, fabricator.getTotalSegmentMicros(), largeNames.get(0));

    else if (isMediumTransitionSegment() && !medium.isEmpty())
      for (var mediumAudio : medium)
        pickInstrumentAudio(arrangement, mediumAudio, 0, fabricator.getTotalSegmentMicros(), mediumNames.get(0));

    else if (!small.isEmpty())
      for (var smallAudio : small)
        pickInstrumentAudio(arrangement, smallAudio, 0, fabricator.getTotalSegmentMicros(), smallNames.get(0));

    var deltaUnits = Bar.of(fabricator.getCurrentMainProgramConfig().getBarBeats()).computeSubsectionBeats(fabricator.getSegment().getTotal());
    var pos = deltaUnits;
    while (pos < fabricator.getSegment().getTotal()) {
      if (!small.isEmpty())
        for (var smallAudio : small)
          pickInstrumentAudio(arrangement, smallAudio, fabricator.getSegmentMicrosAtPosition(tempo, pos), fabricator.getTotalSegmentMicros(), smallNames.get(0));
      pos += deltaUnits;
    }
  }

  /**
   Select audios for instrument having the given event names

   @return instrument audios
   */
  private Collection<InstrumentAudio> selectAudiosForInstrument(Instrument instrument, List<String> names) {
    var previous = fabricator.retrospective().getPreviousPicksForInstrument(instrument.getId()).stream()
      .filter(pick -> names.contains(StringUtils.toMeme(pick.getEvent())))
      .collect(Collectors.toSet());
    if (fabricator.getInstrumentConfig(instrument).isAudioSelectionPersistent() && !previous.isEmpty()) {
      return previous.stream()
        .map(SegmentChoiceArrangementPick::getInstrumentAudioId)
        .collect(Collectors.toSet()) // unique audio ids
        .stream()
        .map(audioId -> fabricator.sourceMaterial().getInstrumentAudio(audioId))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toSet());
    }

    return selectAudioIntensityLayers(
      fabricator.sourceMaterial().getAudiosOfInstrument(instrument.getId())
        .stream().filter(instrumentAudio -> names.contains(StringUtils.toMeme(instrumentAudio.getEvent()))).collect(Collectors.toSet()),
      fabricator.getTemplateConfig().getIntensityLayers(InstrumentType.Background)
    );
  }
}
