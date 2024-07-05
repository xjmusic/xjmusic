// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_SEQUENCE_PATTERN_H
#define XJMUSIC_PROGRAM_SEQUENCE_PATTERN_H

#include <string>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequencePattern : public ContentEntity {
  public:

    ProgramSequencePattern() = default;

    UUID programId;
    std::string programSequenceId;
    std::string programVoiceId;
    std::string name;
    int total{};
  };

  /**
   * Parse a ProgramSequencePattern from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, ProgramSequencePattern &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "programId", entity.programId);
    EntityUtils::setRequired(json, "programSequenceId", entity.programSequenceId);
    EntityUtils::setRequired(json, "programVoiceId", entity.programVoiceId);
    EntityUtils::setIfNotNull(json, "name", entity.name);
    EntityUtils::setIfNotNull(json, "total", entity.total);
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_SEQUENCE_PATTERN_H
