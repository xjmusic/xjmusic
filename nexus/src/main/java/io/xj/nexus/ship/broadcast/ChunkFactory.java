// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.ship.broadcast;


import org.jetbrains.annotations.Nullable;

/**
 * Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
public interface ChunkFactory {

  /**
   * Build a chunk to represent a Media Segment
   *
   * @param shipKey               for chunk
   * @param sequenceNumber        for chunk
   * @param fromChainMicros       for chunk
   * @param actualDurationSeconds for chunk
   * @param fileExtension         for chunk
   * @return chunk
   */
  Chunk build(
    String shipKey,
    Long sequenceNumber,
    Long fromChainMicros,
    @Nullable Double actualDurationSeconds,
    @Nullable String fileExtension
  );
}
