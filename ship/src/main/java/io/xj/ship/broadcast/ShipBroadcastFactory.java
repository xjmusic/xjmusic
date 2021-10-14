// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.broadcast;

import com.google.inject.assistedinject.Assisted;
import io.xj.api.Segment;
import io.xj.ship.source.SegmentAudio;

/**
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public interface ShipBroadcastFactory {

  Chunk chunk(
    @Assisted("shipKey") String shipKey,
    @Assisted("fromSecondsUTC") long fromSecondsUTC
  );

}
