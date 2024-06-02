// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/content/InstrumentConfig.h"
#include "xjmusic/util/StringUtils.h"

using namespace Content;

TEST(InstrumentConfigTest, SetFromInstrument) {
  Instrument instrument;
  instrument.config = "isMultiphonic = true";

  InstrumentConfig subject(instrument);

  ASSERT_TRUE(subject.isMultiphonic);
}

TEST(InstrumentConfigTest, SetFromInstrument_isAudioSelectionPersistent_false) {
  Instrument instrument;
  instrument.config = "isAudioSelectionPersistent = false";

  InstrumentConfig subject(instrument);

  ASSERT_FALSE(subject.isAudioSelectionPersistent);
}

TEST(InstrumentConfigTest, SetFromDefaults) {
  InstrumentConfig subject;

  ASSERT_FALSE(subject.isMultiphonic);
}

TEST(InstrumentConfigTest, DefaultsToString) {
  InstrumentConfig subject;

  std::vector<std::string> defaultLines = Util::StringUtils::split(InstrumentConfig::getDefaultString(), '\n');
  std::vector<std::string> subjectLines = Util::StringUtils::split(subject.toString(), '\n');

  ASSERT_EQ(defaultLines, subjectLines);
}

TEST(InstrumentConfigTest, IsOneShot) {
  InstrumentConfig subject("isOneShot=true");

  ASSERT_TRUE(subject.isOneShot);
}

TEST(InstrumentConfigTest, OneShotObserveLengthOfEvents) {
  InstrumentConfig subject("oneShotObserveLengthOfEvents=[   bada ,     bIng, b    ooM ]");

  std::vector<std::string> expected = {"BADA", "BING", "BOOM"};
  ASSERT_EQ(expected, subject.oneShotObserveLengthOfEvents);
}

TEST(InstrumentConfigTest, IsOneShotCutoffEnabled) {
  InstrumentConfig subject("");

  ASSERT_TRUE(subject.isOneShotCutoffEnabled);
}

TEST(InstrumentConfigTest, ReleaseMillis) {
  InstrumentConfig subject("");

  ASSERT_EQ(5, subject.releaseMillis);
}