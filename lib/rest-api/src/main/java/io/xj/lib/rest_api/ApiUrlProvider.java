// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.rest_api;

import java.net.URI;

/**
 Provider of URLs using the injected configuration and some custom formatting
 */
public interface ApiUrlProvider {
  /**
   @return API path
   */
  String getApiPath();

  /**
   Get URI object for a path within the API

   @param path within API
   @return String
   */
  URI getApiURI(String path);

  /**
   Get URL String for a path within the API

   @param path within API
   @return String
   */
  String getApiUrlString(String path);

  /**
   @return app base URL
   */
  String getAppBaseUrl();

  /**
   @return app host
   */
  String getAppHost();

  /**
   @return app hostname
   */
  String getAppHostname();

  /**
   @return app name
   */
  String getAppName();

  /**
   @return Audio base URL (for Amazon S3)
   */
  String getAudioBaseUrl() throws RestApiException;

  /**
   @return Segments base URL (for Amazon S3)
   */
  String getSegmentBaseUrl() throws RestApiException;

  /**
   @return path for unauthorized redirect
   */
  String getAppPathUnauthorized();

  /**
   @return path for unauthorized redirect
   */
  String getAppPathWelcome();

  /**
   Set ApiPath

   @param apiPath to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setApiPath(String apiPath);

  /**
   Set AppBaseUrl

   @param appBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppBaseUrl(String appBaseUrl);

  /**
   Set AppHost

   @param appHost to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppHost(String appHost);

  /**
   Set AppHostname

   @param appHostname to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppHostname(String appHostname);

  /**
   Set AppName

   @param appName to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppName(String appName);

  /**
   Set AudioBaseUrl

   @param audioBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAudioBaseUrl(String audioBaseUrl);

  /**
   Set SegmentBaseUrl

   @param segmentBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setSegmentBaseUrl(String segmentBaseUrl);

  /**
   Set AppPathUnauthorized

   @param appPathUnauthorized to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppPathUnauthorized(String appPathUnauthorized);

  /**
   Set AppPathWelcome

   @param appPathWelcome to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppPathWelcome(String appPathWelcome);

}
