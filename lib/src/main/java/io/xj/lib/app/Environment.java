// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.app;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 All secrets from environment variables
 */
public class Environment {
  private static final Logger LOG = LoggerFactory.getLogger(Environment.class);
  private static final int SECONDS_PER_HOUR = 60 * 60;
  private static final String EMPTY = "";
  private final String accessLogFilename;
  private final String accessTokenDomain;
  private final String accessTokenName;
  private final String accessTokenPath;
  private final String apiUnauthorizedRedirectPath;
  private final String apiWelcomeRedirectPath;
  private final String appBaseURL;
  private final String audioBaseURL;
  private final String audioCacheFilePrefix;
  private final String audioFileBucket;
  private final String audioUploadURL;
  private final String awsAccessKeyID;
  private final String awsDefaultRegion;
  private final String awsFileUploadACL;
  private final String awsSecretKey;
  private final String awsSecretName;
  private final String awsSnsTopicArn;
  private final String gcpProjectId;
  private final String gcpSecretId;
  private final String googleClientID;
  private final String googleClientSecret;
  private final String hostname;
  private final String ingestTokenName;
  private final String ingestTokenValue;
  private final String ingestURL;
  private final String platformEnvironment;
  private final String playerBaseURL;
  private final String postgresDatabase;
  private final String postgresHost;
  private final String postgresPass;
  private final String postgresSchemas;
  private final String postgresUser;
  private final String redisHost;
  private final String redisSessionNamespace;
  private final String shipBaseUrl;
  private final String shipBucket;
  private final String shipChunkAudioEncoder;
  private final String shipChunkContentType;
  private final String shipFFmpegVerbosity;
  private final String shipKey;
  private final String shipM3u8ContentType;
  private final String shipMode;
  private final String streamBaseURL;
  private final String streamBucket;
  private final String tempFilePathPrefix;
  private final boolean telemetryEnabled;
  private final boolean workJanitorEnabled;
  private final boolean workMedicEnabled;
  private final boolean workRehydrationEnabled;
  private final int accessTokenMaxAgeSeconds;
  private final int appPort;
  private final int awsS3retryLimit;
  private final int awsUploadExpireMinutes;
  private final int chainStartInFutureSeconds;
  private final int fabricationChainThresholdFabricatedBehindSeconds;
  private final int fabricationPreviewLengthMaxHours;
  private final int httpClientPoolMaxPerRoute;
  private final int httpClientPoolMaxTotal;
  private final int playbackExpireSeconds;
  private final int postgresPoolSizeMax;
  private final int postgresPort;
  private final int redisPort;
  private final int segmentComputeTimeFramesPerBeat;
  private final int segmentComputeTimeResolutionHz;
  private final int shipBitrateHigh;
  private final int shipChainJsonMaxAgeSeconds;
  private final int shipChunkTargetDuration;
  private final int shipInitialMediaSequenceNumberOffset;
  private final int shipLoadCycleSeconds;
  private final int shipM3u8MaxAgeSeconds;
  private final int shipMixCycleSeconds;
  private final int shipPlaylistAheadSeconds;
  private final int shipPlaylistBackSeconds;
  private final int shipSegmentLoadAheadSeconds;
  private final int shipSegmentLoadTimeoutSeconds;
  private final int workCycleMillis;
  private final int workEraseSegmentsOlderThanSeconds;
  private final int workHealthCycleStalenessThresholdSeconds;
  private final int workIngestCycleSeconds;
  private final int workJanitorCycleSeconds;
  private final int workLabHubLabPollSeconds;
  private final int workMedicCycleSeconds;
  private final int workPublishCycleSeconds;
  private final int workRehydrateFabricatedAheadThreshold;
  private final int workTelemetryCycleSeconds;

  // This must be set manually by the bootstrap before starting the application
  private String appName;

  /**
   Zero-argument construction defaults to system environment
   */
  @Inject
  public Environment() {
    this(System.getenv());
  }

  /**
   Manual construction

   @param vars to build environment from
   */
  public Environment(Map<String, String> vars) {
    LOG.debug("Received values for {} keys: {}", vars.size(), CSV.join(vars.keySet()));

    // Application
    accessLogFilename = readStr(vars, "ACCESS_LOG_FILENAME", "/tmp/access.log");
    accessTokenDomain = readStr(vars, "ACCESS_TOKEN_DOMAIN", "");
    accessTokenMaxAgeSeconds = readInt(vars, "ACCESS_TOKEN_MAX_AGE_SECONDS", 2419200);
    accessTokenName = readStr(vars, "ACCESS_TOKEN_NAME", "access_token");
    accessTokenPath = readStr(vars, "ACCESS_TOKEN_PATH", "/");
    apiUnauthorizedRedirectPath = readStr(vars, "API_UNAUTHORIZED_REDIRECT_PATH", "unauthorized");
    apiWelcomeRedirectPath = readStr(vars, "API_WELCOME_REDIRECT_PATH", "");
    appBaseURL = readStr(vars, "APP_BASE_URL", "http://localhost/");
    appName = readStr(vars, "APP_NAME", "app");
    appPort = readInt(vars, "APP_PORT", 3000);
    audioBaseURL = readStr(vars, "AUDIO_BASE_URL", "https://audio.dev.xj.io/");
    audioCacheFilePrefix = readStr(vars, "AUDIO_CACHE_FILE_PREFIX", "/tmp/");
    audioFileBucket = readStr(vars, "AUDIO_FILE_BUCKET", "xj-dev-audio");
    audioUploadURL = readStr(vars, "AUDIO_UPLOAD_URL", "https://xj-dev-audio.s3.amazonaws.com/");
    chainStartInFutureSeconds = readInt(vars, "CHAIN_START_IN_FUTURE_SECONDS", 0);
    fabricationChainThresholdFabricatedBehindSeconds = readInt(vars, "FABRICATION_CHAIN_THRESHOLD_FABRICATED_BEHIND_SECONDS", 15);
    fabricationPreviewLengthMaxHours = readInt(vars, "FABRICATION_PREVIEW_LENGTH_MAX_HOURS", 8);
    hostname = readStr(vars, "HOSTNAME", "localhost");
    httpClientPoolMaxPerRoute = readInt(vars, "HTTP_CLIENT_POOL_MAX_PER_ROUTE", 20);
    httpClientPoolMaxTotal = readInt(vars, "HTTP_CLIENT_POOL_MAX_TOTAL", 200);
    ingestTokenName = readStr(vars, "INGEST_TOKEN_NAME", "access_token");
    ingestTokenValue = readStr(vars, "INGEST_TOKEN_VALUE", EMPTY);
    ingestURL = readStr(vars, "INGEST_URL", "http://localhost/");
    platformEnvironment = readStr(vars, "ENVIRONMENT", "dev");
    playbackExpireSeconds = readInt(vars, "PLAYBACK_EXPIRE_SECONDS", SECONDS_PER_HOUR * 8);
    playerBaseURL = readStr(vars, "PLAYER_BASE_URL", "http://localhost/");
    segmentComputeTimeFramesPerBeat = readInt(vars, "SEGMENT_COMPUTE_TIME_FRAMES_PER_BEAT", 64);
    segmentComputeTimeResolutionHz = readInt(vars, "SEGMENT_COMPUTE_TIME_RESOLUTION_HZ", 1000000);
    shipBaseUrl = readStr(vars, "SHIP_BASE_URL", "https://ship.dev.xj.io/");
    shipBitrateHigh = readInt(vars, "SHIP_BITRATE_HIGH", 320000);
    shipBucket = readStr(vars, "SHIP_BUCKET", "xj-dev-ship");
    shipChainJsonMaxAgeSeconds = readInt(vars, "SHIP_CHAIN_JSON_MAX_AGE_SECONDS", 15);
    shipChunkAudioEncoder = readStr(vars, "SHIP_CHUNK_AUDIO_ENCODER", "aac");
    shipChunkContentType = readStr(vars, "SHIP_CHUNK_CONTENT_TYPE", "audio/aac");
    shipChunkTargetDuration = readInt(vars, "SHIP_CHUNK_TARGET_DURATION", 10);
    shipFFmpegVerbosity = readStr(vars, "SHIP_FFMPEG_VERBOSITY", "info");
    shipInitialMediaSequenceNumberOffset = readInt(vars, "SHIP_INITIAL_MEDIA_SEQUENCE_NUMBER_OFFSET", 3);
    shipKey = readStr(vars, "SHIP_KEY", EMPTY);
    shipLoadCycleSeconds = readInt(vars, "SHIP_LOAD_CYCLE_SECONDS", 20);
    shipM3u8ContentType = readStr(vars, "SHIP_M3U8_CONTENT_TYPE", "application/vnd.apple.mpegurl");
    shipM3u8MaxAgeSeconds = readInt(vars, "SHIP_M3U8_MAX_AGE_SECONDS", 5);
    shipMixCycleSeconds = readInt(vars, "WORK_PRINT_CYCLE_SECONDS", 1);
    shipMode = readStr(vars, "SHIP_MODE", "hls");
    shipPlaylistAheadSeconds = readInt(vars, "SHIP_PLAYLIST_AHEAD_SECONDS", 30);
    shipPlaylistBackSeconds = readInt(vars, "SHIP_PLAYLIST_BACK_SECONDS", 300);
    shipSegmentLoadAheadSeconds = readInt(vars, "SHIP_SEGMENT_LOAD_AHEAD_SECONDS", 120);
    shipSegmentLoadTimeoutSeconds = readInt(vars, "SHIP_SEGMENT_LOAD_TIMEOUT_SECONDS", 5);
    streamBaseURL = readStr(vars, "STREAM_BASE_URL", "https://stream.dev.xj.io/");
    streamBucket = readStr(vars, "STREAM_BUCKET", "xj-dev-stream");
    telemetryEnabled = readBool(vars, "TELEMETRY_ENABLED", false);
    tempFilePathPrefix = readStr(vars, "TEMP_FILE_PATH_PREFIX", "/tmp/");
    workCycleMillis = readInt(vars, "WORK_CYCLE_MILLIS", 1200);
    workEraseSegmentsOlderThanSeconds = readInt(vars, "WORK_ERASE_SEGMENTS_OLDER_THAN_SECONDS", 300);
    workHealthCycleStalenessThresholdSeconds = readInt(vars, "WORK_HEALTH_CYCLE_STALENESS_THRESHOLD_SECONDS", 60);
    workIngestCycleSeconds = readInt(vars, "WORK_INGEST_CYCLE_SECONDS", 20);
    workJanitorCycleSeconds = readInt(vars, "WORK_JANITOR_CYCLE_SECONDS", 60);
    workJanitorEnabled = readBool(vars, "WORK_JANITOR_ENABLED", true);
    workLabHubLabPollSeconds = readInt(vars, "WORK_LAB_HUB_LAB_POLL_SECONDS", 10);
    workMedicCycleSeconds = readInt(vars, "WORK_MEDIC_CYCLE_SECONDS", 30);
    workMedicEnabled = readBool(vars, "WORK_MEDIC_ENABLED", true);
    workPublishCycleSeconds = readInt(vars, "WORK_PUBLISH_CYCLE_SECONDS", 10);
    workRehydrateFabricatedAheadThreshold = readInt(vars, "WORK_REHYDRATE_FABRICATED_AHEAD_THRESHOLD", 60);
    workRehydrationEnabled = readBool(vars, "WORK_REHYDRATION_ENABLED", true);
    workTelemetryCycleSeconds = readInt(vars, "WORK_TELEMETRY_CYCLE_SECONDS", 2);

    // Resource: Amazon Web Services (AWS)
    awsAccessKeyID = readStr(vars, "AWS_ACCESS_KEY_ID", EMPTY);
    awsDefaultRegion = readStr(vars, "AWS_DEFAULT_REGION", EMPTY);
    awsFileUploadACL = readStr(vars, "AWS_FILE_UPLOAD_ACL", "bucket-owner-full-control");
    awsS3retryLimit = readInt(vars, "AWS_S3_RETRY_LIMIT", 10);
    awsSecretKey = readStr(vars, "AWS_SECRET_KEY", EMPTY);
    awsSecretName = readStr(vars, "AWS_SECRET_NAME", EMPTY);
    awsSnsTopicArn = readStr(vars, "AWS_SNS_TOPIC_ARN", EMPTY);
    awsUploadExpireMinutes = readInt(vars, "AWS_UPLOAD_EXPIRE_MINUTES", 60);

    // Resource: Google
    gcpProjectId = readStr(vars, "GCP_PROJECT_ID", EMPTY);
    gcpSecretId = readStr(vars, "GCP_SECRET_ID", EMPTY);
    googleClientID = readStr(vars, "GOOGLE_CLIENT_ID", EMPTY);
    googleClientSecret = readStr(vars, "GOOGLE_CLIENT_SECRET", EMPTY);

    // Resource: Postgres
    postgresDatabase = readStr(vars, "POSTGRES_DATABASE", "xj_test");
    postgresHost = readStr(vars, "POSTGRES_HOST", "localhost");
    postgresPass = readStr(vars, "POSTGRES_PASS", "postgres");
    postgresPoolSizeMax = readInt(vars, "postgres pool size max", 20);
    postgresPort = readInt(vars, "POSTGRES_PORT", 5432);
    postgresSchemas = readStr(vars, "postgres schemas", "xj");
    postgresUser = readStr(vars, "POSTGRES_USER", "postgres");

    // Resource: Redis
    redisHost = readStr(vars, "REDIS_HOST", "localhost");
    redisPort = readInt(vars, "REDIS_PORT", 6379);
    redisSessionNamespace = readStr(vars, "REDIS_SESSION_NAMESPACE", "xj_session");
  }

  /**
   Get the environment from a specific set of variables

   @param vars from which to get environment
   @return environment from variables
   */
  public static Environment from(Map<String, String> vars) {
    return new Environment(vars);
  }

  /**
   Get the environment from system environment variables

   @return system environment
   */
  public static Environment fromSystem() {
    return from(System.getenv());
  }

  /**
   Get the default environment

   @return environment with default values
   */
  public static Environment getDefault() {
    return from(ImmutableMap.of());
  }

  /**
   Augment the system environment variables with a source text body of key=value lines

   @param secretKeyValueLines to parse for key=value lines
   @return system environment augmented with source
   */
  public static Environment augmentSystem(String secretKeyValueLines) {
    var vars = new HashMap<>(System.getenv());
    var pairs = Text.parseEnvironmentVariableKeyPairs(secretKeyValueLines);
    vars.putAll(pairs);
    if (0 < pairs.size())
      LOG.info("Augmented system environment with {} secrets having keys {}", pairs.size(), CSV.join(pairs.keySet()));
    else
      LOG.warn("Did not parse any secrets with which to augment system environment.");
    return from(vars);
  }

  /**
   Get an integer value from the given map, or if the key isn't in the map, return the default value

   @param map    in which to search for a key
   @param key    to search for
   @param orElse to return if the key is not found in the map
   @return value at key in map, else the default value
   */
  @SuppressWarnings("SameParameterValue")
  private Integer readInt(Map<String, String> map, String key, Integer orElse) {
    if (!map.containsKey(key)) return orElse;
    try {
      return Integer.valueOf(map.get(key));

    } catch (NumberFormatException e) {
      LOG.warn("{} has non-int value: {}", key, map.get(key));
      return orElse;
    }
  }

  /**
   Get a boolean value from the given map, or if the key isn't in the map, return the default value

   @param map    in which to search for a key
   @param key    to search for
   @param orElse to return if the key is not found in the map
   @return value at key in map, else the default value
   */
  @SuppressWarnings("SameParameterValue")
  private Boolean readBool(Map<String, String> map, String key, Boolean orElse) {
    if (!map.containsKey(key)) return orElse;
    try {
      return Boolean.valueOf(map.get(key));

    } catch (NumberFormatException e) {
      LOG.warn("{} has non-int value: {}", key, map.get(key));
      return orElse;
    }
  }

  /**
   Get a string value from the given map, or if the key isn't in the map, return the default value

   @param map    in which to search for a key
   @param key    to search for
   @param orElse to return if the key is not found in the map
   @return value at key in map, else the default value
   */
  private String readStr(Map<String, String> map, String key, String orElse) {
    if (!map.containsKey(key)) return orElse;
    return map.get(key);
  }

  /**
   @return the access token domain
   */
  public String getAccessTokenDomain() {
    return accessTokenDomain;
  }

  /**
   @return the access log filename, e.g. "/var/log/my-app/access.log"
   */
  public String getAccessLogFilename() {
    return accessLogFilename;
  }

  /**
   @return the access token max age seconds
   */
  public int getAccessTokenMaxAgeSeconds() {
    return accessTokenMaxAgeSeconds;
  }

  /**
   @return the access token name
   */
  public String getAccessTokenName() {
    return accessTokenName;
  }

  /**
   @return the access token path
   */
  public String getAccessTokenPath() {
    return accessTokenPath;
  }

  /**
   @return the api unauthorized redirect path
   */
  public String getApiUnauthorizedRedirectPath() {
    return apiUnauthorizedRedirectPath;
  }

  /**
   @return the api welcome redirect path
   */
  public String getApiWelcomeRedirectPath() {
    return apiWelcomeRedirectPath;
  }

  /**
   @return the application base URL
   */
  public String getAppBaseUrl() {
    return appBaseURL;
  }

  /**
   @return the application name
   */
  public String getAppName() {
    return appName;
  }

  /**
   Set the app name

   @param name to set
   */
  public void setAppName(String name) {
    this.appName = name;
  }

  /**
   @return application port
   */
  public int getAppPort() {
    return appPort;
  }

  /**
   @return the audio base URL
   */
  public String getAudioBaseUrl() {
    return audioBaseURL;
  }

  /**
   @return the audio cache file prefix
   */
  public String getAudioCacheFilePrefix() {
    return audioCacheFilePrefix;
  }

  /**
   @return the audio file Bucket
   */
  public String getAudioFileBucket() {
    return audioFileBucket;
  }

  /**
   @return the audio upload URL
   */
  public String getAudioUploadURL() {
    return audioUploadURL;
  }

  /**
   @return the AWS access key ID
   */
  public String getAwsAccessKeyID() {
    return awsAccessKeyID;
  }

  /**
   @return the AWS default region, e.g. "us-east-1"
   */
  public String getAwsDefaultRegion() {
    return awsDefaultRegion;
  }

  /**
   @return the aws file upload ACL
   */
  public String getAwsFileUploadACL() {
    return awsFileUploadACL;
  }

  /**
   @return the aws s3 retry limit
   */
  public int getAwsS3retryLimit() {
    return awsS3retryLimit;
  }

  /**
   @return the AWS secret key
   */
  public String getAwsSecretKey() {
    return awsSecretKey;
  }

  /**
   @return aws secret name
   */
  public String getAwsSecretName() {
    return awsSecretName;
  }

  /**
   @return the AWS SNS topic ARN
   */
  public String getAwsSnsTopicArn() {
    return awsSnsTopicArn;
  }

  /**
   @return the aws upload expire minutes
   */
  public int getAwsUploadExpireMinutes() {
    return awsUploadExpireMinutes;
  }

  /**
   @return chain start in future seconds
   */
  public int getChainStartInFutureSeconds() {
    return chainStartInFutureSeconds;
  }

  /**
   @return the fabrication preview length max hours
   */
  public int getFabricationPreviewLengthMaxHours() {
    return fabricationPreviewLengthMaxHours;
  }

  /**
   @return the threshold (in seconds) behind which a chain will be considered stale during fabrication
   */
  public int getFabricationChainThresholdFabricatedBehindSeconds() {
    return fabricationChainThresholdFabricatedBehindSeconds;
  }

  /**
   @return GCP project id
   */
  public String getGcpProjectId() {
    return gcpProjectId;
  }

  /**
   @return GCP secret id
   */
  public String getGcpSecretId() {
    return gcpSecretId;
  }

  /**
   @return the Google client ID
   */
  public String getGoogleClientID() {
    return googleClientID;
  }

  /**
   @return the Google client secret
   */
  public String getGoogleClientSecret() {
    return googleClientSecret;
  }

  /**
   @return the application hostname, e.g. "localhost"
   */
  public String getHostname() {
    return hostname;
  }

  /**
   @return the http client pool max # connections total
   */
  public int getHttpClientPoolMaxTotal() {
    return httpClientPoolMaxTotal;
  }

  /**
   @return the http client pool max # connections per route
   */
  public int getHttpClientPoolMaxPerRoute() {
    return httpClientPoolMaxPerRoute;
  }

  /**
   @return the Hub internal token name
   */
  public String getIngestTokenName() {
    return ingestTokenName;
  }

  /**
   @return ingest URL
   */
  public String getIngestURL() {
    return ingestURL;
  }

  /**
   @return the Hub internal token
   */
  public String getIngestTokenValue() {
    return ingestTokenValue;
  }

  /**
   @return the environment, e.g. "dev" "stage" or "prod"
   */
  public String getPlatformEnvironment() {
    return platformEnvironment;
  }

  /**
   @return the postgres database
   */
  public String getPostgresDatabase() {
    return postgresDatabase;
  }

  /**
   @return the postgres host
   */
  public String getPostgresHost() {
    return postgresHost;
  }

  /**
   @return postgres schemas
   */
  public String getPostgresSchemas() {
    return postgresSchemas;
  }

  /**
   @return postgres pool size max
   */
  public int getPostgresPoolSizeMax() {
    return postgresPoolSizeMax;
  }

  /**
   @return the postgres port
   */
  public Integer getPostgresPort() {
    return postgresPort;
  }

  /**
   @return the postgres user
   */
  public String getPostgresUser() {
    return postgresUser;
  }

  /**
   @return the postgres pass
   */
  public String getPostgresPass() {
    return postgresPass;
  }

  /**
   @return the playback expire seconds
   */
  public int getPlaybackExpireSeconds() {
    return playbackExpireSeconds;
  }

  /**
   @return the Player base URL
   */
  public String getPlayerBaseUrl() {
    return playerBaseURL;
  }

  /**
   @return the Redis host
   */
  public String getRedisHost() {
    return redisHost;
  }

  /**
   @return the Redis port
   */
  public int getRedisPort() {
    return redisPort;
  }

  /**
   @return the redis session namespace
   */
  public String getRedisSessionNamespace() {
    return redisSessionNamespace;
  }

  /**
   @return the segment compute time frames per beat
   */
  public int getSegmentComputeTimeFramesPerBeat() {
    return segmentComputeTimeFramesPerBeat;
  }

  /**
   @return the segment compute time resolution hz
   */
  public int getSegmentComputeTimeResolutionHz() {
    return segmentComputeTimeResolutionHz;
  }

  /**
   @return the segment base URL
   */
  public String getShipBaseUrl() {
    return shipBaseUrl;
  }

  /**
   @return the ship MPEG2 TS bitrate
   */
  public int getShipBitrateHigh() {
    return shipBitrateHigh;
  }

  /**
   @return the segment file bucket
   */
  public String getShipBucket() {
    return shipBucket;
  }

  /**
   @return chain json max-age seconds for cache control http header
   */
  public int getShipChainJsonMaxAgeSeconds() {
    return shipChainJsonMaxAgeSeconds;
  }

  /**
   @return the ship ffmpeg audio compressor
   */
  public String getShipChunkAudioEncoder() {
    return shipChunkAudioEncoder;
  }

  /**
   @return the ship audio segment MPEG-TS content-type
   */
  public String getShipChunkContentType() {
    return shipChunkContentType;
  }

  /**
   @return the ship media segment target duration
   */
  public int getShipChunkTargetDuration() {
    return shipChunkTargetDuration;
  }

  /**
   @return ship ffmpeg verbosity
   */
  public String getShipFFmpegVerbosity() {
    return shipFFmpegVerbosity;
  }

  /**
   @return the offset before now (now minus N media segments) where ship should begin on startup
   */
  public int getShipInitialMediaSequenceNumberOffset() {
    return shipInitialMediaSequenceNumberOffset;
  }

  /**
   Ship broadcast via HTTP Live Streaming #179453189

   @return the ship key for this ship instance
   */
  public String getShipKey() {
    return shipKey;
  }

  /**
   @return the ship .m3u8 playlist content-type
   */
  public String getShipM3u8ContentType() {
    return shipM3u8ContentType;
  }

  /**
   @return the ship .m3u8 playlist max-age seconds for cache control http header
   */
  public int getShipM3u8MaxAgeSeconds() {
    return shipM3u8MaxAgeSeconds;
  }

  /**
   @return the work print cycle seconds
   */
  public int getShipMixCycleSeconds() {
    return shipMixCycleSeconds;
  }

  /**
   @return ship mode
   */
  public String getShipMode() {
    return shipMode;
  }

  /**
   @return the ship reload seconds
   */
  public int getShipLoadCycleSeconds() {
    return shipLoadCycleSeconds;
  }

  /**
   @return # of seconds ahead of "now" we will endeavor to ship media segments up to
   */
  public int getShipPlaylistAheadSeconds() {
    return shipPlaylistAheadSeconds;
  }

  /**
   @return # of seconds back from "now" before we delete a media segment from the playlist
   */
  public int getShipPlaylistBackSeconds() {
    return shipPlaylistBackSeconds;
  }

  /**
   @return # of seconds after which Ship will ignore future segments
   */
  public int getShipSegmentLoadAheadSeconds() {
    return shipSegmentLoadAheadSeconds;
  }

  /**
   @return ship segment load timeout seconds
   */
  public int getShipSegmentLoadTimeoutSeconds() {
    return shipSegmentLoadTimeoutSeconds;
  }

  /**
   @return stream base URL
   */
  public String getStreamBaseUrl() {
    return streamBaseURL;
  }

  /**
   @return stream base URL
   */
  public String getStreamBucket() {
    return streamBucket;
  }

  /**
   @return true if telemetry is enabled
   */
  public boolean isTelemetryEnabled() {
    return telemetryEnabled;
  }

  /**
   @return temp file path prefix
   */
  public String getTempFilePathPrefix() {
    return tempFilePathPrefix;
  }

  /**
   @return erase segments older than seconds
   */
  public int getWorkEraseSegmentsOlderThanSeconds() {
    return workEraseSegmentsOlderThanSeconds;
  }

  /**
   @return the work cycle millis
   */
  public int getWorkCycleMillis() {
    return workCycleMillis;
  }

  /**
   @return the threshold (in seconds) after which, if we haven't seen the work cycle happen, we're declaring this whole process dead
   */
  public int getWorkHealthCycleStalenessThresholdSeconds() {
    return workHealthCycleStalenessThresholdSeconds;
  }

  /**
   @return the work ingest cycle seconds
   */
  public int getWorkIngestCycleSeconds() {
    return workIngestCycleSeconds;
  }

  /**
   @return the work janitor cycle seconds
   */
  public int getWorkJanitorCycleSeconds() {
    return workJanitorCycleSeconds;
  }

  /**
   @return true if the work janitor is enabled
   */
  public boolean isWorkJanitorEnabled() {
    return workJanitorEnabled;
  }

  /**
   @return the work lab hub lab poll seconds
   */
  public int getWorkLabHubLabPollSeconds() {
    return workLabHubLabPollSeconds;
  }

  /**
   @return the work medic cycle seconds
   */
  public int getWorkMedicCycleSeconds() {
    return workMedicCycleSeconds;
  }

  /**
   @return true if the work medic is enabled
   */
  public boolean isWorkMedicEnabled() {
    return workMedicEnabled;
  }

  /**
   @return the work publish cycle seconds
   */
  public int getWorkPublishCycleSeconds() {
    return workPublishCycleSeconds;
  }

  /**
   @return the work telemetry cycle seconds
   */
  public int getWorkTelemetryCycleSeconds() {
    return workTelemetryCycleSeconds;
  }

  /**
   @return the work rehydrate fabricated ahead threshold
   */
  public int getWorkRehydrateFabricatedAheadThreshold() {
    return workRehydrateFabricatedAheadThreshold;
  }

  /**
   @return true if work rehydration is enabled
   */
  public boolean isWorkRehydrationEnabled() {
    return workRehydrationEnabled;
  }
}
