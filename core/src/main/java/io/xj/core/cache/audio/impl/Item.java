// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.cache.audio.impl;

import io.xj.core.config.Config;
import io.xj.core.exception.ConfigException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Item {
  private final String _key;
  private static final String DASH = "-";
  private int _bytes; // in bytes
  private final String _path;

  final Logger log = LoggerFactory.getLogger(Item.class);
  final String pathPrefix = Config.cacheFilePathPrefix();
  final String pathSuffix = Config.cacheFilePathSuffix();

  /**
   New Item
   Compute path to cached file on disk.
   <p>
   [#153228109] During Dubbing, when the audio cache refreshes an item, filenames should be unique in order to avoid deletion-collision

   @param key of item to cache
   @throws ConfigException when missing required configurations
   */
  public Item(String key) throws ConfigException {
    _key = key;
    String bucket = Config.audioFileBucket();
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
  public InputStream stream() throws IOException {
    return FileUtils.openInputStream(new File(_path));
  }

  /**
   write underlying cache data on disk, from stream

   @param data to save to file
   @throws IOException on failure
   */
  public void writeFrom(InputStream data) throws IOException {
    OutputStream toFile = FileUtils.openOutputStream(new File(_path));
    _bytes = IOUtils.copy(data, toFile);
    toFile.close();
    log.info("Wrote media item to disk cache: {} ({} bytes)", _path, _bytes);
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
