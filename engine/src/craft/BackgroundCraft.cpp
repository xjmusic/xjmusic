// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/BackgroundCraft.h"


BackgroundCraft::BackgroundCraft(
    Fabricator *fabricator
) : Craft(fabricator) {}

void BackgroundCraft::doWork() {
  auto previousChoice = fabricator->getRetrospective()->getPreviousChoiceOfType(Instrument::Type::Background);

  auto instrument = previousChoice.has_value() ?
                    fabricator->getSourceMaterial()->getInstrument(previousChoice.value()->instrumentId) :
                    chooseFreshInstrument(Instrument::Type::Background, {});

  if (!instrument.has_value()) {
    return;
  }

  craftBackground(instrument.value());
}

void BackgroundCraft::craftBackground(const Instrument *instrument) {
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

  for (InstrumentAudio audio: selectGeneralAudioIntensityLayers(instrument)) {
    auto pick = new SegmentChoiceArrangementPick();
    pick.setId(EntityUtils::computeUniqueId());
    pick.setSegmentId(fabricator.getSegment().getId());
    pick.setSegmentChoiceArrangementId(arrangement.getId());
    pick.setStartAtSegmentMicros(0L);
    pick.setLengthMicros(fabricator.getTotalSegmentMicros());
    pick.setAmplitude(1.0f);
    pick.setEvent("BACKGROUND");
    pick.setInstrumentAudioId(audio.getId());
    fabricator.put(pick, false);
  }
}

