// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CRAFT_BACKGROUND_CRAFT_H
#define XJMUSIC_CRAFT_BACKGROUND_CRAFT_H

#include "xjmusic/fabricator/Fabricator.h"
#include "xjmusic/fabricator/FabricationWrapper.h"

namespace XJ {

  /**
   Background-type Instrument https://github.com/xjmusic/xjmusic/issues/256
   */
  class BackgroundCraft : FabricationWrapper {
  public:
    explicit BackgroundCraft(Fabricator *fabricator);

    /**
     perform craft for the current segment
     */
    void doWork();
  };

}// namespace XJ

#endif//XJMUSIC_CRAFT_BACKGROUND_CRAFT_H