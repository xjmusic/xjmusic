// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import io.xj.lib.core.payload.Payload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Objects;

/**
Utilities for reading internal resources
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
   Parse some test JSON, deserializing it into a Payload

   @param json to deserialize
   @return Payload
   @throws IOException on failure to parse
   */
  public static Payload deserializePayload(Object json) throws IOException {
    return new ObjectMapper().readValue(String.valueOf(json), Payload.class);
  }

  /**
   Now

   @return now
   */
  public static Instant now() {
    return Instant.now();
  }
}
