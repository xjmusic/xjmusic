// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.audio.impl;

import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public class Item {
  private static final String DASH = "-";
  final Logger log = LoggerFactory.getLogger(Item.class);
  final String pathPrefix = Config.getCacheFilePathPrefix();
  final String pathSuffix = Config.getCacheFilePathSuffix();
  private final String _key;
  private final String _path;
  private int _bytes; // in bytes

  /**
   New Item
   Compute path to cached file on disk.
   <p>
   [#153228109] During Dubbing, when the audio cache refreshes an item, filenames should be unique in order to avoid deletion-collision

   @param key of item to cache
   @throws CoreException when missing required configurations
   */
  public Item(String key) throws CoreException {
    _key = key;
    String bucket = Config.getAudioFileBucket();
    _path = pathPrefix +
      bucket + File.separator +
      ItemNumber.next() + DASH + // atomic integer is always unique
      _key + pathSuffix;
  }

  /**
   _key of stored data

   @return _key
   */
  public String key() {
    return _key;
  }

  /**
   stream content of stored data from file

   @return content
   */
  public BufferedInputStream stream() throws IOException {
    return new BufferedInputStream(FileUtils.openInputStream(new File(_path)));
  }

  /**
   write underlying cache data on disk, from stream

   @param data to save to file
   @throws IOException on failure
   */
  public void writeFrom(InputStream data) throws IOException {
    if (Objects.nonNull(data)) {
      OutputStream toFile = FileUtils.openOutputStream(new File(_path));
      _bytes = IOUtils.copy(data, toFile);
      toFile.close();
      log.info("Did write media item to disk cache: {} ({} bytes)", _path, _bytes);
    } else {
      log.warn("Will not write 0 bytes to disk cache: {}", _path);
    }
  }

  /**
   this item has been removed
   */
  public void remove() {
    File file = new File(_path);
    if (file.exists()) try {
      FileUtils.forceDelete(file);
      log.info("Deleted: {}", _path);

    } catch (IOException e) {
      log.error("Failed to delete media item from disk cache: {}", _path, e);
    }
  }

  /**
   _path to stored data file

   @return _path
   */
  public String path() {
    return _path;
  }

  /**
   size of media item

   @return size in bytes
   */
  public int size() {
    return _bytes;
  }

}
