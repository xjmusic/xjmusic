// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.entity;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.DAO;
import io.xj.core.model.entity.Entity;

import java.math.BigInteger;

public interface EntityCacheProvider {

  /**
   Fetch one entity.
   Caches results by class+id.

   @param entityClass of entity
   @param entityDAO   to readOne() from, as secondary source
   @param entityId    to fetch
   @param <N>         class of entity
   @return entity
   */
  <N extends Entity> N fetchOne(Access access, Class<N> entityClass, DAO<N> entityDAO, BigInteger entityId);
}
