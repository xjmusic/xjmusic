// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/TransitionCraft.h"

using namespace XJ;

List<std::string> smallNames;
  List<std::string> mediumNames;
  List<std::string> largeNames;

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
    Optional<SegmentChoice> previousChoice = fabricator.retrospective().getPreviousChoiceOfType(Instrument::Type::Transition);

    auto instrument = previousChoice.isPresent() ?
      fabricator.sourceMaterial().getInstrument(previousChoice.get().getInstrumentId()) :
      chooseFreshInstrument(Instrument::Type::Transition, List.of());

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
    auto choice = new SegmentChoice();
    choice.setId(EntityUtils::computeUniqueId());
    choice.setSegmentId(fabricator.getSegment().getId());
    choice.setMute(computeMute(instrument.getType()));
    choice.setInstrumentType(instrument.getType());
    choice.setInstrumentMode(instrument.getMode());
    choice.setInstrumentId(instrument.getId());
    fabricator.put(choice, false);
    auto arrangement = new SegmentChoiceArrangement();
    arrangement.setId(EntityUtils::computeUniqueId());
    arrangement.setSegmentId(fabricator.getSegment().getId());
    arrangement.segmentChoiceId(choice.getId());
    fabricator.put(arrangement, false);

    auto small = selectAudiosForInstrument(instrument, smallNames);
    auto medium = selectAudiosForInstrument(instrument, mediumNames);
    auto big = selectAudiosForInstrument(instrument, largeNames);

    if (isBigTransitionSegment() && !big.isEmpty())
      for (auto bigAudio : big)
        pickInstrumentAudio(arrangement, bigAudio, 0, fabricator.getTotalSegmentMicros(), largeNames.get(0));

    else if (isMediumTransitionSegment() && !medium.isEmpty())
      for (auto mediumAudio : medium)
        pickInstrumentAudio(arrangement, mediumAudio, 0, fabricator.getTotalSegmentMicros(), mediumNames.get(0));

    else if (!small.isEmpty())
      for (auto smallAudio : small)
        pickInstrumentAudio(arrangement, smallAudio, 0, fabricator.getTotalSegmentMicros(), smallNames.get(0));

    auto deltaUnits = Bar.of(fabricator.getCurrentMainProgramConfig().getBarBeats()).computeSubsectionBeats(fabricator.getSegment().getTotal());
    auto pos = deltaUnits;
    while (pos < fabricator.getSegment().getTotal()) {
      if (!small.isEmpty())
        for (auto smallAudio : small)
          pickInstrumentAudio(arrangement, smallAudio, fabricator.getSegmentMicrosAtPosition(tempo, pos), fabricator.getTotalSegmentMicros(), smallNames.get(0));
      pos += deltaUnits;
    }
  }

  /**
   Select audios for instrument having the given event names

   @return instrument audios
   */
  private Collection<InstrumentAudio> selectAudiosForInstrument(Instrument instrument, List<std::string> names) {
    auto previous = fabricator.retrospective().getPreviousPicksForInstrument(instrument.getId()).stream()
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
      fabricator.getTemplateConfig().getIntensityLayers(Instrument::Type::Background)
    );
  }
}
