// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CRAFT_TRANSITION_CRAFT_H
#define XJMUSIC_CRAFT_TRANSITION_CRAFT_H

#include <set>

#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/Fabricator.h"

namespace XJ {

  /**
   Transition-type Instrument https://github.com/xjmusic/xjmusic/issues/262
   */
  class TransitionCraft : protected Craft {
    std::set<std::string> smallNames;
    std::set<std::string> mediumNames;
    std::set<std::string> largeNames;

  public:
    explicit TransitionCraft(Fabricator *fabricator);

    /**
     perform craft for the current segment
     */
    void doWork();
  };

}// namespace XJ

#endif//XJMUSIC_CRAFT_TRANSITION_CRAFT_H