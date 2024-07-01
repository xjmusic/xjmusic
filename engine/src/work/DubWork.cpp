// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO implement this

#include "xjmusic/work/DubWork.h"
#include "xjmusic/audio/ActiveAudio.h"
#include "xjmusic/util/ValueUtils.h"

#include <xjmusic/audio/AudioMathUtils.h>

using namespace XJ;

  DubWork::DubWork(
      CraftWork* craftWork,
    const int dubAheadSeconds
  ) : Work() {
    this->craftWork = craftWork;
    dubAheadMicros = dubAheadSeconds * ValueUtils::MICROS_PER_SECOND;
    templateConfig = craftWork->getTemplateConfig();
    atChainMicros = 0;
    running = true;
  }

  void DubWork::finish() {
    if (!running) return;
    running = false;
    craftWork->finish();
    spdlog::info("Finished");
  }

  void DubWork::runCycle(long shippedToChainMicros) {
    if (!running) return;

    // Only ready to dub after at least one craft cycle is completed since the last time we weren't ready to dub
    // live performance modulation https://github.com/xjmusic/xjmusic/issues/197
    if (!craftWork->isReady()) {
      spdlog::debug("Waiting for Craft readiness...");
      return;
    }

    if (craftWork->isFinished()) {
      spdlog::info("Craft is finished. Dub will finish.");
      finish();
      return;
    }

    if (atChainMicros >= shippedToChainMicros + dubAheadMicros) {
      spdlog::debug("Waiting to catch up with dub-ahead");
      return;
    }

    // Action based on state and mode
    try {
      doDubFrame();

    } catch (
        std::exception e) {
      didFailWhile("running dub work", e);
    }
  }

  bool DubWork::isFinished() {
    return !running;
  }

  const Chain *DubWork::getChain() const {
    return craftWork->getChain();
  }

  std::optional<Segment *> DubWork::getSegmentAtChainMicros(long atChainMicros) const {
    return craftWork->getSegmentAtChainMicros(atChainMicros);
  }

  std::optional<Segment *> DubWork::getSegmentAtOffset(int offset) const {
    return craftWork->getSegmentAtOffset(offset);
  }

  std::optional<const Program *> DubWork::getMainProgram(const Segment *segment) const {
    return craftWork->getMainProgram(segment);
  }

  std::optional<const Program *> DubWork::getMacroProgram(const Segment &segment) const {
    return craftWork->getMacroProgram(segment);
  }

  std::optional<unsigned long long> DubWork::getDubbedToChainMicros() {
    return {atChainMicros};
  }

  void DubWork::setIntensityOverride(std::optional<float> intensity) {
    intensityOverride = intensity;
  }

  void DubWork::doDubFrame() {
    auto toChainMicros = atChainMicros + dubAheadMicros;
    auto segments = craftWork->getSegmentsIfReady(atChainMicros, toChainMicros);
    std::map<int, const Segment*> segmentById;
    //= segments.stream().collect(Collectors.toMap(Segment::getId, segment -> segment));
    for (const Segment * segment : segments) {
      segmentById.emplace(segment->id, segment);
    }
    if (segments.empty()) {
      spdlog::debug("Waiting for segments");
      return;
    }

    if (intensityOverride.has_value()) {
      nextIntensity = intensityOverride.value();
    } else {
      nextIntensity = segments.at(0)->intensity;
    }

    try {
      std::set<SegmentChoiceArrangementPick*> picks;
      for (auto pick : craftWork->getPicks(segments))
        if (!craftWork->isMuted(pick))
          picks.emplace(pick);

      std::set<ActiveAudio> activeAudios;
      for (const auto pick : picks) {
        const InstrumentAudio *audio = craftWork->getInstrumentAudio(pick);
        if (audio->waveformKey.empty()) {
          continue;
        }
        const long transientMicros = 0 < audio->transientSeconds ? static_cast<long>(audio->transientSeconds * ValueUtils::MICROS_PER_SECOND) : 0; // audio transient microseconds (to start audio before picked time)
        std::optional<unsigned long long> lengthMicros = 0 < pick->lengthMicros ? pick->lengthMicros : std::nullopt; // pick length microseconds, or empty if infinite
        const unsigned long long startAtMixerMicros = segmentById.at(pick->segmentId)->beginAtChainMicros                  // segment begin at chain microseconds
                                                + pick->startAtSegmentMicros                                         // plus pick start microseconds
                                                - transientMicros                                                    // minus transient microseconds
                                                - atChainMicros; // relative to beginning of this chunk
        std::optional<unsigned long long> stopAtMixerMicros = lengthMicros.has_value() ? startAtMixerMicros   // from start of this active audio
                                                                                             + transientMicros// revert transient microseconds from previous computation
                                                                                             + lengthMicros.value()
                                                                                       : std::nullopt; // add length of pick in microseconds
        if (startAtMixerMicros <= dubAheadMicros && (!stopAtMixerMicros.has_value() || stopAtMixerMicros >= 0)) {
          const auto instrument = craftWork->getInstrument(audio);
          // TODO don't recalculate all the active audios every time-- make this a part of a unified tick + get active audios function which is as efficient as possible at everything short of audio mixing
          activeAudios.emplace(ActiveAudio(
              *pick,
              *instrument,
              *audio,
              startAtMixerMicros,
              stopAtMixerMicros.has_value() ? stopAtMixerMicros : std::nullopt,
              AudioMathUtils::computeIntensityAmplitude(
                  audio,
                  templateConfig.getIntensityLayers(instrument->type),
                  templateConfig.getIntensityThreshold(instrument->type),
                  false, prevIntensity.value()
              ),
              AudioMathUtils::computeIntensityAmplitude(
                  audio,
                  templateConfig.getIntensityLayers(instrument->type),
                  templateConfig.getIntensityThreshold(instrument->type),
                  false, nextIntensity.value()
              )
          ));
        }
      }
      prevIntensity = {nextIntensity.value()};


      atChainMicros = toChainMicros;
      spdlog::debug("Dubbed to {}", toChainMicros / static_cast<float>(ValueUtils::MICROS_PER_SECOND));

    } catch (std::exception e) {
      didFailWhile("dubbing frame", e);
    }
  }


  void DubWork::didFailWhile(std::string msgWhile, const std::exception &e) {
    spdlog::error("Failed while {} because {}", msgWhile, e.what());

    running = false;
    finish();
  }

