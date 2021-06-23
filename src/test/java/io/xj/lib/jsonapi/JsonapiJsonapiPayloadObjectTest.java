// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.Library;
import io.xj.Program;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.util.ValueException;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static io.xj.lib.jsonapi.AssertPayloadObject.assertPayloadObject;
import static io.xj.lib.util.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 Payload object test
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class JsonapiJsonapiPayloadObjectTest {
  JsonapiPayloadFactory jsonapiPayloadFactory;
  EntityFactory entityFactory;
  JsonapiPayloadObject subject;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new JsonApiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.empty());
      }
    });
    jsonapiPayloadFactory = injector.getInstance(JsonapiPayloadFactory.class);
    entityFactory = injector.getInstance(EntityFactory.class);
    entityFactory.register(Program.class);
    subject = jsonapiPayloadFactory.newPayloadObject();
  }

  @Test
  public void add() throws JsonApiException {
    Program parentEntity1 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("Test Program")
      .build();
    subject.add("parentEntity", jsonapiPayloadFactory.setDataEntity(jsonapiPayloadFactory.newJsonapiPayload(), parentEntity1));

    assertTrue(subject.getRelationships().get("parentEntity").getDataOne().isPresent());
    assertEquals(parentEntity1.getId(), subject.getRelationships().get("parentEntity").getDataOne().get().getId());
  }

  @Test
  public void addIfRelated() throws IOException, JsonApiException {
    Library library1 = Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("test.jpg")
      .build();
    Program program2 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("sham")
      .setLibraryId(library1.getId())
      .build();
    entityFactory.register(Library.class);
    entityFactory.register(Program.class).belongsTo(Library.class);
    subject = jsonapiPayloadFactory.toPayloadObject(library1);
    JsonapiPayloadObject rel = jsonapiPayloadFactory.toPayloadObject(program2);

    jsonapiPayloadFactory.addIfRelated(subject, rel);

    assertPayloadObject(subject)
      .hasMany(Program.class, ImmutableList.of(program2));
  }

  @Test
  public void getAttribute_setAttribute() {
    subject
      .setAttribute("swanky", 23)
      .setAttribute("times", 52);

    assertTrue(subject.getAttribute("swanky").isPresent());
    assertEquals(23, subject.getAttribute("swanky").get());
  }

  @Test
  public void toMinimal() throws JsonApiException {
    Program parentEntity1 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("Test Program")
      .build();
    subject = jsonapiPayloadFactory.toPayloadObject(parentEntity1);

    JsonapiPayloadObject result = subject.toMinimal();

    assertEquals(subject.getType(), result.getType());
    assertEquals(subject.getId(), result.getId());
    assertFalse(result.getAttributes().containsKey("name"));
  }

  @Test
  public void getAttributes_setAttributes() throws ValueException {
    Map<String, Object> attr = ImmutableMap.of(
      "kittens", "cute",
      "puppies", 5
    );
    subject.setAttributes(attr);

    assertSameItems(attr, subject.getAttributes());
  }

  @Test
  public void getId_setId() {
    subject.setId("15");

    assertEquals("15", subject.getId());
  }

  @Test
  public void getLinks_setLinks() throws ValueException {
    Map<String, String> links = ImmutableMap.of(
      "kittens", "https://kittens.com/",
      "puppies", "https://puppies.com/"
    );
    subject.setLinks(links);

    assertSameItems(links, subject.getLinks());
  }

  @Test
  public void getRelationshipObject() throws JsonApiException {
    Program parentEntity1 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("Test Program")
      .build();
    subject.add("parentEntity", jsonapiPayloadFactory.setDataEntity(jsonapiPayloadFactory.newJsonapiPayload(), parentEntity1));

    assertTrue(subject.getRelationshipDataOne("parentEntity").isPresent());
    assertEquals(parentEntity1.getId(), subject.getRelationshipDataOne("parentEntity").get().getId());
  }

  @Test
  public void getRelationships_setRelationships() throws JsonApiException {
    subject.setRelationships(ImmutableMap.of(
      "parentEntity", jsonapiPayloadFactory.setDataEntity(jsonapiPayloadFactory.newJsonapiPayload(), Program.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setName("Test Program")
        .build()),
      "childEntity", jsonapiPayloadFactory.setDataEntity(jsonapiPayloadFactory.newJsonapiPayload(), Program.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setName("Test Program")
        .build())
    ));

    assertEquals(2, subject.getRelationships().size());
  }

  @Test
  public void getType_setType() {
    subject.setType("programs");

    assertEquals("programs", subject.getType());
  }

  @Test
  public void setType_fromClass() {
    assertEquals("programs", subject.setType(Program.class).getType());
  }

  @Test
  public void setAttributes_nullValueSetsNullValueInstance() throws ValueException {
    Map<String, Object> attr = Maps.newHashMap();
    attr.put("kittens", "cute");
    attr.put("puppies", null);
    subject.setAttributes(attr);

    assertSameItems(attr, subject.getAttributes());
  }

  @Test
  public void isSame() {
    String id = UUID.randomUUID().toString();
    subject.setId(id);
    subject.setType("Program");

    assertTrue(subject.isSame(Program.newBuilder().setId(id).build()));
    assertFalse(subject.isSame(Program.newBuilder().setId(UUID.randomUUID().toString()).build()));
  }

  @Test
  public void isSame_subjectStringId_compareToUuid() {
    String id = UUID.randomUUID().toString();
    subject.setId(id);
    subject.setType("Program");
    Program compareTo = Program.newBuilder().setId(id).build();

    assertTrue(subject.isSame(compareTo));
  }

}
