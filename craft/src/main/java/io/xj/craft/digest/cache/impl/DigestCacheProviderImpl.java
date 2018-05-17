// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest.cache.impl;

import com.google.inject.Inject;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.xj.craft.digest.cache.DigestCacheProvider;
import io.xj.core.config.Config;
import io.xj.craft.digest.DigestFactory;
import io.xj.craft.digest.chord_markov.DigestChordMarkov;
import io.xj.craft.digest.chord_progression.DigestChordProgression;
import io.xj.craft.digest.hash.DigestHash;
import io.xj.craft.digest.meme.DigestMeme;
import io.xj.craft.digest.sequence_style.DigestSequenceStyle;
import io.xj.craft.ingest.Ingest;

import java.util.concurrent.TimeUnit;

public class DigestCacheProviderImpl implements DigestCacheProvider {
  //  private static final Expiry<Ingest, Digest> hashChanges = new DigestCacheExpiry(); // FUTURE: custom expiry when Ingest Hash changes!
  private final DigestFactory digestFactory;
  private final LoadingCache<Ingest, DigestMeme> digestMeme;
  private final LoadingCache<Ingest, DigestChordMarkov> digestChordMarkov;
  private final LoadingCache<Ingest, DigestChordProgression> digestChordProgression;
  private final LoadingCache<Ingest, DigestSequenceStyle> digestSequenceStyle;

  @Inject
  DigestCacheProviderImpl(
    DigestFactory digestFactory
  ) {
    this.digestFactory = digestFactory;
    digestMeme = cacheBuilder().build(digestFactory::meme);
    digestSequenceStyle = cacheBuilder().build(digestFactory::sequenceStyle);
    digestChordProgression = cacheBuilder().build(digestFactory::chordProgression);
    digestChordMarkov = cacheBuilder().build(digestFactory::chordMarkov);
  }

  /**
   New Caffeine cache builder
   <p>
   FUTURE: custom expiry when Ingest Hash changes! (builder).expireAfter(hashChanges)

   @return cache builder
   */
  private static Caffeine<Object, Object> cacheBuilder() {
    return Caffeine.newBuilder()
      .maximumSize(Config.digestCacheSize())
      .expireAfterWrite(Config.digestCacheExpireMinutes(), TimeUnit.MINUTES)
      .refreshAfterWrite(Config.digestCacheRefreshMinutes(), TimeUnit.MINUTES);
  }

  @Override
  public DigestMeme meme(Ingest ingest) {
    return digestMeme.get(ingest);
  }

  @Override
  public DigestSequenceStyle sequenceStyle(Ingest ingest) {
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
