// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import com.google.inject.Inject;
import com.typesafe.config.Config;

import java.net.URI;

class ApiUrlProviderImpl implements ApiUrlProvider {

  @Inject
  public Config config;

  @Override
  public String getApiPath() {
    return config.getString("app.apiURL");
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
    return config.getString("app.baseURL");
  }

  @Override
  public String getAppHost() {
    return config.getString("app.host");
  }

  @Override
  public String getAppHostname() {
    return config.getString("app.hostname");
  }

  @Override
  public String getAppName() {
    return config.getString("app.name");
  }

  @Override
  public String getAudioBaseUrl() {
    return config.getString("audio.baseURL");
  }

  @Override
  public String getSegmentBaseUrl() {
    return config.getString("segment.baseURL");
  }

  @Override
  public String getAppPathUnauthorized() {
    return config.getString("api.unauthorizedRedirectPath");
  }

  @Override
  public String getAppPathWelcome() {
    return config.getString("api.welcomeRedirectPath");
  }

}
