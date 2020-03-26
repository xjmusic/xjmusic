// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 Entity for Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public enum PayloadEntity {
  ;

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

    switch (PayloadKey.getSimpleName(setter.getParameterTypes()[0]).toLowerCase()) {

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
        if (Objects.equals("Timestamp", value.getClass().getSimpleName()))
          setter.invoke(target, Timestamp.valueOf(String.valueOf(value)).toInstant().truncatedTo(ChronoUnit.MICROS).toString());
        else if (Objects.equals("String", setter.getParameterTypes()[0].getSimpleName()))
          setter.invoke(target, String.valueOf(value));
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
  public static <N> void set(N target, String attributeName, Object value) throws RestApiException {
    if (Objects.isNull(value)) return;

    String setterName = PayloadKey.toSetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(setterName, method.getName()))
        try {
          set(target, method, value);
          return;

        } catch (InvocationTargetException e) {
          throw new RestApiException(String.format("Failed to %s.%s(), reason: %s", PayloadKey.getSimpleName(target), setterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new RestApiException(String.format("Could not access %s.%s(), reason: %s", PayloadKey.getSimpleName(target), setterName, e.getMessage()));

        } catch (NoSuchMethodException e) {
          throw new RestApiException(String.format("No such method %s.%s(), reason: %s", PayloadKey.getSimpleName(target), setterName, e.getMessage()));
        }

    throw new RestApiException(String.format("%s has no attribute '%s'", PayloadKey.getSimpleName(target), attributeName));
  }

  /**
   Get a value of a target object via attribute name

   @param target        to get attribute from
   @param attributeName of attribute to get
   @return value gotten from target attribute
   @throws RestApiException on failure to get
   */
  public static <N> Optional<Object> get(N target, String attributeName) throws RestApiException {
    String getterName = PayloadKey.toGetterName(attributeName);

    for (Method method : target.getClass().getMethods())
      if (Objects.equals(getterName, method.getName()))
        try {
          return get(target, method);

        } catch (InvocationTargetException e) {
          throw new RestApiException(String.format("Failed to %s.%s(), reason: %s", PayloadKey.getSimpleName(target), getterName, e.getTargetException().getMessage()));

        } catch (IllegalAccessException e) {
          throw new RestApiException(String.format("Could not access %s.%s(), reason: %s", PayloadKey.getSimpleName(target), getterName, e.getMessage()));
        }

    return Optional.empty();
  }

  /**
   get Entity ID

   @return Entity Id
   */
  public static <N> String getResourceId(N target) throws RestApiException {
    Optional<Object> id = get(target, "id");
    if (id.isEmpty()) throw new RestApiException("Has no id");
    return (id.get().toString());
  }

  /**
   get Entity type

   @return Entity Type
   */
  public static <N> String getResourceType(N target) {
    return PayloadKey.toResourceType(target);
  }

}
