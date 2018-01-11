// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.account_user.AccountUser;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// future test: permissions of different users to readMany vs. create vs. update or delete account users
public class AccountUserIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
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
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertUser(5, "Jim", "jim@email.com", "http://pictures.com/jim.gif");
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1))
      .setUserId(BigInteger.valueOf(5));

    AccountUser result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getAccountId());
    assertEquals(BigInteger.valueOf(5), result.getUserId());
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfAlreadyExists() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1))
      .setUserId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailIfNotAdmin() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User"
    ));
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1))
      .setUserId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutAccountID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    AccountUser inputData = new AccountUser()
      .setUserId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutUserId() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    AccountUser inputData = new AccountUser()
      .setAccountId(BigInteger.valueOf(1));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    AccountUser result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getAccountId());
    assertEquals(BigInteger.valueOf(2), result.getUserId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    AccountUser result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll_Admin() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals(2, result1.get("userId"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals(3, result2.get("userId"));
  }

  @Test
  public void readAll_UserCanSeeInsideOwnAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals(2, result1.get("userId"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals(3, result2.get("userId"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, BigInteger.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void delete() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.delete(access, BigInteger.valueOf(1));

    AccountUser result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailIfNotAdmin() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User"
    ));

    testDAO.delete(access, BigInteger.valueOf(1));
  }
}
