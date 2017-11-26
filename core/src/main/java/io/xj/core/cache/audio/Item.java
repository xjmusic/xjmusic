// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache.audio;

import io.xj.core.app.config.Config;
import io.xj.core.app.exception.ConfigException;

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
  private int _bytes; // in bytes
  private final String _path;

  final Logger log = LoggerFactory.getLogger(Item.class);
  final String pathPrefix = Config.cacheFilePathPrefix();
  final String pathSuffix = Config.cacheFilePathSuffix();

  /**
   New Item

   @param _key to store
   */
  public Item(String _key) throws ConfigException {
    this._key = _key;
    String bucket = Config.audioFileBucket();
    _path = pathPrefix + bucket + "-" + _key + pathSuffix;
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
