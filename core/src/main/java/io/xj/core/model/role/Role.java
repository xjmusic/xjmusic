// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.role;

import io.xj.core.transport.CSV;

/**
 This is purely a reference model for Role as a concept,
 more specifically the constants and methods pertaining.
 */
public interface Role {

  /**
   Define each role.
   */
  String INTERNAL = "internal";
  String ADMIN = "admin";
  //
  String ENGINEER = "engineer";
  String ARTIST = "artist";
  String USER = "user";
  //
  String BANNED = "banned";

  /**
   List of all Roles
   */
  String[] TYPES = {
    ADMIN,
    ARTIST,
    ENGINEER,
    USER,
    //
    BANNED
  };

  /**
   CSV generated from list of all roles
   */
  String TYPES_CSV = CSV.join(TYPES);

  /**
   For use in maps.
   */
  String KEY_ONE = "role";
  String KEY_MANY = "roles";

  /**
   Checks whether a string is a valid role.

   @param role to check.
   @return true if valid; otherwise false;
   */
  static boolean isValid(String role) {
    for (String validRole : TYPES) {
      if (role.equals(validRole)) {
        return true;
      }
    }
    return false;
  }
}
