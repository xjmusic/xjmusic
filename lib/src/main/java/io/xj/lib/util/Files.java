// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import io.xj.lib.mixer.InternalResource;

import java.io.*;

/**
 Get a temp file
 */
public interface Files {
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

  /**
   Assert size of two different files is within a tolerated threshold

   @param f1 to compare
   @param f2 to compare
   @return true if within tolerance
   */
  static boolean isFileSizeWithin(File f1, File f2) {
    float deviance = (float) f1.getTotalSpace() / f2.getTotalSpace();
    return (1 - (float) 0.02) < deviance && (1 + (float) 0.02) > deviance;
  }
  /**
   get the content of a file from java resources

   @param filePath to get
   @return File
   */
  static String getFileContent(String filePath) throws IOException {
    return new String(new BufferedInputStream(new FileInputStream(filePath)).readAllBytes());
  }

  /**
   get the content of a file from java resources

   @param filePath to get
   @return File
   */
  static String getResourceFileContent(String filePath) throws IOException {
    return new String(new BufferedInputStream(new FileInputStream(getResourceFile(filePath))).readAllBytes());
  }

  /**
   get a file from java resources

   @param filePath to get
   @return File
   */
  static File getResourceFile(String filePath) {
    InternalResource internalResource = new InternalResource(filePath);
    return internalResource.getFile();
  }

  /**
   get unique temp filename

   @param subFilename filename within this filename
   @return filename
   */
  static String getUniqueTempFilename(String subFilename) {
    return getTempFilePathPrefix() + System.nanoTime() + "-" + subFilename;
  }
}
