package io.outright.xj.core.app.access;

import io.outright.xj.core.util.CSV.CSV;

public interface Role {

  /**
   * Define each role.
   */
  String ADMIN="admin";
  String ARTIST ="artist";
  String USER="user";
  //
  String BANNED="banned";

  /**
   * List of all Roles
   */
  String[] ALL={
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
