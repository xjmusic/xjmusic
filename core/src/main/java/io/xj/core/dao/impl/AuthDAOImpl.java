// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.impl;

import io.xj.core.exception.AccessException;
import io.xj.core.dao.AuthDAO;
import io.xj.core.dao.UserDAO;
import io.xj.core.external.AuthType;
import io.xj.core.external.google.GoogleProvider;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.plus.model.Person;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 NOTE: THIS IS AN IRREGULAR D.A.O.
 <p>
 Conceptually, because Authentication is a dependency of all other DAOs.
 */
public class AuthDAOImpl extends DAOImpl implements AuthDAO {
  private static Logger log = LoggerFactory.getLogger(AuthDAOImpl.class);
  private GoogleProvider googleProvider;
  private UserDAO userDAO;

  @Inject
  public AuthDAOImpl(
    GoogleProvider googleProvider,
    UserDAO userDAO
  ) {
    this.googleProvider = googleProvider;
    this.userDAO = userDAO;
  }

  public String authenticate(String accessCode) throws Exception {
    String externalAccessToken;
    String externalRefreshToken;
    Person person;

    try {
      GoogleTokenResponse tokenResponse = googleProvider.getTokenFromCode(accessCode);
      externalAccessToken = tokenResponse.getAccessToken();
      externalRefreshToken = tokenResponse.getRefreshToken();
    } catch (AccessException e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new AccessException("Authentication failed: " + e.getMessage());
    }

    try {
      person = googleProvider.getMe(externalAccessToken);
    } catch (AccessException e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new AccessException("Authentication failed: " + e.getMessage());
    }

    return userDAO.authenticate(
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
