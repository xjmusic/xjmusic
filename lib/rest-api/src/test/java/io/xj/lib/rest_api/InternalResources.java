// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.rest_api;

import com.google.common.io.CharStreams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Objects;

/**
 Utilities for reading internal resources
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public enum InternalResources {
  ;

  /**
   Read a file as a string of java resources

   @param filePath to get and read as string
   @return contents of file
   @throws FileNotFoundException if resource does not exist
   */
  public static String readResourceFile(String filePath) throws IOException {
    File file = resourceFile(filePath);
    String text;
    try (final FileReader reader = new FileReader(file)) {
      text = CharStreams.toString(reader);
    }
    return text;
  }

  /**
   get a file of java resources

   @param filePath to get
   @return File
   @throws FileNotFoundException if resource does not exist
   */
  public static File resourceFile(String filePath) throws FileNotFoundException {
    ClassLoader classLoader = InternalResources.class.getClassLoader();
    URL resource = classLoader.getResource(filePath);
    if (Objects.isNull(resource))
      throw new FileNotFoundException(String.format("Failed to load resource: %s", filePath));
    return new File(resource.getFile());
  }

  /**
   Now

   @return now
   */
  public static Instant now() {
    return Instant.now();
  }
}
