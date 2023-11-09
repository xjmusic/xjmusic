// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.ship.broadcast;


import io.xj.nexus.mixer.AudioFileWriter;

import javax.sound.sampled.AudioFormat;

/**
 Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
public interface BroadcastFactory {

  /**
   Play the stream locally

   @param format of audio
   @return stream player
   */
  StreamPlayer player(
    AudioFormat format
  );

  /**
   Write the stream to a local .WAV file
   <p>
   Ship service can be used to write N seconds to local .WAV file https://www.pivotaltracker.com/story/show/181082015

   @param format of audio
   @return stream writer
   */
  AudioFileWriter writer(
    AudioFormat format
  );
}
