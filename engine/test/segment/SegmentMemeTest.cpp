// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include "xjmusic/segment/SegmentMeme.h"

using namespace XJ;

TEST(SegmentMemeTest, testEquals) {
  SegmentMeme segmentMeme1;
  segmentMeme1.id = "id";
  segmentMeme1.segmentId = 1;
  segmentMeme1.name = "name";

  SegmentMeme segmentMeme2;
  segmentMeme2.id = "id";
  segmentMeme2.segmentId = 1;
  segmentMeme2.name = "name";

  ASSERT_TRUE(segmentMeme1.equals(segmentMeme2));
}

TEST(SegmentMemeTest, testHashCode) {
  SegmentMeme baselineInput;
  baselineInput.id = "id";
  baselineInput.segmentId = 1;
  baselineInput.name = "name";
  unsigned long long baselineOutput = baselineInput.hashCode();

  SegmentMeme testInputSame;
  testInputSame.id = "id";
  testInputSame.segmentId = 1;
  testInputSame.name = "name";
  ASSERT_EQ(baselineOutput, testInputSame.hashCode());

  SegmentMeme testInputDifferentId;
  testInputDifferentId.id = "id2";
  testInputDifferentId.segmentId = 1;
  testInputDifferentId.name = "name";
  ASSERT_NE(baselineOutput, testInputDifferentId.hashCode());

  SegmentMeme testInputDifferentSegmentId;
  testInputDifferentSegmentId.id = "id";
  testInputDifferentSegmentId.segmentId = 2;
  testInputDifferentSegmentId.name = "name";
  ASSERT_NE(baselineOutput, testInputDifferentSegmentId.hashCode());

  SegmentMeme testInputDifferentName;
  testInputDifferentName.id = "id";
  testInputDifferentName.segmentId = 1;
  testInputDifferentName.name = "name2";
  ASSERT_NE(baselineOutput, testInputDifferentName.hashCode());
}

TEST(SegmentMemeTest, testGetNames) {
  SegmentMeme segmentMeme1;
  segmentMeme1.id = "id1";
  segmentMeme1.segmentId = 1;
  segmentMeme1.name = "name1";

  SegmentMeme segmentMeme2;
  segmentMeme2.id = "id2";
  segmentMeme2.segmentId = 2;
  segmentMeme2.name = "name2";

  std::set<const SegmentMeme*> segmentMemes;
  segmentMemes.insert(&segmentMeme1);
  segmentMemes.insert(&segmentMeme2);

  std::set<std::string> names = SegmentMeme::getNames(segmentMemes);
  ASSERT_EQ(2, names.size());
  ASSERT_TRUE(names.find("name1") != names.end());
  ASSERT_TRUE(names.find("name2") != names.end());
}