// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/TransitionCraft.h"
#include "xjmusic/music/Bar.h"

using namespace XJ;

TransitionCraft::TransitionCraft(
    Fabricator *fabricator
) : Craft(fabricator) {
  this->smallNames = fabricator->getTemplateConfig().eventNamesSmall;
  this->mediumNames = fabricator->getTemplateConfig().eventNamesMedium;
  this->largeNames = fabricator->getTemplateConfig().eventNamesLarge;
}


void TransitionCraft::doWork() {
  const auto previousChoice =
      fabricator->getRetrospective()->getPreviousChoiceOfType(Instrument::Type::Transition);

  const auto instrument = previousChoice.has_value() ?
                    fabricator->getSourceMaterial()->getInstrument(previousChoice.value()->instrumentId) :
                    chooseFreshInstrument(Instrument::Type::Transition, {});

  if (!instrument.has_value()) {
    return;
  }

  craftTransition(fabricator->getTempo(), instrument.value());
}

bool TransitionCraft::isBigTransitionSegment() const {
  switch (fabricator->getType()) {
    case Segment::Type::Initial:
    case Segment::Type::NextMain:
    case Segment::Type::NextMacro:
      return true;
    default:
      return false;
  };
}

bool TransitionCraft::isMediumTransitionSegment() const {
  if (fabricator->getType() != Segment::Type::Continue) return false;
  const auto mainSequence = fabricator->getCurrentMainSequence();
  if (!mainSequence.has_value())
    throw FabricationException("Can't get current main sequence");
  const auto previousMainSequence = fabricator->getPreviousMainSequence();
  if (!previousMainSequence.has_value())
    throw FabricationException("Can't get previous main sequence");
  return mainSequence.value()->id != previousMainSequence.value()->id;
}

void TransitionCraft::craftTransition(double tempo, const Instrument* instrument) {
  auto choice = SegmentChoice();
  choice.id = EntityUtils::computeUniqueId();
  choice.segmentId = fabricator->getSegment()->id;
  choice.mute = computeMute(instrument->type);
  choice.instrumentType = instrument->type;
  choice.instrumentMode = instrument->mode;
  choice.instrumentId = instrument->id;
  if (!fabricator->put(choice, false).has_value()) return;
  auto arrangement = SegmentChoiceArrangement();
  arrangement.id = EntityUtils::computeUniqueId();
  arrangement.segmentId = fabricator->getSegment()->id;
  arrangement.segmentChoiceId = choice.id;
  fabricator->put(arrangement);

  auto small = selectAudiosForInstrument(instrument, smallNames);
  auto medium = selectAudiosForInstrument(instrument, mediumNames);
  auto big = selectAudiosForInstrument(instrument, largeNames);

  if (isBigTransitionSegment() && !big.empty())
    for (auto bigAudio: big)
      pickInstrumentAudio(&arrangement, bigAudio, 0, fabricator->getTotalSegmentMicros(), *largeNames.begin());

  else if (isMediumTransitionSegment() && !medium.empty())
    for (auto mediumAudio: medium)
      pickInstrumentAudio(&arrangement, mediumAudio, 0, fabricator->getTotalSegmentMicros(), *mediumNames.begin());

  else if (!small.empty())
    for (auto smallAudio: small)
      pickInstrumentAudio(&arrangement, smallAudio, 0, fabricator->getTotalSegmentMicros(), *smallNames.begin());

  auto deltaUnits = Bar::of(fabricator->getCurrentMainProgramConfig().barBeats).computeSubsectionBeats(
      fabricator->getSegment()->total);
  auto pos = deltaUnits;
  while (pos < fabricator->getSegment()->total) {
    if (!small.empty())
      for (auto smallAudio: small)
        pickInstrumentAudio(&arrangement, smallAudio, fabricator->getSegmentMicrosAtPosition(tempo, pos),
                            fabricator->getTotalSegmentMicros(), *smallNames.begin());
    pos += deltaUnits;
  }
}

std::set<const InstrumentAudio *> TransitionCraft::selectAudiosForInstrument(const Instrument *instrument, std::set<std::string> names) {
  std::set<const SegmentChoiceArrangementPick*> previous;
  for (auto candidate : fabricator->getRetrospective()->getPreviousPicksForInstrument(instrument->id)) {
    std::string seek = StringUtils::toMeme(candidate->event);
    if (names.find(seek) != names.end())
      previous.emplace(candidate);
  }
  if (fabricator->getInstrumentConfig(instrument).isAudioSelectionPersistent && !previous.empty()) {
    std::set<const InstrumentAudio *> result;
    for (const auto pick : previous) {
      auto audio = fabricator->getSourceMaterial()->getInstrumentAudio(pick->instrumentAudioId);
      if (audio.has_value())
        result.emplace(audio.value());
    }
    return result;
  }

  std::set<const InstrumentAudio *> result;
  for (auto audio : fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument->id)) {
    std::string seek = StringUtils::toMeme(audio->event);
    if (names.find(seek) != names.end())
      result.emplace(audio);
  }
  return selectAudioIntensityLayers(
      result,
      fabricator->getTemplateConfig().getIntensityLayers(Instrument::Type::Background)
  );
}
