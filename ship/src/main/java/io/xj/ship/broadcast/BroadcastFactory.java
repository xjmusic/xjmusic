// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

import com.google.inject.assistedinject.Assisted;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface BroadcastFactory {

  /**
   Build a chunk to represent a Fragmented MP4 Media Segment

   @param shipKey        for chunk
   @param fromSecondsUTC for chunk
   @return chunk
   */
  Chunk chunk(
    @Assisted("shipKey") String shipKey,
    @Assisted("fromSecondsUTC") long fromSecondsUTC
  );

  /**
   Publish the playlist for a ship key

   @param shipKey to publish
   @return playlist
   */
  PlaylistPublisher publisher(
    @Assisted("shipKey") String shipKey,
    @Assisted("shipTitle") String shipTitle,
    @Assisted("shipSource") String shipSource
  );

  /**
   Print one media chunk

   @param chunk to print
   @return media chunk printer
   */
  ChunkPrinter printer(
    @Assisted("chunk") Chunk chunk
  );

}
