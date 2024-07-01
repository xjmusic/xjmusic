// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_ACTIVE_AUDIO_H
#define XJMUSIC_WORK_ACTIVE_AUDIO_H
#include <xjmusic/content/InstrumentAudio.h>
#include <xjmusic/content/InstrumentConfig.h>
#include <xjmusic/segment/SegmentChoiceArrangementPick.h>

namespace XJ {

class ActiveAudio {
  InstrumentConfig instrumentConfig;
  SegmentChoiceArrangementPick pick;
  InstrumentAudio audio;
  unsigned long long startAtMixerMicros;
  std::optional<unsigned long long > stopAtMixerMicros;
  Instrument instrument;
  float fromAmplitude;
  float toAmplitude;

public:
  ActiveAudio(
      SegmentChoiceArrangementPick pick,
      Instrument instrument,
      InstrumentAudio audio,
      unsigned long long startAtMixerMicros,
      std::optional<unsigned long long> stopAtMixerMicros,
      float fromIntensityAmplitude,
      float toIntensityAmplitude);

  UUID getId();

  SegmentChoiceArrangementPick getPick();

  Instrument getInstrument();

  unsigned long long getStartAtMixerMicros() const;

  std::optional<unsigned long long> getStopAtMixerMicros() const;

  InstrumentAudio getAudio();

  int getReleaseMillis() const;

  /**
   Get the amplitude at a given amplitude position between 0 and 1.

   @param ap amplitude position
   @return amplitude
   */
  float getAmplitude(const float ap) const;
};

}// namespace XJ

#endif //XJMUSIC_WORK_ACTIVE_AUDIO_H

