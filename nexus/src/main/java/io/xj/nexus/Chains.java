// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus;

import com.google.common.base.Strings;
import io.xj.api.Chain;
import io.xj.api.ChainType;
import io.xj.api.Segment;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.lib.telemetry.MultiStopwatch.MILLIS_PER_SECOND;

/**
 Utilities for working with chains
 */
public enum Chains {
  ;
  private static final String EXTENSION_SEPARATOR = ".";

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
    return Strings.isNullOrEmpty(chain.getShipKey()) ? chain.getId().toString() : chain.getShipKey();
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
   Describe a collection of chainbindings like Type[id###], Type[id###}, etc

   @param chainBindings to describe
   @return description of chain bindings
   */
  public static String describe(Collection<TemplateBinding> chainBindings) {
    return chainBindings.stream()
      .map(b -> String.format("%s[%s]", b.getType().toString(), b.getTargetId()))
      .collect(Collectors.joining(", "));
  }

  /**
   Get the storage key for a Chain

   @param chainKey  for which to get storage key
   @param extension of key
   @return chain storage key
   */
  public static String getStorageKey(String chainKey, String extension) {
    return String.format("%s%s%s", chainKey, EXTENSION_SEPARATOR, extension);
  }


  /**
   Compute the fabricated-ahead seconds for any collection of Segments

   @param segments for which to get fabricated-ahead seconds
   @return fabricated-ahead seconds for this collection of Segments
   */
  public static float computeFabricatedAheadSeconds(Chain chain, Collection<Segment> segments) {
    var lastDubbedSegment = Segments.getLastDubbed(segments);
    var dubbedUntil = lastDubbedSegment.isPresent() ?
      Instant.parse(lastDubbedSegment.get().getEndAt()) :
      Instant.parse(chain.getStartAt());
    var now = Instant.now();
    return (float) (dubbedUntil.toEpochMilli() - now.toEpochMilli()) / MILLIS_PER_SECOND;
  }

  /**
   Get a chain based on a template

   @param template from which to get chain
   @return chain from template
   */
  public static Chain fromTemplate(Template template) {
    var chain = new Chain();
    chain.setTemplateId(template.getId());
    chain.setShipKey(template.getShipKey());
    chain.setAccountId(template.getAccountId());
    chain.setType(ChainType.fromValue(template.getType().toString()));
    chain.setName(template.getName());
    return chain;
  }
}
