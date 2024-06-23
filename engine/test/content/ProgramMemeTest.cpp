// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/content/ProgramMeme.h"

using namespace XJ;

TEST(ProgramMemeTest, FieldValues) {
  ProgramMeme subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.programId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.name = "Test Meme";

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.programId);
  ASSERT_EQ("Test Meme", subject.name);
}


TEST(ProgramMemeTest, GetNames) {
  std::set<const ProgramMeme *> programMemes;
  ProgramMeme programMeme1;
  programMeme1.name = "Test Meme 1";
  programMemes.emplace(programMeme1);
  ProgramMeme programMeme2;
  programMeme2.name = "Test Meme 2";
  programMemes.emplace(programMeme2);

  std::set<std::string> names = ProgramMeme::getNames(programMemes);

  ASSERT_EQ(2, names.size());
  ASSERT_TRUE(names.find("Test Meme 1") != names.end());
  ASSERT_TRUE(names.find("Test Meme 2") != names.end());
}