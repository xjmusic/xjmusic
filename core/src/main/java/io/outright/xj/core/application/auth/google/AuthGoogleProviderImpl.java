// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.DataStoreFactory;

import com.google.inject.Inject;

import java.io.IOException;

public class AuthGoogleProviderImpl implements AuthGoogleProvider {
  private JsonFactory jsonFactory;
  private HttpTransport httpTransport;
  private DataStoreFactory dataStoreFactory;
  private AuthorizationCodeFlow flow;

  @Inject
  public AuthGoogleProviderImpl(
    JsonFactory jsonFactory,
    HttpTransport httpTransport,
    DataStoreFactory dataStoreFactory
  ) {
    this.jsonFactory = jsonFactory;
    this.httpTransport = httpTransport;
    this.dataStoreFactory = dataStoreFactory;
  }

  public void setup(String clientId, String clientSecret) throws IOException {
    flow = new AuthorizationCodeFlow.Builder(BearerToken.authorizationHeaderAccessMethod(),
      httpTransport,
      jsonFactory,
      new GenericUrl(GoogleOAuthConstants.TOKEN_SERVER_URL),
      new BasicAuthentication(clientId,clientSecret),
      clientId,
      GoogleOAuthConstants.AUTHORIZATION_SERVER_URL).setCredentialDataStore(
      StoredCredential.getDefaultDataStore(dataStoreFactory))
      .build();
  }

  public AuthorizationCodeFlow getFlow() {
    return flow;
  }
}
