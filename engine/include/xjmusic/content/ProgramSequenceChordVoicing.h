// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_SEQUENCE_CHORD_VOICING_H
#define XJMUSIC_PROGRAM_SEQUENCE_CHORD_VOICING_H

#include <string>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequenceChordVoicing : public ContentEntity {
  public:

    ProgramSequenceChordVoicing() = default;

    UUID programId;
    std::string programSequenceChordId;
    std::string programVoiceId;
    std::string notes;
  };

  /**
   * Parse a ProgramSequenceChordVoicing from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, ProgramSequenceChordVoicing &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "programId", entity.programId);
    EntityUtils::setRequired(json, "programSequenceChordId", entity.programSequenceChordId);
    EntityUtils::setRequired(json, "programVoiceId", entity.programVoiceId);
    EntityUtils::setIfNotNull(json, "notes", entity.notes);
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_SEQUENCE_CHORD_VOICING_H
