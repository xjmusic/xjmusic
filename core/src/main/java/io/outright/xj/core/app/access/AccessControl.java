package io.outright.xj.core.app.access;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.model.account.Account;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;
import io.outright.xj.core.util.CSV.CSV;

import com.google.api.client.json.JsonFactory;
import com.google.common.collect.ImmutableList;
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

public class AccessControl {
  private static final Logger log = LoggerFactory.getLogger(AccessControl.class);
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static final String USER_ID_KEY = "userId";
  private static final String USER_AUTH_ID_KEY = "userAuthId";
  private static final String ACCOUNTS_KEY = Account.KEY_MANY;
  private static final String ROLES_KEY = Role.KEY_MANY;
  private final JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);
  static final String CONTEXT_KEY = "userAccess";
  private Map<String, String> innerMap;
  private ULong[] accountIds;
  private Boolean isAdmin;

  public AccessControl(
    UserAuthRecord userAuthRecord,
    Collection<AccountUserRecord> userAccountRoleRecords,
    Collection<UserRoleRecord> userRoleRecords
  ) {
    this.innerMap = new HashMap<>();
    this.innerMap.put(USER_ID_KEY, String.valueOf(userAuthRecord.getUserId()));
    this.innerMap.put(USER_AUTH_ID_KEY, String.valueOf(userAuthRecord.getId()));

    this.accountIds = accountIdsFromRoleRecords(userAccountRoleRecords);
    this.innerMap.put(ACCOUNTS_KEY, csvFromAccountIds(this.accountIds));

    List<String> roles = new ArrayList<>();
    for (UserRoleRecord role : userRoleRecords) {
      roles.add(role.getType());
    }
    this.innerMap.put(ROLES_KEY, CSV.join(roles));
    this.isAdmin = matchRoles(Role.ADMIN);
  }

  public AccessControl(
    Map<String, String> innerMap
  ) {
    this.innerMap = innerMap;
    if (innerMap != null) {
      this.accountIds = accountIdsFromCSV(innerMap.get(ACCOUNTS_KEY));
      this.isAdmin = matchRoles(Role.ADMIN);
    }
  }

  /**
   * Determine if user access roles match resource access roles.
   *
   * @param matchRoles of the resource to match.
   * @return whether user access roles match resource access roles.
   */
  public boolean matchRoles(String... matchRoles) {
    // inefficient?

    for (String matchRole : matchRoles) {
      for (String userRole : getRoles()) {
        if (userRole.equals(matchRole)) {
          return true;
        }
      }
    }
    return false;
  }

  /**
   * Get user ID of this access control
   *
   * @return id
   */
  public ULong getUserId() {
    return ULong.valueOf(innerMap.get(USER_ID_KEY));
  }

  /**
   * Create an access control object from request context
   *
   * @param crc container request context
   * @return access control
   */
  public static AccessControl fromContext(ContainerRequestContext crc) {
    return (AccessControl) crc.getProperty(AccessControl.CONTEXT_KEY);
  }

  /**
   * Get a representation of this access control
   *
   * @return JSON
   */
  public String toJSON() {
    try {
      return jsonFactory.toString(innerMap);
    } catch (IOException e) {
      log.error("failed JSON serialization", e);
      return "{}";
    }
  }

  /**
   * Get Accounts
   *
   * @return array of account id
   */
  public ULong[] getAccounts() {
    return accountIds;
  }

  /**
   * Is Top Level?
   *
   * @return boolean
   */
  public Boolean isTopLevel() {
    return isAdmin; // TODO: OR isWorker
  }

  /**
   * Inner map
   */
  Map<String, String> intoMap() {
    return innerMap;
  }

  /**
   * Validation
   */
  boolean valid() {
    return
      innerMap != null &&
        innerMap.containsKey(USER_ID_KEY) &&
        innerMap.containsKey(USER_AUTH_ID_KEY) &&
        innerMap.containsKey(ROLES_KEY) &&
        innerMap.containsKey(ACCOUNTS_KEY);
  }

  /**
   * Convert a collection of account role records into an array of account ids
   */
  private ULong[] accountIdsFromRoleRecords(Collection<AccountUserRecord> userAccountRoleRecords) {
    ULong[] result = new ULong[userAccountRoleRecords.size()];
    int i = 0;
    for (AccountUserRecord accountRole : userAccountRoleRecords) {
      result[i] = accountRole.getAccountId();
      ++i;
    }
    return result;
  }

  /**
   * Convert an array of account ids into a CSV
   */
  private String csvFromAccountIds(ULong[] accountIds) {
    if (accountIds.length == 0) {
      return "";
    }
    String result = accountIds[0].toString();
    if (accountIds.length > 1) {
      for (int i = 1; i < accountIds.length; i++) {
        result += "," + accountIds[i].toString();
      }
    }
    return result;
  }

  /**
   * Get a list of roles for this access control
   */
  private List<String> getRoles() {
    String roles = innerMap.get(ROLES_KEY);
    if (roles != null) {
      return CSV.split(roles);
    } else {
      return ImmutableList.of();
    }
  }

  /**
   * Get an array of account ids from a CSV string
   */
  private ULong[] accountIdsFromCSV(String csv) {
    if (csv == null || csv.length() == 0) {
      return new ULong[0];
    }
    List<String> accountIdList = CSV.split(csv);
    ULong[] result = new ULong[accountIdList.size()];
    int i = 0;
    for (String accountId : accountIdList) {
      result[i] = ULong.valueOf(accountId);
      ++i;
    }
    return result;
  }
}
