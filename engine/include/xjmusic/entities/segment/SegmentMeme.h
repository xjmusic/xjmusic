// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_MEME_H
#define SEGMENT_MEME_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class SegmentMeme : public Entity {
  public:

    SegmentMeme() = default;

    UUID id;
    int segmentId{};
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
     * Compare two Segment Memes
     * @param lhs segment meme
     * @param rhs segment meme
     * @return true if lhs < rhs
     */
    friend bool operator<(const SegmentMeme &lhs, const SegmentMeme &rhs) {
      return lhs.id < rhs.id;
    }
  };

}// namespace XJ

#endif//SEGMENT_MEME_H
