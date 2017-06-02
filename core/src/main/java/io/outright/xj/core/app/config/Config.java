// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.config;

import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.transport.CSV;

import java.util.List;

/**
 ALL APPLICATION CONFIGURATION MUST IMPLEMENT THIS CENTRAL CLASS.
 */
public abstract class Config {

  public static Boolean isTestEnvironment() {
    return getBoolOrDefault("env.test", false);
  }

  public static String authGoogleId() throws ConfigException {
    return get("auth.google.id");
  }

  public static String authGoogleSecret() throws ConfigException {
    return get("auth.google.secret");
  }

  public static String awsAccessKeyId() throws ConfigException {
    return get("aws.accessKeyId");
  }

  public static String awsSecretKey() throws ConfigException {
    return get("aws.secretKey");
  }

  public static String audioFileBucket() throws ConfigException {
    return get("audio.file.bucket");
  }

  public static String awsDefaultRegion() {
    return getOrDefault("aws.defaultRegion", "us-east-1");
  }

  public static int audioFileUploadExpireMinutes() throws ConfigException {
    return getInt("audio.file.upload.expire.minutes");
  }

  public static String audioFileUploadACL() throws ConfigException {
    return get("audio.file.upload.acl");
  }

  public static String audioBaseUrl() throws ConfigException {
    return get("audio.url.base");
  }

  public static String audioUploadUrl() throws ConfigException {
    return get("audio.url.upload");
  }

  public static String linkFileBucket() throws ConfigException {
    return get("link.file.bucket");
  }

  public static int linkFileUploadExpireMinutes() throws ConfigException {
    return getInt("link.file.upload.expire.minutes");
  }

  public static String linkFileUploadACL() throws ConfigException {
    return get("link.file.upload.acl");
  }

  public static String linkBaseUrl() throws ConfigException {
    return get("link.url.base");
  }

  public static String linkUploadUrl() throws ConfigException {
    return get("link.url.upload");
  }

  public static String appBaseUrl() {
    return getOrDefault("app.url.base", "http://localhost/");
  }

  public static String apiPath() {
    return getOrDefault("app.url.api", "api/1/");
  }

  public static String appHost() {
    return getOrDefault("app.host", "0.0.0.0");
  }

  public static Integer appPort() {
    return getIntOrDefault("app.port", 80);
  }

  public static String appPathUnauthorized() {
    return getOrDefault("app.path.unauthorized", "unauthorized");
  }

  public static String appPathSuccess() {
    return getOrDefault("app.path.welcome", "");
  }

  public static int chainPreviewLengthMax() {
    return getIntOrDefault("chain.preview.size.max", 300);
  }

  public static int chainDestroyLinksMax() {
    return getIntOrDefault("chain.destroy.links.max", 24);
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
    return getOrDefault("access.token.path", "/");
  }

  public static int accessTokenMaxAge() {
    return getIntOrDefault("access.token.max.age", 60/*seconds*/ * 60/*minutes*/ * 24/*hours*/ * 28/*days*/);
  }

  public static String accessTokenName() {
    return getOrDefault("access.token.name", "access_token");
  }

  public static String tuningRootNote() {
    return getOrDefault("tuning.root.note", "A4");
  }

  public static Double tuningRootPitch() {
    return getDoubleOrDefault("tuning.root.pitch", 432.0);
  }

  public static int limitLinkReadSize() {
    return getIntOrDefault("limit.link.read.size", 50);
  }

  public static int workConcurrency() {
    return getIntOrDefault("work.concurrency", 10);
  }

  public static int workBatchSize() {
    return getIntOrDefault("work.batch.size", 10);
  }

  public static long workBatchSleepSeconds() {
    return getIntOrDefault("work.batch.sleep.seconds", 1);
  }

  public static int workAheadSeconds() {
    return getIntOrDefault("work.buffer.seconds", 300);
  }

  public static String workTempFilePath() {
    return getOrDefault("work.temp.file.path", "/tmp");
  }

  /**
   Set a System Property if no value has yet been set for it.

   @param k name of system property
   @param v default value to set for property
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

  private static Double getDouble(String key) throws ConfigException {
    String value = get(key);
    return Double.valueOf(value);
  }

  private static Integer getIntOrDefault(String key, Integer defaultValue) {
    try {
      return getInt(key);
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

  private static Double getDoubleOrDefault(String key, Double defaultValue) {
    try {
      return getDouble(key);
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

  private static List<String> getListOrDefault(String key, List<String> defaultValue) {
    try {
      return CSV.split(get(key));
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

  private static String getOrDefault(String key, String defaultValue) {
    try {
      return get(key);
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

}
