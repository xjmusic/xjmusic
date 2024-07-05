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
