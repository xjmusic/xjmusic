// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.entity.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.cache.entity.EntityCacheProvider;
import io.xj.core.dao.DAO;
import io.xj.core.model.entity.Entity;

import com.google.common.collect.Maps;
import com.google.inject.Singleton;

import java.math.BigInteger;
import java.util.Map;

@Singleton
public class EntityCacheProviderImpl implements EntityCacheProvider {
  private final Map<Class, EntityClassCache> entityClassCacheMap = Maps.newConcurrentMap();

  @Override
  public <N extends Entity> N fetchOne(Access access, Class<N> entityClass, DAO<N> entityDAO, BigInteger entityId) {
    prune();

    if (!entityClassCacheMap.containsKey(entityClass))
      entityClassCacheMap.put(entityClass, new EntityClassCache<N>());

    // I KNOW the following is is an unchecked assignment. But the architecture of this class guarantees that it is in fact always the correct class being returned.
    EntityClassCache<N> entityClassCache = entityClassCacheMap.get(entityClass);
    return entityClassCache.fetchOne(access, entityDAO, entityId);
  }

  /**
   Prune all caches for expired entires
   */
  private void prune() {
    entityClassCacheMap.values().forEach(EntityClassCache::prune);
  }

}
