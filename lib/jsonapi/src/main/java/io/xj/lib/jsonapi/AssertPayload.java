// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.util.ValueException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

import static io.xj.lib.util.Assert.assertEquals;
import static io.xj.lib.util.Assert.assertFalse;
import static io.xj.lib.util.Assert.assertTrue;


/**
 Assertion utilities for testing Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
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
   @throws JsonApiException if assertion fails
   */
  public AssertPayload hasDataMany(String resourceType, Collection<String> resourceIds) throws JsonApiException {
    try {
      assertEquals("payload data type", PayloadDataType.Many, payload.getDataType());
      assertEquals("same number of ids", resourceIds.size(), payload.getDataMany().size());
      Collection<String> foundIds = Lists.newArrayList();
      for (PayloadObject payloadObject : payload.getDataMany()) {
        assertFalse("already found id", foundIds.contains(payloadObject.getId()));
        assertEquals(String.format("unexpected %s", payloadObject.getType()), resourceType, payloadObject.getType());
        assertTrue(String.format("unexpected %s id=%s", resourceType, payloadObject.getId()), resourceIds.contains(payloadObject.getId()));
        foundIds.add(payloadObject.getId());
      }
      return this;

    } catch (ValueException e) {
      throw new JsonApiException(e);
    }
  }

  /**
   Assert the Payload has-many data, with an empty set

   @throws JsonApiException if assertion fails
   */
  public void hasDataManyEmpty() throws JsonApiException {
    try {
      assertEquals("payload data type", PayloadDataType.Many, payload.getDataType());
      assertEquals("empty set", 0, payload.getDataMany().size());

    } catch (ValueException e) {
      throw new JsonApiException(e);
    }
  }

  /**
   Assert the Payload has-one data, with the specified class + id

   @param resourceType to assert
   @param resourceId   to assert
   @return PayloadObject assertion utility, for further assertions on that payload object
   @throws JsonApiException if assertion fails
   */
  public AssertPayloadObject hasDataOne(String resourceType, String resourceId) throws JsonApiException {
    try {
      assertEquals("payload data type", PayloadDataType.One, payload.getDataType());
      Optional<PayloadObject> dataOne = payload.getDataOne();
      assertTrue("has one data", dataOne.isPresent());
      assertEquals("payload object type", resourceType, dataOne.orElseThrow().getType());
      assertEquals("payload object id", resourceId, dataOne.get().getId());
      return new AssertPayloadObject(dataOne.get());

    } catch (ValueException e) {
      throw new JsonApiException(e);
    }
  }

  /**
   Assert the Payload has-one data, with the specified class + id

   @param resource to assert
   @return PayloadObject assertion utility, for further assertions on that payload object
   @throws JsonApiException if assertion fails
   */
  public <N> AssertPayloadObject hasDataOne(N resource) throws JsonApiException {
    try {
      assertEquals("payload data type", PayloadDataType.One, payload.getDataType());
      Optional<PayloadObject> dataOne = payload.getDataOne();
      assertTrue("has one data", dataOne.isPresent());
      assertTrue(String.format("one data same as %s id=%s", Entities.toType(resource), Entities.getId(resource)), dataOne.orElseThrow().isSame(resource));
      return new AssertPayloadObject(dataOne.get());

    } catch (EntityException | ValueException e) {
      throw new JsonApiException(e);
    }
  }

  /**
   Assert the Payload has-one data, with empty (null) specified

   @throws JsonApiException if assertion fails
   */
  public void hasDataOneEmpty() throws JsonApiException {
    try {
      assertEquals("payload data type", PayloadDataType.One, payload.getDataType());
      Optional<PayloadObject> dataOne = payload.getDataOne();
      assertTrue("has empty data", dataOne.isEmpty());

    } catch (ValueException e) {
      throw new JsonApiException(e);
    }
  }

  /**
   Assert has specified number of errors

   @param errorCount to assert
   @return this Payload assertion utility (for chaining methods)
   @throws JsonApiException if assertion fails
   */
  public AssertPayload hasErrorCount(int errorCount) throws JsonApiException {
    try {
      assertEquals("payload errors count", errorCount, payload.getErrors().size());
      return this;

    } catch (ValueException e) {
      throw new JsonApiException(e);
    }
  }

  /**
   Assert has included entity, and return a payload object assertion utility to make assertions about it

   @param resource to assert is included
   @param <N>      type of resource
   @return payload object assertion utility
   */
  public <N> AssertPayloadObject hasIncluded(N resource) throws JsonApiException {
    try {
      Optional<PayloadObject> payloadObject = payload.getIncluded().stream().filter(obj -> obj.isSame(resource)).findFirst();
      assertTrue(String.format("has included %s id=%s", Entities.toType(resource), Entities.getId(resource)), payloadObject.isPresent());
      assertTrue("payload object exists", payloadObject.isPresent());
      return new AssertPayloadObject(payloadObject.orElseThrow());

    } catch (EntityException | ValueException e) {
      throw new JsonApiException(e);
    }
  }

  /**
   Assert has included entities

   @param resourceType to assert
   @param resources    to assert
   @return this Payload assertion utility (for chaining methods)
   */
  public <N> AssertPayload hasIncluded(String resourceType, ImmutableList<N> resources) throws JsonApiException {
    try {
      Collection<PayloadObject> included = payload.getIncludedOfType(resourceType);
      assertEquals("same number of ids", resources.size(), included.size());
      Collection<String> foundIds = Lists.newArrayList();
      Collection<String> resourceIds = new ArrayList<>();
      for (N resource : resources) {
        String resourceId = Entities.getId(resource);
        resourceIds.add(resourceId);
      }
      for (PayloadObject payloadObject : included) {
        assertFalse("already found id", foundIds.contains(payloadObject.getId()));
        assertEquals(String.format("unexpected %s", payloadObject.getType()), resourceType, payloadObject.getType());
        assertTrue(String.format("unexpected %s id=%s", resourceType, payloadObject.getId()), resourceIds.contains(payloadObject.getId()));
        foundIds.add(payloadObject.getId());
      }
      return this;
    } catch (EntityException | ValueException e) {
      throw new JsonApiException(e);
    }
  }

}
