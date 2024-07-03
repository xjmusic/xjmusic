// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef TEMPLATE_H
#define TEMPLATE_H

#include <string>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class Template : public ContentEntity {
  public:

    Template() = default;

    UUID projectId;
    std::string name;
    std::string config;
    std::string shipKey;
    bool isDeleted{false};
    long long updatedAt{EntityUtils::currentTimeMillis()};
  };

}// namespace XJ

#endif//TEMPLATE_H
