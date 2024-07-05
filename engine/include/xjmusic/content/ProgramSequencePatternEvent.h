// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_SEQUENCE_PATTERN_EVENT_H
#define XJMUSIC_PROGRAM_SEQUENCE_PATTERN_EVENT_H

#include <string>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequencePatternEvent : public ContentEntity {
  public:

    ProgramSequencePatternEvent() = default;

    UUID programId;
    std::string programSequencePatternId;
    std::string programVoiceTrackId;
    float velocity{};
    float position{};
    float duration{};
    std::string tones;
  };

  /**
   * Parse a ProgramSequencePatternEvent from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, ProgramSequencePatternEvent &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "programId", entity.programId);
    EntityUtils::setRequired(json, "programSequencePatternId", entity.programSequencePatternId);
    EntityUtils::setRequired(json, "programVoiceTrackId", entity.programVoiceTrackId);
    EntityUtils::setIfNotNull(json, "velocity", entity.velocity);
    EntityUtils::setIfNotNull(json, "position", entity.position);
    EntityUtils::setIfNotNull(json, "duration", entity.duration);
    EntityUtils::setIfNotNull(json, "tones", entity.tones);
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_SEQUENCE_PATTERN_EVENT_H
