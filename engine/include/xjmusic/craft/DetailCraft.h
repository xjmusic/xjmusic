// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CRAFT_DETAIL_CRAFT_H
#define XJMUSIC_CRAFT_DETAIL_CRAFT_H

#include "xjmusic/fabricator/FabricationWrapper.h"
#include "xjmusic/fabricator/Fabricator.h"

namespace XJ {

  /**
   Structure craft for the current segment includes all kinds of Detail
   [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
   */
  class DetailCraft : FabricationWrapper {
  public:
    explicit DetailCraft(Fabricator *fabricator);

    /**
     Perform Detail craft for the current segment
     <p>
     Workstation fabrication Layering/Intensity
     https://github.com/xjmusic/xjmusic/issues/196
     */
    void doWork();
  };

}// namespace XJ

#endif//XJMUSIC_CRAFT_DETAIL_CRAFT_H
