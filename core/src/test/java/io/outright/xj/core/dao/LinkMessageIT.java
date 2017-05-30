// Copyright (c) 1985, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link_message.LinkMessage;
import io.outright.xj.core.model.message.Message;
import io.outright.xj.core.tables.records.LinkMessageRecord;

import org.jooq.Result;
import org.jooq.types.ULong;

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
import java.util.List;

import static io.outright.xj.core.Tables.LINK_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class LinkMessageIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Injector injector = Guice.createInjector(new CoreModule());
  private LinkMessageDAO testDAO;
  private List<ULong> linkIds = ImmutableList.of(ULong.valueOf(1), ULong.valueOf(2), ULong.valueOf(3), ULong.valueOf(4));

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);

    // Chain "Test Print #1" has 5 sequential links
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("1985-02-14 12:01:00.000001"), Timestamp.valueOf("1985-02-14 12:01:32.000001"), "B major", 64, 0.73, 120);
    IntegrationTestEntity.insertLink(2, 1, 1, Link.DUBBING, Timestamp.valueOf("1985-02-14 12:01:32.000001"), Timestamp.valueOf("1985-02-14 12:02:04.000001"), "Eb minor", 64, 0.85, 120);
    IntegrationTestEntity.insertLink(3, 1, 2, Link.CRAFTED, Timestamp.valueOf("1985-02-14 12:02:04.000001"), Timestamp.valueOf("1985-02-14 12:02:36.000001"), "G minor", 64, 0.30, 120);
    IntegrationTestEntity.insertLink(4, 1, 3, Link.CRAFTING, Timestamp.valueOf("1985-02-14 12:02:36.000001"), Timestamp.valueOf("1985-02-14 12:03:08.000001"), "D major", 64, 0.41, 120);
    IntegrationTestEntity.insertLink_Planned(5, 1, 4, Timestamp.valueOf("1985-02-14 12:03:08.000001"));

    // One link already has a message
    IntegrationTestEntity.insertLinkMessage(12, 1, Message.INFO, "Consider yourself informed.");
    IntegrationTestEntity.insertLinkMessage(14, 1, Message.WARN, "Consider yourself warned, too.");
    IntegrationTestEntity.insertLinkMessage(15, 2, Message.INFO, "Others were informed.");
    IntegrationTestEntity.insertLinkMessage(16, 3, Message.INFO, "Even further persons were warned twice.");

    // Instantiate the test subject
    testDAO = injector.getInstance(LinkMessageDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    LinkMessageRecord result = testDAO.create(Access.internal(), new LinkMessage()
      .setLinkId(BigInteger.valueOf(2))
      .setType(Message.WARN)
      .setBody("This is a warning"));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getLinkId());
    assertEquals(Message.WARN, result.getType());
    assertEquals("This is a warning", result.getBody());
  }

  @Test
  public void create_failsWithoutLinkID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Link ID is required");

    testDAO.create(Access.internal(), new LinkMessage()
      .setType(Message.WARN)
      .setBody("This is a warning"));
  }

  @Test
  public void readOne() throws Exception {
    LinkMessageRecord result = testDAO.readOne(Access.internal(), ULong.valueOf(12));

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(1), result.getLinkId());
    assertEquals(Message.INFO, result.getType());
    assertEquals("Consider yourself informed.", result.getBody());
  }

  @Test
  public void readOne_nullIfNotExist() throws Exception {
    LinkMessageRecord result = testDAO.readOne(Access.internal(), ULong.valueOf(357));

    assertNull(result);
  }

  @Test
  public void readOne_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    LinkMessageRecord result = testDAO.readOne(access, ULong.valueOf(12));

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(ULong.valueOf(1), result.getLinkId());
    assertEquals(Message.INFO, result.getType());
    assertEquals("Consider yourself informed.", result.getBody());
  }

  @Test
  public void readOne_emptyIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "6123"
    ));

    LinkMessageRecord result = testDAO.readOne(access, ULong.valueOf(12));

    assertNull(result);
  }

  @Test
  public void readAllInLink() throws Exception {
    Result<LinkMessageRecord> result = testDAO.readAllInLink(Access.internal(), ULong.valueOf(1));

    assertEquals(2, result.size());
  }

  @Test
  public void readAllInLink_nullIfLinkNotExist() throws Exception {
    LinkMessageRecord result = testDAO.readOne(Access.internal(), ULong.valueOf(12097));

    assertNull(result);
  }

  @Test
  public void readAllInLink_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Result<LinkMessageRecord> result = testDAO.readAllInLink(access, ULong.valueOf(1));

    assertEquals(2, result.size());
  }

  @Test
  public void readAllInLink_emptyIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "73"
    ));

    Result<LinkMessageRecord> result = testDAO.readAllInLink(access, ULong.valueOf(1));
    assertEquals(0, result.size());
  }

  @Test
  public void readAllInChain() throws Exception {
    Result<LinkMessageRecord> result = testDAO.readAllInLinks(Access.internal(), linkIds);

    assertEquals(4, result.size());
  }

  @Test
  public void readAllInChain_nullIfChainNotExist() throws Exception {
    LinkMessageRecord result = testDAO.readOne(Access.internal(), ULong.valueOf(12097));

    assertNull(result);
  }

  @Test
  public void readAllInChain_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Result<LinkMessageRecord> result = testDAO.readAllInLinks(access, linkIds);

    assertEquals(4, result.size());
  }

  @Test
  public void readAllInChain_emptyIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "73"
    ));

    Result<LinkMessageRecord> result = testDAO.readAllInLinks(access, linkIds);
    assertEquals(0, result.size());
  }

  @Test
  public void delete() throws Exception {
    testDAO.delete(Access.internal(), ULong.valueOf(12));

    assertNull(IntegrationTestService.getDb()
      .selectFrom(LINK_MESSAGE)
      .where(LINK_MESSAGE.ID.eq(ULong.valueOf(12)))
      .fetchOne());
  }

  @Test
  public void delete_failsIfNotTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("top-level access is required");

    testDAO.delete(access, ULong.valueOf(12));
  }


}
