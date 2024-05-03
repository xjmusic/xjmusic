// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.ship.broadcast;


import io.xj.nexus.mixer.AudioFileWriter;

import javax.sound.sampled.AudioFormat;

/**
 Ship broadcast via HTTP Live Streaming https://github.com/xjmusic/workstation/issues/279
 */
public interface BroadcastFactory {

  /**
   Play the stream locally

   @param format     of audio
   @param bufferSize of audio
   @return stream player
   */
  StreamPlayer player(
    AudioFormat format,
    int bufferSize
  );

  /**
   Write the stream to a local .WAV file
   <p>
   Ship service can be used to write N seconds to local .WAV file https://github.com/xjmusic/workstation/issues/272

   @param format of audio
   @return stream writer
   */
  AudioFileWriter writer(
    AudioFormat format
  );
}
