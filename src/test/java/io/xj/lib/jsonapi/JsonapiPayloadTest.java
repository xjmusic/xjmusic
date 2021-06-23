// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.Program;
import io.xj.lib.entity.EntityFactory;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 Payload test
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class JsonapiPayloadTest {
  private JsonapiPayload subject;
  private JsonapiPayloadFactory jsonapiPayloadFactory;

  @Before
  public void setUp() {
    var injector = Guice.createInjector(new JsonApiModule(), new AbstractModule() {
      @Override
      protected void configure() {
        bind(Config.class).toInstance(ConfigFactory.empty());
      }
    });
    var entityFactory = injector.getInstance(EntityFactory.class);
    jsonapiPayloadFactory = injector.getInstance(JsonapiPayloadFactory.class);
    entityFactory.register(Program.class);
    subject = jsonapiPayloadFactory.newJsonapiPayload();
  }

  @Test
  public void isEmpty() {
    assertTrue(subject.isEmpty());
    assertEquals(PayloadDataType.Ambiguous, subject.getDataType());
    assertFalse(subject.getDataOne().isPresent());
    assertTrue(subject.getDataMany().isEmpty());
    assertTrue(subject.getLinks().isEmpty());
    assertTrue(subject.getIncluded().isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetDataEntity() throws JsonApiException {
    assertFalse(jsonapiPayloadFactory.setDataEntity(subject, Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("Test")
      .build()).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetDataEntities() throws JsonApiException {
    assertFalse(jsonapiPayloadFactory.setDataEntities(subject, ImmutableList.of(Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("Test")
      .build())).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetData_one() {
    assertFalse(subject.setDataOne(new JsonapiPayloadObject()).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetData_many() {
    assertFalse(subject.setDataMany(ImmutableList.of(new JsonapiPayloadObject())).isEmpty());
  }

  @Test
  public void setDataMany_setsTypeEvenWithEmptyList() {
    assertEquals(PayloadDataType.Many, subject.setDataMany(ImmutableList.of()).getDataType());
  }

  @Test
  public void isEmpty_falseAfterAddData() {
    assertFalse(subject.addData(new JsonapiPayloadObject()).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetLinks() {
    assertFalse(subject.setLinks(ImmutableMap.of("One", "1", "Two", "2")).isEmpty());
  }

  @Test
  public void type_hasOne_afterSetDataEntity() throws JsonApiException {
    assertEquals(PayloadDataType.One, jsonapiPayloadFactory.setDataEntity(subject,
      Program.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setName("Test")
        .build()).getDataType());
  }

  @Test
  public void type_hasMany_afterSetDataEntities() throws JsonApiException {
    assertEquals(PayloadDataType.Many, jsonapiPayloadFactory.setDataEntities(subject,
      ImmutableList.of(Program.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setName("Test")
        .build())).getDataType());
  }

  @Test
  public void type_hasOne_afterSetData_one() {
    assertEquals(PayloadDataType.One, subject.setDataOne(new JsonapiPayloadObject()).getDataType());
  }

  @Test
  public void type_hasMany_afterSetData_many() {
    assertEquals(PayloadDataType.Many, subject.setDataMany(ImmutableList.of(new JsonapiPayloadObject())).getDataType());
  }

  @Test
  public void type_hasMany_afterAddData() {
    assertEquals(PayloadDataType.Many, subject.addData(new JsonapiPayloadObject()).getDataType());
  }

  @Test
  public void setType() {
    assertEquals(PayloadDataType.One, subject.setDataType(PayloadDataType.One).getDataType());
  }

  @Test
  public void addIncluded() throws JsonApiException {
    assertEquals(1, jsonapiPayloadFactory.addIncluded(subject, new JsonapiPayloadObject()).getIncluded().size());
  }

  @Test
  public void setIncluded() {
    assertEquals(1, subject.setIncluded(ImmutableList.of(new JsonapiPayloadObject())).getIncluded().size());
  }

  @Test
  public void addToIncluded() {
    assertEquals(1, subject.addToIncluded(new JsonapiPayloadObject()).getIncluded().size());
  }

  @Test
  public void addAllToIncluded() {
    assertEquals(2, subject.addAllToIncluded(ImmutableList.of(new JsonapiPayloadObject(), new JsonapiPayloadObject())).getIncluded().size());
  }

  /**
   Serialize a payload comprising a Program

   @throws JsonApiException on failure
   */
  @Test
  public void setDataOne_program() throws JsonApiException {
    Program entity1 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("Test")
      .build();
    Program entity2 = Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName("Shim")
      .build();

    jsonapiPayloadFactory.setDataEntity(subject, entity1);
    jsonapiPayloadFactory.addIncluded(subject, jsonapiPayloadFactory.toPayloadObject(entity2));

    assertTrue(subject.getLinks().isEmpty());
    assertTrue(subject.getDataOne().isPresent());
    assertEquals("programs", subject.getDataOne().get().getType());
    assertEquals(1, subject.getIncluded().size());
  }

  @Test
  public void setSelfURI() {
    assertEquals("https://www.example.com/api/1/things", subject.setSelfURI(URI.create("https://www.example.com/api/1/things")).getLinks().get("self"));
  }

  @Test
  public void getSelfURI() {
    subject.getLinks().put("self", "https://www.example.com/api/1/things");

    assertEquals(Optional.of(URI.create("https://www.example.com/api/1/things")), subject.getSelfURI());
  }

  @Test
  public void setDataEntities_empty_setsDataTypeToHasMany() throws JsonApiException {
    jsonapiPayloadFactory.setDataEntities(subject, ImmutableList.of());

    assertEquals(PayloadDataType.Many, subject.getDataType());
  }

  @Test
  public void addData() {
  }

  @Test
  public void addError() {
  }

  @Test
  public void clear() {
  }

  @Test
  public void getDataMany() {
  }

  @Test
  public void setDataMany() {
  }

  @Test
  public void getDataOne() {
  }

  @Test
  public void setDataOne() {
  }

  @Test
  public void getDataType() {
  }

  @Test
  public void setDataType() {
  }

  @Test
  public void getErrors() {
  }

  @Test
  public void getIncluded() {
  }

  @Test
  public void getIncludedOfType() {
  }

  @Test
  public void getLinks() {
  }

  @Test
  public void setLinks() {
  }

  @Test
  public void setDataReference() {
  }

  @Test
  public void addToDataMany() {
  }

  @Test
  public void hasDataMany() {
  }

  @Test
  public void hasDataManyEmpty() {
  }

  @Test
  public void hasDataOne() {
  }

  @Test
  public void testHasDataOne() {
  }

  @Test
  public void testHasDataOne1() {
  }

  @Test
  public void hasDataOneEmpty() {
  }

  @Test
  public void hasErrorCount() {
  }

  @Test
  public void hasIncluded() {
  }

}
