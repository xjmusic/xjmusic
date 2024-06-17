// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_MEME_H
#define SEGMENT_MEME_H

#include <string>
#include <utility>
#include <set>

#include "xjmusic/entities/Entity.h"
#include "SegmentEntity.h"

namespace XJ {

  class SegmentMeme : public SegmentEntity {
  public:

    SegmentMeme() = default;

    std::string name;

    /**
     * Assert equality with another Segment Meme
     * @param segmentMeme  The Segment Meme to compare
     * @return       true if equal
     */
    [[nodiscard]] bool equals(const SegmentMeme &segmentMeme) const;

    /**
     * Determine a unique hash code for the Segment Meme
     * @return       hash code
     */
    [[nodiscard]] unsigned long long hashCode() const;

    /**
     * Get the names of a set of Segment Memes
     * @param segmentMemes  The set of Segment Memes
     * @return       The names
     */
    static std::set<std::string> getNames(const std::set<SegmentMeme> &segmentMemes);

  };

}// namespace XJ

#endif//SEGMENT_MEME_H
