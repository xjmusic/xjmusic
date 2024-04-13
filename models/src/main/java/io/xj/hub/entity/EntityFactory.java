// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub.entity;


import io.xj.hub.HubContent;
import io.xj.hub.pojos.Template;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 Singleton allows an app to register entity types to their attributes and belongs-to relationships.
 <p>
 Has-many relationships will be added implicitly-- therefore it is necessary to register parent classes first.
 <p>
 Entity types are casified differently for different contexts.
 + The type of an entity in any context is enough information to compute its type in another context.
 + Java Bean/POJO name is upper camel singular, e.g. Entity or ProjectUser
 + Belongs-to type is lower camel singular, e.g. "entity" or "projectUser"
 + Has-many type is lower camel plural, e.g. "entities" or "projectUsers"
 + Entity object type is lower hyphen plural, e.g. "entities" or "project-users"
 + REST API endpoint path type is lower hyphen plural, e.g. "/entities" or "/project-users"
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
  <N> N getInstance(String type) throws EntityException;

  /**
   Get an instance of a given type of entity

   @param type of entity to get an instance of
   @return instance of given type of entity
   */
  <N> N getInstance(Class<N> type) throws EntityException;

  /**
   Get the belongs-to types for a given type of entity

   @param type of entity to get the belongs-to types for
   @return belongs-to types for the type of entity
   */
  Set<String> getBelongsTo(String type) throws EntityException;

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
  <N> Map<String, Object> getResourceAttributes(N target) throws EntityException;

  /**
   Set all values available of a source Entity

   @param source source Entity
   @param target on which to set all resource attributes
   */
  <N> void setAllAttributes(N source, N target) throws EntityException;

  /**
   Set all empty target values from a source Entity

   @param source source Entity
   @param target on which to set all resource attributes
   */
  <N> void setAllEmptyAttributes(N source, N target) throws EntityException;

  /**
   Duplicate an entity: make a copy with a new id, but the same attributes and relationships

   @param entity to duplicate
   @param <N>    type of entity
   @return duplicate of original entity
   */
  <N> N duplicate(N entity) throws EntityException;

  /**
   Duplicate a collection of entities: make copies with new ids, but the same attributes and relationships

   @param entities to duplicate
   @param <N>      type of entities
   @return map of original entity UUIDs to their new duplicated entities
   */
  <N> Map<UUID, N> duplicateAll(Collection<N> entities) throws EntityException;

  /**
   Duplicate a collection of entities: make copies with new ids, but the same attributes and relationships,

   @param entities         to duplicate
   @param newRelationships to overwrite relationships with, e.g. an entity Superwidget will overwrite attributes superwidgetId with the id of the Superwidget
   @param <N>              type of entities
   @return map of original entity UUIDs to their new duplicated entities
   */
  <N> Map<UUID, N> duplicateAll(Collection<N> entities, Collection<?> newRelationships) throws EntityException;

  /**
   Copy an entity: its id, know attributes, and relationships,
   for example when getting/putting objects from an in-memory store to prevent unintended mutation of the store ;)

   @param entity to copy
   @param <N>    type of entity
   @return copy of original entity
   */
  <N> N copy(N entity) throws EntityException;

  /**
   Copy a collection of entities: their id, know attributes, and relationships,
   for example when getting/putting objects from an in-memory store to prevent unintended mutation of the store ;)

   @param entities to copy
   @param <N>      type of entities
   @return map of original entity UUIDs to their new copyd entities
   */
  <N> Collection<N> copyAll(Collection<N> entities) throws EntityException;

  /**
   Parse some JSON text, and deserialize it into the specified class

   @param <N>       class of entity
   @param valueType class which deserialization will result in
   @param json      to deserialize
   @return {@link Object} deserialized from JSON
   @throws EntityException on failure to deserialize
   */
  <N> N deserialize(Class<N> valueType, String json) throws EntityException;

  /**
   Serialize an object  into JSON string

   @param obj to serialize
   @return Payload serialized as JSON text
   @throws EntityException on failure to serialize
   */
  String serialize(Object obj) throws EntityException;

  /**
   Get a new HubContent object for a specific template
   - Only include entities bound to the template
   - Only include entities in a published state (for entities with a state)

   @param original HubContent
   @param template for which to get a new HubContent object
   @return new HubContent object for the template
   */
  HubContent forTemplate(HubContent original, Template template);
}
