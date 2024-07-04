// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <sstream>

#include "xjmusic/util/EntityUtils.h"

using namespace XJ;


std::string EntityUtils::computeUniqueId() {
  std::stringstream ss;
  ss << std::hex << std::setw(12) << std::setfill('0') << UNIQUE_ID_COUNTER++;
  return ss.str();
}

void EntityUtils::setRequired(const json &json, const std::string &key, UUID &value) {
  if (!json.contains(key)) {
    throw std::invalid_argument("Missing required UUID: " + key);
  }
  try {
    value = json.at(key).get<std::string>();
  } catch (const std::exception &e) {
    throw std::invalid_argument("Invalid value for UUID: " + key + " - " + e.what());
  }
}

void EntityUtils::setIfNotNull(const json &json, const std::string &key, std::string &value) {
  if (json.contains(key) && json.at(key).is_string()) {
    try {
      value = json.at(key).get<std::string>();
    } catch (const std::exception &e) {
      throw std::invalid_argument("Invalid value for string " + key + " - " + e.what());
    }
  }
}

void EntityUtils::setIfNotNull(const json &json, const std::string &key, float &value) {
  if (json.contains(key) && json.at(key).is_number_float()) {
    try {
      value = json.at(key).get<float>();
    } catch (const std::exception &e) {
      throw std::invalid_argument("Invalid value for float " + key + " - " + e.what());
    }
  }
}

void EntityUtils::setIfNotNull(const json &json, const std::string &key, bool &value) {
  if (json.contains(key) && json.at(key).is_boolean()) {
    try {
      value = json.at(key).get<bool>();
    } catch (const std::exception &e) {
      throw std::invalid_argument("Invalid value for bool " + key + " - " + e.what());
    }
  }
}

void EntityUtils::setIfNotNull(const json &json, const std::string &key, int &value) {
  if (json.contains(key) && json.at(key).is_number_integer()) {
    try {
      value = json.at(key).get<int>();
    } catch (const std::exception &e) {
      throw std::invalid_argument("Invalid value for integer " + key + " - " + e.what());
    }
  }
}

void EntityUtils::setIfNotNull(const json &json, const std::string &key, long long int &value) {
  if (json.contains(key) && json.at(key).is_number_unsigned()) {
    try {
      value = json.at(key).get<long long>();
    } catch (const std::exception &e) {
      throw std::invalid_argument("Invalid value for long " + key + " - " + e.what());
    }
  }
}

