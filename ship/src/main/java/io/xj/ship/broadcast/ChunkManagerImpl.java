// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.api.client.util.Lists;
import com.google.api.client.util.Sets;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.app.Environment;
import io.xj.nexus.persistence.ChainManager;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
@Singleton
public class ChunkManagerImpl implements ChunkManager {
  private static final long MILLIS_PER_SECOND = 1000;
  private final Map<String/* shipKey */, Map<Long/* secondsUTC */, Chunk>> chunks = Maps.newConcurrentMap();
  private final BroadcastFactory broadcast;
  private final int shipAheadChunks;
  private final long shipAheadMillis;
  private final long shipChunkSeconds;
  private final ChainManager chainManager;
  private final Set<String> initializedShipKeys;

  @Inject
  public ChunkManagerImpl(
    Environment env,
    BroadcastFactory broadcastFactory,
    ChainManager chainManager
  ) {
    broadcast = broadcastFactory;
    shipAheadChunks = env.getShipAheadChunks();
    shipChunkSeconds = env.getShipChunkSeconds();
    this.chainManager = chainManager;
    shipAheadMillis = (shipAheadChunks - 1) * shipChunkSeconds * MILLIS_PER_SECOND;
    initializedShipKeys = Sets.newHashSet();
  }

  @Override
  public long computeAssembledToMillis(String shipKey, long nowMillis) {
    return getContiguousDone(shipKey, nowMillis).stream()
      .max(Comparator.comparing(Chunk::getToInstant))
      .map(Chunk::getToInstant)
      .map(Instant::toEpochMilli)
      .orElse(0L);
  }

  @Override
  public Collection<Chunk> getAll(String shipKey, long nowMillis) {
    if (!chainManager.existsForShipKey(shipKey)) return List.of();
    var fromSecondsUTC = computeFromSecondUTC(nowMillis);
    return Stream.iterate(0, n -> n + 1)
      .limit(shipAheadChunks)
      .map(n -> computeOne(shipKey, fromSecondsUTC + n * shipChunkSeconds))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<Chunk> getContiguousDone(String shipKey, long nowMillis) {
    var seeking = true;
    List<Chunk> done = Lists.newArrayList();
    var chunks = getAll(shipKey, nowMillis);
    for (var chunk : chunks) {
      if (seeking && ChunkState.Done.equals(chunk.getState())) {
        done.add(chunk);
      } else seeking = false;
    }
    return done;
  }

  /**
   Compute one chunk for the given ship key, start from seconds utc, and length seconds

   @param shipKey        of chunk
   @param fromSecondsUTC of chunk
   @return chunk
   */
  private Chunk computeOne(String shipKey, long fromSecondsUTC) {
    if (!chunks.containsKey(shipKey)) chunks.put(shipKey, Maps.newConcurrentMap());
    if (chunks.get(shipKey).containsKey(fromSecondsUTC)) return chunks.get(shipKey).get(fromSecondsUTC);
    return put(broadcast.chunk(shipKey, fromSecondsUTC));
  }

  @Override
  public long computeFromSecondUTC(long nowMillis) {
    return (long) (Math.floor((double) (nowMillis / MILLIS_PER_SECOND) / shipChunkSeconds) * shipChunkSeconds);
  }

  @Override
  public Chunk put(Chunk chunk) {
    chunk.setUpdated(Instant.now());
    getChunks(chunk.getShipKey()).put(chunk.getFromSecondsUTC(), chunk);
    return chunk;
  }

  @Override
  public void clear() {
    chunks.clear();
  }

  @Override
  public boolean isInitialized(String shipKey) {
    return initializedShipKeys.contains(shipKey);
  }

  @Override
  public void didInitialize(String shipKey) {
    initializedShipKeys.add(shipKey);
  }

  @Override
  public boolean isAssembledFarEnoughAhead(String shipKey, long nowMillis) {
    return computeAssembledToMillis(shipKey, nowMillis) >= nowMillis + shipAheadMillis;
  }

  /**
   Get the map of chunks for a given ship key

   @param shipKey for which to get chunks
   @return chunk map
   */
  private Map<Long, Chunk> getChunks(String shipKey) {
    if (!chunks.containsKey(shipKey)) chunks.put(shipKey, Maps.newConcurrentMap());
    return chunks.get(shipKey);
  }
}
