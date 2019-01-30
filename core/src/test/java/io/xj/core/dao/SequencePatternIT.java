//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.JSON;
import io.xj.core.work.WorkManager;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// future test: permissions of different users to readMany vs. create vs. update or destroy patterns
@RunWith(MockitoJUnitRunner.class)
public class SequencePatternIT {
  @Spy
  private final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private SequencePatternDAO subject;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // inject mocks
    createInjector();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "palm tree" has sequence "leaves" and sequence "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Main, SequenceState.Published, "leaves", 0.342, "C#", 110.286);
    IntegrationTestEntity.insertSequence(2, 2, 1, SequenceType.Macro, SequenceState.Published, "coconuts", 8.02, "D", 130.2);

    // Sequence "leaves" has patterns "Ants" and "Jibbawhammers"
    IntegrationTestEntity.insertPatternAndSequencePattern(1, 1, PatternType.Main, PatternState.Published, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPatternAndSequencePattern(2, 1, PatternType.Main, PatternState.Published, 1, 16, "Jibbawhammers", 0.583, "E major", 140.0);

    // Instantiate the test subject
    subject = injector.getInstance(SequencePatternDAO.class);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(WorkManager.class).toInstance(workManager);
        }
      }));
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePattern inputData = new SequencePattern()
      .setSequenceId(BigInteger.valueOf(2L))
      .setPatternId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(16L));

    SequencePattern result = subject.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getSequenceId());
    assertEquals(BigInteger.valueOf(1L), result.getPatternId());
    assertEquals(BigInteger.valueOf(16L), result.getOffset());
  }

  @Test
  public void create_FailsWithoutSequenceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePattern inputData = new SequencePattern()
      .setPatternId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(16L));

    failure.expect(BusinessException.class);
    failure.expectMessage("Sequence ID is required");

    subject.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutPatternID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePattern inputData = new SequencePattern()
      .setSequenceId(BigInteger.valueOf(2L))
      .setOffset(BigInteger.valueOf(16L));

    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    subject.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePattern inputData = new SequencePattern()
      .setSequenceId(BigInteger.valueOf(2L))
      .setPatternId(BigInteger.valueOf(1L));

    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    subject.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    SequencePattern result = subject.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getSequenceId());
    assertEquals(BigInteger.valueOf(2L), result.getPatternId());
    assertEquals(BigInteger.valueOf(1L), result.getOffset());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    SequencePattern result = subject.readOne(access, BigInteger.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(subject.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(2L, result.length());
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(subject.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(0L, result.length());
  }

  @Test
  public void update_Fails() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SequencePattern inputData = new SequencePattern()
      .setSequenceId(BigInteger.valueOf(2L))
      .setPatternId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(16L));

    failure.expect(BusinessException.class);
    failure.expectMessage("Not allowed to update SequencePattern record.");

    subject.update(access, BigInteger.valueOf(1L), inputData);
  }

  @Test
  public void destroy() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    subject.destroy(access, BigInteger.valueOf(1L));

    SequencePattern result = subject.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNull(result);
  }

}
