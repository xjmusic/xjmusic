// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJNEXUS_MUSIC_OCTAVE_H
#define XJNEXUS_MUSIC_OCTAVE_H

#include <regex>

namespace Music {

  /**
   * Octave models a musical octave
   * <p>
   * A perfect octave is the interval between one musical pitch and another with half or double its frequency.
   * @param text note
   * @return octave
   */
  int octaveOf(const std::string& text);

}// namespace Music

#endif// XJNEXUS_MUSIC_OCTAVE_H