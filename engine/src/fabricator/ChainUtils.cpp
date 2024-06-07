// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/entities/segment/Chain.h"
#include "xjmusic/entities/content/TemplateBinding.h"
#include "xjmusic/entities/segment/Segment.h"
#include "xjmusic/entities/content/Template.h"
#include "xjmusic/fabricator/SegmentUtils.h"

namespace XJ {

  std::string ChainUtils::getFullKey(const std::string &key) {
    return key + "-full";
  }

  std::string ChainUtils::getIdentifier(const std::optional<Chain> &chain) {
    if (!chain.has_value()) return "N/A";
    return chain->shipKey.empty() ? chain->id : chain->shipKey;
  }

  std::set<UUID>
  ChainUtils::targetIdsOfType(const std::vector<TemplateBinding> &chainBindings, TemplateBinding::Type type) {
    std::set < UUID > result;
    for (const auto &binding: chainBindings) {
      if (binding.type == type) {
        result.insert(binding.targetId);
      }
    }
    return result;
  }

  std::string ChainUtils::getShipKey(const std::string &chainKey, const std::string &extension) {
    return chainKey + "." + extension;
  }

  long ChainUtils::computeFabricatedToChainMicros(const std::vector<Segment> &segments) {
    auto lastDubbedSegment = SegmentUtils::getLastCrafted(segments);
    if (lastDubbedSegment.has_value()) {
      return lastDubbedSegment.value()->durationMicros.has_value() ? lastDubbedSegment.value()->beginAtChainMicros +
                                                                     lastDubbedSegment.value()->durationMicros.value()
                                                                   : lastDubbedSegment.value()->beginAtChainMicros;
    }
    return 0;
  }

  Chain ChainUtils::fromTemplate(const Template &tmpl) {
    Chain chain;
    chain.templateId = tmpl.id;
    chain.templateConfig = tmpl.config;
    chain.shipKey = tmpl.shipKey;
    chain.name = tmpl.name;
    return chain;
  }

  std::string ChainUtils::computeBaseKey(const Chain &chain) {
    return chain.shipKey.empty() ? "chain-" + chain.id : chain.shipKey;
  }

} // namespace XJ