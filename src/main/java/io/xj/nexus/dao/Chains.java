// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.dao;

import com.google.common.base.Strings;
import io.xj.api.Chain;
import io.xj.api.ChainBinding;
import io.xj.api.ChainBindingType;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Utilities for working with chains
 */
public enum Chains {
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
   Get the identifier or a Chain: embed key if available, else ID

   @param chain to get identifier of
   @return embed key if available, else ID
   */
  public static String getIdentifier(@Nullable Chain chain) {
    if (Objects.isNull(chain)) return "N/A";
    return Strings.isNullOrEmpty(chain.getEmbedKey()) ? chain.getId().toString() : chain.getEmbedKey();
  }

  /**
   Filter and map target ids of a specified type from a set of chain bindings

   @param chainBindings to filter and map from
   @param type          to include
   @return set of target ids of the specified type of chain binding targets
   */
  public static Set<UUID> targetIdsOfType(Collection<ChainBinding> chainBindings, ChainBindingType type) {
    return chainBindings.stream().filter(chainBinding -> Objects.equals(chainBinding.getType(), type))
      .map(ChainBinding::getTargetId).collect(Collectors.toSet());
  }

  /**
   Describe a collection of chainbindings like Type[id###], Type[id###}, etc

   @param chainBindings to describe
   @return description of chain bindings
   */
  public static String describe(Collection<ChainBinding> chainBindings) {
    return chainBindings.stream()
      .map(b -> String.format("%s[%s]", b.getType().toString(), b.getTargetId()))
      .collect(Collectors.joining(", "));
  }
}
