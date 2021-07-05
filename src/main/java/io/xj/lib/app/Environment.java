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
  private static final String EMPTY = "";
  private final String accessLogFilename;
  private final String accessTokenName;
  private final String accessTokenDomain;
  private final String accessTokenPath;
  private final int accessTokenMaxAgeSeconds;
  private final String appBaseURL;
  private final String audioBaseURL;
  private final String audioCacheFilePrefix;
  private final String audioFileBucket;
  private final String audioUploadURL;
  private final String awsFileUploadACL;
  private final String awsAccessKeyID;
  private final String awsDefaultRegion;
  private final int awsS3retryLimit;
  private final String awsSecretKey;
  private final String awsSecretName;
  private final String awsSnsTopicArn;
  private final int awsUploadExpireMinutes;
  private final String chainBootstrapJson;
  private final String datadogStatsdHostname;
  private final int datadogStatsdPort;
  private final String datadogStatsdPrefix;
  private final String platformEnvironment;
  private final String googleClientID;
  private final String googleClientSecret;
  private final String hostname;
  private final String ingestTokenName;
  private final String ingestTokenValue;
  private final String ingestURL;
  private final String playerBaseURL;
  private final String segmentBaseURL;
  private final String tempFilePathPrefix;
  private final String segmentFileBucket;

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
    pairs.forEach(vars::put);
    if (0 < pairs.size())
      LOG.info("Augmented system environment with {} secrets having keys {}", pairs.size(), CSV.join(pairs.keySet()));
    else
      LOG.warn("Did not parse any secrets with which to augment system environment.");
    return from(vars);
  }

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
    accessLogFilename = getStr(vars, "ACCESS_LOG_FILENAME", "/tmp/access.log");
    accessTokenName = getStr(vars, "ACCESS_TOKEN_NAME", "access_token");
    accessTokenDomain = getStr(vars, "ACCESS_TOKEN_DOMAIN", "");
    accessTokenPath = getStr(vars, "ACCESS_TOKEN_PATH", "/");
    accessTokenMaxAgeSeconds = getInt(vars, "ACCESS_TOKEN_MAX_AGE_SECONDS", 2419200);
    audioCacheFilePrefix = getStr(vars, "AUDIO_CACHE_FILE_PREFIX", "/tmp/");
    appBaseURL = getStr(vars, "APP_BASE_URL", "http://localhost/");
    audioBaseURL = getStr(vars, "AUDIO_BASE_URL", "https://audio.dev.xj.io/");
    audioFileBucket = getStr(vars, "AUDIO_FILE_BUCKET", "xj-dev-audio");
    audioUploadURL = getStr(vars, "AUDIO_UPLOAD_URL", "https://xj-dev-audio.s3.amazonaws.com/");
    awsAccessKeyID = getStr(vars, "AWS_ACCESS_KEY_ID", EMPTY);
    awsDefaultRegion = getStr(vars, "AWS_DEFAULT_REGION", EMPTY);
    awsSecretKey = getStr(vars, "AWS_SECRET_KEY", EMPTY);
    awsSecretName = getStr(vars, "AWS_SECRET_NAME", EMPTY);
    awsSnsTopicArn = getStr(vars, "AWS_SNS_TOPIC_ARN", EMPTY);
    awsUploadExpireMinutes = getInt(vars, "AWS_UPLOAD_EXPIRE_MINUTES", 60);
    awsFileUploadACL = getStr(vars, "AWS_FILE_UPLOAD_ACL", "bucket-owner-full-control");
    awsS3retryLimit = getInt(vars, "AWS_S3_RETRY_LIMIT", 10);
    chainBootstrapJson = getStr(vars, "CHAIN_BOOTSTRAP_JSON", EMPTY);
    datadogStatsdHostname = getStr(vars, "DATADOG_STATSD_HOSTNAME", "localhost");
    datadogStatsdPort = getInt(vars, "DATADOG_STATSD_PORT", 8125);
    datadogStatsdPrefix = getStr(vars, "DATADOG_STATSD_PREFIX", "xj");
    platformEnvironment = getStr(vars, "ENVIRONMENT", "dev");
    googleClientID = getStr(vars, "GOOGLE_CLIENT_ID", EMPTY);
    googleClientSecret = getStr(vars, "GOOGLE_CLIENT_SECRET", EMPTY);
    hostname = getStr(vars, "HOSTNAME", "localhost");
    ingestTokenValue = getStr(vars, "INGEST_TOKEN_VALUE", EMPTY);
    ingestTokenName = getStr(vars, "INGEST_TOKEN_NAME", "access_token");
    ingestURL = getStr(vars, "INGEST_URL", "http://localhost/");
    playerBaseURL = getStr(vars, "PLAYER_BASE_URL", "http://localhost/");
    segmentBaseURL = getStr(vars, "SEGMENT_BASE_URL", "https://ship.dev.xj.io/");
    segmentFileBucket = getStr(vars, "SEGMENT_FILE_BUCKET", "xj-dev-ship");
    tempFilePathPrefix = getStr(vars, "TEMP_FILE_PATH_PREFIX", "/tmp/");
  }

  /**
   Get a integer value from the given map, or if the key isn't in the map, return the default value

   @param map    in which to search for a key
   @param key    to search for
   @param orElse to return if the key is not found in the map
   @return value at key in map, else the default value
   */
  @SuppressWarnings("SameParameterValue")
  private Integer getInt(Map<String, String> map, String key, Integer orElse) {
    if (!map.containsKey(key)) return orElse;
    try {
      return Integer.valueOf(map.get(key));

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
  private String getStr(Map<String, String> map, String key, String orElse) {
    if (!map.containsKey(key)) return orElse;
    return map.get(key);
  }

  /**
   @return the application base URL
   */
  public String getAppBaseURL() {
    return appBaseURL;
  }

  /**
   @return the application hostname, e.g. "localhost"
   */
  public String getHostname() {
    return hostname;
  }

  /**
   @return the audio base URL
   */
  public String getAudioBaseURL() {
    return audioBaseURL;
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
   @return the AWS secret key
   */
  public String getAwsSecretKey() {
    return awsSecretKey;
  }

  /**
   @return the AWS SNS topic ARN
   */
  public String getAwsSnsTopicArn() {
    return awsSnsTopicArn;
  }

  /**
   @return the AWS default region, e.g. "us-east-1"
   */
  public String getAwsDefaultRegion() {
    return awsDefaultRegion;
  }

  /**
   @return the environment, e.g. "dev" "stage" or "prod"
   */
  public String getPlatformEnvironment() {
    return platformEnvironment;
  }

  /**
   @return the access log filename, e.g. "/var/log/my-app/access.log"
   */
  public String getAccessLogFilename() {
    return accessLogFilename;
  }

  /**
   @return the Datadog statsd prefix
   */
  public String getDatadogStatsdPrefix() {
    return datadogStatsdPrefix;
  }

  /**
   @return the Datadog statsd hostname
   */
  public String getDatadogStatsdHostname() {
    return datadogStatsdHostname;
  }

  /**
   @return the Datadog statsd port
   */
  public int getDatadogStatsdPort() {
    return datadogStatsdPort;
  }

  /**
   @return the Hub internal token value
   */
  public String getIngestTokenValue() {
    return ingestTokenValue;
  }

  /**
   @return the Hub internal token name
   */
  public String getIngestTokenName() {
    return ingestTokenName;
  }

  /**
   @return the Player base URL
   */
  public String getPlayerBaseURL() {
    return playerBaseURL;
  }

  /**
   @return the segment base URL
   */
  public String getSegmentBaseURL() {
    return segmentBaseURL;
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
   @return the aws upload expire minutes
   */
  public int getAwsUploadExpireMinutes() {
    return awsUploadExpireMinutes;
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
   @return the access token name
   */
  public String getAccessTokenName() {
    return accessTokenName;
  }

  /**
   @return the access token domain
   */
  public String getAccessTokenDomain() {
    return accessTokenDomain;
  }

  /**
   @return the access token path
   */
  public String getAccessTokenPath() {
    return accessTokenPath;
  }

  /**
   @return the access token max age seconds
   */
  public int getAccessTokenMaxAgeSeconds() {
    return accessTokenMaxAgeSeconds;
  }

  /**
   @return aws secret name
   */
  public String getAwsSecretName() {
    return awsSecretName;
  }

  /**
   @return the ingest URL
   */
  public String getIngestURL() {
    return ingestURL;
  }

  /**
   @return the temp file path prefix
   */
  public String getTempFilePathPrefix() {
    return tempFilePathPrefix;
  }

  /**
   @return the audio cache file prefix
   */
  public String getAudioCacheFilePrefix() {
    return audioCacheFilePrefix;
  }

  /**
   @return the chain bootstrap json
   */
  public String getChainBootstrapJson() {
    return chainBootstrapJson;
  }

  /**
   @return the segment file bucket
   */
  public String getSegmentFileBucket() {
    return segmentFileBucket;
  }
}
