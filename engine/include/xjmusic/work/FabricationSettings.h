// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_FABRICATION_SETTINGS_H
#define XJMUSIC_WORK_FABRICATION_SETTINGS_H

#include "xjmusic/fabricator/Fabricator.h"
#include "xjmusic/content/Template.h"

namespace XJ {

  class FabricationSettings {
  public:
    Fabricator::ControlMode controlMode;
    Template inputTemplate;
    int craftAheadSeconds;
    int dubAheadSeconds;
    int outputFrameRate;
    int outputChannels;
    int mixerLengthSeconds = 2;
    int shipOutputFileNumberDigits = 7;
    int shipOutputPcmChunkSizeBytes = 1024;
    long persistenceWindowSeconds = 3600;
  };

}// namespace XJ

#endif// XJMUSIC_WORK_FABRICATION_SETTINGS_H
