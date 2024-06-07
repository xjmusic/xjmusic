// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/entities/content/TemplateConfig.h"

using namespace XJ;

TEST(TemplateConfigTest, SetFromTemplate) {
  Template input;
  input.config = ("mixerCompressToAmplitude = 0.95");

  TemplateConfig subject(input);

  EXPECT_NEAR(0.95, subject.mixerCompressToAmplitude, 0.01);
}

TEST(TemplateConfigTest, SetFromDefaults) {
  TemplateConfig subject;

  EXPECT_NEAR(1.0, subject.mixerCompressToAmplitude, 0.01);
}

TEST(TemplateConfigTest, DefaultsToString) {
  TemplateConfig subject;

  std::vector<std::string> defaultLines = StringUtils::split(TemplateConfig::getDefaultString(), '\n');
  std::vector<std::string> subjectLines = StringUtils::split(subject, '\n');

  for (int i = 0; i < defaultLines.size(); i++) {
    EXPECT_EQ(defaultLines[i], subjectLines[i]) << "Mismatch at line " << i << " of " << defaultLines.size();
  }
}

TEST(TemplateConfigTest, GetChoiceMuteProbability) {
  TemplateConfig subject;

  EXPECT_FLOAT_EQ(0.0f, subject.choiceMuteProbability.at(Instrument::Type::Bass));
}

TEST(TemplateConfigTest, GetDubMasterVolume) {
  TemplateConfig subject;

  EXPECT_FLOAT_EQ(1.0f, subject.dubMasterVolume.at(Instrument::Type::Bass));
}

TEST(TemplateConfigTest, GetIntensityLayers) {
  TemplateConfig subject;

  EXPECT_EQ(1, subject.intensityLayers.at(Instrument::Type::Bass));
  EXPECT_EQ(3, subject.intensityLayers.at(Instrument::Type::Pad));
}