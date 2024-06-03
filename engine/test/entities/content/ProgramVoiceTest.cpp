// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/entities/content/ProgramVoice.h"

using namespace XJ;

TEST(ProgramVoiceTest, FieldValues) {
  ProgramVoice subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.programId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.type = Instrument::Type::Drum;
  subject.name = "Drum Voice";
  subject.order = 7.0f;

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.programId);
  ASSERT_EQ(Instrument::Type::Drum, subject.type);
  ASSERT_EQ("Drum Voice", subject.name);
  ASSERT_EQ(7.0f, subject.order);
}
