// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.role;

import io.outright.xj.core.transport.CSV;

public interface Role {

  /**
   * Define each role.
   */
  String INTERNAL = "internal";
  String ADMIN = "admin";
  //
  String ARTIST = "artist";
  String USER = "user";
  //
  String BANNED = "banned";

  /**
   * List of all Roles
   */
  String[] ALL = {
    ADMIN,
    ARTIST,
    USER,
    //
    BANNED
  };

  /**
   * CSV generated from list of all roles
   */
  String ALL_CSV = CSV.join(ALL);

  /**
   * For use in maps.
   */
  String KEY_ONE = "role";
  String KEY_MANY = "roles";

  /**
   * Checks whether a string is a valid role.
   *
   * @param role to check.
   * @return true if valid; otherwise false;
   */
  static boolean isValid(String role) {
    for (String validRole : ALL) {
      if (role.equals(validRole)) {
        return true;
      }
    }
    return false;
  }
}
