// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_VOICE_TRACK_H
#define XJMUSIC_PROGRAM_VOICE_TRACK_H

#include <string>
#include <utility>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramVoiceTrack : public ContentEntity {
  public:

    ProgramVoiceTrack() = default;

    UUID programId;
    std::string programVoiceId;
    std::string name;
    float order{};
  };

  /**
   * Parse a ProgramVoiceTrack from a JSON object
   * @param json  input
   * @param entity  output
   */
  void from_json(const json &json, ProgramVoiceTrack &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "programId", entity.programId);
    EntityUtils::setRequired(json, "programVoiceId", entity.programVoiceId);
    EntityUtils::setIfNotNull(json, "duration", entity.name);
    EntityUtils::setIfNotNull(json, "tones", entity.order);
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_VOICE_TRACK_H
