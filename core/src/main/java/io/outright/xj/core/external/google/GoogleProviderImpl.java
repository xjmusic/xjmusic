// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.external.google;

import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.ConfigException;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.plus.model.Person;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

public class GoogleProviderImpl implements GoogleProvider {
  private final static String CALLBACK_PATH = "auth/google/callback";
  private final static Collection<String> SCOPES = ImmutableList.of("profile","email");
  private final static String API_PEOPLE_ENDPOINT = "https://www.googleapis.com/plus/v1/people/me";
  private static Logger log = LoggerFactory.getLogger(GoogleProviderImpl.class);
  private GoogleHttpProvider googleHttpProvider;
  private GoogleProvider googleProvider;
  private JsonFactory jsonFactory;

  private String clientId;
  private String clientSecret;

  @Inject
  public GoogleProviderImpl(
    GoogleHttpProvider googleHttpProvider,
    GoogleProvider googleProvider,
    JsonFactory jsonFactory
  ) {
    this.googleHttpProvider = googleHttpProvider;
    this.googleProvider = googleProvider;
    this.jsonFactory = jsonFactory;
    try {
      this.clientId = Config.authGoogleId();
      this.clientSecret = Config.authGoogleSecret();
    } catch (ConfigException e) {
      log.error("Failed to initialize Google Provider: " + e.getMessage());
    }
  }

  @Override
  public String getAuthCodeRequestUrl() throws ConfigException {
    return new AuthorizationCodeRequestUrl(GoogleOAuthConstants.AUTHORIZATION_SERVER_URL, clientId)
      .setResponseTypes(ImmutableList.of("code"))
      .setRedirectUri(getCallbackUrl())
      .setState("xj-music")
      .setScopes(SCOPES)
      .build();
  }

  @Override
  public String getCallbackUrl() throws ConfigException {
    return Exposure.apiUrlString(CALLBACK_PATH);
  }

  @Override
  public GoogleTokenResponse getTokenFromCode(String code) throws AccessException, ConfigException {
    GoogleAuthorizationCodeTokenRequest request;
    GoogleTokenResponse response;
    try {
      HttpTransport httpTransport = googleHttpProvider.getTransport();
      request = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory,
        clientId, clientSecret,
        code, googleProvider.getCallbackUrl());
      response = request.execute();
    } catch (TokenResponseException e) {
      log.error("GoogleProvider.getTokenFromCode failed to retrieve token response: {}", detailsOfTokenException(e));
      throw new AccessException("Failed to retrieve token response for Google OAuth2 code.");
    } catch (IOException e) {
      log.error("GoogleProvider.getTokenFromCode had I/O failure!", e);
      throw new AccessException("I/O failure.");
    }

    return response;
  }

  private String detailsOfTokenException(TokenResponseException e) {
    return
      (e.getMessage() != null ? e.getMessage() : "") +
      (e.getDetails() != null ? (
        (e.getDetails().getError() != null ? e.getDetails().getError() : "") +
          (e.getDetails().getErrorDescription() != null ? e.getDetails().getErrorDescription() : "") +
          (e.getDetails().getErrorUri() != null ? e.getDetails().getErrorUri() : "")
      ) : "");
  }

  @Override
  public Person getMe(String externalAccessToken) throws AccessException {
    GoogleCredential credential = new GoogleCredential()
      .setAccessToken(externalAccessToken)
      .createScoped(SCOPES);
    GenericUrl url = new GenericUrl(API_PEOPLE_ENDPOINT);
    String responseJson;
    try {
      HttpTransport httpTransport = googleHttpProvider.getTransport();
      HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
      HttpRequest request = requestFactory.buildGetRequest(url);
      HttpResponse response = request.execute();
      responseJson = response.parseAsString();
    } catch (IOException e) {
      log.error("Failed to request profile from Google+ API: {}", e);
      throw new AccessException("Failed to request profile from Google+ API: "+ e.getMessage());
    }

    Person person;
    try {
      person = jsonFactory.createJsonParser(responseJson).parse(Person.class);
    } catch (Exception e) {
      log.error("Google API result is not valid JSON", e);
      throw new AccessException("Google API result is not valid JSON: " + e);
    }

    return person;
  }
}
