//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.entity.impl;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.gson.JsonNull;
import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;
import io.xj.core.model.entity.ResourceEntity;
import io.xj.core.model.entity.SubEntity;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.payload.PayloadObject;
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
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#167276586] JSON API facilitates complex transactions
 */
public abstract class ResourceImpl implements ResourceEntity {
  private static final Logger log = LoggerFactory.getLogger(ResourceImpl.class);
  private Collection<CoreException> errors = com.google.common.collect.Lists.newArrayList();

  /**
   Compute an attribute name based on the name of the getter method,
   by removing the first three letters "get", then lower-casing the new first letter.
   <p>
   e.g., input of "getNewsPaper" results in "newsPaper"

   @param method for which to compute name of attribute
   @return attribute name
   */
  private static String computeAttributeName(Method method) {
    return String.format("%s%s",
      method.getName().substring(3, 4).toLowerCase(Locale.ENGLISH),
      method.getName().substring(4));
  }

  /**
   Compute a getter method name based on the name of the attribute,
   capitalize the first letter, then prepend "get"
   <p>
   e.g., input of "newsPaper" results in "getNewsPaper"

   @param attributeName for which to get name of getter method
   @return attribute name
   */
  private static String computeGetterName(String attributeName) {
    return String.format("%s%s%s", "get",
      attributeName.substring(0, 1).toUpperCase(Locale.ENGLISH),
      attributeName.substring(1));
  }

  /**
   Compute a setter method name based on the name of the attribute,
   capitalize the first letter, then prepend "set"
   <p>
   e.g., input of "newsPaper" results in "setNewsPaper"

   @param attributeName for which to get name of setter method
   @return attribute name
   */
  private static String computeSetterName(String attributeName) {
    return String.format("%s%s%s", "set",
      attributeName.substring(0, 1).toUpperCase(Locale.ENGLISH),
      attributeName.substring(1));
  }

  /**
   Require a payload to have one resource object of a specified type

   @param payload      to inspect
   @param resourceType to require a primary object of
   @throws CoreException if there exists NO primary object of the specified type
   */
  public static PayloadObject extractPrimaryObject(Payload payload, String resourceType) throws CoreException {
    Optional<PayloadObject> obj = payload.getDataOne();
    if (obj.isEmpty())
      throw new CoreException("Cannot deserialize single entity from payload without singular data!");
    if (!Objects.equals(resourceType, obj.get().getType()))
      throw new CoreException(String.format("Cannot deserialize single %s-type entity from %s-type payload!", resourceType, obj.get().getType()));
    return obj.get();
  }

  @Override
  public void add(CoreException exception) {
    errors.add(exception);
  }

  @Override
  public boolean belongsTo(ResourceEntity resource) {
    try {
      Optional<Object> id = get(Text.toIdAttribute(resource));
      return id.isPresent() && id.get().equals(resource.getResourceId());
    } catch (CoreException e) {
      return false;
    }
  }

  @Override
  public ResourceEntity consume(Payload payload) throws CoreException {
    consume(extractPrimaryObject(payload, getResourceType()));
    return this;
  }

  @Override
  public ResourceEntity consume(PayloadObject payloadObject) throws CoreException {
    if (!Objects.equals(payloadObject.getType(), getResourceType()))
      throw new CoreException(String.format("Cannot set single %s-type entity from %s-type payload object!", getResourceType(), payloadObject.getType()));

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

    return this;
  }

  @Override
  public Optional<Object> get(String attributeName) throws CoreException {
    String getterName = computeGetterName(attributeName);

    for (Method method : getClass().getMethods())
      if (Objects.equals(getterName, method.getName()))
        try {
          return get(method);

        } catch (InvocationTargetException e) {
          throw new CoreException(String.format("Failed to %s.%s(), reason: %s", Text.getSimpleName(this), getterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new CoreException(String.format("Could not access %s.%s(), reason: %s", Text.getSimpleName(this), getterName, e.getMessage()));
        }

    return Optional.empty();
  }

  @Override
  public Optional<Object> get(Method getter) throws InvocationTargetException, IllegalAccessException {
    Object value = getter.invoke(this);
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
      default:
        return Optional.of(String.valueOf(value));
    }
  }

  @Override
  public Collection<SubEntity> getAllSubEntities() {
    return Lists.newArrayList();
  }

  @Override
  public Collection<CoreException> getErrors() {
    return errors;
  }

  @Override
  public Map<String, Object> getResourceAttributes() {
    Map<String, Object> attributes = Maps.newHashMap();
    ReflectionUtils.getAllMethods(getClass(),
      ReflectionUtils.withModifier(Modifier.PUBLIC),
      ReflectionUtils.withPrefix("get"),
      ReflectionUtils.withParametersCount(0)).forEach(method -> {
      try {
        String attributeName = computeAttributeName(method);
        if (getResourceAttributeNames().contains(attributeName)) {
          get(method).ifPresentOrElse(value -> attributes.put(attributeName, value),
            () -> attributes.put(attributeName, JsonNull.INSTANCE));
        }
      } catch (Exception e) {
        log.warn("Failed to transmogrify value of method {} from entity {}", method, this, e);
      }
    });
    return attributes;
  }

  @Override
  public ImmutableList<Class> getResourceBelongsTo() {
    return ImmutableList.of();
  }

  @Override
  public ImmutableList<Class> getResourceHasMany() {
    return ImmutableList.of();
  }

  @Override
  public String getResourceType() {
    return Text.toResourceType(this);
  }

  @Override
  public URI getURI() {
    return Config.getApiURI(String.format("%s/%s", getResourceType(), getResourceId()));
  }

  /**
   Is a value not present?

   @param value to test
   @return true if null or empty
   */
  public boolean isEmpty(Object value) {
    return Objects.isNull(value) || String.valueOf(value).isEmpty();
  }

  /**
   Require a non-null value, or else throw an exception with the specified name

   @param notNull value
   @param name    to describe in exception
   @throws CoreException if null
   */
  protected <V> void require(V notNull, String name) throws CoreException {
    if (Objects.isNull(notNull) || String.valueOf(notNull).isEmpty())
      throw new CoreException(String.format("%s is required.", name));
  }

  /**
   Require a non-zero value, or else throw an exception with the specified name

   @param value value
   @param name  to describe in exception
   @throws CoreException if null
   */
  protected <V> void requireNonZero(V value, String name) throws CoreException {
    if (Objects.isNull(value) || String.valueOf(value).isEmpty() || Double.valueOf(String.valueOf(value)).equals(0.0))
      throw new CoreException(String.format("Non-zero %s is required.", name));
  }

  /**
   allow only the specified values, or else throw an exception with the specified name

   @param value value
   @param name  to describe in exception
   @throws CoreException if null
   */
  protected <V> void require(V value, String name, Collection<V> allowed) throws CoreException {
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
  protected <E extends Exception> void requireNo(E exception, String name) throws CoreException {
    if (Objects.nonNull(exception))
      throw new CoreException(String.format("%s is invalid because %s", name, exception.getMessage()));
  }

  @Override
  public void set(Method name, Object value) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
    if (0 == name.getParameterTypes().length)
      throw new NoSuchMethodException("Setter accepts no parameters!");

    switch (Text.getSimpleName(name.getParameterTypes()[0])) {

      case "BigInteger":
        name.invoke(this, new BigInteger(String.valueOf(value)));
        break;

      case "UUID":
        name.invoke(this, UUID.fromString(String.valueOf(value)));
        break;

      case "Integer":
        name.invoke(this, Integer.valueOf(String.valueOf(value)));
        break;

      case "Long":
        name.invoke(this, Long.valueOf(String.valueOf(value)));
        break;

      case "Double":
        name.invoke(this, Double.valueOf(String.valueOf(value)));
        break;

      case "Float":
        name.invoke(this, Float.valueOf(String.valueOf(value)));
        break;

      default:
        if (Objects.equals("Timestamp", value.getClass().getSimpleName()))
          name.invoke(this, Timestamp.valueOf(String.valueOf(value)).toInstant().truncatedTo(ChronoUnit.MICROS).toString());
        else
          name.invoke(this, String.valueOf(value));
        break;
    }
  }

  @Override
  public void set(String attributeName, Object value) throws CoreException {
    if (Objects.isNull(value)) return;

    String setterName = computeSetterName(attributeName);

    for (Method method : getClass().getMethods())
      if (Objects.equals(setterName, method.getName()))
        try {
          set(method, value);
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

  @Override
  public PayloadObject toPayloadObject() {
    return toPayloadObject(ImmutableList.of());
  }

  @Override
  public <N extends ResourceEntity> PayloadObject toPayloadObject(Collection<N> childResources) {
    PayloadObject obj = toPayloadReferenceObject();
    obj.setAttributes(getResourceAttributes());

    getResourceBelongsTo().forEach(key -> {
      try {
        Optional<Object> value = get(Text.toIdAttribute(key));
        value.ifPresent(id -> obj.add(Text.toResourceBelongsTo(key),
          new Payload().setDataReference(Text.toResourceType(key), String.valueOf(id))));
      } catch (CoreException e) {
        log.error("Failed to add belongs-to {} relationship", key, e);
      }
    });

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

  @Override
  public PayloadObject toPayloadReferenceObject() {
    return new PayloadObject()
      .setId(getResourceId())
      .setType(getResourceType());
  }
}
