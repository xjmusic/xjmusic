// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.jsonapi;

import io.xj.hub.util.ValueException;
import io.xj.nexus.entity.EntityUtils;
import io.xj.nexus.entity.EntityException;

import java.util.*;

import static io.xj.hub.util.Assertion.*;


/**
 Assertion utilities for testing Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public class AssertPayload {
  final JsonapiPayload jsonapiPayload;

  /**
   of instance of payload assertion utility object

   @param jsonapiPayload to make assertions on
   */
  public AssertPayload(JsonapiPayload jsonapiPayload) {
    this.jsonapiPayload = jsonapiPayload;
  }

  /**
   of instance of payload assertion utility object of Payload

   @param jsonapiPayload to make assertions on
   @return payload assertion utility
   */
  public static AssertPayload assertPayload(JsonapiPayload jsonapiPayload) {
    return new AssertPayload(jsonapiPayload);
  }

  /**
   Assert the Payload has-many data, with the specified class + ids

   @param resourceType to assert
   @param resourceIds  to assert
   @return this Payload assertion utility (for chaining methods)
   @throws JsonapiException if assertion fails
   */
  public AssertPayload hasDataMany(String resourceType, Collection<String> resourceIds) throws JsonapiException {
    try {
      assertEquality("payload data type", PayloadDataType.Many, jsonapiPayload.getDataType());
      assertEquality("same number of ids", resourceIds.size(), jsonapiPayload.getDataMany().size());
      Collection<String> foundIds = new ArrayList<>();
      for (JsonapiPayloadObject jsonapiPayloadObject : jsonapiPayload.getDataMany()) {
        assertFalse("already found id", foundIds.contains(jsonapiPayloadObject.getId()));
        assertEquality(String.format("unexpected %s", jsonapiPayloadObject.getType()), resourceType, jsonapiPayloadObject.getType());
        assertTrue(String.format("unexpected %s id=%s", resourceType, jsonapiPayloadObject.getId()), resourceIds.contains(jsonapiPayloadObject.getId()));
        foundIds.add(jsonapiPayloadObject.getId());
      }
      return this;

    } catch (ValueException e) {
      throw new JsonapiException(e);
    }
  }

  /**
   Assert the Payload has-many data, with an empty set

   @throws JsonapiException if assertion fails
   */
  public void hasDataManyEmpty() throws JsonapiException {
    try {
      assertEquality("payload data type", PayloadDataType.Many, jsonapiPayload.getDataType());
      assertEquality("empty set", 0, jsonapiPayload.getDataMany().size());

    } catch (ValueException e) {
      throw new JsonapiException(e);
    }
  }

  /**
   Assert the Payload has-one data, with the specified class + id

   @param resourceType to assert
   @param resourceId   to assert
   @return PayloadObject assertion utility, for further assertions on that payload object
   @throws JsonapiException if assertion fails
   */
  public AssertPayloadObject hasDataOne(String resourceType, String resourceId) throws JsonapiException {
    try {
      assertEquality("payload data type", PayloadDataType.One, jsonapiPayload.getDataType());
      Optional<JsonapiPayloadObject> dataOne = jsonapiPayload.getDataOne();
      if (dataOne.isEmpty())
        throw new JsonapiException(String.format("payload data one is empty, expected %s id=%s", resourceType, resourceId));
      assertEquality("payload object type", resourceType, dataOne.get().getType());
      assertEquality("payload object id", resourceId, dataOne.get().getId());
      return new AssertPayloadObject(dataOne.get());

    } catch (ValueException e) {
      throw new JsonapiException(e);
    }
  }

  /**
   Assert the Payload has-one data, with the specified class + id

   @param resource to assert
   @throws JsonapiException if assertion fails
   */
  public <N> void hasDataOne(N resource) throws JsonapiException {
    try {
      assertEquality("payload data type", PayloadDataType.One, jsonapiPayload.getDataType());
      Optional<JsonapiPayloadObject> dataOne = jsonapiPayload.getDataOne();
      if (dataOne.isEmpty())
        throw new JsonapiException(String.format("payload data one is empty, expected class %s", resource.getClass().getName()));
      assertTrue(String.format("one data same as %s id=%s", EntityUtils.toType(resource), EntityUtils.getId(resource)), dataOne.get().isSame(resource));
      new AssertPayloadObject(dataOne.get());

    } catch (EntityException | ValueException e) {
      throw new JsonapiException(e);
    }
  }

  /**
   Assert the Payload has-one data, with empty (null) specified

   @throws JsonapiException if assertion fails
   */
  public void hasDataOneEmpty() throws JsonapiException {
    try {
      assertEquality("payload data type", PayloadDataType.One, jsonapiPayload.getDataType());
      Optional<JsonapiPayloadObject> dataOne = jsonapiPayload.getDataOne();
      assertTrue("has empty data", dataOne.isEmpty());

    } catch (ValueException e) {
      throw new JsonapiException(e);
    }
  }

  /**
   Assert has included entities

   @param resourceType to assert
   @param resources    to assert
   @return this Payload assertion utility (for chaining methods)
   */
  public <N> AssertPayload hasIncluded(String resourceType, List<N> resources) throws JsonapiException {
    try {
      Collection<JsonapiPayloadObject> included = jsonapiPayload.getIncludedOfType(resourceType);
      assertEquality("same number of ids", resources.size(), included.size());
      Collection<String> foundIds = new ArrayList<>();
      Collection<String> resourceIds = new ArrayList<>();
      for (N resource : resources) {
        var resourceId = EntityUtils.getId(resource);
        if (Objects.nonNull(resourceId))
          resourceIds.add(resourceId.toString());
      }
      for (JsonapiPayloadObject jsonapiPayloadObject : included) {
        assertFalse("already found id", foundIds.contains(jsonapiPayloadObject.getId()));
        assertEquality(String.format("unexpected %s", jsonapiPayloadObject.getType()), resourceType, jsonapiPayloadObject.getType());
        assertTrue(String.format("unexpected %s id=%s", resourceType, jsonapiPayloadObject.getId()), resourceIds.contains(jsonapiPayloadObject.getId()));
        foundIds.add(jsonapiPayloadObject.getId());
      }
      return this;
    } catch (EntityException | ValueException e) {
      throw new JsonapiException(e);
    }
  }

}
