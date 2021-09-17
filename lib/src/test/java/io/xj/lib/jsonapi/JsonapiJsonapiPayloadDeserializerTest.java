// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.lib.Widget;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.UUID;

import static io.xj.lib.jsonapi.AssertPayload.assertPayload;

/**
 Payload deserializer test
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class JsonapiJsonapiPayloadDeserializerTest {
  private JsonapiPayloadFactory jsonapiPayloadFactory;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new JsonapiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.empty());
      }
    });
    jsonapiPayloadFactory = injector.getInstance(JsonapiPayloadFactory.class);
  }

  @Test
  public void deserializeOneIncludingEmbeddedEntities() throws IOException, JsonapiException {
    JsonapiPayload result = jsonapiPayloadFactory.deserialize(readResourceFile("payload/deserializeOneIncludingEmbeddedEntities.json"));

    assertPayload(result)
      .hasDataOne("widgets", "805cf759-4e94-4275-a82d-5255c9e69347")
      .belongsTo("Superwidget", "f94290f4-537c-4444-92e5-dc0b0df352e5")
      .hasMany("WidgetSequence", ImmutableList.of(new Widget().setId(UUID.fromString("9b862c2f-192f-4041-b849-442a2ec50218"))));
  }

  @Test
  public void deserializeOneWithRelationship() throws IOException, JsonapiException {
    JsonapiPayload result = null;
    try {
      result = jsonapiPayloadFactory.deserialize(readResourceFile("payload/deserializeOneWithRelationship.json"));
    } catch (JsonapiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
      .hasDataOne("widgets", "a1acee68-ade3-465c-8b07-596b179e1a51")
      .belongsTo("Superwidget", "aec6e236-322b-4c66-bd48-8c7ce6a1ef4a");
  }

  @Test
  public void deserializeOne() throws IOException, JsonapiException {
    JsonapiPayload result = null;
    try {
      result = jsonapiPayloadFactory.deserialize(readResourceFile("payload/deserializeOne.json"));
    } catch (JsonapiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
      .hasDataOne("widgets", "19f3fced-2c3f-405d-bde8-f7ef71f9ad9d");
  }

  @Test
  public void deserializeErrors() throws IOException, JsonapiException {
    JsonapiPayload result = null;
    try {
      result = jsonapiPayloadFactory.deserialize(readResourceFile("payload/deserializeErrors.json"));
    } catch (JsonapiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
      .hasErrorCount(1);
  }

  @Test
  public void deserializeOneWithNullAttributeValue() throws IOException, JsonapiException {
    JsonapiPayload result = null;
    try {
      result = jsonapiPayloadFactory.deserialize(readResourceFile("payload/deserializeOneWithNullAttributeValue.json"));
    } catch (JsonapiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
      .hasDataOne("widgets", "5d5db584-0530-4477-8a5b-9429e779316e");
  }

  @Test
  public void deserializeMany() throws IOException, JsonapiException {
    JsonapiPayload result = null;
    try {
      result = jsonapiPayloadFactory.deserialize(readResourceFile("payload/deserializeMany.json"));
    } catch (JsonapiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
      .hasDataMany("widgets", ImmutableList.of("716f0033-e7fe-4242-b993-1f840e4a242f"));
  }

  @Test
  public void deserializeMany_emptyTypeHasMany() throws JsonapiException {
    String json = "{\"data\":[]}";

    JsonapiPayload result = null;
    try {
      result = jsonapiPayloadFactory.deserialize(json);
    } catch (JsonapiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
      .hasDataManyEmpty();
  }

  @Test
  public void deserializeOne_nullDataSetsTypeToHasOne() throws JsonapiException {
    String json = "{\"data\":null}";

    JsonapiPayload result = null;
    try {
      result = jsonapiPayloadFactory.deserialize(json);
    } catch (JsonapiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
      .hasDataOneEmpty();
  }

  /**
   Read a file as a string of java resources

   @param filePath to get and read as string
   @return contents of file
   @throws FileNotFoundException if resource does not exist
   */
  private String readResourceFile(String filePath) throws IOException {
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
  private File resourceFile(String filePath) throws FileNotFoundException {
    ClassLoader classLoader = JsonapiJsonapiPayloadDeserializerTest.class.getClassLoader();
    URL resource = classLoader.getResource(filePath);
    if (Objects.isNull(resource))
      throw new FileNotFoundException(String.format("Failed to load resource: %s", filePath));
    return new File(resource.getFile());
  }

}
