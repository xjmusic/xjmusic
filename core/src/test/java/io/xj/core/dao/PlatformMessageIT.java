// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableMap;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.message.platform.PlatformMessage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PlatformMessageIT extends FixtureIT {
  private static final long secondsPerDay = 86400L;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private PlatformMessageDAO testDAO;

  @Before
  public void setUp() throws Exception {
    reset();

    // Account "Testing" has chain "Test Print #1"
    insert(newAccount(1, "Testing"));
    insert(newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now()));

    // One platform already has a message
    insert(newPlatformMessage(12, MessageType.Info, "Consider yourself informed.", Instant.now().minusSeconds(10L * secondsPerDay)));
    insert(newPlatformMessage(3, MessageType.Warning, "Consider yourself warned, too.", Instant.now().minusSeconds(10L * secondsPerDay)));
    insert(newPlatformMessage(14, MessageType.Info, "Others were informed.", Instant.now().minusSeconds(10L * secondsPerDay)));
    insert(newPlatformMessage(15, MessageType.Info, "Even further persons were warned twice.", Instant.now().minusSeconds(10L * secondsPerDay)));

    // Instantiate the test subject
    testDAO = injector.getInstance(PlatformMessageDAO.class);
  }

  @Test
  public void create() throws Exception {
    PlatformMessage result = testDAO.create(Access.internal(), new PlatformMessage()
      .setType("Warning")
      .setBody("This is a warning"));

    assertNotNull(result);
    assertEquals(MessageType.Warning, result.getType());
    assertEquals("This is a warning", result.getBody());
  }

  @Test
  public void create_asEngineer() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Engineer",
      "accounts", "1"
    ));

    PlatformMessage result = testDAO.create(access, new PlatformMessage()
      .setType("Warning")
      .setBody("This is a warning"));

    assertNotNull(result);
    assertEquals(MessageType.Warning, result.getType());
    assertEquals("This is a warning", result.getBody());
  }

  @Test
  public void create_failsIfNotEngineer() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User,Artist",
      "accounts", "1"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("platform access is required");

    testDAO.create(access, new PlatformMessage()
      .setType("Warning")
      .setBody("This is a warning"));
  }

  @Test
  public void readOne() throws Exception {
    PlatformMessage result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(12L), result.getId());
    assertEquals(MessageType.Info, result.getType());
    assertEquals("Consider yourself informed.", result.getBody());
  }

  @Test
  public void readOne_failtIfNotExist() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(Access.internal(), BigInteger.valueOf(357L));
  }

  @Test
  public void readAllInPreviousDays() throws Exception {
    Collection<PlatformMessage> result = testDAO.readAllPreviousDays(Access.internal(), 20);

    assertEquals(4L, result.size());
  }

  @Test
  public void readAllInPreviousDays_emptyIfOutOfRange() throws Exception {
    Collection<PlatformMessage> result = testDAO.readAllPreviousDays(Access.internal(), 5);

    assertEquals(0L, result.size());
  }

  @Test
  public void readAllInPlatform_failIfPlatformNotExist() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(Access.internal(), BigInteger.valueOf(12097L));
  }

  @Test
  public void delete() throws Exception {
    testDAO.destroy(Access.internal(), BigInteger.valueOf(12L));

    assertNotExist(testDAO, BigInteger.valueOf(2L));
  }

  @Test
  public void delete_failsIfNotTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");

    testDAO.destroy(access, BigInteger.valueOf(12L));
  }

}
