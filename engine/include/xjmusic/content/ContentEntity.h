// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef CONTENT_ENTITY_H
#define CONTENT_ENTITY_H

#include <string>

#include "xjmusic/util/EntityUtils.h"

namespace XJ {

  class ContentEntity {
  public:

    ContentEntity() = default;

    UUID id;

    /**
     * Compare two Segment Choices
     * @param lhs segment choice
     * @param rhs segment choice
     * @return true if lhs < rhs
     */
    friend bool operator<(const ContentEntity &lhs, const ContentEntity &rhs) {
      return lhs.id < rhs.id;
    }

  };

}// namespace XJ

#endif//CONTENT_ENTITY_H
