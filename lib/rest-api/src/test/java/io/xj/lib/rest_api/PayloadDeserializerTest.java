// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.rest_api;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.UUID;

import static io.xj.lib.rest_api.AssertPayload.assertPayload;

/**
 Payload deserializer test
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public class PayloadDeserializerTest extends TestTemplate {
  private PayloadFactory payloadFactory;

  @Before
  public void setUp() {
    Injector injector = Guice.createInjector(new RestApiModule());
    payloadFactory = injector.getInstance(PayloadFactory.class);
  }

  @Test
  public void deserializeOneIncludingEmbeddedEntities() throws IOException, RestApiException {
    Payload result = payloadFactory.deserialize(InternalResources.readResourceFile("payload/deserializeOneIncludingEmbeddedEntities.json"));

    assertPayload(result)
            .hasDataOne("mock-entities", "805cf759-4e94-4275-a82d-5255c9e69347")
            .belongsTo("MockParentEntity", "f94290f4-537c-4444-92e5-dc0b0df352e5")
            .hasMany("MockChildEntity", ImmutableList.of(createMockEntity(UUID.fromString("9b862c2f-192f-4041-b849-442a2ec50218"))));
  }

  @Test
  public void deserializeOneWithRelationship() throws IOException {
    Payload result = null;
    try {
      result = payloadFactory.deserialize(InternalResources.readResourceFile("payload/deserializeOneWithRelationship.json"));
    } catch (RestApiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
            .hasDataOne("mock-entities", "a1acee68-ade3-465c-8b07-596b179e1a51")
            .belongsTo("MockParentEntity", "aec6e236-322b-4c66-bd48-8c7ce6a1ef4a");
  }

  @Test
  public void deserializeOne() throws IOException {
    Payload result = null;
    try {
      result = payloadFactory.deserialize(InternalResources.readResourceFile("payload/deserializeOne.json"));
    } catch (RestApiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
            .hasDataOne("mock-entities", "19f3fced-2c3f-405d-bde8-f7ef71f9ad9d");
  }

  @Test
  public void deserializeErrors() throws IOException {
    Payload result = null;
    try {
      result = payloadFactory.deserialize(InternalResources.readResourceFile("payload/deserializeErrors.json"));
    } catch (RestApiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
            .hasErrorCount(1);
  }

  @Test
  public void deserializeOneWithNullAttributeValue() throws IOException {
    Payload result = null;
    try {
      result = payloadFactory.deserialize(InternalResources.readResourceFile("payload/deserializeOneWithNullAttributeValue.json"));
    } catch (RestApiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
            .hasDataOne("mock-entities", "5d5db584-0530-4477-8a5b-9429e779316e");
  }

  @Test
  public void deserializeMany() throws IOException {
    Payload result = null;
    try {
      result = payloadFactory.deserialize(InternalResources.readResourceFile("payload/deserializeMany.json"));
    } catch (RestApiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
            .hasDataMany("mock-entities", ImmutableList.of("716f0033-e7fe-4242-b993-1f840e4a242f"));
  }

  @Test
  public void deserializeMany_emptyTypeHasMany() throws IOException {
    String json = "{\"data\":[]}";

    Payload result = null;
    try {
      result = payloadFactory.deserialize(json);
    } catch (RestApiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
            .hasDataManyEmpty();
  }

  @Test
  public void deserializeOne_nullDataSetsTypeToHasOne() throws IOException {
    String json = "{\"data\":null}";

    Payload result = null;
    try {
      result = payloadFactory.deserialize(json);
    } catch (RestApiException e) {
      e.printStackTrace();
    }

    assertPayload(result)
            .hasDataOneEmpty();
  }
}
