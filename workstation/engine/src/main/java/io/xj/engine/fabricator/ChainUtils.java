// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.fabricator;

import io.xj.model.enums.ContentBindingType;
import io.xj.model.pojos.Template;
import io.xj.model.pojos.TemplateBinding;
import io.xj.model.util.StringUtils;
import io.xj.engine.model.Chain;
import io.xj.engine.model.Segment;
import jakarta.annotation.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Utilities for working with chains
 */
public enum ChainUtils {
  ;

  /**
   Get the full key from a key

   @param key of which to get full key
   @return full key
   */
  public static String getFullKey(String key) {
    return String.format("%s-full", key);
  }

  /**
   Get the identifier or a Chain: ship key if available, else ID

   @param chain to get identifier of
   @return ship key if available, else ID
   */
  public static String getIdentifier(@Nullable Chain chain) {
    if (Objects.isNull(chain)) return "N/A";
    return StringUtils.isNullOrEmpty(chain.getShipKey()) ? chain.getId().toString() : chain.getShipKey();
  }

  /**
   Filter and map target ids of a specified type from a set of chain bindings

   @param chainBindings to filter and map from
   @param type          to include
   @return set of target ids of the specified type of chain binding targets
   */
  public static Set<UUID> targetIdsOfType(Collection<TemplateBinding> chainBindings, ContentBindingType type) {
    return chainBindings.stream().filter(templateBinding -> Objects.equals(templateBinding.getType(), type))
      .map(TemplateBinding::getTargetId).collect(Collectors.toSet());
  }

  /**
   Get the ship key for a Chain

   @param chainKey  for which to get ship key
   @param extension of key
   @return chain ship key
   */
  public static String getShipKey(String chainKey, String extension) {
    return String.format("%s%s%s", chainKey, Chain.EXTENSION_SEPARATOR, extension);
  }

  /**
   Compute the fabricated-ahead seconds for any collection of Segments

   @param segments for which to get fabricated-ahead seconds
   @return fabricated-ahead seconds for this collection of Segments
   */
  public static Long computeFabricatedToChainMicros(Collection<Segment> segments) {
    var lastDubbedSegment = SegmentUtils.getLastCrafted(segments);
    return lastDubbedSegment.map(segment -> (Objects.nonNull(segment.getDurationMicros()) ? segment.getBeginAtChainMicros() + segment.getDurationMicros() : segment.getBeginAtChainMicros())).orElse(0L);
  }

  /**
   Get a chain based on a template

   @param template from which to get chain
   @return chain from template
   */
  public static Chain fromTemplate(Template template) {
    var chain = new Chain();
    chain.setTemplateId(template.getId());
    chain.setTemplateConfig(template.getConfig());
    chain.setShipKey(template.getShipKey());
    chain.setProjectId(template.getProjectId());
    chain.setName(template.getName());
    return chain;
  }

  /**
   @return Chain base key
   */
  public static String computeBaseKey(Chain chain) {
    return StringUtils.isNullOrEmpty(chain.getShipKey()) ? String.format("chain-%s", chain.getId()) : chain.getShipKey();
  }
}
