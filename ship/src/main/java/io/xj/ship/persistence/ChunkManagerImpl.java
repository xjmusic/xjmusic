package io.xj.ship.persistence;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.app.Environment;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.lib.telemetry.MultiStopwatch.MILLIS_PER_SECOND;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
@Singleton
public class ChunkManagerImpl implements ChunkManager {
  private final Map<String/* shipKey */, Map<Long/* secondsUTC */, Chunk>> chunks = Maps.newConcurrentMap();
  private final int shipAheadChunks;
  private final long shipChunkSeconds;
  private final long shipAheadMillis;

  @Inject
  public ChunkManagerImpl(
    Environment env
  ) {
    shipAheadChunks = env.getShipAheadChunks();
    shipChunkSeconds = env.getShipChunkSeconds();
    shipAheadMillis = shipChunkSeconds * shipAheadChunks * MILLIS_PER_SECOND;
  }

  @Override
  public long computeAssembledToMillis() {
    return 0;
  }

  @Override
  public Collection<Chunk> computeAll(String shipKey) {
    var fromSecondsUTC = computeFromSecondUTC();
    return Stream.iterate(0, n -> n + 1)
      .limit(shipAheadChunks)
      .map(n -> computeOne(shipKey, fromSecondsUTC + n * shipChunkSeconds, shipChunkSeconds))
      .collect(Collectors.toList());
  }

  /**
   Compute one chunk for the given ship key, start from seconds utc, and length seconds

   @param shipKey        of chunk
   @param fromSecondsUTC of chunk
   @param lengthSeconds  of chunk
   @return chunk
   */
  private Chunk computeOne(String shipKey, long fromSecondsUTC, long lengthSeconds) {
    if (!chunks.containsKey(shipKey)) chunks.put(shipKey, Maps.newConcurrentMap());
    if (chunks.get(shipKey).containsKey(fromSecondsUTC)) return chunks.get(shipKey).get(fromSecondsUTC);
    return put(Chunk.from(shipKey, fromSecondsUTC, lengthSeconds));
  }

  /**
   Compute the seconds UTC from which we will create chunks.
   This number is always rounded down to the latest 6-second interval since 0 seconds UTC.

   @return seconds UTC from which to create chunks
   */
  private long computeFromSecondUTC() {
    return (long) (Math.floor((double) Instant.now().getEpochSecond() / shipChunkSeconds) * shipChunkSeconds);
  }

  @Override
  public Chunk put(Chunk chunk) {
    getChunks(chunk.getShipKey()).put(chunk.getFromSecondsUTC(), chunk);
    return chunk;
  }

  @Override
  public boolean isAssembledFarEnoughAhead() {
    return computeAssembledToMillis() < System.currentTimeMillis() + shipAheadMillis;
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
