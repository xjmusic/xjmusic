// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.application.auth.google;

import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import io.outright.xj.core.application.exception.AuthenticationException;
import io.outright.xj.core.application.exception.ConfigException;
import io.outright.xj.core.application.server.BaseUrlProvider;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleAuthProviderImpl implements GoogleAuthProvider {
  private Logger log = LoggerFactory.getLogger(GoogleAuthProviderImpl.class);
  private BaseUrlProvider baseUrlProvider;
  private GoogleOAuth2Credentials googleOAuth2Credentials;
  private HttpTransport transport;
  private JsonFactory jsonFactory;

  private final static String CALLBACK_PATH = "auth/google/callback";

  @Inject
  public GoogleAuthProviderImpl(
    BaseUrlProvider baseUrlProvider,
    GoogleOAuth2Credentials googleOAuth2Credentials,
    HttpTransport transport,
    JsonFactory jsonFactory
  ) {
    this.baseUrlProvider = baseUrlProvider;
    this.googleOAuth2Credentials = googleOAuth2Credentials;
    this.transport = transport;
    this.jsonFactory = jsonFactory;
  }

  @Override
  public String getAuthCodeRequestUrl() throws ConfigException {
    return new AuthorizationCodeRequestUrl(GoogleOAuthConstants.AUTHORIZATION_SERVER_URL,googleOAuth2Credentials.getClientId())
      .setResponseTypes(ImmutableList.of("code"))
      .setRedirectUri(redirectUri())
      .setState("xj-music")
      .setScopes(ImmutableList.of("profile"))
      .build();
  }

  @Override
  public String getTokenFromCode(String code) throws AuthenticationException {
    GoogleTokenResponse response = null;
    try {
      response = new GoogleAuthorizationCodeTokenRequest(transport, jsonFactory,
        googleOAuth2Credentials.getClientId(), googleOAuth2Credentials.getClientSecret(),
        code, redirectUri())
        .execute();
    } catch (TokenResponseException e) {
      log.error("GoogleAuthorizationCodeTokenRequest failed!", e.getMessage());
      if (e.getDetails() != null) {
        log.error("GoogleAuthorizationCodeTokenRequest failure details: " + e.getDetails().getError());
        if (e.getDetails().getErrorDescription() != null) {
          log.error("GoogleAuthorizationCodeTokenRequest failure description: " + e.getDetails().getErrorDescription());
        }
        if (e.getDetails().getErrorUri() != null) {
          log.error("GoogleAuthorizationCodeTokenRequest failure URI:" + e.getDetails().getErrorUri());
        }
      } else {
        log.error("GoogleAuthorizationCodeTokenRequest failure message: " + e.getMessage());
      }
      throw new AuthenticationException("Failed to get token from Google OAuth2 code.");
    } catch (IOException e) {
      log.error("GoogleAuthorizationCodeTokenRequest failed!", e.getMessage());
      throw new AuthenticationException("Failed to get token from Google OAuth2 code.");
    } catch (ConfigException e) {
      log.error("Application configuration error!", e);
    }

    if (response == null) {
      log.error("GoogleAuthorizationCodeTokenRequest must have failed somehow; it's null.");
      throw new AuthenticationException("Failed to get token from Google OAuth2 code.");
    }

    return response.getAccessToken();
  }

  /**
   * URI that the authorization server directs the resource owner's user-agent back to
   * @return String URI
   * @throws ConfigException if required system properties are not set.
   */
  private String redirectUri() throws ConfigException {
    return baseUrlProvider.getUrl() + CALLBACK_PATH;
  }
}
