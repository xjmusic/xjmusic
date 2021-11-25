// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;

import com.google.inject.assistedinject.Assisted;
import io.xj.api.Segment;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public interface SourceFactory {

  /**
   Create a Segment audio

   @param shipKey to load
   @param segment to load
   @return segment audio
   */
  SegmentAudio segmentAudio(
    @Assisted("shipKey") String shipKey,
    @Assisted("segment") Segment segment,
    @Assisted("absolutePath") String absolutePath
    );

  /**
   Load the chain manifest, including all its chains

   @param shipKey   to load
   @param onFailure callback
   @return chain manifest loader
   */
  ChainLoader spawnChainBoss(
    @Assisted("shipKey") String shipKey,
    @Assisted("onFailure") Runnable onFailure
  );

  /**
   Loads the audio for a segment. These requests are idempotent; there will be no effect of multiple requests to load
   the same segment, because the Segment Audio Manager tracks each segment id as a state machine from the moment that
   it is first encountered, updating its state when loading is complete or failed, so that it will be retried the next
   time the load is requested.
   <p>
   - If the segment has never been encountered, create a new Segment Audio in LOADING state.
   - If the segment id exists, check its state
   - If the existing segment is in LOADING state, take no action
   - If the existing segment is in FAILED state, retry
   <p>

   @param segment to load
   @return segment audio loader
   */
  SegmentLoader spawnSegmentLoader(
    @Assisted("shipKey") String shipKey,
    @Assisted("segment") Segment segment
  );

}
