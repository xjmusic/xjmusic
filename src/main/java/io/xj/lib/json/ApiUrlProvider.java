// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.json;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.lib.app.Environment;

import java.net.URI;

/**
 Provider of URLs using the injected configuration and some custom formatting
 */
@Singleton
public class ApiUrlProvider {

  private String appBaseUrl;
  private String appHost;
  private String appHostname;
  private String appName;
  private String audioBaseUrl;
  private String segmentBaseUrl;

  private String playerBaseUrl;

  private String appPathUnauthorized;
  private String appPathWelcome;

  @Inject
  public ApiUrlProvider(Config config, Environment env) {
    this.appBaseUrl = env.getAppBaseURL();
    this.appName = config.getString("app.name");
    this.appPathUnauthorized = config.getString("api.unauthorizedRedirectPath");
    this.appPathWelcome = config.getString("api.welcomeRedirectPath");
    this.audioBaseUrl = env.getAudioBaseURL();
    this.playerBaseUrl = env.getPlayerBaseURL();
    this.segmentBaseUrl = env.getSegmentBaseURL();
  }

  /**
   Get URI object for a path within the API

   @param path within API
   @return String
   */
  public URI getApiURI(String path) {
    return URI.create(getApiUrlString(path));
  }

  /**
   Get URL String for a path within the API

   @param path within API
   @return String
   */
  public String getApiUrlString(String path) {
    return getAppBaseUrl() + path;
  }

  /**
   @return app base URL
   */
  public String getAppBaseUrl() {
    return appBaseUrl;
  }

  /**
   Set AppBaseUrl

   @param appBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppBaseUrl(String appBaseUrl) {
    this.appBaseUrl = appBaseUrl;
    return this;
  }

  /**
   Get the URL for a path in the app

   @param path to get URL for
   @return RUL for given path
   */
  public String getAppUrl(String path) {
    return String.format("%s%s", appBaseUrl, path);
  }

  /**
   @return app host
   */
  public String getAppHost() {
    return appHost;
  }

  /**
   Set AppHost

   @param appHost to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppHost(String appHost) {
    this.appHost = appHost;
    return this;
  }

  /**
   @return app hostname
   */
  public String getAppHostname() {
    return appHostname;
  }

  /**
   Set AppHostname

   @param appHostname to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppHostname(String appHostname) {
    this.appHostname = appHostname;
    return this;
  }

  /**
   @return app name
   */
  public String getAppName() {
    return appName;
  }

  /**
   Set AppName

   @param appName to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  /**
   @return Audio base URL (for Amazon S3)
   */
  public String getAudioBaseUrl() throws Exception {
    return audioBaseUrl;
  }

  /**
   Set AudioBaseUrl

   @param audioBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAudioBaseUrl(String audioBaseUrl) {
    this.audioBaseUrl = audioBaseUrl;
    return this;
  }

  /**
   @return Segments base URL (for Amazon S3)
   */
  public String getSegmentBaseUrl() throws Exception {
    return segmentBaseUrl;
  }

  /**
   Set SegmentBaseUrl

   @param segmentBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setSegmentBaseUrl(String segmentBaseUrl) {
    this.segmentBaseUrl = segmentBaseUrl;
    return this;
  }

  /**
   @return Players base URL
   */
  public String getPlayerBaseUrl() throws Exception {
    return playerBaseUrl;
  }

  /**
   Set PlayerBaseUrl

   @param playerBaseUrl to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setPlayerBaseUrl(String playerBaseUrl) {
    this.playerBaseUrl = playerBaseUrl;
    return this;
  }

  /**
   @return path for unauthorized redirect
   */
  public String getAppPathUnauthorized() {
    return appPathUnauthorized;
  }

  /**
   Set AppPathUnauthorized

   @param appPathUnauthorized to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppPathUnauthorized(String appPathUnauthorized) {
    this.appPathUnauthorized = appPathUnauthorized;
    return this;
  }

  /**
   @return path for unauthorized redirect
   */
  public String getAppPathWelcome() {
    return appPathWelcome;
  }

  /**
   Set AppPathWelcome

   @param appPathWelcome to set
   @return this ApiUrlProvider (for chaining methods)
   */
  public ApiUrlProvider setAppPathWelcome(String appPathWelcome) {
    this.appPathWelcome = appPathWelcome;
    return this;
  }

}
