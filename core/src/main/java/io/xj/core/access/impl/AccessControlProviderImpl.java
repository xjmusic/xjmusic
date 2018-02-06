// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access.impl;

import io.xj.core.access.AccessControlProvider;
import io.xj.core.config.Config;
import io.xj.core.dao.UserDAO;
import io.xj.core.exception.AccessException;
import io.xj.core.exception.DatabaseException;
import io.xj.core.external.google.GoogleProvider;
import io.xj.core.model.account_user.AccountUser;
import io.xj.core.model.user_auth.UserAuth;
import io.xj.core.model.user_auth.UserAuthType;
import io.xj.core.model.user_role.UserRole;
import io.xj.core.persistence.redis.RedisDatabaseProvider;
import io.xj.core.access.token.TokenGenerator;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.plus.model.Person;
import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.NewCookie;
import java.util.Collection;
import java.util.Map;

public class AccessControlProviderImpl implements AccessControlProvider {
  private static final Logger log = LoggerFactory.getLogger(AccessControlProviderImpl.class);
  private final RedisDatabaseProvider redisDatabaseProvider;
  private final TokenGenerator tokenGenerator;
  private final GoogleProvider googleProvider;
  private final UserDAO userDAO;

  private final String tokenName = Config.accessTokenName();
  private final String tokenDomain = Config.accessTokenDomain();
  private final String tokenPath = Config.accessTokenPath();
  private final String tokenMaxAge = String.valueOf(Config.accessTokenMaxAge());

  @Inject
  public AccessControlProviderImpl(
    RedisDatabaseProvider redisDatabaseProvider,
    TokenGenerator tokenGenerator,
    GoogleProvider googleProvider,
    UserDAO userDAO
  ) {
    this.redisDatabaseProvider = redisDatabaseProvider;
    this.tokenGenerator = tokenGenerator;
    this.googleProvider = googleProvider;
    this.userDAO = userDAO;
  }

  @Override
  public String create(UserAuth userAuth, Collection<AccountUser> userAccountRoles, Collection<UserRole> userRoles) throws AccessException {
    String accessToken = tokenGenerator.generate();
    update(accessToken, userAuth, userAccountRoles, userRoles);
    return accessToken;
  }

  @Override
  public Map<String, String> update(String accessToken, UserAuth userAuth, Collection<AccountUser> userAccountRoles, Collection<UserRole> userRoles) throws AccessException {
    Access access = new Access(userAuth, userAccountRoles, userRoles);
    Map<String, String> userMap = access.toMap();
    try {
      redisDatabaseProvider.getClient().hmset(accessToken, userMap);
    } catch (Exception e) {
      log.error("Redis database connection is not get properly!", e);
      throw new AccessException("Redis database connection is not get properly: " + e);
    }
    return userMap;
  }


  @Override
  public void expire(String token) throws DatabaseException {
    try {
      redisDatabaseProvider.getClient().del(token);
    } catch (Exception e) {
      throw new DatabaseException("Redis error", e);
    }
  }

  @Override
  public Access get(String token) throws DatabaseException {
    try {
      return new Access(redisDatabaseProvider.getClient().hgetAll(token));
    } catch (Exception e) {
      throw new DatabaseException("Redis error(" + e.getClass().getName() + ")", e);
    }
  }

  @Override
  public NewCookie newCookie(String accessToken) {
    return NewCookie.valueOf(
      cookieSetToken(accessToken) +
        cookieSetDomainPath() +
        "Max-Age=" + tokenMaxAge
    );
  }

  @Override
  public NewCookie newExpiredCookie() {
    return NewCookie.valueOf(
      cookieSetToken("expired") +
        cookieSetDomainPath() +
        "Expires=Thu, 01 Jan 1970 00:00:00 GMT"
    );
  }

  @Override
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
      throw new AccessException("Authentication failed", e);
    }

    try {
      person = googleProvider.getMe(externalAccessToken);
    } catch (AccessException e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new AccessException("Authentication failed", e);
    }

    return userDAO.authenticate(
      UserAuthType.Google,
      person.getId(),
      externalAccessToken,
      externalRefreshToken,
      person.getDisplayName(),
      person.getImage().getUrl(),
      person.getEmails().get(0).getValue()
    );
  }

  /**
   Access cookieSetToken value setter

   @param value to set
   @return name=value pair
   */
  private String cookieSetToken(String value) {
    return tokenName + "=" + value + ";";
  }

  /**
   Access cookieSetToken cookie Domain and Path

   @return String
   */
  private String cookieSetDomainPath() {
    return (tokenDomain.isEmpty() ? "" : "Domain=" + tokenDomain + ";") +
      "Path=" + tokenPath + ";";
  }
}
