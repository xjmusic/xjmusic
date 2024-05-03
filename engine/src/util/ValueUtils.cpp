// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <string>
#include <utility>
#include <vector>
#include <algorithm>
#include <cmath>
#include <set>
#include <random>

#include "xjnexus/util/ValueUtils.h"

namespace Util {

  std::regex ValueUtils::isIntegerRgx("[0-9]+");
  float ValueUtils::entityPositionDecimalPlaces = 2.0;
  float ValueUtils::roundPositionMultiplier = std::pow(10.0f, entityPositionDecimalPlaces);
  std::string ValueUtils::K = "k";
  long ValueUtils::MILLIS_PER_SECOND = 1000;
  long ValueUtils::MICROS_PER_MILLI = 1000;
  long ValueUtils::NANOS_PER_MICRO = 1000;
  long ValueUtils::MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;
  long ValueUtils::NANOS_PER_SECOND = NANOS_PER_MICRO * MICROS_PER_SECOND;
  long ValueUtils::SECONDS_PER_MINUTE = 60;
  long ValueUtils::MICROS_PER_MINUTE = SECONDS_PER_MINUTE * MICROS_PER_SECOND;
  long ValueUtils::MINUTES_PER_HOUR = 60;
  long ValueUtils::HOURS_PER_DAY = 24;
  long ValueUtils::SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
  long ValueUtils::SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
  std::random_device ValueUtils::rd;
  std::mt19937 ValueUtils::gen(rd());

  float ValueUtils::eitherOr(float d1, float d2) {
    if (!std::isnan(d1) && d1 != 0.0f)
      return d1;
    else
      return d2;
  }

  std::string ValueUtils::eitherOr(std::string s1, std::string s2) {
    if (!s1.empty())
      return s1;
    else
      return s2;
  }

  std::set<int> ValueUtils::dividedBy(float divisor, const std::set<int> &originals) {
    std::vector<int> result(originals.size());
    std::transform(originals.begin(), originals.end(), result.begin(), [divisor](int original) {
      return static_cast<int>(std::floor(static_cast<float>(original) / divisor));
    });
    return {result.begin(), result.end()};
  }

  float ValueUtils::ratio(float value, float limit) {
    return std::max(std::min(1.0f, value / limit), 0.0f);
  }

  bool ValueUtils::isInteger(const std::string &raw) {
    return std::regex_match(raw, isIntegerRgx);
  }

  float ValueUtils::limitDecimalPrecision(float value) {
    return std::floor(value * roundPositionMultiplier) / roundPositionMultiplier;
  }

  std::string ValueUtils::k(int value) {
    return std::to_string((int) std::floor((float) value / 1000)) + K;
  }

  std::string ValueUtils::randomFrom(std::vector<std::string> from) {
    if (from.empty()) return "";
    std::uniform_int_distribution<> distrib(0, static_cast<int>(from.size()) - 1);
    int randomIndex = distrib(gen);
    return from[randomIndex];
  }

  std::vector<std::string> ValueUtils::randomFrom(std::vector<std::string> from, int num) {
    std::vector<std::string> result;
    std::sample(from.begin(), from.end(), std::back_inserter(result), num, gen);
    return result;
  }

  long ValueUtils::gcd(long a, long b) {
    while (b > 0) {
      long temp = b;
      b = a % b; // % is remainder
      a = temp;
    }
    return a;
  }

  std::vector<int> ValueUtils::factors(long target, std::vector<int> testFactors) {
    std::vector<int> result;
    std::copy_if(testFactors.begin(), testFactors.end(), std::back_inserter(result), [target](int tf) {
      return target % tf == 0;
    });
    return result;
  }

  int ValueUtils::subDiv(int numerator, int denominator) {
    if (numerator % denominator != 0 || numerator <= denominator) return numerator;
    int result = numerator;
    while (result % denominator == 0 && result > denominator)
      result = result / denominator;
    while (result % 2 == 0 && result > denominator * 1.38)
      result = result / 2;
    return result;
  }

  int ValueUtils::multipleFloor(int factor, float value) {
    auto factorF = static_cast<float>(factor);
    return static_cast<int>(std::floor(value / factorF) * factorF);
  }

  float ValueUtils::interpolate(float floor, float ceiling, float position, float multiplier) {
    return floor + (ceiling - floor) * position * multiplier;
  }

  void ValueUtils::enforceMaxStereo(int value) {
    if (value > 2)
      throw std::runtime_error("more than 2 input audio channels not allowed");
  }

  std::optional<UUID> ValueUtils::getKeyOfHighestValue(const std::map<UUID, int> &map) {
    if (map.empty()) return std::nullopt;

    auto max_it = std::max_element(map.begin(), map.end(),
                                   [](const auto &a, const auto &b) {
                                     return a.second < b.second;
                                   });

    return max_it->first;
  }

  int ValueUtils::roundToNearest(int N, int value) {
    return std::max(0, static_cast<int>(std::round(static_cast<float>(value) / static_cast<float>(N)))) * N;
  }

  std::vector<UUID> ValueUtils::withIdsRemoved(std::vector<UUID> fromIds, int count) {
    std::vector<UUID> ids = std::move(fromIds);
    for (int i = 0; i < count; i++) {
      if (!ids.empty()) {
        std::uniform_int_distribution<> distrib(0, static_cast<int>(ids.size()) - 1);
        int randomIndex = distrib(gen);
        ids.erase(ids.begin() + randomIndex);
      }
    }
    return ids;
  }

  std::string ValueUtils::emptyZero(int value) {
    return (0 != value) ? std::to_string(value) : "";
  }

  std::vector<std::string> ValueUtils::last(int num, std::vector<std::string> list) {
    if (num < 1) return {};
    return {list.begin() + std::max(0, static_cast<int>(list.size()) - num), list.end()};
  }


}