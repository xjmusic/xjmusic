// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.core.access.Access;
import io.xj.core.cache.CacheKey;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ChainBinding;

import java.util.Collection;
import java.util.Map;

@Singleton
class IngestCacheProviderImpl implements IngestCacheProvider {
  private final Map<String, IngestCacheItem> cachedIngestMap = Maps.newConcurrentMap();
  private final IngestFactory ingestFactory;
  private int cacheSeconds;

  @Inject
  public IngestCacheProviderImpl(
    IngestFactory ingestFactory,
    Config config
  ) {
    this.ingestFactory = ingestFactory;

    cacheSeconds = config.getInt("ingest.cacheSeconds");
  }

  @Override
  public Ingest ingest(Access access, Collection<ChainBinding> bindings) throws CoreException {
    prune();

    String cacheKey = CacheKey.of(access, bindings);

    if (!cachedIngestMap.containsKey(cacheKey)) {
      Ingest ingest = ingestFactory.ingest(access, bindings);
      cachedIngestMap.put(cacheKey, new IngestCacheItem(ingest, cacheSeconds));
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
