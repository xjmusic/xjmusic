// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef LIBRARY_H
#define LIBRARY_H

#include <string>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class Library : public Entity {
  public:
    Library() = default;
    UUID projectId;
    std::string name;
    bool isDeleted{false};
    long long updatedAt{};
  };

}// namespace XJ

#endif//LIBRARY_H
