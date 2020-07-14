// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.jsonapi;

import com.google.inject.Singleton;

import java.net.URI;

@Singleton
class ApiUrlProviderImpl implements ApiUrlProvider {
  private String apiPath;
  private String appBaseUrl;
  private String appHost;
  private String appHostname;
  private String appName;
  private String audioBaseUrl;
  private String segmentBaseUrl;
  private String appPathUnauthorized;
  private String appPathWelcome;

  @Override
  public String getApiPath() {
    return apiPath;
  }

  @Override
  public URI getApiURI(String path) {
    return URI.create(getApiUrlString(path));
  }

  @Override
  public String getApiUrlString(String path) {
    return getAppBaseUrl() + getApiPath() + path;
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
  public String getAppPathUnauthorized() {
    return appPathUnauthorized;
  }

  @Override
  public String getAppPathWelcome() {
    return appPathWelcome;
  }

  @Override
  public ApiUrlProvider setApiPath(String apiPath) {
    this.apiPath = apiPath;
    return this;
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
