//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.entity;

import com.google.api.client.util.Lists;
import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;
import com.google.common.collect.Ordering;
import com.google.gson.JsonNull;
import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.payload.Payload;
import io.xj.core.payload.PayloadObject;
import io.xj.core.util.Text;
import org.reflections.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#167276586] JSON API facilitates complex transactions
 */
public abstract class Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES = ImmutableList.of(); /* "createdAt", "updatedAt" are database-managed, we don't add them here */
  private static final Logger log = LoggerFactory.getLogger(Entity.class);
  private static final double entityPositionDecimalPlaces = 2.0;
  private static final double roundPositionMultiplier = StrictMath.pow(10.0, entityPositionDecimalPlaces);
  protected UUID id;
  protected Instant createdAt;
  protected Instant updatedAt;

  /**
   Require a payload to have one resource object of a specified type

   @param payload      to inspect
   @param resourceType to require a primary object of
   @throws CoreException if there exists NO primary object of the specified type
   */
  public static PayloadObject extractPrimaryObject(Payload payload, String resourceType) throws CoreException {
    Optional<PayloadObject> obj = payload.getDataOne();
    if (obj.isEmpty())
      throw new CoreException("Cannot deserialize single entity create payload without singular data!");
    if (!Objects.equals(resourceType, obj.get().getType()))
      throw new CoreException(String.format("Cannot deserialize single %s-type entity create %s-type payload!", resourceType, obj.get().getType()));
    return obj.get();
  }

  /**
   Is a value not present?

   @param value to test
   @return true if null or empty
   */
  public static boolean isEmpty(Object value) {
    return Objects.isNull(value) || String.valueOf(value).isEmpty();
  }

  /**
   Require a non-null value, or else throw an exception with the specified name

   @param notNull value
   @param name    to describe in exception
   @throws CoreException if null
   */
  protected static <V> void require(V notNull, String name) throws CoreException {
    if (Objects.isNull(notNull) || String.valueOf(notNull).isEmpty())
      throw new CoreException(String.format("%s is required.", name));
  }

  /**
   Require a minimum value, or else throw an exception with the specified name

   @param minimum threshold minimum
   @param value   value
   @param name    to describe in exception
   @throws CoreException if null
   */
  protected static void requireMinimum(Double minimum, Double value, String name) throws CoreException {
    if (value < minimum)
      throw new CoreException(String.format("%s must be at least %f", name, minimum));
  }

  /**
   Require a non-zero value, or else throw an exception with the specified name

   @param value value
   @param name  to describe in exception
   @throws CoreException if null
   */
  protected static <V> void requireNonZero(V value, String name) throws CoreException {
    if (Objects.isNull(value) || String.valueOf(value).isEmpty() || Double.valueOf(String.valueOf(value)).equals(0.0))
      throw new CoreException(String.format("Non-zero %s is required.", name));
  }

  /**
   allow only the specified values, or else throw an exception with the specified name

   @param value value
   @param name  to describe in exception
   @throws CoreException if null
   */
  protected static <V> void require(V value, String name, Collection<V> allowed) throws CoreException {
    require(value, name);
    if (!allowed.contains(value))
      throw new CoreException(String.format("%s '%s' is invalid.", name, value));
  }

  /**
   Require no exception is present, or else throw an exception with the specified name

   @param exception cannot be present
   @param name      to describe in exception
   @throws CoreException if exception is present
   */
  protected static <E extends Exception> void requireNo(E exception, String name) throws CoreException {
    if (Objects.nonNull(exception))
      throw new CoreException(String.format("%s is invalid because %s", name, exception.getMessage()));
  }

  /**
   Format a comma-separated list of entity counts of a collection of entities

   @param entities for format a comma-separated list of the # occurrences of each class
   @return comma-separated list in text
   */
  public static <N extends Entity> String histogramString(Collection<N> entities) {
    Multiset<String> entityHistogram = ConcurrentHashMultiset.create();
    entities.forEach((N entity) -> entityHistogram.add(Text.getSimpleName(entity)));
    List<String> descriptors = Lists.newArrayList();
    Collection<String> names = Ordering.from(String.CASE_INSENSITIVE_ORDER).sortedCopy(entityHistogram.elementSet());
    names.forEach((String name) -> descriptors.add(String.format("%d %s", entityHistogram.count(name), name)));
    return String.join(", ", descriptors);
  }

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
    switch (value.getClass().getSimpleName()) {

      case "UInteger":
      case "Integer":
        return Optional.of(Integer.valueOf(String.valueOf(value)));

      case "Long":
        return Optional.of(Long.valueOf(String.valueOf(value)));

      case "Double":
        return Optional.of(Double.valueOf(String.valueOf(value)));

      case "Float":
        return Optional.of(Float.valueOf(String.valueOf(value)));

      case "Timestamp":
        return Optional.of(Timestamp.valueOf(String.valueOf(value)).toInstant().truncatedTo(ChronoUnit.MICROS).toString());

      case "ULong":
      case "BigInteger":
      case "String":
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

    switch (Text.getSimpleName(setter.getParameterTypes()[0])) {

      case "BigInteger":
        setter.invoke(target, new BigInteger(String.valueOf(value)));
        break;

      case "UUID":
        setter.invoke(target, UUID.fromString(String.valueOf(value)));
        break;

      case "Integer":
        setter.invoke(target, Integer.valueOf(String.valueOf(value)));
        break;

      case "Short":
        setter.invoke(target, Short.valueOf(String.valueOf(value)));
        break;

      case "Long":
        setter.invoke(target, Long.valueOf(String.valueOf(value)));
        break;

      case "Instant":
        if (value.getClass().getSimpleName().equals("Instant"))
          setter.invoke(target, (Instant) value);
        else if (value.getClass().getSimpleName().equals("Timestamp"))
          setter.invoke(target, ((Timestamp) value).toInstant());
        else
          setter.invoke(target, Instant.parse(String.valueOf(value)));
        break;

      case "Timestamp":
        if (value.getClass().getSimpleName().equals("Timestamp"))
          setter.invoke(target, (Timestamp) value);
        else if (value.getClass().getSimpleName().equals("Instant"))
          setter.invoke(target, Timestamp.from((Instant) value));
        else
          setter.invoke(target, Timestamp.from(Instant.parse(String.valueOf(value))));
        break;

      case "Double":
        setter.invoke(target, Double.valueOf(String.valueOf(value)));
        break;

      case "Float":
        setter.invoke(target, Float.valueOf(String.valueOf(value)));
        break;

      default:
        if (Objects.equals("Timestamp", value.getClass().getSimpleName()))
          setter.invoke(target, Timestamp.valueOf(String.valueOf(value)).toInstant().truncatedTo(ChronoUnit.MICROS).toString());
        else
          setter.invoke(target, String.valueOf(value));
        break;

    }
  }

  /**
   Round a value to N decimal places.
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.

   @param value to round
   @return rounded position
   */
  public static Double limitDecimalPrecision(Double value) {
    return Math.floor(value * roundPositionMultiplier) / roundPositionMultiplier;
  }

  /**
   Filter a collection of entities to only one class

   @param entities    to filter source
   @param entityClass only allow these to pass through
   @param <N>         type of entities
   @return collection of only specified class of entities
   */
  public static <N extends Entity> Collection<N> filter(Collection<N> entities, Class entityClass) {
    return entities.stream().filter(e -> e.getClass().equals(entityClass)).collect(Collectors.toList());
  }

  /**
   Set a value using an attribute name

   @param attributeName of attribute for which to find setter method
   @param value         to set
   */
  public void set(String attributeName, Object value) throws CoreException {
    if (Objects.isNull(value)) return;

    String setterName = Text.toSetterName(attributeName);

    for (Method method : getClass().getMethods())
      if (Objects.equals(setterName, method.getName()))
        try {
          set(this, method, value);
          return;

        } catch (InvocationTargetException e) {
          throw new CoreException(String.format("Failed to %s.%s(), reason: %s", Text.getSimpleName(this), setterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new CoreException(String.format("Could not access %s.%s(), reason: %s", Text.getSimpleName(this), setterName, e.getMessage()));

        } catch (NoSuchMethodException e) {
          throw new CoreException(String.format("No such method %s.%s(), reason: %s", Text.getSimpleName(this), setterName, e.getMessage()));
        }

    throw new CoreException(String.format("%s has no attribute '%s'", Text.getSimpleName(this), attributeName));
  }

  /**
   Get a value of a target object via attribute name

   @param attributeName of attribute to get
   @return value
   @throws CoreException on failure to get
   */
  public Optional<Object> get(String attributeName) throws CoreException {
    String getterName = Text.toGetterName(attributeName);

    for (Method method : getClass().getMethods())
      if (Objects.equals(getterName, method.getName()))
        try {
          return get(this, method);

        } catch (InvocationTargetException e) {
          throw new CoreException(String.format("Failed to %s.%s(), reason: %s", Text.getSimpleName(this), getterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new CoreException(String.format("Could not access %s.%s(), reason: %s", Text.getSimpleName(this), getterName, e.getMessage()));
        }

    return Optional.empty();
  }

  /**
   Validate this entity

   @throws CoreException on failure
   */
  public void validate() throws CoreException {
    // no op
  }

  /**
   @return parent ID
   */
  public UUID getParentId() {
    return null;
  }

  /**
   Whether this resource belongs to the specified resource

   @param resource to test whether this belongs to
   @return true if this belongs to the specified resource
   */
  public boolean belongsTo(Entity resource) {
    try {
      Optional<Object> id = get(Text.toIdAttribute(resource));
      return id.isPresent() && id.get().equals(resource.getId());
    } catch (CoreException e) {
      return false;
    }
  }

  /**
   Consume all data of a payload:
   + Set all attributes
   + Adding any available sub-entities
   + Re-index relationships and prune orphaned entities

   @param payload to consume
   @return this Entity (for chaining methods)
   @throws CoreException on failure to consume payload
   */
  public <N extends Entity> N consume(Payload payload) throws CoreException {
    consume(extractPrimaryObject(payload, getResourceType()));
    return (N) this;
  }

  /**
   Set all attributes of entity of a payload object
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

   @param payloadObject of which to get attributes
   @return this Entity (for chaining methods)
   @throws CoreException on failure to set
   */
  public Entity consume(PayloadObject payloadObject) throws CoreException {
    if (!Objects.equals(payloadObject.getType(), getResourceType()))
      throw new CoreException(String.format("Cannot set single %s-type entity create %s-type payload object!", getResourceType(), payloadObject.getType()));

    for (Map.Entry<String, Object> entry : payloadObject.getAttributes().entrySet())
      set(entry.getKey(), entry.getValue());

    // consume all belongs-to relationships
    getResourceBelongsTo().forEach(belongsToClass -> {
      String key = Text.toResourceBelongsTo(belongsToClass);
      Optional<PayloadObject> obj = payloadObject.getRelationshipDataOne(key);
      obj.ifPresent(object -> {
        try {
          set(Text.toIdAttribute(key), object.getId());
        } catch (CoreException e) {
          log.error("Failed to consume belongs-to {} relationship", key, e);
        }
      });
    });

    // consume if if set
    if (Objects.nonNull(payloadObject.getId())) try {
      setId(UUID.fromString(payloadObject.getId()));
    } catch (IllegalArgumentException exception) {
      log.error("Failed to consume id from payload object.getId(): {}", payloadObject.getId());
    }


    return this;
  }

  /**
   Get resource attributes based on getResourceAttributeNames() for this instance
   NOTE: this is implemented in EntityImpl and widely shared; individual classes simply return getResourceAttributeNames()

   @return payload attributes
   */
  public Map<String, Object> getResourceAttributes() {
    Map<String, Object> attributes = Maps.newHashMap();
    List<String> resourceAttributeNames = getResourceAttributeNames();
    ReflectionUtils.getAllMethods(getClass(),
      ReflectionUtils.withModifier(Modifier.PUBLIC),
      ReflectionUtils.withPrefix("get"),
      ReflectionUtils.withParametersCount(0)).forEach(method -> {
      try {
        String attributeName = Text.toAttributeName(method);
        if (resourceAttributeNames.contains(attributeName)) {
          get(this, method).ifPresentOrElse(value -> attributes.put(attributeName, value),
            () -> attributes.put(attributeName, JsonNull.INSTANCE));
        }
      } catch (Exception e) {
        log.warn("Failed to transmogrify value create method {} create entity {}", method, this, e);
      }
    });
    return attributes;
  }

  /**
   Get this resource's belongs-to relations

   @return list of classes this resource belongs to
   */
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.of();
  }

  /**
   Get this resource's has-many relations

   @return list of classes this resource has many of
   */
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.of();
  }

  /**
   get Entity Type- always a plural noun, i.e. Users or Libraries

   @return Entity Type
   */
  public String getResourceType() {
    return Text.toResourceType(this);
  }

  /**
   Get the URI of any entity

   @return Entity URI
   */
  public URI getURI() {
    return Config.getApiURI(String.format("%s/%s", getResourceType(), getResourceId()));
  }

  /**
   Set all values available of a source Entity

   @param from source Entity
   */
  public void setAllResourceAttributes(Entity from) {
    from.getResourceAttributes().forEach((Object name, Object attribute) -> {
      try {
        set(String.valueOf(name), attribute);
      } catch (CoreException e) {
        log.error("Failed to set {}", attribute, e);
      }
    });
  }

  /**
   Shortcut to build payload object with no child entities

   @return resource object
   */
  public PayloadObject toPayloadObject() {
    return toPayloadObject(ImmutableList.of());
  }

  /**
   Build and return a Entity Object of this entity, probably for an API Payload
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
  public <N extends Entity> PayloadObject toPayloadObject(Collection<N> childResources) {
    PayloadObject obj = toPayloadReferenceObject();
    obj.setAttributes(getResourceAttributes());

    // add belongs-to
    getResourceBelongsTo().forEach(key -> {
      try {
        Optional<Object> value = get(Text.toIdAttribute(key));
        value.ifPresent(id -> obj.add(Text.toResourceBelongsTo(key),
          new Payload().setDataReference(Text.toResourceType(key), String.valueOf(id))));
      } catch (CoreException e) {
        log.error("Failed to add belongs-to {} relationship", key, e);
      }
    });

    // add has-many
    Map<String, Collection<N>> hasMany = Maps.newConcurrentMap();
    childResources.forEach(resource -> {
      String type = Text.toResourceType(resource);
      if (!hasMany.containsKey(type)) hasMany.put(type, Lists.newArrayList());
      hasMany.get(type).add(resource);
    });
    getResourceHasMany().forEach(key -> {
      String type = Text.toResourceType(key);
      obj.add(Text.toResourceHasMany(key),
        new Payload().setDataReferences(hasMany.containsKey(type) ?
          hasMany.get(type).stream().filter(r -> r.belongsTo(this)).collect(Collectors.toList()) :
          ImmutableList.of()));
    });

    return obj;
  }

  /**
   Build and return a reference (type and id only) Entity Object of this entity, probably for an API Payload
   <p>
   There's a default implementation in EntityImpl, which uses the resource type and id

   @return resource object
   */
  public PayloadObject toPayloadReferenceObject() {
    return new PayloadObject()
      .setId(getResourceId())
      .setType(getResourceType());
  }

  /**
   Get ofd-at instant

   @return ofd-at instant
   */
  public Instant getCreatedAt() {
    return createdAt;
  }

  /**
   Get entity id

   @return entity id
   */
  public UUID getId() {
    return id;
  }

  /**
   Get a collection of resource attribute names
   Each class that extends Entity must compose its RESOURCE_ATTRIBUTE_NAMES including those of the class it directly extends,
   all the way up to the classes that extend Entity

   @return resource attribute names
   */
  public abstract ImmutableList<String> getResourceAttributeNames();

  /**
   get Entity ID

   @return Entity Id
   */
  public String getResourceId() {
    return id.toString();
  }

  /**
   Get updated-at time

   @return updated-at time
   */
  public Instant getUpdatedAt() {
    return updatedAt;
  }

  /**
   Set createdat time

   @param createdAt time
   @return entity
   */
  public Entity setCreatedAt(String createdAt) {
    try {
      this.createdAt = Instant.parse(createdAt);
    } catch (Exception ignored) {
      // value unchanged
    }
    return this;
  }

  /**
   Set createdat time

   @param createdAt time
   @return entity
   */
  public Entity setCreatedAtInstant(Instant createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  /**
   Set entity id

   @param id to set
   @return entity
   */
  public Entity setId(UUID id) {
    this.id = id;
    return this;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Entity setUpdatedAt(String updatedAt) {
    try {
      this.updatedAt = Instant.parse(updatedAt);
    } catch (Exception ignored) {
      // value unchanged
    }
    return this;
  }

  /**
   Set updated-at time

   @param updatedAt time
   @return entity
   */
  public Entity setUpdatedAtInstant(Instant updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }


}


/*
TODO need any of this of the original Entity class?

  **
   Add an exception to the SuperEntity errors

   @param exception to add
   *
void add(CoreException exception);

  **
   Whether this resource belongs to the specified resource

   @param resource to test whether this belongs to
   @return true if this belongs to the specified resource
   *
  boolean belongsTo(ResourceEntity resource);

  **
   Consume all data of a payload:
   + Set all attributes
   + Adding any available sub-entities
   + Re-index relationships and prune orphaned entities

   @param payload to consume
   @return this ResourceEntity (for chaining methods)
   @throws CoreException on failure to consume payload
   *
  <N extends ResourceEntity> N consume(Payload payload) throws CoreException;

  **
   Set all attributes of entity of a payload object
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

   @param payloadObject of which to get attributes
   @return this ResourceEntity (for chaining methods)
   @throws CoreException on failure to set
   *
  <N extends ResourceEntity> N consume(PayloadObject payloadObject) throws CoreException;

  **
   Get a value of a target object via attribute name

   @param name of attribute to get
   @return value
   @throws InvocationTargetException on failure to invoke method
   @throws IllegalAccessException    on access failure
   *
  Optional<Object> get(String name) throws InvocationTargetException, IllegalAccessException, CoreException;

  **
   Get a value of a target object via getter method

   @param getter to use
   @return value
   @throws InvocationTargetException on failure to invoke method
   @throws IllegalAccessException    on access failure
   *
  Optional<Object> get(Method getter) throws InvocationTargetException, IllegalAccessException;

  **
   Get all entities contained within this entity.
   Empty by default, but some entity types that extend this (e.g. SuperEntity) contain many Sub-entities

   @return collection of entities
   *
  Collection<SubEntity> getAllSubEntities();

  **
   Get errors

   @return errors
   *
  Collection<CoreException> getErrors();

  **
   Get a collection of resource attribute names

   @return resource attribute names
   *
  ImmutableList<String> getResourceAttributeNames();

  **
   Get resource attributes based on getResourceAttributeNames() for this instance
   NOTE: this is implemented in EntityImpl and widely shared; individual classes simply return getResourceAttributeNames()

   @return payload attributes
   *
  Map<String, Object> getResourceAttributes();

  **
   Get this resource's belongs-to relations

   @return list of classes this resource belongs to
   *
  ImmutableList<Class> getResourceBelongsTo();

  **
   Get this resource's has-many relations

   @return list of classes this resource has many of
   *
  ImmutableList<Class> getResourceHasMany();

  **
   get ResourceEntity ID
   <p>
   For SuperEntity, that's a BigInteger
   <p>
   For SubEntity, that's a UUID

   @return ResourceEntity Id
   *
  String getResourceId();

  **
   get ResourceEntity Type- always a plural noun, i.e. Users or Libraries

   @return ResourceEntity Type
   *
  String getResourceType();

  **
   Get the URI of any entity

   @return Entity URI
   *
  URI getURI();

  **
   Set all values available of a source ResourceEntity

   @param of source ResourceEntity
   *
  void setAllResourceAttributes(ResourceEntity of);

  **
   Set a value using a setter method

   @param method setter to use
   @param value  to set
   *
  void set(Method method, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException;

  **
   Set a value using an attribute name

   @param name  of attribute for which to find setter method
   @param value to set
   *
  void set(String name, Object value) throws CoreException;

  **
   Shortcut to build payload object with no child entities

   @return resource object
   *
  PayloadObject toPayloadObject();

  **
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
   *
  <N extends ResourceEntity> PayloadObject toPayloadObject(Collection<N> childResources);

  **
   Build and return a reference (type and id only) ResourceEntity Object of this entity, probably for an API Payload
   <p>
   There's a default implementation in EntityImpl, which uses the resource type and id

   @return resource object
   *
  PayloadObject toPayloadReferenceObject();

  **
   Validate data.

   @return this ResourceEntity (for chaining methods)
   @throws CoreException if invalid.
   *
  <N extends ResourceEntity> N validate() throws CoreException; 
 */
