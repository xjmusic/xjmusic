// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/entities/content/ProgramSequencePatternEvent.h"

using namespace XJ;

TEST(ProgramSequencePatternEventTest, FieldValues) {
  ProgramSequencePatternEvent subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.programId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.programSequencePatternId = "2009bf1a-9f4c-483a-8762-d1002a89879b";
  subject.programVoiceTrackId = "fd472031-dd6e-47ae-b0b7-ae4f02b94726";
  subject.velocity = 1.0f;
  subject.position = 2.5f;
  subject.duration = 1.0f;
  subject.tones = "X";

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.programId);
  ASSERT_EQ("2009bf1a-9f4c-483a-8762-d1002a89879b", subject.programSequencePatternId);
  ASSERT_EQ("fd472031-dd6e-47ae-b0b7-ae4f02b94726", subject.programVoiceTrackId);
  ASSERT_EQ(1.0f, subject.velocity);
  ASSERT_EQ(2.5f, subject.position);
  ASSERT_EQ(1.0f, subject.duration);
  ASSERT_EQ("X", subject.tones);
}
