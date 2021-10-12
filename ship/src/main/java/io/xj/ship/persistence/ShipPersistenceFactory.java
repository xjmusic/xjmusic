// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.persistence;

import com.google.inject.assistedinject.Assisted;
import io.xj.api.Segment;

/**
 * Ship broadcast via HTTP Live Streaming #179453189
 */
public interface ShipPersistenceFactory {

  SegmentAudio segmentAudio(
    @Assisted("shipKey") String shipKey,
    @Assisted("segment") Segment segment
  );

  Chunk chunk(
    @Assisted("shipKey") String shipKey,
    @Assisted("fromSecondsUTC") long fromSecondsUTC
  );

}
