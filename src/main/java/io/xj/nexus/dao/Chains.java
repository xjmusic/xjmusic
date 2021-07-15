// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.dao;

import com.google.common.base.Strings;
import io.xj.Chain;
import io.xj.ChainBinding;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
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
    return Strings.isNullOrEmpty(chain.getEmbedKey()) ? chain.getId() : chain.getEmbedKey();
  }

  /**
   Filter and map target ids of a specified type from a set of chain bindings

   @param chainBindings to filter and map from
   @param type          to include
   @return set of target ids of the specified type of chain binding targets
   */
  public static Set<String> targetIdsOfType(Collection<ChainBinding> chainBindings, ChainBinding.Type type) {
    return chainBindings.stream().filter(chainBinding -> chainBinding.getType().equals(type))
      .map(ChainBinding::getTargetId).collect(Collectors.toSet());
  }
}
