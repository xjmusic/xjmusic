// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
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

public class ChoiceIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private ChoiceDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing", User "bill"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");
    IntegrationTestEntity.insertPattern(1, 2, 1, PatternType.Macro, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPattern(2, 2, 1, PatternType.Rhythm, "fat beat", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPattern(3, 2, 1, PatternType.Main, "dope jam", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPattern(4, 2, 1, PatternType.Detail, "great accompaniment", 0.342, "C#", 0.286);

    // Chain "Test Print #1" has one link
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");

    // Link "Test Print #1" has 4 choices
    IntegrationTestEntity.insertChoice(1, 1, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(2, 1, 2, PatternType.Rhythm, 1, +2);
    IntegrationTestEntity.insertChoice(3, 1, 4, PatternType.Detail, 4, -7);
    IntegrationTestEntity.insertChoice(4, 1, 3, PatternType.Main, 3, -4);

    // Instantiate the test subject
    testDAO = injector.getInstance(ChoiceDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(1))
      .setPatternId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    Choice result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getLinkId());
    assertEquals(BigInteger.valueOf(3), result.getPatternId());
    assertEquals(PatternType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2), result.getPhaseOffset());
    assertEquals(Integer.valueOf(-3), result.getTranspose());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(1))
      .setPatternId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLinkID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setPatternId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithInvalidType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(1))
      .setPatternId(BigInteger.valueOf(3))
      .setType("BULLSHIT TYPE!")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Choice result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getLinkId());
    assertEquals(BigInteger.valueOf(2), result.getPatternId());
    assertEquals(PatternType.Rhythm, result.getType());
    assertEquals(BigInteger.valueOf(1), result.getPhaseOffset());
    assertEquals(Integer.valueOf(+2), result.getTranspose());
  }

  @Test
  public void readOne_LinkPattern() throws Exception {
    Choice result = testDAO.readOneLinkPattern(Access.internal(), BigInteger.valueOf(1), BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getLinkId());
    assertEquals(BigInteger.valueOf(2), result.getPatternId());
    assertEquals(PatternType.Rhythm, result.getType());
    assertEquals(BigInteger.valueOf(1), result.getPhaseOffset());
    assertEquals(Integer.valueOf(+2), result.getTranspose());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Choice result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readOneLinkType() throws Exception {
    IntegrationTestEntity.insertPatternMeme(12, 2, "leafy");
    IntegrationTestEntity.insertPatternMeme(14, 2, "smooth");

    IntegrationTestEntity.insertPhase(10, 2, PhaseType.Loop, 0, 64, "intro", 0.5, "C", 121);
    IntegrationTestEntity.insertPhase(11, 2, PhaseType.Loop, 1, 64, "drop", 0.5, "C", 121);
    IntegrationTestEntity.insertPhase(12, 2, PhaseType.Loop, 2, 64, "break", 0.5, "C", 121);

    Choice result = testDAO.readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), BigInteger.valueOf(1), PatternType.Rhythm);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getPatternId());
    assertEquals(PatternType.Rhythm, result.getType());
    assertEquals(BigInteger.valueOf(1), result.getPhaseOffset());
    assertEquals(Integer.valueOf(2), result.getTranspose());
    assertEquals(ImmutableList.of(BigInteger.valueOf(0), BigInteger.valueOf(1), BigInteger.valueOf(2)), result.getAvailablePhaseOffsets());
  }


  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(4, result.length());

    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals("Macro", actualResult0.get("type"));
    JSONObject result1 = (JSONObject) result.get(1);
    assertEquals("Rhythm", result1.get("type"));
    JSONObject result2 = (JSONObject) result.get(2);
    assertEquals("Detail", result2.get("type"));
    JSONObject result3 = (JSONObject) result.get(3);
    assertEquals("Main", result3.get("type"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void readAllInLinks() throws Exception {
    Collection<Choice> result = testDAO.readAllInLinks(Access.internal(), ImmutableList.of(BigInteger.valueOf(1)));

    assertEquals(4, result.size());
  }

  @Test
  public void readOne_nullIfChainNotExist() throws Exception {
    Choice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12097));

    assertNull(result);
  }

  @Test
  public void readAllInLinks_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Choice> result = testDAO.readAllInLinks(access, ImmutableList.of(BigInteger.valueOf(1)));

    assertEquals(4, result.size());
  }

  @Test
  public void readAllInLinks_failsIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "73"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (1) links in chain(s) to which user has access is required");

    testDAO.readAllInLinks(access, ImmutableList.of(BigInteger.valueOf(1)));
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(1))
      .setPatternId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.update(access, BigInteger.valueOf(2), inputData);

    Choice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1), result.getLinkId());
    assertEquals(BigInteger.valueOf(3), result.getPatternId());
    assertEquals(PatternType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2), result.getPhaseOffset());
    assertEquals(Integer.valueOf(-3), result.getTranspose());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLinkID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setPatternId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.update(access, BigInteger.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(1))
      .setPatternId(BigInteger.valueOf(3))
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.update(access, BigInteger.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsToChangeLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(7))
      .setPatternId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    try {
      testDAO.update(access, BigInteger.valueOf(2), inputData);

    } catch (Exception e) {
      Choice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
      assertNotNull(result);
      assertEquals(BigInteger.valueOf(1), result.getLinkId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));

    Choice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfChoiceHasChilds() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertPhase(1, 1, PhaseType.Main, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(1, 1, InstrumentType.Percussive, "This is a percussive voice");
    IntegrationTestEntity.insertInstrument(1, 1, 2, "jams", InstrumentType.Percussive, 0.6);
    IntegrationTestEntity.insertArrangement(1, 1, 1, 1);

    try {
      testDAO.destroy(access, BigInteger.valueOf(1));

    } catch (Exception e) {
      Choice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
      assertNotNull(result);
      throw e;
    }
  }
}
