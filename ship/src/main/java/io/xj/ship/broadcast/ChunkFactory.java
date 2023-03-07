// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;


import javax.annotation.Nullable;

/**
 * Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
public interface ChunkFactory {

  /**
   * Build a chunk to represent a Media Segment
   *
   * @param shipKey        for chunk
   * @param sequenceNumber for chunk
   * @param fileExtension  for chunk
   * @param actualDuration for chunk
   * @return chunk
   */
  Chunk build(
    String shipKey,
    Long sequenceNumber,
    @Nullable String fileExtension,
    @Nullable Double actualDuration
  );
}
