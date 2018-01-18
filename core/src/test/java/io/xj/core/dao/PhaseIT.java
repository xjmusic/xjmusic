// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.JSON;
import io.xj.core.work.WorkManager;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

// future test: permissions of different users to readMany vs. create vs. update or delete phases
@RunWith(MockitoJUnitRunner.class)
public class PhaseIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private PhaseDAO subject;
  @Spy final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

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

    // Library "palm tree" has pattern "leaves" and pattern "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertPattern(1, 2, 1, PatternType.Main, "leaves", 0.342, "C#", 110.286);
    IntegrationTestEntity.insertPattern(2, 2, 1, PatternType.Macro, "coconuts", 8.02, "D", 130.2);

    // Pattern "leaves" has phases "Ants" and "Caterpillars"
    IntegrationTestEntity.insertPhase(1, 1, PhaseType.Main, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPhase(2, 1, PhaseType.Main, 1, 16, "Caterpillars", 0.583, "E major", 140.0);

    // Instantiate the test subject
    subject = injector.getInstance(PhaseDAO.class);
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
    subject = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(2))
      .setTypeEnum(PhaseType.Macro)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16))
      .setTotal(16);

    Phase result = subject.create(access, inputData);

    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.01);
    assertEquals("G minor 7", result.getKey());
    assertEquals(BigInteger.valueOf(2), result.getPatternId());
    assertEquals("cannons", result.getName());
    assertEquals(129.4, result.getTempo(), 0.01);
    assertEquals(BigInteger.valueOf(16), result.getOffset());
    assertEquals(Integer.valueOf(16), result.getTotal());
  }

  /**
   [#153976073] Artist wants Macro-type Pattern to have Macro-type Phase
   */
  @Test
  public void create_failsWithWrongTypeForMacroPattern() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(2))
      .setTypeEnum(PhaseType.Loop)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16))
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Macro-type Phase in Macro-type Pattern is required");

    subject.create(access, inputData);
  }

  /**
   [#153976073] Artist wants Main-type Pattern to have Main-type Phase
   */
  @Test
  public void create_failsWithWrongTypeForMainPattern() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(1))
      .setTypeEnum(PhaseType.Loop)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16))
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Main-type Phase in Main-type Pattern is required");

    subject.create(access, inputData);
  }

  /**
   [#153976073] Artist wants Rhythm-type Pattern to have Intro-, Loop-, or Outro- type Phase
   */
  @Test
  public void create_failsWithWrongTypeForRhythmPattern() throws Exception {
    IntegrationTestEntity.insertPattern(51, 2, 1, PatternType.Rhythm, "tester-b", 0.342, "C#", 110.286);
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(51))
      .setTypeEnum(PhaseType.Main)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16))
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Phase of type (Intro,Loop,Outro) in Rhythm-type Pattern is required");

    subject.create(access, inputData);
  }

  /**
   [#153976073] Artist wants Detail-type Pattern to have Intro-, Loop-, or Outro- type Phase
   */
  @Test
  public void create_failsWithWrongTypeForDetailPattern() throws Exception {
    IntegrationTestEntity.insertPattern(51, 2, 1, PatternType.Detail, "tester-b", 0.342, "C#", 110.286);
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(51))
      .setTypeEnum(PhaseType.Main)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16))
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Phase of type (Intro,Loop,Outro) in Detail-type Pattern is required");

    subject.create(access, inputData);
  }

  @Test
  public void create_TotalNotRequiredForMacroPatternPhase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(2))
      .setTypeEnum(PhaseType.Macro)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16));

    Phase result = subject.create(access, inputData);

    assertNotNull(result);
    assertNull(result.getTotal());
  }

  /**
   [#150279647] Artist wants to create multiple Phases with the same offset in the same Pattern, in order that XJ randomly select one of the phases at that offset.
   Reverts legacy [Trello#237] shouldn't be able to create phase with same offset in pattern
   */
  @Test
  public void create_MultiplePhasesAtSameOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Phase inputData = new Phase()
      .setOffset(BigInteger.valueOf(1))
      .setDensity(0.42)
      .setPatternId(BigInteger.valueOf(1))
      .setTypeEnum(PhaseType.Main)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    Phase result = subject.create(access, inputData);
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getOffset());
  }

  @Test
  public void create_TotalIsRequiredForNonMacroTypePatternPhase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(1))
      .setTypeEnum(PhaseType.Main)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16));

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type pattern, total (# beats) must be greater than zero");

    subject.create(access, inputData);
  }

  @Test
  public void create_TotalMustBeGreaterThanZeroForNonMacroTypePatternPhase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(1))
      .setTypeEnum(PhaseType.Main)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16))
      .setTotal(0);

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type pattern, total (# beats) must be greater than zero");

    subject.create(access, inputData);
  }

  @Test
  public void create_NullOptionalFieldsAllowed() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(null)
      .setKey(null)
      .setPatternId(BigInteger.valueOf(2))
      .setTypeEnum(PhaseType.Macro)
      .setName(null)
      .setTempo(null)
      .setOffset(BigInteger.valueOf(0))
      .setTotal(16);

    Phase result = subject.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getPatternId());
    assertNull(result.getDensity());
    assertNull(result.getKey());
    assertNull(result.getName());
    assertNull(result.getTempo());
    assertEquals(BigInteger.valueOf(0), result.getOffset());
    assertEquals(Integer.valueOf(16), result.getTotal());
  }

  @Test
  public void create_FailsWithoutPatternID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setTypeEnum(PhaseType.Macro)
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(0))
      .setTotal(16);

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
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setPatternId(BigInteger.valueOf(2))
      .setTypeEnum(PhaseType.Macro)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    subject.create(access, inputData);
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setPatternId(BigInteger.valueOf(2))
      .setTypeEnum(PhaseType.Macro)
      .setOffset(BigInteger.valueOf(5))
      .setName("cannons fifty nine");

    Phase result = subject.clone(access, BigInteger.valueOf(1), inputData);

    assertNotNull(result);
    assertEquals(0.583, result.getDensity(), 0.01);
    assertEquals("D minor", result.getKey());
    assertEquals(BigInteger.valueOf(2), result.getPatternId());
    assertEquals("cannons fifty nine", result.getName());
    assertEquals(120.0, result.getTempo(), 0.1);

    // Verify enqueued audio clone jobs
    verify(workManager).schedulePhaseClone(eq(0), eq(BigInteger.valueOf(1)), any());
  }

  /**
   [#150279647] Artist wants to create multiple Phases with the same offset in the same Pattern, in order that XJ randomly select one of the phases at that offset.
   */
  @Test
  public void clone_fromOriginal_toOffsetOfExistingPhase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setPatternId(BigInteger.valueOf(1))
      .setTypeEnum(PhaseType.Main)
      .setOffset(BigInteger.valueOf(1))
      .setName("cannons fifty nine");

    Phase result = subject.clone(access, BigInteger.valueOf(1), inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getOffset());
  }

  @Test(expected = BusinessException.class)
  public void clone_fromOriginal_failsWithoutOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setPatternId(BigInteger.valueOf(2))
      .setTypeEnum(PhaseType.Macro)
      .setName("cannons fifty nine");

    subject.clone(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Phase result = subject.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getPatternId());
    assertEquals("Caterpillars", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    Phase result = subject.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAllAtPatternOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<Phase> result = subject.readAllAtPatternOffset(access, BigInteger.valueOf(1), BigInteger.valueOf(1));

    assertNotNull(result);
    Phase resultOne = result.iterator().next();
    assertEquals(BigInteger.valueOf(2), resultOne.getId());
    assertEquals(BigInteger.valueOf(1), resultOne.getPatternId());
    assertEquals("Caterpillars", resultOne.getName());
  }

  /**
   [#150279647] Artist wants to create multiple Phases with the same offset in the same Pattern, in order that XJ randomly select one of the phases at that offset.
   */
  @Test
  public void readAllAtPatternOffset_multiplePhasesAtOffset() throws Exception {
    IntegrationTestEntity.insertPhase(5, 1, PhaseType.Main, 0, 16, "Army Ants", 0.683, "Eb minor", 122.4);
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    Collection<Phase> result = subject.readAllAtPatternOffset(access, BigInteger.valueOf(1), BigInteger.valueOf(0));

    assertNotNull(result);
    assertEquals(2, result.size());
    Iterator<Phase> it = result.iterator();
    assertEquals("Ants", it.next().getName());
    assertEquals("Army Ants", it.next().getName());
  }

  @Test
  public void readAllAtPatternOffset_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "143"
    ));

    Collection<Phase> result = subject.readAllAtPatternOffset(access, BigInteger.valueOf(1), BigInteger.valueOf(1));

    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(subject.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("Ants", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("Caterpillars", result2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(subject.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  // future test: DAO cannot update Pattern to a User or Library not owned by current session

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setPatternId(BigInteger.valueOf(1))
      .setTypeEnum(PhaseType.Main)
      .setOffset(BigInteger.valueOf(7))
      .setTotal(32)
      .setName(null)
      .setDensity(null)
      .setKey("")
      .setTempo((double) 0);

    subject.update(access, BigInteger.valueOf(1), inputData);

    Phase result = subject.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNotNull(result);
    assertNull(result.getName());
    assertNull(result.getDensity());
    assertNull(result.getTempo());
    assertNull(result.getKey());
    assertEquals(BigInteger.valueOf(7), result.getOffset());
    assertEquals(Integer.valueOf(32), result.getTotal());
    assertEquals(BigInteger.valueOf(1), result.getPatternId());
  }

  /**
   [#150279647] Artist wants to create multiple Phases with the same offset in the same Pattern, in order that XJ randomly select one of the phases at that offset.
   */
  @Test
  public void update_toOffsetOfExistingPhase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setPatternId(BigInteger.valueOf(1))
      .setTypeEnum(PhaseType.Main)
      .setOffset(BigInteger.valueOf(0))
      .setTotal(16)
      .setName("Caterpillars")
      .setDensity(0.583)
      .setKey("E major")
      .setTempo(140.0);

    subject.update(access, BigInteger.valueOf(2), inputData);

    Phase result = subject.readOne(Access.internal(), BigInteger.valueOf(2));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(0), result.getOffset());
  }

  @Test
  public void update_FailsWithoutPatternID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setTypeEnum(PhaseType.Macro)
      .setDensity(0.42)
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(0))
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern ID is required");

    subject.update(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void update_TotalNotRequiredForMacroPatternPhase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setTypeEnum(PhaseType.Macro)
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(2))
      .setTypeEnum(PhaseType.Macro)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16));

    subject.update(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void update_TotalIsRequiredForNonMacroTypePatternPhase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(1))
      .setTypeEnum(PhaseType.Main)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16));

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type pattern, total (# beats) must be greater than zero");

    subject.update(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void update_TotalMustBeGreaterThanZeroForNonMacroTypePatternPhase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(1))
      .setTypeEnum(PhaseType.Main)
      .setName("cannons")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(16))
      .setTotal(0);

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type pattern, total (# beats) must be greater than zero");

    subject.update(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void update_FailsWithoutOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(2))
      .setTypeEnum(PhaseType.Macro)
      .setTempo(129.4)
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    subject.update(access, BigInteger.valueOf(1), inputData);
  }

  @Test
  public void update_FailsUpdatingToNonexistentPattern() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Phase inputData = new Phase()
      .setDensity(0.42)
      .setKey("G minor 7")
      .setPatternId(BigInteger.valueOf(57))
      .setTypeEnum(PhaseType.Macro)
      .setName("Smash!")
      .setTempo(129.4)
      .setOffset(BigInteger.valueOf(0))
      .setTotal(16);

    failure.expect(BusinessException.class);
    failure.expectMessage("Pattern does not exist");

    try {
      subject.update(access, BigInteger.valueOf(2), inputData);

    } catch (Exception e) {
      Phase result = subject.readOne(Access.internal(), BigInteger.valueOf(2));
      assertNotNull(result);
      assertEquals("Caterpillars", result.getName());
      assertEquals(BigInteger.valueOf(1), result.getPatternId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    subject.destroy(access, BigInteger.valueOf(1));

    Phase result = subject.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "2"
    ));

    subject.destroy(access, BigInteger.valueOf(1));
  }

  @Test
  public void delete_SucceedsEvenIfPatternHasManyChildren_andWasUsedInProduction() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertPhaseMeme(2001, 1, "mashup");
    IntegrationTestEntity.insertPhaseChord(2011, 1, 0, "G");
    IntegrationTestEntity.insertPhaseChord(2012, 1, 2, "D");
    IntegrationTestEntity.insertVoice(2051, 1, InstrumentType.Percussive, "Smash");
    IntegrationTestEntity.insertPhaseEvent(2061, 1, 2051, 1, 4, "Bang", "G2", 0, 1);
    IntegrationTestEntity.insertPhaseEvent(2062, 1, 2051, 3, 4, "Crash", "D2", 0, 1);
    IntegrationTestEntity.insertVoice(2052, 1, InstrumentType.Percussive, "Boom");
    IntegrationTestEntity.insertPhaseEvent(2063, 1, 2052, 0, 4, "Poom", "C3", 1, 1);
    IntegrationTestEntity.insertPhaseEvent(2064, 1, 2052, 2, 4, "Paam", "F4", 1, 1);
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", InstrumentType.Percussive, 0.6);
    IntegrationTestEntity.insertChoice(1, 1, 1, PatternType.Main, 0, -5);
    IntegrationTestEntity.insertArrangement(1, 1, 2051, 9);

    subject.destroy(access, BigInteger.valueOf(1));

    // Assert total annihilation
    assertNull(subject.readOne(Access.internal(), BigInteger.valueOf(1)));
    assertNull(injector.getInstance(PhaseEventDAO.class).readOne(Access.internal(), BigInteger.valueOf(2061)));
    assertNull(injector.getInstance(PhaseEventDAO.class).readOne(Access.internal(), BigInteger.valueOf(2062)));
    assertNull(injector.getInstance(PhaseEventDAO.class).readOne(Access.internal(), BigInteger.valueOf(2063)));
    assertNull(injector.getInstance(PhaseEventDAO.class).readOne(Access.internal(), BigInteger.valueOf(2064)));
    assertNull(injector.getInstance(PhaseChordDAO.class).readOne(Access.internal(), BigInteger.valueOf(2012)));
    assertNull(injector.getInstance(PhaseChordDAO.class).readOne(Access.internal(), BigInteger.valueOf(2011)));
    assertNull(injector.getInstance(PhaseMemeDAO.class).readOne(Access.internal(), BigInteger.valueOf(2001)));
  }

  // future test: PhaseDAO cannot delete record unless user has account access

}
