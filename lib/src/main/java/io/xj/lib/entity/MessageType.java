// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum MessageType {
  Debug,
  Info,
  Warning,
  Error;

  private static final String TYPE_KEY = "type";

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
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new ValueException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").", e);
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
   Get the most severe type out of a collection of messages

   @param messages to get most severe type of
   @return most severe type out of the collection
   */
  public static MessageType mostSevereType(Collection<?> messages) {
    return mostSevere(messages.stream().flatMap(e -> {
      try {
        return Stream.of(MessageType.valueOf(String.valueOf(Entities.get(e, TYPE_KEY).orElseThrow())));
      } catch (Exception ignore) {
        return Stream.empty();
      }
    }).collect(Collectors.toList()));
  }

  /**
   Whether one type is more severe than another type

   @param type        to check for most severity
   @param anotherType standard to check against
   @return true if type is more severe than anotherType
   */
  public static boolean isMoreSevere(MessageType type, MessageType anotherType) {
    switch (type) {
      default:
      case Debug:
        return false;
      case Info:
        return Debug == anotherType;
      case Warning:
        return Debug == anotherType || Info == anotherType;
      case Error:
        return Debug == anotherType || Info == anotherType || Warning == anotherType;
    }
  }
}
