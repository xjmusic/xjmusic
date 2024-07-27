// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_AUDIO_SCHEDULE_EVENT_H
#define XJMUSIC_AUDIO_SCHEDULE_EVENT_H

#include "ActiveAudio.h"

namespace XJ {

  class AudioScheduleEvent {
  public:

    enum class EType {
      Create,
      Update,
      Delete
    };

    EType type;
    ActiveAudio audio;

    explicit AudioScheduleEvent(
        EType type,
        const ActiveAudio &audio
    );

    [[nodiscard]] unsigned long long getStartAtChainMicros() const {
      return this->audio.getStartAtChainMicros();
    }

    bool operator<(const AudioScheduleEvent &rhs) const {
      return this->getStartAtChainMicros() < rhs.getStartAtChainMicros();
    }

  };

} // XJ

#endif //XJMUSIC_AUDIO_SCHEDULE_EVENT_H
