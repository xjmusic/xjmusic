// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest;

import io.xj.core.ingest.Ingest;

import java.time.Instant;

/**
 Evaluate any combination of Entities for ingest. Assumes inclusion of child entities of all entities provided
 CACHES the result for any access+entities signature, for N seconds.
 Where N is configurable in system properties `ingest.cache.seconds`
 */
public class IngestCacheItem {
  private final Instant createdAt;
  private final Ingest ingest;
  private int cacheSeconds;

  /**
   Create a new cached ingest.@param ingest to cache

   @param cacheSeconds to cache item for
   */
  IngestCacheItem(Ingest ingest, int cacheSeconds) {
    this.ingest = ingest;
    this.cacheSeconds = cacheSeconds;
    createdAt = Instant.now();
  }

  /**
   Whether this cached ingest is valid (NOT expired) because N seconds have not yet transpired since it was cached.
   Where N is configurable in system properties `ingest.cache.seconds`

   @return true if expired
   */
  Boolean isValid() {
    return Instant.now().getEpochSecond() <
      createdAt.getEpochSecond() + cacheSeconds;
  }

  /**
   Get the ingest

   @return ingest
   */
  public Ingest getIngest() {
    return ingest;
  }

}
