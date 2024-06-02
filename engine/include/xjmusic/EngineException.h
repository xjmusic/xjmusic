// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_ENGINE_EXCEPTION_H
#define XJMUSIC_FABRICATOR_ENGINE_EXCEPTION_H

#include <stdexcept>
#include <string>

namespace Fabricator {

  class EngineException : public std::runtime_error {
  public:
    explicit EngineException(const std::string &msg) : std::runtime_error(msg) {}
  };

}// namespace Fabricator

#endif//XJMUSIC_FABRICATOR_ENGINE_EXCEPTION_H
