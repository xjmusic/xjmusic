// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import java.net.URI;

/**
 Provider of URLs using the injected configuration and some custom formatting
 */
public interface ApiUrlProvider {

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
   Get the URL for a path in the app
   @param path to get URL for
   @return RUL for given path
   */
  String getAppUrl(String path);

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
  String getAudioBaseUrl() throws JsonApiException;

  /**
   @return Segments base URL (for Amazon S3)
   */
  String getSegmentBaseUrl() throws JsonApiException;

  /**
   @return Players base URL
   */
  String getPlayerBaseUrl() throws JsonApiException;

  /**
   @return path for unauthorized redirect
   */
  String getAppPathUnauthorized();

  /**
   @return path for unauthorized redirect
   */
  String getAppPathWelcome();

  /**
   Set AppBaseUrl

   @param appBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  ApiUrlProvider setAppBaseUrl(String appBaseUrl);

  /**
   Set AppHost

   @param appHost to set
   @return this ApiUrlProvider (for chaining methods)
   */
  ApiUrlProvider setAppHost(String appHost);

  /**
   Set AppHostname

   @param appHostname to set
   @return this ApiUrlProvider (for chaining methods)
   */
  ApiUrlProvider setAppHostname(String appHostname);

  /**
   Set AppName

   @param appName to set
   @return this ApiUrlProvider (for chaining methods)
   */
  ApiUrlProvider setAppName(String appName);

  /**
   Set AudioBaseUrl

   @param audioBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  ApiUrlProvider setAudioBaseUrl(String audioBaseUrl);

  /**
   Set SegmentBaseUrl

   @param segmentBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  ApiUrlProvider setSegmentBaseUrl(String segmentBaseUrl);

  /**
   Set PlayerBaseUrl

   @param playerBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  ApiUrlProvider setPlayerBaseUrl(String playerBaseUrl);

  /**
   Set AppPathUnauthorized

   @param appPathUnauthorized to set
   @return this ApiUrlProvider (for chaining methods)
   */
  ApiUrlProvider setAppPathUnauthorized(String appPathUnauthorized);

  /**
   Set AppPathWelcome

   @param appPathWelcome to set
   @return this ApiUrlProvider (for chaining methods)
   */
  ApiUrlProvider setAppPathWelcome(String appPathWelcome);

}
