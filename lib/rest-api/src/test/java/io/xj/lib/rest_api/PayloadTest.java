// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 Payload test
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class PayloadTest extends TestTemplate {
  private Payload subject;
  private PayloadFactory payloadFactory;

  @Before
  public void setUp() {
    Injector injector = Guice.createInjector(new RestApiModule());
    payloadFactory = injector.getInstance(PayloadFactory.class);
    payloadFactory.register(MockEntity.class);
    subject = payloadFactory.newPayload();
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
  public void isEmpty_falseAfterSetDataEntity() throws RestApiException {
    assertFalse(payloadFactory.setDataEntity(subject, createMockEntity("Test")).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetDataEntities() throws RestApiException {
    assertFalse(payloadFactory.setDataEntities(subject, ImmutableList.of(createMockEntity("Test"))).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetData_one() {
    assertFalse(subject.setDataOne(new PayloadObject()).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetData_many() {
    assertFalse(subject.setDataMany(ImmutableList.of(new PayloadObject())).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterAddData() {
    assertFalse(subject.addData(new PayloadObject()).isEmpty());
  }

  @Test
  public void isEmpty_falseAfterSetLinks() {
    assertFalse(subject.setLinks(ImmutableMap.of("One", "1", "Two", "2")).isEmpty());
  }

  @Test
  public void type_hasOne_afterSetDataEntity() throws RestApiException {
    assertEquals(PayloadDataType.HasOne, payloadFactory.setDataEntity(subject, createMockEntity("Test")).getDataType());
  }

  @Test
  public void type_hasMany_afterSetDataEntities() throws RestApiException {
    assertEquals(PayloadDataType.HasMany, payloadFactory.setDataEntities(subject, ImmutableList.of(createMockEntity("Test"))).getDataType());
  }

  @Test
  public void type_hasOne_afterSetData_one() {
    assertEquals(PayloadDataType.HasOne, subject.setDataOne(new PayloadObject()).getDataType());
  }

  @Test
  public void type_hasMany_afterSetData_many() {
    assertEquals(PayloadDataType.HasMany, subject.setDataMany(ImmutableList.of(new PayloadObject())).getDataType());
  }

  @Test
  public void type_hasMany_afterAddData() {
    assertEquals(PayloadDataType.HasMany, subject.addData(new PayloadObject()).getDataType());
  }

  @Test
  public void setType() {
    assertEquals(PayloadDataType.HasOne, subject.setDataType(PayloadDataType.HasOne).getDataType());
  }

  @Test
  public void addIncluded() throws RestApiException {
    assertEquals(1, payloadFactory.addIncluded(subject, new PayloadObject()).getIncluded().size());
  }

  @Test
  public void setIncluded() {
    assertEquals(1, subject.setIncluded(ImmutableList.of(new PayloadObject())).getIncluded().size());
  }

  /**
   Serialize a payload comprising a MockEntity

   @throws RestApiException on failure
   */
  @Test
  public void setDataOne_program() throws RestApiException {
    MockEntity entity1 = createMockEntity("Test");
    MockEntity entity2 = createMockEntity("Shim");

    payloadFactory.setDataEntity(subject, entity1);
    payloadFactory.addIncluded(subject, payloadFactory.toPayloadObject(entity2));

    assertTrue(subject.getLinks().isEmpty());
    assertTrue(subject.getDataOne().isPresent());
    assertEquals("mock-entities", subject.getDataOne().get().getType());
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
  public void setDataEntities_empty_setsDataTypeToHasMany() throws RestApiException {
    payloadFactory.setDataEntities(subject, ImmutableList.of());

    assertEquals(PayloadDataType.HasMany, subject.getDataType());
  }


}
