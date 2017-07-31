// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.cache;

import io.xj.core.app.config.Config;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Content {
  private final String key;
  private int totalBytes;
  private DateTime lastUsed;
  private final String path;

  final String pathPrefix = Config.cacheFilePathPrefix();
  final String pathSuffix = Config.cacheFilePathSuffix();

  /**
   New Content

   @param key to store
   */
  public Content(String key) {
    this.key = key;
    path = pathPrefix + key + pathSuffix;
    resetLastUsed();
  }

  /**
   Reset last-used date to now
   */
  private void resetLastUsed() {
    lastUsed = new DateTime();
  }

  /**
   Datetime last used

   @return datetime last uysed
   */
  public DateTime getLastUsed() {
    return lastUsed;
  }

  /**
   Total bytes saved to file

   @return total bytes
   */
  public Integer getTotalBytes() {
    return totalBytes;
  }

  /**
   key of stored data

   @return key
   */
  public String getKey() {
    return key;
  }

  /**
   stream content of stored data from file

   @return content
   */
  public InputStream getContent() throws IOException {
    resetLastUsed();
    return FileUtils.openInputStream(new File(path));
  }

  /**
   save content to file, from stream

   @param data to save to file
   @throws IOException on failure
   */
  public void save(InputStream data) throws IOException {
    OutputStream toFile = FileUtils.openOutputStream(new File(path));
    totalBytes = IOUtils.copy(data, toFile);
  }

  /**
   path to stored data file

   @return path
   */
  public String getPath() {
    return path;
  }
}
