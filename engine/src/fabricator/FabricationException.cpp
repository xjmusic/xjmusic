// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/FabricationException.h"

XJ::FabricationException::FabricationException(std::string msg) : msg_(std::move(msg)) {}

const char *XJ::FabricationException::what() const noexcept {
  return msg_.c_str();
}


