// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.xj.lib.rest_api.Payload;
import io.xj.lib.rest_api.PayloadDataType;
import io.xj.lib.rest_api.PayloadEntity;
import io.xj.lib.rest_api.PayloadObject;
import io.xj.lib.rest_api.RestApiException;
import io.xj.service.hub.entity.Entity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 [#167276586] JSON API facilitates complex transactions
 */
public class AssertPayload {
  private final Payload payload;

  /**
   of instance of payload assertion utility object

   @param payload to make assertions on
   */
  public AssertPayload(Payload payload) {
    this.payload = payload;
  }

  /**
   of instance of payload assertion utility object of Payload

   @param payload to make assertions on
   @return payload assertion utility
   */
  public static AssertPayload assertPayload(Payload payload) {
    return new AssertPayload(payload);
  }

  /**
   Assert the Payload has-many data, with the specified class + ids

   @param resourceType to assert
   @param resourceIds  to assert
   @return this Payload assertion utility (for chaining methods)
   */
  public AssertPayload hasDataMany(String resourceType, Collection<String> resourceIds) {
    assertEquals("payload data type", PayloadDataType.HasMany, payload.getDataType());
    assertEquals("same number of ids", resourceIds.size(), payload.getDataMany().size());
    Collection<String> foundIds = Lists.newArrayList();
    payload.getDataMany().forEach(payloadObject -> {
      assertFalse("already found id", foundIds.contains(payloadObject.getId()));
      assertEquals(String.format("unexpected %s", payloadObject.getType()), resourceType, payloadObject.getType());
      assertTrue(String.format("unexpected %s id=%s", resourceType, payloadObject.getId()), resourceIds.contains(payloadObject.getId()));
      foundIds.add(payloadObject.getId());
    });
    return this;
  }

  /**
   Assert the Payload has-many data, with an empty set
   */
  public void hasDataManyEmpty() {
    assertEquals("payload data type", PayloadDataType.HasMany, payload.getDataType());
    assertEquals("empty set", 0, payload.getDataMany().size());
  }

  /**
   Assert the Payload has-one data, with the specified class + id

   @param resourceType to assert
   @param resourceId   to assert
   @return PayloadObject assertion utility, for further assertions on that payload object
   */
  public AssertPayloadObject hasDataOne(String resourceType, String resourceId) {
    assertEquals("payload data type", PayloadDataType.HasOne, payload.getDataType());
    Optional<PayloadObject> dataOne = payload.getDataOne();
    assertTrue("has one data", dataOne.isPresent());
    assertEquals("payload object type", resourceType, dataOne.get().getType());
    assertEquals("payload object id", resourceId, dataOne.get().getId());
    return new AssertPayloadObject(dataOne.get());
  }

  /**
   Assert the Payload has-one data, with the specified class + id

   @param resource to assert
   @return PayloadObject assertion utility, for further assertions on that payload object
   */
  public <N extends Entity> AssertPayloadObject hasDataOne(N resource) throws RestApiException {
    assertEquals("payload data type", PayloadDataType.HasOne, payload.getDataType());
    Optional<PayloadObject> dataOne = payload.getDataOne();
    assertTrue("has one data", dataOne.isPresent());
    assertTrue(String.format("one data same as %s id=%s", PayloadEntity.getResourceType(resource), PayloadEntity.getResourceId(resource)), dataOne.get().isSame(resource));
    return new AssertPayloadObject(dataOne.get());
  }

  /**
   Assert the Payload has-one data, with empty (null) specified
   */
  public void hasDataOneEmpty() {
    assertEquals("payload data type", PayloadDataType.HasOne, payload.getDataType());
    Optional<PayloadObject> dataOne = payload.getDataOne();
    assertTrue("has empty data", dataOne.isEmpty());
  }

  /**
   Assert has specified number of errors

   @param errorCount to assert
   @return this Payload assertion utility (for chaining methods)
   */
  public AssertPayload hasErrorCount(int errorCount) {
    assertEquals("payload errors count", errorCount, payload.getErrors().size());
    return this;
  }

  /**
   Assert has included entity, and return a payload object assertion utility to make assertions about it

   @param resource to assert is included
   @param <N>      type of resource
   @return payload object assertion utility
   */
  public <N extends Entity> AssertPayloadObject hasIncluded(N resource) throws RestApiException {
    Optional<PayloadObject> payloadObject = payload.getIncluded().stream().filter(obj -> obj.isSame(resource)).findFirst();
    assertTrue(String.format("has included %s id=%s", PayloadEntity.getResourceType(resource), PayloadEntity.getResourceId(resource)), payloadObject.isPresent());
    return new AssertPayloadObject(payloadObject.get());
  }

  /**
   Assert has included entities

   @param resourceType to assert
   @param resources    to assert
   @return this Payload assertion utility (for chaining methods)
   */
  public <N extends Entity> AssertPayload hasIncluded(String resourceType, ImmutableList<N> resources) throws RestApiException {
    Collection<PayloadObject> included = payload.getIncludedOfType(resourceType);
    assertEquals("same number of ids", resources.size(), included.size());
    Collection<String> foundIds = Lists.newArrayList();
    Collection<String> resourceIds = new ArrayList<>();
    for (N resource : resources) {
      String resourceId = PayloadEntity.getResourceId(resource);
      resourceIds.add(resourceId);
    }
    included.forEach(payloadObject -> {
      assertFalse("already found id", foundIds.contains(payloadObject.getId()));
      assertEquals(String.format("unexpected %s", payloadObject.getType()), resourceType, payloadObject.getType());
      assertTrue(String.format("unexpected %s id=%s", resourceType, payloadObject.getId()), resourceIds.contains(payloadObject.getId()));
      foundIds.add(payloadObject.getId());
    });
    return this;
  }

}
