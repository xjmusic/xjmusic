// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craft.ingest.cache.impl;

import io.xj.core.config.Config;
import io.xj.craft.ingest.Ingest;
import io.xj.core.util.TimestampUTC;

import java.sql.Timestamp;

/**
 Evaluate any combination of Entities for ingest. Assumes inclusion of child entities of all entities provided
 CACHES the result for any access+entities signature, for N seconds.
 Where N is configurable in system properties `ingest.cache.seconds`
 */
public class IngestCacheItem {
  private final Timestamp createdAt;
  private final Ingest ingest;

  /**
   Create a new cached ingest.

   @param ingest to cache
   */
  IngestCacheItem(Ingest ingest) {
    this.ingest = ingest;
    createdAt = TimestampUTC.now();
  }

  /**
   Whether this cached ingest is valid (NOT expired) because N seconds have not yet transpired since it was cached.
   Where N is configurable in system properties `ingest.cache.seconds`

   @return true if expired
   */
  Boolean isValid() {
    return TimestampUTC.now().toInstant().getEpochSecond() <
      createdAt.toInstant().getEpochSecond() + Config.ingestCacheSeconds();
  }

  /**
   Get the ingest

   @return ingest
   */
  public Ingest getIngest() {
    return ingest;
  }

}
