// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_MUSIC_OCTAVE_H
#define XJMUSIC_MUSIC_OCTAVE_H

#include <regex>

namespace XJ {

  /**
   * Octave models a musical octave
   * <p>
   * A perfect octave is the interval between one musical pitch and another with half or double its frequency.
   * @param text note
   * @return octave
   */
  int octaveOf(const std::string& text);

}// namespace XJ

#endif// XJMUSIC_MUSIC_OCTAVE_H