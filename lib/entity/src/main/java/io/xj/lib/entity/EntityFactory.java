// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

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
 + Entity object type is lower hyphen plural, e.g. "entities" or "account-users"
 + REST API endpoint path type is lower hyphen plural, e.g. "/entities" or "/account-users"
 <p>
 Created by Charney Kaye on 2020/03/09
 */
public interface EntityFactory {

  /**
   Register an entity type. This method returns a builder which this used to specify the relations of this type.

   @param type of entity
   @return this entity factory (for chaining methods)
   */
  EntitySchema register(String type);

  /**
   Register an entity type. This method returns a builder which this used to specify the relations of this type.

   @param typeClass of entity
   @return this entity factory (for chaining methods)
   */
  EntitySchema register(Class<?> typeClass);

  /**
   Get an instance of a given type of entity

   @param type of entity to get an instance of
   @return instance of given type of entity
   */
  <N extends Entity> N getInstance(String type) throws EntityException;

  /**
   Get an instance of a given type of entity

   @param type of entity to get an instance of
   @return instance of given type of entity
   */
  <N extends Entity> N getInstance(Class<N> type) throws EntityException;

  /**
   Get the belongs-to types for a given type of entity

   @param type of entity to get the belongs-to types for
   @return belongs-to types for the type of entity
   */
  Set<String> getBelongsTo(String type) throws EntityException;

  /**
   Get the belongs-to types for a given type of entity

   @param type of entity to get the belongs-to types for
   @return belongs-to types for the type of entity
   */
  Set<String> getBelongsTo(Class<?> type) throws EntityException;

  /**
   Get the has-many types for a given type of entity

   @param type of entity to get the has-many types for
   @return has-many types for the type of entity
   */
  Set<String> getHasMany(String type) throws EntityException;

  /**
   Get the attributes for a given type of entity

   @param type of entity to get attributes for
   @return attributes for given type of entity
   */
  Set<String> getAttributes(String type) throws EntityException;

  /**
   Get resource attributes based on getResourceAttributeNames() for target instance
   NOTE: target is implemented in EntityImpl and widely shared; individual classes simply return getResourceAttributeNames()

   @param target from which to get resource attributes
   @return entity attributes
   */
  <N extends Entity> Map<String, Object> getResourceAttributes(N target) throws EntityException;

  /**
   Set all values available of a source Entity

   @param source source Entity
   @param target on which to set all resource attributes
   */
  <N extends Entity> void setAllAttributes(N source, N target) throws EntityException;

  /**
   Serialize an object  into JSON string

   @param obj to serialize, probably a {@link Entity}, but it doesn't have to be.
   @return Entity serialized as JSON text
   @throws EntityException on failure to serialize
   */
  String serialize(Object obj) throws EntityException;

  /**
   Parse some JSON text, deserializing it into a {@link Entity}

   @param json to deserialize
   @return {@link Entity} deserialized from JSON
   @throws EntityException on failure to deserialize
   */
  Entity deserialize(String json) throws EntityException;

  /**
   Parse some JSON text, deserializing it into a the specified class

   @param <N>       class of entity
   @param valueType class which deserialization will result in
   @param json      to deserialize
   @return {@link Object} deserialized from JSON
   @throws EntityException on failure to deserialize
   */
  <N> N deserialize(Class<N> valueType, String json) throws EntityException;

  /**
   Whether the specified type is a known schema

   @param entityName to check for known schema
   @return true if schema is known
   */
  boolean isKnownSchema(String entityName);

  /**
   Whether the specified type is a known schema

   @param entityClass to check for known schema
   @return true if schema is known
   */
  boolean isKnownSchema(Class<?> entityClass);

  /**
   Whether the specified type is a known schema

   @param entity to check for known schema
   @return true if schema is known
   */
  boolean isKnownSchema(Object entity);

  /**
   Clone an entity: its id, know attributes, and relationships,
   for example when getting/putting objects from an in-memory store to prevent unintended mutation of the store ;)

   @param entity to clone
   @param <N>    type of entity
   @return clone of original entity
   */
  <N extends Entity> N clone(N entity) throws EntityException;

  /**
   Clone a collection of entities: their id, know attributes, and relationships,
   for example when getting/putting objects from an in-memory store to prevent unintended mutation of the store ;)

   @param entities to clone
   @param <N>      type of entities
   @return clones of original entities
   */
  <N extends Entity> Collection<N> cloneAll(Collection<N> entities) throws EntityException;
}
