// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.broadcast;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.app.Environment;

import static io.xj.lib.util.Values.MILLIS_PER_SECOND;

@Singleton
public class MediaSeqNumProvider {
  private final int chunkTargetDuration;
  private final int mediaSeqNumOffset;

  @Inject
  public MediaSeqNumProvider(
    Environment env
  ) {
    chunkTargetDuration = env.getShipChunkTargetDuration();
    mediaSeqNumOffset = env.getShipMediaSequenceNumberOffset();
  }

  /**
   Get the media sequence number of a given time in milliseconds

   @param epochMillis for which to get media sequence number
   @return media sequence number
   */
  public int computeMediaSeqNum(long epochMillis) {
    return (int) (Math.floor((double) epochMillis / (MILLIS_PER_SECOND * chunkTargetDuration)));
  }

  /**
   Compute the initial media sequence number to begin shipping for any moment in time.
   <p>
   This will be slight in the past to adjust for ffmpeg needing to catch up.

   @param epochMillis for which to get media sequence number
   @return initial media sequence number
   */
  public int computeInitialMediaSeqNum(long epochMillis) {
    return computeMediaSeqNum(System.currentTimeMillis()) - mediaSeqNumOffset;
  }
}
