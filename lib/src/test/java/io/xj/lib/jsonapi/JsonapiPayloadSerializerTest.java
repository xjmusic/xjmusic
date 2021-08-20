// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.api.Program;
import io.xj.lib.entity.EntityFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.assertFalse;

/**
 Payload serializer test
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class JsonapiPayloadSerializerTest {
  private JsonapiPayloadFactory jsonapiPayloadFactory;
  private EntityFactory entityFactory;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new JsonapiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.empty());
      }
    });
    entityFactory = injector.getInstance(EntityFactory.class);
    jsonapiPayloadFactory = injector.getInstance(JsonapiPayloadFactory.class);
    entityFactory.register(Program.class);
  }

  @Test
  public void serialize() throws JsonapiException {
    Program program = new Program()
      .id(UUID.randomUUID())
      .name("test_print")
      ;
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.setDataEntity(jsonapiPayloadFactory.newJsonapiPayload(), program);

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    AssertPayload.assertPayload(jsonapiPayloadFactory.deserialize(result))
      .hasDataOne("programs", program.getId().toString());
  }

  @Test
  public void serializeOne() throws JsonapiException {
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.newJsonapiPayload();
    Program library = new Program()
      .id(UUID.randomUUID())
      .name("Test Program")
      ;
    jsonapiPayloadFactory.setDataEntity(jsonapiPayload, library);

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    AssertPayload.assertPayload(jsonapiPayloadFactory.deserialize(result))
      .hasDataOne("programs", library.getId().toString());
  }

  @Test
  public void serializeOne_withBelongsTo() throws JsonapiException {
    entityFactory.register("Library");
    entityFactory.register("Program").belongsTo("Library");
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.newJsonapiPayload();
    Program library = new Program()
      .id(UUID.randomUUID())
      .name("y")
      ;
    Program program = new Program()
      .id(UUID.randomUUID())
      .libraryId(library.getId())
      .name("x")
      ;
    jsonapiPayloadFactory.setDataEntity(jsonapiPayload, program);

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    AssertPayload.assertPayload(jsonapiPayloadFactory.deserialize(result))
      .hasDataOne("programs", program.getId().toString())
      .belongsTo("Library", library.getId().toString());
  }

  /**
   [#175792528] JSON:API serializer must not include relationship payload where there is none
   */
  @Test
  public void serializeOne_withBelongsTo_empty() throws JsonapiException {
    entityFactory.register("Library");
    entityFactory.register("Program").belongsTo("Library");
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.newJsonapiPayload();
    Program program = new Program()
      .id(UUID.randomUUID())
      .libraryId(null)
      .name("x")
      ;
    jsonapiPayloadFactory.setDataEntity(jsonapiPayload, program);

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    assertFalse(result.contains("libraries"));
  }

  @Test
  public void serializeOne_withHasMany() throws JsonapiException {
    entityFactory.register("Program").hasMany("Program");
    Program program0 = new Program()
      .id(UUID.randomUUID())
      .name("y")
      ;
    Program program1 = new Program()
      .id(UUID.randomUUID())
      .libraryId(program0.getId())
      .name("x")
      ;
    Program program2 = new Program()
      .id(UUID.randomUUID())
      .libraryId(program1.getId())
      .name("b")
      ;
    Program program3 = new Program()
      .id(UUID.randomUUID())
      .libraryId(program1.getId())
      .name("c")
      ;
    JsonapiPayloadObject mainObj = jsonapiPayloadFactory.toPayloadObject(program1, ImmutableSet.of(program2, program3));
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.newJsonapiPayload().setDataOne(mainObj);
    jsonapiPayloadFactory.addIncluded(jsonapiPayload, jsonapiPayloadFactory.toPayloadObject(program2));
    jsonapiPayloadFactory.addIncluded(jsonapiPayload, jsonapiPayloadFactory.toPayloadObject(program3));

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    JsonapiPayload resultJsonapiPayload = jsonapiPayloadFactory.deserialize(result);
    AssertPayload.assertPayload(resultJsonapiPayload)
      .hasIncluded("programs", ImmutableList.of(program2, program3))
      .hasDataOne("programs", program1.getId().toString());
  }

  @Test
  public void serializeMany() throws JsonapiException {
    JsonapiPayload jsonapiPayload = jsonapiPayloadFactory.newJsonapiPayload();
    Program accountA = new Program()
      .id(UUID.randomUUID())
      .name("Test Program A")
      ;
    Program accountB = new Program()
      .id(UUID.randomUUID())
      .name("Test Program B")
      ;
    Program accountC = new Program()
      .id(UUID.randomUUID())
      .name("Test Program C")
      ;
    jsonapiPayloadFactory.setDataEntities(jsonapiPayload, ImmutableList.of(accountA, accountB, accountC));

    String result = jsonapiPayloadFactory.serialize(jsonapiPayload);

    AssertPayload.assertPayload(jsonapiPayloadFactory.deserialize(result))
      .hasDataMany("programs", ImmutableList.of(
        accountA.getId().toString(),
        accountB.getId().toString(),
        accountC.getId().toString()))
      .hasIncluded("programs", ImmutableList.of());
  }

}
