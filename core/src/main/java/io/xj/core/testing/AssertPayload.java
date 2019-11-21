package io.xj.core.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.xj.core.entity.Entity;
import io.xj.core.payload.Payload;
import io.xj.core.payload.PayloadDataType;
import io.xj.core.payload.PayloadObject;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

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
  public <N extends Entity> AssertPayloadObject hasDataOne(N resource) {
    assertEquals("payload data type", PayloadDataType.HasOne, payload.getDataType());
    Optional<PayloadObject> dataOne = payload.getDataOne();
    assertTrue("has one data", dataOne.isPresent());
    assertTrue(String.format("one data same as %s id=%s", resource.getResourceType(), resource.getResourceId()), dataOne.get().isSame(resource));
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
  public <N extends Entity> AssertPayloadObject hasIncluded(N resource) {
    Optional<PayloadObject> payloadObject = payload.getIncluded().stream().filter(obj -> obj.isSame(resource)).findFirst();
    assertTrue(String.format("has included %s id=%s", resource.getResourceType(), resource.getResourceId()), payloadObject.isPresent());
    return new AssertPayloadObject(payloadObject.get());
  }

  /**
   Assert has included entities

   @param resourceType to assert
   @param resources    to assert
   @return this Payload assertion utility (for chaining methods)
   */
  public <N extends Entity> AssertPayload hasIncluded(String resourceType, ImmutableList<N> resources) {
    Collection<PayloadObject> included = payload.getIncludedOfType(resourceType);
    assertEquals("same number of ids", resources.size(), included.size());
    Collection<String> foundIds = Lists.newArrayList();
    Collection<String> resourceIds = resources.stream().map(Entity::getResourceId).collect(Collectors.toList());
    included.forEach(payloadObject -> {
      assertFalse("already found id", foundIds.contains(payloadObject.getId()));
      assertEquals(String.format("unexpected %s", payloadObject.getType()), resourceType, payloadObject.getType());
      assertTrue(String.format("unexpected %s id=%s", resourceType, payloadObject.getId()), resourceIds.contains(payloadObject.getId()));
      foundIds.add(payloadObject.getId());
    });
    return this;
  }

}
