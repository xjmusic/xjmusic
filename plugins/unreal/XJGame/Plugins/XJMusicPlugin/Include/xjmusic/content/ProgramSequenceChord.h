// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_SEQUENCE_CHORD_H
#define XJMUSIC_PROGRAM_SEQUENCE_CHORD_H

#include <string>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequenceChord : public ContentEntity {
  public:

    ProgramSequenceChord() = default;

    UUID programId;
    std::string programSequenceId;
    std::string name;
    float position{};
  };

  /**
   * Parse a ProgramSequenceChord from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, ProgramSequenceChord &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "programId", entity.programId);
    EntityUtils::setRequired(json, "programSequenceId", entity.programSequenceId);
    EntityUtils::setIfNotNull(json, "name", entity.name);
    EntityUtils::setIfNotNull(json, "position", entity.position);
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_SEQUENCE_CHORD_H
