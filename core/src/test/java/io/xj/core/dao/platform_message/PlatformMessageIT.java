// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao.platform_message;

import com.google.common.collect.ImmutableList;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.PlatformMessageDAO;
import io.xj.core.entity.MessageType;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Account;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.PlatformMessage;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class PlatformMessageIT extends FixtureIT {
  private static final long secondsPerDay = 86400L;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private PlatformMessageDAO testDAO;
  private PlatformMessage platformMessage1;

  @Before
  public void setUp() throws Exception {
    reset();

    // Account "Testing" has chain "Test Print #1"
    account1 = insert(Account.create("Testing"));
    chain3 = insert(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));

    // One platform already has a message
    platformMessage1 = insert(PlatformMessage.create(MessageType.Info, "Consider yourself informed.", Instant.now().minusSeconds(10L * secondsPerDay)));
    insert(PlatformMessage.create(MessageType.Warning, "Consider yourself warned, too.", Instant.now().minusSeconds(10L * secondsPerDay)));
    insert(PlatformMessage.create(MessageType.Info, "Others were informed.", Instant.now().minusSeconds(10L * secondsPerDay)));
    insert(PlatformMessage.create(MessageType.Info, "Even further persons were warned twice.", Instant.now().minusSeconds(10L * secondsPerDay)));

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
    Access access = Access.create(ImmutableList.of(Account.create()), "User,Engineer");

    PlatformMessage result = testDAO.create(access, new PlatformMessage()
      .setType("Warning")
      .setBody("This is a warning"));

    assertNotNull(result);
    assertEquals(MessageType.Warning, result.getType());
    assertEquals("This is a warning", result.getBody());
  }

  @Test
  public void create_failsIfNotEngineer() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User,Artist");

    failure.expect(CoreException.class);
    failure.expectMessage("platform access is required");

    testDAO.create(access, new PlatformMessage()
      .setType("Warning")
      .setBody("This is a warning"));
  }

  @Test
  public void readOne() throws Exception {
    PlatformMessage result = testDAO.readOne(Access.internal(), platformMessage1.getId());

    assertNotNull(result);
    assertEquals(platformMessage1.getId(), result.getId());
    assertEquals(MessageType.Info, result.getType());
    assertEquals("Consider yourself informed.", result.getBody());
  }

  @Test
  public void readOne_failtIfNotExist() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(Access.internal(), UUID.randomUUID());
  }

  @Test
  public void readAllInPreviousDays() throws Exception {
    Collection<PlatformMessage> result = testDAO.readAllPreviousDays(Access.internal(), 20);

    assertEquals(4L, result.size());
  }

  @Test
  public void readAllInPreviousDays_emptyIfOutOfRange() throws Exception {
    // all the messages are 10 days ago, so this should return zero messages
    Collection<PlatformMessage> result = testDAO.readAllPreviousDays(Access.internal(), 5);

    assertEquals(0L, result.size());
  }

  @Test
  public void readAllInPlatform_failIfPlatformNotExist() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(Access.internal(), UUID.randomUUID());
  }

  @Test
  public void delete() throws Exception {
    testDAO.destroy(Access.internal(), platformMessage1.getId());

    assertNotExist(testDAO, platformMessage1.getId());
  }

  @Test
  public void delete_failsIfNotTopLevelAccess() throws Exception {
    Access access = Access.create(ImmutableList.of(account1), "User");

    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");

    testDAO.destroy(access, platformMessage1.getId());
  }

}
