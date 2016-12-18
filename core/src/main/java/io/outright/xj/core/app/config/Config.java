// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.config;

import io.outright.xj.core.app.exception.ConfigException;

/**
 * ALL APPLICATION CONFIGURATION MUST IMPLEMENT THIS CENTRAL CLASS.
 */
public abstract class Config {

  public static String authGoogleId() throws ConfigException {
    return get("auth.google.id");
  }

  public static String authGoogleSecret() throws ConfigException {
    return get("auth.google.secret");
  }

  public static String appUrl() {
    return getOrDefault("app.url","http://localhost/");
  }

  public static String appHost() {
    return getOrDefault("app.host", "0.0.0.0");
  }

  public static Integer appPort() {
    return getIntOrDefault("app.port", 80);
  }

  public static String appPathUnauthorized() {
    return getOrDefault("app.path.unauthorized","unauthorized");
  }

  public static String appPathSuccess() {
    return getOrDefault("app.path.welcome","welcome");
  }
  public static String logAccessFilename() {
    return getOrDefault("log.access.filename", "/tmp/access.log");
  }

  public static Integer logAccessEntitiesMaxsize() {
    return getIntOrDefault("log.access.entities.maxsize", 0);
  }

  public static Boolean logAccessEntitiesAll() {
    return getBoolOrDefault("log.access.entities.all", false);
  }

  public static String dbMysqlHost() {
    return getOrDefault("db.mysql.host", "localhost");
  }

  public static String dbMysqlPort() {
    return getOrDefault("db.mysql.port", "3306");
  }

  public static String dbMysqlUser() {
    return getOrDefault("db.mysql.user", "root");
  }

  public static String dbMysqlPass() {
    return getOrDefault("db.mysql.pass", "");
  }

  public static String dbMysqlDatabase() {
    return getOrDefault("db.mysql.database", "xj");
  }

  public static String dbRedisHost() {
    return getOrDefault("db.redis.host", "localhost");
  }

  public static Integer dbRedisPort() {
    return getIntOrDefault("db.redis.port", 6379);
  }

  public static String accessTokenDomain() {
    return getOrDefault("access.token.domain", "");
  }

  public static String accessTokenPath() {
    return getOrDefault("access.token.path","/");
  }

  public static int accessTokenMaxAge() {
    return getIntOrDefault("access.token.max.age",60/*seconds*/ * 60/*minutes*/ * 24/*hours*/ * 28/*days*/);
  }

  public static String accessTokenName() {
    return getOrDefault("access.token.name", "access_token");
  }

  /**
   * Set a System Property if no value has yet been set for it.
   *
   * @param k name of system property
   * @param v default value to set for property
   */
  public static void setDefault(String k, String v) {
    if (System.getProperty(k) == null) {
      System.setProperty(k, v);
    }
  }

  // Private

  private static Boolean getBool(String key) throws ConfigException {
    String value = get(key);
    return Boolean.valueOf(value);
  }

  private static Boolean getBoolOrDefault(String key, Boolean defaultValue) {
    try {
      return getBool(key);
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

  private static Integer getInt(String key) throws ConfigException {
    String value = get(key);
    return Integer.valueOf(value);
  }

  private static Integer getIntOrDefault(String key, Integer defaultValue) {
    try {
      return getInt(key);
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

  private static String get(String key) throws ConfigException {
    String value = System.getProperty(key);
    if (value == null) {
      throw new ConfigException("Must set system property: " + key);
    }
    return value;
  }

  private static String getOrDefault(String key, String defaultValue) {
    try {
      return get(key);
    } catch (ConfigException e) {
      return defaultValue;
    }
  }
}
