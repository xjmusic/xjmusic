// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROJECT_H
#define PROJECT_H

#include <string>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class Project : public ContentEntity {
  public:

    Project() = default;

    std::string name;
    std::string platformVersion;
    bool isDeleted{false};
    long long updatedAt{EntityUtils::currentTimeMillis()};
  };

}// namespace XJ

#endif//PROJECT_H
