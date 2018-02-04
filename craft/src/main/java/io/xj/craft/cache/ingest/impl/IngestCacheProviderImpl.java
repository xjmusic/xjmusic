// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.cache.ingest.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.cache.CacheKey;
import io.xj.craft.cache.ingest.IngestCacheProvider;
import io.xj.craft.ingest.Ingest;
import io.xj.craft.ingest.IngestFactory;
import io.xj.core.model.entity.Entity;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;
import java.util.Map;

@Singleton
public class IngestCacheProviderImpl implements IngestCacheProvider {
  private final Map<String, IngestCacheItem> cachedIngestMap = Maps.newConcurrentMap();
  private final IngestFactory ingestFactory;

  @Inject
  public IngestCacheProviderImpl(
    IngestFactory ingestFactory
  ) {
    this.ingestFactory = ingestFactory;
  }

  @Override
  public Ingest evaluate(Access access, Collection<Entity> entities) throws Exception {
    prune();

    String cacheKey = CacheKey.of(access, entities);

    if (!cachedIngestMap.containsKey(cacheKey)) {
      Ingest ingest = ingestFactory.evaluate(access, entities);
      cachedIngestMap.put(cacheKey, new IngestCacheItem(ingest));
    }

    return cachedIngestMap.get(cacheKey).getIngest();
  }

  /**
   Prune expired entries from cache
   */
  private void prune() {
    cachedIngestMap.forEach((key, ingestCacheItem) -> {
      if (!ingestCacheItem.isValid())
        cachedIngestMap.remove(key);
    });
  }

}
