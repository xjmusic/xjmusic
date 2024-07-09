// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/content/InstrumentConfig.h"
#include "xjmusic/util/StringUtils.h"

using namespace XJ;

TEST(InstrumentConfigTest, SetFromDefaults) {
  const InstrumentConfig subject;

  ASSERT_FALSE(subject.isMultiphonic);
}

TEST(InstrumentConfigTest, DefaultsToString) {
  const InstrumentConfig subject;

  const std::vector<std::string> defaultLines = StringUtils::split(InstrumentConfig::DEFAULT, '\n');
  const std::vector<std::string> subjectLines = StringUtils::split(subject.toString(), '\n');

  ASSERT_EQ(defaultLines, subjectLines);
}

TEST(InstrumentConfigTest, IsOneShot) {
  const InstrumentConfig subject("isOneShot=true");

  ASSERT_TRUE(subject.isOneShot);
}

TEST(InstrumentConfigTest, OneShotObserveLengthOfEvents) {
  const InstrumentConfig subject("oneShotObserveLengthOfEvents=[   bada ,     bIng, b    ooM ]");

  const std::vector<std::string> expected = {"BADA", "BING", "BOOM"};
  ASSERT_EQ(expected, subject.oneShotObserveLengthOfEvents);
}

TEST(InstrumentConfigTest, IsOneShotCutoffEnabled) {
  const InstrumentConfig subject("");

  ASSERT_TRUE(subject.isOneShotCutoffEnabled);
}

TEST(InstrumentConfigTest, ReleaseMillis) {
  const InstrumentConfig subject("");

  ASSERT_EQ(5, subject.releaseMillis);
}

TEST(InstrumentConfigTest, IsTonal) {
  const InstrumentConfig subject("isTonal=true");

  ASSERT_TRUE(subject.isTonal);
}

TEST(InstrumentConfigTest, OneShotObserveLengthOfEventsContains) {
  const InstrumentConfig subject("oneShotObserveLengthOfEvents=[   bada ,     bIng, b    ooM ]");

  ASSERT_TRUE(subject.oneShotObserveLengthOfEventsContains("BADA"));
  ASSERT_TRUE(subject.oneShotObserveLengthOfEventsContains("BING"));
  ASSERT_TRUE(subject.oneShotObserveLengthOfEventsContains("BOOM"));
  ASSERT_FALSE(subject.oneShotObserveLengthOfEventsContains("BEEP"));
}