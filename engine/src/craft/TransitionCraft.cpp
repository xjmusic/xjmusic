// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/TransitionCraft.h"

using namespace XJ;

TransitionCraft::TransitionCraft(
    Fabricator *fabricator
  ) : FabricationWrapper(fabricator) {
    this->smallNames = fabricator->getTemplateConfig().eventNamesSmall;
    this->mediumNames = fabricator->getTemplateConfig().eventNamesMedium;
    this->largeNames = fabricator->getTemplateConfig().eventNamesLarge;
  }


  void doWork()  {
    std::optional<SegmentChoice> previousChoice = fabricator-getRetrospective().getPreviousChoiceOfType(Instrument::Type::Transition);

    auto instrument = previousChoice.isPresent() ?
      fabricator->getSourceMaterial()->getInstrument(previousChoice.get().getInstrumentId()) :
      chooseFreshInstrument(Instrument::Type::Transition, List.of());

    if (instrument.isEmpty()) {
      return;
    }

    craftTransition(fabricator->getTempo(), instrument.get());
  }

  /**
   Is this a big-transition segment? (next main or next macro)

   @return true if it is a big transition segment
   */
  boolean isBigTransitionSegment()  {
    return switch (fabricator->type) {
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
  boolean isMediumTransitionSegment()  {
    return switch (fabricator->type) {
      case PENDING, INITIAL, NEXT_MAIN, NEXT_MACRO -> false;
      case CONTINUE -> !fabricator->getCurrentMainSequence()
        .orElseThrow(() -> new FabricationException("Can't get current main sequence"))
        .id
        .equals(fabricator->getPreviousMainSequence().orElseThrow(() ->
          new FabricationException("Can't get previous main sequence")).id);
    };
  }

  /**
   Craft percussion loop

   @param tempo      of main program
   @param instrument of percussion loop instrument to craft
   */
  @SuppressWarnings("DuplicatedCode")
  void craftTransition(double tempo, Instrument instrument)  {
    auto choice = SegmentChoice();
    choice.setId(EntityUtils::computeUniqueId());
    choice.setSegmentId(fabricator->getSegment().id);
    choice.setMute(computeMute(instrument.type));
    choice.setInstrumentType(instrument.type);
    choice.setInstrumentMode(instrument.mode);
    choice.setInstrumentId(instrument.id);
    fabricator->put(choice, false);
    auto arrangement = SegmentChoiceArrangement();
    arrangement.setId(EntityUtils::computeUniqueId());
    arrangement.setSegmentId(fabricator->getSegment().id);
    arrangement.segmentChoiceId(choice.id);
    fabricator->put(arrangement, false);

    auto small = selectAudiosForInstrument(instrument, smallNames);
    auto medium = selectAudiosForInstrument(instrument, mediumNames);
    auto big = selectAudiosForInstrument(instrument, largeNames);

    if (isBigTransitionSegment() && !big.isEmpty())
      for (auto bigAudio : big)
        pickInstrumentAudio(arrangement, bigAudio, 0, fabricator->getTotalSegmentMicros(), largeNames.get(0));

    else if (isMediumTransitionSegment() && !medium.isEmpty())
      for (auto mediumAudio : medium)
        pickInstrumentAudio(arrangement, mediumAudio, 0, fabricator->getTotalSegmentMicros(), mediumNames.get(0));

    else if (!small.isEmpty())
      for (auto smallAudio : small)
        pickInstrumentAudio(arrangement, smallAudio, 0, fabricator->getTotalSegmentMicros(), smallNames.get(0));

    auto deltaUnits = Bar.of(fabricator->getCurrentMainProgramConfig().getBarBeats()).computeSubsectionBeats(fabricator->getSegment().getTotal());
    auto pos = deltaUnits;
    while (pos < fabricator->getSegment().getTotal()) {
      if (!small.isEmpty())
        for (auto smallAudio : small)
          pickInstrumentAudio(arrangement, smallAudio, fabricator->getSegmentMicrosAtPosition(tempo, pos), fabricator->getTotalSegmentMicros(), smallNames.get(0));
      pos += deltaUnits;
    }
  }

  /**
   Select audios for instrument having the given event names

   @return instrument audios
   */
  private Collection<InstrumentAudio> selectAudiosForInstrument(Instrument instrument, List<std::string> names) {
    auto previous = fabricator->getRetrospective().getPreviousPicksForInstrument(instrument.id).stream()
      .filter(pick -> names.contains(StringUtils.toMeme(pick.getEvent())))
      .collect(Collectors.toSet());
    if (fabricator->getInstrumentConfig(instrument).isAudioSelectionPersistent() && !previous.isEmpty()) {
      return previous.stream()
        .map(SegmentChoiceArrangementPick::getInstrumentAudioId)
        .collect(Collectors.toSet()) // unique audio ids
        .stream()
        .map(audioId -> fabricator->getSourceMaterial()->getInstrumentAudio(audioId))
        .filter(std::optional::isPresent)
        .map(std::optional::get)
        .collect(Collectors.toSet());
    }

    return selectAudioIntensityLayers(
      fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument.id)
        .stream().filter(instrumentAudio -> names.contains(StringUtils.toMeme(instrumentAudio.getEvent()))).collect(Collectors.toSet()),
      fabricator->getTemplateConfig().getIntensityLayers(Instrument::Type::Background)
    );
  }
}
