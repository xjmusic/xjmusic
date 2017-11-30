// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.config;

import io.xj.core.exception.ConfigException;
import io.xj.core.transport.CSV;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 ALL APPLICATION CONFIGURATION MUST IMPLEMENT THIS CENTRAL CLASS.
 */
public interface Config {

  double DEFAULT_TUNING_ROOT_PITCH = 432.0;
  int DAYS_PER_MONTH = 28;
  int DEFAULT_APP_PORT = 80;
  long DEFAULT_CACHE_FILE_ALLOCATE_BYTES = 1_000_000_000; // 1 gigabyte
  int DEFAULT_CHAIN_DESTROY_LINKS_MAX = 24;
  int DEFAULT_CHAIN_PREVIEW_LENGTH_MAX = 300;
  int DEFAULT_LIMIT_LINK_READ_SIZE = 20;
  int DEFAULT_PLATFORM_MESSAGE_READ_PREVIOUS_DAYS = 90;
  int DEFAULT_PLAY_BUFFER_AHEAD_SECONDS = 60;
  int DEFAULT_PLAY_BUFFER_DELAY_SECONDS = 5;
  int DEFAULT_WORK_BUFFER_CRAFT_DELAY_SECONDS = 1;
  int DEFAULT_WORK_BUFFER_DUB_DELAY_SECONDS = 30;
  int DEFAULT_WORK_BUFFER_SECONDS = 300;
  int HOURS_PER_DAY = 24;
  int MINUTES_PER_HOUR = 60;
  int SECONDS_PER_MINUTE = 60;

  static Boolean isTestEnvironment() {
    return getBoolOrDefault("env.test", false);
  }

  static String authGoogleId() throws ConfigException {
    return get("auth.google.id");
  }

  static String authGoogleSecret() throws ConfigException {
    return get("auth.google.secret");
  }

  static String awsAccessKeyId() throws ConfigException {
    return get("aws.accessKeyId");
  }

  static String awsSecretKey() throws ConfigException {
    return get("aws.secretKey");
  }

  static String audioFileBucket() throws ConfigException {
    return get("audio.file.bucket");
  }

  static String awsDefaultRegion() {
    return getOrDefault("aws.defaultRegion", "us-east-1");
  }

  static int awsS3RetryLimit() {
    return getIntOrDefault("aws.s3.retry.limit", 10);
  }

  static int audioFileUploadExpireMinutes() throws ConfigException {
    return getInt("audio.file.upload.expire.minutes");
  }

  static String audioFileUploadACL() throws ConfigException {
    return get("audio.file.upload.acl");
  }

  static String audioBaseUrl() throws ConfigException {
    return get("audio.url.base");
  }

  static String audioUploadUrl() throws ConfigException {
    return get("audio.url.upload");
  }

  static String linkFileBucket() throws ConfigException {
    return get("link.file.bucket");
  }

  static int linkFileUploadExpireMinutes() throws ConfigException {
    return getInt("link.file.upload.expire.minutes");
  }

  static String linkFileUploadACL() throws ConfigException {
    return get("link.file.upload.acl");
  }

  static String linkBaseUrl() throws ConfigException {
    return get("link.url.base");
  }

  static String linkUploadUrl() throws ConfigException {
    return get("link.url.upload");
  }

  static String appBaseUrl() {
    return getOrDefault("app.url.base", "http://localhost/");
  }

  static String apiPath() {
    // this is not a file separator! it's part of the W3C spec.
    return getOrDefault("app.url.api", "api/1/");
  }

  static String appHost() {
    return getOrDefault("app.host", "0.0.0.0");
  }

  static Integer appPort() {
    return getIntOrDefault("app.port", DEFAULT_APP_PORT);
  }

  static String appPathUnauthorized() {
    return getOrDefault("app.path.unauthorized", "unauthorized");
  }

  static String appPathSuccess() {
    return getOrDefault("app.path.welcome", "");
  }

  static Integer platformMessageReadPreviousDays() {
    return getIntOrDefault("platform.message.read.previousDays", DEFAULT_PLATFORM_MESSAGE_READ_PREVIOUS_DAYS);
  }

  static String platformHeartbeatKey() throws ConfigException {
    return get("platform.heartbeat.key");
  }

  static int chainPreviewLengthMax() {
    return getIntOrDefault("chain.preview.length.max", DEFAULT_CHAIN_PREVIEW_LENGTH_MAX);
  }

  static int chainDestroyLinksMax() {
    return getIntOrDefault("chain.destroy.links.max", DEFAULT_CHAIN_DESTROY_LINKS_MAX);
  }

  static String logAccessFilename() {
    return getOrDefault("log.access.filename", getTempFilePathPrefix() + "access.log");
  }

  static Integer logAccessEntitiesMaxsize() {
    return getIntOrDefault("log.access.entities.maxsize", 0);
  }

  static Boolean logAccessEntitiesAll() {
    return getBoolOrDefault("log.access.entities.all", false);
  }

  static Long cacheFileAllocateBytes() {
    return getLongOrDefault("cache.file.allocate.bytes", DEFAULT_CACHE_FILE_ALLOCATE_BYTES);
  }

  static String cacheFilePathPrefix() {
    return getOrDefault("cache.file.path.prefix", getTempFilePathPrefix() + "cache" + File.separator);
  }

  static String cacheFilePathSuffix() {
    return getOrDefault("cache.file.path.suffix", ".data");
  }

  static String dbMysqlHost() {
    return getOrDefault("db.mysql.host", "localhost");
  }

  static String dbMysqlPort() {
    return getOrDefault("db.mysql.port", "3300");
  }

  static String dbMysqlUser() {
    return getOrDefault("db.mysql.user", "root");
  }

  static String dbMysqlPass() {
    return getOrDefault("db.mysql.pass", "");
  }

  static String dbMysqlDatabase() {
    return getOrDefault("db.mysql.database", "xj");
  }

  static String dbRedisHost() {
    return getOrDefault("db.redis.host", "localhost");
  }

  static Integer dbRedisPort() {
    return getIntOrDefault("db.redis.port", 6300);
  }

  static String dbRedisQueueNamespace() {
    return getOrDefault("db.redis.queue.namespace", "xj");
  }

  static String accessTokenDomain() {
    return getOrDefault("access.token.domain", "");
  }

  static String accessTokenPath() {
    // this is not a file separator! it's part of the W3C spec.
    return getOrDefault("access.token.path", "/"); // do not replace with path separator! see note above
  }

  static int accessTokenMaxAge() {
    return getIntOrDefault("access.token.max.age", SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY * DAYS_PER_MONTH);
  }

  static String accessTokenName() {
    return getOrDefault("access.token.name", "access_token");
  }

  static String tuningRootNote() {
    return getOrDefault("tuning.root.note", "A4");
  }

  static Double tuningRootPitch() {
    return getDoubleOrDefault("tuning.root.pitch", DEFAULT_TUNING_ROOT_PITCH);
  }

  static int limitLinkReadSize() {
    return getIntOrDefault("limit.link.read.size", DEFAULT_LIMIT_LINK_READ_SIZE);
  }

  static int playBufferDelaySeconds() {
    return getIntOrDefault("play.buffer.delay.seconds", DEFAULT_PLAY_BUFFER_DELAY_SECONDS);
  }

  static int playBufferAheadSeconds() {
    return getIntOrDefault("play.buffer.ahead.seconds", DEFAULT_PLAY_BUFFER_AHEAD_SECONDS);
  }

  static int workConcurrency() {
    return getIntOrDefault("work.concurrency", 10);
  }

  static int workBufferSeconds() {
    return getIntOrDefault("work.buffer.seconds", DEFAULT_WORK_BUFFER_SECONDS);
  }

  static int workBufferCraftDelaySeconds() {
    return getIntOrDefault("work.buffer.craft.delay.seconds", DEFAULT_WORK_BUFFER_CRAFT_DELAY_SECONDS);
  }

  static int workBufferDubDelaySeconds() {
    return getIntOrDefault("work.buffer.dub.delay.seconds", DEFAULT_WORK_BUFFER_DUB_DELAY_SECONDS);
  }

  static Integer workChainRecurSeconds() {
    return getIntOrDefault("work.chain.recur.seconds", 2);
  }

  static Integer workChainDeleteRecurSeconds() {
    return getIntOrDefault("work.chain.delete.recur.seconds", 10);
  }

  static Integer workChainDelaySeconds() {
    return getIntOrDefault("work.chain.delay.seconds", 1);
  }

  static int workLinkCraftRetryLimit() {
    return getIntOrDefault("work.link.craft.retry.limit", 6);
  }

  static int workLinkCraftRetrySleepSeconds() {
    return getIntOrDefault("work.link.craft.retry.sleep.seconds", 1);
  }

  static int workLinkDubRetryLimit() {
    return getIntOrDefault("work.link.dub.retry.limit", 6);
  }

  static int workLinkDubRetrySleepSeconds() {
    return getIntOrDefault("work.link.dub.retry.sleep.seconds", 1);
  }

  static String workTempFilePathPrefix() {
    return getOrDefault("work.temp.file.path.prefix", getTempFilePathPrefix());
  }

  static String workQueueName() {
    return getOrDefault("work.queue.name", "xj_work");
  }

  /**
   Set a System Property if no value has yet been set for it.

   @param k name of system property
   @param v default value to set for property
   */
  static void setDefault(String k, String v) {
    if (System.getProperty(k) == null) {
      System.setProperty(k, v);
    }
  }

  static Boolean getBool(String key) throws ConfigException {
    String value = get(key);
    return Boolean.valueOf(value);
  }

  static Boolean getBoolOrDefault(String key, Boolean defaultValue) {
    try {
      return getBool(key);
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

  static Integer getInt(String key) throws ConfigException {
    String value = get(key);
    return Integer.valueOf(value);
  }

  static Long getLong(String key) throws ConfigException {
    String value = get(key);
    return Long.valueOf(value);
  }

  static Double getDouble(String key) throws ConfigException {
    String value = get(key);
    return Double.valueOf(value);
  }

  static Integer getIntOrDefault(String key, Integer defaultValue) {
    try {
      return getInt(key);
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

  static Long getLongOrDefault(String key, Long defaultValue) {
    try {
      return getLong(key);
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

  static Double getDoubleOrDefault(String key, Double defaultValue) {
    try {
      return getDouble(key);
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

  static String get(String key) throws ConfigException {
    String value = System.getProperty(key);
    if (value == null) {
      throw new ConfigException("Must set system property: " + key);
    }
    return value;
  }

  static List<String> getListOrDefault(String key, List<String> defaultValue) {
    try {
      return CSV.split(get(key));
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

  static String getOrDefault(String key, String defaultValue) {
    try {
      return get(key);
    } catch (ConfigException e) {
      return defaultValue;
    }
  }

  static String getTempFilePathPrefix() {
    String path = File.separator + "tmp" + File.separator;
    try {
      String absolutePath = File.createTempFile("temp-file-name", ".tmp").getAbsolutePath();
      path = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)) + File.separator;

    } catch (IOException e) {
      // noop
    }
    return path;
  }

  static String lineSeparator() {
    return System.getProperty("line.separator");
  }

}
