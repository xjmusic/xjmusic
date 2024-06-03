// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.model.jsonapi;

import io.xj.model.entity.EntityException;
import io.xj.model.entity.EntityUtils;
import io.xj.model.util.ValueException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static io.xj.model.util.Assertion.assertTrue;

/**
 Assertion utilities for testing Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public class AssertPayloadObject {
  final JsonapiPayloadObject jsonapiPayloadObject;

  /**
   of instance of payloadObject assertion utility object

   @param jsonapiPayloadObject to make assertions on
   */
  public AssertPayloadObject(JsonapiPayloadObject jsonapiPayloadObject) {
    this.jsonapiPayloadObject = jsonapiPayloadObject;
  }

  /**
   of instance of payloadObject assertion utility object of JSON string

   @param jsonapiPayloadObject to parse
   @return payloadObject assertion utility
   */
  public static AssertPayloadObject assertPayloadObject(JsonapiPayloadObject jsonapiPayloadObject) {
    return new AssertPayloadObject(jsonapiPayloadObject);
  }

  /**
   Assert has a belongs-to relationship to the specified type (by class) and id

   @param type of relationship
   @param id   of relationship
   @return payloadObject assertion utility (for chaining methods)
   @throws JsonapiException if assertion fails
   */
  public AssertPayloadObject belongsTo(Class<?> type, String id) throws JsonapiException, ValueException {
    String key = EntityUtils.toBelongsTo(type);
    assertTrue(String.format("Belongs to %s id=%s", type, id), jsonapiPayloadObject.getRelationships().containsKey(key));
    new AssertPayload(jsonapiPayloadObject.getRelationships().get(key))
      .hasDataOne(EntityUtils.toType(type), id);
    return this;
  }

  /**
   Assert has a belongs-to relationship to the specified type (by class) and id

   @param type of relationship
   @param id   of relationship
   @return payloadObject assertion utility (for chaining methods)
   @throws JsonapiException if assertion fails
   */
  public AssertPayloadObject belongsTo(String type, String id) throws JsonapiException {
    try {
      String key = EntityUtils.toBelongsTo(type);
      assertTrue(String.format("Belongs to %s id=%s", type, id), jsonapiPayloadObject.getRelationships().containsKey(key));
      new AssertPayload(jsonapiPayloadObject.getRelationships().get(key))
        .hasDataOne(EntityUtils.toType(type), id);
      return this;

    } catch (ValueException e) {
      throw new JsonapiException(e);
    }
  }

  /**
   Assert has a belongs-to relationship to the specified type (by class) and id

   @param resource to assert belongs-to
   @return payloadObject assertion utility (for chaining methods)
   */
  public <N> AssertPayloadObject belongsTo(N resource) throws JsonapiException {
    try {
      String key = EntityUtils.toBelongsTo(resource);
      assertTrue(String.format("Belongs to %s id=%s", EntityUtils.toType(resource), EntityUtils.getId(resource)), jsonapiPayloadObject.getRelationships().containsKey(key));
      new AssertPayload(jsonapiPayloadObject.getRelationships().get(key))
        .hasDataOne(resource);
      return this;

    } catch (EntityException | ValueException e) {
      throw new JsonapiException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   Assert has a has-many relationship to the specified type (by class) and collection of ids

   @param type      of resource
   @param resources to assert has-many of
   @return payloadObject assertion utility (for chaining methods)
   */
  public <N> AssertPayloadObject hasMany(Class<?> type, Collection<N> resources) throws JsonapiException {
    return hasMany(EntityUtils.toHasMany(type), resources);
  }

  /**
   Assert has a has-many relationship to the specified type (by class) and collection of ids

   @param type      of resource
   @param resources to assert has-many of
   @return payloadObject assertion utility (for chaining methods)
   */
  public <N> AssertPayloadObject hasMany(String type, Collection<N> resources) throws JsonapiException {
    try {
      String key = EntityUtils.toHasMany(type);
      assertTrue(String.format("Has relationship %s", key), jsonapiPayloadObject.getRelationships().containsKey(key));
      List<String> list = new ArrayList<>();
      for (N resource : resources) {
        var resourceId = EntityUtils.getId(resource);
        if (Objects.nonNull(resourceId))
          list.add(resourceId.toString());
      }
      new AssertPayload(jsonapiPayloadObject.getRelationships().get(key))
        .hasDataMany(EntityUtils.toType(type), list);
      return this;

    } catch (EntityException | ValueException e) {
      throw new JsonapiException(e);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
