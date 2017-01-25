package io.outright.xj.core.integration;

import io.outright.xj.core.tables.records.AccountRecord;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.core.tables.records.UserAccessTokenRecord;
import io.outright.xj.core.tables.records.UserAuthRecord;
import io.outright.xj.core.tables.records.UserRecord;
import io.outright.xj.core.tables.records.UserRoleRecord;

import org.jooq.types.ULong;

import static io.outright.xj.core.Tables.ACCOUNT;
import static io.outright.xj.core.Tables.ACCOUNT_USER;
import static io.outright.xj.core.Tables.USER;
import static io.outright.xj.core.Tables.USER_ACCESS_TOKEN;
import static io.outright.xj.core.Tables.USER_AUTH;
import static io.outright.xj.core.Tables.USER_ROLE;

public interface IntegrationTestEntity {

  static void insertUserAuth(Integer id, Integer userId, String type, String externalAccessToken, String externalRefreshToken, String externalAccount) {
    UserAuthRecord record = IntegrationTestService.getDb().newRecord(USER_AUTH);
    record.setId(ULong.valueOf(id));
    record.setUserId(ULong.valueOf(userId));
    record.setType(type);
    record.setExternalAccessToken(externalAccessToken);
    record.setExternalRefreshToken(externalRefreshToken);
    record.setExternalAccount(externalAccount);
    record.store();
  }

  static void insertUser(Integer id, String name, String email, String avatarUrl) {
    UserRecord record = IntegrationTestService.getDb().newRecord(USER);
    record.setId(ULong.valueOf(id));
    record.setName(name);
    record.setEmail(email);
    record.setAvatarUrl(avatarUrl);
    record.store();
  }

  static void insertUserRole(Integer userId, String type) {
    UserRoleRecord record = IntegrationTestService.getDb().newRecord(USER_ROLE);
    record.setUserId(ULong.valueOf(userId));
    record.setType(type);
    record.store();
  }

  static void insertAccountUser(Integer accountId, Integer userId) {
    AccountUserRecord record = IntegrationTestService.getDb().newRecord(ACCOUNT_USER);
    record.setAccountId(ULong.valueOf(accountId));
    record.setUserId(ULong.valueOf(userId));
    record.store();
  }

  static void insertAccount(Integer id, String name) {
    AccountRecord record = IntegrationTestService.getDb().newRecord(ACCOUNT);
    record.setId(ULong.valueOf(id));
    record.setName(name);
    record.store();
  }

  static void insertUserAccessToken(int userId, int userAuthId, String accessToken) {
    UserAccessTokenRecord record = IntegrationTestService.getDb().newRecord(USER_ACCESS_TOKEN);
    record.setUserId(ULong.valueOf(userId));
    record.setUserAuthId(ULong.valueOf(userAuthId));
    record.setAccessToken(accessToken);
    record.store();
  }
}
