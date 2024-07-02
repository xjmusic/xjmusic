// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <xjmusic/work/WorkSettings.h>

std::string WorkSettings::toString() const {
  return "WorkSettings: controlMode: " + Fabricator::toString(controlMode) +
         ", inputTemplate: " + inputTemplate.name +
         ", craftAheadSeconds: " + std::to_string(craftAheadSeconds) +
         ", dubAheadSeconds: " + std::to_string(dubAheadSeconds) +
         ", persistenceWindowSeconds: " + std::to_string(persistenceWindowSeconds);
}
