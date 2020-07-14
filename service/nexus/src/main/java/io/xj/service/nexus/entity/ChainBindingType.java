// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.List;
import java.util.Objects;

/**
 Chain Binding Type
 */
public enum ChainBindingType {
  Library,
  Program,
  Instrument;

  /**
   cast string to chain binding type enum

   @param typeString to cast to enum
   @return binding type enum
   @throws ValueException on failure
   */
  public static ChainBindingType validate(String typeString) throws ValueException {
    if (Objects.isNull(typeString))
      throw new ValueException("Type is required.");

    try {
      return valueOf(typeString);
    } catch (Exception e) {
      throw new ValueException("'" + typeString + "' is not a valid type (" + CSV.joinEnum(ChainBindingType.values()) + ").", e);
    }
  }

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValues() {
    return Text.toStrings(values());
  }

  /**
   Type based on entity

   @param entity to get type based on
   @return type baesd on entity
   */
  public static ChainBindingType valueOf(Entity entity) throws ValueException {
    String entityName = entity.getClass().getSimpleName();
    switch (entityName) {
      case "Library":
        return Library;
      case "Program":
        return Program;
      case "Instrument":
        return Instrument;
      default:
        throw new ValueException("'" + entityName + "' is not a valid entity (" + CSV.joinEnum(ChainBindingType.values()) + ").");
    }
  }
}
