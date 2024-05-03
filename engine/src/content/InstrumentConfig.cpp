// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <algorithm>
#include <string>
#include <vector>

#include "xjnexus/content/InstrumentConfig.h"
#include "xjnexus/util/StringUtils.h"

namespace Content {

  const std::string InstrumentConfig::DEFAULT = R"(
      isAudioSelectionPersistent = true
      isMultiphonic = false
      isOneShot = false
      isOneShotCutoffEnabled = true
      isTonal = false
      oneShotObserveLengthOfEvents = []
      releaseMillis = 5
  )";

  InstrumentConfig::InstrumentConfig() : InstrumentConfig(DEFAULT) {}

  InstrumentConfig::InstrumentConfig(const Instrument &source) : InstrumentConfig(source.config) {}

  InstrumentConfig::InstrumentConfig(const std::string &input) : ConfigParser(input, ConfigParser(DEFAULT)) {
    isAudioSelectionPersistent = getSingleValue("isAudioSelectionPersistent").getBool();
    isMultiphonic = getSingleValue("isMultiphonic").getBool();
    isOneShot = getSingleValue("isOneShot").getBool();
    isOneShotCutoffEnabled = getSingleValue("isOneShotCutoffEnabled").getBool();
    isTonal = getSingleValue("isTonal").getBool();
    releaseMillis = getSingleValue("releaseMillis").getInt();

    // events are processed with Util::StringUtils::toMeme
    std::vector<std::string> rawEvents = getListValue("oneShotObserveLengthOfEvents").asListOfStrings();
    oneShotObserveLengthOfEvents.reserve(rawEvents.size());
    std::transform(rawEvents.begin(), rawEvents.end(), std::back_inserter(oneShotObserveLengthOfEvents),
                   [](const std::string &s) {
                     return Util::StringUtils::toMeme(s);
                   });
  }

  std::string InstrumentConfig::toString() const {
    std::map<std::string, std::string> config;
    config["isAudioSelectionPersistent"] = ConfigParser::format(isAudioSelectionPersistent);
    config["isMultiphonic"] = ConfigParser::format(isMultiphonic);
    config["isOneShot"] = ConfigParser::format(isOneShot);
    config["isOneShotCutoffEnabled"] = ConfigParser::format(isOneShotCutoffEnabled);
    config["isTonal"] = ConfigParser::format(isTonal);
    config["releaseMillis"] = std::to_string(releaseMillis);
    config["oneShotObserveLengthOfEvents"] = ConfigParser::format(oneShotObserveLengthOfEvents);

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

  std::string InstrumentConfig::getDefaultString() {
    return DEFAULT;
  }

}// namespace Content