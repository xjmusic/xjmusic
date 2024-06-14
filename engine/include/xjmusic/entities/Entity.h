// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_ENTITY_H
#define XJMUSIC_ENTITY_H

#include <map>
#include <string>
#include <typeinfo>
#include <random>

#include "nlohmann/json.hpp"
using json = nlohmann::json;

#include "xjmusic/entities/music/Root.h"
#include "xjmusic/entities/music/PitchClass.h"
#include "xjmusic/entities/music/Note.h"

namespace XJ {

  /**
   * This is in fact not a UUID at all, not even random.
   * But since the domain of this application is entirely local, we do not require globally-unique randomness.
   * Instead, for performance reasons, we use a simple counter, which provides guaranteed locally unique identifiers
   * @return locally unique identifier
   */
  using UUID = std::string;
  static unsigned long long UNIQUE_ID_COUNTER = 0;

  /**
   * Base class for all models
   */
  class Entity {
  public:

    /**
     * Default constructor
     */
    virtual ~Entity() = default;

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
    static std::string randomUUID();
  };

}// namespace XJ

#endif//XJMUSIC_ENTITY_H
