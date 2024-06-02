// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_CHORD_VOICING_H
#define PROGRAM_SEQUENCE_CHORD_VOICING_H

#include <string>
#include <utility>

#include "Entity.h"

namespace Content {

  class ProgramSequenceChordVoicing : public Entity {
  public:
    ProgramSequenceChordVoicing() = default;
    UUID programId;
    std::string programSequenceChordId;
    std::string programVoiceId;
    std::string notes;
  };

}// namespace Content

#endif//PROGRAM_SEQUENCE_CHORD_VOICING_H
