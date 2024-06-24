// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/content/ProgramVoice.h"

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

TEST(ProgramVoiceTest, GetNames) {
  ProgramVoice voice1;
  voice1.name = "Voice 1";
  ProgramVoice voice2;
  voice2.name = "Voice 2";
  ProgramVoice voice3;
  voice3.name = "Voice 3";

  std::set<const ProgramVoice *> voices = {&voice1, &voice2, &voice3};
  std::set<std::string> names = ProgramVoice::getNames(voices);

  ASSERT_EQ(3, names.size());
  ASSERT_TRUE(names.find("Voice 1") != names.end());
  ASSERT_TRUE(names.find("Voice 2") != names.end());
  ASSERT_TRUE(names.find("Voice 3") != names.end());
}
