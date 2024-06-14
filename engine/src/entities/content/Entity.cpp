// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "Entity.h"
#include "TestHelpers.h"
#include <gtest/gtest.h>
#include "xjmusic/entities/Entity.h"
#include "../../../test/_helper/TestHelpers.h"

std::string XJ::Entity::randomUUID() {
  std::stringstream ss;
  ss << std::hex << std::setw(16) << std::setfill('0') << currentTimeMillis();
  ss << "-";
  ss << std::hex << std::setw(16) << std::setfill('0') << RANDOM_UUID_COUNTER++;
  ss << "-";
  ss << std::hex << std::setw(16) << std::setfill('0') << RANDOM_UUID_COUNTER;
  return ss.str();
}