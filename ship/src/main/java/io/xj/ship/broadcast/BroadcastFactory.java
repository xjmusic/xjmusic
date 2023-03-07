// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;


import javax.sound.sampled.AudioFormat;

/**
 * Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
public interface BroadcastFactory {

  /**
   * Encode the stream for a ship key
   *
   * @param format of audio
   * @return stream
   */
  StreamEncoder encoder(
    String shipKey,
    AudioFormat format,
    Long initialSeqNum
  );

  /**
   * Mix one chunk of the stream
   *
   * @param chunk  to mix
   * @param format of audio
   * @return media chunk printer
   */
  ChunkMixer mixer(
    Chunk chunk,
    AudioFormat format
  );

  /**
   * Play the stream locally
   *
   * @param format of audio
   * @return stream player
   */
  StreamPlayer player(
    AudioFormat format
  );

  /**
   * Write the stream to a local .WAV file
   * <p>
   * Ship service can be used to write N seconds to local .WAV file https://www.pivotaltracker.com/story/show/181082015
   *
   * @param format of audio
   * @return stream writer
   */
  StreamWriter writer(
    AudioFormat format
  );
}
