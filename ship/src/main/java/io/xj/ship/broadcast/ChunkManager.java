package io.xj.ship.broadcast;

import java.util.Collection;

/**
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public interface ChunkManager {
  /**
   * Whether the chunks are assembled far enough ahead into the future
   *
   * @param shipKey   for which to check if assembled far enough ahead
   * @param nowMillis milliseconds since epoch, from which to compute
   * @return true if assembled far enough ahead
   */
  boolean isAssembledFarEnoughAhead(String shipKey, long nowMillis);

  /**
   * Compute the farthest-future millis to which we have a contiguous series of streamed chunks
   *
   * @param shipKey   for which to compute assembled-to millis
   * @param nowMillis milliseconds since epoch, from which to compute
   * @return millis to which we've assembled the contiguous series of streamed chunks
   */
  long computeAssembledToMillis(String shipKey, long nowMillis);

  /**
   * Compute the expected contiguous series of chunks
   * <p>
   *
   * @param shipKey   of chain for which to compute planned chunks
   * @param nowMillis milliseconds since epoch, from which to compute
   * @return list of planned chunk
   */
  Collection<Chunk> getAll(String shipKey, long nowMillis);

  /**
   * Compute the expected contiguous series of chunks to be exported right now as an HTTP live stream.
   * <p>
   *
   * @param shipKey   of chain for which to compute planned chunks
   * @param nowMillis milliseconds since epoch, from which to compute
   * @return list of planned chunk
   */
  Collection<Chunk> getContiguousDone(String shipKey, long nowMillis);

  /**
   * Compute the seconds UTC from which we will create chunks.
   * This number is always rounded down to the latest 6-second interval since 0 seconds UTC.
   *
   * @param nowMillis from which to compute seconds UTC
   * @return seconds UTC from which to create chunks
   */
  long computeFromSecondUTC(long nowMillis);

  /**
   * Store a Chunk
   *
   * @param chunk to store
   * @return stored chunk
   */
  Chunk put(Chunk chunk);

  /**
   * Clear contents
   */
  void clear();

  /**
   * Whether the given ship key has been initialized
   *
   * @param shipKey to test
   * @return true if it's been initialized
   */
  boolean isInitialized(String shipKey);

  /**
   * Acknowledge that the given ship key has been initialized
   *
   * @param shipKey to test
   */
  void didInitialize(String shipKey);
}
