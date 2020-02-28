// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import io.xj.lib.core.entity.Entity;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.util.CSV;
import io.xj.lib.core.util.Text;

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
   @throws CoreException on failure
   */
  public static ChainBindingType validate(String typeString) throws CoreException {
    if (Objects.isNull(typeString))
      throw new CoreException("Type is required.");

    try {
      return valueOf(typeString);
    } catch (Exception e) {
      throw new CoreException("'" + typeString + "' is not a valid type (" + CSV.joinEnum(ChainBindingType.values()) + ").", e);
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
  public static ChainBindingType valueOf(Entity entity) throws CoreException {
    String entityName = entity.getClass().getSimpleName();
    switch (entityName) {
      case "Library":
        return Library;
      case "Program":
        return Program;
      case "Instrument":
        return Instrument;
      default:
        throw new CoreException("'" + entityName + "' is not a valid entity (" + CSV.joinEnum(ChainBindingType.values()) + ").");
    }
  }
}
