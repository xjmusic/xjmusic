// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest.cache.impl;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.core.access.impl.Access;
import io.xj.core.cache.CacheKey;
import io.xj.core.model.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.ingest.cache.IngestCacheProvider;

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
  public Ingest evaluate(Access access, Collection<Entity> entities) throws CoreException {
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
