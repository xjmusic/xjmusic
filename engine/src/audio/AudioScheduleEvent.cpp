// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/audio/AudioScheduleEvent.h"

namespace XJ {
  AudioScheduleEvent::AudioScheduleEvent(
      const AudioScheduleEvent::EType type,
      const ActiveAudio &audio)
      : type(type),
        audio(audio) {}
} // XJ