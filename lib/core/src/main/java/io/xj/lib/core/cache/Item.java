// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.cache;

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
  final Logger log = LoggerFactory.getLogger(Item.class);
  private final String key;
  private final String path;
  private int _bytes; // in bytes

  /**
   of Item
   Compute path to cached file on disk.
   <p>
   [#153228109] During Dubbing, when the audio cache refreshes an item, filenames should be unique in order to avoid deletion-collision

   @param key of item to cache
   */
  public Item(String key, String path) {
    this.key = key;
    this.path = path;
  }

  /**
   key of stored data

   @return key
   */
  public String key() {
    return key;
  }

  /**
   stream content of stored data of file

   @return content
   */
  public BufferedInputStream stream() throws IOException {
    return new BufferedInputStream(FileUtils.openInputStream(new File(path)));
  }

  /**
   write underlying cache data on disk, of stream

   @param data to save to file
   @throws IOException on failure
   */
  public void writeFrom(InputStream data) throws IOException {
    if (Objects.nonNull(data)) {
      OutputStream toFile = FileUtils.openOutputStream(new File(path));
      _bytes = IOUtils.copy(data, toFile);
      toFile.close();
      log.info("Did write media item to disk cache: {} ({} bytes)", path, _bytes);
    } else {
      log.warn("Will not write 0 bytes to disk cache: {}", path);
    }
  }

  /**
   this item has been removed
   */
  public void remove() {
    File file = new File(path);
    if (file.exists()) try {
      FileUtils.forceDelete(file);
      log.info("Deleted: {}", path);

    } catch (IOException e) {
      log.error("Failed to delete media item create disk cache: {}", path, e);
    }
  }

  /**
   path to stored data file

   @return path
   */
  public String path() {
    return path;
  }

  /**
   size of media item

   @return size in bytes
   */
  public int size() {
    return _bytes;
  }

}
