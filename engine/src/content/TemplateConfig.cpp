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
              detailLayerOrder = ["Bass","Stripe","Pad","Sticky","Stab"]
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
              eventNamesLarge = ["LARGE","BIG","HIGH","PRIMARY"]
              eventNamesMedium = ["MEDIUM","REGULAR","MIDDLE","SECONDARY"]
              eventNamesSmall = ["SMALL","LITTLE","LOW"]
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


TemplateConfig::TemplateConfig(const Template &source) : TemplateConfig(source.config) {}


std::map<Instrument::Type, int> parseInstrumentTypeIntMap(ConfigObjectValue objectValue) {
  std::map<Instrument::Type, int> resultMap;
  for (const auto &entry: objectValue.asMapOfSingleOrList()) {
    Instrument::Type instrumentTypeKey = Instrument::parseType(entry.first);
    if (std::holds_alternative<ConfigSingleValue>(entry.second)) {
      resultMap[instrumentTypeKey] = std::get<ConfigSingleValue>(entry.second).getInt();
    }
  }
  return resultMap;
}


std::map<Instrument::Type, float> parseInstrumentTypeFloatMap(ConfigObjectValue objectValue) {
  std::map<Instrument::Type, float> resultMap;
  for (const auto &entry: objectValue.asMapOfSingleOrList()) {
    Instrument::Type instrumentTypeKey = Instrument::parseType(entry.first);
    if (std::holds_alternative<ConfigSingleValue>(entry.second)) {
      resultMap[instrumentTypeKey] = std::get<ConfigSingleValue>(entry.second).getFloat();
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


std::string formatInstrumentTypeIntMap(const std::map<Instrument::Type, int> &values) {
  // Convert the map to a vector of pairs for sorting
  std::vector<std::pair<std::string, int>> valuesVec;
  valuesVec.reserve(values.size());
  for (const auto &entry: values) {
    valuesVec.emplace_back(Instrument::toString(entry.first), entry.second);
  }

  // Sort the vector by key
  std::sort(valuesVec.begin(), valuesVec.end());

  std::ostringstream oss;
  oss << "{\n";
  for (const auto &entry: valuesVec) {
    oss << "    " << entry.first << " = " << std::to_string(entry.second) << "\n";
  }
  oss << "  }";
  return oss.str();
}


std::string formatInstrumentTypeFloatMap(const std::map<Instrument::Type, float> &values) {
  // Convert the map to a vector of pairs for sorting
  std::vector<std::pair<std::string, float>> valuesVec;
  valuesVec.reserve(values.size());
  for (const auto &entry: values) {
    valuesVec.emplace_back(Instrument::toString(entry.first), entry.second);
  }

  // Sort the vector by key
  std::sort(valuesVec.begin(), valuesVec.end());

  std::ostringstream oss;
  oss << "{\n";
  for (const auto &entry: valuesVec) {
    oss << "    " << entry.first << " = " << StringUtils::formatFloat(entry.second) << "\n";
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


TemplateConfig::TemplateConfig(const std::string &input) : ConfigParser(input, ConfigParser(DEFAULT)) {
  choiceMuteProbability = parseInstrumentTypeFloatMap(getObjectValue("choiceMuteProbability"));
  deltaArcBeatLayersIncoming = getSingleValue("deltaArcBeatLayersIncoming").getInt();
  deltaArcBeatLayersToPrioritize = getListValue("deltaArcBeatLayersToPrioritize").asListOfStrings();
  deltaArcDetailLayersIncoming = getSingleValue("deltaArcDetailLayersIncoming").getInt();
  deltaArcEnabled = getSingleValue("deltaArcEnabled").getBool();
  detailLayerOrder = parseInstrumentTypeList(getListValue("detailLayerOrder"));
  dubMasterVolume = parseInstrumentTypeFloatMap(getObjectValue("dubMasterVolume"));
  eventNamesLarge = getListValue("eventNamesLarge").asListOfStrings();
  eventNamesMedium = getListValue("eventNamesMedium").asListOfStrings();
  eventNamesSmall = getListValue("eventNamesSmall").asListOfStrings();
  instrumentTypesForAudioLengthFinalization = parseInstrumentTypeList(
      getListValue("instrumentTypesForAudioLengthFinalization"));
  instrumentTypesForInversionSeeking = parseInstrumentTypeList(
      getListValue("instrumentTypesForInversionSeeking"));
  intensityAutoCrescendoEnabled = getSingleValue("intensityAutoCrescendoEnabled").getBool();
  intensityAutoCrescendoMaximum = getSingleValue("intensityAutoCrescendoMaximum").getFloat();
  intensityAutoCrescendoMinimum = getSingleValue("intensityAutoCrescendoMinimum").getFloat();
  intensityLayers = parseInstrumentTypeIntMap(getObjectValue("intensityLayers"));
  intensityThreshold = parseInstrumentTypeFloatMap(getObjectValue("intensityThreshold"));
  mainProgramLengthMaxDelta = getSingleValue("mainProgramLengthMaxDelta").getInt();
  auto setOfMapsOfStrings = getListValue("memeTaxonomy").asListOfMapsOfStrings();
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
  for (const auto &pair: configVec) {
    oss << pair.first << " = " << pair.second << "\n";
  }

  return oss.str();
}


std::string TemplateConfig::getDefaultString() {
  return DEFAULT;
}

