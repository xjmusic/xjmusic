// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "TestHelpers.h"

namespace XJ {

  void TestHelpers::assertNote(const std::string &expect, const Note &actual) {
    ASSERT_EQ(Note::of(expect).pitchClass, actual.pitchClass);
    ASSERT_EQ(Note::of(expect).octave, actual.octave);
  }

  long long TestHelpers::currentTimeMillis() {
    auto now = std::chrono::system_clock::now();
    auto duration = now.time_since_epoch();
    return std::chrono::duration_cast<std::chrono::milliseconds>(duration).count();
  }

  std::string TestHelpers::randomUUID() {
    std::stringstream ss;
    ss << std::hex << std::setw(16) << std::setfill('0') << currentTimeMillis();
    ss << "-";
    ss << std::hex << std::setw(16) << std::setfill('0') << RANDOM_UUID_COUNTER++;
    ss << "-";
    ss << std::hex << std::setw(16) << std::setfill('0') << RANDOM_UUID_COUNTER;
    return ss.str();
  }

}