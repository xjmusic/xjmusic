// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/content/ProgramConfig.h"
#include "xjmusic/util/StringUtils.h"
#include <gtest/gtest.h>

using namespace XJ;

TEST(ProgramConfigTest, SetFromProgram) {
  Program program;
  program.config = "doPatternRestartOnChord = true";

  const ProgramConfig subject(program);

  ASSERT_TRUE(subject.doPatternRestartOnChord);
}

TEST(ProgramConfigTest, SetFromDefaults) {
  const ProgramConfig subject;

  ASSERT_FALSE(subject.doPatternRestartOnChord);
}

TEST(ProgramConfigTest, GetCutoffMinimumBars) {
  const ProgramConfig subject;

  ASSERT_EQ(2, subject.cutoffMinimumBars);
}

TEST(ProgramConfigTest, GetBarBeats) {
  const ProgramConfig subject;

  ASSERT_EQ(4, subject.barBeats);
}

TEST(ProgramConfigTest, DefaultsToString) {
  const ProgramConfig subject;

  const std::vector<std::string> defaultLines = StringUtils::split(ProgramConfig::getDefaultString(), '\n');
  const std::vector<std::string> subjectLines = StringUtils::split(subject.toString(), '\n');

  ASSERT_EQ(defaultLines, subjectLines);
}