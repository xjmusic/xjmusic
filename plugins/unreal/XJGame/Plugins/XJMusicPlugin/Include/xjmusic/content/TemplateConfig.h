
// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_TEMPLATE_CONFIG_H
#define XJMUSIC_TEMPLATE_CONFIG_H

#include <string>
#include <vector>

#include "Instrument.h"
#include "xjmusic/meme/MemeTaxonomy.h"
#include "xjmusic/util/ConfigParser.h"

namespace XJ {

  class TemplateConfig : public ConfigParser {
  public:
    static const std::string DEFAULT;
    static std::string formatMemeTaxonomy(MemeTaxonomy taxonomy);
    static std::string formatInstrumentTypeList(const std::vector<Instrument::Type> &input);
    static std::string formatInstrumentTypeList(const std::set<Instrument::Type> &input);
    explicit TemplateConfig();

    explicit TemplateConfig(const std::string &input);

    std::vector<Instrument::Type> detailLayerOrder;
    std::set<Instrument::Type> instrumentTypesForAudioLengthFinalization;
    std::set<Instrument::Type> instrumentTypesForInversionSeeking;
    std::set<std::string> eventNamesLarge;
    std::set<std::string> eventNamesMedium;
    std::set<std::string> eventNamesSmall;
    MemeTaxonomy memeTaxonomy;
    std::set<std::string> deltaArcBeatLayersToPrioritize;
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
     * Whether the instrument types for inverion seeking contains the given type
     * @param type  to find
     * @return  true if found
     */
    [[nodiscard]] bool instrumentTypesForInversionSeekingContains(Instrument::Type type) const;


    /**
     * Get the Choice Mute Probability of the given type of instrument
     * @param type of instrument
     * @return  The Choice Mute Probability
     */
    float getChoiceMuteProbability(Instrument::Type type);

    /**
     * Get the Dub Master Volume of the given type of instrument
     * @param type of instrument
     * @return The Dub Master Volume
     */
    float getDubMasterVolume(Instrument::Type type);

    /**
     * Get the Intensity Threshold of the given type of instrument
     * @param type of instrument
     * @return The Intensity Threshold
     */
    float getIntensityThreshold(Instrument::Type type);

    /**
     * Get the Intensity Layers of the given type of instrument
     * @param type of instrument
     * @return The Intensity Layers
     */
    int getIntensityLayers(Instrument::Type type);

    /**
     * Compare two TemplateConfigs for equality
     * @param other  The TemplateConfig to compare
     * @return  true if equal
     */
    bool operator==(const TemplateConfig & other) const;
  };

}// namespace XJ

#endif//XJMUSIC_TEMPLATE_CONFIG_H
