// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_FABRICATION_EXCEPTION_H
#define XJMUSIC_FABRICATOR_FABRICATION_EXCEPTION_H

#include <stdexcept>
#include <string>

namespace Fabricator {

  /**
   When this occurs during fabrication, a warning is show .
   This differentiates from retry-able network or service failures.
   <p>
   Fabrication should recover from having no main choice https://github.com/xjmusic/workstation/issues/263
   */
  class FabricationException : public std::runtime_error {
  public:
    explicit FabricationException(const std::string &msg) : std::runtime_error(msg) {}
  };

}// namespace Fabricator

#endif//XJMUSIC_FABRICATOR_FABRICATION_EXCEPTION_H
