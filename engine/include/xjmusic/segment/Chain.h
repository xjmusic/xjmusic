// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef CHAIN_H
#define CHAIN_H

#include <string>

#include "xjmusic/util/EntityUtils.h"

#include <xjmusic/content/TemplateConfig.h>

namespace XJ {

  class Chain {
  public:

    enum Type {
      Preview,
      Production,
    };

    enum State {
      Draft,
      Ready,
      Fabricate,
      Failed,
    };

    Chain() = default;

    UUID id;
    UUID templateId{};
    Type type{};
    State state{};
    std::string shipKey{};
    TemplateConfig config{};
    std::string name{};
    long long updatedAt{EntityUtils::currentTimeMillis()};

    /**
     * Parse the Chain Type enum value from a string
     * @param value  The string to parse
     * @return      The Chain Type enum value
     */
    static Type parseType(const std::string &value);

    /**
     * Parse the Chain State enum value from a string
     * @param value  The string to parse
     * @return      The Chain State enum value
     */
    static State parseState(const std::string &value);

    /**
     * Convert an Chain Type enum value to a string
     * @param type  The Chain Type enum value
     * @return      The string representation of the Chain Type
     */
    static std::string toString(const Type &type);

    /**
     * Convert an Chain State enum value to a string
     * @param state  The Chain State enum value
     * @return      The string representation of the Chain State
     */
    static std::string toString(const State &state);

    /**
     * Assert equality with another Chain
     * @param chain  The Chain to compare
     * @return       true if equal
     */
    [[nodiscard]] bool equals(const Chain &chain) const;

    /**
     * Determine a unique hash code for the Chain
     * @return       hash code
     */
    [[nodiscard]] unsigned long long hashCode() const;

    /**
     * Compare two Chains
     * @param lhs chain
     * @param rhs chain
     * @return true if lhs < rhs
     */
    friend bool operator<(const Chain &lhs, const Chain &rhs) {
      return lhs.id < rhs.id;
    }
  };

}// namespace XJ

#endif//CHAIN_H
