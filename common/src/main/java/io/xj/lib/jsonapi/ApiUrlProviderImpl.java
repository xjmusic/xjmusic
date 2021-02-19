// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;

import java.net.URI;

@Singleton
class ApiUrlProviderImpl implements ApiUrlProvider {
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
  public ApiUrlProviderImpl(Config config) {
    this.appBaseUrl = config.getString("app.baseUrl");
    this.appHost = config.getString("app.host");
    this.appHostname = config.getString("app.hostname");
    this.appName = config.getString("app.name");
    this.appPathUnauthorized = config.getString("api.unauthorizedRedirectPath");
    this.appPathWelcome = config.getString("api.welcomeRedirectPath");
    this.audioBaseUrl = config.getString("audio.baseUrl");
    this.playerBaseUrl = config.getString("player.baseUrl");
    this.segmentBaseUrl = config.getString("segment.baseUrl");
  }

  @Override
  public URI getApiURI(String path) {
    return URI.create(getApiUrlString(path));
  }

  @Override
  public String getApiUrlString(String path) {
    return getAppBaseUrl() + path;
  }

  @Override
  public String getAppBaseUrl() {
    return appBaseUrl;
  }

  @Override
  public String getAppHost() {
    return appHost;

  }

  @Override
  public String getAppHostname() {
    return appHostname;
  }

  @Override
  public String getAppName() {
    return appName;
  }

  @Override
  public String getAudioBaseUrl() {
    return audioBaseUrl;
  }

  @Override
  public String getSegmentBaseUrl() {
    return segmentBaseUrl;
  }

  @Override
  public String getPlayerBaseUrl() {
    return playerBaseUrl;
  }

  @Override
  public String getAppPathUnauthorized() {
    return appPathUnauthorized;
  }

  @Override
  public String getAppPathWelcome() {
    return appPathWelcome;
  }

  @Override
  public ApiUrlProvider setAppBaseUrl(String appBaseUrl) {
    this.appBaseUrl = appBaseUrl;
    return this;
  }

  @Override
  public ApiUrlProvider setAppHost(String appHost) {
    this.appHost = appHost;
    return this;
  }

  @Override
  public ApiUrlProvider setAppHostname(String appHostname) {
    this.appHostname = appHostname;
    return this;
  }

  @Override
  public ApiUrlProvider setAppName(String appName) {
    this.appName = appName;
    return this;
  }

  @Override
  public ApiUrlProvider setAudioBaseUrl(String audioBaseUrl) {
    this.audioBaseUrl = audioBaseUrl;
    return this;
  }

  @Override
  public ApiUrlProvider setSegmentBaseUrl(String segmentBaseUrl) {
    this.segmentBaseUrl = segmentBaseUrl;
    return this;
  }

  @Override
  public ApiUrlProvider setPlayerBaseUrl(String playerBaseUrl) {
    this.playerBaseUrl = playerBaseUrl;
    return this;
  }

  @Override
  public ApiUrlProvider setAppPathUnauthorized(String appPathUnauthorized) {
    this.appPathUnauthorized = appPathUnauthorized;
    return this;
  }

  @Override
  public ApiUrlProvider setAppPathWelcome(String appPathWelcome) {
    this.appPathWelcome = appPathWelcome;
    return this;
  }
}
