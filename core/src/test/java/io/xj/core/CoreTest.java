//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.CharStreams;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.access.Access;
import io.xj.core.payload.Payload;
import io.xj.core.transport.GsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.util.Objects;

public class CoreTest {
  protected Logger log = LoggerFactory.getLogger(CoreTest.class);
  protected Injector injector = Guice.createInjector(new CoreModule());
  protected GsonProvider gsonProvider = injector.getInstance(GsonProvider.class);
  protected Access internal = Access.internal();

  /**
   Read a file as a string of java resources

   @param filePath to get and read as string
   @return contents of file
   @throws FileNotFoundException if resource does not exist
   */
  protected static String readResourceFile(String filePath) throws IOException {
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
  protected static File resourceFile(String filePath) throws FileNotFoundException {
    ClassLoader classLoader = CoreTest.class.getClassLoader();
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
  protected static Payload deserializePayload(Object json) throws IOException {
    return new ObjectMapper().readValue(String.valueOf(json), Payload.class);
  }


  /**
   Now

   @return now
   */
  protected static Instant now() {
    return Instant.now();
  }

}
