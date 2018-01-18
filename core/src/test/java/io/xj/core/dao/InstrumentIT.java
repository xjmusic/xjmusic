// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.pattern.PatternType;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

// future test: permissions of different users to readMany vs. create vs. update or delete instruments
@RunWith(MockitoJUnitRunner.class)
public class InstrumentIT {
  @Spy final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  private Injector injector;
  private InstrumentDAO testDAO;

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

    // Library "sandwich" has instrument "jams" and instrument "buns"
    IntegrationTestEntity.insertLibrary(1, 1, "sandwich");
    IntegrationTestEntity.insertInstrument(1, 1, 2, "jams", InstrumentType.Percussive, 0.6);
    IntegrationTestEntity.insertInstrument(2, 1, 2, "buns", InstrumentType.Harmonic, 0.4);
    IntegrationTestEntity.insertInstrumentMeme(1, 1, "smooth");

    // Instantiate the test subject
    testDAO = injector.getInstance(InstrumentDAO.class);
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
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setLibraryId(BigInteger.valueOf(1))
      .setDescription("bimmies")
      .setType("Percussive")
      .setUserId(BigInteger.valueOf(2));

    Instrument result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(0.42, result.getDensity(), 0.01);
    assertEquals(BigInteger.valueOf(1), result.getLibraryId());
    assertEquals("bimmies", result.getDescription());
    assertEquals(InstrumentType.Percussive, result.getType());
    assertEquals(BigInteger.valueOf(2), result.getUserId());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLibraryID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setDescription("bimmies")
      .setType("Percussive")
      .setUserId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutUserID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setDescription("bimmies")
      .setType("Percussive")
      .setLibraryId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test
  public void clone_fromOriginal() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setLibraryId(BigInteger.valueOf(1))
      .setDescription("cannons fifty nine");

    Instrument result = testDAO.clone(access, BigInteger.valueOf(1), inputData);

    assertNotNull(result);
    assertEquals(0.6, result.getDensity(), 0.01);
    assertEquals(BigInteger.valueOf(2), result.getUserId());
    assertEquals(BigInteger.valueOf(1), result.getLibraryId());
    assertEquals("cannons fifty nine", result.getDescription());
    assertEquals(InstrumentType.Percussive, result.getType());
    assertEquals(BigInteger.valueOf(2), result.getUserId());

    // Verify enqueued audio clone jobs
    verify(workManager).scheduleInstrumentClone(eq(0), eq(BigInteger.valueOf(1)), any());
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Instrument result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getLibraryId());
    assertEquals("buns", result.getDescription());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Instrument result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  // future test: readAllInAccount vs readAllInLibraries, positive and negative cases

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(2, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("buns", result1.get("description"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("jams", result2.get("description"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(0, result.length());
  }
  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLibraryID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDescription("bimmies");

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setLibraryId(BigInteger.valueOf(3));

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDescription("bimmies")
      .setLibraryId(BigInteger.valueOf(387));

    try {
      testDAO.update(access, BigInteger.valueOf(2), inputData);

    } catch (Exception e) {
      Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
      assertNotNull(result);
      assertEquals("buns", result.getDescription());
      assertEquals(BigInteger.valueOf(1), result.getLibraryId());
      throw e;
    }
  }

  @Test
  public void update_Name() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "User",
      "accounts", "1"
    ));
    Instrument inputData = new Instrument()
      .setDensity(0.42)
      .setLibraryId(BigInteger.valueOf(1))
      .setDescription("bimmies")
      .setType("Percussive")
      .setUserId(BigInteger.valueOf(2));

    testDAO.update(access, BigInteger.valueOf(2), inputData);

    Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
    assertNotNull(result);
    assertEquals("bimmies", result.getDescription());
    assertEquals(BigInteger.valueOf(1), result.getLibraryId());
  }

  // future test: DAO cannot update Instrument to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertInstrument(86, 1, 2, "jub", InstrumentType.Harmonic, 0.4);

    testDAO.destroy(access, BigInteger.valueOf(86));

    Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(86));
    assertNull(result);
  }

  @Test
  public void delete_evenAfterUsedInArrangement() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertInstrument(86, 1, 2, "jub", InstrumentType.Harmonic, 0.4);
    IntegrationTestEntity.insertPattern(1, 2, 1, PatternType.Macro, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPhase(1, 1, PhaseType.Macro, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(8, 1, InstrumentType.Percussive, "This is a percussive voice");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(7, 1, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertArrangement(1, 7, 8, 86);

    testDAO.destroy(access, BigInteger.valueOf(86));

    Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(86));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfInstrumentHasChilds() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertInstrument(86, 1, 2, "hamsicle", InstrumentType.Harmonic, 0.4);
    IntegrationTestEntity.insertInstrumentMeme(5, 86, "frozen");
    IntegrationTestEntity.insertInstrumentMeme(6, 86, "ham");

    try {
      testDAO.destroy(access, BigInteger.valueOf(86));

    } catch (Exception e) {
      Instrument result = testDAO.readOne(Access.internal(), BigInteger.valueOf(86));
      assertNotNull(result);
      throw e;
    }

  }

}
