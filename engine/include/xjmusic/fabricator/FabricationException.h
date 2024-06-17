// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATION_EXCEPTION_H
#define XJMUSIC_FABRICATION_EXCEPTION_H

#include <exception>
#include <string>
#include <utility>

namespace XJ {

/**
 When this occurs during fabrication, it's just a warning.
 Fabrication continues.
 */
  class FabricationException : public std::exception {
  public:
    explicit FabricationException(std::string msg);

    [[nodiscard]] const char *what() const noexcept override;

  private:
    std::string msg_;
  };

}// namespace XJ

#endif//XJMUSIC_FABRICATION_EXCEPTION_H