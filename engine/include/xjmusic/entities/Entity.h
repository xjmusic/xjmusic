// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_ENTITY_H
#define XJMUSIC_ENTITY_H

#include <map>
#include <string>
#include <typeinfo>

#include "nlohmann/json.hpp"

using json = nlohmann::json;

namespace XJ {

  /**
   * Universally Unique Identifier (UUID)
   */
  using UUID = std::string;

  /**
   * Reverse a map
   * @tparam A           original key type -> final value type
   * @tparam B           original value type -> final key type
   * @param originalMap  The original map
   * @return             The reversed map
   */
  template<typename A, typename B>
  static std::map<B, A> reverseMap(const std::map<A, B> &originalMap) {
    std::map<B, A> reverseMap;
    for (const auto &pair: originalMap) {
      reverseMap[pair.second] = pair.first;
    }
    return reverseMap;
  }

  /**
   * Base class for all models
   */
  class Entity {
  public:
    virtual ~Entity() = default;

    /**
     * UUID of the entity
     */
    UUID id;
  };

}// namespace XJ

#endif//XJMUSIC_ENTITY_H
