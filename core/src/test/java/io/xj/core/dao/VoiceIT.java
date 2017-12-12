// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.role.Role;
import io.xj.core.model.voice.Voice;
import io.xj.core.tables.records.VoiceRecord;
import io.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.sql.Timestamp;

import static io.xj.core.tables.Voice.VOICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// future test: permissions of different users to readMany vs. create vs. update or delete patterns
public class VoiceIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private VoiceDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, Role.ADMIN);

    // Library "palm tree" has pattern "leaves" and pattern "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertPattern(1, 2, 1, PatternType.Main, "leaves", 0.342, "C#", 110.286);

    // Pattern "leaves" has phases "Ants" and "Caterpillars"
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPhase(2, 1, 1, 16, "Caterpillars", 0.583, "E major", 140.0);

    // Phase "Ants" has Voices "Head" and "Body"
    IntegrationTestEntity.insertVoice(1, 1, InstrumentType.Percussive, "This is a percussive voice");
    IntegrationTestEntity.insertVoice(2, 1, InstrumentType.Melodic, "This is melodious");
    IntegrationTestEntity.insertVoice(3, 1, InstrumentType.Harmonic, "This is harmonious");
    IntegrationTestEntity.insertVoice(4, 1, InstrumentType.Vocal, "This is a vocal voice");

    // Instantiate the test subject
    testDAO = injector.getInstance(VoiceDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setPhaseId(BigInteger.valueOf(2))
      .setType("Harmonic")
      .setDescription("This is harmonious");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(InstrumentType.Harmonic, result.get("type"));
    assertEquals("This is harmonious", result.get("description"));
    assertEquals(ULong.valueOf(2), result.get("phaseId"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutPhaseID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setType("Harmonic")
      .setDescription("This is harmonious");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setPhaseId(BigInteger.valueOf(2))
      .setDescription("This is harmonious");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    Voice result = new Voice().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getPhaseId());
    assertEquals(InstrumentType.Melodic, result.getType());
    assertEquals("This is melodious", result.getDescription());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    VoiceRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(4, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("This is a percussive voice", result1.get("description"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("This is melodious", result2.get("description"));
    JSONObject result3 = (JSONObject) result.get(2);
    assertEquals("This is harmonious", result3.get("description"));
    JSONObject result4 = (JSONObject) result.get(3);
    assertEquals("This is a vocal voice", result4.get("description"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void readAllForPatternPhaseOffset() throws Exception {
    JSONArray result = JSON.arrayOf(testDAO.readAllForPatternPhaseOffset(Access.internal(), ULong.valueOf(1), ULong.valueOf(0)));

    assertNotNull(result);
    assertEquals(4, result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("This is a percussive voice", result1.get("description"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("This is melodious", result2.get("description"));
    JSONObject result3 = (JSONObject) result.get(2);
    assertEquals("This is harmonious", result3.get("description"));
    JSONObject result4 = (JSONObject) result.get(3);
    assertEquals("This is a vocal voice", result4.get("description"));
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutPhaseID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setType("Harmonic")
      .setDescription("This is harmonious");

    testDAO.update(access, ULong.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setPhaseId(BigInteger.valueOf(2))
      .setDescription("This is harmonious");

    testDAO.update(access, ULong.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentPhase() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setPhaseId(BigInteger.valueOf(7))
      .setType("Melodic")
      .setDescription("This is melodious");

    try {
      testDAO.update(access, ULong.valueOf(3), inputData);

    } catch (Exception e) {
      VoiceRecord result = IntegrationTestService.getDb()
        .selectFrom(VOICE)
        .where(VOICE.ID.eq(ULong.valueOf(3)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("Harmonic", result.getType());
      assertEquals("This is harmonious", result.getDescription());
      assertEquals(ULong.valueOf(1), result.getPhaseId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    Voice inputData = new Voice()
      .setPhaseId(BigInteger.valueOf(1))
      .setType("Melodic")
      .setDescription("This is melodious; Yoza!");

    testDAO.update(access, ULong.valueOf(1), inputData);

    VoiceRecord result = IntegrationTestService.getDb()
      .selectFrom(VOICE)
      .where(VOICE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("This is melodious; Yoza!", result.getDescription());
    assertEquals("Melodic", result.getType());
    assertEquals(ULong.valueOf(1), result.getPhaseId());
  }

  // future test: DAO cannot update Pattern to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    VoiceRecord result = IntegrationTestService.getDb()
      .selectFrom(VOICE)
      .where(VOICE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  /**
   [#153539566] Artist should be able to delete pattern voice, even after chosen in Arrangement and Pick
   */
  @Test
  public void delete_afterChosenInArrangement() throws Exception {
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", InstrumentType.Percussive, 0.6);
    IntegrationTestEntity.insertAudio(1, 9, "Published", "Kick", "https://static.xj.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(7, 1, 1, PatternType.Macro, 2, -5);
    IntegrationTestEntity.insertArrangement(1, 7, 1, 9);
    IntegrationTestEntity.insertPick(1, 1, 1, 0.125, 1.23, 0.94, 440);
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    VoiceRecord result = IntegrationTestService.getDb()
      .selectFrom(VOICE)
      .where(VOICE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "2"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfPatternHasChildRecords() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertVoiceEvent(1, 1, 0.42, 0.41, "HEAVY", "C", 0.7, 0.98);

    try {
      testDAO.delete(access, ULong.valueOf(1));

    } catch (Exception e) {
      VoiceRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(VOICE)
        .where(VOICE.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }

  // future test: VoiceDAO cannot delete record unless user has account access

}
