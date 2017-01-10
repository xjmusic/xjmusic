// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.hub.controller.auth;

import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.external.AuthType;
import io.outright.xj.core.external.google.GoogleProvider;
import io.outright.xj.hub.controller.user.UserController;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.plus.model.Person;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleAuthController implements AuthController {
  private static Logger log = LoggerFactory.getLogger(GoogleAuthController.class);
  private GoogleProvider googleProvider;
  private UserController userController;

  @Inject
  public GoogleAuthController(
    GoogleProvider googleProvider,
    UserController userController
  ) {
    this.googleProvider = googleProvider;
    this.userController = userController;
  }

  public String authenticate(String accessCode) throws AccessException, ConfigException, DatabaseException {
    String externalAccessToken;
    String externalRefreshToken;
    Person person;

    try {
      GoogleTokenResponse tokenResponse = googleProvider.getTokenFromCode(accessCode);
      externalAccessToken = tokenResponse.getAccessToken();
      externalRefreshToken = tokenResponse.getRefreshToken();
    } catch (AccessException e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new AccessException("Authentication failed: "+ e.getMessage());
    }

    try {
      person = googleProvider.getMe(externalAccessToken);
    } catch (AccessException e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new AccessException("Authentication failed: "+ e.getMessage());
    }

    return userController.authenticate(
      AuthType.GOOGLE,
      person.getId(),
      externalAccessToken,
      externalRefreshToken,
      person.getDisplayName(),
      person.getImage().getUrl(),
      person.getEmails().get(0).getValue()
    );
  }
}
