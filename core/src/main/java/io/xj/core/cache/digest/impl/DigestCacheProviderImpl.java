// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache.digest.impl;

import io.xj.core.cache.digest.DigestCacheProvider;
import io.xj.core.config.Config;
import io.xj.core.evaluation.Evaluation;
import io.xj.core.evaluation.digest.DigestFactory;
import io.xj.core.evaluation.digest.chords.DigestChords;
import io.xj.core.evaluation.digest.hash.DigestHash;
import io.xj.core.evaluation.digest.memes.DigestMemes;

import com.google.inject.Inject;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import java.util.concurrent.TimeUnit;

public class DigestCacheProviderImpl implements DigestCacheProvider {
  //  private static final Expiry<Evaluation, Digest> hashChanges = new EvaluationDigestExpiry(); // FUTURE: custom expiry when Evaluation Hash changes!
  private final DigestFactory digestFactory;
  private final LoadingCache<Evaluation, DigestMemes> digestMemes;
  private final LoadingCache<Evaluation, DigestChords> digestChords;

  @Inject
  DigestCacheProviderImpl(
    DigestFactory digestFactory
  ) {
    this.digestFactory = digestFactory;
    digestMemes = cacheBuilder().build(digestFactory::memesOf);
    digestChords = cacheBuilder().build(digestFactory::chordsOf);
  }

  /**
   New Caffeine cache builder
   <p>
   FUTURE: custom expiry when Evaluation Hash changes! (builder).expireAfter(hashChanges)

   @return cache builder
   */
  private static Caffeine<Object, Object> cacheBuilder() {
    return Caffeine.newBuilder()
      .maximumSize(Config.evaluationDigestCacheSize())
      .expireAfterWrite(Config.evaluationDigestCacheExpireMinutes(), TimeUnit.MINUTES)
      .refreshAfterWrite(Config.evaluationDigestCacheRefreshMinutes(), TimeUnit.MINUTES);
  }

  @Override
  public DigestMemes memesOf(Evaluation evaluation) throws Exception {
    return digestMemes.get(evaluation);
  }

  @Override
  public DigestChords chordsOf(Evaluation evaluation) throws Exception {
    return digestChords.get(evaluation);
  }

  @Override
  public DigestHash hashOf(Evaluation evaluation) throws Exception {
    return digestFactory.hashOf(evaluation);
  }

}
