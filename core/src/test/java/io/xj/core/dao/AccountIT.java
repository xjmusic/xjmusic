// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.account.Account;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.assertj.core.util.Lists;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class AccountIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private AccountDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // Instantiate the test subject
    testDAO = injector.getInstance(AccountDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    // future test: AccountDAOImpl create()
  }

  @Test
  public void readOne_asSetToModel() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Account result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getId());
    assertEquals("bananas", result.getName());
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, Lists.newArrayList()));

    assertNotNull(result);
    assertEquals(1, result.length());

    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals("bananas", actualResult0.get("name"));
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));
    Account entity = new Account()
      .setName("jammers");

    testDAO.update(access, BigInteger.valueOf(1), entity);

    Account result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNotNull(result);
    assertEquals("jammers", result.getName());
  }

  @Test
  public void update_failsIfNotAdmin() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Account entity = new Account()
      .setName("jammers");

    failure.expect(BusinessException.class);
    failure.expectMessage("top-level access is required");

    testDAO.update(access, BigInteger.valueOf(1), entity);
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));

    Account result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test
  public void delete_failsIfNotAdmin() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("top-level access is required");

    testDAO.destroy(access, BigInteger.valueOf(1));
  }

  @Test
  public void delete_failsIfHasChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertChain(1, 1, "Test", ChainType.Preview, ChainState.Draft, Timestamp.valueOf("2009-08-12 12:17:02.527142"), Timestamp.valueOf("2009-08-12 12:17:02.527142"), null);

    failure.expect(BusinessException.class);
    failure.expectMessage("Found Chain in Account");

    testDAO.destroy(access, BigInteger.valueOf(1));
  }

  @Test
  public void delete_failsIfHasLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertLibrary(1, 1, "Testing");

    failure.expect(BusinessException.class);
    failure.expectMessage("Found Library in Account");

    testDAO.destroy(access, BigInteger.valueOf(1));
  }

  @Test
  public void delete_failsIfHasAccountUser() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertUser(1, "jim", "jim@jim.com", "http://www.jim.com/jim.png");
    IntegrationTestEntity.insertAccountUser(1, 1, 1);

    failure.expect(BusinessException.class);
    failure.expectMessage("Found User in Account");

    testDAO.destroy(access, BigInteger.valueOf(1));
  }

}
