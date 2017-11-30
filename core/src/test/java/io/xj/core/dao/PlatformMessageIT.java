// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.platform_message.PlatformMessage;
import io.xj.core.tables.records.PlatformMessageRecord;
import io.xj.core.timestamp.TimestampUTC;

import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Timestamp;

import static io.xj.core.Tables.PLATFORM_MESSAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PlatformMessageIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private PlatformMessageDAO testDAO;
  private static final long secondsPerDay = 86400L;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    // One platform already has a message
    IntegrationTestEntity.insertPlatformMessage(12, MessageType.Info, "Consider yourself informed.", TimestampUTC.nowMinusSeconds(10 * secondsPerDay));
    IntegrationTestEntity.insertPlatformMessage(3, MessageType.Warning, "Consider yourself warned, too.", TimestampUTC.nowMinusSeconds(10 * secondsPerDay));
    IntegrationTestEntity.insertPlatformMessage(14, MessageType.Info, "Others were informed.", TimestampUTC.nowMinusSeconds(10 * secondsPerDay));
    IntegrationTestEntity.insertPlatformMessage(15, MessageType.Info, "Even further persons were warned twice.", TimestampUTC.nowMinusSeconds(10 * secondsPerDay));

    // Instantiate the test subject
    testDAO = injector.getInstance(PlatformMessageDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    PlatformMessageRecord result = testDAO.create(Access.internal(), new PlatformMessage()
      .setType("Warning")
      .setBody("This is a warning"));

    assertNotNull(result);
    assertEquals(MessageType.Warning.toString(), result.getType());
    assertEquals("This is a warning", result.getBody());
  }

  @Test
  public void create_asEngineer() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user,engineer",
      "accounts", "1"
    ));

    PlatformMessageRecord result = testDAO.create(access, new PlatformMessage()
      .setType("Warning")
      .setBody("This is a warning"));

    assertNotNull(result);
    assertEquals(MessageType.Warning.toString(), result.getType());
    assertEquals("This is a warning", result.getBody());
  }

  @Test
  public void create_failsIfNotEngineer() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user,artist",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("platform access is required");

    testDAO.create(access, new PlatformMessage()
      .setType("Warning")
      .setBody("This is a warning"));
  }

  @Test
  public void readOne() throws Exception {
    PlatformMessageRecord result = testDAO.readOne(Access.internal(), ULong.valueOf(12));

    assertNotNull(result);
    assertEquals(ULong.valueOf(12), result.getId());
    assertEquals(MessageType.Info.toString(), result.getType());
    assertEquals("Consider yourself informed.", result.getBody());
  }

  @Test
  public void readOne_nullIfNotExist() throws Exception {
    PlatformMessageRecord result = testDAO.readOne(Access.internal(), ULong.valueOf(357));

    assertNull(result);
  }

  @Test
  public void readAllInPreviousDays() throws Exception {
    Result<PlatformMessageRecord> result = testDAO.readAllPreviousDays(Access.internal(), 20);

    assertEquals(4, result.size());
  }

  @Test
  public void readAllInPreviousDays_emptyIfOutOfRange() throws Exception {
    Result<PlatformMessageRecord> result = testDAO.readAllPreviousDays(Access.internal(), 5);

    assertEquals(0, result.size());
  }

  @Test
  public void readAllInPlatform_nullIfPlatformNotExist() throws Exception {
    PlatformMessageRecord result = testDAO.readOne(Access.internal(), ULong.valueOf(12097));

    assertNull(result);
  }

  @Test
  public void delete() throws Exception {
    testDAO.delete(Access.internal(), ULong.valueOf(12));

    assertNull(IntegrationTestService.getDb()
      .selectFrom(PLATFORM_MESSAGE)
      .where(PLATFORM_MESSAGE.ID.eq(ULong.valueOf(12)))
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
