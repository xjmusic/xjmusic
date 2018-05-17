// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.config;

import io.xj.core.exception.ConfigException;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

/**
 ALL APPLICATION CONFIGURATION MUST IMPLEMENT THIS CENTRAL CLASS.
 */
public interface Config {
  Double DEFAULT_TUNING_ROOT_PITCH = 432.0;
  Integer DAYS_PER_MONTH = 28;
  Integer DEFAULT_APP_PORT = 80;
  Integer DEFAULT_AWS_S3_RETRY_LIMIT = 10;
  Integer DEFAULT_CHAIN_PREVIEW_LENGTH_MAX = 300;
  Integer DEFAULT_CHORD_MARKOV_ORDER = 3;
  Integer DEFAULT_DIGEST_CACHE_EXPIRE_MINUTES = 3;
  Integer DEFAULT_DIGEST_CACHE_REFRESH_MINUTES = 1;
  Integer DEFAULT_ENTITY_CACHE_SECONDS = 60;
  Integer DEFAULT_GENERATION_SEQUENCE_PATTERNS_MULTIPLIER = 3;
  Integer DEFAULT_GENERATION_SPLICE_SAFETY_MARGIN = 1;
  Integer DEFAULT_INGEST_ACHE_SECONDS = 60;
  Integer DEFAULT_INGEST_CHORD_SEQUENCE_LENGTH_MAX = 5;
  Integer DEFAULT_INGEST_CHORD_SEQUENCE_PRESERVE_LENGTH_MIN = 2;
  Integer DEFAULT_INGEST_CHORD_SEQUENCE_REDUNDANCY_THRESHOLD = 1;
  Integer DEFAULT_LIMIT_SEGMENT_READ_SIZE = 20;
  Integer DEFAULT_LOG_ACCESS_ENTITIES_MAX_SIZE = 0;
  Integer DEFAULT_PLATFORM_MESSAGE_READ_PREVIOUS_DAYS = 90;
  Integer DEFAULT_PLAY_BUFFER_AHEAD_SECONDS = 60;
  Integer DEFAULT_PLAY_BUFFER_DELAY_SECONDS = 5;
  Integer DEFAULT_REDIS_PORT = 6300;
  Integer DEFAULT_WORK_BUFFER_CRAFT_DELAY_SECONDS = 1;
  Integer DEFAULT_WORK_BUFFER_SECONDS = 300;
  Integer DEFAULT_WORK_CHAIN_DELAY_SECONDS = 1;
  Integer DEFAULT_WORK_CHAIN_ERASE_RECUR_SECONDS = 10;
  Integer DEFAULT_WORK_CHAIN_RECUR_SECONDS = 2;
  Integer DEFAULT_WORK_CONCURRENCY = 10;
  Integer HOURS_PER_DAY = 24;
  Integer MINUTES_PER_HOUR = 60;
  Integer SECONDS_PER_MINUTE = 60;
  Long DEFAULT_CACHE_FILE_ALLOCATE_BYTES = 1_000_000_000L; // 1 gigabyte
  Long DEFAULT_DIGEST_CACHE_SIZE = 1_000_000L;
  String DEFAULT_ACCESS_TOKEN_NAME = "access_token";
  String DEFAULT_API_PATH = "api/1/";
  String DEFAULT_APP_BASE_URL = "http://localhost/";
  String DEFAULT_APP_HOST = "0.0.0.0";
  String DEFAULT_APP_HOSTNAME = "localhost";
  String DEFAULT_APP_NAME = "app";
  String DEFAULT_APP_PATH_UNAUTHORIZED = "unauthorized";
  String DEFAULT_APP_PATH_WELCOME = "";
  String DEFAULT_AWS_DEFAULT_REGION = "us-east-1";
  String DEFAULT_LOG_ACCESS_FILE_NAME_SUFFIX = "access.log";
  String DEFAULT_PLATFORM_RELEASE = "develop";
  String DEFAULT_TEMP_FILE_PATH_PREFIX_CHUNK = "tmp";
  String DEFAULT_TEMP_FILE_PATH_PREFIX_CREATE_NAME = "temp-file-name";
  String DEFAULT_TEMP_FILE_PATH_PREFIX_CREATE_SUFFIX = ".tmp";
  String DEFAULT_TUNING_ROOT_NOTE = "A4";

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
    return getOrDefault("aws.defaultRegion", DEFAULT_AWS_DEFAULT_REGION);
  }

  static int awsS3RetryLimit() {
    return getIntOrDefault("aws.s3.retry.limit", DEFAULT_AWS_S3_RETRY_LIMIT);
  }

  /**
   @return number of seconds to cache an entity before re-reading it from the DAOs
   */
  static int entityCacheSeconds() {
    return getIntOrDefault("entity.cache.seconds", DEFAULT_ENTITY_CACHE_SECONDS);
  }

  /**
   @return Max length of chord progression for ingest
   */
  static int ingestChordProgressionLengthMax() {
    return getIntOrDefault("ingest.chord.sequence.length.max", DEFAULT_INGEST_CHORD_SEQUENCE_LENGTH_MAX);
  }

  /**
   @return threshold X, where during pruning of redundant subsets of chord progressions, a redundant subset with length greater than or equal to X will have its chord progressions preserved, meaning that they are moved into the ingest that is deprecating their original sequence descriptor.
   */
  static int ingestChordProgressionPreserveLengthMin() {
    return getIntOrDefault("ingest.chord.sequence.preserve.length.min", DEFAULT_INGEST_CHORD_SEQUENCE_PRESERVE_LENGTH_MIN);
  }

  /**
   @return threshold X, where during pruning of redundant subsets of chord progressions, in order to be considered redundant, a subset must have length of at least X less than the length of the superset.
   */
  static int ingestChordProgressionRedundancyThreshold() {
    return getIntOrDefault("ingest.chord.sequence.redundancy.threshold", DEFAULT_INGEST_CHORD_SEQUENCE_REDUNDANCY_THRESHOLD);
  }

  /**
   @return number of seconds to cache an ingest before re-reading it from the DAOs
   */
  static int ingestCacheSeconds() {
    return getIntOrDefault("ingest.cache.seconds", DEFAULT_INGEST_ACHE_SECONDS);
  }

  /**
   @return max size (in memory) for the ingest cache of one type of digest
   */
  static long digestCacheSize() {
    return getLongOrDefault("digest.cache.size", DEFAULT_DIGEST_CACHE_SIZE);
  }

  /**
   @return ingest digest cache expire # minutes
   */
  static int digestCacheExpireMinutes() {
    return getIntOrDefault("digest.cache.expire.minutes", DEFAULT_DIGEST_CACHE_EXPIRE_MINUTES);
  }

  /**
   @return ingest digest cache refresh # minutes
   */
  static int digestCacheRefreshMinutes() {
    return getIntOrDefault("digest.cache.refresh.minutes", DEFAULT_DIGEST_CACHE_REFRESH_MINUTES);
  }

  /**
   @return Audio File upload expire # minutes (for Amazon S3)
   */
  static int audioFileUploadExpireMinutes() throws ConfigException {
    return getInt("audio.file.upload.expire.minutes");
  }

  /**
   @return Audio file upload ACL (for Amazon S3)
   */
  static String audioFileUploadACL() throws ConfigException {
    return get("audio.file.upload.acl");
  }

  /**
   @return Audio base URL (for Amazon S3)
   */
  static String audioBaseUrl() throws ConfigException {
    return get("audio.url.base");
  }

  /**
   @return Audio upload URL (for Amazon S3)
   */
  static String audioUploadUrl() throws ConfigException {
    return get("audio.url.upload");
  }

  /**
   @return Segment File Bucket (for Amazon S3)
   */
  static String segmentFileBucket() throws ConfigException {
    return get("segment.file.bucket");
  }

  /**
   @return Segments base URL (for Amazon S3)
   */
  static String segmentBaseUrl() throws ConfigException {
    return get("segment.url.base");
  }

  /**
   @return app base URL
   */
  static String appBaseUrl() {
    return getOrDefault("app.url.base", DEFAULT_APP_BASE_URL);
  }

  /**
   @return API path
   */
  static String apiPath() {
    // this is not a file separator! it's part of the W3C spec.
    return getOrDefault("app.url.api", DEFAULT_API_PATH);
  }

  /**
   @return app name
   */
  static String appName() {
    return getOrDefault("app.name", DEFAULT_APP_NAME);
  }

  /**
   @return app hostname
   */
  static String appHostname() {
    return getOrDefault("app.hostname", DEFAULT_APP_HOSTNAME);
  }

  /**
   @return app host
   */
  static String appHost() {
    return getOrDefault("app.host", DEFAULT_APP_HOST);
  }

  /**
   @return app port
   */
  static Integer appPort() {
    return getIntOrDefault("app.port", DEFAULT_APP_PORT);
  }

  /**
   @return app path unauthorized
   */
  static String appPathUnauthorized() {
    return getOrDefault("app.path.unauthorized", DEFAULT_APP_PATH_UNAUTHORIZED);
  }

  /**
   @return app login welcome path
   */
  static String appPathSuccess() {
    return getOrDefault("app.path.welcome", DEFAULT_APP_PATH_WELCOME);
  }

  /**
   @return platform message read # previous days
   */
  static Integer platformMessageReadPreviousDays() {
    return getIntOrDefault("platform.message.read.previousDays", DEFAULT_PLATFORM_MESSAGE_READ_PREVIOUS_DAYS);
  }

  /**
   @return platform heartbeat key
   */
  static String platformHeartbeatKey() throws ConfigException {
    return get("platform.heartbeat.key");
  }

  /**
   @return platform release version, overwritten in production so that platforms know what version has been deployed
   */
  static String platformRelease() {
    return getOrDefault("platform.release", DEFAULT_PLATFORM_RELEASE);
  }

  /**
   @return Max chain preview length
   */
  static int chainPreviewLengthMax() {
    return getIntOrDefault("chain.preview.length.max", DEFAULT_CHAIN_PREVIEW_LENGTH_MAX);
  }

  /**
   @return Access log filename
   */
  static String logAccessFilename() {
    return getOrDefault("log.access.filename", getTempFilePathPrefix() + DEFAULT_LOG_ACCESS_FILE_NAME_SUFFIX);
  }

  /**
   @return Access log file max size
   */
  static Integer logAccessEntitiesMaxsize() {
    return getIntOrDefault("log.access.entities.maxsize", DEFAULT_LOG_ACCESS_ENTITIES_MAX_SIZE);
  }

  /**
   @return Should access log all entities?
   */
  static Boolean isLogAccessEntitiesAll() {
    return isOrDefault("log.access.entities.all", false);
  }

  /**
   @return Cache file allocate bytes
   */
  static Long cacheFileAllocateBytes() {
    return getLongOrDefault("cache.file.allocate.bytes", DEFAULT_CACHE_FILE_ALLOCATE_BYTES);
  }

  /**
   @return Cache file path prefix
   */
  static String cacheFilePathPrefix() {
    return getOrDefault("cache.file.path.prefix", getTempFilePathPrefix() + "cache" + File.separator);
  }

  /**
   @return Cache file suffix
   */
  static String cacheFilePathSuffix() {
    return getOrDefault("cache.file.path.suffix", ".data");
  }

  /**
   @return Database MySQL database host
   */
  static String dbMysqlHost() {
    return getOrDefault("db.mysql.host", "localhost");
  }

  /**
   @return Database MySQL database port
   */
  static String dbMysqlPort() {
    return getOrDefault("db.mysql.port", "3300");
  }

  /**
   @return Database MySQL database use
   */
  static String dbMysqlUser() {
    return getOrDefault("db.mysql.user", "root");
  }

  /**
   @return Database MySQL database password
   */
  static String dbMysqlPass() {
    return getOrDefault("db.mysql.pass", "");
  }

  /**
   @return Database MySQL database name
   */
  static String dbMysqlDatabase() {
    return getOrDefault("db.mysql.database", "xj");
  }

  /**
   @return Database Redis Host
   */
  static String dbRedisHost() {
    return getOrDefault("db.redis.host", "localhost");
  }

  /**
   @return Database Redis Port
   */
  static Integer dbRedisPort() {
    return getIntOrDefault("db.redis.port", DEFAULT_REDIS_PORT);
  }

  /**
   @return Database Redis Queue Namespace
   */
  static String dbRedisQueueNamespace() {
    return getOrDefault("db.redis.queue.namespace", "xj");
  }

  /**
   @return Access token name (for Cookies)
   */
  static String accessTokenDomain() {
    return getOrDefault("access.token.domain", "");
  }

  /**
   @return Access token path (for Cookies)
   */
  static String accessTokenPath() {
    // this is not a file separator! it's part of the W3C spec.
    return getOrDefault("access.token.path", "/"); // do not replace with path separator! see note above
  }

  /**
   @return Access token max age in seconds (for Cookies)
   */
  static int accessTokenMaxAge() {
    return getIntOrDefault("access.token.max.age", SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY * DAYS_PER_MONTH);
  }

  /**
   @return Access token name that will be used for client API access (for Cookies)
   */
  static String accessTokenName() {
    return getOrDefault("access.token.name", DEFAULT_ACCESS_TOKEN_NAME);
  }

  /**
   The root Tuning Note of th eentire XJ platform, as a Note, e.g. "A4"

   @return Note of root tuning
   */
  static String tuningRootNote() {
    return getOrDefault("tuning.root.note", DEFAULT_TUNING_ROOT_NOTE);
  }

  /**
   The root Tuning of the entire XJ platform, in Hz.

   @return Tuning Root Pitch, in Hz.
   */
  static Double tuningRootPitch() {
    return getDoubleOrDefault("tuning.root.pitch", DEFAULT_TUNING_ROOT_PITCH);
  }

  /**
   @return Limit Segment Read Size
   */
  static int limitSegmentReadSize() {
    return getIntOrDefault("limit.segment.read.size", DEFAULT_LIMIT_SEGMENT_READ_SIZE);
  }

  /**
   @return Play Buffer Delay Seconds
   */
  static int playBufferDelaySeconds() {
    return getIntOrDefault("play.buffer.delay.seconds", DEFAULT_PLAY_BUFFER_DELAY_SECONDS);
  }

  /**
   @return Play Buffer Ahead Seconds
   */
  static int playBufferAheadSeconds() {
    return getIntOrDefault("play.buffer.ahead.seconds", DEFAULT_PLAY_BUFFER_AHEAD_SECONDS);
  }

  /**
   @return Work Concurrency
   */
  static int workConcurrency() {
    return getIntOrDefault("work.concurrency", DEFAULT_WORK_CONCURRENCY);
  }

  /**
   @return Work Buffer Seconds
   */
  static int workBufferSeconds() {
    return getIntOrDefault("work.buffer.seconds", DEFAULT_WORK_BUFFER_SECONDS);
  }

  /**
   @return Work Buffer Fabricate Delay Seconds
   */
  static int workBufferFabricateDelaySeconds() {
    return getIntOrDefault("work.buffer.fabricate.delay.seconds", DEFAULT_WORK_BUFFER_CRAFT_DELAY_SECONDS);
  }

  /**
   @return Work Chain Recur Seconds
   */
  static Integer workChainRecurSeconds() {
    return getIntOrDefault("work.chain.recur.seconds", DEFAULT_WORK_CHAIN_RECUR_SECONDS);
  }

  /**
   @return Work Chain Erase Recur Seconds
   */
  static Integer workChainEraseRecurSeconds() {
    return getIntOrDefault("work.chain.delete.recur.seconds", DEFAULT_WORK_CHAIN_ERASE_RECUR_SECONDS);
  }

  /**
   @return Work Chain Delay Seconds
   */
  static Integer workChainDelaySeconds() {
    return getIntOrDefault("work.chain.delay.seconds", DEFAULT_WORK_CHAIN_DELAY_SECONDS);
  }

  /**
   @return Work Temp File Path Prefix
   */
  static String workTempFilePathPrefix() {
    return getOrDefault("work.temp.file.path.prefix", getTempFilePathPrefix());
  }

  /**
   @return Work Queue Name
   */
  static String workQueueName() {
    return getOrDefault("work.queue.name", "xj_work");
  }

  /**
   Set a System Property if no value has yet been set for it.

   @param key name of system property
   @param val default value to set for property
   */
  static void setDefault(String key, String val) {
    if (Objects.isNull(System.getProperty(key))) {
      System.setProperty(key, val);
    }
  }

  static Boolean is(String key) throws ConfigException {
    String value = get(key);
    return Boolean.valueOf(value);
  }

  /**
   Get a Boolean value from a system property, else (if null) return default value

   @param key          of system property to get
   @param defaultValue to return if no system property is set
   @return value
   */
  static Boolean isOrDefault(String key, Boolean defaultValue) {
    try {
      return is(key);
    } catch (ConfigException ignored) {
      return defaultValue;
    }
  }

  /**
   Get an Integer value from a system property

   @param key of system property to get
   @return value
   @throws ConfigException if the system property is not set
   */
  static Integer getInt(String key) throws ConfigException {
    String value = get(key);
    return Integer.valueOf(value);
  }

  /**
   Get a Long value from a system property

   @param key of system property to get
   @return value
   @throws ConfigException if the system property is not set
   */
  static Long getLong(String key) throws ConfigException {
    String value = get(key);
    return Long.valueOf(value);
  }

  /**
   Get a Double value from a system property

   @param key of system property to get
   @return value
   @throws ConfigException if the system property is not set
   */
  static Double getDouble(String key) throws ConfigException {
    String value = get(key);
    return Double.valueOf(value);
  }

  /**
   Get an Integer value from a system property, else (if null) return default value

   @param key          of system property to get
   @param defaultValue to return if no system property is set
   @return value
   */
  static Integer getIntOrDefault(String key, Integer defaultValue) {
    try {
      return getInt(key);
    } catch (ConfigException ignored) {
      return defaultValue;
    }
  }

  /**
   Get a Long value from a system property, else (if null) return default value

   @param key          of system property to get
   @param defaultValue to return if no system property is set
   @return value
   */
  static Long getLongOrDefault(String key, Long defaultValue) {
    try {
      return getLong(key);
    } catch (ConfigException ignored) {
      return defaultValue;
    }
  }

  /**
   Get a Double value from a system property, else (if null) return default value

   @param key          of system property to get
   @param defaultValue to return if no system property is set
   @return value
   */
  static Double getDoubleOrDefault(String key, Double defaultValue) {
    try {
      return getDouble(key);
    } catch (ConfigException ignored) {
      return defaultValue;
    }
  }

  /**
   Get a String value from a system property

   @param key of system property to get
   @return value
   @throws ConfigException if the system property is not set
   */
  static String get(String key) throws ConfigException {
    String value = System.getProperty(key);
    if (Objects.isNull(value)) {
      throw new ConfigException("Must set system property: " + key);
    }
    return value;
  }

  /**
   Get a String value from a system property, else (if null) return default value

   @param key          of system property to get
   @param defaultValue to return if no system property is set
   @return value
   */
  static String getOrDefault(String key, String defaultValue) {
    try {
      return get(key);
    } catch (ConfigException ignored) {
      return defaultValue;
    }
  }

  /**
   @return temp file path prefix
   */
  static String getTempFilePathPrefix() {
    String path = File.separator + DEFAULT_TEMP_FILE_PATH_PREFIX_CHUNK + File.separator;
    try {
      String absolutePath = File.createTempFile(DEFAULT_TEMP_FILE_PATH_PREFIX_CREATE_NAME, DEFAULT_TEMP_FILE_PATH_PREFIX_CREATE_SUFFIX).getAbsolutePath();
      path = absolutePath.substring(0, absolutePath.lastIndexOf(File.separator)) + File.separator;

    } catch (IOException ignored) {
      // noop
    }
    return path;
  }

  /**
   @return line separator
   */
  static String lineSeparator() {
    return System.getProperty("line.separator");
  }

  /**
   @return order (# of past states upon which the current state prediction is dependent) of Markov-chain prediction
   */
  static Integer chordMarkovOrder() {
    return getIntOrDefault("chord.markov.order", DEFAULT_CHORD_MARKOV_ORDER);
  }

  /**
   @return multiplier of # of patterns per sequence, when generating a sequence
   */
  static Integer generationSequencePatternsMultiplier() {
    return getIntOrDefault("generation.sequence.patterns.multiplier", DEFAULT_GENERATION_SEQUENCE_PATTERNS_MULTIPLIER);
  }

  /**
   @return safety margin (# beats) at beginning and end of pattern during splice
   */
  static Integer generationSpliceSafetyMargin() {
    return getIntOrDefault("generation.splice.safety.margin", DEFAULT_GENERATION_SPLICE_SAFETY_MARGIN);
  }
}
