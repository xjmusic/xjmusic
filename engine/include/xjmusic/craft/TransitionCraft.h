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

    /**
     Is this a big-transition segment? (next main or next macro)

     @return true if it is a big transition segment
     */
    bool isBigTransitionSegment() const;

    /**
     Is this a medium-transition segment? (not the same sequence as the previous segment)
     <p>
     Transition craft uses Small (instead of Medium) when a sequence repeats for more than 1 segment https://github.com/xjmusic/xjmusic/issues/264

     @return true if it is a medium transition segment
     */
    bool isMediumTransitionSegment() const;

    /**
     Craft percussion loop

     @param tempo      of main program
     @param instrument of percussion loop instrument to craft
     */
    void craftTransition(double tempo, const Instrument *instrument);

    /**
     Select audios for instrument having the given event names

     @return instrument audios
     */
    std::set<const InstrumentAudio *>
    selectAudiosForInstrument(const Instrument *instrument, std::set<std::string> names);

  };

}// namespace XJ

#endif//XJMUSIC_CRAFT_TRANSITION_CRAFT_H