// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.persistence;

import com.google.inject.assistedinject.Assisted;
import io.xj.api.Segment;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface SegmentAudioFactory {
  SegmentAudio from(
    @Assisted("shipKey") String shipKey,
    @Assisted("segment") Segment segment
  );
}
