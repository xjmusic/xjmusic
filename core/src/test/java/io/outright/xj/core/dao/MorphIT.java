// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.morph.Morph;
import io.outright.xj.core.model.morph.MorphWrapper;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.tables.records.MorphRecord;

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

import static io.outright.xj.core.tables.Morph.MORPH;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class MorphIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private MorphDAO testDAO;

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
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertLink(1, 1, 0, Link.MIXED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120);

    // Link "Test Print #1" has 4 choices
    IntegrationTestEntity.insertChoice(7, 1, 1, Choice.MACRO, 2, -5);

    // Arrangement picks something
    IntegrationTestEntity.insertArrangement(1, 7, 8, 9);

    // Morph is in arrangement
    IntegrationTestEntity.insertMorph(1, 1, 0.75, "C", 0.5);
    IntegrationTestEntity.insertMorph(2, 1, 1.75, "E", 1.5);

    // Instantiate the test subject
    testDAO = injector.getInstance(MorphDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    MorphWrapper inputDataWrapper = new MorphWrapper()
      .setMorph(new Morph()
        .setArrangementId(BigInteger.valueOf(1))
        .setPosition(1.75)
        .setNote("G")
        .setDuration(3.75)
      );

    JSONObject actualResult = testDAO.create(access, inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(1), actualResult.get("arrangementId"));
    assertEquals(1.75, actualResult.get("position"));
    assertEquals("G", actualResult.get("note"));
    assertEquals(3.75, actualResult.get("duration"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user"
    ));
    MorphWrapper inputDataWrapper = new MorphWrapper()
      .setMorph(new Morph()
        .setArrangementId(BigInteger.valueOf(1))
        .setPosition(1.75)
        .setNote("G")
        .setDuration(3.75)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutArrangementID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    MorphWrapper inputDataWrapper = new MorphWrapper()
      .setMorph(new Morph()
        .setPosition(1.75)
        .setNote("G")
        .setDuration(3.75)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutPosition() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    MorphWrapper inputDataWrapper = new MorphWrapper()
      .setMorph(new Morph()
        .setArrangementId(BigInteger.valueOf(1))
        .setNote("G")
        .setDuration(3.75)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONObject actualResult = testDAO.readOne(access, ULong.valueOf(1));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(1), actualResult.get("arrangementId"));
    assertEquals(.75, actualResult.get("position"));
    assertEquals("C", actualResult.get("note"));
    assertEquals(0.5, actualResult.get("duration"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInArrangement() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    JSONObject actualResult = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(actualResult);
  }

  @Test
  public void readAll() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(2, actualResultList.length());

    JSONObject actualResult0 = (JSONObject) actualResultList.get(0);
    assertEquals("C", actualResult0.get("note"));
    JSONObject actualResult1 = (JSONObject) actualResultList.get(1);
    assertEquals("E", actualResult1.get("note"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfArrangement() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(0, actualResultList.length());
  }

  @Test
  public void update() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    MorphWrapper inputDataWrapper = new MorphWrapper()
      .setMorph(new Morph()
        .setArrangementId(BigInteger.valueOf(1))
        .setPosition(1.75)
        .setNote("G")
        .setDuration(3.75)
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    MorphRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(MORPH)
      .where(MORPH.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals(ULong.valueOf(1), updatedRecord.getArrangementId());
    assertEquals(Double.valueOf(1.75), updatedRecord.getPosition());
    assertEquals("G", updatedRecord.getNote());
    assertEquals(Double.valueOf(3.75), updatedRecord.getDuration());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutArrangementID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    MorphWrapper inputDataWrapper = new MorphWrapper()
      .setMorph(new Morph()
        .setPosition(1.75)
        .setNote("G")
        .setDuration(3.75)
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsToChangeArrangement() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    MorphWrapper inputDataWrapper = new MorphWrapper()
      .setMorph(new Morph()
        .setArrangementId(BigInteger.valueOf(15))
        .setPosition(1.75)
        .setNote("G")
        .setDuration(3.75)
      );

    try {
      testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    } catch (Exception e) {
      MorphRecord updatedRecord = IntegrationTestService.getDb()
        .selectFrom(MORPH)
        .where(MORPH.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(updatedRecord);
      assertEquals(ULong.valueOf(1), updatedRecord.getArrangementId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    MorphRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(MORPH)
      .where(MORPH.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfMorphHasChildRecords() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertAudio(1, 9, "Kick", "https://static.xj.outright.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertPick(1,1,1,1,0.125,1.23,0.94,440);

    try {
      testDAO.delete(access, ULong.valueOf(1));

    } catch (Exception e) {
      MorphRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(MORPH)
        .where(MORPH.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }
}
