// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;

import com.google.inject.assistedinject.Assisted;
import io.xj.api.Segment;
import io.xj.ship.broadcast.Chunk;

/**
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public interface ShipSourceFactory {

  SegmentAudio segmentAudio(
    @Assisted("shipKey") String shipKey,
    @Assisted("segment") Segment segment
  );

}
