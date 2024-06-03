// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_CHORD_VOICING_H
#define PROGRAM_SEQUENCE_CHORD_VOICING_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class ProgramSequenceChordVoicing : public Entity {
  public:

    ProgramSequenceChordVoicing() = default;

    UUID id;
    UUID programId;
    std::string programSequenceChordId;
    std::string programVoiceId;
    std::string notes;
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_CHORD_VOICING_H
