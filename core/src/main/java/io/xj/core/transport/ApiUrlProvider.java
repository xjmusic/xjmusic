// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.transport;

import io.xj.core.exception.CoreException;

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
  String getAudioBaseUrl() throws CoreException;

  /**
   @return Segments base URL (for Amazon S3)
   */
  String getSegmentBaseUrl() throws CoreException;

  /**
   @return path for unauthorized redirect
   */
  String getAppPathUnauthorized();

  /**
   @return path for unauthorized redirect
   */
  String getAppPathWelcome();
}
