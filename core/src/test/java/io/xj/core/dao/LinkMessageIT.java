// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.model.message.MessageType;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

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

public class LinkMessageIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule public ExpectedException failure = ExpectedException.none();
  private LinkMessageDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    // Chain "Test Print #1" has 5 sequential links
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("1985-02-14 12:01:00.000001"), Timestamp.valueOf("1985-02-14 12:01:32.000001"), "B major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, LinkState.Dubbing, Timestamp.valueOf("1985-02-14 12:01:32.000001"), Timestamp.valueOf("1985-02-14 12:02:04.000001"), "Eb minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(3, 1, 2, LinkState.Crafted, Timestamp.valueOf("1985-02-14 12:02:04.000001"), Timestamp.valueOf("1985-02-14 12:02:36.000001"), "G minor", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(4, 1, 3, LinkState.Crafting, Timestamp.valueOf("1985-02-14 12:02:36.000001"), Timestamp.valueOf("1985-02-14 12:03:08.000001"), "D major", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink_Planned(5, 1, 4, Timestamp.valueOf("1985-02-14 12:03:08.000001"));

    // One link already has a message
    IntegrationTestEntity.insertLinkMessage(12, 1, MessageType.Info, "Consider yourself informed.");
    IntegrationTestEntity.insertLinkMessage(14, 1, MessageType.Warning, "Consider yourself warned, too.");
    IntegrationTestEntity.insertLinkMessage(15, 2, MessageType.Info, "Others were informed.");
    IntegrationTestEntity.insertLinkMessage(16, 3, MessageType.Info, "Even further persons were warned twice.");

    // Instantiate the test subject
    testDAO = injector.getInstance(LinkMessageDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    LinkMessage result = testDAO.create(Access.internal(), new LinkMessage()
      .setType(MessageType.Warning.toString())
      .setLinkId(BigInteger.valueOf(2))
      .setBody("This is a warning"));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getLinkId());
    assertNotNull(result.getType());
  }

  @Test
  public void create_failsWithoutLinkID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Link ID is required");

    testDAO.create(Access.internal(), new LinkMessage()
      .setType(MessageType.Warning.toString())
      .setBody("This is a warning"));
  }

  @Test
  public void readOne() throws Exception {
    LinkMessage result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(12), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getLinkId());
    assertEquals(MessageType.Info, result.getType());
    assertEquals("Consider yourself informed.", result.getBody());
  }

  @Test
  public void readOne_nullIfNotExist() throws Exception {
    LinkMessage result = testDAO.readOne(Access.internal(), BigInteger.valueOf(357));

    assertNull(result);
  }

  @Test
  public void readOne_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    LinkMessage result = testDAO.readOne(access, BigInteger.valueOf(12));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(12), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getLinkId());
    assertEquals(MessageType.Info, result.getType());
    assertEquals("Consider yourself informed.", result.getBody());
  }

  @Test
  public void readOne_emptyIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "6123"
    ));

    LinkMessage result = testDAO.readOne(access, BigInteger.valueOf(12));

    assertNull(result);
  }

  @Test
  public void readAllInLink() throws Exception {
    Collection<LinkMessage> result = testDAO.readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(1)));

    assertEquals(2, result.size());
  }

  @Test
  public void readAllInLink_nullIfLinkNotExist() throws Exception {
    LinkMessage result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12097));

    assertNull(result);
  }

  @Test
  public void readAllInLink_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<LinkMessage> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1)));

    assertEquals(2, result.size());
  }

  @Test
  public void readAllInLink_failsIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "73"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (1) links in chain(s) to which user has access is required");

    testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1)));
  }

  @Test
  public void readAllInLinks() throws Exception {
    Collection<LinkMessage> result = testDAO.readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(4)));

    assertEquals(4, result.size());
  }

  @Test
  public void readAllInLinks_nullIfChainNotExist() throws Exception {
    LinkMessage result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12097));

    assertNull(result);
  }

  @Test
  public void readAllInLinks_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<LinkMessage> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(4)));

    assertEquals(4, result.size());
  }

  @Test
  public void readAllInLinks_failsIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "73"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (4) links in chain(s) to which user has access is required");

    testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1), BigInteger.valueOf(2), BigInteger.valueOf(3), BigInteger.valueOf(4)));
  }

  @Test
  public void delete() throws Exception {
    testDAO.destroy(Access.internal(), BigInteger.valueOf(12));

    assertNull(testDAO.readOne(Access.internal(), BigInteger.valueOf(12)));
  }

  @Test
  public void delete_failsIfNotTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("top-level access is required");

    testDAO.destroy(access, BigInteger.valueOf(12));
  }


}
