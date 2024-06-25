// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_VOICE_H
#define PROGRAM_VOICE_H

#include <set>
#include <string>
#include <utility>

#include "Instrument.h"

namespace XJ {

  class ProgramVoice : public ContentEntity {
  public:

    ProgramVoice() = default;

    UUID programId;
    Instrument::Type type{Instrument::Type::Drum};
    std::string name;
    float order{};

    /**
     * Get the names of a set of voices
     * @param voices for which to get names
     * @return  a set of names
     */
    static std::set<std::string> getNames(const std::set<const ProgramVoice *>& voices);
  };

}// namespace XJ

#endif//PROGRAM_VOICE_H
