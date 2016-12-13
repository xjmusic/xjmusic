// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeFlow;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.util.store.DataStoreFactory;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.outright.xj.core.application.exception.ConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class GoogleAuthProviderImpl implements GoogleAuthProvider {
  private Logger log = LoggerFactory.getLogger(GoogleAuthProviderImpl.class);
  private JsonFactory jsonFactory;
  private HttpTransport httpTransport;
  private DataStoreFactory dataStoreFactory;
  private AuthorizationCodeFlow flow;

  @Inject
  public GoogleAuthProviderImpl(
    JsonFactory jsonFactory,
    HttpTransport httpTransport,
    DataStoreFactory dataStoreFactory
  ) {
    this.jsonFactory = jsonFactory;
    this.httpTransport = httpTransport;
    this.dataStoreFactory = dataStoreFactory;
  }

  public String getAuthCodeRequestUrl() throws ConfigException {
    String clientId = getRequiredProperty("auth.google.id");
    String baseUrl = getRequiredProperty("app.url");
//    String clientSecret = getRequiredProperty("auth.google.secret");
    return new AuthorizationCodeRequestUrl(GoogleOAuthConstants.AUTHORIZATION_SERVER_URL,clientId)
      .setResponseTypes(ImmutableList.of("code"))
      .setRedirectUri(baseUrl + "auth/google/callback")
      .setScopes(ImmutableList.of("profile"))
      .build();
  }

  private String getRequiredProperty(String key) throws ConfigException {
    String value = System.getProperty(key);
    if (value == null) {
      throw new ConfigException("Must set system property: "+key);
    }
    return value;
  }
}
