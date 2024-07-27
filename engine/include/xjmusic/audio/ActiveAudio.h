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
    unsigned long long startAtChainMicros;
    std::optional<unsigned long long> stopAtChainMicros;
    float fromVolume;
    float toVolume;

  public:
    ActiveAudio(
        const SegmentChoiceArrangementPick *pick,
        const Instrument *instrument,
        const InstrumentAudio *audio,
        unsigned long long startAtChainMicros,
        std::optional<unsigned long long> stopAtChainMicros,
        float fromIntensity,
        float toIntensity);

    [[nodiscard]] float getFromVolume() const;

    [[nodiscard]] float getToVolume() const;

    [[nodiscard]] UUID getId() const;

    [[nodiscard]] const SegmentChoiceArrangementPick *getPick() const;

    [[nodiscard]] const Instrument *getInstrument() const;

    [[nodiscard]] unsigned long long getStartAtChainMicros() const;

    [[nodiscard]] std::optional<unsigned long long> getStopAtChainMicros() const;

    [[nodiscard]] const InstrumentAudio *getAudio() const;

    [[nodiscard]] int getReleaseMillis() const;

    /**
     Get the amplitude at a given amplitude position between 0 and 1.

     @param ap amplitude position
     @return amplitude
     */
    [[nodiscard]] float getAmplitude(float ap) const;

    /**
     * Compare two Active Audio by startAtChainMicros
     * @param lhs  Active Audio
     * @param rhs  Active Audio
     * @return   true if lhs < rhs
     */
    friend bool operator<(const ActiveAudio &lhs, const ActiveAudio &rhs) {
      return lhs.getStartAtChainMicros() < rhs.getStartAtChainMicros();
    }

    /**
     * Compare two Active Audio by startAtChainMicros
     * @param lhs  Active Audio
     * @param rhs  Active Audio
     * @return   true if lhs >rhs
     */
    friend bool operator>(const ActiveAudio &lhs, const ActiveAudio &rhs) {
      return lhs.getStartAtChainMicros() > rhs.getStartAtChainMicros();
    }

    /**
     * Compare two Active Audio for equality
     * @param lhs  Active Audio
     * @param rhs  Active Audio
     * @return   true if lhs == rhs
     */
    friend bool operator==(const ActiveAudio &lhs, const ActiveAudio &rhs) {
      return lhs.getStartAtChainMicros() == rhs.getStartAtChainMicros()
             && lhs.getStopAtChainMicros() == rhs.getStopAtChainMicros()
             && lhs.getFromVolume() == rhs.getFromVolume()
             && lhs.getToVolume() == rhs.getToVolume();
    }

    /**
     * Compare two Active Audio for equality
     * @param lhs  Active Audio
     * @param rhs  Active Audio
     * @return   true if lhs == rhs
     */
    friend bool operator!=(const ActiveAudio &lhs, const ActiveAudio &rhs) {
      return !(lhs == rhs);
    }

  };

}// namespace XJ

#endif //XJMUSIC_WORK_ACTIVE_AUDIO_H

