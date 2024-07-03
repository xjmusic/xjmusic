// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <string>
#include <utility>
#include <vector>
#include <algorithm>
#include <cmath>
#include <set>
#include <random>

#include "xjmusic/util/ValueUtils.h"

using namespace XJ;


std::regex ValueUtils::isIntegerRgx("[0-9]+");
float ValueUtils::entityPositionDecimalPlaces = 2.0;
float ValueUtils::roundPositionMultiplier = std::pow(10.0f, entityPositionDecimalPlaces);
std::string ValueUtils::K = "k";
long ValueUtils::MILLIS_PER_SECOND = 1000;
long ValueUtils::MICROS_PER_MILLI = 1000;
long ValueUtils::NANOS_PER_MICRO = 1000;
long ValueUtils::MICROS_PER_SECOND = MICROS_PER_MILLI * MILLIS_PER_SECOND;
float ValueUtils::MICROS_PER_SECOND_FLOAT = static_cast<float>(MICROS_PER_SECOND);
long ValueUtils::NANOS_PER_SECOND = NANOS_PER_MICRO * MICROS_PER_SECOND;
long ValueUtils::SECONDS_PER_MINUTE = 60;
long ValueUtils::MICROS_PER_MINUTE = SECONDS_PER_MINUTE * MICROS_PER_SECOND;
long ValueUtils::MINUTES_PER_HOUR = 60;
long ValueUtils::HOURS_PER_DAY = 24;
long ValueUtils::SECONDS_PER_HOUR = SECONDS_PER_MINUTE * MINUTES_PER_HOUR;
long ValueUtils::SECONDS_PER_DAY = SECONDS_PER_HOUR * HOURS_PER_DAY;
std::random_device ValueUtils::rd;
std::mt19937 ValueUtils::gen(rd());


float ValueUtils::eitherOr(const float d1, const float d2) {
  if (!std::isnan(d1) && d1 != 0.0f)
    return d1;

  return d2;
}


std::string ValueUtils::eitherOr(std::string s1, std::string s2) {
  if (!s1.empty())
    return s1;

  return s2;
}


std::set<int> ValueUtils::dividedBy(float divisor, const std::set<int> &originals) {
  std::vector<int> result(originals.size());
  std::transform(originals.begin(), originals.end(), result.begin(), [divisor](const int original) {
    return static_cast<int>(std::floor(static_cast<float>(original) / divisor));
  });
  return {result.begin(), result.end()};
}


float ValueUtils::ratio(const float value, const float limit) {
  return std::max(std::min(1.0f, value / limit), 0.0f);
}


bool ValueUtils::isInteger(const std::string &raw) {
  return std::regex_match(raw, isIntegerRgx);
}


float ValueUtils::limitDecimalPrecision(const float value) {
  return std::floor(value * roundPositionMultiplier) / roundPositionMultiplier;
}


std::string ValueUtils::k(const int value) {
  return std::to_string(static_cast<int>(std::floor(static_cast<float>(value) / 1000))) + K;
}


std::string ValueUtils::randomFrom(std::vector<std::string> from) {
  if (from.empty()) return "";
  std::uniform_int_distribution distrib(0, static_cast<int>(from.size()) - 1);
  const int randomIndex = distrib(gen);
  return from[randomIndex];
}


std::vector<std::string> ValueUtils::randomFrom(std::vector<std::string> from, const int num) {
  std::vector<std::string> result;
  std::sample(from.begin(), from.end(), std::back_inserter(result), num, gen);
  return result;
}


long ValueUtils::gcd(long a, long b) {
  while (b > 0) {
    const long temp = b;
    b = a % b; // % is remainder
    a = temp;
  }
  return a;
}


std::vector<int> ValueUtils::factors(long target, std::vector<int> testFactors) {
  std::vector<int> result;
  std::copy_if(testFactors.begin(), testFactors.end(), std::back_inserter(result), [target](const int tf) {
    return target % tf == 0;
  });
  return result;
}


int ValueUtils::subDiv(const int numerator, const int denominator) {
  if (numerator % denominator != 0 || numerator <= denominator) return numerator;
  int result = numerator;
  while (result % denominator == 0 && result > denominator)
    result = result / denominator;
  while (result % 2 == 0 && result > denominator * 1.38)
    result = result / 2;
  return result;
}


int ValueUtils::multipleFloor(const int factor, const float value) {
  const auto factorF = static_cast<float>(factor);
  return static_cast<int>(std::floor(value / factorF) * factorF);
}


float ValueUtils::interpolate(const float floor, const float ceiling, const float position) {
  return floor + (ceiling - floor) * position;
}


void ValueUtils::enforceMaxStereo(const int value) {
  if (value > 2)
    throw std::runtime_error("more than 2 input audio channels not allowed");
}


std::optional<UUID> ValueUtils::getKeyOfHighestValue(const std::map<UUID, int> &map) {
  if (map.empty()) return std::nullopt;

  const auto max_it = std::max_element(map.begin(), map.end(),
                                       [](const auto &a, const auto &b) {
                                         return a.second < b.second;
                                       });

  return max_it->first;
}


int ValueUtils::roundToNearest(const int N, const int value) {
  return std::max(0, static_cast<int>(std::round(static_cast<float>(value) / static_cast<float>(N)))) * N;
}


std::vector<UUID> ValueUtils::withIdsRemoved(std::vector<UUID> fromIds, const int count) {
  std::vector<UUID> ids = std::move(fromIds);
  for (int i = 0; i < count; i++) {
    if (!ids.empty()) {
      std::uniform_int_distribution distrib(0, static_cast<int>(ids.size()) - 1);
      const int randomIndex = distrib(gen);
      ids.erase(ids.begin() + randomIndex);
    }
  }
  return ids;
}


std::string ValueUtils::emptyZero(const int value) {
  return 0 != value ? std::to_string(value) : "";
}

std::vector<std::string> ValueUtils::last(const int num, std::vector<std::string> list) {
  if (num < 1) return {};
  return {list.begin() + std::max(0, static_cast<int>(list.size()) - num), list.end()};
}


