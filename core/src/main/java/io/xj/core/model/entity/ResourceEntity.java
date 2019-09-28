//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.entity;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.payload.PayloadObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 [#167276586] JSON API facilitates complex transactions
 */
public interface ResourceEntity {

  /**
   Add an exception to the SuperEntity errors

   @param exception to add
   */
  void add(CoreException exception);

  /**
   Whether this resource belongs to the specified resource

   @param resource to test whether this belongs to
   @return true if this belongs to the specified resource
   */
  boolean belongsTo(ResourceEntity resource);

  /**
   Consume all data from a payload:
   + Set all attributes
   + Adding any available sub-entities
   + Re-index relationships and prune orphaned entities

   @param payload to consume
   @return this ResourceEntity (for chaining methods)
   @throws CoreException on failure to consume payload
   */
  <N extends ResourceEntity> N consume(Payload payload) throws CoreException;

  /**
   Set all attributes of entity from a payload object
   <p>
   There's a default implementation in EntityImpl, which uses the attribute names to compute setter method names,
   and maps all value objects to setters. Simple entities need not override this method.
   <p>
   However, entities with relationships ought to override the base method, invoke the super, then parse additionally:
   |
   |  @Override
   |  public PayloadObject toResourceObject() {
   |    return super.toResourceObject()
   |      .add("account", ResourceRelationship.of("accounts", accountId));
   |  }
   |

   @param payloadObject from which to get attributes
   @return this ResourceEntity (for chaining methods)
   @throws CoreException on failure to set
   */
  <N extends ResourceEntity> N consume(PayloadObject payloadObject) throws CoreException;

  /**
   Get a value from a target object via attribute name

   @param name of attribute to get
   @return value
   @throws InvocationTargetException on failure to invoke method
   @throws IllegalAccessException    on access failure
   */
  Optional<Object> get(String name) throws InvocationTargetException, IllegalAccessException, CoreException;

  /**
   Get a value from a target object via getter method

   @param getter to use
   @return value
   @throws InvocationTargetException on failure to invoke method
   @throws IllegalAccessException    on access failure
   */
  Optional<Object> get(Method getter) throws InvocationTargetException, IllegalAccessException;

  /**
   Get all entities contained within this entity.
   Empty by default, but some entity types that extend this (e.g. SuperEntity) contain many Sub-entities

   @return collection of entities
   */
  Collection<SubEntity> getAllSubEntities();

  /**
   Get errors

   @return errors
   */
  Collection<CoreException> getErrors();

  /**
   Get a collection of resource attribute names

   @return resource attribute names
   */
  ImmutableList<String> getResourceAttributeNames();

  /**
   Get resource attributes based on getResourceAttributeNames() for this instance
   NOTE: this is implemented in EntityImpl and widely shared; individual classes simply return getResourceAttributeNames()

   @return payload attributes
   */
  Map<String, Object> getResourceAttributes();

  /**
   Get this resource's belongs-to relations

   @return list of classes this resource belongs to
   */
  ImmutableList<Class> getResourceBelongsTo();

  /**
   Get this resource's has-many relations

   @return list of classes this resource has many of
   */
  ImmutableList<Class> getResourceHasMany();

  /**
   get ResourceEntity ID
   <p>
   For SuperEntity, that's a BigInteger
   <p>
   For SubEntity, that's a UUID

   @return ResourceEntity Id
   */
  String getResourceId();

  /**
   get ResourceEntity Type- always a plural noun, i.e. Users or Libraries

   @return ResourceEntity Type
   */
  String getResourceType();

  /**
   Get the URI of any entity

   @return Entity URI
   */
  URI getURI();

  /**
   Set all values available from a source ResourceEntity

   @param from source ResourceEntity
   */
  void setAllResourceAttributes(ResourceEntity from);

  /**
   Set a value using a setter method

   @param method setter to use
   @param value  to set
   */
  void set(Method method, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

  /**
   Set a value using an attribute name

   @param name  of attribute for which to find setter method
   @param value to set
   */
  void set(String name, Object value) throws CoreException;

  /**
   Shortcut to build payload object with no child entities

   @return resource object
   */
  PayloadObject toPayloadObject();

  /**
   Build and return a ResourceEntity Object of this entity, probably for an API Payload
   <p>
   There's a default implementation in EntityImpl, which uses the attribute names to compute getter method names,
   and maps all attribute names to value objects. Simple entities need not override this method.
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

   @param childResources to search for possible children-- only add matched resources
   @return resource object
   */
  <N extends ResourceEntity> PayloadObject toPayloadObject(Collection<N> childResources);

  /**
   Build and return a reference (type and id only) ResourceEntity Object of this entity, probably for an API Payload
   <p>
   There's a default implementation in EntityImpl, which uses the resource type and id

   @return resource object
   */
  PayloadObject toPayloadReferenceObject();

  /**
   Validate data.

   @return this ResourceEntity (for chaining methods)
   @throws CoreException if invalid.
   */
  <N extends ResourceEntity> N validate() throws CoreException;
}
