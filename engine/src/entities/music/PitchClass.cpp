// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <regex>

#include "xjmusic/entities/music/PitchClass.h"
#include "xjmusic/util/StringUtils.h"

namespace XJ {

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

  PitchClass pitchClassOf(const std::string name) {
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
  std::string stringOf(PitchClass from, Accidental with) {
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