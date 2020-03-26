// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 Singleton allows an app to register entity types to their attributes and belongs-to relationships.
 <p>
 Has-many relationships will be added implicitly-- therefore it is necessary to register parent classes first.
 <p>
 Entity types are casified differently for different contexts.
 + The type of an entity in any context is enough information to compute its type in another context.
 + Java Bean/POJO name is upper camel singular, e.g. Entity or AccountUser
 + Belongs-to type is lower camel singular, e.g. "entity" or "accountUser"
 + Has-many type is lower camel plural, e.g. "entities" or "accountUsers"
 + Payload object type is lower hyphen plural, e.g. "entities" or "account-users"
 + REST API endpoint path type is lower hyphen plural, e.g. "/entities" or "/account-users"
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public interface PayloadFactory {

  /**
   Register an entity type. This method returns a builder which this used to specify the relations of this type.

   @param type of entity
   @return this payload factory (for chaining methods)
   */
  PayloadEntitySchema register(String type);

  /**
   Register an entity type. This method returns a builder which this used to specify the relations of this type.

   @param typeClass of entity
   @return this payload factory (for chaining methods)
   */
  PayloadEntitySchema register(Class<?> typeClass);

  /**
   Get an instance of a given type of entity

   @param type of entity to get an instance of
   @return instance of given type of entity
   */
  Object getInstance(String type) throws RestApiException;

  /**
   Get the belongs-to types for a given type of entity

   @param type of entity to get the belongs-to types for
   @return belongs-to types for the type of entity
   */
  Set<String> getBelongsTo(String type) throws RestApiException;

  /**
   Get the has-many types for a given type of entity

   @param type of entity to get the has-many types for
   @return has-many types for the type of entity
   */
  Set<String> getHasMany(String type) throws RestApiException;

  /**
   Get the attributes for a given type of entity

   @param type of entity to get attributes for
   @return attributes for given type of entity
   */
  Set<String> getAttributes(String type) throws RestApiException;

  /**
   Consume all data of a payload:
   + Set all attributes
   + Adding any available sub-entities
   + Re-index relationships and prune orphaned entities

   @param target  into which payload will be consumed
   @param payload to consume
   @return target Entity (for chaining methods)
   @throws RestApiException on failure to consume payload
   */
  <N> N consume(N target, Payload payload) throws RestApiException;

  /**
   Set all attributes of entity of a payload object
   <p>
   There's a default implementation in EntityImpl, which uses the attribute names to compute setter method names,
   and maps all value objects to setters. Simple entities need not override target method.
   <p>
   However, entities with relationships ought to override the base method, invoke the super, then parse additionally:
   |
   |  @Override
   |  public PayloadObject toResourceObject() {
   |    return super.toResourceObject()
   |      .add("account", ResourceRelationship.of("accounts", accountId));
   |  }
   |

   @param target        into which payload object will be consumed
   @param payloadObject of which to get attributes
   @return target Entity (for chaining methods)
   @throws RestApiException on failure to set
   */
  <N> N consume(N target, PayloadObject payloadObject) throws RestApiException;

  /**
   Get resource attributes based on getResourceAttributeNames() for target instance
   NOTE: target is implemented in EntityImpl and widely shared; individual classes simply return getResourceAttributeNames()

   @param target from which to get resource attributes
   @return payload attributes
   */
  <N> Map<String, Object> getResourceAttributes(N target) throws RestApiException;

  /**
   Shortcut to build payload object with no child entities

   @param target from which to build payload object
   @return resource object
   */
  <N> PayloadObject toPayloadObject(N target) throws RestApiException;

  /**
   Build and return a Entity Object of target entity, probably for an API Payload
   <p>
   There's a default implementation in EntityImpl, which uses the attribute names to compute getter method names,
   and maps all attribute names to value objects. Simple entities need not override target method.
   <p>
   However, entities with relationships ought to override the base method, get the super result, then add to it, e.g.:
   |
   |  @Override
   |  public PayloadObject toPayloadObject() {
   |    return super.toPayloadObject()
   |      .add("account", ResourceRelationship.of("accounts", accountId));
   |  }
   |
   <p>
   Also, receives an (optionally, empty) collection of potential child resources-- only match resources are added

   @param target         entity of which to return payload object with child resources
   @param childResources to search for possible children-- only add matched resources
   @return resource object
   */
  <N> PayloadObject toPayloadObject(N target, Collection<N> childResources) throws RestApiException;

  /**
   Create a new Payload instance

   @return new Payload
   */
  Payload newPayload();

  /**
   Create a new PayloadObject instance

   @return new Payload
   */
  PayloadObject newPayloadObject();

  /**
   Create a new PayloadError instance

   @return new PayloadError
   */
  PayloadError newPayloadError();

  /**
   Build and return a reference (type and id only) Entity Object of target entity, probably for an API Payload
   <p>
   There's a default implementation in EntityImpl, which uses the resource type and id

   @param target from while to get payload reference object
   @return resource object
   */
  <N> PayloadObject toPayloadReferenceObject(N target) throws RestApiException;

  /**
   Set all values available of a source Entity

   @param source source Entity
   @param target on which to set all resource attributes
   */
  <N> void setAllAttributes(N source, N target) throws RestApiException;

  /**
   Add all sub-entities of given entity to included resource objects
   + cross-references all entities and adds hasMany references to payloads

   @param payload   on which to add included resources
   @param resources to be added
   @return this Payload (for chaining methods)
   */
  <N> Payload addIncluded(Payload payload, Collection<N> resources) throws RestApiException;

  /**
   Add a resource to the included objects

   @param payloadObject which will be added to included objects
   @return this Payload (for chaining methods)
   */
  Payload addIncluded(Payload payload, PayloadObject payloadObject) throws RestApiException;

  /**
   Add a payload object to a relationship, if it's related

   @param rel to potentially add
   */
  void addIfRelated(PayloadObject target, PayloadObject rel) throws RestApiException;

  /**
   Set has-many data
   <p>
   NOTE we do NOT addIncluded(subEntities)
   + This is key to NOT sending overwhelmingly large resource index payloads
   + FUTURE maybe include partial filter of sub-entities

   @param entities to set
   @return this Payload (for chaining methods)
   */
  <N> Payload setDataEntities(Payload payload, Collection<N> entities) throws RestApiException;

  /**
   Set has-one data

   @param entity to set
   @return this Payload (for chaining methods)
   */
  <N> Payload setDataEntity(Payload payload, N entity) throws RestApiException;

  /**
   Set Data as references to many type and id

   @param resources to reference
   @return this payload (for chaining methods)
   */
  <R> Payload setDataReferences(Payload payload, Collection<R> resources) throws RestApiException;

  /**
   Add one payload to a has-many relationship of this resource object

   @param obj              payload object to which relationship will be added
   @param relationshipName for which object will be added
   @param payloadObject    relationship to add
   @return the payload object after the relationship has been added
   */
  PayloadObject add(PayloadObject obj, String relationshipName, PayloadObject payloadObject) throws RestApiException;

  /**
   Serialize an object  into JSON string

   @param obj to serialize, probably a {@link Payload}, but it doesn't have to be.
   @return Payload serialized as JSON text
   @throws RestApiException on failure to serialize
   */
  String serialize(Object obj) throws RestApiException;

  /**
   Parse some JSON text, deserializing it into a {@link Payload}

   @param json to deserialize
   @return {@link Payload} deserialized from JSON
   @throws RestApiException on failure to deserialize
   */
  Payload deserialize(String json) throws RestApiException;

  /**
   Parse some JSON text, deserializing it into a the specified class

   @param json to deserialize
   @param valueType class which deserialization will result in
   @return {@link Object} deserialized from JSON
   @throws RestApiException on failure to deserialize
   */
  <N> N deserialize(String json, Class<N> valueType) throws RestApiException;
}
