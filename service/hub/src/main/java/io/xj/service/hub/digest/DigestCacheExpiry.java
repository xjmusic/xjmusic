// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

import com.github.benmanes.caffeine.cache.Expiry;
import io.xj.service.hub.ingest.HubIngest;

import javax.annotation.Nonnull;

public class DigestCacheExpiry implements Expiry<HubIngest, Digest> {
  @Override
  public long expireAfterCreate(@Nonnull HubIngest key, @Nonnull Digest value, long currentTime) {
    return 0;
  }

  @Override
  public long expireAfterUpdate(@Nonnull HubIngest key, @Nonnull Digest value, long currentTime, long currentDuration) {
    return 0;
  }

  @Override
  public long expireAfterRead(@Nonnull HubIngest key, @Nonnull Digest value, long currentTime, long currentDuration) {
    return 0;
  }
}

