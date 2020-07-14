// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.service.hub.ingest.HubIngest;

import java.util.concurrent.TimeUnit;

public class DigestCacheProviderImpl implements DigestCacheProvider {
  //  private static final Expiry<HubIngest, Digest> hashChanges = new DigestCacheExpiry(); // FUTURE: custom expiry when HubIngest Hash changes!
  private final DigestFactory digestFactory;
  private final LoadingCache<HubIngest, DigestMeme> digestMeme;
  private final LoadingCache<HubIngest, DigestChordMarkov> digestChordMarkov;
  private final LoadingCache<HubIngest, DigestChordProgression> digestChordProgression;
  private final LoadingCache<HubIngest, DigestProgramStyle> digestSequenceStyle;
  private final int digestCacheSize;
  private final int digestCacheExpireMinutes;
  private final int digestCacheRefreshMinutes;

  @Inject
  DigestCacheProviderImpl(
    DigestFactory digestFactory,
    Config config
  ) {
    digestCacheSize = config.getInt("digest.cacheSize");
    digestCacheExpireMinutes = config.getInt("digest.cacheExpireMinutes");
    digestCacheRefreshMinutes = config.getInt("digest.cacheRefreshMinutes");

    this.digestFactory = digestFactory;
    digestMeme = cacheBuilder().build(digestFactory::meme);
    digestSequenceStyle = cacheBuilder().build(digestFactory::programStyle);
    digestChordProgression = cacheBuilder().build(digestFactory::chordProgression);
    digestChordMarkov = cacheBuilder().build(digestFactory::chordMarkov);
  }

  /**
   of Caffeine cache builder
   <p>
   FUTURE: custom expiry when HubIngest Hash changes! (builder).expireAfter(hashChanges)

   @return cache builder
   */
  private Caffeine<Object, Object> cacheBuilder() {
    return Caffeine.newBuilder()
      .maximumSize(digestCacheSize)
      .expireAfterWrite(digestCacheExpireMinutes, TimeUnit.MINUTES)
      .refreshAfterWrite(digestCacheRefreshMinutes, TimeUnit.MINUTES);
  }

  @Override
  public DigestMeme meme(HubIngest ingest) {
    return digestMeme.get(ingest);
  }

  @Override
  public DigestProgramStyle sequenceStyle(HubIngest ingest) {
    return digestSequenceStyle.get(ingest);
  }

  @Override
  public DigestChordProgression chordProgression(HubIngest ingest) {
    return digestChordProgression.get(ingest);
  }

  @Override
  public DigestChordMarkov chordMarkov(HubIngest ingest) {
    return digestChordMarkov.get(ingest);
  }

  @Override
  public DigestHash hash(HubIngest ingest) {
    return digestFactory.hashOf(ingest);
  }

}
