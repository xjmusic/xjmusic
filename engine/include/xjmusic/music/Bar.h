// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_MUSIC_BAR_H
#define XJMUSIC_MUSIC_BAR_H

#include <vector>

namespace XJ {

  class Bar {
  private:
    static std::vector<int> FACTORS_TO_TEST;

  public:
    int beats;

    /**
     * Create a Bar with a given number of beats
     * @param beats  number of beats
     */
    explicit Bar(const int &beats);

    /**
     * Create a Bar with a given number of beats
     * @param beats  number of beats
     */
    static Bar of(const int &beats);

    /**
     * Compute the number of beats in a subsection of this Bar
     * @param beats  number of beats in the subsection
     * @return       number of beats in the subsection
     */
    [[nodiscard]] int computeSubsectionBeats(int beats) const;
  };

}// namespace XJ

#endif// XJMUSIC_MUSIC_BAR_H