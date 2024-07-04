// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_LOREM_IPSUM_H
#define XJMUSIC_LOREM_IPSUM_H

#include <chrono>
#include <iomanip>
#include <random>
#include <sstream>

#include "xjmusic/content/ContentEntityStore.h"

namespace XJ {

  class LoremIpsum {
  public:
    /**
     List of colors
     */
    static const std::vector<std::string> COLORS;

    /**
     List of variants
     */

    static const std::vector<std::string> VARIANTS;

    /**
     List of variants
     */
    static const std::vector<std::string> PERCUSSIVE_NAMES;

    /**
     List of musical keys
     */
    static const std::vector<std::string> MUSICAL_CHORDS;

    /**
     List of musical keys
     */
    static const std::vector<std::string> MUSICAL_KEYS;

    /**
     List of possible Sequence totals
     */
    static const std::vector<int> SEQUENCE_TOTALS;

    /**
     List of possible Pattern totals
     */
    static const std::vector<int> PATTERN_TOTALS;

    /**
     List of elements
     */
    static const std::vector<std::string> ELEMENTS;
  };

} // namespace XJ

#endif//XJMUSIC_LOREM_IPSUM_H
