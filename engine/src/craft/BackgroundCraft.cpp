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
  auto choice = SegmentChoice();
  choice.id = EntityUtils::computeUniqueId();
  choice.segmentId = fabricator->getSegment()->id;
  choice.mute = computeMute(instrument->type);
  choice.instrumentType = instrument->type;
  choice.instrumentMode = instrument->mode;
  choice.instrumentId = instrument->id;
  fabricator->put(choice, false);
  auto arrangement = SegmentChoiceArrangement();
  arrangement.id = EntityUtils::computeUniqueId();
  arrangement.segmentId = fabricator->getSegment()->id;
  arrangement.segmentChoiceId = choice.id;
  fabricator->put(arrangement);

  for (const InstrumentAudio& audio: selectGeneralAudioIntensityLayers(instrument)) {
    auto pick = SegmentChoiceArrangementPick();
    pick.id = EntityUtils::computeUniqueId();
    pick.segmentId = fabricator->getSegment()->id;
    pick.segmentChoiceArrangementId = arrangement.id;
    pick.startAtSegmentMicros = 0L;
    pick.lengthMicros = fabricator->getTotalSegmentMicros();
    pick.amplitude = 1.0f;
    pick.event = "BACKGROUND";
    pick.instrumentAudioId = audio.id;
    fabricator->put(pick);
  }
}

