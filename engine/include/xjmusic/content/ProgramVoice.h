// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_VOICE_H
#define XJMUSIC_PROGRAM_VOICE_H

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

  /**
   * Parse a ProgramVoice from a JSON object
   * @param json  input
   * @param entity  output
   */
  void from_json(const json &json, ProgramVoice &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "programId", entity.programId);
    entity.type = Instrument::parseType(json.at("type").get<std::string>());
    EntityUtils::setIfNotNull(json, "duration", entity.name);
    EntityUtils::setIfNotNull(json, "tones", entity.order);
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_VOICE_H
