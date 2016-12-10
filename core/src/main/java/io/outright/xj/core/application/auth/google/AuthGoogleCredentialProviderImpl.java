// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;

public class AuthGoogleCredentialProviderImpl implements AuthGoogleCredentialProvider {
  @Override
  public GoogleCredential getCredential(String accessToken) {
    return new GoogleCredential().setAccessToken(accessToken);
  }
}
