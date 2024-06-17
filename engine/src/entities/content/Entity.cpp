// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <sstream>

#include "xjmusic/entities/Entity.h"

using namespace XJ;

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
std::string XJ::Entity::computeUniqueId() {
  std::stringstream ss;
  ss << std::hex << std::setw(12) << std::setfill('0') << UNIQUE_ID_COUNTER++;
  return ss.str();
}

