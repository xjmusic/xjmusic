// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.cache.impl;

import io.xj.core.ingest.Ingest;
import io.xj.craft.digest.Digest;

import com.github.benmanes.caffeine.cache.Expiry;

import javax.annotation.Nonnull;

public class DigestCacheExpiry implements Expiry<Ingest, Digest> {
  @Override
  public long expireAfterCreate(@Nonnull Ingest key, @Nonnull Digest value, long currentTime) {
    return 0;
  }

  @Override
  public long expireAfterUpdate(@Nonnull Ingest key, @Nonnull Digest value, long currentTime, long currentDuration) {
    return 0;
  }

  @Override
  public long expireAfterRead(@Nonnull Ingest key, @Nonnull Digest value, long currentTime, long currentDuration) {
    return 0;
  }
}

