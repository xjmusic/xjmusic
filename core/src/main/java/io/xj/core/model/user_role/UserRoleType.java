// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.user_role;

import io.xj.core.exception.BusinessException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

/**
 This is purely a reference model for UserRole as a concept,
 more specifically the constants and methods pertaining.
 */
public enum UserRoleType {
  // Top Level
  Internal,
  Admin,
  // Mid Level
  Engineer,
  Artist,
  // Basic Level
  User,
  // Banned
  Banned;

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValues() {
    return Text.stringValues(values());
  }

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws BusinessException on failure
   */
  public static UserRoleType validate(String value) throws BusinessException {
    if (Objects.isNull(value))
      throw new BusinessException("Role is required");

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new BusinessException("'" + value + "' is not a valid role (" + CSV.joinEnum(values()) + ").", e);
    }
  }

  /**
   String constants -- required by JAX-RS @RolesAllowed
   */
  public static final String INTERNAL = "Internal";
  public static final String ADMIN = "Admin";
  public static final String ENGINEER = "Engineer";
  public static final String ARTIST = "Artist";
  public static final String USER = "User";
  public static final String BANNED = "Banned";
}
