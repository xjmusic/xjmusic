// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef LIBRARY_H
#define LIBRARY_H

#include <string>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class Library : public ContentEntity {
  public:

    Library() = default;

    UUID projectId;
    std::string name;
    bool isDeleted{false};
    long long updatedAt{EntityUtils::currentTimeMillis()};
  };

}// namespace XJ

#endif//LIBRARY_H
