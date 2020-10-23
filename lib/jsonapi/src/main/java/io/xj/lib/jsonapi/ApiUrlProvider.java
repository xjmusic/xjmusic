// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import com.typesafe.config.Config;

import java.net.URI;

/**
 Provider of URLs using the injected configuration and some custom formatting
 */
public interface ApiUrlProvider {
  /**
   Given an instance of the REST API library's ApiUrlProvider, configure it for this Hub Application@param apiUrlProvider of app to configure
   */
  static void configureApiUrls(Config config, ApiUrlProvider apiUrlProvider) {
    apiUrlProvider.setApiPath(config.getString("app.apiUrl"));
    apiUrlProvider.setAppBaseUrl(config.getString("app.baseUrl"));
    apiUrlProvider.setAppHost(config.getString("app.host"));
    apiUrlProvider.setAppHostname(config.getString("app.hostname"));
    apiUrlProvider.setAppName(config.getString("app.name"));
    apiUrlProvider.setAppPathUnauthorized(config.getString("api.unauthorizedRedirectPath"));
    apiUrlProvider.setAppPathWelcome(config.getString("api.welcomeRedirectPath"));
    apiUrlProvider.setAudioBaseUrl(config.getString("audio.baseUrl"));
    apiUrlProvider.setPlayerBaseUrl(config.getString("player.baseUrl"));
    apiUrlProvider.setSegmentBaseUrl(config.getString("segment.baseUrl"));
  }

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
   Set ApiPath

   @param apiPath to set
   @return this ApiUrlProvider (for chaining methods)
   */
  ApiUrlProvider setApiPath(String apiPath);

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
