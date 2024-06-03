// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.model.entity;

import io.xj.model.util.CsvUtils;
import io.xj.model.util.StringUtils;
import io.xj.model.util.ValueException;

import java.util.Collection;
import java.util.Objects;

public enum MessageType {
  Debug,
  Info,
  Warning,
  Error;

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws ValueException on failure
   */
  public static MessageType validate(String value) throws ValueException {
    if (Objects.isNull(value))
      throw new ValueException("Type is required");

    try {
      return valueOf(StringUtils.toProperSlug(value));
    } catch (Exception e) {
      throw new ValueException("'" + value + "' is not a valid type (" + CsvUtils.joinEnum(values()) + ").", e);
    }
  }

  /**
   Get the most severe out of a collection of types

   @param messageTypes to get most severe type of
   @return most severe type out of the collection
   */
  public static MessageType mostSevere(Collection<MessageType> messageTypes) {
    MessageType most = MessageType.Debug;
    for (MessageType type : messageTypes)
      if (isMoreSevere(type, most))
        most = type;
    return most;
  }

  /**
   Whether one type is more severe than another type

   @param type        to check for most severity
   @param anotherType standard to check against
   @return true if type is more severe than anotherType
   */
  public static boolean isMoreSevere(MessageType type, MessageType anotherType) {
    return switch (type) {
      default -> false;
      case Info -> Debug == anotherType;
      case Warning -> Debug == anotherType || Info == anotherType;
      case Error -> Debug == anotherType || Info == anotherType || Warning == anotherType;
    };
  }
}
