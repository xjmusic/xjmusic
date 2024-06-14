// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_MUSIC_PITCH_CLASS_H
#define XJMUSIC_MUSIC_PITCH_CLASS_H

#include <map>
#include <string>

#include "xjmusic/util/StringUtils.h"

#include "Accidental.h"

namespace XJ {

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

  static std::regex pitchClassAtBeginning("^([A-G][#b]*)");

  static std::map<std::string, PitchClass> pitchClassOfString = {
      {"X", Atonal},
      {"C", C},
      {"Cb", B},
      {"C#", Cs},
      {"D", D},
      {"D#", Ds},
      {"Db", Cs},
      {"E", E},
      {"E#", F},
      {"Eb", Ds},
      {"F", F},
      {"F#", Fs},
      {"Fb", E},
      {"G", G},
      {"G#", Gs},
      {"Gb", Fs},
      {"A", A},
      {"A#", As},
      {"Ab", Gs},
      {"B", B},
      {"B#", C},
      {"Bb", As},
  };


  /**
   * Pitch Class based on the first character of the text
   *
   * @param text to get pitch class from
   * @return pitch class
   */
  static PitchClass pitchClassOf(const std::string& name) {
    if (name.empty())
      return Atonal;
    std::string normalized = accidentalNormalized(name);
    std::string atBeginning = StringUtils::match(pitchClassAtBeginning, normalized).value_or("");
    if (pitchClassOfString.find(atBeginning) != pitchClassOfString.end())
      return pitchClassOfString[atBeginning];
    return Atonal;
  }


  /**
   * std::string of Pitch class, with adjustment symbol
   *
   * @param from pitch class
   * @param with adjustment symbol
   * @return string of pitch class
   */
  static std::string stringOf(PitchClass from, Accidental with) {
    switch (from) {
      case C:
        return "C";
      case D:
        return "D";
      case E:
        return "E";
      case F:
        return "F";
      case G:
        return "G";
      case A:
        return "A";
      case B:
        return "B";

      default:
        switch (with) {
          case Sharp:
          case Natural:
            switch (from) {
              case Cs:
                return "C#";
              case Ds:
                return "D#";
              case Fs:
                return "F#";
              case Gs:
                return "G#";
              case As:
                return "A#";
              default:
                return "X";
            }

          case Flat:
            switch (from) {
              case Cs:
                return "Db";
              case Ds:
                return "Eb";
              case Fs:
                return "Gb";
              case Gs:
                return "Ab";
              case As:
                return "Bb";
              default:
                return "X";
            }
        }
    }
    return "X";
  }

}// namespace XJ

#endif//XJMUSIC_MUSIC_PITCH_CLASS_H