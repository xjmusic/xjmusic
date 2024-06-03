// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model.entity;

import io.xj.model.util.CsvUtils;
import io.xj.model.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 Utilities for Object having id, type, attributes, and has-many/belongs-to relationship with other Entities.
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public enum EntityUtils {
  ;
  public static final String NAME_KEY = "name";
  static final Logger LOG = LoggerFactory.getLogger(EntityUtils.class);
  static final String SIMPLE_NAME_OF_NULL = "null";
  static final String ID_KEY = "id";
  static final String UPDATED_AT_KEY = "updatedAt";

  /**
   Get a value of a target object via getter method

   @param getter to use
   @return value
   @throws EntityException on access failure
   */
  public static Optional<Object> get(Object target, Method getter) throws EntityException {
    try {
      Object value = getter.invoke(target);
      if (Objects.isNull(value)) return Optional.empty();
      return switch (value.getClass().getSimpleName().toLowerCase()) {
        case "uinteger", "integer", "int" -> Optional.of(Integer.valueOf(String.valueOf(value)));
        case "long" -> Optional.of(Long.valueOf(String.valueOf(value)));
        case "double" -> Optional.of(Double.valueOf(String.valueOf(value)));
        case "float" -> Optional.of(Float.valueOf(String.valueOf(value)));
        case "timestamp" ->
          Optional.of(Timestamp.valueOf(String.valueOf(value)).toInstant().truncatedTo(ChronoUnit.MICROS));
        case "date" -> Optional.of(Instant.ofEpochMilli(((Date) value).getTime()).truncatedTo(ChronoUnit.MICROS));
        case "ulong", "biginteger" -> Optional.of(new BigInteger(String.valueOf(value)));
        case "string" -> String.valueOf(value).isBlank() ? Optional.empty() : Optional.of(String.valueOf(value));
        default -> Optional.of(value);
      };
    } catch (InvocationTargetException e) {
      throw new EntityException(String.format("Failed to %s.%s(), reason: %s",
        getSimpleName(target), getter.getName(), e.getTargetException().getMessage()));

    } catch (IllegalAccessException e) {
      throw new EntityException(String.format("Could not access %s.%s(), reason: %s",
        getSimpleName(target), getter.getName(), e.getMessage()));
    }
  }

  /**
   Set a non-null value using a setter method

   @param target on which to set
   @param setter method
   @param value  to set
   @throws EntityException on failure to access setter
   */
  public static void set(Object target, Method setter, Object value) throws EntityException {
    try {
      if (0 == setter.getParameterTypes().length)
        throw new NoSuchMethodException("Setter accepts no parameters!");

      Class<?> type = setter.getParameterTypes()[0];
      switch (getSimpleName(type).toLowerCase()) {
        case "biginteger" -> setter.invoke(target, new BigInteger(String.valueOf(value)));
        case "uuid" -> setter.invoke(target, UUID.fromString(String.valueOf(value)));
        case "short" -> setter.invoke(target, Short.valueOf(String.valueOf(value)));
        case "integer", "int" -> setter.invoke(target, Integer.valueOf(String.valueOf(value)));
        case "long" -> setter.invoke(target, Long.valueOf(String.valueOf(value)));
        case "instant" -> {
          if (value.getClass().isAssignableFrom(Instant.class))
            setter.invoke(target, value);
          else if (value.getClass().isAssignableFrom(Timestamp.class))
            setter.invoke(target, ((Timestamp) value).toInstant());
          else
            setter.invoke(target, Instant.parse(String.valueOf(value)));
        }
        case "timestamp" -> {
          if (value.getClass().isAssignableFrom(Timestamp.class))
            setter.invoke(target, value);
          else if (value.getClass().isAssignableFrom(Instant.class))
            setter.invoke(target, Timestamp.from((Instant) value));
          else
            setter.invoke(target, Timestamp.from(Instant.parse(String.valueOf(value))));
        }
        case "date" -> {
          if (value.getClass().isAssignableFrom(Date.class))
            setter.invoke(target, value);
          else if (value.getClass().isAssignableFrom(Instant.class))
            setter.invoke(target, Date.from((Instant) value));
          else if (value.getClass().isAssignableFrom(Timestamp.class))
            setter.invoke(target, Date.from(((Timestamp) value).toInstant()));
          else
            setter.invoke(target, Date.from(Instant.parse(String.valueOf(value))));
        }
        case "localdatetime" -> {
          if (value.getClass().isAssignableFrom(LocalDateTime.class))
            setter.invoke(target, value);
          else if (value.getClass().isAssignableFrom(Instant.class))
            setter.invoke(target, LocalDateTime.from((Instant) value));
          else if (value.getClass().isAssignableFrom(Timestamp.class))
            setter.invoke(target, LocalDateTime.from(((Timestamp) value).toInstant()));
          else
            setter.invoke(target, LocalDateTime.parse(String.valueOf(value)));
        }
        case "double" -> setter.invoke(target, Double.valueOf(String.valueOf(value)));
        case "float" -> setter.invoke(target, Float.valueOf(String.valueOf(value)));
        case "boolean" -> setter.invoke(target, Boolean.valueOf(String.valueOf(value)));
        default -> {
          if (type.isAssignableFrom(String.class))
            setter.invoke(target, String.valueOf(value));
          else if (type.isEnum())
            setter.invoke(target, enumValue(type, String.valueOf(value)));
          else
            setter.invoke(target, value);
        }
      }

    } catch (InvocationTargetException e) {
      throw new EntityException(String.format("Failed to %s.%s(), reason: %s",
        getSimpleName(target), setter.getName(), e.getTargetException().getMessage()), e);

    } catch (IllegalAccessException e) {
      throw new EntityException(String.format("Could not access %s.%s(), reason: %s",
        getSimpleName(target), setter.getName(), e.getMessage()), e);

    } catch (IllegalArgumentException e) {
      throw new EntityException(String.format("Could not provide value for %s.%s(), reason: %s",
        getSimpleName(target), setter.getName(), e.getMessage()), e);

    } catch (NoSuchMethodException e) {
      throw new EntityException(String.format("No such method %s.%s(), reason: %s",
        getSimpleName(target), setter.getName(), e.getMessage()), e);
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
      if (Objects.equals(setterName, method.getName())) {
        set(target, method, value);
        return;
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
      if (Objects.equals(getterName, method.getName())) {
        return get(target, method);
      }

    return Optional.empty();
  }

  /**
   get Object ID

   @param entity to get id from
   @return Object ID
   */
  public static <N> UUID getId(N entity) throws EntityException {
    Optional<Object> id = get(entity, ID_KEY);
    return id.map(o -> UUID.fromString(String.valueOf(o))).orElse(null);
  }

  /**
   Get a collection of entity IDs from a collection of entities

   @param entities of which to get ids
   @param <N>      type of entity
   @return collection of entity ids
   */
  public static <N> Collection<UUID> getIds(Collection<N> entities) {
    return entities.stream()
      .flatMap(entity -> {
        try {
          return Stream.of(EntityUtils.getId(entity));
        } catch (Exception e) {
          return Stream.empty();
        }
      })
      .toList();
  }


  /**
   get Object Updated At

   @param target to get updatedAt from
   @return Object Updated At
   */
  public static <N> Long getUpdatedAt(N target) throws EntityException {
    Optional<Object> updatedAt = get(target, UPDATED_AT_KEY);
    return updatedAt.map(o -> Long.valueOf(String.valueOf(o))).orElse(null);
  }

  /**
   set Object ID

   @param target to set id
   @param id     to set on target
   */
  public static <N> void setId(N target, UUID id) throws EntityException {
    set(target, ID_KEY, id);
  }

  /**
   get Object type

   @return Object Type
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
    return (T) Arrays.stream(type.getEnumConstants()).filter(n -> n.toString().equals(value)).findFirst()
      .orElseThrow(() -> new IllegalArgumentException(String.format("No enum constant %s.%s", type.getSimpleName(), value)));
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
      "impl".equalsIgnoreCase(entityClass.getSimpleName().substring(entityClass.getSimpleName().length() - 4)))
      return entityClass.getInterfaces()[0].getSimpleName();
    else
      return entityClass.getSimpleName();
  }


  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + ProjectUser.class -> "project-users"
   + Library.class -> "libraries"

   @param resource to get resource type of
   @return resource type of object
   */
  public static String toResourceType(Class<?> resource) {
    return toResourceType(StringUtils.getSimpleName(resource));
  }

  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + ProjectUser.class -> "project-users"
   + Library.class -> "libraries"

   @param type to conform
   @return conformed resource type
   */
  static String toResourceType(String type) {
    return StringUtils.toPlural(StringUtils.camelToKebabCase(type));
  }

  /**
   To a thingId style attribute of an object

   @param obj to add ID to
   @return id attribute of key
   */
  public static String toIdAttribute(Object obj) {
    return String.format("%sId", EntityUtils.toBelongsTo(obj));
  }

  /**
   To a thingId style attribute of an object's class

   @param key to add ID to
   @return id attribute of key
   */
  public static String toIdAttribute(Class<?> key) {
    return String.format("%sId", EntityUtils.toBelongsTo(key));
  }

  /**
   To an thingId style attribute

   @param key to add ID to
   @return id attribute of key
   */
  public static String toIdAttribute(String key) {
    return String.format("%sId", EntityUtils.toBelongsTo(key));
  }

  /**
   get belongs-to relationship name of object, the key to use when this class is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + ProjectUser.class -> "projectUser"
   + Object.class -> "entity"

   @param belongsTo to get resource belongsTo of
   @return resource belongsTo of object
   */
  public static String toBelongsTo(Object belongsTo) {
    return toBelongsTo(EntityUtils.getSimpleName(belongsTo));
  }

  /**
   get belongs-to relationship name of class, the key to use when this class is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + ProjectUser.class -> "projectUser"
   + Object.class -> "entity"

   @param belongsTo to get resource belongsTo of
   @return resource belongsTo of object
   */
  public static String toBelongsTo(Class<?> belongsTo) {
    return toBelongsTo(EntityUtils.getSimpleName(belongsTo));
  }

  /**
   get belongs-to relationship name, to use when this key is the target of a belongs-to relationship
   + Chain.class -> "chain"
   + ProjectUser.class -> "projectUser"
   + Object.class -> "entity"

   @param belongsTo to conform
   @return conformed resource belongsTo
   */
  public static String toBelongsTo(String belongsTo) {
    return StringUtils.firstLetterToLowerCase(StringUtils.toSingular(belongsTo));
  }

  /**
   get belongs-to relationship name, to use when this key is the target of a belongs-to relationship
   FROM a resource type
   + "chains" -> "chain"
   + "project-users" -> "projectUser"
   + "entities" -> "entity"

   @param type to conform
   @return conformed resource hasMany
   */
  public static String toBelongsToFromType(String type) {
    return StringUtils.snakeToLowerCamelCase(StringUtils.toSingular(type));
  }

  /**
   get has-many relationship name of class, the key to use when this class is the target of a has-many relationship
   + Chain.class -> "chains"
   + ProjectUser.class -> "projectUsers"
   + Object.class -> "entities"

   @param resource to get resource hasMany of
   @return resource hasMany of object
   */
  public static String toHasMany(Class<?> resource) {
    return toHasMany(EntityUtils.getSimpleName(resource));
  }

  /**
   get has-many relationship name of object, the key to use when this class is the target of a has-many relationship
   + Chain.class -> "chains"
   + ProjectUser.class -> "projectUsers"
   + Object.class -> "entities"

   @param resource to get resource hasMany of
   @return resource hasMany of object
   */
  public static String toHasMany(Object resource) {
    return toHasMany(EntityUtils.getSimpleName(resource));
  }

  /**
   get has-many relationship name, to use when this key is the target of a has-many relationship
   + Chain.class -> "chains"
   + ProjectUser.class -> "projectUsers"
   + Object.class -> "entities"

   @param hasMany to conform
   @return conformed resource hasMany
   */
  public static String toHasMany(String hasMany) {
    return StringUtils.firstLetterToLowerCase(StringUtils.toPlural(hasMany));
  }

  /**
   get has-many relationship name, to use when this key is the target of a has-many relationship
   FROM a resource type
   + "chains" -> "chains"
   + "project-users" -> "projectUsers"
   + "entities" -> "entities"

   @param type to conform
   @return conformed resource hasMany
   */
  public static String toHasManyFromType(String type) {
    return StringUtils.kebabToLowerCamelCase(StringUtils.toPlural(type));
  }

  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + ProjectUser.class -> "project-users"
   + Object.class -> "entities"

   @param resource to get resource type of
   @return resource type of object
   */
  public static String toType(Class<?> resource) {
    return toType(EntityUtils.getSimpleName(resource));
  }

  /**
   Get resource type for any object, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + ProjectUser.class -> "project-users"
   + Object.class -> "entities"

   @param resource to get resource type of
   @return resource type of object
   */
  public static String toType(Object resource) {
    return toType(EntityUtils.getSimpleName(resource));
  }

  /**
   Get resource type for any class, which is hyphenated lowercase pluralized
   + Chain.class -> "chains"
   + ProjectUser.class -> "project-users"
   + Object.class -> "entities"

   @param type to conform
   @return conformed resource type
   */
  public static String toType(String type) {
    return StringUtils.toPlural(StringUtils.camelToKebabCase(type));
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

   @param entity        to get belongsToId attribute value from
   @param belongsToType of belong-to relationship to get
   @param <R>           entity type
   @return Optional<String> value of belongsToId attribute from given entity
   @throws EntityException on failure to get relationship value
   */
  public static <R> Optional<UUID> getBelongsToId(R entity, String belongsToType) throws EntityException {
    Optional<Object> raw = get(entity, toIdAttribute(toBelongsTo(belongsToType)));
    if (raw.isEmpty()) return Optional.empty();
    try {
      return Optional.of(UUID.fromString(String.valueOf(raw.get())));
    } catch (Exception e) {
      throw new EntityException("Failed to get String value", e);
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
   Add an entity to a collection, then return that entity

   @param to     collection
   @param entity to add
   @param <R>    type of entity
   @return entity that's been added
   */
  public static <R> R add(Collection<Object> to, R entity) {
    to.add(entity);
    return entity;
  }

  /**
   CSV string of the ids of a list of entities

   @param entities to get ids of
   @param <E>      type of entity
   @return CSV list of entity ids
   */
  public static <E> String csvIdsOf(Collection<E> entities) {
    if (Objects.isNull(entities) || entities.isEmpty()) {
      return "";
    }
    Iterator<E> it = entities.iterator();
    StringBuilder result = new StringBuilder();
    try {
      result.append(EntityUtils.getId(it.next()));
      while (it.hasNext()) {
        result.append(",").append(EntityUtils.getId(it.next()));
      }
    } catch (Exception e) {
      LOG.error("Failed to get id of entity", e);
    }
    return result.toString();
  }

  /**
   ids of an entity set

   @param entities to get ids of
   @return ids
   */
  public static <N> Set<UUID> idsOf(Collection<N> entities) {
    return entities.stream()
      .flatMap(EntityUtils::flatMapIds)
      .collect(Collectors.toSet());
  }

  /**
   For flat-mapping an entity to a stream of id or empty stream

   @param n       to flat map
   @param <N>type
   @return stream of id or empty stream
   */
  public static <N> Stream<UUID> flatMapIds(N n) {
    try {
      return Stream.of(EntityUtils.getId(n));
    } catch (Exception e) {
      LOG.error("Failed to get id of entity", e);
      return Stream.empty();
    }
  }

  /**
   extract a collection of ids of a string CSV

   @param csv to parse
   @return collection of ids
   */
  public static Collection<UUID> idsFromCSV(String csv) {
    Collection<UUID> result = new ArrayList<>();

    if (Objects.nonNull(csv) && !csv.isEmpty()) {
      result = CsvUtils.split(csv).stream().map(UUID::fromString).distinct().collect(Collectors.toList());
    }

    return result;
  }

  /**
   Whether target resource belongs to the specified resource

   @param child      to test for childhood
   @param parentType to test whether this entity belongs to
   @param parentIds  to test whether this entity belongs to
   @return true if target belongs to the specified resource
   */
  public static boolean isChild(Object child, Class<?> parentType, Collection<UUID> parentIds) {
    try {
      var keyOpt = get(child, toIdAttribute(parentType));
      if (keyOpt.isEmpty()) return false;
      var key = UUID.fromString(String.valueOf(keyOpt.get()));
      return parentIds.contains(key);
    } catch (Exception e) {
      return false;
    }
  }

  /**
   Whether this entity is the child of another entity

   @param parent entity to test for parenthood
   @param child  to test for childhood
   @return true if this entity is a child of the target entity
   */
  public static boolean isChild(Object child, Object parent) {
    try {
      return isChild(child, parent.getClass(), Collections.singleton(EntityUtils.getId(parent)));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   Whether this entity is the parent of another entity

   @param child entity to test for parenthood
   @return true if this entity is a parent of the target entity
   */
  public static boolean isParent(Object parent, Object child) {
    return isChild(child, parent);
  }

  /**
   Whether this entity is the same as another entity

   @param a entity to check for same type and id
   @param b entity to check for same type and id
   @return true if this and the target entity have the same type and id
   */
  public static boolean isSame(Object a, Object b) {
    try {
      return Objects.equals(EntityUtils.getType(a), EntityUtils.getType(b)) &&
        Objects.equals(EntityUtils.getId(a), EntityUtils.getId(b));
    } catch (Exception e) {
      return false;
    }
  }

  /**
   Map the meme names of a group of memes

   @param memeEntities to get meme names of
   @return meme names
   */
  public static Collection<String> namesOf(Collection<?> memeEntities) {
    return memeEntities.stream()
      .flatMap(e -> {
        try {
          return Stream.of(String.valueOf(get(e, NAME_KEY).orElseThrow()));
        } catch (Exception ignored) {
          return Stream.empty();
        }
      }).collect(Collectors.toList());
  }

  /**
   Whether an entity is a given type

   @param entity to test
   @param type   for which to test
   @return true if entity is this type
   */
  public static boolean isType(Object entity, Class<?> type) {
    return getType(entity).equals(toType(type));
  }

  /**
   Get CSV of a collection of UUIDs

   @param ids for which to get CSV
   @return CSV of uuids
   */
  public static String csvOf(Collection<UUID> ids) {
    return CsvUtils.join(ids.stream().map(UUID::toString).toList());
  }
}
