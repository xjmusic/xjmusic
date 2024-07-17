// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/content/TemplateConfig.h"

using namespace XJ;

TEST(TemplateConfigTest, SetFromDefaults) {
  const TemplateConfig subject;

  EXPECT_NEAR(1.0, subject.mixerCompressToAmplitude, 0.01);
}

TEST(TemplateConfigTest, DefaultsToString) {
  const TemplateConfig subject;

  const std::vector<std::string> defaultLines = StringUtils::split(TemplateConfig::DEFAULT, '\n');
  const std::vector<std::string> subjectLines = StringUtils::split(subject.toString(), '\n');

  for (int i = 0; i < defaultLines.size(); i++) {
    EXPECT_EQ(defaultLines[i], subjectLines[i]) << "Mismatch at line " << i << " of " << defaultLines.size();
  }
}

TEST(TemplateConfigTest, GetChoiceMuteProbability) {
  const TemplateConfig subject;

  EXPECT_FLOAT_EQ(0.0f, subject.choiceMuteProbability.at(Instrument::Type::Bass));
}

TEST(TemplateConfigTest, GetDubMasterVolume) {
  const TemplateConfig subject;

  EXPECT_FLOAT_EQ(1.0f, subject.dubMasterVolume.at(Instrument::Type::Bass));
}

TEST(TemplateConfigTest, GetIntensityLayers) {
  const TemplateConfig subject;

  EXPECT_EQ(1, subject.intensityLayers.at(Instrument::Type::Bass));
  EXPECT_EQ(3, subject.intensityLayers.at(Instrument::Type::Pad));
}

TEST(TemplateConfigTest, InstrumentTypesForInversionSeekingContains) {
  const TemplateConfig subject;

  EXPECT_TRUE(subject.instrumentTypesForInversionSeekingContains(Instrument::Type::Pad));
  EXPECT_FALSE(subject.instrumentTypesForInversionSeekingContains(Instrument::Type::Bass));
}

TEST(TemplateConfigTest, getChoiceMuteProbability) {
  TemplateConfig subject;

  EXPECT_EQ(0, subject.getChoiceMuteProbability(Instrument::Type::Pad));
}

TEST(TemplateConfigTest, getDubMasterVolume) {
  TemplateConfig subject;

  EXPECT_EQ(1.0, subject.getDubMasterVolume(Instrument::Type::Pad));
}

TEST(TemplateConfigTest, getIntensityThreshold) {
  TemplateConfig subject;

  EXPECT_EQ(0.5, subject.getIntensityThreshold(Instrument::Type::Pad));
}

TEST(TemplateConfigTest, getIntensityLayers) {
  TemplateConfig subject;

  EXPECT_EQ(3.0, subject.getIntensityLayers(Instrument::Type::Percussion));
}

TEST(TemplateConfigTest, ParseRealWorldComplexConfig) {
  auto input = "choiceMuteProbability = {\n    Background = 0.0\n    Bass = 0.0\n    Drum = 0.0\n    Hook = 0.0\n    Pad = 0.0\n    Percussion = 0.0\n    Stab = 0.0\n    Sticky = 0.0\n    Stripe = 0.0\n    Transition = 0.0\n  }\ndeltaArcBeatLayersIncoming = 1\ndeltaArcBeatLayersToPrioritize = Hook\ndeltaArcDetailLayersIncoming = 3\ndeltaArcEnabled = false\ndetailLayerOrder = [\"Pad\",\"Stripe\",\"Bass\",\"Sticky\",\"Stab\"]\ndubMasterVolume = {\n    Background = 1.0\n    Bass = 1.0\n    Drum = 1.0\n    Hook = 1.0\n    Pad = 1.0\n    Percussion = 1.0\n    Stab = 1.0\n    Sticky = 1.0\n    Stripe = 1.0\n    Transition = 1.0\n  }\neventNamesLarge = [\"LARGE\",\"BIG\",\"HIGH\",\"PRIMARY\"]\neventNamesMedium = [\"MEDIUM\",\"REGULAR\",\"MIDDLE\",\"SECONDARY\"]\neventNamesSmall = [\"SMALL\",\"LITTLE\",\"LOW\"]\ninstrumentTypesForAudioLengthFinalization = [\"Bass\",\"Pad\",\"Stab\",\"Sticky\",\"Stripe\"]\ninstrumentTypesForInversionSeeking = [\"Pad\",\"Stab\",\"Sticky\",\"Stripe\"]\nintensityAutoCrescendoEnabled = true\nintensityAutoCrescendoMaximum = 0.8\nintensityAutoCrescendoMinimum = 0.2\nintensityLayers = {\n    Background = 3\n    Bass = 2\n    Drum = 1\n    Hook = 2\n    Pad = 2\n    Percussion = 3\n    Stab = 2\n    Sticky = 2\n    Stripe = 2\n    Transition = 3\n  }\nintensityThreshold = {\n    Background = 0.5\n    Bass = 0.5\n    Drum = 0.5\n    Hook = 0.5\n    Pad = 0.5\n    Percussion = 0.5\n    Stab = 0.5\n    Sticky = 0.5\n    Stripe = 0.5\n    Transition = 0.5\n  }\nmainProgramLengthMaxDelta = 280\nmemeTaxonomy = [\n    {\n      \"memes\":[\"EXPLORATION\",\"DIALOG\",\"COMBAT\"],\n      \"name\":\"ACTION\"\n    },\n    {\n      \"memes\":[\"CALLTOACTION\",\"EMBARK\",\"ATONEMENT\",\"VICTORY\"],\n      \"name\":\"HEROSJOURNEY\"\n    }\n  ]\nmixerCompressAheadSeconds = 0.05\nmixerCompressDecaySeconds = 0.125\nmixerCompressRatioMax = 1.0\nmixerCompressRatioMin = 0.3\nmixerCompressToAmplitude = 1.0\nmixerDspBufferSize = 1024\nmixerHighpassThresholdHz = 30\nmixerLowpassThresholdHz = 15000\nmixerNormalizationBoostThreshold = 1.0\nmixerNormalizationCeiling = 0.999\nstickyBunEnabled = true\n";

  auto subject = TemplateConfig(input);

  EXPECT_EQ(0.0, subject.getChoiceMuteProbability(Instrument::Type::Pad));
  EXPECT_EQ(1.0, subject.getDubMasterVolume(Instrument::Type::Pad));
  EXPECT_EQ(0.5, subject.getIntensityThreshold(Instrument::Type::Pad));
  EXPECT_EQ(3.0, subject.getIntensityLayers(Instrument::Type::Percussion));
  EXPECT_TRUE(subject.instrumentTypesForInversionSeekingContains(Instrument::Type::Pad));
  EXPECT_EQ(1, subject.deltaArcBeatLayersIncoming);
  EXPECT_EQ(1, subject.deltaArcBeatLayersToPrioritize.size());
  EXPECT_EQ(3, subject.deltaArcDetailLayersIncoming);
  EXPECT_FALSE(subject.deltaArcEnabled);
  EXPECT_EQ(5, subject.detailLayerOrder.size());
  EXPECT_EQ(Instrument::Type::Pad, subject.detailLayerOrder[0]);
  EXPECT_EQ(Instrument::Type::Stripe, subject.detailLayerOrder[1]);
  EXPECT_EQ(Instrument::Type::Bass, subject.detailLayerOrder[2]);
  EXPECT_EQ(Instrument::Type::Sticky, subject.detailLayerOrder[3]);
  EXPECT_EQ(Instrument::Type::Stab, subject.detailLayerOrder[4]);
  EXPECT_EQ(4, subject.eventNamesLarge.size());
  EXPECT_EQ(4, subject.eventNamesMedium.size());
  EXPECT_EQ(3, subject.eventNamesSmall.size());
  EXPECT_EQ(5, subject.instrumentTypesForAudioLengthFinalization.size());
  EXPECT_EQ(4, subject.instrumentTypesForInversionSeeking.size());
  EXPECT_TRUE(subject.intensityAutoCrescendoEnabled);
  EXPECT_NEAR(0.8, subject.intensityAutoCrescendoMaximum, 0.1);
  EXPECT_NEAR(0.2, subject.intensityAutoCrescendoMinimum, 0.1);
  EXPECT_EQ(280, subject.mainProgramLengthMaxDelta);
  ASSERT_EQ(2, subject.memeTaxonomy.getCategories().size());
  std::vector<MemeCategory> categories;
  for (const auto &category: subject.memeTaxonomy.getCategories())
    categories.emplace_back(category);
  std::sort(categories.begin(), categories.end(), [](const MemeCategory &a, const MemeCategory &b) {
    return a.getName().compare(b.getName()) < 0;
  });
  EXPECT_EQ("ACTION", categories[0].getName());
  ASSERT_EQ(3, categories[0].getMemes().size());
  EXPECT_EQ("HEROSJOURNEY", categories[1].getName());
  ASSERT_EQ(4, categories[1].getMemes().size());
  EXPECT_NEAR(0.05, subject.mixerCompressAheadSeconds, 0.01);
  EXPECT_NEAR(0.125, subject.mixerCompressDecaySeconds, 0.01);
  EXPECT_NEAR(1.0, subject.mixerCompressRatioMax, 0.01);
  EXPECT_NEAR(0.3, subject.mixerCompressRatioMin, 0.01);
  EXPECT_NEAR(1.0, subject.mixerCompressToAmplitude, 0.01);
  EXPECT_EQ(1024, subject.mixerDspBufferSize);
}