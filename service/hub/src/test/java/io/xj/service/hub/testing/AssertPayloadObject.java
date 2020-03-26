// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.testing;

import io.xj.lib.rest_api.PayloadEntity;
import io.xj.lib.rest_api.PayloadObject;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.Text;
import io.xj.service.hub.entity.Entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 [#167276586] JSON API facilitates complex transactions
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
   */
  public AssertPayloadObject belongsTo(Class<?> type, String id) {
    String key = Text.toResourceBelongsTo(type);
    assertTrue(String.format("Belongs to %s id=%s", type, id), payloadObject.getRelationships().containsKey(key));
    new AssertPayload(payloadObject.getRelationships().get(key))
      .hasDataOne(Text.toResourceType(type), id);
    return this;
  }

  /**
   Assert has a belongs-to relationship to the specified type (by class) and id

   @param resource to assert belongs-to
   @return payloadObject assertion utility (for chaining methods)
   */
  public <N extends Entity> AssertPayloadObject belongsTo(N resource) throws RestApiException {
    String key = Text.toResourceBelongsTo(resource);
    assertTrue(String.format("Belongs to %s id=%s", PayloadEntity.getResourceType(resource), PayloadEntity.getResourceId(resource)), payloadObject.getRelationships().containsKey(key));
    new AssertPayload(payloadObject.getRelationships().get(key))
      .hasDataOne(resource);
    return this;
  }

  /**
   Assert has a has-many relationship to the specified type (by class) and collection of ids

   @param type      of resource
   @param resources to assert has-many of
   @return payloadObject assertion utility (for chaining methods)
   */
  public <N extends Entity> AssertPayloadObject hasMany(Class<?> type, Collection<N> resources) throws RestApiException {
    String key = Text.toResourceHasMany(type);
    assertTrue(String.format("Has relationship %s", key), payloadObject.getRelationships().containsKey(key));
    List<String> list = new ArrayList<>();
    for (N resource : resources) {
      String resourceId = PayloadEntity.getResourceId(resource);
      list.add(resourceId);
    }
    new AssertPayload(payloadObject.getRelationships().get(key))
      .hasDataMany(Text.toResourceType(type), list);
    return this;
  }

}
