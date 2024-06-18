// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/FabricationFatalException.h"

using namespace XJ;


FabricationFatalException::FabricationFatalException(std::string msg) : msg_(std::move(msg)) {}


const char *FabricationFatalException::what() const noexcept {
  return msg_.c_str();
}


