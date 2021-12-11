// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

import com.google.inject.assistedinject.Assisted;

import javax.sound.sampled.AudioFormat;

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
   Encode the stream for a ship key

   @param shipKey to encode
   @param format  of audio
   @return stream
   */
  StreamEncoder encoder(
    @Assisted("shipKey") String shipKey,
    @Assisted("audioFormat") AudioFormat format,
    @Assisted("firstChunk") Chunk firstChunk
  );

  /**
   Mix one chunk of the stream

   @param shipKey for which to print chunks
   @param format  of audio
   @return media chunk printer
   */
  ChunkMixer mixer(
    @Assisted("shipKey") String shipKey,
    @Assisted("audioFormat") AudioFormat format
  );

  /**
   Play the stream locally

   @param format of audio
   @return stream player
   */
  StreamPlayer player(
    @Assisted("audioFormat") AudioFormat format
  );

  /**
   Publish the stream for a ship key

   @param shipKey to publish
   @return playlist
   */
  StreamPublisher publisher(
    @Assisted("shipKey") String shipKey
  );
}
