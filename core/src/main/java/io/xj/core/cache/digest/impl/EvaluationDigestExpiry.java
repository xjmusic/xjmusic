// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache.digest.impl;

import io.xj.core.evaluation.Evaluation;
import io.xj.core.evaluation.digest.Digest;

import com.github.benmanes.caffeine.cache.Expiry;

import javax.annotation.Nonnull;

public class EvaluationDigestExpiry implements Expiry<Evaluation, Digest> {
  @Override
  public long expireAfterCreate(@Nonnull Evaluation key, @Nonnull Digest value, long currentTime) {
    return 0;
  }

  @Override
  public long expireAfterUpdate(@Nonnull Evaluation key, @Nonnull Digest value, long currentTime, long currentDuration) {
    return 0;
  }

  @Override
  public long expireAfterRead(@Nonnull Evaluation key, @Nonnull Digest value, long currentTime, long currentDuration) {
    return 0;
  }
}

