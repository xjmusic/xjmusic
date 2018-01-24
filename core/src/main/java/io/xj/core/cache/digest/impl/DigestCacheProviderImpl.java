// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache.digest.impl;

import com.google.inject.Inject;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.xj.core.cache.digest.DigestCacheProvider;
import io.xj.core.config.Config;
import io.xj.core.digest.DigestFactory;
import io.xj.core.digest.chord_markov.DigestChordMarkov;
import io.xj.core.digest.chord_sequence.DigestChordProgression;
import io.xj.core.digest.hash.DigestHash;
import io.xj.core.digest.meme.DigestMeme;
import io.xj.core.evaluation.Evaluation;

import java.util.concurrent.TimeUnit;

public class DigestCacheProviderImpl implements DigestCacheProvider {
  //  private static final Expiry<Evaluation, Digest> hashChanges = new EvaluationDigestExpiry(); // FUTURE: custom expiry when Evaluation Hash changes!
  private final DigestFactory digestFactory;
  private final LoadingCache<Evaluation, DigestMeme> digestMeme;
  private final LoadingCache<Evaluation, DigestChordMarkov> digestChordMarkov;
  private final LoadingCache<Evaluation, DigestChordProgression> digestChordProgression;

  @Inject
  DigestCacheProviderImpl(
    DigestFactory digestFactory
  ) {
    this.digestFactory = digestFactory;
    digestMeme = cacheBuilder().build(digestFactory::meme);
    digestChordProgression = cacheBuilder().build(digestFactory::chordProgression);
    digestChordMarkov = cacheBuilder().build(digestFactory::chordMarkov);
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
  public DigestMeme meme(Evaluation evaluation) throws Exception {
    return digestMeme.get(evaluation);
  }

  @Override
  public DigestChordProgression chordProgression(Evaluation evaluation) throws Exception {
    return digestChordProgression.get(evaluation);
  }

  @Override
  public DigestChordMarkov chordMarkov(Evaluation evaluation) throws Exception {
    return digestChordMarkov.get(evaluation);
  }

  @Override
  public DigestHash hash(Evaluation evaluation) throws Exception {
    return digestFactory.hashOf(evaluation);
  }

}
