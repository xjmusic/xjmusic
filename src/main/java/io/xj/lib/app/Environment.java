package io.xj.lib.app;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Map;

/**
 All secrets from environment variables
 */
@Singleton
public class Environment {
  private static final String EMPTY = "";
  private final String appBaseURL;
  private final String audioBaseURL;
  private final String audioFileBucket;
  private final String audioUploadURL;
  private final String awsAccessKeyID;
  private final String awsSecretKey;
  private final String awsSnsTopicArn;
  private final String segmentBaseURL;
  private final String segmentFileBucket;
  private final String environment;
  private final String accessLogFilename;
  private final String hostname;
  private final String awsDefaultRegion;
  private final String datadogStatsdPrefix;
  private final String datadogStatsdHostname;
  private final int datadogStatsdPort;
  private final String hubBaseURL;
  private final String hubTokenName;
  private final String hubTokenValue;
  private final String playerBaseURL;
  private final String audioCacheFilePrefix;
  private final String tempFilePathPrefix;
  private final String chainBootstrapJsonPath;

  @Inject
  public Environment() {
    Map<String, String> env = System.getenv();
    accessLogFilename = getStr(env, "ACCESS_LOG_FILENAME", "/tmp/access.log");
    appBaseURL = getStr(env, "APP_BASE_URL", "http://localhost/");
    audioBaseURL = getStr(env, "AUDIO_BASE_URL", "https://audio.dev.xj.io/");
    audioCacheFilePrefix = getStr(env, "AUDIO_CACHE_FILE_PREFIX", "/tmp/");
    audioFileBucket = getStr(env, "AUDIO_FILE_BUCKET", "xj-dev-audio");
    audioUploadURL = getStr(env, "AUDIO_UPLOAD_URL", "https://xj-dev-audio.s3.amazonaws.com/");
    awsAccessKeyID = getStr(env, "AWS_ACCESS_KEY_ID", EMPTY);
    awsDefaultRegion = getStr(env, "AWS_DEFAULT_REGION", EMPTY);
    awsSecretKey = getStr(env, "AWS_SECRET_KEY", EMPTY);
    awsSnsTopicArn = getStr(env, "AWS_SNS_TOPIC_ARN", EMPTY);
    datadogStatsdHostname = getStr(env, "DATADOG_STATSD_HOSTNAME", "localhost");
    datadogStatsdPort = getInt(env, "DATADOG_STATSD_PORT", 8125);
    datadogStatsdPrefix = getStr(env, "DATADOG_STATSD_PREFIX", "xj");
    environment = getStr(env, "ENVIRONMENT", "dev");
    hostname = getStr(env, "HOSTNAME", "localhost");
    hubBaseURL = getStr(env, "HUB_BASE_URL", "http://localhost:3001/");
    hubTokenName = getStr(env, "HUB_TOKEN_NAME", "access_token");
    hubTokenValue = getStr(env, "HUB_TOKEN_VALUE", EMPTY);
    playerBaseURL = getStr(env, "PLAYER_BASE_URL", "http://localhost/");
    segmentBaseURL = getStr(env, "SEGMENT_BASE_URL", "https://ship.dev.xj.io/");
    segmentFileBucket = getStr(env, "SEGMENT_FILE_BUCKET", "xj-dev-ship");
    tempFilePathPrefix = getStr(env, "TEMP_FILE_PATH_PREFIX", "/tmp/");
    chainBootstrapJsonPath = getStr(env, "CHAIN_BOOTSTRAP_JSON_PATH", EMPTY);
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
   Get a integer value from the given map, or if the key isn't in the map, return the default value

   @param map    in which to search for a key
   @param key    to search for
   @param orElse to return if the key is not found in the map
   @return value at key in map, else the default value
   */
  @SuppressWarnings("SameParameterValue")
  private Integer getInt(Map<String, String> map, String key, Integer orElse) {
    if (!map.containsKey(key)) return orElse;
    return Integer.valueOf(map.get(key));
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
   @return the audio cache file prefix
   */
  public String getAudioCacheFilePrefix() {
    return audioCacheFilePrefix;
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
   @return the segment base URL
   */
  public String getSegmentBaseURL() {
    return segmentBaseURL;
  }

  /**
   @return the segment file bucket
   */
  public String getSegmentFileBucket() {
    return segmentFileBucket;
  }

  /**
   @return the environment, e.g. "dev" "stage" or "prod"
   */
  public String getEnvironment() {
    return environment;
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
   @return the Player base URL
   */
  public String getPlayerBaseURL() {
    return playerBaseURL;
  }

  /**
   @return the Hub base URL
   */
  public String getHubBaseURL() {
    return hubBaseURL;
  }

  /**
   @return the Hub internal token name, e.g. "access_token"
   */
  public String getHubTokenName() {
    return hubTokenName;
  }

  /**
   @return the Hub internal token
   */
  public String getHubTokenValue() {
    return hubTokenValue;
  }

  /**
   @return the temp file path prefix
   */
  public String getTempFilePathPrefix() {
    return tempFilePathPrefix;
  }

  /**
   @return the chain bootstrap json path
   */
  public String getChainBootstrapJsonPath() {
    return chainBootstrapJsonPath;
  }
}
