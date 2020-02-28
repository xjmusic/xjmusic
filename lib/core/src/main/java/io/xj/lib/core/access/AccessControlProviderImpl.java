// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.access;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.plus.model.Person;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.lib.core.dao.UserDAO;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.external.GoogleProvider;
import io.xj.lib.core.model.AccountUser;
import io.xj.lib.core.model.UserAuth;
import io.xj.lib.core.model.UserAuthType;
import io.xj.lib.core.model.UserRole;
import io.xj.lib.core.persistence.RedisDatabaseProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.ws.rs.core.NewCookie;
import java.util.Collection;
import java.util.Map;

class AccessControlProviderImpl implements AccessControlProvider {
  private static final Logger log = LoggerFactory.getLogger(AccessControlProviderImpl.class);
  private final RedisDatabaseProvider redisDatabaseProvider;
  private final TokenGenerator tokenGenerator;
  private final GoogleProvider googleProvider;
  private final UserDAO userDAO;

  private final String tokenName;
  private final String tokenDomain;
  private final String tokenPath;
  private final int tokenMaxAge;

  @Inject
  public AccessControlProviderImpl(
    RedisDatabaseProvider redisDatabaseProvider,
    TokenGenerator tokenGenerator,
    GoogleProvider googleProvider,
    UserDAO userDAO,
    Config config
  ) {
    this.redisDatabaseProvider = redisDatabaseProvider;
    this.tokenGenerator = tokenGenerator;
    this.googleProvider = googleProvider;
    this.userDAO = userDAO;

    tokenName = config.getString("access.tokenName");
    tokenDomain = config.getString("access.tokenDomain");
    tokenPath = config.getString("access.tokenPath");
    tokenMaxAge = config.getInt("access.tokenMaxAgeSeconds");
  }

  @Override
  public String create(UserAuth userAuth, Collection<AccountUser> accountUsers, Collection<UserRole> userRoles) throws CoreException {
    String accessToken = tokenGenerator.generate();
    update(accessToken, userAuth, accountUsers, userRoles);
    return accessToken;
  }

  @Override
  public Map<String, String> update(String accessToken, UserAuth userAuth, Collection<AccountUser> userAccountRoles, Collection<UserRole> userRoles) throws CoreException {
    Access access = new Access(userAuth, userAccountRoles, userRoles);
    Map<String, String> userMap = access.toMap();
    Jedis client = redisDatabaseProvider.getClient();
    try {
      client.hmset(accessToken, userMap);
      client.close();
    } catch (Exception e) {
      client.close();
      log.error("Redis database connection", e);
      throw new CoreException("Redis database connection", e);
    }
    return userMap;
  }


  @Override
  public void expire(String token) throws CoreException {
    Jedis client = redisDatabaseProvider.getClient();
    try {
      client.del(token);
      client.close();
    } catch (Exception e) {
      client.close();
      throw new CoreException("Redis error", e);
    }
  }

  @Override
  public Access get(String token) throws CoreException {
    Jedis client = redisDatabaseProvider.getClient();
    try {
      Access access = new Access(client.hgetAll(token));
      client.close();
      return access;
    } catch (Exception e) {
      client.close();
      throw new CoreException("Redis error(" + e.getClass().getName() + ")", e);
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

    try {
      GoogleTokenResponse tokenResponse = googleProvider.getTokenFromCode(accessCode);
      externalAccessToken = tokenResponse.getAccessToken();
      externalRefreshToken = tokenResponse.getRefreshToken();
    } catch (Exception e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new CoreException("Authentication failed", e);
    }

    Person person;
    try {
      person = googleProvider.getMe(externalAccessToken);
    } catch (Exception e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new CoreException("Authentication failed", e);
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
