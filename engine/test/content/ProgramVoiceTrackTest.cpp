// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/content/ProgramVoiceTrack.h"

using namespace XJ;

TEST(ProgramVoiceTrackTest, FieldValues) {
  ProgramVoiceTrack subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.programId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.programVoiceId = "2009bf1a-9f4c-483a-8762-d1002a89879b";
  subject.name = "Test Voice";
  subject.order = 7.0f;

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.programId);
  ASSERT_EQ("2009bf1a-9f4c-483a-8762-d1002a89879b", subject.programVoiceId);
  ASSERT_EQ("Test Voice", subject.name);
  ASSERT_EQ(7.0f, subject.order);
}
