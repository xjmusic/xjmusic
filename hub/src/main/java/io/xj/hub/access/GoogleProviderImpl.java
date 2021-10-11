// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.*;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.plus.model.Person;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.xj.lib.app.Environment;
import io.xj.lib.json.ApiUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;

class GoogleProviderImpl implements GoogleProvider {
  private static final String CALLBACK_PATH = "auth/google/callback";
  private static final Collection<String> SCOPES = ImmutableList.of("profile", "email");
  private static final String API_PEOPLE_ENDPOINT = "https://www.googleapis.com/plus/v1/people/me";
  private static final Logger log = LoggerFactory.getLogger(GoogleProviderImpl.class);
  private final GoogleHttpProvider googleHttpProvider;
  private final JsonFactory jsonFactory;
  private final ApiUrlProvider apiUrlProvider;

  private String clientId;
  private String clientSecret;

  @Inject
  public GoogleProviderImpl(
    GoogleHttpProvider googleHttpProvider,
    JsonFactory jsonFactory,
    ApiUrlProvider apiUrlProvider,
    Environment env
  ) {
    this.googleHttpProvider = googleHttpProvider;
    this.jsonFactory = jsonFactory;
    this.apiUrlProvider = apiUrlProvider;
    try {
      clientId = env.getGoogleClientID();
      clientSecret = env.getGoogleClientSecret();
    } catch (Exception e) {
      log.error("Failed to initialize Google Provider: {}", e.getMessage());
    }
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
  public String getAuthCodeRequestUrl() {
    return new AuthorizationCodeRequestUrl(GoogleOAuthConstants.AUTHORIZATION_SERVER_URL, clientId)
      .setResponseTypes(ImmutableList.of("code"))
      .setRedirectUri(getCallbackUrl())
      .setState("xj-music")
      .setScopes(SCOPES)
      .build();
  }

  @Override
  public String getCallbackUrl() {
    return apiUrlProvider.getApiUrlString(CALLBACK_PATH);
  }

  @Override
  public GoogleTokenResponse getTokenFromCode(String code) throws HubAccessException {
    GoogleTokenResponse response;
    try {
      HttpTransport httpTransport = googleHttpProvider.getTransport();
      GoogleAuthorizationCodeTokenRequest request = new GoogleAuthorizationCodeTokenRequest(httpTransport, jsonFactory,
        clientId, clientSecret,
        code, getCallbackUrl());
      response = request.execute();
    } catch (TokenResponseException e) {
      log.error("GoogleProvider.getTokenFromCode failed to retrieve token response: {}", detailsOfTokenException(e));
      throw new HubAccessException("Failed to retrieve token response for Google OAuth2 code.", e);
    } catch (IOException e) {
      log.error("GoogleProvider.getTokenFromCode had I/O failure!", e);
      throw new HubAccessException("I/O failure.", e);
    }

    return response;
  }

  @Override
  public Person getMe(String externalAccessToken) throws HubAccessException {
    @SuppressWarnings("deprecation")
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
      throw new HubAccessException("Failed to request profile create Google+ API: ", e);
    }

    Person person;
    try {
      person = jsonFactory.createJsonParser(responseJson).parse(Person.class);
    } catch (Exception e) {
      throw new HubAccessException("Google API result is not valid JSON", e);
    }

    return person;
  }
}
