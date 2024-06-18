// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_ENTITYUTILS_H
#define XJMUSIC_ENTITYUTILS_H

#include <map>
#include <string>
#include <typeinfo>
#include <random>

#include "nlohmann/json.hpp"
using json = nlohmann::json;

#include "xjmusic/music/Root.h"
#include "xjmusic/music/PitchClass.h"
#include "xjmusic/music/Note.h"

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
     * Get the current time in milliseconds
     * @return  The current time in milliseconds
     */
    static long long currentTimeMillis() {
      return std::chrono::duration_cast<std::chrono::milliseconds>(
          std::chrono::system_clock::now().time_since_epoch()
      ).count();
    }

    /**
       * Generate a random UUID
       */
    static std::string computeUniqueId();
  };

}// namespace XJ

#endif//XJMUSIC_ENTITYUTILS_H
