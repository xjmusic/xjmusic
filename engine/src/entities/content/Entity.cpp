// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/entities/Entity.h"

using namespace XJ;

/**
 * This is in fact not a UUID at all, not even random.
 * But since the domain of this application is entirely local, we do not require globally-unique randomness.
 * Instead, for performance reasons, we use a simple counter, which provides guaranteed locally unique identifiers
 * @return locally unique identifier
 */
std::string XJ::Entity::randomUUID() {
  std::stringstream ss;
  ss << std::hex << std::setw(16) << std::setfill('0') << currentTimeMillis();
  ss << "-";
  ss << std::hex << std::setw(16) << std::setfill('0') << UNIQUE_ID_COUNTER++;
  ss << "-";
  ss << std::hex << std::setw(16) << std::setfill('0') << UNIQUE_ID_COUNTER;
  return ss.str();
}

