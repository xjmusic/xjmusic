// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.external.google;

import io.xj.core.config.Config;
import io.xj.core.exception.CoreException;

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
  private static final String CALLBACK_PATH = "auth/google/callback";
  private static final Collection<String> SCOPES = ImmutableList.of("profile", "email");
  private static final String API_PEOPLE_ENDPOINT = "https://www.googleapis.com/plus/v1/people/me";
  private static final Logger log = LoggerFactory.getLogger(GoogleProviderImpl.class);
  private final GoogleHttpProvider googleHttpProvider;
  private final JsonFactory jsonFactory;

  private String clientId;
  private String clientSecret;

  @Inject
  public GoogleProviderImpl(
    GoogleHttpProvider googleHttpProvider,
    JsonFactory jsonFactory
  ) {
    this.googleHttpProvider = googleHttpProvider;
    this.jsonFactory = jsonFactory;
    try {
      clientId = Config.getAuthGoogleId();
      clientSecret = Config.getAuthGoogleSecret();
    } catch (CoreException e) {
      log.error("Failed to initialize Google Provider: {}", e.getMessage());
    }
  }

  @Override
  public String getAuthCodeRequestUrl() throws CoreException {
    return new AuthorizationCodeRequestUrl(GoogleOAuthConstants.AUTHORIZATION_SERVER_URL, clientId)
      .setResponseTypes(ImmutableList.of("code"))
      .setRedirectUri(getCallbackUrl())
      .setState("xj-music")
      .setScopes(SCOPES)
      .build();
  }

  @Override
  public String getCallbackUrl() {
    return Config.getApiUrlString(CALLBACK_PATH);
  }

  @Override
  public GoogleTokenResponse getTokenFromCode(String code) throws CoreException {
    GoogleTokenResponse response;
    try {
      HttpTransport httpTransport = googleHttpProvider.getTransport();
      GoogleAuthorizationCodeTokenRequest request = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory,
        clientId, clientSecret,
        code, getCallbackUrl());
      response = request.execute();
    } catch (TokenResponseException e) {
      log.error("GoogleProvider.getTokenFromCode failed to retrieve token response: {}", detailsOfTokenException(e));
      throw new CoreException("Failed to retrieve token response for Google OAuth2 code.", e);
    } catch (IOException e) {
      log.error("GoogleProvider.getTokenFromCode had I/O failure!", e);
      throw new CoreException("I/O failure.", e);
    }

    return response;
  }

  private static String detailsOfTokenException(TokenResponseException e) {
    return
      (null != e.getMessage() ? e.getMessage() : "") +
        (null != e.getDetails() ? (
          (null != e.getDetails().getError() ? e.getDetails().getError() : "") +
            (null != e.getDetails().getErrorDescription() ? e.getDetails().getErrorDescription() : "") +
            (null != e.getDetails().getErrorUri() ? e.getDetails().getErrorUri() : "")
        ) : "");
  }

  @Override
  public Person getMe(String externalAccessToken) throws CoreException {
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
      throw new CoreException("Failed to request profile create Google+ API: ", e);
    }

    Person person;
    try {
      person = jsonFactory.createJsonParser(responseJson).parse(Person.class);
    } catch (Exception e) {
      throw new CoreException("Google API result is not valid JSON", e);
    }

    return person;
  }
}
