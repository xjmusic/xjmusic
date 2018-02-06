// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache.entity.impl;

import io.xj.core.config.Config;
import io.xj.core.model.entity.Entity;
import io.xj.core.util.TimestampUTC;

import java.sql.Timestamp;

/**
 Where N is configurable in system properties `entity.cache.seconds`
 */
public class EntityClassCacheItem<N extends Entity> {
  private final Timestamp createdAt;
  private final N entity;

  /**
   Create a new cached entity.

   @param entity to cache
   */
  public EntityClassCacheItem(N entity) {
    this.entity = entity;
    createdAt = TimestampUTC.now();
  }

  /**
   Whether this cached entity is valid (NOT expired) because N seconds have not yet transpired since it was cached.
   Where N is configurable in system properties `entity.cache.seconds`

   @return true if expired
   */
  public Boolean isValid() {
    return TimestampUTC.now().toInstant().getEpochSecond() <
      createdAt.toInstant().getEpochSecond() + Config.entityCacheSeconds();
  }

  /**
   Get the entity

   @return entity
   */
  public N getEntity() {
    return entity;
  }

}
