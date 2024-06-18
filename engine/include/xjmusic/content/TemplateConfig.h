
// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_TEMPLATE_CONFIG_H
#define XJMUSIC_TEMPLATE_CONFIG_H

#include <string>
#include <vector>

#include "xjmusic/util/ConfigParser.h"
#include "Instrument.h"
#include "xjmusic/meme/MemeTaxonomy.h"
#include "Template.h"

namespace XJ {

  class TemplateConfig : public ConfigParser {
  private:
    static const std::string DEFAULT;
    static std::string formatMemeTaxonomy(MemeTaxonomy taxonomy);
    static std::string formatInstrumentTypeList(const std::vector<Instrument::Type> &input);

  public:
    explicit TemplateConfig();

    explicit TemplateConfig(const Template &input);

    explicit TemplateConfig(const std::string &input);

    std::vector<Instrument::Type> detailLayerOrder;
    std::vector<Instrument::Type> instrumentTypesForAudioLengthFinalization;
    std::vector<Instrument::Type> instrumentTypesForInversionSeeking;
    std::vector<std::string> eventNamesLarge;
    std::vector<std::string> eventNamesMedium;
    std::vector<std::string> eventNamesSmall;
    MemeTaxonomy memeTaxonomy;
    std::vector<std::string> deltaArcBeatLayersToPrioritize;
    bool intensityAutoCrescendoEnabled;
    bool deltaArcEnabled;
    bool stickyBunEnabled;
    std::map<Instrument::Type, float> choiceMuteProbability;
    std::map<Instrument::Type, float> dubMasterVolume;
    std::map<Instrument::Type, float> intensityThreshold;
    std::map<Instrument::Type, int> intensityLayers;
    float intensityAutoCrescendoMaximum;
    float intensityAutoCrescendoMinimum;
    float mixerCompressAheadSeconds;
    float mixerCompressDecaySeconds;
    float mixerCompressRatioMax;
    float mixerCompressRatioMin;
    float mixerCompressToAmplitude;
    float mixerNormalizationBoostThreshold;
    float mixerNormalizationCeiling;
    int deltaArcBeatLayersIncoming;
    int deltaArcDetailLayersIncoming;
    int mainProgramLengthMaxDelta;
    int mixerDspBufferSize;
    int mixerHighpassThresholdHz;
    int mixerLowpassThresholdHz;

    /**
     * Format the TemplateConfig as a HOCON string
     * @return  The HOCON string
     */
    [[nodiscard]] std::string toString() const;


    /**
     * Get the default TemplateConfig as a HOCON string
     */
    [[nodiscard]] static std::string getDefaultString();
  };

}// namespace XJ

#endif//XJMUSIC_TEMPLATE_CONFIG_H
