// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <algorithm>
#include <string>
#include <vector>
#include <sstream>

#include "xjmusic/content/InstrumentConfig.h"

#include <xjmusic/util/StringUtils.h>

using namespace XJ;


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


InstrumentConfig::InstrumentConfig(const std::string &input) : ConfigParser(input, ConfigParser(DEFAULT)) {
  isAudioSelectionPersistent = getSingleValue("isAudioSelectionPersistent").getBool();
  isMultiphonic = getSingleValue("isMultiphonic").getBool();
  isOneShot = getSingleValue("isOneShot").getBool();
  isOneShotCutoffEnabled = getSingleValue("isOneShotCutoffEnabled").getBool();
  isTonal = getSingleValue("isTonal").getBool();
  releaseMillis = getSingleValue("releaseMillis").getInt();

  // events are processed with StringUtils::toMeme
  std::vector<std::string> rawEvents = getListValue("oneShotObserveLengthOfEvents").asListOfStrings();
  oneShotObserveLengthOfEvents.reserve(rawEvents.size());
  std::transform(rawEvents.begin(), rawEvents.end(), std::back_inserter(oneShotObserveLengthOfEvents),
                 [](const std::string &s) {
                   return StringUtils::toMeme(s);
                 });
}


std::string InstrumentConfig::toString() const {
  std::map<std::string, std::string> config;
  config["isAudioSelectionPersistent"] = format(isAudioSelectionPersistent);
  config["isMultiphonic"] = format(isMultiphonic);
  config["isOneShot"] = format(isOneShot);
  config["isOneShotCutoffEnabled"] = format(isOneShotCutoffEnabled);
  config["isTonal"] = format(isTonal);
  config["releaseMillis"] = std::to_string(releaseMillis);
  config["oneShotObserveLengthOfEvents"] = format(oneShotObserveLengthOfEvents);

  // Convert the map to a vector of pairs for sorting
  std::vector<std::pair<std::string, std::string>> configVec(config.begin(), config.end());

  // Sort the vector by key
  std::sort(configVec.begin(), configVec.end());

  std::ostringstream oss;
  for (const auto &[key, val]: configVec) {
    oss << key << " = " << val << "\n";
  }

  return oss.str();
}


std::string InstrumentConfig::getDefaultString() {
  return DEFAULT;
}


bool InstrumentConfig::oneShotObserveLengthOfEventsContains(const std::string &eventName) const {
  return std::find(oneShotObserveLengthOfEvents.begin(), oneShotObserveLengthOfEvents.end(), eventName) !=
         oneShotObserveLengthOfEvents.end();
}

