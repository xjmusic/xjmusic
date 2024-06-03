// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROJECT_H
#define PROJECT_H

#include <string>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class Project : public Entity {
  public:
    Project() = default;
    std::string name;
    std::string platformVersion;
    bool isDeleted{false};
    long long updatedAt{currentTimeMillis()};
  };

}// namespace XJ

#endif//PROJECT_H
