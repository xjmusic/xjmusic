// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.util.ValueException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.xj.lib.util.Assert.assertTrue;

/**
 Assertion utilities for testing Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public class AssertPayloadObject {
  private final PayloadObject payloadObject;

  /**
   of instance of payloadObject assertion utility object

   @param payloadObject to make assertions on
   */
  public AssertPayloadObject(PayloadObject payloadObject) {
    this.payloadObject = payloadObject;
  }

  /**
   of instance of payloadObject assertion utility object of JSON string

   @param payloadObject to parse
   @return payloadObject assertion utility
   @throws IOException on failure to parse JSON
   */
  public static AssertPayloadObject assertPayloadObject(PayloadObject payloadObject) throws IOException {
    return new AssertPayloadObject(payloadObject);
  }

  /**
   Assert has a belongs-to relationship to the specified type (by class) and id

   @param type of relationship
   @param id   of relationship
   @return payloadObject assertion utility (for chaining methods)
   @throws JsonApiException if assertion fails
   */
  public AssertPayloadObject belongsTo(Class<?> type, String id) throws JsonApiException, ValueException {
    String key = Entities.toBelongsTo(type);
    assertTrue(String.format("Belongs to %s id=%s", type, id), payloadObject.getRelationships().containsKey(key));
    new AssertPayload(payloadObject.getRelationships().get(key))
      .hasDataOne(Entities.toType(type), id);
    return this;
  }

  /**
   Assert has a belongs-to relationship to the specified type (by class) and id

   @param type of relationship
   @param id   of relationship
   @return payloadObject assertion utility (for chaining methods)
   @throws JsonApiException if assertion fails
   */
  public AssertPayloadObject belongsTo(String type, String id) throws JsonApiException {
    try {
      String key = Entities.toBelongsTo(type);
      assertTrue(String.format("Belongs to %s id=%s", type, id), payloadObject.getRelationships().containsKey(key));
      new AssertPayload(payloadObject.getRelationships().get(key))
        .hasDataOne(Entities.toType(type), id);
      return this;

    } catch (ValueException e) {
      throw new JsonApiException(e);
    }
  }

  /**
   Assert has a belongs-to relationship to the specified type (by class) and id

   @param resource to assert belongs-to
   @return payloadObject assertion utility (for chaining methods)
   */
  public <N> AssertPayloadObject belongsTo(N resource) throws JsonApiException {
    try {
      String key = Entities.toBelongsTo(resource);
      assertTrue(String.format("Belongs to %s id=%s", Entities.toType(resource), Entities.getId(resource)), payloadObject.getRelationships().containsKey(key));
      new AssertPayload(payloadObject.getRelationships().get(key))
        .hasDataOne(resource);
      return this;

    } catch (EntityException | ValueException e) {
      throw new JsonApiException(e);
    }
  }

  /**
   Assert has a has-many relationship to the specified type (by class) and collection of ids

   @param type      of resource
   @param resources to assert has-many of
   @return payloadObject assertion utility (for chaining methods)
   */
  public <N> AssertPayloadObject hasMany(Class<?> type, Collection<N> resources) throws JsonApiException {
    return hasMany(Entities.toHasMany(type), resources);
  }

  /**
   Assert has a has-many relationship to the specified type (by class) and collection of ids

   @param type      of resource
   @param resources to assert has-many of
   @return payloadObject assertion utility (for chaining methods)
   */
  public <N> AssertPayloadObject hasMany(String type, Collection<N> resources) throws JsonApiException {
    try {
      String key = Entities.toHasMany(type);
      assertTrue(String.format("Has relationship %s", key), payloadObject.getRelationships().containsKey(key));
      List<String> list = new ArrayList<>();
      for (N resource : resources) {
        String resourceId = Entities.getId(resource);
        list.add(resourceId);
      }
      new AssertPayload(payloadObject.getRelationships().get(key))
        .hasDataMany(Entities.toType(type), list);
      return this;

    } catch (EntityException | ValueException e) {
      throw new JsonApiException(e);
    }
  }

}
