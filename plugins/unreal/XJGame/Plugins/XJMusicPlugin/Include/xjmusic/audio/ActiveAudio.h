// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_ACTIVE_AUDIO_H
#define XJMUSIC_WORK_ACTIVE_AUDIO_H

#include "xjmusic/content/InstrumentAudio.h"
#include "xjmusic/content/InstrumentConfig.h"
#include "xjmusic/segment/SegmentChoiceArrangementPick.h"

namespace XJ {

  class ActiveAudio {
    const SegmentChoiceArrangementPick *pick;
    const Instrument *instrument;
    const InstrumentAudio *audio;
    InstrumentConfig instrumentConfig;
    unsigned long long startAtChainMicros;
    std::optional<unsigned long long> stopAtChainMicros;
    float fromAmplitude;
    float toAmplitude;

  public:
    ActiveAudio(
        const SegmentChoiceArrangementPick *pick,
        const Instrument *instrument,
        const InstrumentAudio *audio,
        unsigned long long startAtChainMicros,
        std::optional<unsigned long long> stopAtChainMicros,
        float fromIntensityAmplitude,
        float toIntensityAmplitude);

    UUID getId();

    const SegmentChoiceArrangementPick * getPick();

    const Instrument * getInstrument();

    unsigned long long getStartAtChainMicros() const;

    std::optional<unsigned long long> getStopAtChainMicros() const;

    const InstrumentAudio * getAudio();

    int getReleaseMillis() const;

    /**
     Get the amplitude at a given amplitude position between 0 and 1.

     @param ap amplitude position
     @return amplitude
     */
    float getAmplitude(const float ap) const;

    /**
     * Compare two Active Audio
     * @param lhs  Active Audio
     * @param rhs  Active Audio
     * @return   true if lhs < rhs
     */
    friend bool operator<(const ActiveAudio &lhs, const ActiveAudio &rhs) {
      return lhs.getStartAtChainMicros() < rhs.getStartAtChainMicros();
    }

  };

}// namespace XJ

#endif //XJMUSIC_WORK_ACTIVE_AUDIO_H

