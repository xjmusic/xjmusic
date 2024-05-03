// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <stdexcept>

#include "xjnexus/music/Bar.h"
#include "xjnexus/util/ValueUtils.h"

using namespace Util;

namespace Music {

  std::vector<int> Bar::FACTORS_TO_TEST = {3, 4, 5, 7};

  Bar::Bar(const int &beats) : beats(beats) {
    if (0 >= beats) throw std::runtime_error("Bar must beats greater than zero!");
  }

  Bar Bar::of(const int &beats) {
    return Bar(beats);
  }

  int Bar::computeSubsectionBeats(int subBeats) const {
    auto subDiv = ValueUtils::subDiv(subBeats, beats);
    auto factors = ValueUtils::factors(subBeats, FACTORS_TO_TEST);
    auto minFactor = factors.empty() ? 1 : *std::min_element(factors.begin(), factors.end());
    return std::min(subBeats, std::max(this->beats * minFactor, this->beats * subDiv));
  }

}// namespace Music