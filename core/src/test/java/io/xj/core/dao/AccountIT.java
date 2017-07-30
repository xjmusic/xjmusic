// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.account.Account;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.tables.records.AccountRecord;
import io.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Timestamp;

import static io.xj.core.Tables.ACCOUNT;
import static junit.framework.Assert.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector = Guice.createInjector(new CoreModule());
  private AccountDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

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
    // TODO: test AccountDAOImpl create()
  }

  @Test
  public void readOne_asRecordSetToModel() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Account result = new Account().setFromRecord(testDAO.readOne(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.getId());
    assertEquals("bananas", result.getName());
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access));

    assertNotNull(result);
    assertEquals(1, result.length());

    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals("bananas", actualResult0.get("name"));
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin",
      "accounts", "1"
    ));
    Account entity = new Account()
      .setName("jammers");

    testDAO.update(access, ULong.valueOf(1), entity);

    AccountRecord result = IntegrationTestService.getDb()
      .selectFrom(ACCOUNT)
      .where(ACCOUNT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("jammers", result.getName());
  }

  @Test
  public void update_failsIfNotAdmin() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));
    Account entity = new Account()
      .setName("jammers");

    failure.expect(BusinessException.class);
    failure.expectMessage("top-level access is required");

    testDAO.update(access, ULong.valueOf(1), entity);
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    AccountRecord result = IntegrationTestService.getDb()
      .selectFrom(ACCOUNT)
      .where(ACCOUNT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test
  public void delete_failsIfNotAdmin() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("top-level access is required");

    testDAO.delete(access, ULong.valueOf(1));
  }

  @Test
  public void delete_failsIfHasChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertChain(1, 1, "Test", ChainType.Preview, ChainState.Draft, Timestamp.valueOf("2009-08-12 12:17:02.527142"), Timestamp.valueOf("2009-08-12 12:17:02.527142"));

    failure.expect(BusinessException.class);
    failure.expectMessage("Found Chain in Account");

    testDAO.delete(access, ULong.valueOf(1));
  }

  @Test
  public void delete_failsIfHasLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertLibrary(1, 1, "Testing");

    failure.expect(BusinessException.class);
    failure.expectMessage("Found Library in Account");

    testDAO.delete(access, ULong.valueOf(1));
  }

  @Test
  public void delete_failsIfHasAccountUser() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertUser(1, "jim", "jim@jim.com", "http://www.jim.com/jim.png");
    IntegrationTestEntity.insertAccountUser(1, 1, 1);

    failure.expect(BusinessException.class);
    failure.expectMessage("Found User in Account");

    testDAO.delete(access, ULong.valueOf(1));
  }

}
