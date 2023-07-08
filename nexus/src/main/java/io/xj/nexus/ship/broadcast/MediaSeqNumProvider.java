// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.ship.broadcast;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static io.xj.lib.util.Values.MICROS_PER_SECOND;

@Service
public class MediaSeqNumProvider {
  final int chunkDurationSeconds;

  @Autowired
  public MediaSeqNumProvider(
    @Value("${ship.chunk.duration.seconds}") int chunkDurationSeconds
  ) {
    this.chunkDurationSeconds = chunkDurationSeconds;
  }

  /**
   * Get the media sequence number of a given time in milliseconds
   *
   * @param chainMicros for which to get media sequence number
   * @return media sequence number
   */
  public int computeMediaSeqNum(long chainMicros) {
    return (int) (Math.floor((double) chainMicros / (MICROS_PER_SECOND * chunkDurationSeconds)));
  }

  public long computeChainMicros(long seqNum) {
    return seqNum * MICROS_PER_SECOND * chunkDurationSeconds;
  }
}
