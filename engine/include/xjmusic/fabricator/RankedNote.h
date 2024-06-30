// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJ_MUSIC_FABRICATOR_RANKED_NOTE_H
#define XJ_MUSIC_FABRICATOR_RANKED_NOTE_H

#include "xjmusic/music/Note.h"

namespace XJ {

/**
 Rank a Note based on its delta
 <p>
 */
  class RankedNote {
    Note note;
    int delta;

  public:

    /**
     * Construct a Ranked Note
     * @param note   to rank
     * @param delta  delta
     */
    RankedNote(
        Note note,
        int delta
    );

    /**
     * Get the tones of this note
     * @return  the tones
     */
    Note getTones() const;

    /**
     * Get the delta of this note
     * @return  the delta
     */
    int getDelta();

  };

}// namespace XJ

#endif //XJ_MUSIC_FABRICATOR_RANKED_NOTE_H