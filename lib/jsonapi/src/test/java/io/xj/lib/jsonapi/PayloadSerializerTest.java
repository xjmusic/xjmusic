// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.lib.entity.EntityFactory;
import org.junit.Before;
import org.junit.Test;

/**
 Payload serializer test
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class PayloadSerializerTest extends TestTemplate {
  private PayloadFactory payloadFactory;
  private EntityFactory entityFactory;

  @Before
  public void setUp() {
    Injector injector = Guice.createInjector(new JsonApiModule());
    entityFactory = injector.getInstance(EntityFactory.class);
    payloadFactory = injector.getInstance(PayloadFactory.class);
    entityFactory.register(MockEntity.class);
  }

  @Test
  public void serialize() throws JsonApiException {
    MockEntity mockEntity = createMockEntity("test_print");
    Payload payload = payloadFactory.setDataEntity(payloadFactory.newPayload(), mockEntity);

    String result = payloadFactory.serialize(payload);

    AssertPayload.assertPayload(payloadFactory.deserialize(result))
      .hasDataOne("mock-entities", mockEntity.getId().toString());
  }

  @Test
  public void serializeOne() throws JsonApiException {
    Payload payload = payloadFactory.newPayload();
    MockEntity mockParentEntity = createMockEntity("Test MockEntity");
    payloadFactory.setDataEntity(payload, mockParentEntity);

    String result = payloadFactory.serialize(payload);

    AssertPayload.assertPayload(payloadFactory.deserialize(result))
      .hasDataOne("mock-entities", mockParentEntity.getId().toString());
  }

  @Test
  public void serializeOne_withBelongsTo() throws JsonApiException {
    entityFactory.register("MockEntity").belongsTo("MockEntity");
    Payload payload = payloadFactory.newPayload();
    MockEntity mockParentEntity = createMockEntity("y");
    MockEntity mockEntity = createMockEntity("x", mockParentEntity);
    payloadFactory.setDataEntity(payload, mockEntity);

    String result = payloadFactory.serialize(payload);

    AssertPayload.assertPayload(payloadFactory.deserialize(result))
      .hasDataOne("mock-entities", mockEntity.getId().toString())
      .belongsTo("MockEntity", mockParentEntity.getId().toString());
  }

  @Test
  public void serializeOne_withHasMany() throws JsonApiException {
    entityFactory.register("MockEntity").hasMany("MockEntity");
    MockEntity mockEntity = createMockEntity("x", createMockEntity("y"));
    MockEntity mockEntity1 = createMockEntity("b", mockEntity);
    MockEntity mockEntity2 = createMockEntity("c", mockEntity);
    PayloadObject mainObj = payloadFactory.toPayloadObject(mockEntity, ImmutableSet.of(mockEntity1, mockEntity2));
    Payload payload = payloadFactory.newPayload().setDataOne(mainObj);
    payloadFactory.addIncluded(payload, payloadFactory.toPayloadObject(mockEntity1));
    payloadFactory.addIncluded(payload, payloadFactory.toPayloadObject(mockEntity2));

    String result = payloadFactory.serialize(payload);

    Payload resultPayload = payloadFactory.deserialize(result);
    AssertPayload.assertPayload(resultPayload)
      .hasIncluded("mock-entities", ImmutableList.of(mockEntity1, mockEntity2))
      .hasDataOne("mock-entities", mockEntity.getId().toString());
  }

  @Test
  public void serializeMany() throws JsonApiException {
    Payload payload = payloadFactory.newPayload();
    MockEntity accountA = createMockEntity("Test MockEntity A");
    MockEntity accountB = createMockEntity("Test MockEntity B");
    MockEntity accountC = createMockEntity("Test MockEntity C");
    payloadFactory.setDataEntities(payload, ImmutableList.of(accountA, accountB, accountC));

    String result = payloadFactory.serialize(payload);

    AssertPayload.assertPayload(payloadFactory.deserialize(result))
      .hasDataMany("mock-entities", ImmutableList.of(
        accountA.getId().toString(),
        accountB.getId().toString(),
        accountC.getId().toString()))
      .hasIncluded("mock-entities", ImmutableList.of());
  }

}
