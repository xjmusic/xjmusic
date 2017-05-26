// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.arrangement.Arrangement;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.tables.records.ArrangementRecord;
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

import static io.outright.xj.core.tables.Arrangement.ARRANGEMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ArrangementIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private ArrangementDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing", User "bill"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MACRO, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(8, 1, Voice.PERCUSSIVE, "This is a percussive voice");

    // Library has Instrument
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", Instrument.PERCUSSIVE, 0.6);

    // Chain "Test Print #1" has one link
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120);

    // Link "Test Print #1" has 4 choices
    IntegrationTestEntity.insertChoice(7, 1, 1, Choice.MACRO, 2, -5);

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
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(7))
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    ArrangementRecord result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(ULong.valueOf(7), result.get(ARRANGEMENT.CHOICE_ID));
    assertEquals(ULong.valueOf(8), result.get(ARRANGEMENT.VOICE_ID));
    assertEquals(ULong.valueOf(9), result.get(ARRANGEMENT.INSTRUMENT_ID));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(7))
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutChoiceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Arrangement inputData = new Arrangement()
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutVoiceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(7))
      .setInstrumentId(BigInteger.valueOf(9));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutInstrumentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(7))
      .setVoiceId(BigInteger.valueOf(8));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne_asRecordSetToModel() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Arrangement result = new Arrangement().setFromRecord(testDAO.readOne(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(7), result.getChoiceId());
    assertEquals(ULong.valueOf(8), result.getVoiceId());
    assertEquals(ULong.valueOf(9), result.getInstrumentId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInChoice() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    ArrangementRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(7)));

    assertNotNull(result);
    assertEquals(1, result.length());

    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals(ULong.valueOf(8), actualResult0.get("voiceId"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfChoice() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(7))
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    testDAO.update(access, ULong.valueOf(1), inputData);

    ArrangementRecord result = IntegrationTestService.getDb()
      .selectFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals(ULong.valueOf(7), result.getChoiceId());
    assertEquals(ULong.valueOf(8), result.getVoiceId());
    assertEquals(ULong.valueOf(9), result.getInstrumentId());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutChoiceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Arrangement inputData = new Arrangement()
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsToChangeChoice() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceId(BigInteger.valueOf(12))
      .setVoiceId(BigInteger.valueOf(8))
      .setInstrumentId(BigInteger.valueOf(9));

    try {
      testDAO.update(access, ULong.valueOf(1), inputData);

    } catch (Exception e) {
      ArrangementRecord result = IntegrationTestService.getDb()
        .selectFrom(ARRANGEMENT)
        .where(ARRANGEMENT.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(result);
      assertEquals(ULong.valueOf(7), result.getChoiceId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    ArrangementRecord result = IntegrationTestService.getDb()
      .selectFrom(ARRANGEMENT)
      .where(ARRANGEMENT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfArrangementHasChildRecords() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertAudio(1, 9, "Kick", "https://static.xj.outright.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertPick(1, 1, 1, 0.125, 1.23, 0.94, 440);

    try {
      testDAO.delete(access, ULong.valueOf(1));

    } catch (Exception e) {
      ArrangementRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(ARRANGEMENT)
        .where(ARRANGEMENT.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }
}
