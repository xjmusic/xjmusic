// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.app.access;

import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.db.RedisDatabaseProvider;
import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;
import io.outright.xj.core.util.token.TokenGenerator;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.NewCookie;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class AccessControlProviderImpl implements AccessControlProvider {
  private final static Logger log = LoggerFactory.getLogger(AccessControlProviderImpl.class);
  private final RedisDatabaseProvider redisDatabaseProvider;
  private final TokenGenerator tokenGenerator;

  private final String tokenName = Config.accessTokenName();
  private final String tokenDomain = Config.accessTokenDomain();
  private final String tokenPath = Config.accessTokenPath();
  private final String tokenMaxAge = String.valueOf(Config.accessTokenMaxAge());

  @Inject
  public AccessControlProviderImpl(
    RedisDatabaseProvider redisDatabaseProvider,
    TokenGenerator tokenGenerator
  ) {
    this.redisDatabaseProvider = redisDatabaseProvider;
    this.tokenGenerator = tokenGenerator;
  }

  @Override
  public String create(UserAuthRecord userAuthRecord, Collection<AccountUserRecord> userAccountRoleRecords, Collection<UserRoleRecord> userRoleRecords) throws AccessException {
    String accessToken = tokenGenerator.generate();
    update(accessToken,userAuthRecord,userAccountRoleRecords,userRoleRecords);
    return accessToken;
  }

  @Override
  public Map<String, String> update(String accessToken, UserAuthRecord userAuthRecord, Collection<AccountUserRecord> userAccountRoleRecords, Collection<UserRoleRecord> userRoleRecords) throws AccessException {
    AccessControl accessControl = new AccessControl(userAuthRecord, userAccountRoleRecords, userRoleRecords);
    Map<String, String> userMap = accessControl.intoMap();
    try {
      redisDatabaseProvider.getClient().hmset(accessToken, userMap);
    } catch (ConfigException e) {
      log.error("Redis database connection is not get properly!", e);
      throw new AccessException("Redis database connection is not get properly: " + e);
    }
    return userMap;
  }


  @Override
  public void expire(String accessToken) throws DatabaseException {
    try {
      redisDatabaseProvider.getClient().del(accessToken);
    } catch (Exception e) {
      throw new DatabaseException("Redis error: " + e.toString());
    }
  }

  @Override
  public AccessControl get(String accessToken) throws DatabaseException {
    try {
      return new AccessControl(redisDatabaseProvider.getClient().hgetAll(accessToken));
    } catch (Exception e) {
      throw new DatabaseException("Redis error(" + e.getClass().getName() + "): " + Arrays.toString(e.getStackTrace()));
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

  /**
   * Access cookieSetToken value setter
   *
   * @param value to set
   * @return name=value pair
   */
  private String cookieSetToken(String value) {
    return tokenName + "=" + value + ";";
  }

  /**
   * Access cookieSetToken cookie Domain and Path
   *
   * @return String
   */
  private String cookieSetDomainPath() {
    return (tokenDomain.length() > 0 ? "Domain=" + tokenDomain + ";" : "") +
      "Path=" + tokenPath + ";";
  }
}
