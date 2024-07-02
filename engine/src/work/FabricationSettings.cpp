// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <xjmusic/work/FabricationSettings.h>

inline std::string FabricationSettings::toString() const {
  return "FabricationSettings: controlMode: " + Fabricator::toString(controlMode) +
         ", inputTemplate: " + inputTemplate.name +
         ", craftAheadSeconds: " + std::to_string(craftAheadSeconds) +
         ", dubAheadSeconds: " + std::to_string(dubAheadSeconds) +
         ", outputFrameRate: " + std::to_string(outputFrameRate) +
         ", outputChannels: " + std::to_string(outputChannels) +
         ", mixerLengthSeconds: " + std::to_string(mixerLengthSeconds) +
         ", shipOutputFileNumberDigits: " + std::to_string(shipOutputFileNumberDigits) +
         ", shipOutputPcmChunkSizeBytes: " + std::to_string(shipOutputPcmChunkSizeBytes) +
         ", persistenceWindowSeconds: " + std::to_string(persistenceWindowSeconds);
}
