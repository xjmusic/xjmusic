// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_FABRICATION_STATE_H
#define XJMUSIC_WORK_FABRICATION_STATE_H

namespace XJ {

  enum FabricationState {
    Standby,
    Starting,
    PreparingAudio,
    PreparedAudio,
    Initializing,
    Active,
    Done,
    Cancelled,
    Failed,
  };

}// namespace XJ

#endif// XJMUSIC_WORK_FABRICATION_STATE_H