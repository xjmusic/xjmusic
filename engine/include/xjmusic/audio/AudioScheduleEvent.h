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
    ActiveAudio schedule;

    /**
     * Construct an AudioScheduleEvent object
     * @param type  The type of the event
     * @param audio  The ActiveAudio object
     */
    explicit AudioScheduleEvent(
        EType type,
        ActiveAudio audio
    );

    /**
     * Get the startAtChainMicros of the ActiveAudio object
     * @return  The startAtChainMicros of the ActiveAudio object
     */
    [[nodiscard]] unsigned long long getStartAtChainMicros() const {
      return this->schedule.getStartAtChainMicros();
    }

    /**
     * Compare two AudioScheduleEvent objects by their startAtChainMicros
     * @param rhs  The right-hand side AudioScheduleEvent object
     * @return   True if this object is less than the right-hand side object
     */
    bool operator<(const AudioScheduleEvent &rhs) const {
      return this->getStartAtChainMicros() < rhs.getStartAtChainMicros();
    }

    /**
     * Convert an Audio Schedule Event Type enum value to a string
     * @param type  The Event Type enum value
     * @return      The string representation of the Event Type
     */
    static std::string toString(const EType &type);

    /**
     * Parse the Audio Schedule Event Type enum value from a string
     * @param value  The string to parse
     * @return      The Audio Schedule Event Type enum value
     */
    static EType parseType(const std::string &value);
  };

} // XJ

#endif //XJMUSIC_AUDIO_SCHEDULE_EVENT_H
