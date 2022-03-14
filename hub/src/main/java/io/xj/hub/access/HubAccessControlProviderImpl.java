// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.access;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.services.plus.model.Person;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import io.xj.hub.manager.UserManager;
import io.xj.hub.enums.UserAuthType;
import io.xj.hub.persistence.HubRedisProvider;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.ws.rs.core.NewCookie;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

class HubAccessControlProviderImpl implements HubAccessControlProvider {
  private static final Logger LOG = LoggerFactory.getLogger(HubAccessControlProviderImpl.class);
  private final String redisSessionNamespace;
  private final HubRedisProvider hubRedisProvider;
  private final HubAccessTokenGenerator hubAccessTokenGenerator;
  private final GoogleProvider googleProvider;
  private final UserManager userManager;

  private final String tokenName;
  private final String tokenDomain;
  private final String tokenPath;
  private final int tokenMaxAge;
  private final Set<String> internalTokens;
  private final EntityFactory entityFactory;

  @Inject
  public HubAccessControlProviderImpl(
    HubRedisProvider hubRedisProvider,
    HubAccessTokenGenerator hubAccessTokenGenerator,
    GoogleProvider googleProvider,
    UserManager userManager,
    Environment env,
    EntityFactory entityFactory
  ) {
    this.hubRedisProvider = hubRedisProvider;
    this.hubAccessTokenGenerator = hubAccessTokenGenerator;
    this.googleProvider = googleProvider;
    this.userManager = userManager;

    redisSessionNamespace = env.getRedisSessionNamespace();
    tokenName = env.getAccessTokenName();
    tokenDomain = env.getAccessTokenDomain();
    tokenPath = env.getAccessTokenPath();
    tokenMaxAge = env.getAccessTokenMaxAgeSeconds();
    internalTokens = ImmutableSet.of(env.getIngestTokenValue());
    this.entityFactory = entityFactory;
  }

  @Override
  public String create(User user, UserAuth userAuth, Collection<AccountUser> accountUsers) throws HubAccessException {
    String accessToken = hubAccessTokenGenerator.generate();
    HubAccess access = HubAccess.create(user, userAuth, accountUsers.stream().map(AccountUser::getAccountId).collect(Collectors.toList()));
    Jedis client = hubRedisProvider.getClient();
    try {
      client.set(computeKey(accessToken), entityFactory.serialize(access));
      client.close();
    } catch (Exception e) {
      client.close();
      LOG.error("Redis database connection", e);
      throw new HubAccessException("Redis database connection", e);
    }
    return accessToken;
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
      HubAccess access = entityFactory.deserialize(HubAccess.class, client.get(computeKey(token)));
      client.close();
      if (Objects.isNull(access)) throw new HubAccessException("Token does not exist!");
      return access;
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
      LOG.error("Authentication failed: {}", e.getMessage());
      throw new HubAccessException("Authentication failed", e);
    }

    Person person;
    try {
      person = googleProvider.getMe(externalAccessToken);
    } catch (Exception e) {
      LOG.error("Authentication failed: {}", e.getMessage());
      throw new HubAccessException("Authentication failed", e);
    }

    return userManager.authenticate(
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
