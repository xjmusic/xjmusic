// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/content/ProgramSequence.h"

using namespace XJ;

TEST(ProgramSequenceTest, FieldValues) {
  ProgramSequence subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.programId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.name = "Test Sequence";
  subject.key = "C minor";
  subject.intensity = 0.8f;
  subject.total = 16;

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.programId);
  ASSERT_EQ("Test Sequence", subject.name);
  ASSERT_EQ("C minor", subject.key);
  ASSERT_EQ(0.8f, subject.intensity);
  ASSERT_EQ(16, subject.total);
}
