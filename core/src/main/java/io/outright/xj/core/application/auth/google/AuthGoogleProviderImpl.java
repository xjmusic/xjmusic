// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

public class AuthGoogleProviderImpl implements AuthGoogleProvider {

  private final String clientId;
  private final String clientSecret;

  public AuthGoogleProviderImpl(String clientId, String clientSecret) {
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }
}
