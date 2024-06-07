// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_FABRICATOR_CHAIN_UTILS_H
#define XJMUSIC_FABRICATOR_CHAIN_UTILS_H

#include <string>
#include <set>
#include <vector>
#include <optional>
#include <algorithm>
#include "xjmusic/entities/segment/Chain.h"
#include "xjmusic/entities/content/TemplateBinding.h"
#include "xjmusic/entities/content/Template.h"
#include "xjmusic/entities/Entity.h"
#include "xjmusic/entities/segment/Segment.h"

namespace XJ {

  class ChainUtils {
  public:
    static std::string getFullKey(const std::string &key);

    static std::string getIdentifier(const std::optional<Chain> &chain);

    static std::set<UUID> targetIdsOfType(const std::vector<TemplateBinding> &chainBindings, TemplateBinding::Type type);

    static std::string getShipKey(const std::string &chainKey, const std::string &extension);

    static long computeFabricatedToChainMicros(const std::vector<Segment> &segments);

    static Chain fromTemplate(const Template &tmpl);

    static std::string computeBaseKey(const Chain &chain);

  };

} // namespace XJ

#endif //XJMUSIC_FABRICATOR_CHAIN_UTILS_H