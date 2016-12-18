package io.outright.xj.core.app.access;

import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;
import io.outright.xj.core.util.CSV.CSV;

import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAccess {
  private final static Logger log = LoggerFactory.getLogger(UserAccess.class);
  private static final String USER_ID_KEY = "userId";
  private static final String USER_AUTH_ID_KEY = "userAuthId";
  private static final String ACCOUNTS_KEY = "accounts";
  private static final String ROLES_KEY = "roles";
  static final String CONTEXT_KEY = "userAccess";
  //  private final static Injector injector = Guice.createInjector(new AppModule());
  private Map<String, String> innerMap;

  UserAccess(
    UserAuthRecord userAuthRecord,
    Collection<AccountUserRecord> userAccountRecords,
    Collection<UserRoleRecord> userRoleRecords
  ) {
    this.innerMap = new HashMap<>();
    this.innerMap.put(USER_ID_KEY,String.valueOf(userAuthRecord.getUserId()));
    this.innerMap.put(USER_AUTH_ID_KEY,String.valueOf(userAuthRecord.getId()));

    List<String> accounts = new ArrayList<>();
    userAccountRecords.forEach(account -> accounts.add(String.valueOf(account.getAccountId().toBigInteger())));
    this.innerMap.put(ACCOUNTS_KEY,CSV.join(accounts));

    List<String> roles = new ArrayList<>();
    userRoleRecords.forEach(role -> roles.add(role.getType()));
    this.innerMap.put(ROLES_KEY,CSV.join(roles));

    // TODO don't need to log this
    log.info("UserAccess(<fromRecords>): {}", innerMap.toString());
  }

  UserAccess(
    Map<String, String> innerMap
  ) {
    this.innerMap = innerMap;
  }

  /**
   * Determine if user access roles match resource access roles.
   *
   * @param matchRoles of the resource to match.
   * @return whether user access roles match resource access roles.
   */
  boolean matchRoles(String[] matchRoles) {
    // inefficient

    for (String matchRole : matchRoles) {
      for (String userRole: getRoles()) {
        if (userRole.equals(matchRole)) {
          return true;
        }
      }
    }
    return false;
  }

  public Map<String, String> getMap() {
    return innerMap;
  }

  public ULong getUserId() {
    return ULong.valueOf(innerMap.get(USER_ID_KEY));
  }

  public String getUserAuthId() {
    return innerMap.get(USER_AUTH_ID_KEY);
  }

  public List<String> getRoles() {
    return CSV.split(innerMap.get(ROLES_KEY));
  }

  public List<String> getAccounts() {
    return CSV.split(innerMap.get(ACCOUNTS_KEY));
  }

  public static UserAccess fromContext(ContainerRequestContext crc) {
    return (UserAccess) crc.getProperty(UserAccess.CONTEXT_KEY);
  }
}
