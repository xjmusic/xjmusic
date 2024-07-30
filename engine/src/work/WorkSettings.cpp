// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <xjmusic/work/WorkSettings.h>

std::string WorkSettings::toString() const {
  return "WorkSettings: controlMode: " + Fabricator::toString(controlMode) +
         ", craftAheadSeconds: " + std::to_string(ValueUtils::limitDecimalPrecision(craftAheadMicros / ValueUtils::MICROS_PER_SECOND)) +
         ", dubAheadSeconds: " + std::to_string(ValueUtils::limitDecimalPrecision(dubAheadMicros / ValueUtils::MICROS_PER_SECOND)) +
         ", deadlineSeconds: " + std::to_string(ValueUtils::limitDecimalPrecision(deadlineMicros / ValueUtils::MICROS_PER_SECOND)) +
         ", persistenceWindowSeconds: " + std::to_string(persistenceWindowSeconds);
}
