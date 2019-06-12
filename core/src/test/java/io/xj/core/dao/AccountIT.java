// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableMap;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account.Account;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import org.assertj.core.util.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AccountIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountDAO testDAO;

  @Before
  public void setUp() throws Exception {
    reset();

    // Account "bananas"
    insert(newAccount(1, "bananas"));

    // Instantiate the test subject
    testDAO = injector.getInstance(AccountDAO.class);
  }

  @Test
  public void create() {
    // future test: AccountDAOImpl create()
  }

  @Test
  public void readOne_asSetToModel() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Account result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getId());
    assertEquals("bananas", result.getName());
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Account> results = testDAO.readMany(access, Lists.newArrayList());

    assertNotNull(results);
    assertEquals(1L, results.size());

    Account result0 = results.iterator().next();
    assertEquals("bananas", result0.getName());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));
    Account entity = new Account()
      .setName("jammers");

    testDAO.update(access, BigInteger.valueOf(1L), entity);

    Account result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
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

    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");

    testDAO.update(access, BigInteger.valueOf(1L), entity);
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  @Test
  public void delete_failsIfNotAdmin() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }

  @Test
  public void delete_failsIfHasChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));
    insert(newChain(1, 1, "Test", ChainType.Preview, ChainState.Draft, Instant.parse("2009-08-12T12:17:02.527142Z"), Instant.parse("2009-08-12T12:17:02.527142Z"), null, now()));

    failure.expect(CoreException.class);
    failure.expectMessage("Found Chain in Account");

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }

  @Test
  public void delete_failsIfHasLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));
    insert(newLibrary(1, 1, "Testing", now()));

    failure.expect(CoreException.class);
    failure.expectMessage("Found Library in Account");

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }

  @Test
  public void delete_failsIfHasAccountUser() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));
    insert(newUser(1, "jim", "jim@jim.com", "http://www.jim.com/jim.png"));
    insert(newAccountUser(1, 1));

    failure.expect(CoreException.class);
    failure.expectMessage("Found User in Account");

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }

}
