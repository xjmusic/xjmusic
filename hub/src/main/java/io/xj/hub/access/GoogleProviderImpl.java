// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

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
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.plus.model.Person;
import io.xj.lib.json.ApiUrlProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@Service
public class GoogleProviderImpl implements GoogleProvider {
  static final String CALLBACK_PATH = "auth/google/callback";
  static final Collection<String> SCOPES = List.of("profile", "email");
  static final String API_PEOPLE_ENDPOINT = "https://www.googleapis.com/plus/v1/people/me";
  static final Logger LOG = LoggerFactory.getLogger(GoogleProviderImpl.class);
  final GoogleHttpProvider googleHttpProvider;
  final GsonFactory gsonFactory;
  final ApiUrlProvider apiUrlProvider;
  final String clientId;
  final String clientSecret;

  @Autowired
  public GoogleProviderImpl(
    GoogleHttpProvider googleHttpProvider,
    ApiUrlProvider apiUrlProvider,
    @Value("${google.client.id}") String clientId,
    @Value("${google.client.secret}") String clientSecret
  ) {
    this.googleHttpProvider = googleHttpProvider;
    this.gsonFactory = new GsonFactory();
    this.apiUrlProvider = apiUrlProvider;
    this.clientId = clientId;
    this.clientSecret = clientSecret;
  }

  static String detailsOfTokenException(TokenResponseException e) {
    return
      (null != e.getMessage() ? e.getMessage() : "") +
        (null != e.getDetails() ? (
          (null != e.getDetails().getError() ? e.getDetails().getError() : "") +
            (null != e.getDetails().getErrorDescription() ? e.getDetails().getErrorDescription() : "") +
            (null != e.getDetails().getErrorUri() ? e.getDetails().getErrorUri() : "")
        ) : "");
  }

  @Override
  public String getAuthCodeRequestUrl(String state) {
    return new AuthorizationCodeRequestUrl(GoogleOAuthConstants.AUTHORIZATION_SERVER_URL, clientId)
      .setResponseTypes(List.of("code"))
      .setRedirectUri(getCallbackUrl())
      .setState(state)
      .setScopes(SCOPES)
      .build();
  }

  @Override
  public String getCallbackUrl() {
    return apiUrlProvider.getAppUrl(CALLBACK_PATH);
  }

  @Override
  public GoogleTokenResponse getTokenFromCode(String code) throws HubAccessException {
    GoogleTokenResponse response;
    try {
      HttpTransport httpTransport = googleHttpProvider.getTransport();
      GoogleAuthorizationCodeTokenRequest request = new GoogleAuthorizationCodeTokenRequest(httpTransport, gsonFactory,
        clientId, clientSecret,
        code, getCallbackUrl());
      response = request.execute();
    } catch (TokenResponseException e) {
      LOG.error("GoogleProvider.getTokenFromCode failed to retrieve token response: {}", detailsOfTokenException(e));
      throw new HubAccessException("Failed to retrieve token response for Google OAuth2 code.", e);
    } catch (IOException e) {
      LOG.error("GoogleProvider.getTokenFromCode had I/O failure!", e);
      throw new HubAccessException("I/O failure.", e);
    } catch (IllegalArgumentException e) {
      LOG.error("GoogleProvider.getTokenFromCode had illegal argument failure!", e);
      throw new HubAccessException("Illegal argument failure.", e);
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
    try (var parser = gsonFactory.createJsonParser(responseJson)) {
      person = parser.parse(Person.class);
    } catch (Exception e) {
      throw new HubAccessException("Google API result is not valid JSON", e);
    }

    return person;
  }
}
