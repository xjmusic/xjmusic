// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/content/Library.h"

using namespace Content;

TEST(LibraryTest, FieldValues) {
  Library subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.name = "Test Library";
  subject.projectId = "0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9";
  subject.isDeleted = false;
  subject.updatedAt = 1711089919558;

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("Test Library", subject.name);
  ASSERT_EQ("0c39f908-5eb3-4c94-a8c6-ac87ec02f7e9", subject.projectId);
  ASSERT_EQ(false, subject.isDeleted);
  ASSERT_EQ(1711089919558, subject.updatedAt);
}
