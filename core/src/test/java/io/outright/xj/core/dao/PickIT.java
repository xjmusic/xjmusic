// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.pick.Pick;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.tables.records.PickRecord;
import io.outright.xj.core.transport.JSON;

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

import static io.outright.xj.core.tables.Pick.PICK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PickIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private PickDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing", User "bill"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");

    // Idea, Phase, Voice
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MACRO, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(8, 1, Voice.PERCUSSIVE, "This is a percussive voice");

    // Instrument, Audio
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", Instrument.PERCUSSIVE, 0.6);
    IntegrationTestEntity.insertAudio(1, 9, "Kick", "https://static.xj.outright.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);

    // Chain, Link
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");

    // Choice, Arrangement, Pick
    IntegrationTestEntity.insertChoice(7, 1, 1, Choice.MACRO, 2, -5);
    IntegrationTestEntity.insertArrangement(1, 7, 8, 9);
    IntegrationTestEntity.insertPick(1, 1, 1, 0.125, 1.23, 0.94, 440);

    // Instantiate the test subject
    testDAO = injector.getInstance(PickDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Pick inputData = new Pick()
      .setArrangementId(BigInteger.valueOf(1))
      .setMorphId(BigInteger.valueOf(1))
      .setAudioId(BigInteger.valueOf(1))
      .setStart(0.12)
      .setLength(1.04)
      .setAmplitude(0.94)
      .setPitch(754.02);

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("arrangementId"));
    assertEquals(ULong.valueOf(1), result.get("audioId"));
    assertEquals(0.12, result.get("start"));
    assertEquals(1.04, result.get("length"));
    assertEquals(0.94, result.get("amplitude"));
    assertEquals(754.02, result.get("pitch"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user"
    ));
    Pick inputData = new Pick()
      .setArrangementId(BigInteger.valueOf(1))
      .setMorphId(BigInteger.valueOf(1))
      .setAudioId(BigInteger.valueOf(1))
      .setStart(0.12)
      .setLength(1.04)
      .setAmplitude(0.94)
      .setPitch(754.02);

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutArrangementID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Pick inputData = new Pick()
      .setMorphId(BigInteger.valueOf(1))
      .setAudioId(BigInteger.valueOf(1))
      .setStart(0.12)
      .setLength(1.04)
      .setAmplitude(0.94)
      .setPitch(754.02);

    testDAO.create(access, inputData);
  }

  @Test
  public void create_withoutMorphID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Pick inputData = new Pick()
      .setArrangementId(BigInteger.valueOf(1))
      .setAudioId(BigInteger.valueOf(1))
      .setStart(0.12)
      .setLength(1.04)
      .setAmplitude(0.94)
      .setPitch(754.02);

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithZeroAmplitude() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Pick inputData = new Pick()
      .setArrangementId(BigInteger.valueOf(1))
      .setMorphId(BigInteger.valueOf(1))
      .setAudioId(BigInteger.valueOf(1))
      .setStart(0.12)
      .setLength(1.04)
      .setAmplitude(0.0)
      .setPitch(754.02);

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Pick result = new Pick().setFromRecord(testDAO.readOne(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.getArrangementId());
    assertEquals(null, result.getMorphId());
    assertEquals(ULong.valueOf(1), result.getAudioId());
    assertEquals(Double.valueOf(0.125), result.getStart());
    assertEquals(Double.valueOf(1.23), result.getLength());
    assertEquals(Double.valueOf(0.94), result.getAmplitude());
    assertEquals(Double.valueOf(440.0), result.getPitch());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInMorph() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    PickRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(1, result.length());

    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals(440.0, actualResult0.get("pitch"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfMorph() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void readAllInLink() throws Exception {
    IntegrationTestEntity.insertPick(2, 1, 1, 1.125, 1.23, 0.94, 440);
    IntegrationTestEntity.insertPick(3, 1, 1, 2.125, 1.23, 0.94, 220);
    IntegrationTestEntity.insertPick(4, 1, 1, 3.125, 1.23, 0.94, 110);
    IntegrationTestEntity.insertPick(5, 1, 1, 4.125, 1.23, 0.94, 55);

    JSONArray result = JSON.arrayOf(testDAO.readAllInLink(Access.internal(), ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(5, result.length());

    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals(440.0, actualResult0.get("pitch"));
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Pick inputData = new Pick()
      .setArrangementId(BigInteger.valueOf(1))
      .setMorphId(BigInteger.valueOf(1))
      .setAudioId(BigInteger.valueOf(1))
      .setStart(0.12)
      .setLength(1.04)
      .setAmplitude(0.94)
      .setPitch(754.02);

    testDAO.update(access, ULong.valueOf(1), inputData);

    PickRecord result = IntegrationTestService.getDb()
      .selectFrom(PICK)
      .where(PICK.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.getArrangementId());
    assertEquals(ULong.valueOf(1), result.getAudioId());
    assertEquals(Double.valueOf(0.12), result.getStart());
    assertEquals(Double.valueOf(1.04), result.getLength());
    assertEquals(Double.valueOf(0.94), result.getAmplitude());
    assertEquals(Double.valueOf(754.02), result.getPitch());
  }

  @Test
  public void update_withoutMorphID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Pick inputData = new Pick()
      .setArrangementId(BigInteger.valueOf(1))
      .setAudioId(BigInteger.valueOf(1))
      .setStart(0.12)
      .setLength(1.04)
      .setAmplitude(0.94)
      .setPitch(754.02);

    testDAO.update(access, ULong.valueOf(1), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsToChangeArrangement() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Pick inputData = new Pick()
      .setMorphId(BigInteger.valueOf(1))
      .setArrangementId(BigInteger.valueOf(12))
      .setAudioId(BigInteger.valueOf(1))
      .setStart(0.12)
      .setLength(1.04)
      .setAmplitude(0.94)
      .setPitch(754.02);

    try {
      testDAO.update(access, ULong.valueOf(1), inputData);

    } catch (Exception e) {
      PickRecord result = IntegrationTestService.getDb()
        .selectFrom(PICK)
        .where(PICK.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(result);
      assertEquals(ULong.valueOf(1), result.getArrangementId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    PickRecord result = IntegrationTestService.getDb()
      .selectFrom(PICK)
      .where(PICK.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

}
