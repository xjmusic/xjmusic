// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/content/InstrumentMeme.h"

using namespace XJ;

TEST(InstrumentMemeTest, FieldValues) {
  InstrumentMeme subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.instrumentId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.name = "Test Meme";

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.instrumentId);
  ASSERT_EQ("Test Meme", subject.name);
}


TEST(InstrumentMemeTest, GetNames) {
  std::set<const InstrumentMeme *> instrumentMemes;
  InstrumentMeme instrumentMeme1;
  instrumentMeme1.name = "Test Meme 1";
  instrumentMemes.insert(&instrumentMeme1);
  InstrumentMeme instrumentMeme2;
  instrumentMeme2.name = "Test Meme 2";
  instrumentMemes.insert(&instrumentMeme2);

  std::set<std::string> names = InstrumentMeme::getNames(instrumentMemes);

  ASSERT_EQ(2, names.size());
  ASSERT_TRUE(names.find("Test Meme 1") != names.end());
  ASSERT_TRUE(names.find("Test Meme 2") != names.end());
}