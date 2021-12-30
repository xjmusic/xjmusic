// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;

import com.google.inject.assistedinject.Assisted;
import io.xj.api.Segment;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface SourceFactory {

  /**
   Load the chain manifest, including all its chains

   @param shipKey   to load
   @param onFailure callback
   @return chain manifest loader
   */
  ChainLoader loadChain(
    @Assisted("shipKey") String shipKey,
    @Assisted("onFailure") Runnable onFailure
  );

  /**
   Create a Segment audio

   @param shipKey to load
   @param segment to load
   @return segment audio
   */
  SegmentAudio loadSegmentAudio(
    @Assisted("shipKey") String shipKey,
    @Assisted("segment") Segment segment,
    @Assisted("absolutePath") String absolutePath
  );

}
