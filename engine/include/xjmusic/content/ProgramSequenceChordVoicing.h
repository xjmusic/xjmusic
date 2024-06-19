// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_CHORD_VOICING_H
#define PROGRAM_SEQUENCE_CHORD_VOICING_H

#include <string>
#include <utility>

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

}// namespace XJ

#endif//PROGRAM_SEQUENCE_CHORD_VOICING_H