package io.outright.xj.core.app.access;

import io.outright.xj.core.app.CoreModule;
import io.outright.xj.core.external.google.GoogleModule;
import io.outright.xj.core.tables.records.AccountUserRoleRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;
import io.outright.xj.core.util.CSV.CSV;

import com.google.api.client.json.JsonFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.container.ContainerRequestContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserAccessModel {
  private static final Logger log = LoggerFactory.getLogger(UserAccessModel.class);
  private static final Injector injector = Guice.createInjector(new CoreModule(), new GoogleModule());
  private static final String USER_ID_KEY = "userId";
  private static final String USER_AUTH_ID_KEY = "userAuthId";
  private static final String ACCOUNTS_KEY = "accountRoles";
  private static final String ROLES_KEY = "roles";
  private final JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);
  static final String CONTEXT_KEY = "userAccess";
  private Map<String, String> innerMap;

  UserAccessModel(
    UserAuthRecord userAuthRecord,
    Collection<AccountUserRoleRecord> userAccountRoleRecords,
    Collection<UserRoleRecord> userRoleRecords
  ) {
    this.innerMap = new HashMap<>();
    this.innerMap.put(USER_ID_KEY, String.valueOf(userAuthRecord.getUserId()));
    this.innerMap.put(USER_AUTH_ID_KEY, String.valueOf(userAuthRecord.getId()));

    List<String> accounts = new ArrayList<>();
    for (AccountUserRoleRecord accountRole : userAccountRoleRecords) {
      accounts.add(
        String.valueOf(accountRole.getAccountId().toBigInteger()) +
          ":" + accountRole.getType()
      );
    }
    this.innerMap.put(ACCOUNTS_KEY, CSV.join(accounts));

    List<String> roles = new ArrayList<>();
    for (UserRoleRecord role : userRoleRecords) {
      roles.add(role.getType());
    }
    this.innerMap.put(ROLES_KEY, CSV.join(roles));
  }

  UserAccessModel(
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

  public static UserAccessModel fromContext(ContainerRequestContext crc) {
    return (UserAccessModel) crc.getProperty(UserAccessModel.CONTEXT_KEY);
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
}
