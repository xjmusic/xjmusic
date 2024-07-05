// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_SEQUENCE_BINDING_H
#define XJMUSIC_PROGRAM_SEQUENCE_BINDING_H

#include <string>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequenceBinding : public ContentEntity {
  public:

    ProgramSequenceBinding() = default;

    UUID programId;
    std::string programSequenceId;
    int offset{};
  };

  /**
   * Parse a ProgramSequenceBinding from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, ProgramSequenceBinding &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "programId", entity.programId);
    EntityUtils::setRequired(json, "programSequenceId", entity.programSequenceId);
    EntityUtils::setIfNotNull(json, "offset", entity.offset);
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_SEQUENCE_BINDING_H
