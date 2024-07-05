// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_TEMPLATE_H
#define XJMUSIC_TEMPLATE_H

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

  /**
   * Parse a Template from a JSON object
   * @param json  input
   * @param entity  output
   */
  void from_json(const json &json, Template &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "projectId", entity.projectId);
    EntityUtils::setIfNotNull(json, "name", entity.name);
    EntityUtils::setIfNotNull(json, "config", entity.config);
    EntityUtils::setIfNotNull(json, "shipKey", entity.shipKey);
    EntityUtils::setIfNotNull(json, "isDeleted", entity.isDeleted);
    EntityUtils::setIfNotNull(json, "updatedAt", entity.updatedAt);
  }

}// namespace XJ

#endif//XJMUSIC_TEMPLATE_H
