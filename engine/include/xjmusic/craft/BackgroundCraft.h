// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CRAFT_BACKGROUND_CRAFT_H
#define XJMUSIC_CRAFT_BACKGROUND_CRAFT_H

#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/Fabricator.h"

namespace XJ {

  /**
   Background-type Instrument https://github.com/xjmusic/xjmusic/issues/256
   */
  class BackgroundCraft : protected Craft {

    /**
     Craft percussion loop

     @param instrument for which to craft
     */
    void craftBackground(const Instrument *instrument);

  public:
    explicit BackgroundCraft(Fabricator *fabricator);

    /**
     perform craft for the current segment
     */
    void doWork();
  };

}// namespace XJ

#endif//XJMUSIC_CRAFT_BACKGROUND_CRAFT_H