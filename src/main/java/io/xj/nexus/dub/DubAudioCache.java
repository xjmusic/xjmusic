// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;

import io.xj.lib.filestore.FileStoreException;

import java.io.BufferedInputStream;
import java.io.IOException;

public interface DubAudioCache {

  /**
   Get bytes of audio for a particular key
   <p>
   NO LONGER using Caffeine in-memory caching-- just caching on disk originally loading from S3
   <p>
   [#176642679] Advanced audio caching during fabrication

   @return stream if cached; null if not
   @param key to retrieve
   */
  BufferedInputStream get(String key) throws FileStoreException, IOException;

}
