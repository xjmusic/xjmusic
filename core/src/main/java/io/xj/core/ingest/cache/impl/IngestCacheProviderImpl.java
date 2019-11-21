// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest.cache.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.core.access.Access;
import io.xj.core.cache.CacheKey;
import io.xj.core.exception.CoreException;
import io.xj.core.ingest.Ingest;
import io.xj.core.ingest.IngestFactory;
import io.xj.core.ingest.cache.IngestCacheProvider;
import io.xj.core.model.ChainBinding;

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
  public Ingest ingest(Access access, Collection<ChainBinding> bindings) throws CoreException {
    prune();

    String cacheKey = CacheKey.of(access, bindings);

    if (!cachedIngestMap.containsKey(cacheKey)) {
      Ingest ingest = ingestFactory.ingest(access, bindings);
      cachedIngestMap.put(cacheKey, new IngestCacheItem(ingest));
    }

    return cachedIngestMap.get(cacheKey).getIngest();
  }

  /**
   Prune expired entries of cache
   */
  private void prune() {
    Collection<String> keysToRemove = Lists.newArrayList();
    cachedIngestMap.forEach((key, ingestCacheItem) -> {
      if (!ingestCacheItem.isValid())
        keysToRemove.add(key);
    });
    keysToRemove.forEach(cachedIngestMap::remove);
  }

}
