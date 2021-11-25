// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;

import io.xj.api.Segment;
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
   [#176642679] Advanced audio caching during fabrication
   [#180243873] Refactor ship to mix from disk as well, to resolve heap overflows

   @return stream if cached; null if not
   @param segment for which to cache audio
   */
  String getAbsolutePathToUncompressedAudio(Segment segment) throws FileStoreException, IOException, ShipException, InterruptedException;

}
