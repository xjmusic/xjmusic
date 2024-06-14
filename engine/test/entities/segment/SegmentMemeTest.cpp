// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include "xjmusic/entities/segment/SegmentMeme.h"

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
  SegmentMeme segmentMeme;
  segmentMeme.id = "id";
  segmentMeme.segmentId = 1;
  segmentMeme.name = "name";

  ASSERT_EQ(1235, segmentMeme.hashCode());
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

  std::set<SegmentMeme> segmentMemes;
  segmentMemes.insert(segmentMeme1);
  segmentMemes.insert(segmentMeme2);

  std::set<std::string> names = SegmentMeme::getNames(segmentMemes);
  ASSERT_EQ(2, names.size());
  ASSERT_TRUE(names.find("name1") != names.end());
  ASSERT_TRUE(names.find("name2") != names.end();
}