// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.config;

import io.outright.xj.core.app.access.Role;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * ALL APPLICATION CONSTANTS MUST IMPLEMENT THIS CENTRAL CLASS.
 */
public abstract class Exposure {

  /**
   * This config property is how database column names become JSON key names.
   *
   * This is also a whitelist for publicly visible properties.
   *
   * If a SQL column name is not found in this map,
   * it will never be exposed as JSON.
   */
  public static final Map<String, String> keyDatabaseColumnsInJSON = new ImmutableMap.Builder<String, String>()
    .put("avatar_url", "avatarUrl")
    .put("email", "email")
    .put("id", "id")
    .put("name", "name")
    .put(Role.KEY_ONE, Role.KEY_ONE)
    .put(Role.KEY_MANY, Role.KEY_MANY)
    .build();

  /**
   * Error message as JSON output payload.
   */
  public static final String ERRORS_KEY = "errors";
  public static final String ERROR_DETAIL_KEY = "detail";

}
