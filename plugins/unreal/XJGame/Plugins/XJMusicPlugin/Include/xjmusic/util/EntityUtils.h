// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_ENTITY_UTILS_H
#define XJMUSIC_ENTITY_UTILS_H

#include <map>
#include <string>
#include <chrono>

#include <nlohmann/json.hpp>

#include "xjmusic/music/Note.h"

using json = nlohmann::json;

namespace XJ {

  /**
 * XJ legacy application used UUIDs because networked data was a possibility.
 * But since the domain of this application is now entirely local, we do not require globally-unique randomness.
 * Instead, we use a simple counter, which provides guaranteed locally unique identifiers
   * <p>
   * In the future, all entity IDs should be simple integers-- but that's a massive refactoring job
   * See: https://github.com/xjmusic/xjmusic/issues/400
   * <p>
   * @return locally unique identifier
   */
  using UUID = std::string;
  static unsigned long long UNIQUE_ID_COUNTER = 0;

  /**
   * Base class for all models
   */
  class EntityUtils {
  public:

    /**
     * Default constructor
     */
    virtual ~EntityUtils() = default;

    /**
     * Reverse a map
     * @tparam A           original key type -> value type
     * @tparam B           original value type -> key type
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
     * Get the current time in milliseconds
     * @return  The current time in milliseconds
     */
    static long long currentTimeMillis() {
      return std::chrono::duration_cast<std::chrono::milliseconds>(
          std::chrono::system_clock::now().time_since_epoch()
      ).count();
    }

    /**
     * XJ legacy application used UUIDs because networked data was a possibility.
     * But since the domain of this application is now entirely local, we do not require globally-unique randomness.
     * Instead, we use a simple counter, which provides guaranteed locally unique identifiers
     * <p>
     * In the future, all entity IDs should be simple integers-- but that's a massive refactoring job
     * See: https://github.com/xjmusic/xjmusic/issues/400
     * <p>
     * @return locally unique identifier
     */
    static std::string computeUniqueId();

    /**
     * Set a required field on an entity from a JSON object
     * @param json  to source
     * @param key  to set
     * @param value  to set
     */
    static void setRequired(const json &json, const std::string &key, UUID &value);

    /**
     * Set an optional string field on an entity from a JSON object, if the value is not null
     * @param json  to source
     * @param key  to set
     * @param value  to set
     */
    static void setIfNotNull(const json &json, const std::string &key, std::string &value);

    /**
     * Set an optional float on an entity from a JSON object, if the value is not null
     * @param json  to source
     * @param key  to set
     * @param value  to set
     */
    static void setIfNotNull(const json &json, const std::string &key, float &value);

    /**
     * Set an optional boolean field on an entity from a JSON object, if the value is not null
     * @param json  to source
     * @param key  to set
     * @param value  to set
     */
    static void setIfNotNull(const json &json, const std::string &key, bool &value);

    /**
     * Set an optional integer field on an entity from a JSON object, if the value is not null
     * @param json  to source
     * @param key  to set
     * @param value  to set
     */
    static void setIfNotNull(const json &json, const std::string &key, int &value);

    /**
     * Set an optional long integer field on an entity from a JSON object, if the value is not null
     * @param json  to source
     * @param key  to set
     * @param value  to set
     */
    static void setIfNotNull(const json &json, const std::string &key, long long &value);
  };

}// namespace XJ

#endif//XJMUSIC_ENTITY_UTILS_H
