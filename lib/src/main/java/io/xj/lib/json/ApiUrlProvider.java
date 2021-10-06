// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.json;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.lib.app.Environment;

import java.net.URI;

/**
 * Provider of URLs using the injected configuration and some custom formatting
 */
@Singleton
public class ApiUrlProvider {
  private final String appBaseUrl;
  private final String audioBaseUrl;
  private final String shipBaseUrl;
  private final String playerBaseUrl;
  private final String appPathUnauthorized;
  private final String appPathWelcome;

  @Inject
  public ApiUrlProvider(Environment env) {
    this.appBaseUrl = env.getAppBaseURL();
    this.appPathUnauthorized = env.getApiUnauthorizedRedirectPath();
    this.appPathWelcome = env.getApiWelcomeRedirectPath();
    this.audioBaseUrl = env.getAudioBaseURL();
    this.playerBaseUrl = env.getPlayerBaseURL();
    this.shipBaseUrl = env.getShipBaseUrl();
  }

  /**
   * Get URI object for a path within the API
   *
   * @param path within API
   * @return String
   */
  public URI getApiURI(String path) {
    return URI.create(getApiUrlString(path));
  }

  /**
   * Get URL String for a path within the API
   *
   * @param path within API
   * @return String
   */
  public String getApiUrlString(String path) {
    return getAppBaseUrl() + path;
  }

  /**
   * @return app base URL
   */
  public String getAppBaseUrl() {
    return appBaseUrl;
  }

  /**
   * Get the URL for a path in the app
   *
   * @param path to get URL for
   * @return RUL for given path
   */
  public String getAppUrl(String path) {
    return String.format("%s%s", appBaseUrl, path);
  }

  /**
   * @return Audio base URL (for Amazon S3)
   */
  public String getAudioBaseUrl() {
    return audioBaseUrl;
  }

  /**
   * @return Segments base URL (for Amazon S3)
   */
  public String getShipBaseUrl() {
    return shipBaseUrl;
  }

  /**
   * @return Players base URL
   */
  public String getPlayerBaseUrl() {
    return playerBaseUrl;
  }

  /**
   * @return path for unauthorized redirect
   */
  public String getAppPathUnauthorized() {
    return appPathUnauthorized;
  }

  /**
   * @return path for unauthorized redirect
   */
  public String getAppPathWelcome() {
    return appPathWelcome;
  }

}
