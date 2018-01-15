// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.transport.JSON;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
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

public class ArrangementIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private ArrangementDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing", User "bill"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");
    IntegrationTestEntity.insertPattern(1, 2, 1, PatternType.Macro, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPhase(1, 1, PhaseType.Macro, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(8, 1, InstrumentType.Percussive, "This is a percussive voice");

    // Library has Instrument
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", InstrumentType.Percussive, 0.6);

    // Chain "Test Print #1" has one link
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");

    // Link "Test Print #1" has 4 choices
    IntegrationTestEntity.insertChoice(7, 1, 1, PatternType.Macro, 2, -5);

    // Arrangement picks something
    IntegrationTestEntity.insertArrangement(1, 7, 8, 9);

    // Instantiate the test subject
    testDAO = injector.getInstance(ArrangementDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(7))
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    Arrangement result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(7), result.getChoiceId());
    assertEquals(BigInteger.valueOf(8), result.getVoiceId());
    assertEquals(BigInteger.valueOf(9), result.getInstrumentId());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(7))
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutChoiceID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutVoiceID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(7))
      .setInstrumentId(BigInteger.valueOf(9));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutInstrumentID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(7))
      .setVoiceId(BigInteger.valueOf(8));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne_asSetToModel() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Arrangement result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(7), result.getChoiceId());
    assertEquals(BigInteger.valueOf(8), result.getVoiceId());
    assertEquals(BigInteger.valueOf(9), result.getInstrumentId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInChoice() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Arrangement result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(7))));

    assertNotNull(result);
    assertEquals(1, result.length());

    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals(8, actualResult0.get("voiceId"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfChoice() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  /**
   [#154118202] FIX: Artist should have access to view and listen to Chain from Account
   */
  @Test
  public void readAllInLinks_adminAccess() throws Exception {
    IntegrationTestEntity.insertLink(101, 1, 1, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:30.000001"), Timestamp.valueOf("2017-02-14 12:01:40.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(102, 1, 2, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:40.000001"), Timestamp.valueOf("2017-02-14 12:01:50.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(103, 1, 3, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:50.000001"), Timestamp.valueOf("2017-02-14 12:02:00.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(201, 101, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(202, 102, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(203, 103, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertArrangement(301, 201, 8, 9);
    IntegrationTestEntity.insertArrangement(302, 202, 8, 9);
    IntegrationTestEntity.insertArrangement(303, 203, 8, 9);

    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));

    Collection<Arrangement> result = testDAO.readAllInLinks(access, ImmutableList.of(BigInteger.valueOf(101), BigInteger.valueOf(102)));

    assertNotNull(result);
    assertEquals(2, result.size());
  }

  /**
   [#154118202] FIX: Artist should have access to view and listen to Chain from Account
   */
  @Test
  public void readAllInLinks_regularUserAccess() throws Exception {
    IntegrationTestEntity.insertLink(101, 1, 1, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:30.000001"), Timestamp.valueOf("2017-02-14 12:01:40.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(102, 1, 2, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:40.000001"), Timestamp.valueOf("2017-02-14 12:01:50.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(103, 1, 3, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:50.000001"), Timestamp.valueOf("2017-02-14 12:02:00.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(201, 101, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(202, 102, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(203, 103, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertArrangement(301, 201, 8, 9);
    IntegrationTestEntity.insertArrangement(302, 202, 8, 9);
    IntegrationTestEntity.insertArrangement(303, 203, 8, 9);

    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Arrangement> result = testDAO.readAllInLinks(access, ImmutableList.of(BigInteger.valueOf(101), BigInteger.valueOf(102)));

    assertNotNull(result);
    assertEquals(2, result.size());
  }

  /**
   [#154118202] FIX: Artist should have access to view and listen to Chain from Account
   */
  @Test
  public void readAllInLinks_regularUserAccess_failsIfLinkOutsideAccount() throws Exception {
    IntegrationTestEntity.insertLink(101, 1, 1, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:30.000001"), Timestamp.valueOf("2017-02-14 12:01:40.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(102, 1, 2, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:40.000001"), Timestamp.valueOf("2017-02-14 12:01:50.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(201, 101, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(202, 102, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertArrangement(301, 201, 8, 9);
    IntegrationTestEntity.insertArrangement(302, 202, 8, 9);
    IntegrationTestEntity.insertAccount(79, "Account that user does not have access to");
    IntegrationTestEntity.insertChain(7903, 79, "chain that user does not have access to", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertLink(7003, 7903, 3, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:50.000001"), Timestamp.valueOf("2017-02-14 12:02:00.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(8003, 7003, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertArrangement(9003, 8003, 8, 9);
    Access access = Access.from(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (3) links in chain(s) to which user has access is required");

    testDAO.readAllInLinks(access, ImmutableList.of(BigInteger.valueOf(101), BigInteger.valueOf(102), BigInteger.valueOf(7003)));
  }

  @Test
  public void update() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(7))
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    testDAO.update(access, BigInteger.valueOf(1), inputData);

    Arrangement result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(7), result.getChoiceId());
    assertEquals(BigInteger.valueOf(8), result.getVoiceId());
    assertEquals(BigInteger.valueOf(9), result.getInstrumentId());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutChoiceID() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    testDAO.update(access, BigInteger.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsToChangeChoice() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(12))
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    try {
      testDAO.update(access, BigInteger.valueOf(1), inputData);

    } catch (Exception e) {
      Arrangement result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
      assertNotNull(result);
      assertEquals(BigInteger.valueOf(7), result.getChoiceId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    Access access = Access.from(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));

    Arrangement result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

}
