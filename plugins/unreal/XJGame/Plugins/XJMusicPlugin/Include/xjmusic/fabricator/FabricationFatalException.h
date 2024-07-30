// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATION_FATAL_EXCEPTION_H
#define XJMUSIC_FABRICATION_FATAL_EXCEPTION_H

#include <exception>
#include <string>
#include <utility>

namespace XJ {

/**
 When this occurs during fabrication, the chain must be restarted.
 This differentiates from retry-able network or service failures.
 <p>
 Fabrication should recover from having no main choice https://github.com/xjmusic/xjmusic/issues/263
 */
  class FabricationFatalException : public std::exception {
  public:
    explicit FabricationFatalException(std::string msg);

    [[nodiscard]] const char *what() const noexcept override;

  private:
    std::string msg_;
  };

}// namespace XJ

#endif//XJMUSIC_FABRICATION_FATAL_EXCEPTION_H