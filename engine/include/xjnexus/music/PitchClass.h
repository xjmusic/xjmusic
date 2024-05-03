// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJNEXUS_MUSIC_PITCH_CLASS_H
#define XJNEXUS_MUSIC_PITCH_CLASS_H

#include <map>
#include <string>

#include "Accidental.h"

namespace Music {

  /**
   * PitchClass of pitch for a note (across all octaves)
   * In music, a pitch class is a set of all pitches that are a whole number of octaves apart, e.g., the pitch class C consists of the Cs in all octaves.
   */
  enum PitchClass {
    Atonal,
    C,
    Cs,
    D,
    Ds,
    E,
    F,
    Fs,
    G,
    Gs,
    A,
    As,
    B
  };

  /**
   * Pitch Class based on the first character of the text
   *
   * @param text to get pitch class from
   * @return pitch class
   */
  PitchClass pitchClassOf(const std::string &name);

  /**
   * std::string of Pitch class, with adjustment symbol
   *
   * @param from pitch class
   * @param with adjustment symbol
   * @return string of pitch class
   */
  std::string stringOf(PitchClass from, Accidental with = Sharp);

}// namespace Music

#endif//XJNEXUS_MUSIC_PITCH_CLASS_H