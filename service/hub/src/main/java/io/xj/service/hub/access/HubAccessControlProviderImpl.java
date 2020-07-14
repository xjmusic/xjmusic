// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.access;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.plus.model.Person;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.typesafe.config.Config;
import io.xj.service.hub.dao.UserDAO;
import io.xj.service.hub.entity.AccountUser;
import io.xj.service.hub.entity.UserAuth;
import io.xj.service.hub.entity.UserAuthType;
import io.xj.service.hub.entity.UserRole;
import io.xj.service.hub.persistence.HubRedisProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.ws.rs.core.NewCookie;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

class HubAccessControlProviderImpl implements HubAccessControlProvider {
  private static final Logger log = LoggerFactory.getLogger(HubAccessControlProviderImpl.class);
  private final String redisSessionNamespace;
  private final HubRedisProvider hubRedisProvider;
  private final HubAccessTokenGenerator hubAccessTokenGenerator;
  private final GoogleProvider googleProvider;
  private final UserDAO userDAO;

  private final String tokenName;
  private final String tokenDomain;
  private final String tokenPath;
  private final int tokenMaxAge;
  private final Set<String> internalTokens;

  @Inject
  public HubAccessControlProviderImpl(
    HubRedisProvider hubRedisProvider,
    HubAccessTokenGenerator hubAccessTokenGenerator,
    GoogleProvider googleProvider,
    UserDAO userDAO,
    Config config
  ) {
    this.hubRedisProvider = hubRedisProvider;
    this.hubAccessTokenGenerator = hubAccessTokenGenerator;
    this.googleProvider = googleProvider;
    this.userDAO = userDAO;

    redisSessionNamespace = config.getString("redis.sessionNamespace");
    tokenName = config.getString("access.tokenName");
    tokenDomain = config.getString("access.tokenDomain");
    tokenPath = config.getString("access.tokenPath");
    tokenMaxAge = config.getInt("access.tokenMaxAgeSeconds");
    internalTokens = ImmutableSet.of(config.getString("hub.internalToken"));
  }

  @Override
  public String create(UserAuth userAuth, Collection<AccountUser> accountUsers, Collection<UserRole> userRoles) throws HubAccessException {
    String accessToken = hubAccessTokenGenerator.generate();
    update(accessToken, userAuth, accountUsers, userRoles);
    return accessToken;
  }

  @Override
  public Map<String, String> update(String token, UserAuth userAuth, Collection<AccountUser> accountUsers, Collection<UserRole> userRoles) throws HubAccessException {
    Map<String, String> userMap = HubAccess.create(userAuth, accountUsers, userRoles).toMap();
    Jedis client = hubRedisProvider.getClient();
    try {
      client.hmset(computeKey(token), userMap);
      client.close();
    } catch (Exception e) {
      client.close();
      log.error("Redis database connection", e);
      throw new HubAccessException("Redis database connection", e);
    }
    return userMap;
  }


  @Override
  public void expire(String token) throws HubAccessException {
    Jedis client = hubRedisProvider.getClient();
    try {
      client.del(computeKey(token));
      client.close();
    } catch (Exception e) {
      client.close();
      throw new HubAccessException("Redis error", e);
    }
  }

  @Override
  public HubAccess get(String token) throws HubAccessException {
    if (internalTokens.contains(token)) return HubAccess.internal();

    Jedis client = hubRedisProvider.getClient();
    try {
      HubAccess hubAccess = new HubAccess(client.hgetAll(computeKey(token)));
      client.close();
      return hubAccess;
    } catch (Exception e) {
      client.close();
      throw new HubAccessException("Redis error(" + e.getClass().getName() + ")", e);
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
      throw new HubAccessException("Authentication failed", e);
    }

    Person person;
    try {
      person = googleProvider.getMe(externalAccessToken);
    } catch (Exception e) {
      log.error("Authentication failed: {}", e.getMessage());
      throw new HubAccessException("Authentication failed", e);
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

  @Override
  public String computeKey(String token) {
    return String.format("%s:%s", redisSessionNamespace, token);
  }

  /**
   HubAccess cookieSetToken value setter

   @param value to set
   @return name=value pair
   */
  private String cookieSetToken(String value) {
    return tokenName + "=" + value + ";";
  }

  /**
   HubAccess cookieSetToken cookie Domain and Path

   @return String
   */
  private String cookieSetDomainPath() {
    return (tokenDomain.isEmpty() ? "" : "Domain=" + tokenDomain + ";") +
      "Path=" + tokenPath + ";";
  }
}
