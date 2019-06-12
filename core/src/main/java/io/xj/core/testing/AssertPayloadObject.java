package io.xj.core.testing;

import io.xj.core.model.entity.Resource;
import io.xj.core.model.payload.PayloadObject;
import io.xj.core.util.Text;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;

/**
 [#167276586] JSON API facilitates complex transactions
 */
public class AssertPayloadObject {
  private final PayloadObject payloadObject;

  /**
   New instance of payloadObject assertion utility object

   @param payloadObject to make assertions on
   */
  public AssertPayloadObject(PayloadObject payloadObject) {
    this.payloadObject = payloadObject;
  }

  /**
   New instance of payloadObject assertion utility object from JSON string

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
  public AssertPayloadObject belongsTo(Class type, String id) {
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
  public <N extends Resource> AssertPayloadObject belongsTo(N resource) {
    String key = Text.toResourceBelongsTo(resource);
    assertTrue(String.format("Belongs to %s id=%s", resource.getResourceType(), resource.getResourceId()), payloadObject.getRelationships().containsKey(key));
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
  public <N extends Resource> AssertPayloadObject hasMany(Class type, Collection<N> resources) {
    String key = Text.toResourceHasMany(type);
    assertTrue(String.format("Has relationship %s", key), payloadObject.getRelationships().containsKey(key));
    new AssertPayload(payloadObject.getRelationships().get(key))
      .hasDataMany(Text.toResourceType(type), resources.stream().map(Resource::getResourceId).collect(Collectors.toList()));
    return this;
  }

}
