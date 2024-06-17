// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/entities/content/Project.h"

using namespace XJ;

TEST(ProjectTest, FieldValues) {
  Project subject;

  subject.id = "7ec2d282-d481-4fee-b57b-082e90284102";
  subject.name = "Test Project";
  subject.platformVersion = "1.7.33";
  subject.isDeleted = false;
  subject.updatedAt = 1711089919558;

  ASSERT_EQ("7ec2d282-d481-4fee-b57b-082e90284102", subject.id);
  ASSERT_EQ("Test Project", subject.name);
  ASSERT_EQ("1.7.33", subject.platformVersion);
  ASSERT_EQ(false, subject.isDeleted);
  ASSERT_EQ(1711089919558, subject.updatedAt);
}
