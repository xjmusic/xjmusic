// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.config;

import com.google.common.collect.Maps;
import io.xj.core.exception.CoreException;
import io.xj.core.model.ChainConfigType;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.ProgramPatternType;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramType;
import io.xj.core.model.SegmentState;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.Objects;

/**
 ALL APPLICATION CONFIGURATION MUST IMPLEMENT THIS CENTRAL CLASS.
 */
public interface Config {
  // time
  int DAYS_PER_MONTH = 28;
  int HOURS_PER_DAY = 24;
  int MINUTES_PER_HOUR = 60;
  int SECONDS_PER_MINUTE = 60;
  // defaults
  double DEFAULT_MIXER_COMPRESS_AHEAD_SECONDS = 0.05;
  double DEFAULT_MIXER_COMPRESS_DECAY_SECONDS = 0.125;
  double DEFAULT_MIXER_COMPRESS_RATIO_MAX = 10.0;
  double DEFAULT_MIXER_COMPRESS_RATIO_MIN = 0.5;
  double DEFAULT_MIXER_COMPRESS_TO_AMPLITUDE = 5.0;
  double DEFAULT_MIXER_HIGHPASS_THRESHOLD_HZ = 60.0;
  double DEFAULT_MIXER_LOWPASS_THRESHOLD_HZ = 6000.0;
  double DEFAULT_MIXER_NORMALIZATION_MAX = 0.999;
  double DEFAULT_TUNING_ROOT_PITCH = 432.0;
  int DEFAULT_APP_PORT = 80;
  int DEFAULT_AUDIO_FILE_UPLOAD_EXPIRE_MINUTES = 60;
  int DEFAULT_AWS_S3_RETRY_LIMIT = 10;
  int DEFAULT_CHAIN_PREVIEW_LENGTH_MAX = 300;
  int DEFAULT_CHAIN_REVIVE_THRESHOLD_HEAD_SECONDS = 120;
  int DEFAULT_CHAIN_REVIVE_THRESHOLD_START_SECONDS = 300;
  int DEFAULT_CHORD_MARKOV_ORDER = 3;
  int DEFAULT_COMPUTE_TIME_FRAMES_PER_BEAT = 64;
  int DEFAULT_COMPUTE_TIME_RESOLUTION_HZ = 1000000;
  int DEFAULT_DIGEST_CACHE_EXPIRE_MINUTES = 3;
  int DEFAULT_DIGEST_CACHE_REFRESH_MINUTES = 1;
  int DEFAULT_GENERATION_SEQUENCE_PATTERNS_MULTIPLIER = 3;
  int DEFAULT_GENERATION_SPLICE_SAFETY_MARGIN = 1;
  int DEFAULT_INGEST_ACHE_SECONDS = 60;
  int DEFAULT_INGEST_CHORD_SEQUENCE_LENGTH_MAX = 5;
  int DEFAULT_INGEST_CHORD_SEQUENCE_PRESERVE_LENGTH_MIN = 2;
  int DEFAULT_INGEST_CHORD_SEQUENCE_REDUNDANCY_THRESHOLD = 1;
  int DEFAULT_LIMIT_SEGMENT_READ_SIZE = 20;
  int DEFAULT_MIXER_DSP_BUFFER_SIZE = 1024; // DSP buffer size must be a power of 2
  int DEFAULT_MIXER_SAMPLE_ATTACK_MICROS = 1000;
  int DEFAULT_MIXER_SAMPLE_RELEASE_MICROS = 50000;
  int DEFAULT_PLATFORM_MESSAGE_READ_PREVIOUS_DAYS = 90;
  int DEFAULT_PLAY_BUFFER_AHEAD_SECONDS = 60;
  int DEFAULT_PLAY_BUFFER_DELAY_SECONDS = 5;
  int DEFAULT_REDIS_PORT = 6300;
  int DEFAULT_REDIS_TIMEOUT = 300;
  int DEFAULT_SEGMENT_REQUEUE_SECONDS = 1;
  int DEFAULT_WORK_BUFFER_CRAFT_DELAY_SECONDS = 1;
  int DEFAULT_WORK_BUFFER_SECONDS = 300;
  int DEFAULT_WORK_CHAIN_DELAY_SECONDS = 1;
  int DEFAULT_WORK_CHAIN_ERASE_RECUR_SECONDS = 10;
  int DEFAULT_WORK_CHAIN_RECUR_SECONDS = 2;
  int DEFAULT_WORK_CONCURRENCY = 12;
  long DEFAULT_CACHE_FILE_ALLOCATE_BYTES = 1_000_000_000L; // 1 gigabyte
  long DEFAULT_DIGEST_CACHE_SIZE = 1_000_000L;
  String DEFAULT_ACCESS_TOKEN_NAME = "access_token";
  String DEFAULT_API_PATH = "api/1/";
  String DEFAULT_APP_BASE_URL = "http://localhost/";
  String DEFAULT_APP_HOST = "0.0.0.0";
  String DEFAULT_APP_HOSTNAME = "localhost";
  String DEFAULT_APP_NAME = "app";
  String DEFAULT_APP_PATH_UNAUTHORIZED = "unauthorized";
  String DEFAULT_APP_PATH_WELCOME = "";
  String DEFAULT_AUDIO_FILE_UPLOAD_ACL = "bucket-owner-full-control";
  String DEFAULT_AWS_DEFAULT_REGION = "us-east-1";
  String DEFAULT_EXTENSION_SEPARATOR = ".";
  String DEFAULT_NAME_SEPARATOR = "-";
  String DEFAULT_FILE_SEPARATOR = "/";
  String DEFAULT_LOG_ACCESS_FILE_NAME_SUFFIX = "access.log";
  String DEFAULT_PLATFORM_RELEASE = "develop";
  String DEFAULT_TEMP_FILE_PATH_PREFIX_CHUNK = "tmp";
  String DEFAULT_TEMP_FILE_PATH_PREFIX_CREATE_NAME = "temp-file-name";
  String DEFAULT_TEMP_FILE_PATH_PREFIX_CREATE_SUFFIX = ".tmp";
  String DEFAULT_TUNING_ROOT_NOTE = "A4";

  // W3C standard
  String W3C_PATH_SEPARATOR = "/"; // do not touch this! It's part of the W3C spec

  /**
   Get a String value of a system property

   @param key of system property to get
   @return value
   @throws CoreException if the system property is not set
   */
  static String get(String key) throws CoreException {
    String value = System.getProperty(key);
    if (Objects.isNull(value)) {
      throw new CoreException("Must set system property: " + key);
    }
    return value;
  }

  /**
   @return Access token name (for Cookies)
   */
  static String getAccessTokenDomain() {
    return getOrDefault("access.token.domain", "");
  }

  /**
   @return Access token max age in seconds (for Cookies)
   */
  static int getAccessTokenMaxAge() {
    return getIntOrDefault("access.token.max.age", SECONDS_PER_MINUTE * MINUTES_PER_HOUR * HOURS_PER_DAY * DAYS_PER_MONTH);
  }

  /**
   @return Access token name that will be used for client API access (for Cookies)
   */
  static String getAccessTokenName() {
    return getOrDefault("access.token.name", DEFAULT_ACCESS_TOKEN_NAME);
  }

  /**
   @return Access token path (for Cookies)
   */
  static String getAccessTokenPath() {
    return getOrDefault("access.token.path", W3C_PATH_SEPARATOR);
  }

  /**
   Get platform configuration

   @return JSON object
   */
  static Map<String, Object> getAPI() throws CoreException {
    Map<String, Object> config = Maps.newHashMap();
    config.put("apiBaseUrl", getAppBaseUrl() + getApiPath());
    config.put("audioBaseUrl", getAudioBaseUrl());
    config.put("baseUrl", getAppBaseUrl());
    config.put("chainConfigTypes", ChainConfigType.stringValues());
    config.put("chainStates", ChainState.stringValues());
    config.put("chainTypes", ChainType.stringValues());
    config.put("choiceTypes", ProgramType.stringValues());
    config.put("instrumentStates", InstrumentState.stringValues());
    config.put("instrumentTypes", InstrumentType.stringValues());
    config.put("patternDetailTypes", ProgramPatternType.stringValuesForDetailSequence());
    config.put("patternTypes", ProgramPatternType.stringValues());
    config.put("programStates", ProgramState.stringValues());
    config.put("programTypes", ProgramType.stringValues());
    config.put("segmentBaseUrl", getSegmentBaseUrl());
    config.put("segmentStates", SegmentState.stringValues());
    config.put("voiceTypes", InstrumentType.stringValues());
    return config;
  }

  /**
   @return API path
   */
  static String getApiPath() {
    return getOrDefault("app.url.api", DEFAULT_API_PATH);
  }

  /**
   Get URI object for a path within the API

   @param path within API
   @return String
   */
  static URI getApiURI(String path) {
    return URI.create(getApiUrlString(path));
  }

  /**
   Get URL String for a path within the API

   @param path within API
   @return String
   */
  static String getApiUrlString(String path) {
    return getAppBaseUrl() + getApiPath() + path;
  }

  /**
   @return app base URL
   */
  static String getAppBaseUrl() {
    return getOrDefault("app.url.base", DEFAULT_APP_BASE_URL);
  }

  /**
   @return app host
   */
  static String getAppHost() {
    return getOrDefault("app.host", DEFAULT_APP_HOST);
  }

  /**
   @return app hostname
   */
  static String getAppHostname() {
    return getOrDefault("app.hostname", DEFAULT_APP_HOSTNAME);
  }

  /**
   @return app name
   */
  static String getAppName() {
    return getOrDefault("app.name", DEFAULT_APP_NAME);
  }

  /**
   @return app login welcome path
   */
  static String getAppPathSuccess() {
    return getOrDefault("app.path.welcome", DEFAULT_APP_PATH_WELCOME);
  }

  /**
   @return app path unauthorized
   */
  static String getAppPathUnauthorized() {
    return getOrDefault("app.path.unauthorized", DEFAULT_APP_PATH_UNAUTHORIZED);
  }

  /**
   @return app port
   */
  static Integer getAppPort() {
    return getIntOrDefault("app.port", DEFAULT_APP_PORT);
  }

  /**
   @return Audio base URL (for Amazon S3)
   */
  static String getAudioBaseUrl() throws CoreException {
    return get("audio.url.base");
  }

  /**
   get Audio File Bucket (required)

   @return Audio File Bucket
   @throws CoreException if not set
   */
  static String getAudioFileBucket() throws CoreException {
    return get("audio.file.bucket");
  }

  /**
   @return Audio file upload ACL (for Amazon S3)
   */
  static String getAudioFileUploadACL() {
    return getOrDefault("audio.file.upload.acl", DEFAULT_AUDIO_FILE_UPLOAD_ACL);
  }

  /**
   @return Audio File upload expire # minutes (for Amazon S3)
   */
  static int getAudioFileUploadExpireMinutes() {
    return getIntOrDefault("audio.file.upload.expire.minutes", DEFAULT_AUDIO_FILE_UPLOAD_EXPIRE_MINUTES);
  }

  /**
   @return Audio upload URL (for Amazon S3)
   */
  static String getAudioUploadUrl() throws CoreException {
    return get("audio.url.upload");
  }

  /**
   Get URL String for an audio file, by key

   @param key of audio to retrieve a URL for (empty for base URL)
   @return String
   */
  static String getAudioUrl(String key) throws CoreException {
    return getAudioBaseUrl() + key;
  }

  /**
   get Google Authentication ID (required)

   @return Google Authentication ID
   @throws CoreException if not set
   */
  static String getAuthGoogleId() throws CoreException {
    return get("auth.google.id");
  }

  /**
   get Google Authentication Secret (required)

   @return Google Authentication Secret
   @throws CoreException if not set
   */
  static String getAuthGoogleSecret() throws CoreException {
    return get("auth.google.secret");
  }

  /**
   get AWS Access Key ID (required)

   @return AWS Access Key ID
   @throws CoreException if not set
   */
  static String getAwsAccessKeyId() throws CoreException {
    return get("aws.accessKeyId");
  }

  /**
   get AWS Default Region

   @return AWS Default Region
   */
  static String getAwsDefaultRegion() {
    return getOrDefault("aws.defaultRegion", DEFAULT_AWS_DEFAULT_REGION);
  }

  /**
   get AWS s3 Retry limit

   @return AWS s3 Retry limit
   */
  static int getAwsS3RetryLimit() {
    return getIntOrDefault("aws.s3.retry.limit", DEFAULT_AWS_S3_RETRY_LIMIT);
  }

  /**
   get AWS Secret (required)

   @return AWS Secret
   @throws CoreException if not set
   */
  static String getAwsSecretKey() throws CoreException {
    return get("aws.secretKey");
  }

  /**
   @return Cache file allocate bytes
   */
  static Long getCacheFileAllocateBytes() {
    return getLongOrDefault("cache.file.allocate.bytes", DEFAULT_CACHE_FILE_ALLOCATE_BYTES);
  }

  /**
   @return Cache file path prefix
   */
  static String getCacheFilePathPrefix() {
    return getOrDefault("cache.file.path.prefix", getTempFilePathPrefix() + "cache" + File.separator);
  }

  /**
   @return Cache file suffix
   */
  static String getCacheFilePathSuffix() {
    return getOrDefault("cache.file.path.suffix", ".data");
  }

  /**
   @return Max chain preview length
   */
  static int getChainPreviewLengthMax() {
    return getIntOrDefault("chain.preview.length.max", DEFAULT_CHAIN_PREVIEW_LENGTH_MAX);
  }

  /**
   Get chainReviveThresholdHeadSeconds

   @return chainReviveThresholdHeadSeconds
   */
  static Integer getChainReviveThresholdHeadSeconds() {
    return getIntOrDefault("chain.revive.threshold.head.seconds", DEFAULT_CHAIN_REVIVE_THRESHOLD_HEAD_SECONDS);
  }

  /**
   Get chainReviveThresholdStartSeconds

   @return chainReviveThresholdStartSeconds
   */
  static Integer getChainReviveThresholdStartSeconds() {
    return getIntOrDefault("chain.revive.threshold.start.seconds", DEFAULT_CHAIN_REVIVE_THRESHOLD_START_SECONDS);
  }

  /**
   @return order (# of past states upon which the current state prediction is dependent) of Markov-chain prediction
   */
  static Integer getChordMarkovOrder() {
    return getIntOrDefault("chord.markov.order", DEFAULT_CHORD_MARKOV_ORDER);
  }

  /**
   Get computeTimeFramesPerBeat

   @return computeTimeFramesPerBeat
   */
  static Integer getComputeTimeFramesPerBeat() {
    return getIntOrDefault("compute.time.frames.per.beat", DEFAULT_COMPUTE_TIME_FRAMES_PER_BEAT);
  }

  /**
   Get computeTimeResolutionHz

   @return computeTimeResolutionHz
   */
  static Integer getComputeTimeResolutionHz() {
    return getIntOrDefault("compute.time.resolution.hz", DEFAULT_COMPUTE_TIME_RESOLUTION_HZ);
  }

  /**
   @return Database Postgres database name
   */
  static String getDbPostgresDatabase() {
    return getOrDefault("db.postgres.database", "xj");
  }

  /**
   @return Database Postgres database host
   */
  static String getDbPostgresHost() {
    return getOrDefault("db.postgres.host", "localhost");
  }

  /**
   @return Database Postgres database password
   */
  static String getDbPostgresPass() {
    return getOrDefault("db.postgres.pass", "");
  }

  /**
   @return Database Postgres database port
   */
  static String getDbPostgresPort() {
    return getOrDefault("db.postgres.port", "5400");
  }

  /**
   @return Database Postgres database use
   */
  static String getDbPostgresUser() {
    return getOrDefault("db.postgres.user", "root");
  }

  /**
   @return Database Redis Host
   */
  static String getDbRedisHost() {
    return getOrDefault("db.redis.host", "localhost");
  }

  /**
   @return Database Redis Port
   */
  static Integer getDbRedisPort() {
    return getIntOrDefault("db.redis.port", DEFAULT_REDIS_PORT);
  }

  /**
   @return Database Redis Queue Namespace
   */
  static String getDbRedisQueueNamespace() {
    return getOrDefault("db.redis.queue.namespace", "xj");
  }

  /**
   @return Database Redis Timeout
   */
  static Integer getDbRedisTimeout() {
    return getIntOrDefault("db.redis.timeout", DEFAULT_REDIS_TIMEOUT);
  }

  /**
   @return ingest digest cache expire # minutes
   */
  static int getDigestCacheExpireMinutes() {
    return getIntOrDefault("digest.cache.expire.minutes", DEFAULT_DIGEST_CACHE_EXPIRE_MINUTES);
  }

  /**
   @return ingest digest cache refresh # minutes
   */
  static int getDigestCacheRefreshMinutes() {
    return getIntOrDefault("digest.cache.refresh.minutes", DEFAULT_DIGEST_CACHE_REFRESH_MINUTES);
  }

  /**
   @return max size (in memory) for the ingest cache of one type of digest
   */
  static long getDigestCacheSize() {
    return getLongOrDefault("digest.cache.size", DEFAULT_DIGEST_CACHE_SIZE);
  }

  /**
   Get a Double value of a system property

   @param key of system property to get
   @return value
   @throws CoreException if the system property is not set
   */
  static Double getDouble(String key) throws CoreException {
    String value = get(key);
    return Double.valueOf(value);
  }

  /**
   Get a Double value of a system property, else (if null) return default value

   @param key          of system property to get
   @param defaultValue to return if no system property is set
   @return value
   */
  static Double getDoubleOrDefault(String key, Double defaultValue) {
    try {
      return getDouble(key);
    } catch (CoreException ignored) {
      return defaultValue;
    }
  }

  /**
   Get getDSPBufferSize

   @return getDSPBufferSize
   */
  static Integer getDSPBufferSize() {
    return getIntOrDefault("mixer.dsp.buffer.size", DEFAULT_MIXER_DSP_BUFFER_SIZE);
  }

  /**
   Get file dot (i.e. "dir/my-file.txt" the "." is the extension separator) for this platform

   @return dot for this platform
   */
  static String getExtensionSeparator() {
    return getOrDefault("file.dot", DEFAULT_EXTENSION_SEPARATOR);
  }

  /**
   Get file separator (i.e. "dir/my-file.txt" the "/" is the file separator) for this platform

   @return separator for this platform
   */
  static String getFileSeparator() {
    return getOrDefault("file.separator", DEFAULT_FILE_SEPARATOR);
  }

  /**
   Get name separator (i.e. "dir/my-name.txt" the "-" is the name separator) for this platform

   @return separator for this platform
   */
  static String getNameSeparator() {
    return getOrDefault("name.separator", DEFAULT_NAME_SEPARATOR);
  }

  /**
   @return multiplier of # of patterns per sequence, when generating a sequence
   */
  static Integer getGenerationSequenceBindingsMultiplier() {
    return getIntOrDefault("generation.sequence.patterns.multiplier", DEFAULT_GENERATION_SEQUENCE_PATTERNS_MULTIPLIER);
  }

  /**
   @return safety margin (# beats) at beginning and end of pattern during splice
   */
  static Integer getGenerationSpliceSafetyMargin() {
    return getIntOrDefault("generation.splice.safety.margin", DEFAULT_GENERATION_SPLICE_SAFETY_MARGIN);
  }

  /**
   @return number of seconds to cache an ingest before re-reading it of the DAOs
   */
  static int getIngestCacheSeconds() {
    return getIntOrDefault("ingest.cache.seconds", DEFAULT_INGEST_ACHE_SECONDS);
  }

  /**
   @return Max length of chord progression for ingest
   */
  static int getIngestChordProgressionLengthMax() {
    return getIntOrDefault("ingest.chord.sequence.length.max", DEFAULT_INGEST_CHORD_SEQUENCE_LENGTH_MAX);
  }

  /**
   @return threshold X, where during pruning of redundant subsets of chord progressions, a redundant subset with length greater than or equal to X will have its chord progressions preserved, meaning that they are moved into the ingest that is deprecating their original sequence descriptor.
   */
  static int getIngestChordProgressionPreserveLengthMin() {
    return getIntOrDefault("ingest.chord.sequence.preserve.length.min", DEFAULT_INGEST_CHORD_SEQUENCE_PRESERVE_LENGTH_MIN);
  }

  /**
   @return threshold X, where during pruning of redundant subsets of chord progressions, in order to be considered redundant, a subset must have length of at least X less than the length of the superset.
   */
  static int getIngestChordProgressionRedundancyThreshold() {
    return getIntOrDefault("ingest.chord.sequence.redundancy.threshold", DEFAULT_INGEST_CHORD_SEQUENCE_REDUNDANCY_THRESHOLD);
  }

  /**
   Get an Integer value of a system property

   @param key of system property to get
   @return value
   @throws CoreException if the system property is not set
   */
  static Integer getInt(String key) throws CoreException {
    String value = get(key);
    return Integer.valueOf(value);
  }

  /**
   Get an Integer value of a system property, else (if null) return default value

   @param key          of system property to get
   @param defaultValue to return if no system property is set
   @return value
   */
  static Integer getIntOrDefault(String key, Integer defaultValue) {
    try {
      return getInt(key);
    } catch (CoreException ignored) {
      return defaultValue;
    }
  }

  /**
   @return Limit Segment Read Size
   */
  static int getLimitSegmentReadSize() {
    return getIntOrDefault("limit.segment.read.size", DEFAULT_LIMIT_SEGMENT_READ_SIZE);
  }

  /**
   @return line separator
   */
  static String getLineSeparator() {
    return System.getProperty("line.separator");
  }

  /**
   @return Access log filename
   */
  static String getLogAccessFilename() {
    return getOrDefault("log.access.filename", getTempFilePathPrefix() + DEFAULT_LOG_ACCESS_FILE_NAME_SUFFIX);
  }

  /**
   Get a Long value of a system property

   @param key of system property to get
   @return value
   @throws CoreException if the system property is not set
   */
  static Long getLong(String key) throws CoreException {
    String value = get(key);
    return Long.valueOf(value);
  }

  /**
   Get a Long value of a system property, else (if null) return default value

   @param key          of system property to get
   @param defaultValue to return if no system property is set
   @return value
   */
  static Long getLongOrDefault(String key, Long defaultValue) {
    try {
      return getLong(key);
    } catch (CoreException ignored) {
      return defaultValue;
    }
  }

  /**
   Get getMixerCompressAheadSeconds

   @return getMixerCompressAheadSeconds
   */
  static Double getMixerCompressAheadSeconds() {
    return getDoubleOrDefault("mixer.compress.ahead.seconds", DEFAULT_MIXER_COMPRESS_AHEAD_SECONDS);
  }

  /**
   Get getMixerCompressDecaySeconds

   @return getMixerCompressDecaySeconds
   */
  static Double getMixerCompressDecaySeconds() {
    return getDoubleOrDefault("mixer.compress.decay.seconds", DEFAULT_MIXER_COMPRESS_DECAY_SECONDS);
  }

  /**
   Get getMixerCompressRatioMax

   @return getMixerCompressRatioMax
   */
  static Double getMixerCompressRatioMax() {
    return getDoubleOrDefault("mixer.compress.ratio.max", DEFAULT_MIXER_COMPRESS_RATIO_MAX);
  }

  /**
   Get getMixerCompressRatioMin

   @return getMixerCompressRatioMin
   */
  static Double getMixerCompressRatioMin() {
    return getDoubleOrDefault("mixer.compress.ratio.min", DEFAULT_MIXER_COMPRESS_RATIO_MIN);
  }

  /**
   Get getMixerCompressToAmplitude

   @return getMixerCompressToAmplitude
   */
  static Double getMixerCompressToAmplitude() {
    return getDoubleOrDefault("mixer.compress.to.amplitude", DEFAULT_MIXER_COMPRESS_TO_AMPLITUDE);
  }

  /**
   Get getMixerHighpassThresholdHz

   @return getMixerHighpassThresholdHz
   */
  static Double getMixerHighpassThresholdHz() {
    return getDoubleOrDefault("mixer.highpass.threshold.hz", DEFAULT_MIXER_HIGHPASS_THRESHOLD_HZ);
  }

  /**
   Get getMixerLowpassThresholdHz

   @return getMixerLowpassThresholdHz
   */
  static Double getMixerLowpassThresholdHz() {
    return getDoubleOrDefault("mixer.lowpass.threshold.hz", DEFAULT_MIXER_LOWPASS_THRESHOLD_HZ);
  }

  /**
   Get getMixerNormalizationMax

   @return getMixerNormalizationMax
   */
  static Double getMixerNormalizationMax() {
    return getDoubleOrDefault("mixer.normalization.max", DEFAULT_MIXER_NORMALIZATION_MAX);
  }

  /**
   Get getMixerSampleAttackMicros

   @return getMixerSampleAttackMicros
   */
  static Integer getMixerSampleAttackMicros() {
    return getIntOrDefault("mixer.sample.attack.micros", DEFAULT_MIXER_SAMPLE_ATTACK_MICROS);
  }

  /**
   Get getMixerSampleReleaseMicros

   @return getMixerSampleReleaseMicros
   */
  static Integer getMixerSampleReleaseMicros() {
    return getIntOrDefault("mixer.sample.release.micros", DEFAULT_MIXER_SAMPLE_RELEASE_MICROS);
  }

  /**
   Get a String value of a system property, else (if null) return default value

   @param key          of system property to get
   @param defaultValue to return if no system property is set
   @return value
   */
  static String getOrDefault(String key, String defaultValue) {
    try {
      return get(key);
    } catch (CoreException ignored) {
      return defaultValue;
    }
  }

  /**
   @return platform heartbeat key
   */
  static String getPlatformHeartbeatKey() throws CoreException {
    return get("platform.heartbeat.key");
  }

  /**
   @return platform message read # previous days
   */
  static Integer getPlatformMessageReadPreviousDays() {
    return getIntOrDefault("platform.message.read.previousDays", DEFAULT_PLATFORM_MESSAGE_READ_PREVIOUS_DAYS);
  }

  /**
   @return platform release version, overwritten in production so that platforms know what version has been deployed
   */
  static String getPlatformRelease() {
    return getOrDefault("platform.release", DEFAULT_PLATFORM_RELEASE);
  }

  /**
   @return Play Buffer Ahead Seconds
   */
  static int getPlayBufferAheadSeconds() {
    return getIntOrDefault("play.buffer.ahead.seconds", DEFAULT_PLAY_BUFFER_AHEAD_SECONDS);
  }

  /**
   @return Play Buffer Delay Seconds
   */
  static int getPlayBufferDelaySeconds() {
    return getIntOrDefault("play.buffer.delay.seconds", DEFAULT_PLAY_BUFFER_DELAY_SECONDS);
  }

  /**
   @return Segments base URL (for Amazon S3)
   */
  static String getSegmentBaseUrl() throws CoreException {
    return get("segment.url.base");
  }

  /**
   @return Segment File Bucket (for Amazon S3)
   */
  static String getSegmentFileBucket() throws CoreException {
    return get("segment.file.bucket");
  }

  /**
   Get segmentRequeueSeconds

   @return segmentRequeueSeconds
   */
  static Integer getSegmentRequeueSeconds() {
    return getIntOrDefault("segment.requeue.seconds", DEFAULT_SEGMENT_REQUEUE_SECONDS);
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
   The root Tuning Note of th eentire XJ platform, as a Note, e.g. "A4"

   @return Note of root tuning
   */
  static String getTuningRootNote() {
    return getOrDefault("tuning.root.note", DEFAULT_TUNING_ROOT_NOTE);
  }

  /**
   The root Tuning of the entire XJ platform, in Hz.

   @return Tuning Root Pitch, in Hz.
   */
  static Double getTuningRootPitch() {
    return getDoubleOrDefault("tuning.root.pitch", DEFAULT_TUNING_ROOT_PITCH);
  }

  /**
   @return Work Buffer Fabricate Delay Seconds
   */
  static int getWorkBufferFabricateDelaySeconds() {
    return getIntOrDefault("work.buffer.fabricate.delay.seconds", DEFAULT_WORK_BUFFER_CRAFT_DELAY_SECONDS);
  }

  /**
   @return Work Buffer Seconds
   */
  static int getWorkBufferSeconds() {
    return getIntOrDefault("work.buffer.seconds", DEFAULT_WORK_BUFFER_SECONDS);
  }

  /**
   @return Work Chain Delay Seconds
   */
  static Integer getWorkChainDelaySeconds() {
    return getIntOrDefault("work.chain.delay.seconds", DEFAULT_WORK_CHAIN_DELAY_SECONDS);
  }

  /**
   @return Work Chain Erase Recur Seconds
   */
  static Integer getWorkChainEraseRecurSeconds() {
    return getIntOrDefault("work.chain.delete.recur.seconds", DEFAULT_WORK_CHAIN_ERASE_RECUR_SECONDS);
  }

  /**
   @return Work Chain Recur Seconds
   */
  static Integer getWorkChainRecurSeconds() {
    return getIntOrDefault("work.chain.recur.seconds", DEFAULT_WORK_CHAIN_RECUR_SECONDS);
  }

  /**
   @return Work Concurrency
   */
  static int getWorkConcurrency() {
    return getIntOrDefault("work.concurrency", DEFAULT_WORK_CONCURRENCY);
  }

  /**
   @return Work Queue Name
   */
  static String getWorkQueueName() {
    return getOrDefault("work.queue.name", "xj_work");
  }

  /**
   @return Work Temp File Path Prefix
   */
  static String getWorkTempFilePathPrefix() {
    return getOrDefault("work.temp.file.path.prefix", getTempFilePathPrefix());
  }

  /**
   @return true if API errors should include a stack trace
   */
  static Boolean hasApiErrorStackTrace() {
    return isTruthyOrDefault("api.errors.stacktrace", true);
  }

  /**
   Get an Boolean value of a system property

   @param key of system property to get
   @return value
   @throws CoreException if the system property is not set
   */
  static Boolean isTruthy(String key) throws CoreException {
    String value = get(key);
    return Boolean.valueOf(value);
  }

  /**
   Get an Boolean value of a system property, else (if null) return default value

   @param key          of system property to get
   @param defaultValue to return if no system property is set
   @return value
   */
  static Boolean isTruthyOrDefault(String key, Boolean defaultValue) {
    try {
      return isTruthy(key);
    } catch (CoreException ignored) {
      return defaultValue;
    }
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

  /**
   Get ratio of free disk space (relative to total disk space) below which alarm will be thrown by health check.
   @return lower ratio of free disk space
   */
  static Double getAlarmDiskFreeRatioLower() {
    return getDoubleOrDefault("alarm.disk.free.ratio.lower", 0.1);
  }
}
