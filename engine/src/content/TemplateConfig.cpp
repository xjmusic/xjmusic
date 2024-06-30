// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <algorithm>
#include <string>
#include <vector>

#include "xjmusic/content/TemplateConfig.h"

using namespace XJ;


const std::string TemplateConfig::DEFAULT = R"(
              choiceMuteProbability = {
                  Background = 0.0
                  Bass = 0.0
                  Drum = 0.0
                  Hook = 0.0
                  Pad = 0.0
                  Percussion = 0.0
                  Stab = 0.0
                  Sticky = 0.0
                  Stripe = 0.0
                  Transition = 0.0
                }
              deltaArcBeatLayersIncoming = 1
              deltaArcBeatLayersToPrioritize = ["kick"]
              deltaArcDetailLayersIncoming = 1
              deltaArcEnabled = false
              detailLayerOrder = ["Bass","Pad","Stab","Sticky","Stripe"]
              dubMasterVolume = {
                  Background = 1.0
                  Bass = 1.0
                  Drum = 1.0
                  Hook = 1.0
                  Pad = 1.0
                  Percussion = 1.0
                  Stab = 1.0
                  Sticky = 1.0
                  Stripe = 1.0
                  Transition = 1.0
                }
              eventNamesLarge = ["BIG","HIGH","LARGE","PRIMARY"]
              eventNamesMedium = ["MEDIUM","MIDDLE","REGULAR","SECONDARY"]
              eventNamesSmall = ["LITTLE","LOW","SMALL"]
              instrumentTypesForAudioLengthFinalization = ["Bass","Pad","Stab","Sticky","Stripe"]
              instrumentTypesForInversionSeeking = ["Pad","Stab","Sticky","Stripe"]
              intensityAutoCrescendoEnabled = true
              intensityAutoCrescendoMaximum = 0.8
              intensityAutoCrescendoMinimum = 0.2
              intensityLayers = {
                  Background = 3
                  Bass = 1
                  Drum = 1
                  Hook = 3
                  Pad = 3
                  Percussion = 3
                  Stab = 2
                  Sticky = 2
                  Stripe = 2
                  Transition = 3
                }
              intensityThreshold = {
                  Background = 0.5
                  Bass = 0.5
                  Drum = 0.5
                  Hook = 0.5
                  Pad = 0.5
                  Percussion = 0.5
                  Stab = 0.5
                  Sticky = 0.5
                  Stripe = 0.5
                  Transition = 0.5
                }
              mainProgramLengthMaxDelta = 280
              memeTaxonomy = [
                  {
                    memes = ["BLUE","GREEN","RED"]
                    name = "COLOR"
                  },
                  {
                    memes = ["FALL","SPRING","SUMMER","WINTER"]
                    name = "SEASON"
                  }
                ]
              mixerCompressAheadSeconds = 0.05
              mixerCompressDecaySeconds = 0.125
              mixerCompressRatioMax = 1.0
              mixerCompressRatioMin = 0.3
              mixerCompressToAmplitude = 1.0
              mixerDspBufferSize = 1024
              mixerHighpassThresholdHz = 30
              mixerLowpassThresholdHz = 15000
              mixerNormalizationBoostThreshold = 1.0
              mixerNormalizationCeiling = 0.999
              stickyBunEnabled = true
  )";


TemplateConfig::TemplateConfig() : TemplateConfig(DEFAULT) {}


TemplateConfig::TemplateConfig(const Template *input) : TemplateConfig(input->config) {}


std::map<Instrument::Type, int> parseInstrumentTypeIntMap(ConfigObjectValue objectValue) {
  std::map<Instrument::Type, int> resultMap;
  for (const auto &[key, val]: objectValue.asMapOfSingleOrList()) {
    Instrument::Type instrumentTypeKey = Instrument::parseType(key);
    if (std::holds_alternative<ConfigSingleValue>(val)) {
      resultMap[instrumentTypeKey] = std::get<ConfigSingleValue>(val).getInt();
    }
  }
  return resultMap;
}


std::map<Instrument::Type, float> parseInstrumentTypeFloatMap(ConfigObjectValue objectValue) {
  std::map<Instrument::Type, float> resultMap;
  for (const auto &[key, val]: objectValue.asMapOfSingleOrList()) {
    Instrument::Type instrumentTypeKey = Instrument::parseType(key);
    if (std::holds_alternative<ConfigSingleValue>(val)) {
      resultMap[instrumentTypeKey] = std::get<ConfigSingleValue>(val).getFloat();
    }
  }
  return resultMap;
}


std::vector<Instrument::Type> parseInstrumentTypeList(ConfigListValue listValue) {
  std::vector<Instrument::Type> result;
  result.reserve(listValue.size());
  for (const auto &value: listValue.asListOfStrings()) {
    result.push_back(Instrument::parseType(value));
  }
  return result;
}


std::set<Instrument::Type> parseInstrumentTypeSet(const ConfigListValue &listValue) {
  std::set<Instrument::Type> result;
  for (const auto &value: listValue.asListOfStrings()) {
    result.emplace(Instrument::parseType(value));
  }
  return result;
}


std::string formatInstrumentTypeIntMap(const std::map<Instrument::Type, int> &values) {
  // Convert the map to a vector of pairs for sorting
  std::vector<std::pair<std::string, int>> valuesVec;
  valuesVec.reserve(values.size());
  for (const auto &[key, val]: values) {
    valuesVec.emplace_back(Instrument::toString(key), val);
  }

  // Sort the vector by key
  std::sort(valuesVec.begin(), valuesVec.end());

  std::ostringstream oss;
  oss << "{\n";
  for (const auto &[key, val]: valuesVec) {
    oss << "    " << key << " = " << std::to_string(val) << "\n";
  }
  oss << "  }";
  return oss.str();
}


std::string formatInstrumentTypeFloatMap(const std::map<Instrument::Type, float> &values) {
  // Convert the map to a vector of pairs for sorting
  std::vector<std::pair<std::string, float>> valuesVec;
  valuesVec.reserve(values.size());
  for (const auto &[key, val]: values) {
    valuesVec.emplace_back(Instrument::toString(key), val);
  }

  // Sort the vector by key
  std::sort(valuesVec.begin(), valuesVec.end());

  std::ostringstream oss;
  oss << "{\n";
  for (const auto &[key, val]: valuesVec) {
    oss << "    " << key << " = " << StringUtils::formatFloat(val) << "\n";
  }
  oss << "  }";
  return oss.str();
}


std::string TemplateConfig::formatMemeTaxonomy(MemeTaxonomy taxonomy) {
  std::ostringstream oss;
  std::vector<MemeCategory> sortedCategories;
  sortedCategories.reserve(taxonomy.getCategories().size());
  for (auto &category: taxonomy.getCategories()) {
    sortedCategories.push_back(category);
  }
  std::sort(sortedCategories.begin(), sortedCategories.end());
  oss << "[\n";
  for (int i = 0; i < sortedCategories.size(); i++) {
    std::vector<std::string> sortedMemes;
    sortedMemes.reserve(sortedCategories.at(i).getMemes().size());
    for (const auto &meme: sortedCategories.at(i).getMemes()) {
      sortedMemes.push_back(meme);
    }
    std::sort(sortedMemes.begin(), sortedMemes.end());
    oss << "    {\n";
    oss << "      memes = " << format(sortedMemes) << "\n";
    oss << "      name = " << format(sortedCategories.at(i).getName()) << "\n";
    oss << ((i < sortedCategories.size() - 1) ? "    },\n" : "    }\n");
  }
  oss << "  ]";
  return oss.str();
}


std::string TemplateConfig::formatInstrumentTypeList(const std::vector<Instrument::Type> &input) {
  std::vector<std::string> result;
  result.reserve(input.size());
  for (const auto &value: input) {
    result.push_back(Instrument::toString(value));
  }
  return format(result);
}


std::string TemplateConfig::formatInstrumentTypeList(const std::set<Instrument::Type> &input) {
  std::vector<Instrument::Type> sorted(input.begin(), input.end());
  std::sort(sorted.begin(), sorted.end());
  return formatInstrumentTypeList(sorted);
}


TemplateConfig::TemplateConfig(const std::string &input) : ConfigParser(input, ConfigParser(DEFAULT)) {
  choiceMuteProbability = parseInstrumentTypeFloatMap(getObjectValue("choiceMuteProbability"));
  deltaArcBeatLayersIncoming = getSingleValue("deltaArcBeatLayersIncoming").getInt();
  deltaArcBeatLayersToPrioritize = getListValue("deltaArcBeatLayersToPrioritize").asSetOfStrings();
  deltaArcDetailLayersIncoming = getSingleValue("deltaArcDetailLayersIncoming").getInt();
  deltaArcEnabled = getSingleValue("deltaArcEnabled").getBool();
  detailLayerOrder = parseInstrumentTypeList(getListValue("detailLayerOrder"));
  dubMasterVolume = parseInstrumentTypeFloatMap(getObjectValue("dubMasterVolume"));
  eventNamesLarge = getListValue("eventNamesLarge").asSetOfStrings();
  eventNamesMedium = getListValue("eventNamesMedium").asSetOfStrings();
  eventNamesSmall = getListValue("eventNamesSmall").asSetOfStrings();
  instrumentTypesForAudioLengthFinalization = parseInstrumentTypeSet(
      getListValue("instrumentTypesForAudioLengthFinalization"));
  instrumentTypesForInversionSeeking = parseInstrumentTypeSet(
      getListValue("instrumentTypesForInversionSeeking"));
  intensityAutoCrescendoEnabled = getSingleValue("intensityAutoCrescendoEnabled").getBool();
  intensityAutoCrescendoMaximum = getSingleValue("intensityAutoCrescendoMaximum").getFloat();
  intensityAutoCrescendoMinimum = getSingleValue("intensityAutoCrescendoMinimum").getFloat();
  intensityLayers = parseInstrumentTypeIntMap(getObjectValue("intensityLayers"));
  intensityThreshold = parseInstrumentTypeFloatMap(getObjectValue("intensityThreshold"));
  mainProgramLengthMaxDelta = getSingleValue("mainProgramLengthMaxDelta").getInt();
  const auto setOfMapsOfStrings = getListValue("memeTaxonomy").asListOfMapsOfStrings();
  memeTaxonomy = MemeTaxonomy::fromList(setOfMapsOfStrings);
  mixerCompressAheadSeconds = getSingleValue("mixerCompressAheadSeconds").getFloat();
  mixerCompressDecaySeconds = getSingleValue("mixerCompressDecaySeconds").getFloat();
  mixerCompressRatioMax = getSingleValue("mixerCompressRatioMax").getFloat();
  mixerCompressRatioMin = getSingleValue("mixerCompressRatioMin").getFloat();
  mixerCompressToAmplitude = getSingleValue("mixerCompressToAmplitude").getFloat();
  mixerDspBufferSize = getSingleValue("mixerDspBufferSize").getInt();
  mixerHighpassThresholdHz = getSingleValue("mixerHighpassThresholdHz").getInt();
  mixerLowpassThresholdHz = getSingleValue("mixerLowpassThresholdHz").getInt();
  mixerNormalizationBoostThreshold = getSingleValue("mixerNormalizationBoostThreshold").getFloat();
  mixerNormalizationCeiling = getSingleValue("mixerNormalizationCeiling").getFloat();
  stickyBunEnabled = getSingleValue("stickyBunEnabled").getBool();
}


std::string TemplateConfig::toString() const {
  std::map<std::string, std::string> config;
  config["choiceMuteProbability"] = formatInstrumentTypeFloatMap(choiceMuteProbability);
  config["deltaArcBeatLayersIncoming"] = format(deltaArcBeatLayersIncoming);
  config["deltaArcBeatLayersToPrioritize"] = format(deltaArcBeatLayersToPrioritize);
  config["deltaArcDetailLayersIncoming"] = format(deltaArcDetailLayersIncoming);
  config["deltaArcEnabled"] = format(deltaArcEnabled);
  config["detailLayerOrder"] = formatInstrumentTypeList(detailLayerOrder);
  config["dubMasterVolume"] = formatInstrumentTypeFloatMap(dubMasterVolume);
  config["eventNamesLarge"] = format(eventNamesLarge);
  config["eventNamesMedium"] = format(eventNamesMedium);
  config["eventNamesSmall"] = format(eventNamesSmall);
  config["instrumentTypesForAudioLengthFinalization"] = formatInstrumentTypeList(
      instrumentTypesForAudioLengthFinalization);
  config["instrumentTypesForInversionSeeking"] = formatInstrumentTypeList(instrumentTypesForInversionSeeking);
  config["intensityAutoCrescendoEnabled"] = format(intensityAutoCrescendoEnabled);
  config["intensityAutoCrescendoMaximum"] = format(intensityAutoCrescendoMaximum);
  config["intensityAutoCrescendoMinimum"] = format(intensityAutoCrescendoMinimum);
  config["intensityLayers"] = formatInstrumentTypeIntMap(intensityLayers);
  config["intensityThreshold"] = formatInstrumentTypeFloatMap(intensityThreshold);
  config["mainProgramLengthMaxDelta"] = format(mainProgramLengthMaxDelta);
  config["memeTaxonomy"] = formatMemeTaxonomy(memeTaxonomy);
  config["mixerCompressAheadSeconds"] = format(mixerCompressAheadSeconds);
  config["mixerCompressDecaySeconds"] = format(mixerCompressDecaySeconds);
  config["mixerCompressRatioMax"] = format(mixerCompressRatioMax);
  config["mixerCompressRatioMin"] = format(mixerCompressRatioMin);
  config["mixerCompressToAmplitude"] = format(mixerCompressToAmplitude);
  config["mixerDspBufferSize"] = format(mixerDspBufferSize);
  config["mixerHighpassThresholdHz"] = format(mixerHighpassThresholdHz);
  config["mixerLowpassThresholdHz"] = format(mixerLowpassThresholdHz);
  config["mixerNormalizationBoostThreshold"] = format(mixerNormalizationBoostThreshold);
  config["mixerNormalizationCeiling"] = format(mixerNormalizationCeiling);
  config["stickyBunEnabled"] = format(stickyBunEnabled);

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


bool TemplateConfig::instrumentTypesForInversionSeekingContains(const Instrument::Type type) const {
  return std::find(instrumentTypesForInversionSeeking.begin(), instrumentTypesForInversionSeeking.end(), type) !=
         instrumentTypesForInversionSeeking.end();
}

float TemplateConfig::getChoiceMuteProbability(const Instrument::Type type) {
  if (choiceMuteProbability.find(type) != choiceMuteProbability.end()) {
    return choiceMuteProbability[type];
  } else {
    return 0.0f;
  }
}

float TemplateConfig::getDubMasterVolume(const Instrument::Type type) {
  if (dubMasterVolume.find(type) != dubMasterVolume.end()) {
    return dubMasterVolume[type];
  } else {
    return 0.0f;
  }
}

float TemplateConfig::getIntensityThreshold(const Instrument::Type type) {
  if (intensityThreshold.find(type) != intensityThreshold.end()) {
    return intensityThreshold[type];
  } else {
    return 0.0f;
  }
}

int TemplateConfig::getIntensityLayers(const Instrument::Type type) {
  if (intensityLayers.find(type) != intensityLayers.end()) {
    return intensityLayers[type];
  } else {
    return 0;
  }
}

