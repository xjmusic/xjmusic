// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_pattern_meme.SequencePatternMeme;
import io.xj.core.model.user_auth.UserAuthType;
import io.xj.core.model.user_role.UserRoleType;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// future test: permissions of different users to readMany vs. create vs. update or delete pattern memes
public class SequencePatternMemeIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private SequencePatternMemeDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, UserRoleType.User);
    IntegrationTestEntity.insertUserRole(2, UserRoleType.Admin);
    IntegrationTestEntity.insertAccountUser(1, 2);
    IntegrationTestEntity.insertUserAuth(2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222");
    IntegrationTestEntity.insertUserAccessToken(2, UserAuthType.Google, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Bill has a "user" role but no account membership
    IntegrationTestEntity.insertUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertUserRole(4, UserRoleType.User);

    // Library "palm tree" has sequence "leaves"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Main, SequenceState.Published, "leaves", 0.342, "C#", 120.4);

    // Sequence "leaves" has pattern "growth" and pattern "decay"
    IntegrationTestEntity.insertPattern(1, 1, PatternType.Main, PatternState.Published, 16, "growth", 0.342, "C#", 120.4);
    IntegrationTestEntity.insertSequencePattern(110, 1, 1, 0);
    IntegrationTestEntity.insertPattern(2, 1, PatternType.Main, PatternState.Published, 16, "decay", 0.25, "F#", 110.3);
    IntegrationTestEntity.insertSequencePattern(211, 1, 2, 1);

    // Pattern "growth" has memes "ants" and "mold"
    IntegrationTestEntity.insertSequencePatternMeme(110, "Gravel");
    IntegrationTestEntity.insertSequencePatternMeme(110, "Fuzz");

    // Pattern "decay" has meme "peel"
    IntegrationTestEntity.insertSequencePatternMeme(211, "Peel");

    // Instantiate the test subject
    testDAO = injector.getInstance(SequencePatternMemeDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePatternMeme inputData = new SequencePatternMeme()
      .setSequencePatternId(BigInteger.valueOf(110L))
      .setName("  !!2gnarLY    ");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutSequencePatternID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePatternMeme inputData = new SequencePatternMeme()
      .setName("gnarly");

    testDAO.create(access, inputData);
  }

  @Test(expected = CoreException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePatternMeme inputData = new SequencePatternMeme()
      .setSequencePatternId(BigInteger.valueOf(110L));

    testDAO.create(access, inputData);
  }

  @Test
  public void create_MacroSequencePatternMeme() throws Exception {
    IntegrationTestEntity.insertSequence(15, 2, 1, SequenceType.Macro, SequenceState.Published, "foods", 0.342, "C#", 120.4);
    IntegrationTestEntity.insertPattern(21, 15, PatternType.Macro, PatternState.Published, 16, "meat", 0.342, "C#", 120.4);
    IntegrationTestEntity.insertSequencePattern(21150, 15, 21, 0);
    IntegrationTestEntity.insertPattern(22, 15, PatternType.Macro, PatternState.Published, 16, "vegetable", 0.25, "F#", 110.3);
    IntegrationTestEntity.insertSequencePattern(22151, 15, 22, 1);
    IntegrationTestEntity.insertSequencePatternMeme(22151, "Squash");
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePatternMeme inputData = new SequencePatternMeme()
      .setSequencePatternId(BigInteger.valueOf(22151))
      .setName("Ham");

    SequencePatternMeme result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(22151), result.getSequencePatternId());
    assertEquals("Ham", result.getName());
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    SequencePatternMeme result = testDAO.readOne(access, BigInteger.valueOf(110001L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(110L), result.getSequencePatternId());
    assertEquals("Fuzz", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(110001L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<SequencePatternMeme> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(110L)));

    assertEquals(2L, result.size());
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    Collection<SequencePatternMeme> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(110L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    testDAO.destroy(access, BigInteger.valueOf(110000L));

    IntegrationTestEntity.assertNotExist(testDAO, BigInteger.valueOf(110000L));
  }

  @Test
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.destroy(access, BigInteger.valueOf(110000L));
  }
}
