// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.account_user.AccountUser;
import io.outright.xj.core.model.account_user.AccountUserWrapper;
import io.outright.xj.core.tables.records.AccountUserRecord;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static io.outright.xj.core.tables.AccountUser.ACCOUNT_USER;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete account users
public class AccountUserIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private AccountUserDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertAccountUser(1, 1, 2);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertAccountUser(2, 1, 3);

    // Instantiate the test subject
    testDAO = injector.getInstance(AccountUserDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertUser(5, "Jim", "jim@email.com", "http://pictures.com/jim.gif");
    AccountUserWrapper inputDataWrapper = new AccountUserWrapper()
      .setAccountUser(new AccountUser()
        .setAccountId(BigInteger.valueOf(1))
        .setUserId(BigInteger.valueOf(5))
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.get("accountId"));
    assertEquals(BigInteger.valueOf(5), result.get("userId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    AccountUserWrapper inputDataWrapper = new AccountUserWrapper()
      .setAccountUser(new AccountUser()
        .setAccountId(BigInteger.valueOf(1))
        .setUserId(BigInteger.valueOf(2))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfNotAdmin() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user"
    ));
    AccountUserWrapper inputDataWrapper = new AccountUserWrapper()
      .setAccountUser(new AccountUser()
        .setAccountId(BigInteger.valueOf(1))
        .setUserId(BigInteger.valueOf(2))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutAccountID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    AccountUserWrapper inputDataWrapper = new AccountUserWrapper()
      .setAccountUser(new AccountUser()
        .setUserId(BigInteger.valueOf(2))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutUserId() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    AccountUserWrapper inputDataWrapper = new AccountUserWrapper()
      .setAccountUser(new AccountUser()
        .setAccountId(BigInteger.valueOf(1))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(1));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("id"));
    assertEquals(ULong.valueOf(1), result.get("accountId"));
    assertEquals(ULong.valueOf(2), result.get("userId"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll_Admin() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals(2, actualResult1.get("userId"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals(3, actualResult2.get("userId"));
  }

  @Test
  public void readAll_UserCanSeeInsideOwnAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals(2, actualResult1.get("userId"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals(3, actualResult2.get("userId"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(0, actualResultList.length());
  }

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    AccountUserRecord result = IntegrationTestService.getDb()
      .selectFrom(ACCOUNT_USER)
      .where(ACCOUNT_USER.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailIfNotAdmin() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }
}
