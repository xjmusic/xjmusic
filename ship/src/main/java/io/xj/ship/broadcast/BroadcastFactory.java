// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

import com.google.inject.assistedinject.Assisted;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface BroadcastFactory {

  /**
   Build a chunk to represent a Media Segment

   @param shipKey        for chunk
   @param sequenceNumber for chunk
   @param fileExtension  for chunk
   @param actualDuration  for chunk
   @return chunk
   */
  Chunk chunk(
    @Assisted("shipKey") String shipKey,
    @Assisted("sequenceNumber") Long sequenceNumber,
    @Nullable @Assisted("fileExtension") String fileExtension,
    @Nullable @Assisted("actualDuration") Double actualDuration
  );

  /**
   Encode the stream for a ship key

   @param format of audio
   @return stream
   */
  StreamEncoder encoder(
    @Assisted("shipKey") String shipKey,
    @Assisted("audioFormat") AudioFormat format
  );

  /**
   Mix one chunk of the stream

   @param chunk  to mix
   @param format of audio
   @return media chunk printer
   */
  ChunkMixer mixer(
    @Assisted("chunk") Chunk chunk,
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
  PlaylistPublisher publisher(
    @Assisted("shipKey") String shipKey
  );
}
