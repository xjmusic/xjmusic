// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.entity;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 Utilities for Entity having id, type, attributes, and has-many/belongs-to relationship with other Entities.
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public enum Entities {
  ;

  private static final String SIMPLE_NAME_OF_NULL = "null";

  /**
   Get a value of a target object via getter method

   @param getter to use
   @return value
   @throws InvocationTargetException on failure to invoke method
   @throws IllegalAccessException    on access failure
   */
  public static Optional<Object> get(Object target, Method getter) throws InvocationTargetException, IllegalAccessException {
    Object value = getter.invoke(target);
    if (Objects.isNull(value)) return Optional.empty();
    switch (value.getClass().getSimpleName().toLowerCase()) {

      case "uinteger":
      case "integer":
      case "int":
        return Optional.of(Integer.valueOf(String.valueOf(value)));

      case "long":
        return Optional.of(Long.valueOf(String.valueOf(value)));

      case "double":
        return Optional.of(Double.valueOf(String.valueOf(value)));

      case "float":
        return Optional.of(Float.valueOf(String.valueOf(value)));

      case "timestamp":
        return Optional.of(Timestamp.valueOf(String.valueOf(value)).toInstant().truncatedTo(ChronoUnit.MICROS));

      case "ulong":
      case "biginteger":
        return Optional.of(new BigInteger(String.valueOf(value)));

      case "string":
        return Optional.of(String.valueOf(value));

      default:
        return Optional.of(value);
    }
  }

  /**
   Set a non-null value using a setter method

   @param target on which to set
   @param setter method
   @param value  to set
   @throws InvocationTargetException on failure to invoke setter
   @throws IllegalAccessException    on failure to access setter
   */
  public static void set(Object target, Method setter, Object value) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
    if (0 == setter.getParameterTypes().length)
      throw new NoSuchMethodException("Setter accepts no parameters!");

    Class<?> type = setter.getParameterTypes()[0];
    switch (getSimpleName(type).toLowerCase()) {

      case "biginteger":
        setter.invoke(target, new BigInteger(String.valueOf(value)));
        break;

      case "uuid":
        setter.invoke(target, UUID.fromString(String.valueOf(value)));
        break;

      case "short":
        setter.invoke(target, Short.valueOf(String.valueOf(value)));
        break;

      case "integer":
      case "int":
        setter.invoke(target, Integer.valueOf(String.valueOf(value)));
        break;

      case "long":
        setter.invoke(target, Long.valueOf(String.valueOf(value)));
        break;

      case "instant":
        if (value.getClass().isAssignableFrom(Instant.class))
          setter.invoke(target, value);
        else if (value.getClass().isAssignableFrom(Timestamp.class))
          setter.invoke(target, ((Timestamp) value).toInstant());
        else
          setter.invoke(target, Instant.parse(String.valueOf(value)));
        break;

      case "timestamp":
        if (value.getClass().isAssignableFrom(Timestamp.class))
          setter.invoke(target, value);
        else if (value.getClass().isAssignableFrom(Instant.class))
          setter.invoke(target, Timestamp.from((Instant) value));
        else
          setter.invoke(target, Timestamp.from(Instant.parse(String.valueOf(value))));
        break;

      case "double":
        setter.invoke(target, Double.valueOf(String.valueOf(value)));
        break;

      case "float":
        setter.invoke(target, Float.valueOf(String.valueOf(value)));
        break;

      case "boolean":
        setter.invoke(target, Boolean.valueOf(String.valueOf(value)));
        break;

      default:
        if (type.isAssignableFrom(Timestamp.class))
          setter.invoke(target,
            Timestamp.valueOf(String.valueOf(value)).toInstant().truncatedTo(ChronoUnit.MICROS).toString());
        else if (type.isAssignableFrom(String.class))
          setter.invoke(target, String.valueOf(value));
        else if (type.isEnum())
          setter.invoke(target, enumValue(type, String.valueOf(value)));
        else
          setter.invoke(target, value);
        break;
    }
  }

  /**
   Set a value using an attribute name

   @param target        object to set attribute on
   @param attributeName of attribute for which to find setter method
   @param value         to set
   */
  public static <N> void set(N target, String attributeName, Object value) throws EntityException {
    if (Objects.isNull(value)) return;

    String setterName = toSetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(setterName, method.getName()))
        try {
          set(target, method, value);
          return;

        } catch (InvocationTargetException e) {
          throw new EntityException(String.format("Failed to %s.%s(), reason: %s", getSimpleName(target), setterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new EntityException(String.format("Could not access %s.%s(), reason: %s", getSimpleName(target), setterName, e.getMessage()));

        } catch (NoSuchMethodException e) {
          throw new EntityException(String.format("No such method %s.%s(), reason: %s", getSimpleName(target), setterName, e.getMessage()));
        }

    throw new EntityException(String.format("%s has no attribute '%s'", getSimpleName(target), attributeName));
  }

  /**
   Get a value of a target object via attribute name

   @param target        to get attribute from
   @param attributeName of attribute to get
   @return value gotten from target attribute
   @throws EntityException on failure to get
   */
  public static <N> Optional<Object> get(N target, String attributeName) throws EntityException {
    String getterName = toGetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(getterName, method.getName()))
        try {
          return get(target, method);

        } catch (InvocationTargetException e) {
          throw new EntityException(String.format("Failed to %s.%s(), reason: %s", getSimpleName(target), getterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new EntityException(String.format("Could not access %s.%s(), reason: %s", getSimpleName(target), getterName, e.getMessage()));
        }

    return Optional.empty();
  }

  /**
   get Entity ID

   @return Entity Id
   */
  public static <N> String getId(N target) throws EntityException {
    Optional<Object> id = get(target, "id");
    if (id.isEmpty()) throw new EntityException("Has no id");
    return (id.get().toString());
  }

  /**
   get Entity type

   @return Entity Type
   */
  public static <N> String getType(N target) {
    return toType(target);
  }

  /**
   Get the enum value for a given string value and enum class

   @param type  enum class
   @param value of enum
   @return enum of given class and value
   */
  public static <T extends Enum<T>> T enumValue(Class<?> type, String value) {
    //noinspection unchecked
    Class<T> enumType = (Class<T>) type;
    return valueOf(enumType, String.valueOf(value));
  }

  /**
   Get class simple name, or interface class simple name if it exists

   @param entity to get name of
   @return entity name
   */
  public static String getSimpleName(Object entity) {
    return Objects.nonNull(entity) ? getSimpleName(entity.getClass()) : SIMPLE_NAME_OF_NULL;
  }

  /**
   Get class simple name, or interface class simple name if it exists

   @param entityClass to get name of
   @return entity name
   */
  public static String getSimpleName(Class<?> entityClass) {
    if (entityClass.isInterface())
      return entityClass.getSimpleName();
    if (0 < entityClass.getInterfaces().length &&
      "impl".equals(entityClass.getSimpleName().substring(entityClass.getSimpleName().length() - 4).toLowerCase()))
      return entityClass.getInterfaces()[0].getSimpleName();
    else
      return entityClass.getSimpleName();
  }


  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Library.class -> "libraries"

   @param resource to get resource type of
   @return resource type of object
   */
  public static String toResourceType(Class<?> resource) {
    return toResourceType(Text.getSimpleName(resource));
  }

  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Library.class -> "libraries"

   @param type to conform
   @return conformed resource type
   */
  static String toResourceType(String type) {
    return Text.toPlural(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, type));
  }

  /**
   To a thingId style attribute of an object

   @param obj to add Id to
   @return id attribute of key
   */
  public static String toIdAttribute(Object obj) {
    return String.format("%sId", Entities.toBelongsTo(obj));
  }

  /**
   To a thingId style attribute of an object's class

   @param key to add Id to
   @return id attribute of key
   */
  public static String toIdAttribute(Class<?> key) {
    return String.format("%sId", Entities.toBelongsTo(key));
  }

  /**
   To an thingId style attribute

   @param key to add Id to
   @return id attribute of key
   */
  public static String toIdAttribute(String key) {
    return String.format("%sId", Entities.toBelongsTo(key));
  }

  /**
   get belongs-to relationship name of object, the key to use when this class is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + AccountUser.class -> "accountUser"
   + Entity.class -> "entity"

   @param belongsTo to get resource belongsTo of
   @return resource belongsTo of object
   */
  public static String toBelongsTo(Object belongsTo) {
    return toBelongsTo(Entities.getSimpleName(belongsTo));
  }

  /**
   get belongs-to relationship name of class, the key to use when this class is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + AccountUser.class -> "accountUser"
   + Entity.class -> "entity"

   @param belongsTo to get resource belongsTo of
   @return resource belongsTo of object
   */
  public static String toBelongsTo(Class<?> belongsTo) {
    return toBelongsTo(Entities.getSimpleName(belongsTo));
  }

  /**
   get belongs-to relationship name, to use when this key is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + AccountUser.class -> "accountUser"
   + Entity.class -> "entity"

   @param belongsTo to conform
   @return conformed resource belongsTo
   */
  public static String toBelongsTo(String belongsTo) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, Text.toSingular(belongsTo));
  }

  /**
   get belongs-to relationship name, to use when this key is the target of a belongs-to relationship
   FROM a resource type
   + "chains" -> "chain"
   + "account-users" -> "accountUser"
   + "entities" -> "entity"

   @param type to conform
   @return conformed resource hasMany
   */
  public static String toBelongsToFromType(String type) {
    return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, Text.toSingular(type));
  }

  /**
   get has-many relationship name of class, the key to use when this class is the target of a has-many relationship
   + Chain.class -> "chains"
   + AccountUser.class -> "accountUsers"
   + Entity.class -> "entities"

   @param resource to get resource hasMany of
   @return resource hasMany of object
   */
  public static String toHasMany(Class<?> resource) {
    return toHasMany(Entities.getSimpleName(resource));
  }

  /**
   get has-many relationship name of object, the key to use when this class is the target of a has-many relationship
   + Chain.class -> "chains"
   + AccountUser.class -> "accountUsers"
   + Entity.class -> "entities"

   @param resource to get resource hasMany of
   @return resource hasMany of object
   */
  public static String toHasMany(Object resource) {
    return toHasMany(Entities.getSimpleName(resource));
  }

  /**
   get has-many relationship name, to use when this key is the target of a has-many relationship
   + Chain.class -> "chains"
   + AccountUser.class -> "accountUsers"
   + Entity.class -> "entities"

   @param hasMany to conform
   @return conformed resource hasMany
   */
  public static String toHasMany(String hasMany) {
    return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, Text.toPlural(hasMany));
  }

  /**
   get has-many relationship name, to use when this key is the target of a has-many relationship
   FROM a resource type
   + "chains" -> "chains"
   + "account-users" -> "accountUsers"
   + "entities" -> "entities"

   @param type to conform
   @return conformed resource hasMany
   */
  public static String toHasManyFromType(String type) {
    return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, Text.toPlural(type));
  }

  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Entity.class -> "entities"

   @param resource to get resource type of
   @return resource type of object
   */
  public static String toType(Class<?> resource) {
    return toType(Entities.getSimpleName(resource));
  }

  /**
   Get resource type for any object, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Entity.class -> "entities"

   @param resource to get resource type of
   @return resource type of object
   */
  public static String toType(Object resource) {
    return toType(Entities.getSimpleName(resource));
  }

  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + AccountUser.class -> "account-users"
   + Entity.class -> "entities"

   @param type to conform
   @return conformed resource type
   */
  public static String toType(String type) {
    return Text.toPlural(CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, type));
  }

  /**
   Compute an attribute name based on the name of the getter method,
   by removing the first three letters "get", then lower-casing the new first letter.
   <p>
   e.g., input of "getNewsPaper" results in "newsPaper"

   @param method for which to compute name of attribute
   @return attribute name
   */
  public static String toAttributeName(Method method) {
    return String.format("%s%s",
      method.getName().substring(3, 4).toLowerCase(Locale.ENGLISH),
      method.getName().substring(4));
  }

  /**
   Compute a setter method name based on the name of the attribute,
   capitalize the first letter, then prepend "set"
   <p>
   e.g., input of "newsPaper" results in "setNewsPaper"

   @param attributeName for which to get name of setter method
   @return attribute name
   */
  public static String toSetterName(String attributeName) {
    return String.format("%s%s%s", "set",
      attributeName.substring(0, 1).toUpperCase(Locale.ENGLISH),
      attributeName.substring(1));
  }

  /**
   Compute an attribute name, by just lower-casing the first better
   <p>
   e.g., input of "NewsPaper" results in "newsPaper"

   @param name for conversion to attribute name
   @return attribute name
   */
  public static String toAttributeName(String name) {
    return String.format("%s%s",
      name.substring(0, 1).toLowerCase(Locale.ENGLISH),
      name.substring(1));
  }

  /**
   Get the belongsToId attribute value for a given target entity and key

   @param entity to get belongsToId attribute value from
   @param key    of belong-to relationship to get
   @param <R>    entity type
   @return Optional<UUID> value of belongsToId attribute from given entity
   @throws EntityException on failure to get relationship value
   */
  public static <R> Optional<UUID> getBelongsToId(R entity, String key) throws EntityException {
    Optional<Object> raw = get(entity, toIdAttribute(toBelongsTo(key)));
    if (raw.isEmpty()) return Optional.empty();
    try {
      return Optional.of(UUID.fromString(String.valueOf(raw.get())));
    } catch (Exception e) {
      throw new EntityException("Failed to get UUID value", e);
    }
  }

  /**
   Compute a getter method name based on the name of the attribute,
   capitalize the first letter, then prepend "get"
   <p>
   e.g., input of "newsPaper" results in "getNewsPaper"

   @param attributeName for which to get name of getter method
   @return attribute name
   */
  public static String toGetterName(String attributeName) {
    return String.format("%s%s%s", "get",
      attributeName.substring(0, 1).toUpperCase(Locale.ENGLISH),
      attributeName.substring(1));
  }

  /**
   Get a string representation of an entity, comprising a key-value map of its properties

   @param name       of entity
   @param properties to map
   @return string representation
   */
  public static String toKeyValueString(String name, ImmutableMap<String, String> properties) {
    return String.format("%s{%s}", name, CSV.from(properties));
  }

  /**
   Add an entity to a collection, then return that entity

   @param to     collection
   @param entity to add
   @param <N>    type of entity
   @return entity that's been added
   */
  public static <N extends Entity> N add(Collection<Entity> to, N entity) {
    to.add(entity);
    return entity;
  }
}
