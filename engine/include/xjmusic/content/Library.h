// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef LIBRARY_H
#define LIBRARY_H

#include <string>

#include "Entity.h"

namespace Content {

  class Library : public Entity {
  public:
    Library() = default;
    UUID projectId;
    std::string name;
    bool isDeleted{false};
    long long updatedAt{};
  };

}// namespace Content

#endif//LIBRARY_H
