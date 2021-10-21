// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.Segment;
import io.xj.nexus.persistence.Segments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public class SegmentLoaderImpl extends SegmentLoader {
  private static final Logger LOG = LoggerFactory.getLogger(SegmentLoaderImpl.class);
  private final Segment segment;
  private final SegmentAudioManager segmentAudioManager;
  private final String shipKey;
  private final String threadName;

  @Inject
  public SegmentLoaderImpl(
    @Assisted("segment") Segment segment,
    @Assisted("shipKey") String shipKey,
    SegmentAudioManager segmentAudioManager
  ) {
    this.segment = segment;
    this.segmentAudioManager = segmentAudioManager;
    this.shipKey = shipKey;

    threadName = String.format("SEGMENT:%s", Segments.getIdentifier(segment));
  }

  @Override
  public void compute() {
    final Thread currentThread = Thread.currentThread();
    final String oldName = currentThread.getName();
    currentThread.setName(threadName);
    try {
      doWork();
    } finally {
      currentThread.setName(oldName);
    }
  }

  /**
   Do the work inside a named thread
   */
  private void doWork() {
    try {
      var audio =
        segmentAudioManager.get(segment.getId());

      if (audio.isEmpty()) {
        segmentAudioManager.createAndLoadAudio(shipKey, segment);

      } else switch (audio.get().getState()) {
        case Pending, Decoding, Ready -> {
          // no op; if the segment spends too much time in this state, it'll time out
        }
        case Failed -> segmentAudioManager.retry(segment.getId());
      }

    } catch (Exception e) {
      LOG.error("Failed to load audio!", e);
      segmentAudioManager.collectGarbage(segment.getId());
    }
  }

}
