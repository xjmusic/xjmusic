// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjnexus/content/InstrumentAudio.h"

using namespace Content;

TEST(InstrumentAudioTest, FieldValues) {
  InstrumentAudio subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.instrumentId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.name = "Test Audio";
  subject.waveformKey = "test-audio.wav";
  subject.transientSeconds = 0.057f;
  subject.loopBeats = 4;
  subject.tempo = 120.0f;
  subject.intensity = 1.0f;
  subject.event = "X";
  subject.volume = 0.9f;

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.instrumentId);
  ASSERT_EQ("Test Audio", subject.name);
  ASSERT_EQ("test-audio.wav", subject.waveformKey);
  ASSERT_EQ(0.057f, subject.transientSeconds);
  ASSERT_EQ(4, subject.loopBeats);
  ASSERT_EQ(120.0f, subject.tempo);
  ASSERT_EQ(1.0f, subject.intensity);
  ASSERT_EQ("X", subject.event);
  ASSERT_EQ(0.9f, subject.volume);
}
