// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import java.io.File;
import java.io.IOException;

/**
 Get a temp file
 */
public interface TempFile {
  String DEFAULT_TEMP_FILE_PATH_PREFIX_CHUNK = "tmp";
  String DEFAULT_TEMP_FILE_PATH_PREFIX_CREATE_NAME = "temp-file-name";
  String DEFAULT_TEMP_FILE_PATH_PREFIX_CREATE_SUFFIX = ".tmp";

  /**
   @return temp file path prefix
   */
  static String getTempFilePathPrefix() {
    String path = File.separator + DEFAULT_TEMP_FILE_PATH_PREFIX_CHUNK + File.separator;
    try {
      String absolutePath = File.createTempFile(DEFAULT_TEMP_FILE_PATH_PREFIX_CREATE_NAME, DEFAULT_TEMP_FILE_PATH_PREFIX_CREATE_SUFFIX).getAbsolutePath();
      path = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)) + File.separator;

    } catch (IOException ignored) {
      // noop
    }
    return path;
  }


}
