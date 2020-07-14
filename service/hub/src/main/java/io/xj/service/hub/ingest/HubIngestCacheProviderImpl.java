// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.service.hub.access.HubAccess;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Singleton
class HubIngestCacheProviderImpl implements HubIngestCacheProvider {
  private final Map<String, HubIngestCacheItem> cachedIngestMap = Maps.newConcurrentMap();
  private final HubIngestFactory ingestFactory;
  private int cacheSeconds;

  @Inject
  public HubIngestCacheProviderImpl(
    HubIngestFactory ingestFactory,
    Config config
  ) {
    this.ingestFactory = ingestFactory;

    cacheSeconds = config.getInt("ingest.cacheSeconds");
  }

  @Override
  public HubIngest ingest(HubAccess hubAccess, Set<UUID> libraryIds, Set<UUID> programIds, Set<UUID> instrumentIds) throws HubIngestException {
    prune();

    String cacheKey = HubIngestCacheKey.of(hubAccess, libraryIds, programIds, instrumentIds);

    if (!cachedIngestMap.containsKey(cacheKey)) {
      HubIngest ingest = ingestFactory.ingest(hubAccess, libraryIds, programIds, instrumentIds);
      cachedIngestMap.put(cacheKey, new HubIngestCacheItem(ingest, cacheSeconds));
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
