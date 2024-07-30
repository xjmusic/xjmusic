// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROJECT_H
#define XJMUSIC_PROJECT_H

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

  /**
   * Parse a Project from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, Project &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setIfNotNull(json, "name", entity.name);
    EntityUtils::setIfNotNull(json, "platformVersion", entity.platformVersion);
    EntityUtils::setIfNotNull(json, "isDeleted", entity.isDeleted);
    EntityUtils::setIfNotNull(json, "updatedAt", entity.updatedAt);
  }

}// namespace XJ

#endif//XJMUSIC_PROJECT_H
