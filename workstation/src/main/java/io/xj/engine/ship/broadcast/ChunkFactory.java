// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.ship.broadcast;


import jakarta.annotation.Nullable;

/**
 Ship broadcast via HTTP Live Streaming https://github.com/xjmusic/workstation/issues/279
 */
public interface ChunkFactory {

  /**
   Build a chunk to represent a Media Segment

   @param shipKey               for chunk
   @param sequenceNumber        for chunk
   @param fromChainMicros       for chunk
   @param actualDurationSeconds for chunk
   @param fileExtension         for chunk
   @return chunk
   */
  Chunk build(
    String shipKey,
    Long sequenceNumber,
    Long fromChainMicros,
    @Nullable Double actualDurationSeconds,
    @Nullable String fileExtension
  );
}
