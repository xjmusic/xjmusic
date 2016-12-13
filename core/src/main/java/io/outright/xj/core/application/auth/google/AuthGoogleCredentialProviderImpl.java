// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public class AuthGoogleCredentialProviderImpl implements AuthGoogleCredentialProvider {
  private GoogleCredential instance;

  /**
   * Setup a new Google Credential provider
   * @param accessToken for Google API
   */
  @Override
  public void setup(String accessToken) {
    instance = new GoogleCredential().setAccessToken(accessToken);
  }

  /**
   * Get the Google Credential
   * @return GoogleCredential instance
   */
  @Override
  public GoogleCredential get() {
    return instance;
  }


}
