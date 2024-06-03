// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/entities/content/ProgramConfig.h"
#include "xjmusic/util/StringUtils.h"
#include <gtest/gtest.h>

using namespace XJ;

TEST(ProgramConfigTest, SetFromProgram) {
  Program program;
  program.config = "doPatternRestartOnChord = true";

  ProgramConfig subject(program);

  ASSERT_TRUE(subject.doPatternRestartOnChord);
}

TEST(ProgramConfigTest, SetFromDefaults) {
  ProgramConfig subject;

  ASSERT_FALSE(subject.doPatternRestartOnChord);
}

TEST(ProgramConfigTest, GetCutoffMinimumBars) {
  ProgramConfig subject;

  ASSERT_EQ(2, subject.cutoffMinimumBars);
}

TEST(ProgramConfigTest, GetBarBeats) {
  ProgramConfig subject;

  ASSERT_EQ(4, subject.barBeats);
}

TEST(ProgramConfigTest, DefaultsToString) {
  ProgramConfig subject;

  std::vector<std::string> defaultLines = StringUtils::split(ProgramConfig::getDefaultString(), '\n');
  std::vector<std::string> subjectLines = StringUtils::split(subject.toString(), '\n');

  ASSERT_EQ(defaultLines, subjectLines);
}