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
 * All secrets from environment variables
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
  private final String bootstrapShipKey;
  private final String bootstrapShipSource;
  private final String bootstrapShipTitle;
  private final String bootstrapTemplateId;
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
  private final String shipM3u8ContentType;
  private final String streamBaseURL;
  private final String streamBucket;
  private final String telemetryNamespace;
  private final String tempFilePathPrefix;
  private final boolean workChainManagementEnabled;
  private final boolean workJanitorEnabled;
  private final boolean workMedicEnabled;
  private final int accessTokenMaxAgeSeconds;
  private final int appPort;
  private final int awsS3retryLimit;
  private final int awsUploadExpireMinutes;
  private final int chainStartInFutureSeconds;
  private final int fabricationPreviewLengthMaxHours;
  private final int fabricationPreviewShipKeyLength;
  private final int fabricationReviveChainFabricatedBehindSeconds;
  private final int fabricationReviveChainProductionGraceSeconds;
  private final int playbackExpireSeconds;
  private final int postgresPoolSizeMax;
  private final int postgresPort;
  private final int redisPort;
  private final int segmentComputeTimeFramesPerBeat;
  private final int segmentComputeTimeResolutionHz;
  private final int shipAheadChunks;
  private final int shipBitrateHigh;
  private final int shipChunkPrintTimeoutSeconds;
  private final int shipChunkSeconds;
  private final int shipReloadSeconds;
  private final int shipSegmentLoadTimeoutSeconds;
  private final int workBufferAheadSeconds;
  private final int workBufferBeforeSeconds;
  private final int workBufferPreviewSeconds;
  private final int workBufferProductionSeconds;
  private final int workCycleMillis;
  private final int workEraseSegmentsOlderThanSeconds;
  private final int workHealthCycleStalenessThresholdSeconds;
  private final int workIngestCycleSeconds;
  private final int workJanitorCycleSeconds;
  private final int workLabHubLabPollSeconds;
  private final int workMedicCycleSeconds;
  private final int workPublishCycleSeconds;
  private final int workRehydrateFabricatedAheadThreshold;
  private final int workShipFabricatedAheadThresholdSeconds;

  /**
   * Zero-argument construction defaults to system environment
   */
  @Inject
  public Environment() {
    this(System.getenv());
  }

  /**
   * Get the environment from a specific set of variables
   *
   * @param vars from which to get environment
   * @return environment from variables
   */
  public static Environment from(Map<String, String> vars) {
    return new Environment(vars);
  }

  /**
   * Get the environment from system environment variables
   *
   * @return system environment
   */
  public static Environment fromSystem() {
    return from(System.getenv());
  }

  /**
   * Get the default environment
   *
   * @return environment with default values
   */
  public static Environment getDefault() {
    return from(ImmutableMap.of());
  }

  /**
   * Augment the system environment variables with a source text body of key=value lines
   *
   * @param secretKeyValueLines to parse for key=value lines
   * @return system environment augmented with source
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
   * Manual construction
   *
   * @param vars to build environment from
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
    appPort = readInt(vars, "APP_PORT", 3000);
    audioBaseURL = readStr(vars, "AUDIO_BASE_URL", "https://audio.dev.xj.io/");
    audioCacheFilePrefix = readStr(vars, "AUDIO_CACHE_FILE_PREFIX", "/tmp/");
    audioFileBucket = readStr(vars, "AUDIO_FILE_BUCKET", "xj-dev-audio");
    audioUploadURL = readStr(vars, "AUDIO_UPLOAD_URL", "https://xj-dev-audio.s3.amazonaws.com/");
    bootstrapShipKey = readStr(vars, "BOOTSTRAP_SHIP_KEY", EMPTY);
    bootstrapShipTitle = readStr(vars, "BOOTSTRAP_SHIP_TITLE", EMPTY);
    bootstrapShipSource = readStr(vars, "BOOTSTRAP_SHIP_SOURCE", EMPTY);
    bootstrapTemplateId = readStr(vars, "BOOTSTRAP_TEMPLATE_ID", EMPTY);
    chainStartInFutureSeconds = readInt(vars, "CHAIN_START_IN_FUTURE_SECONDS", 0);
    fabricationPreviewLengthMaxHours = readInt(vars, "FABRICATION_PREVIEW_LENGTH_MAX_HOURS", 8);
    fabricationPreviewShipKeyLength = readInt(vars, "FABRICATION_PREVIEW_SHIP_KEY_LENGTH", 20);
    fabricationReviveChainFabricatedBehindSeconds = readInt(vars, "FABRICATION_REVIVE_CHAIN_FABRICATED_BEHIND_SECONDS", 15);
    fabricationReviveChainProductionGraceSeconds = readInt(vars, "FABRICATION_REVIVE_CHAIN_PRODUCTION_GRACE_SECONDS", 15);
    hostname = readStr(vars, "HOSTNAME", "localhost");
    ingestTokenName = readStr(vars, "INGEST_TOKEN_NAME", "access_token");
    ingestTokenValue = readStr(vars, "INGEST_TOKEN_VALUE", EMPTY);
    ingestURL = readStr(vars, "INGEST_URL", "http://localhost/");
    platformEnvironment = readStr(vars, "ENVIRONMENT", "dev");
    playbackExpireSeconds = readInt(vars, "PLAYBACK_EXPIRE_SECONDS", SECONDS_PER_HOUR * 8);
    playerBaseURL = readStr(vars, "PLAYER_BASE_URL", "http://localhost/");
    segmentComputeTimeFramesPerBeat = readInt(vars, "SEGMENT_COMPUTE_TIME_FRAMES_PER_BEAT", 64);
    segmentComputeTimeResolutionHz = readInt(vars, "SEGMENT_COMPUTE_TIME_RESOLUTION_HZ", 1000000);
    shipAheadChunks = readInt(vars, "SHIP_AHEAD_CHUNKS", 6);
    shipBaseUrl = readStr(vars, "SHIP_BASE_URL", "https://ship.dev.xj.io/");
    shipBucket = readStr(vars, "SHIP_BUCKET", "xj-dev-ship");
    shipChunkPrintTimeoutSeconds = readInt(vars, "SHIP_CHUNK_PRINT_SECONDS", 5);
    shipChunkSeconds = readInt(vars, "SHIP_CHUNK_SECONDS", 10);
    shipM3u8ContentType = readStr(vars, "SHIP_M3U8_CONTENT_TYPE", "application/x-mpegURL");
    shipBitrateHigh = readInt(vars, "SHIP_BITRATE_HIGH", 128000);
    shipReloadSeconds = readInt(vars, "SHIP_RELOAD_SECONDS", 15);
    shipSegmentLoadTimeoutSeconds = readInt(vars, "SHIP_SEGMENT_LOAD_TIMEOUT_SECONDS", 5);
    streamBaseURL = readStr(vars, "STREAM_BASE_URL", "https://stream.dev.xj.io/");
    streamBucket = readStr(vars, "STREAM_BUCKET", "xj-dev-stream");
    telemetryNamespace = readStr(vars, "TELEMETRY_NAMESPACE", "Lab/Hub");
    tempFilePathPrefix = readStr(vars, "TEMP_FILE_PATH_PREFIX", "/tmp/");
    workBufferAheadSeconds = readInt(vars, "WORK_BUFFER_AHEAD_SECONDS", 90);
    workBufferBeforeSeconds = readInt(vars, "WORK_BUFFER_BEFORE_SECONDS", 5);
    workBufferPreviewSeconds = readInt(vars, "WORK_BUFFER_PREVIEW_SECONDS", 90);
    workBufferProductionSeconds = readInt(vars, "WORK_BUFFER_PRODUCTION_SECONDS", 180);
    workChainManagementEnabled = readBool(vars, "WORK_CHAIN_MANAGEMENT_ENABLED", true);
    workCycleMillis = readInt(vars, "WORK_CYCLE_MILLIS", 1200);
    workEraseSegmentsOlderThanSeconds = readInt(vars, "WORK_ERASE_SEGMENTS_OLDER_THAN_SECONDS", 30);
    workHealthCycleStalenessThresholdSeconds = readInt(vars, "WORK_HEALTH_CYCLE_STALENESS_THRESHOLD_SECONDS", 30);
    workIngestCycleSeconds = readInt(vars, "WORK_INGEST_CYCLE_SECONDS", 60);
    workJanitorCycleSeconds = readInt(vars, "WORK_JANITOR_CYCLE_SECONDS", 90);
    workJanitorEnabled = readBool(vars, "WORK_JANITOR_ENABLED", true);
    workLabHubLabPollSeconds = readInt(vars, "WORK_LAB_HUB_LAB_POLL_SECONDS", 10);
    workMedicCycleSeconds = readInt(vars, "WORK_MEDIC_CYCLE_SECONDS", 30);
    workMedicEnabled = readBool(vars, "WORK_MEDIC_ENABLED", true);
    workPublishCycleSeconds = readInt(vars, "WORK_PUBLISH_CYCLE_SECONDS", 10);
    workRehydrateFabricatedAheadThreshold = readInt(vars, "WORK_REHYDRATE_FABRICATED_AHEAD_THRESHOLD", 60);
    workShipFabricatedAheadThresholdSeconds = readInt(vars, "WORK_SHIP_FABRICATED_AHEAD_THRESHOLD_SECONDS", 60);

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
   * Get an integer value from the given map, or if the key isn't in the map, return the default value
   *
   * @param map    in which to search for a key
   * @param key    to search for
   * @param orElse to return if the key is not found in the map
   * @return value at key in map, else the default value
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
   * Get a boolean value from the given map, or if the key isn't in the map, return the default value
   *
   * @param map    in which to search for a key
   * @param key    to search for
   * @param orElse to return if the key is not found in the map
   * @return value at key in map, else the default value
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
   * Get a string value from the given map, or if the key isn't in the map, return the default value
   *
   * @param map    in which to search for a key
   * @param key    to search for
   * @param orElse to return if the key is not found in the map
   * @return value at key in map, else the default value
   */
  private String readStr(Map<String, String> map, String key, String orElse) {
    if (!map.containsKey(key)) return orElse;
    return map.get(key);
  }

  /**
   * @return the access token domain
   */
  public String getAccessTokenDomain() {
    return accessTokenDomain;
  }

  /**
   * @return the access log filename, e.g. "/var/log/my-app/access.log"
   */
  public String getAccessLogFilename() {
    return accessLogFilename;
  }

  /**
   * @return the access token max age seconds
   */
  public int getAccessTokenMaxAgeSeconds() {
    return accessTokenMaxAgeSeconds;
  }

  /**
   * @return the access token name
   */
  public String getAccessTokenName() {
    return accessTokenName;
  }

  /**
   * @return the access token path
   */
  public String getAccessTokenPath() {
    return accessTokenPath;
  }

  /**
   * @return the api unauthorized redirect path
   */
  public String getApiUnauthorizedRedirectPath() {
    return apiUnauthorizedRedirectPath;
  }

  /**
   * @return the api welcome redirect path
   */
  public String getApiWelcomeRedirectPath() {
    return apiWelcomeRedirectPath;
  }

  /**
   * @return the application base URL
   */
  public String getAppBaseUrl() {
    return appBaseURL;
  }

  /**
   * @return application port
   */
  public int getAppPort() {
    return appPort;
  }

  /**
   * @return the audio base URL
   */
  public String getAudioBaseUrl() {
    return audioBaseURL;
  }

  /**
   * @return the audio cache file prefix
   */
  public String getAudioCacheFilePrefix() {
    return audioCacheFilePrefix;
  }

  /**
   * @return the audio file Bucket
   */
  public String getAudioFileBucket() {
    return audioFileBucket;
  }

  /**
   * @return the audio upload URL
   */
  public String getAudioUploadURL() {
    return audioUploadURL;
  }

  /**
   * @return the AWS access key ID
   */
  public String getAwsAccessKeyID() {
    return awsAccessKeyID;
  }

  /**
   * @return the AWS default region, e.g. "us-east-1"
   */
  public String getAwsDefaultRegion() {
    return awsDefaultRegion;
  }

  /**
   * @return the aws file upload ACL
   */
  public String getAwsFileUploadACL() {
    return awsFileUploadACL;
  }

  /**
   * @return the aws s3 retry limit
   */
  public int getAwsS3retryLimit() {
    return awsS3retryLimit;
  }

  /**
   * @return the AWS secret key
   */
  public String getAwsSecretKey() {
    return awsSecretKey;
  }

  /**
   * @return aws secret name
   */
  public String getAwsSecretName() {
    return awsSecretName;
  }

  /**
   * @return the AWS SNS topic ARN
   */
  public String getAwsSnsTopicArn() {
    return awsSnsTopicArn;
  }

  /**
   * @return the aws upload expire minutes
   */
  public int getAwsUploadExpireMinutes() {
    return awsUploadExpireMinutes;
  }

  /**
   * Ship broadcast via HTTP Live Streaming #179453189
   *
   * @return the bootstrap ship key for this ship instance
   */
  public String getBootstrapShipKey() {
    return bootstrapShipKey;
  }

  /**
   * @return the bootstrap ship title
   */
  public String getBootstrapShipTitle() {
    return bootstrapShipTitle;
  }

  /**
   * @return the bootstrap ship source
   */
  public String getBootstrapShipSource() {
    return bootstrapShipSource;
  }

  /**
   * @return the bootstrap template id
   */
  public String getBootstrapTemplateId() {
    return bootstrapTemplateId;
  }

  /**
   * @return chain start in future seconds
   */
  public int getChainStartInFutureSeconds() {
    return chainStartInFutureSeconds;
  }

  /**
   * @return the fabrication preview length max hours
   */
  public int getFabricationPreviewLengthMaxHours() {
    return fabricationPreviewLengthMaxHours;
  }

  /**
   * @return the fabrication preview ship key length
   */
  public int getFabricationPreviewShipKeyLength() {
    return fabricationPreviewShipKeyLength;
  }

  /**
   * @return the fabrication revive chain fabricated behind seconds
   */
  public int getFabricationReviveChainFabricatedBehindSeconds() {
    return fabricationReviveChainFabricatedBehindSeconds;
  }

  /**
   * @return the fabrication revive chain production grace seconds
   */
  public int getFabricationReviveChainProductionGraceSeconds() {
    return fabricationReviveChainProductionGraceSeconds;
  }

  /**
   * @return the application hostname, e.g. "localhost"
   */
  public String getHostname() {
    return hostname;
  }

  /**
   * @return the Hub internal token name
   */
  public String getIngestTokenName() {
    return ingestTokenName;
  }

  /**
   * @return ingest URL
   */
  public String getIngestURL() {
    return ingestURL;
  }

  /**
   * @return the Hub internal token
   */
  public String getIngestTokenValue() {
    return ingestTokenValue;
  }

  /**
   * @return the Google client ID
   */
  public String getGoogleClientID() {
    return googleClientID;
  }

  /**
   * @return the Google client secret
   */
  public String getGoogleClientSecret() {
    return googleClientSecret;
  }

  /**
   * @return the environment, e.g. "dev" "stage" or "prod"
   */
  public String getPlatformEnvironment() {
    return platformEnvironment;
  }

  /**
   * @return the postgres database
   */
  public String getPostgresDatabase() {
    return postgresDatabase;
  }

  /**
   * @return the postgres host
   */
  public String getPostgresHost() {
    return postgresHost;
  }

  /**
   * @return postgres schemas
   */
  public String getPostgresSchemas() {
    return postgresSchemas;
  }

  /**
   * @return postgres pool size max
   */
  public int getPostgresPoolSizeMax() {
    return postgresPoolSizeMax;
  }

  /**
   * @return the postgres port
   */
  public Integer getPostgresPort() {
    return postgresPort;
  }

  /**
   * @return the postgres user
   */
  public String getPostgresUser() {
    return postgresUser;
  }

  /**
   * @return the postgres pass
   */
  public String getPostgresPass() {
    return postgresPass;
  }

  /**
   * @return the playback expire seconds
   */
  public int getPlaybackExpireSeconds() {
    return playbackExpireSeconds;
  }

  /**
   * @return the Player base URL
   */
  public String getPlayerBaseUrl() {
    return playerBaseURL;
  }

  /**
   * @return the Redis host
   */
  public String getRedisHost() {
    return redisHost;
  }

  /**
   * @return the Redis port
   */
  public int getRedisPort() {
    return redisPort;
  }

  /**
   * @return the redis session namespace
   */
  public String getRedisSessionNamespace() {
    return redisSessionNamespace;
  }

  /**
   * @return the segment compute time frames per beat
   */
  public int getSegmentComputeTimeFramesPerBeat() {
    return segmentComputeTimeFramesPerBeat;
  }

  /**
   * @return the segment compute time resolution hz
   */
  public int getSegmentComputeTimeResolutionHz() {
    return segmentComputeTimeResolutionHz;
  }

  /**
   * @return the ship ahead chunks
   */
  public int getShipAheadChunks() {
    return shipAheadChunks;
  }

  /**
   * @return the ship chunk seconds
   */
  public int getShipChunkSeconds() {
    return shipChunkSeconds;
  }

  /**
   * @return the ship .m3u8 playlist content-type
   */
  public String getShipM3u8ContentType() {
    return shipM3u8ContentType;
  }

  /**
   * @return the ship reload seconds
   */
  public int getShipReloadSeconds() {
    return shipReloadSeconds;
  }

  /**
   * @return the segment base URL
   */
  public String getShipBaseUrl() {
    return shipBaseUrl;
  }

  /**
   * @return the segment file bucket
   */
  public String getShipBucket() {
    return shipBucket;
  }

  /**
   * @return the ship chunk printer timeout seconds
   */
  public int getShipChunkPrintTimeoutSeconds() {
    return shipChunkPrintTimeoutSeconds;
  }

  /**
   * @return the ship MPEG2 TS bitrate
   */
  public int getShipBitrateHigh() {
    return shipBitrateHigh;
  }

  /**
   * @return ship segment load timeout seconds
   */
  public int getShipSegmentLoadTimeoutSeconds() {
    return shipSegmentLoadTimeoutSeconds;
  }

  /**
   * @return stream base URL
   */
  public String getStreamBaseUrl() {
    return streamBaseURL;
  }

  /**
   * @return stream base URL
   */
  public String getStreamBucket() {
    return streamBucket;
  }

  /**
   * @return the namespace
   */
  public String getTelemetryNamespace() {
    return telemetryNamespace;
  }

  /**
   * @return temp file path prefix
   */
  public String getTempFilePathPrefix() {
    return tempFilePathPrefix;
  }

  /**
   * @return ship fabricated ahead threshold seconds
   */
  public int getWorkShipFabricatedAheadThresholdSeconds() {
    return workShipFabricatedAheadThresholdSeconds;
  }

  /**
   * @return erase segments older than seconds
   */
  public int getWorkEraseSegmentsOlderThanSeconds() {
    return workEraseSegmentsOlderThanSeconds;
  }

  /**
   * @return the work buffer ahead seconds
   */
  public int getWorkBufferAheadSeconds() {
    return workBufferAheadSeconds;
  }

  /**
   * @return the work buffer before seconds
   */
  public int getWorkBufferBeforeSeconds() {
    return workBufferBeforeSeconds;
  }

  /**
   * @return the work buffer preview seconds
   */
  public int getWorkBufferPreviewSeconds() {
    return workBufferPreviewSeconds;
  }

  /**
   * @return the work buffer production seconds
   */
  public int getWorkBufferProductionSeconds() {
    return workBufferProductionSeconds;
  }

  /**
   * @return true if the work chain management enabled
   */
  public boolean getWorkChainManagementEnabled() {
    return workChainManagementEnabled;
  }

  /**
   * @return the work cycle millis
   */
  public int getWorkCycleMillis() {
    return workCycleMillis;
  }

  /**
   * @return the work health cycle staleness threshold seconds
   */
  public int getWorkHealthCycleStalenessThresholdSeconds() {
    return workHealthCycleStalenessThresholdSeconds;
  }

  /**
   * @return the work ingest cycle seconds
   */
  public int getWorkIngestCycleSeconds() {
    return workIngestCycleSeconds;
  }

  /**
   * @return the work janitor cycle seconds
   */
  public int getWorkJanitorCycleSeconds() {
    return workJanitorCycleSeconds;
  }

  /**
   * @return true if the work janitor is enabled
   */
  public boolean getWorkJanitorEnabled() {
    return workJanitorEnabled;
  }

  /**
   * @return the work lab hub lab poll seconds
   */
  public int getWorkLabHubLabPollSeconds() {
    return workLabHubLabPollSeconds;
  }

  /**
   * @return the work medic cycle seconds
   */
  public int getWorkMedicCycleSeconds() {
    return workMedicCycleSeconds;
  }

  /**
   * @return true if the work medic is enabled
   */
  public boolean getWorkMedicEnabled() {
    return workMedicEnabled;
  }

  /**
   * @return the work publish cycle seconds
   */
  public int getWorkPublishCycleSeconds() {
    return workPublishCycleSeconds;
  }

  /**
   * @return the work rehydrate fabricated ahead threshold
   */
  public int getWorkRehydrateFabricatedAheadThreshold() {
    return workRehydrateFabricatedAheadThreshold;
  }
}
