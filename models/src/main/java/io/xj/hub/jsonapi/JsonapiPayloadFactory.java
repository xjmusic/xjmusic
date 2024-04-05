// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.jsonapi;


import java.io.BufferedReader;
import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Singleton allows an app to register entity types to their attributes and belongs-to relationships.
 * <p>
 * Has-many relationships will be added implicitly-- therefore it is necessary to register parent classes first.
 * <p>
 * Entity types are casified differently for different contexts.
 * + The type of an entity in any context is enough information to compute its type in another context.
 * + Java Bean/POJO name is upper camel singular, e.g. Entity or ProjectUser
 * + Belongs-to type is lower camel singular, e.g. "entity" or "projectUser"
 * + Has-many type is lower camel plural, e.g. "entities" or "projectUsers"
 * + Payload object type is lower hyphen plural, e.g. "entities" or "project-users"
 * + REST API endpoint path type is lower hyphen plural, e.g. "/entities" or "/project-users"
 * <p>
 * Created by Charney Kaye on 2020/03/09
 */
public interface JsonapiPayloadFactory {

  /**
   * Consume all data of a payload:
   * + Set all attributes
   * + Adding any available sub-entities
   * + Re-index relationships and prune orphaned entities
   *
   * @param target         into which payload will be consumed
   * @param jsonapiPayload to consume
   * @return target Entity (for chaining methods)
   * @throws JsonapiException on failure to consume payload
   */
  <N> N consume(N target, JsonapiPayload jsonapiPayload) throws JsonapiException;

  /**
   * Set all attributes of entity of a payload object
   * <p>
   * There's a default implementation in EntityImpl, which uses the attribute names to compute setter method names,
   * and maps all value objects to setters. Simple entities need not override target method.
   * <p>
   * However, entities with relationships ought to override the base method, invoke the super, then parse additionally:
   * |
   * |  @Override
   * |  public PayloadObject toResourceObject() {
   * |    return super.toResourceObject()
   * |      .add("project", ResourceRelationship.of("projects", projectId));
   * |  }
   * |
   *
   * @param target               into which payload object will be consumed
   * @param jsonapiPayloadObject of which to get attributes
   * @return target Entity (for chaining methods)
   * @throws JsonapiException on failure to set
   */
  <N> N consume(N target, JsonapiPayloadObject jsonapiPayloadObject) throws JsonapiException;

  /**
   * Shortcut to build payload object with no child entities
   *
   * @param target from which to build payload object
   * @return resource object
   */
  <N> JsonapiPayloadObject toPayloadObject(N target) throws JsonapiException;

  /**
   * Shortcut to build a collection of payload objects with no child entities
   *
   * @param targets from which to build collection of payload objects
   * @return resource object
   */
  <N> Collection<JsonapiPayloadObject> toPayloadObjects(Collection<N> targets) throws JsonapiException;

  /**
   * Build and return a Entity Object of target entity, probably for an API Payload
   * <p>
   * There's a default implementation in EntityImpl, which uses the attribute names to compute getter method names,
   * and maps all attribute names to value objects. Simple entities need not override target method.
   * <p>
   * However, entities with relationships ought to override the base method, get the super result, then add to it, e.g.:
   * |
   * |  @Override
   * |  public PayloadObject toPayloadObject() {
   * |    return super.toPayloadObject()
   * |      .add("project", ResourceRelationship.of("projects", projectId));
   * |  }
   * |
   * <p>
   * Also, receives an (optionally, empty) collection of potential child resources-- only match resources are added
   *
   * @param target         entity of which to return payload object with child resources
   * @param childResources to search for possible children-- only add matched resources
   * @return resource object
   */
  <N> JsonapiPayloadObject toPayloadObject(N target, Collection<N> childResources) throws JsonapiException;

  /**
   * Create a new Payload instance
   *
   * @return new Payload
   */
  JsonapiPayload newJsonapiPayload();

  /**
   * Create a new PayloadObject instance
   *
   * @return new Payload
   */
  JsonapiPayloadObject newPayloadObject();

  /**
   * Create a new PayloadError instance
   *
   * @return new PayloadError
   */
  PayloadError newPayloadError();

  /**
   * Build and return a reference (type and id only) Entity Object of target entity, probably for an API Payload
   * <p>
   * There's a default implementation in EntityImpl, which uses the resource type and id
   *
   * @param target from while to get payload reference object
   * @return resource object
   */
  <N> JsonapiPayloadObject toPayloadReferenceObject(N target) throws JsonapiException;

  /**
   * Add all sub-entities of given entity to included resource objects
   * + cross-references all entities and adds hasMany references to payloads
   *
   * @param jsonapiPayload on which to add included resources
   * @param resources      to be added
   * @return this Payload (for chaining methods)
   */
  <N> JsonapiPayload addIncluded(JsonapiPayload jsonapiPayload, Collection<N> resources) throws JsonapiException;

  /**
   * Add a resource to the included objects
   *
   * @param jsonapiPayloadObject which will be added to included objects
   * @return this Payload (for chaining methods)
   */
  JsonapiPayload addIncluded(JsonapiPayload jsonapiPayload, JsonapiPayloadObject jsonapiPayloadObject) throws JsonapiException;

  /**
   * Add a payload object to a relationship, if it's related
   *
   * @param rel to potentially add
   */
  void addIfRelated(JsonapiPayloadObject target, JsonapiPayloadObject rel) throws JsonapiException;

  /**
   * Set has-many data
   * <p>
   * NOTE we do NOT addIncluded(subEntities)
   * + This is key to NOT sending overwhelmingly large resource index payloads
   * + FUTURE maybe include partial filter of sub-entities
   *
   * @param entities to set
   * @return this Payload (for chaining methods)
   */
  <N> JsonapiPayload setDataEntities(JsonapiPayload jsonapiPayload, Collection<N> entities) throws JsonapiException;

  /**
   * Set has-one data
   *
   * @param entity to set
   * @return this Payload (for chaining methods)
   */
  <N> JsonapiPayload setDataEntity(JsonapiPayload jsonapiPayload, N entity) throws JsonapiException;

  /**
   * Set Data as references to many type and id
   *
   * @param resources to reference
   * @return this payload (for chaining methods)
   */
  <N> JsonapiPayload setDataReferences(JsonapiPayload jsonapiPayload, Collection<N> resources) throws JsonapiException;

  /**
   * Add one payload to a has-many relationship of this resource object
   *
   * @param obj                  payload object to which relationship will be added
   * @param relationshipName     for which object will be added
   * @param jsonapiPayloadObject relationship to add
   * @return the payload object after the relationship has been added
   */
  JsonapiPayloadObject add(JsonapiPayloadObject obj, String relationshipName, JsonapiPayloadObject jsonapiPayloadObject) throws JsonapiException;

  /**
   * Filter the has-many objects of this payload using the specified lambda condition,
   * and exclude any included objects not belonging to the remaining has-many objects.
   *
   * @param type      class of entity comprising the set of has-many objects
   * @param condition predicate with which to filter has-many objects
   * @return the payload object comprising a filtered set of has-many objects and included objects belonging to them
   */
  JsonapiPayload filter(JsonapiPayload jsonapiPayload, Class<?> type, Predicate<? super JsonapiPayloadObject> condition) throws JsonapiException;

  /**
   * Filter the included objects of this payload using the specified lambda condition,
   *
   * @param condition predicate with which to filter included objects
   * @return the payload object comprising a filtered set of has-many objects and included objects belonging to them
   */
  JsonapiPayload filterIncluded(JsonapiPayload jsonapiPayload, Predicate<? super JsonapiPayloadObject> condition) throws JsonapiException;

  /**
   * Sort the has-many objects of this payload using the specified lambda key extactor,
   * and exclude any included objects not belonging to the remaining has-many objects.
   *
   * @param type         class of entity comprising the set of has-many objects
   * @param keyExtractor to obtain value to compare with
   * @return the payload object comprising a sorted set of has-many objects and included objects belonging to them
   */
  JsonapiPayload sort(JsonapiPayload jsonapiPayload, Class<?> type, Function<? super JsonapiPayloadObject, Long> keyExtractor) throws JsonapiException;

  /**
   * Limit the has-many objects of this payload using the specified lambda condition,
   * and exclude any included objects not belonging to the remaining has-many objects.
   *
   * @param type  class of entity comprising the set of has-many objects
   * @param limit number of has-many objects to truncate to, from beginning of list
   * @return limited set of has-many objects and included objects belonging to them
   */
  JsonapiPayload limit(JsonapiPayload jsonapiPayload, Class<?> type, long limit) throws JsonapiException;

  /**
   * Find the first has-many objects of this payload using the specified lambda condition,
   * transform it into data-one payload comprising only that object,
   * and exclude any included objects not belonging to the remaining has-one object.
   *
   * @param type      class of entity comprising the set of has-many objects, and resulting has-one object
   * @param condition predicate with which to filter has-many objects, to find resulting has-one object
   * @return the first has-one object matching the given predicate from the original has-many set of objects, and included objects belonging to it.
   */
  JsonapiPayload find(JsonapiPayload jsonapiPayload, Class<?> type, Predicate<? super JsonapiPayloadObject> condition) throws JsonapiException;

  /**
   * Parse some JSON text, deserializing it into a {@link JsonapiPayload}
   *
   * @param json to deserialize
   * @return {@link JsonapiPayload} deserialized from JSON
   * @throws JsonapiException on failure to deserialize
   */
  JsonapiPayload deserialize(String json) throws JsonapiException;

  /**
   * Parse some JSON text from a buffered reader, deserializing it into a {@link JsonapiPayload}
   *
   * @param bufferedReader to read from, and deserialize
   * @return {@link JsonapiPayload} deserialized from JSON
   * @throws JsonapiException on failure to deserialize
   */
  JsonapiPayload deserialize(BufferedReader bufferedReader) throws JsonapiException;

  /**
   * Serialize a JSON:API payload into JSON string
   *
   * @param obj to serialize
   * @return Payload serialized as JSON text
   * @throws JsonapiException on failure to serialize
   */
  String serialize(Object obj) throws JsonapiException;

  /**
   * Get an instance of an entity from a payload object comprising it.
   * <p>
   * Looks up the `type` of the provided payload object in our registry of entities, in order to obtain a constructor.
   *
   * @param jsonapiPayloadObject to get instance of
   */
  <N> N toOne(JsonapiPayloadObject jsonapiPayloadObject) throws JsonapiException;

  /**
   * Get an instance of an entity from a payload object comprising it.
   * <p>
   * Requires data-one payload
   * <p>
   * Looks up the `type` of the provided payload object in our registry of entities, in order to obtain a constructor.
   *
   * @param jsonapiPayload to get instance of
   */
  <N> N toOne(JsonapiPayload jsonapiPayload) throws JsonapiException;

  /**
   * Get a set of instances of entities from a payload with data-many payload objects.
   *
   * @param jsonapiPayload to get collection of instances of payload objects of
   */
  <N> Collection<N> toMany(JsonapiPayload jsonapiPayload) throws JsonapiException;

  /**
   * Get a data-one Payload from the given entity
   *
   * @param entity to get payload of
   * @param <N>    type of entity
   * @return data-one Payload of given entity
   */
  <N> JsonapiPayload from(N entity) throws JsonapiException;

  /**
   * Get a data-one Payload from the given entity, including the given entities
   *
   * @param entity   to get payload of
   * @param included entities to include in payload
   * @param <N>      type of entity
   * @return data-one Payload of given entity, including the given entities
   */
  <N> JsonapiPayload from(N entity, Collection<N> included) throws JsonapiException;

  /**
   * Get a data-many Payload from the given entities
   *
   * @param entities to get payload of
   * @param <N>      type of entities
   * @return data-many Payload of given entities
   */
  <N> JsonapiPayload from(Collection<N> entities) throws JsonapiException;

  /**
   * Get a data-many Payload from the given entities, including the given entities
   *
   * @param entities to get payload of
   * @param included entities to include in payload
   * @param <N>      type of entities
   * @return data-many Payload of given entities, including the given entities
   */
  <N> JsonapiPayload from(Collection<N> entities, Collection<N> included) throws JsonapiException;
}
