// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/content/ProgramSequenceChordVoicing.h"

using namespace XJ;

TEST(ProgramSequenceChordVoicingTest, FieldValues) {
  ProgramSequenceChordVoicing subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.programId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.programSequenceChordId = "2009bf1a-9f4c-483a-8762-d1002a89879b";
  subject.programVoiceId = "fd472031-dd6e-47ae-b0b7-ae4f02b94726";
  subject.notes = "C5, E5, G5, Bb5, D6";

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.programId);
  ASSERT_EQ("2009bf1a-9f4c-483a-8762-d1002a89879b", subject.programSequenceChordId);
  ASSERT_EQ("fd472031-dd6e-47ae-b0b7-ae4f02b94726", subject.programVoiceId);
  ASSERT_EQ("C5, E5, G5, Bb5, D6", subject.notes);
}
