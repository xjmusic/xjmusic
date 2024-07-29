// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/work/DubWork.h"
#include "xjmusic/audio/ActiveAudio.h"
#include "xjmusic/audio/AudioMathUtils.h"
#include "xjmusic/util/ValueUtils.h"

using namespace XJ;

DubWork::DubWork(
    CraftWork *craftWork,
    const int dubAheadSeconds) {
  this->craftWork = craftWork;
  dubAheadMicros = dubAheadSeconds * ValueUtils::MICROS_PER_SECOND;
}

void DubWork::start() {
  templateConfig = craftWork->getTemplateConfig();
  running = true;
}

void DubWork::finish() {
  if (!running)
    return;
  running = false;
  craftWork->finish();
  std::cout << "Finished" << std::endl;
}

std::set<ActiveAudio> DubWork::runCycle(unsigned long long atChainMicros) {
  if (!running)
    return {};

  // Only ready to dub after at least one craft cycle is completed since the last time we weren't ready to dub
  // live performance modulation https://github.com/xjmusic/xjmusic/issues/197
  if (!craftWork->isReady()) {
    // Waiting for Craft readiness...
    return {};
  }

  if (craftWork->isFinished()) {
    std::cout << "Craft is finished. Dub will finish." << std::endl;
    finish();
    return {};
  }

  // Action based on state and mode
  try {
    return computeActiveAudios(atChainMicros);
  } catch (std::exception &e) {
    didFailWhile("running dub work", e);
    return {};
  }
}

bool DubWork::isFinished() const {
  return !running;
}

std::optional<const Segment *> DubWork::getSegmentAtChainMicros(long atChainMicros) const {
  return craftWork->getSegmentAtChainMicros(atChainMicros);
}

std::optional<const Segment *> DubWork::getSegmentAtOffset(int offset) const {
  return craftWork->getSegmentAtOffset(offset);
}

std::optional<const Program *> DubWork::getMainProgram(const Segment *segment) const {
  return craftWork->getMainProgram(segment);
}

std::optional<const Program *> DubWork::getMacroProgram(const Segment &segment) const {
  return craftWork->getMacroProgram(segment);
}

void DubWork::setIntensityOverride(const std::optional<float> intensity) {
  intensityOverride = intensity;
}

// FUTURE don't recalculate all the active audios every time, cache and recompute only the ones that changed
std::set<ActiveAudio> DubWork::computeActiveAudios(const unsigned long long atChainMicros) {
  const auto toChainMicros = atChainMicros + dubAheadMicros;
  const auto segments = craftWork->getSegmentsIfReady(atChainMicros, toChainMicros);
  if (segments.empty()) {
    // Waiting for segments
    return {};
  }

  if (intensityOverride.has_value()) {
    nextIntensity = intensityOverride.value();
  } else {
    nextIntensity = segments.at(0)->intensity;
  }

  try {
    std::set<ActiveAudio> activeAudios;
    for (const auto segment: segments)
      for (const auto choice: craftWork->getChoices(segment))
        if (!choice->mute)
          for (const auto arrangement: craftWork->getArrangements(choice))
            for (auto pick: craftWork->getPicks(arrangement)) {

              const InstrumentAudio *audio = craftWork->getInstrumentAudio(pick);
              if (audio->waveformKey.empty()) {
                continue;
              }
              // audio transient microseconds (to start audio before picked time)
              const long transientMicros =
                  0 < audio->transientSeconds ? static_cast<unsigned long long>(audio->transientSeconds * ValueUtils::MICROS_PER_SECOND_FLOAT)
                                              : 0;
              // pick length microseconds, or empty if infinite
              std::optional<unsigned long long> lengthMicros =
                  0 < pick->lengthMicros ? std::optional(pick->lengthMicros)
                                         : std::nullopt;// pick length microseconds, or empty if infinite
              const unsigned long long startAtChainMicros =
                  // segment begin at chain microseconds
                  segment->beginAtChainMicros
                  // plus pick start microseconds
                  + pick->startAtSegmentMicros
                  // minus transient microseconds
                  - transientMicros;
              unsigned long long stopAtChainMicros = lengthMicros.has_value()
                                                         // from start of this active audio
                                                         ? startAtChainMicros +
                                                               // revert transient microseconds from previous computation
                                                               transientMicros
                                                               // add length micros
                                                               + lengthMicros.value()
                                                         : startAtChainMicros + static_cast<unsigned long long>(audio->lengthSeconds * ValueUtils::MICROS_PER_SECOND_FLOAT);
              if (startAtChainMicros <= atChainMicros + dubAheadMicros && stopAtChainMicros >= atChainMicros) {
                const auto instrument = craftWork->getInstrument(audio);
                activeAudios.emplace(
                    pick,
                    instrument,
                    audio,
                    startAtChainMicros,
                    stopAtChainMicros,
                    AudioMathUtils::computeIntensityAmplitude(
                        audio,
                        templateConfig.getIntensityLayers(instrument->type),
                        templateConfig.getIntensityThreshold(instrument->type),
                        false, prevIntensity.has_value() ? prevIntensity.value() : nextIntensity.value()),
                    AudioMathUtils::computeIntensityAmplitude(
                        audio,
                        templateConfig.getIntensityLayers(instrument->type),
                        templateConfig.getIntensityThreshold(instrument->type),
                        false, nextIntensity.value()));
              }
            }
    prevIntensity = {nextIntensity.value()};
    // Dubbed toChainMicros
    return activeAudios;
  } catch (std::exception &e) {
    didFailWhile("dubbing frame", e);
    return {};
  }
}

void DubWork::didFailWhile(const std::string &msgWhile, const std::exception &e) {
  std::cerr << "Failed while " << msgWhile << " because " << e.what() << std::endl;
  running = false;
  finish();
}
