// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

//
// Created by Charney Kaye on 3/25/24.
//

#include <algorithm>
#include <string>

#include "xjnexus/content/ProgramConfig.h"

namespace Content {

  const std::string ProgramConfig::DEFAULT = R"(
      barBeats = 4
      cutoffMinimumBars = 2
      doPatternRestartOnChord = false
  )";

  ProgramConfig::ProgramConfig() : ProgramConfig(DEFAULT) {}

  ProgramConfig::ProgramConfig(const Program &source) : ProgramConfig(source.config) {}

  ProgramConfig::ProgramConfig(const std::string &input) : ConfigParser(input, ConfigParser(DEFAULT)) {
    doPatternRestartOnChord = getSingleValue("doPatternRestartOnChord").getBool();
    barBeats = getSingleValue("barBeats").getInt();
    cutoffMinimumBars = getSingleValue("cutoffMinimumBars").getInt();
  }

  std::string ProgramConfig::toString() const {
    std::map<std::string, std::string> config;
    config["barBeats"] = std::to_string(barBeats);
    config["cutoffMinimumBars"] = std::to_string(cutoffMinimumBars);
    config["doPatternRestartOnChord"] = doPatternRestartOnChord ? "true" : "false";

    // Convert the map to a vector of pairs for sorting
    std::vector<std::pair<std::string, std::string>> configVec(config.begin(), config.end());

    // Sort the vector by key
    std::sort(configVec.begin(), configVec.end());

    std::ostringstream oss;
    for (const auto &pair: configVec) {
      oss << pair.first << " = " << pair.second << "\n";
    }

    return oss.str();
  }

  std::string ProgramConfig::getDefaultString() {
    return DEFAULT;
  }

}// namespace Content