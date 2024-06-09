// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "nlohmann/json.hpp"

#include "xjmusic/entities/Entity.h"

using namespace XJ;

template<typename A, typename B>
std::map<B, A> Entity::reverseMap(const std::map<A, B> &originalMap) {
  std::map<B, A> reverseMap;
  for (const auto &pair: originalMap) {
    reverseMap[pair.second] = pair.first;
  }
  return reverseMap;
}

long long Entity::currentTimeMillis() {
  return std::chrono::duration_cast<std::chrono::milliseconds>(
      std::chrono::system_clock::now().time_since_epoch()
  ).count();
}

