// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.entity;

import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

/**
 Store the schema for one entity type: its attributes, belongs-to and has-many relationships, and creator function.
 <p>
 Note that has-many relationships are updated after creation, when child entities are registered as belonging-to
 */
public class EntitySchema {
  final String type;
  final Set<String> attributes;
  final Set<String> hasManyTypes;
  final Set<String> belongsToTypes;
  Supplier<?> creator;

  /**
   Constructor initializes empty inner sets
   */
  EntitySchema(String type) {
    this.type = EntityUtils.toType(type);
    attributes = new TreeSet<>();
    hasManyTypes = new TreeSet<>();
    belongsToTypes = new TreeSet<>();
  }

  /**
   Create a new instance

   @param type of entity schema to create
   @return new entity schema ot given type
   */
  public static EntitySchema of(String type) {
    return new EntitySchema(type);
  }

  /**
   Create a new instance

   @param type of entity schema to create
   @return new entity schema ot given type
   */
  public static EntitySchema of(Class<?> type) {
    return new EntitySchema(type.getSimpleName());
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
    return Set.copyOf(belongsToTypes);
  }

  /**
   Get the set of has-many types

   @return set of has-many types
   */
  public Set<String> getHasMany() {
    return Set.copyOf(hasManyTypes);
  }

  /**
   Get the set of Attributes

   @return set of Attributes
   */
  public Set<String> getAttributes() {
    return Set.copyOf(attributes);
  }

  /**
   Set creator function which creates a new instance of this type of entity

   @param creator function which creates a new instance of this type of entity
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema createdBy(Supplier<?> creator) {
    this.creator = creator;
    return this;
  }

  /**
   Get creator function which creates a new instance of this type of entity

   @return creator function which creates a new instance of this type of entity
   */
  public Supplier<?> getCreator() {
    return creator;
  }

  /**
   Add attribute for this type of entity

   @param name of attribute
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema withAttribute(String name) {
    this.attributes.add(EntityUtils.toAttributeName(name));
    return this;
  }

  /**
   Set the attributes of this type of entity

   @param names of this entity, just the names of the attributes
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema withAttributes(String... names) {
    for (String name : names) withAttribute(name);
    return this;
  }

  /**
   Add has-many type for this type of entity

   @param typeName which this entity has-many
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema hasMany(String typeName) {
    this.hasManyTypes.add(EntityUtils.toHasMany(typeName));
    return this;
  }

  /**
   Add has-many type for this type of entity

   @param typeNames which this entity has-many
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema hasMany(String... typeNames) {
    for (String typeName : typeNames) hasMany(typeName);
    return this;
  }

  /**
   Add has-many type for this type of entity

   @param typeName which this entity has-many
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema hasMany(Class<?> typeName) {
    this.hasManyTypes.add(EntityUtils.toHasMany(typeName));
    return this;
  }

  /**
   Add has-many type for this type of entity

   @param typeNames which this entity has-many
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema hasMany(Class<?>... typeNames) {
    for (Class<?> typeName : typeNames) hasMany(typeName);
    return this;
  }

  /**
   Add belongs-to type for this type of entity

   @param typeName which this entity belongs-to
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema belongsTo(String typeName) {
    this.belongsToTypes.add(EntityUtils.toBelongsTo(typeName));
    return this;
  }

  /**
   Add belongs-to type for this type of entity

   @param typeNames which this entity belongs-to
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema belongsTo(String... typeNames) {
    for (String typeName : typeNames) belongsTo(typeName);
    return this;
  }

  /**
   Add belongs-to type for this type of entity

   @param typeName which this entity belongs-to
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema belongsTo(Class<?> typeName) {
    this.belongsToTypes.add(EntityUtils.toBelongsTo(typeName));
    return this;
  }

  /**
   Add belongs-to type for this type of entity

   @param typeNames which this entity belongs-to
   @return this entity type schema (for chaining methods)
   */
  public EntitySchema belongsTo(Class<?>... typeNames) {
    for (Class<?> typeName : typeNames) belongsTo(typeName);
    return this;
  }

}
