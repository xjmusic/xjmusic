// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.lib.jsonapi;

import io.xj.lib.Widget;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.JsonProviderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Payload test
 * <p>
 * Created by Charney Kaye on 2020/03/09
 */
public class JsonapiPayloadTest {
  JsonapiPayload subject;
  JsonapiPayloadFactory jsonapiPayloadFactory;

  @BeforeEach
  public void setUp() {
    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    entityFactory.register(Widget.class);
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
  public void isEmpty_falseAfterSetDataEntity() throws JsonapiException {
    assertFalse(jsonapiPayloadFactory.setDataEntity(subject, new Widget()
      .setId(UUID.randomUUID())
      .setName("Test")
    ).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetDataEntities() throws JsonapiException {
    assertFalse(jsonapiPayloadFactory.setDataEntities(subject, List.of(new Widget()
      .setId(UUID.randomUUID())
      .setName("Test")
    )).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetData_one() {
    assertFalse(subject.setDataOne(new JsonapiPayloadObject()).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetData_many() {
    assertFalse(subject.setDataMany(List.of(new JsonapiPayloadObject())).isEmpty());
  }

  @Test
  public void setDataMany_setsTypeEvenWithEmptyList() {
    assertEquals(PayloadDataType.Many, subject.setDataMany(List.of()).getDataType());
  }

  @Test
  public void isEmpty_falseAfterAddData() {
    assertFalse(subject.addData(new JsonapiPayloadObject()).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetLinks() {
    assertFalse(subject.setLinks(Map.of("One", "1", "Two", "2")).isEmpty());
  }

  @Test
  public void type_hasOne_afterSetDataEntity() throws JsonapiException {
    assertEquals(PayloadDataType.One, jsonapiPayloadFactory.setDataEntity(subject,
      new Widget()
        .setId(UUID.randomUUID())
        .setName("Test")
    ).getDataType());
  }

  @Test
  public void type_hasMany_afterSetDataEntities() throws JsonapiException {
    assertEquals(PayloadDataType.Many, jsonapiPayloadFactory.setDataEntities(subject,
      List.of(new Widget()
        .setId(UUID.randomUUID())
        .setName("Test")
      )).getDataType());
  }

  @Test
  public void type_hasOne_afterSetData_one() {
    assertEquals(PayloadDataType.One, subject.setDataOne(new JsonapiPayloadObject()).getDataType());
  }

  @Test
  public void type_hasMany_afterSetData_many() {
    assertEquals(PayloadDataType.Many, subject.setDataMany(List.of(new JsonapiPayloadObject())).getDataType());
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
  public void addIncluded() throws JsonapiException {
    assertEquals(1, jsonapiPayloadFactory.addIncluded(subject, new JsonapiPayloadObject()).getIncluded().size());
  }

  @Test
  public void setIncluded() {
    assertEquals(1, subject.setIncluded(List.of(new JsonapiPayloadObject())).getIncluded().size());
  }

  @Test
  public void addToIncluded() {
    assertEquals(1, subject.addToIncluded(new JsonapiPayloadObject()).getIncluded().size());
  }

  @Test
  public void addAllToIncluded() {
    assertEquals(2, subject.addAllToIncluded(List.of(new JsonapiPayloadObject(), new JsonapiPayloadObject())).getIncluded().size());
  }

  /**
   * Serialize a payload comprising a Widget
   *
   * @throws JsonapiException on failure
   */
  @Test
  public void setDataOne_widget() throws JsonapiException {
    Widget entity1 = new Widget()
      .setId(UUID.randomUUID())
      .setName("Test");
    Widget entity2 = new Widget()
      .setId(UUID.randomUUID())
      .setName("Shim");

    jsonapiPayloadFactory.setDataEntity(subject, entity1);
    jsonapiPayloadFactory.addIncluded(subject, jsonapiPayloadFactory.toPayloadObject(entity2));

    assertTrue(subject.getLinks().isEmpty());
    assertTrue(subject.getDataOne().isPresent());
    assertEquals("widgets", subject.getDataOne().get().getType());
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
  public void setDataEntities_empty_setsDataTypeToHasMany() throws JsonapiException {
    jsonapiPayloadFactory.setDataEntities(subject, List.of());

    assertEquals(PayloadDataType.Many, subject.getDataType());
  }
}
