// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_SEQUENCE_H
#define XJMUSIC_PROGRAM_SEQUENCE_H

#include <string>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequence : public ContentEntity {
  public:

    ProgramSequence() = default;

    UUID programId;
    std::string name;
    std::string key;
    float intensity{};
    int total{};
  };

  /**
   * Parse a ProgramSequence from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, ProgramSequence &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "programId", entity.programId);
    EntityUtils::setIfNotNull(json, "name", entity.name);
    EntityUtils::setIfNotNull(json, "key", entity.key);
    EntityUtils::setIfNotNull(json, "intensity", entity.intensity);
    EntityUtils::setIfNotNull(json, "total", entity.total);
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_SEQUENCE_H
