// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.impl;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.inject.Inject;
import io.xj.core.config.Config;
import io.xj.core.ingest.Ingest;
import io.xj.craft.digest.DigestFactory;
import io.xj.craft.digest.DigestCacheProvider;
import io.xj.craft.digest.DigestChordMarkov;
import io.xj.craft.digest.DigestChordProgression;
import io.xj.craft.digest.DigestHash;
import io.xj.craft.digest.DigestMeme;
import io.xj.craft.digest.DigestProgramStyle;

import java.util.concurrent.TimeUnit;

public class DigestCacheProviderImpl implements DigestCacheProvider {
  //  private static final Expiry<Ingest, Digest> hashChanges = new DigestCacheExpiry(); // FUTURE: custom expiry when Ingest Hash changes!
  private final DigestFactory digestFactory;
  private final LoadingCache<Ingest, DigestMeme> digestMeme;
  private final LoadingCache<Ingest, DigestChordMarkov> digestChordMarkov;
  private final LoadingCache<Ingest, DigestChordProgression> digestChordProgression;
  private final LoadingCache<Ingest, DigestProgramStyle> digestSequenceStyle;

  @Inject
  DigestCacheProviderImpl(
    DigestFactory digestFactory
  ) {
    this.digestFactory = digestFactory;
    digestMeme = cacheBuilder().build(digestFactory::meme);
    digestSequenceStyle = cacheBuilder().build(digestFactory::programStyle);
    digestChordProgression = cacheBuilder().build(digestFactory::chordProgression);
    digestChordMarkov = cacheBuilder().build(digestFactory::chordMarkov);
  }

  /**
   of Caffeine cache builder
   <p>
   FUTURE: custom expiry when Ingest Hash changes! (builder).expireAfter(hashChanges)

   @return cache builder
   */
  private static Caffeine<Object, Object> cacheBuilder() {
    return Caffeine.newBuilder()
      .maximumSize(Config.getDigestCacheSize())
      .expireAfterWrite(Config.getDigestCacheExpireMinutes(), TimeUnit.MINUTES)
      .refreshAfterWrite(Config.getDigestCacheRefreshMinutes(), TimeUnit.MINUTES);
  }

  @Override
  public DigestMeme meme(Ingest ingest) {
    return digestMeme.get(ingest);
  }

  @Override
  public DigestProgramStyle sequenceStyle(Ingest ingest) {
    return digestSequenceStyle.get(ingest);
  }

  @Override
  public DigestChordProgression chordProgression(Ingest ingest) {
    return digestChordProgression.get(ingest);
  }

  @Override
  public DigestChordMarkov chordMarkov(Ingest ingest) {
    return digestChordMarkov.get(ingest);
  }

  @Override
  public DigestHash hash(Ingest ingest) {
    return digestFactory.hashOf(ingest);
  }

}
