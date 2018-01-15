// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.model.user_auth.UserAuthType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableList;
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

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// future test: permissions of different users to readMany vs. create vs. update or delete link memes
public class LinkMemeIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private LinkMemeDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    // Chain "Test Print #1" has 5 sequential links
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, LinkState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(3, 1, 2, LinkState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(4, 1, 3, LinkState.Crafting, Timestamp.valueOf("2017-02-14 12:02:36.000001"), Timestamp.valueOf("2017-02-14 12:03:08.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink_Planned(5, 1, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.User);
    IntegrationTestEntity.insertUserRole(2, 2, UserRoleType.Admin);
    IntegrationTestEntity.insertAccountUser(3, 1, 2);
    IntegrationTestEntity.insertUserAuth(102, 2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222");
    IntegrationTestEntity.insertUserAccessToken(2, 102, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(4, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(5, 1, 3);

    // Bill has a "user" role but no account membership
    IntegrationTestEntity.insertUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertUserRole(6, 4, UserRoleType.User);

    // Link #1 memes
    IntegrationTestEntity.insertLinkMeme(1, 1, "Cool");
    IntegrationTestEntity.insertLinkMeme(2, 1, "Dark");

    // Link #2 memes
    IntegrationTestEntity.insertLinkMeme(3, 2, "Warm");

    // Instantiate the test subject
    testDAO = injector.getInstance(LinkMemeDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    LinkMeme inputData = new LinkMeme()
      .setLinkId(BigInteger.valueOf(1))
      .setName("  !!2gnarLY    ");

    LinkMeme result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getLinkId());
    assertEquals("Gnarly", result.getName());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLinkID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    LinkMeme inputData = new LinkMeme()
      .setName("gnarly");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    LinkMeme inputData = new LinkMeme()
      .setLinkId(BigInteger.valueOf(1));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    LinkMeme result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getLinkId());
    assertEquals("Dark", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    LinkMeme result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("Cool", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("Dark", result2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void readAllInLinks() throws Exception {
    Collection<LinkMeme> result = testDAO.readAllInLinks(Access.internal(), ImmutableList.of(BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(4)));

    assertEquals(3, result.size());
  }

  @Test
  public void readAllInLinks_nullIfChainNotExist() throws Exception {
    LinkMeme result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12097));

    assertNull(result);
  }

  @Test
  public void readAllInLinks_okIfUserInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<LinkMeme> result = testDAO.readAllInLinks(access, ImmutableList.of(BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(4)));

    assertEquals(3, result.size());
  }

  @Test
  public void readAllInLinks_failsIfUserNotInAccount() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "73"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (4) links in chain(s) to which user has access is required");

    testDAO.readAllInLinks(access, ImmutableList.of(BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(4)));
  }

  @Test
  public void delete() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    testDAO.destroy(access, BigInteger.valueOf(1));

    LinkMeme result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }
}
