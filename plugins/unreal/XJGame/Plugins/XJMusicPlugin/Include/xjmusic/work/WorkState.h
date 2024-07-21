// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_WORK_FABRICATION_STATE_H
#define XJMUSIC_WORK_FABRICATION_STATE_H

#include <string>

namespace XJ {

  enum WorkState {
    Standby,
    Active,
    Done,
    Cancelled,
    Failed,
  };

}// namespace XJ

#endif// XJMUSIC_WORK_FABRICATION_STATE_H