// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.NexusException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 Load audio from disk to memory, or if necessary, from S3 to disk (for future caching), then to memory.
 <p>
 [#176642679] Advanced audio caching during fabrication
 <p>
 Original DubAudioCacheItem should not be implemented with Caffeine-- this is the mechanism we use only for downloading files not already present to disk.
 <p>
 Implement Caffeine after loading the audio data from disk into memory-- the real speed lift here is from keeping the audio in memory
 */
public class DubAudioCacheItem {
  final Logger log = LoggerFactory.getLogger(DubAudioCacheItem.class);
  private final String key;
  private final String path;
  private int size; // # of bytes
  private final byte[] bytes;

  /**
   @param config            from app
   @param fileStoreProvider from which to load files
   @param key               ot this item
   @param path              to this item's waveform data on disk
   */
  @Inject
  public DubAudioCacheItem(
    Config config,
    FileStoreProvider fileStoreProvider,
    @Assisted("key") String key,
    @Assisted("path") String path
  ) throws FileStoreException, IOException, NexusException {
    this.key = key;
    this.path = path;
    String audioFileBucket = config.getString("audio.fileBucket");
    if (!existsOnDisk())
      try (InputStream stream = fileStoreProvider.streamS3Object(audioFileBucket, key)) {
        writeFrom(stream);
      }

    try (BufferedInputStream stream = new BufferedInputStream(FileUtils.openInputStream(new File(path)))) {
      bytes = new byte[stream.available()];
      size = stream.read(bytes);
    }
  }

  /**
   key of stored data

   @return key
   */
  public String key() {
    return key;
  }

  /**
   @return true if this dub audio cache item exists (as audio waveform data) on disk
   */
  private boolean existsOnDisk() {
    return new File(path).exists();
  }

  /**
   write underlying cache data on disk, of stream

   @param data to save to file
   @throws IOException on failure
   */
  public void writeFrom(InputStream data) throws IOException, NexusException {
    if (Objects.isNull(data))
      throw new NexusException(String.format("Unable to write bytes to disk cache: %s", path));

    OutputStream toFile = FileUtils.openOutputStream(new File(path));
    size = IOUtils.copy(data, toFile); // stores number of bytes copied
    toFile.close();
    log.debug("Did write media item to disk cache: {} ({} bytes)", path, size);
  }

  /**
   @return path to stored data file
   */
  public String path() {
    return path;
  }

  /**
   @return size in # of bytes, of waveform audio loaded from disk into memory
   */
  public int size() {
    return size;
  }

  /**
   @return bytes of waveform audio loaded from disk into memory
   */
  public BufferedInputStream getBytes() {
    return new BufferedInputStream(new ByteArrayInputStream(bytes));
  }
}
