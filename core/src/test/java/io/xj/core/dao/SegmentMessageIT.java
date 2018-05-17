// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_message.SegmentMessage;
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

public class SegmentMessageIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule public ExpectedException failure = ExpectedException.none();
  private SegmentMessageDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    // Chain "Test Print #1" has 5 sequential segments
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("1985-02-14 12:01:00.000001"), Timestamp.valueOf("1985-02-14 12:01:32.000001"), "B major", 64, 0.73, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("1985-02-14 12:01:32.000001"), Timestamp.valueOf("1985-02-14 12:02:04.000001"), "Eb minor", 64, 0.85, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(3, 1, 2, SegmentState.Crafted, Timestamp.valueOf("1985-02-14 12:02:04.000001"), Timestamp.valueOf("1985-02-14 12:02:36.000001"), "G minor", 64, 0.30, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(4, 1, 3, SegmentState.Crafting, Timestamp.valueOf("1985-02-14 12:02:36.000001"), Timestamp.valueOf("1985-02-14 12:03:08.000001"), "D major", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment_Planned(5, 1, 4, Timestamp.valueOf("1985-02-14 12:03:08.000001"));

    // One segment already has a message
    IntegrationTestEntity.insertSegmentMessage(12, 1, MessageType.Info, "Consider yourself informed.");
    IntegrationTestEntity.insertSegmentMessage(14, 1, MessageType.Warning, "Consider yourself warned, too.");
    IntegrationTestEntity.insertSegmentMessage(15, 2, MessageType.Info, "Others were informed.");
    IntegrationTestEntity.insertSegmentMessage(16, 3, MessageType.Info, "Even further persons were warned twice.");

    // Instantiate the test subject
    testDAO = injector.getInstance(SegmentMessageDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    SegmentMessage result = testDAO.create(Access.internal(), new SegmentMessage()
      .setType(MessageType.Warning.toString())
      .setSegmentId(BigInteger.valueOf(2L))
      .setBody("This is a warning"));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getSegmentId());
    assertNotNull(result.getType());
  }

  @Test
  public void create_failsWithoutSegmentID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Segment ID is required");

    testDAO.create(Access.internal(), new SegmentMessage()
      .setType(MessageType.Warning.toString())
      .setBody("This is a warning"));
  }

  @Test
  public void readOne() throws Exception {
    SegmentMessage result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(12L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
    assertEquals(MessageType.Info, result.getType());
    assertEquals("Consider yourself informed.", result.getBody());
  }

  @Test
  public void readOne_nullIfNotExist() throws Exception {
    SegmentMessage result = testDAO.readOne(Access.internal(), BigInteger.valueOf(357L));

    assertNull(result);
  }

  @Test
  public void readOne_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    SegmentMessage result = testDAO.readOne(access, BigInteger.valueOf(12L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(12L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
    assertEquals(MessageType.Info, result.getType());
    assertEquals("Consider yourself informed.", result.getBody());
  }

  @Test
  public void readOne_emptyIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "6123"
    ));

    SegmentMessage result = testDAO.readOne(access, BigInteger.valueOf(12L));

    assertNull(result);
  }

  @Test
  public void readAllInSegment() throws Exception {
    Collection<SegmentMessage> result = testDAO.readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, (long) result.size());
  }

  @Test
  public void readAllInSegment_nullIfSegmentNotExist() throws Exception {
    SegmentMessage result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12097L));

    assertNull(result);
  }

  @Test
  public void readAllInSegment_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<SegmentMessage> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, (long) result.size());
  }

  @Test
  public void readAllInSegment_failsIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "73"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (1) segments in chain(s) to which user has access is required");

    testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));
  }

  @Test
  public void readAllInSegments() throws Exception {
    Collection<SegmentMessage> result = testDAO.readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L), BigInteger.valueOf(3L), BigInteger.valueOf(4L)));

    assertEquals(4L, (long) result.size());
  }

  @Test
  public void readAllInSegments_nullIfChainNotExist() throws Exception {
    SegmentMessage result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12097L));

    assertNull(result);
  }

  @Test
  public void readAllInSegments_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<SegmentMessage> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L), BigInteger.valueOf(3L), BigInteger.valueOf(4L)));

    assertEquals(4L, (long) result.size());
  }

  @Test
  public void readAllInSegments_failsIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "73"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (4) segments in chain(s) to which user has access is required");

    testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L), BigInteger.valueOf(3L), BigInteger.valueOf(4L)));
  }

  @Test
  public void delete() throws Exception {
    testDAO.destroy(Access.internal(), BigInteger.valueOf(12L));

    assertNull(testDAO.readOne(Access.internal(), BigInteger.valueOf(12L)));
  }

  @Test
  public void delete_failsIfNotTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("top-level access is required");

    testDAO.destroy(access, BigInteger.valueOf(12L));
  }


}
