// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import java.util.Set;
import java.util.function.Supplier;

/**
 Store the schema for one entity type: its attributes, belongs-to and has-many relationships, and creator function.
 <p>
 Note that has-many relationships are updated after creation, when child entities are registered as belonging-to
 */
public class PayloadEntitySchema {
  private String type;
  private Set<String> attributes;
  private Set<String> hasManyTypes;
  private Set<String> belongsToTypes;
  private Supplier<Object> creator;

  /**
   Constructor initializes empty inner sets
   */
  PayloadEntitySchema(String type) {
    this.type = PayloadKey.toResourceType(type);
    attributes = Sets.newTreeSet();
    hasManyTypes = Sets.newTreeSet();
    belongsToTypes = Sets.newTreeSet();
  }

  /**
   Create a new instance

   @param type of entity schema to create
   @return new entity schema ot given type
   */
  public static PayloadEntitySchema of(String type) {
    return new PayloadEntitySchema(type);
  }

  /**
   Create a new instance

   @param type of entity schema to create
   @return new entity schema ot given type
   */
  public static PayloadEntitySchema of(Class<?> type) {
    return new PayloadEntitySchema(type.getSimpleName());
  }

  /**
   Get the resource type of this entity schema

   @return resource type of this entity schema
   */
  public String getType() {
    return type;
  }

  /**
   Get the set of belongs-to types

   @return set of belongs-to types
   */
  public Set<String> getBelongsTo() {
    return ImmutableSet.copyOf(belongsToTypes);
  }

  /**
   Get the set of has-many types

   @return set of has-many types
   */
  public Set<String> getHasMany() {
    return ImmutableSet.copyOf(hasManyTypes);
  }

  /**
   Get the set of Attributes

   @return set of Attributes
   */
  public Set<String> getAttributes() {
    return ImmutableSet.copyOf(attributes);
  }

  /**
   Set creator function which creates a new instance of this type of entity

   @param creator function which creates a new instance of this type of entity
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema createdBy(Supplier<Object> creator) {
    this.creator = creator;
    return this;
  }

  /**
   Get creator function which creates a new instance of this type of entity

   @return creator function which creates a new instance of this type of entity
   */
  public Supplier<Object> getCreator() {
    return creator;
  }

  /**
   Add attribute for this type of entity

   @param name of attribute
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema withAttribute(String name) {
    this.attributes.add(PayloadKey.toAttributeName(name));
    return this;
  }

  /**
   Set the attributes of this type of entity

   @param names of this entity, just the names of the attributes
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema withAttributes(String... names) {
    for (String name : names) withAttribute(name);
    return this;
  }

  /**
   Add has-many type for this type of entity

   @param typeName which this entity has-many
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema hasMany(String typeName) {
    this.hasManyTypes.add(PayloadKey.toResourceHasMany(typeName));
    return this;
  }

  /**
   Add has-many type for this type of entity

   @param typeNames which this entity has-many
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema hasMany(String... typeNames) {
    for (String typeName : typeNames) hasMany(typeName);
    return this;
  }

  /**
   Add has-many type for this type of entity

   @param typeName which this entity has-many
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema hasMany(Class<?> typeName) {
    this.hasManyTypes.add(PayloadKey.toResourceHasMany(typeName));
    return this;
  }

  /**
   Add has-many type for this type of entity

   @param typeNames which this entity has-many
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema hasMany(Class<?>... typeNames) {
    for (Class<?> typeName : typeNames) hasMany(typeName);
    return this;
  }

  /**
   Add belongs-to type for this type of entity

   @param typeName which this entity belongs-to
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema belongsTo(String typeName) {
    this.belongsToTypes.add(PayloadKey.toResourceBelongsTo(typeName));
    return this;
  }

  /**
   Add belongs-to type for this type of entity

   @param typeNames which this entity belongs-to
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema belongsTo(String... typeNames) {
    for (String typeName : typeNames) belongsTo(typeName);
    return this;
  }

  /**
   Add belongs-to type for this type of entity

   @param typeName which this entity belongs-to
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema belongsTo(Class<?> typeName) {
    this.belongsToTypes.add(PayloadKey.toResourceBelongsTo(typeName));
    return this;
  }

  /**
   Add belongs-to type for this type of entity

   @param typeNames which this entity belongs-to
   @return this entity type schema (for chaining methods)
   */
  public PayloadEntitySchema belongsTo(Class<?>... typeNames) {
    for (Class<?> typeName : typeNames) belongsTo(typeName);
    return this;
  }

}
