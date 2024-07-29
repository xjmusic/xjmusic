// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <xjmusic/work/WorkSettings.h>

std::string WorkSettings::toString() const {
  return "WorkSettings: controlMode: " + Fabricator::toString(controlMode) +
         ", craftAheadSeconds: " + std::to_string(craftAheadSeconds) +
         ", dubAheadSeconds: " + std::to_string(dubAheadSeconds) +
         ", deadlineSeconds: " + std::to_string(deadlineSeconds) +
         ", persistenceWindowSeconds: " + std::to_string(persistenceWindowSeconds);
}
