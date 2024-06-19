// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef CONTENT_ENTITY_H
#define CONTENT_ENTITY_H

#include <string>

#include "nlohmann/json.hpp"

#include "xjmusic/util/EntityUtils.h"

using json = nlohmann::json;

namespace XJ {

  class ContentEntity : public EntityUtils {
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