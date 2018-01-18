// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache.entity.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.DAO;
import io.xj.core.model.entity.Entity;

import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;

public class EntityClassCache<N extends Entity> {
  private final Logger log = LoggerFactory.getLogger(EntityClassCache.class);
  private final Map<BigInteger, EntityClassCacheItem<N>> entityClassMap = Maps.newConcurrentMap();

  public N fetchOne(Access access, DAO<N> entityDAO, BigInteger entityId) {
    if (!entityClassMap.containsKey(entityId))
      try {
        entityClassMap.put(entityId, new EntityClassCacheItem<>(entityDAO.readOne(access, entityId)));
      } catch (Exception e) {
        log.error("Failed to {}.readOne(#{})", entityDAO.getClass().getSimpleName(), entityId, e);
      }

    return entityClassMap.get(entityId).getEntity();
  }

  /**
   Prune expired entries from cache
   */
  public void prune() {
    entityClassMap.forEach((key, entityCacheItem) -> {
      if (!entityCacheItem.isValid())
        entityClassMap.remove(key);
    });
  }

}
