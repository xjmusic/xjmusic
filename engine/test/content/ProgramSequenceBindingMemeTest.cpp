// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/content/ProgramSequenceBindingMeme.h"

using namespace XJ;

TEST(ProgramSequenceBindingMemeTest, FieldValues) {
  ProgramSequenceBindingMeme subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.programId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.programSequenceBindingId = "2009bf1a-9f4c-483a-8762-d1002a89879b";
  subject.name = "oranges";

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.programId);
  ASSERT_EQ("2009bf1a-9f4c-483a-8762-d1002a89879b", subject.programSequenceBindingId);
  ASSERT_EQ("oranges", subject.name);
}

TEST(ProgramSequenceBindingMeme, GetNames) {
  std::set<ProgramSequenceBindingMeme> programSequenceBindingMemes;
  ProgramSequenceBindingMeme programSequenceBindingMeme1;
  programSequenceBindingMeme1.name = "apples";
  programSequenceBindingMemes.insert(programSequenceBindingMeme1);
  ProgramSequenceBindingMeme programSequenceBindingMeme2;
  programSequenceBindingMeme2.name = "bananas";
  programSequenceBindingMemes.insert(programSequenceBindingMeme2);

  std::set<std::string> names = ProgramSequenceBindingMeme::getNames(programSequenceBindingMemes);

  ASSERT_EQ(2, names.size());
  ASSERT_TRUE(names.find("apples") != names.end());
  ASSERT_TRUE(names.find("bananas") != names.end());
}