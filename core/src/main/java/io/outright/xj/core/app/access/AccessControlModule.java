package io.outright.xj.core.app.access;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.model.account.Account;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;
import io.outright.xj.core.util.CSV.CSV;

import org.jooq.types.ULong;

import com.google.api.client.json.JsonFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccessControlModule {
  private static final Logger log = LoggerFactory.getLogger(AccessControlModule.class);
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static final String USER_ID_KEY = "userId";
  private static final String USER_AUTH_ID_KEY = "userAuthId";
  private static final String ACCOUNTS_KEY = Account.KEY_MANY;
  private static final String ROLES_KEY = Role.KEY_MANY;
  private final JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);
  static final String CONTEXT_KEY = "userAccess";
  private Map<String, String> innerMap;
  private List<String> accountIds;

  AccessControlModule(
    UserAuthRecord userAuthRecord,
    Collection<AccountUserRecord> userAccountRoleRecords,
    Collection<UserRoleRecord> userRoleRecords
  ) {
    this.innerMap = new HashMap<>();
    this.innerMap.put(USER_ID_KEY, String.valueOf(userAuthRecord.getUserId()));
    this.innerMap.put(USER_AUTH_ID_KEY, String.valueOf(userAuthRecord.getId()));

    this.accountIds = new ArrayList<>();
    for (AccountUserRecord accountRole : userAccountRoleRecords) {
      accountIds.add(accountRole.getAccountId().toString());
    }
    this.innerMap.put(ACCOUNTS_KEY, CSV.join(accountIds));

    List<String> roles = new ArrayList<>();
    for (UserRoleRecord role : userRoleRecords) {
      roles.add(role.getType());
    }
    this.innerMap.put(ROLES_KEY, CSV.join(roles));
  }

  AccessControlModule(
    Map<String, String> innerMap
  ) {
    this.innerMap = innerMap;
    this.accountIds = CSV.split(innerMap.get(ACCOUNTS_KEY));
  }

  /**
   * Determine if user access roles match resource access roles.
   *
   * @param matchRoles of the resource to match.
   * @return whether user access roles match resource access roles.
   */
  public boolean matchRoles(String[] matchRoles) {
    // inefficient

    for (String matchRole : matchRoles) {
      for (String userRole : getRoles()) {
        if (userRole.equals(matchRole)) {
          return true;
        }
      }
    }
    return false;
  }

  Map<String, String> getMap() {
    return innerMap;
  }

  public ULong getUserId() {
    return ULong.valueOf(innerMap.get(USER_ID_KEY));
  }

//  public String getUserAuthId() {
//    return innerMap.get(USER_AUTH_ID_KEY);
//  }

  private List<String> getRoles() {
    return CSV.split(innerMap.get(ROLES_KEY));
  }

//  public List<String> getAccounts() {
//    return CSV.split(innerMap.get(ACCOUNTS_KEY));
//  }

  public static AccessControlModule fromContext(ContainerRequestContext crc) {
    return (AccessControlModule) crc.getProperty(AccessControlModule.CONTEXT_KEY);
  }

  boolean valid() {
    return
      innerMap.containsKey(USER_ID_KEY) &&
        innerMap.containsKey(USER_AUTH_ID_KEY) &&
        innerMap.containsKey(ROLES_KEY) &&
        innerMap.containsKey(ACCOUNTS_KEY);
  }

  public String toJSON() {
    try {
      return jsonFactory.toString(innerMap);
    } catch (IOException e) {
      log.error("failed JSON serialization", e);
      return "{}";
    }
  }

  public boolean isGrantedAccount(String accountId) {
    return accountIds.contains(accountId);
  }
}
