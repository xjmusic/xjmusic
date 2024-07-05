// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_LIBRARY_H
#define XJMUSIC_LIBRARY_H

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

  /**
   * Parse a Library from a JSON object
   * @param json  input
   * @param entity  output
   */
  void from_json(const json &json, Library &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "projectId", entity.projectId);
    EntityUtils::setIfNotNull(json, "name", entity.name);
    EntityUtils::setIfNotNull(json, "isDeleted", entity.isDeleted);
    EntityUtils::setIfNotNull(json, "updatedAt", entity.updatedAt);
  }

}// namespace XJ

#endif//XJMUSIC_LIBRARY_H
