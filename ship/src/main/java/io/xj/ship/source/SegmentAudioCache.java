// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;

import io.xj.nexus.model.Segment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.ship.ShipException;

import java.io.IOException;

public interface SegmentAudioCache {

  /**
   Get bytes of audio for a particular key.
   This cache also understands how to convert source OGG files into WAV on disk for mixing.
   <p>
   NO LONGER using Caffeine in-memory caching-- just caching on disk originally loading from S3
   <p>
   Advanced audio caching during fabrication https://www.pivotaltracker.com/story/show/176642679
   Refactor ship to mix from disk as well, to resolve heap overflows https://www.pivotaltracker.com/story/show/180243873

   @param segment for which to cache audio
   @return stream if cached; null if not
   */
  String downloadAndDecompress(Segment segment) throws FileStoreException, IOException, ShipException, InterruptedException;

  /**
   Cleanup audio files for the given segment

   @param segment for which to collect garbage
   */
  void collectGarbage(Segment segment);
}
