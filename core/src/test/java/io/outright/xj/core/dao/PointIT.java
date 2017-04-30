// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.point.Point;
import io.outright.xj.core.model.point.PointWrapper;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.tables.records.PointRecord;

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

import static io.outright.xj.core.tables.Point.POINT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PointIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private PointDAO testDAO;

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
    IntegrationTestEntity.insertVoiceEvent(1, 8, 0, 1, "KICK", "C", 0.8, 1.0);

    // Library has Instrument
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", Instrument.PERCUSSIVE, 0.6);
    IntegrationTestEntity.insertAudio(1, 9, "Kick", "https://static.xj.outright.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440);

    // Chain "Test Print #1" has one link
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120);

    // Link "Test Print #1" has 4 choices
    IntegrationTestEntity.insertChoice(7, 1, 1, Choice.MACRO, 2, -5);

    // Arrangement points something
    IntegrationTestEntity.insertArrangement(1, 7, 8, 9);

    // Morph is in arrangement
    IntegrationTestEntity.insertMorph(1, 1, 0.75, "C", 0.5);

    // Point is in Morph
    IntegrationTestEntity.insertPoint(1, 1, 1, 0.125, "C", 1.5);

    // Instantiate the test subject
    testDAO = injector.getInstance(PointDAO.class);
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
    PointWrapper inputDataWrapper = new PointWrapper()
      .setPoint(new Point()
        .setMorphId(BigInteger.valueOf(1))
        .setVoiceEventId(BigInteger.valueOf(1))
        .setPosition(12.75)
        .setNote("Db")
        .setDuration(7.25)
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("morphId"));
    assertEquals(ULong.valueOf(1), result.get("voiceEventId"));
    assertEquals(12.75, result.get("position"));
    assertEquals("Db", result.get("note"));
    assertEquals(7.25, result.get("duration"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user"
    ));
    PointWrapper inputDataWrapper = new PointWrapper()
      .setPoint(new Point()
        .setMorphId(BigInteger.valueOf(1))
        .setVoiceEventId(BigInteger.valueOf(1))
        .setPosition(12.75)
        .setNote("Db")
        .setDuration(7.25)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutMorphID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    PointWrapper inputDataWrapper = new PointWrapper()
      .setPoint(new Point()
        .setVoiceEventId(BigInteger.valueOf(1))
        .setPosition(12.75)
        .setNote("Db")
        .setDuration(7.25)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutNote() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    PointWrapper inputDataWrapper = new PointWrapper()
      .setPoint(new Point()
        .setMorphId(BigInteger.valueOf(1))
        .setVoiceEventId(BigInteger.valueOf(1))
        .setPosition(12.75)
        .setDuration(7.25)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(1));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("morphId"));
    assertEquals(ULong.valueOf(1), result.get("voiceEventId"));
    assertEquals(0.125, result.get("position"));
    assertEquals("C", result.get("note"));
    assertEquals(1.5, result.get("duration"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInMorph() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(1, actualResultList.length());

    JSONObject actualResult0 = (JSONObject) actualResultList.get(0);
    assertEquals("C", actualResult0.get("note"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfMorph() throws Exception {
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
    PointWrapper inputDataWrapper = new PointWrapper()
      .setPoint(new Point()
        .setMorphId(BigInteger.valueOf(1))
        .setVoiceEventId(BigInteger.valueOf(1))
        .setPosition(12.75)
        .setNote("Db")
        .setDuration(7.25)
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    PointRecord result = IntegrationTestService.getDb()
      .selectFrom(POINT)
      .where(POINT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.getMorphId());
    assertEquals(ULong.valueOf(1), result.getVoiceEventId());
    assertEquals(Double.valueOf(12.75), result.getPosition());
    assertEquals("Db", result.getNote());
    assertEquals(Double.valueOf(7.25), result.getDuration());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutMorphID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    PointWrapper inputDataWrapper = new PointWrapper()
      .setPoint(new Point()
        .setVoiceEventId(BigInteger.valueOf(1))
        .setPosition(12.75)
        .setNote("Db")
        .setDuration(7.25)
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsToChangeMorph() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    PointWrapper inputDataWrapper = new PointWrapper()
      .setPoint(new Point()
        .setMorphId(BigInteger.valueOf(12))
        .setVoiceEventId(BigInteger.valueOf(1))
        .setPosition(12.75)
        .setNote("Db")
        .setDuration(7.25)
      );

    try {
      testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    } catch (Exception e) {
      PointRecord result = IntegrationTestService.getDb()
        .selectFrom(POINT)
        .where(POINT.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(result);
      assertEquals(ULong.valueOf(1), result.getMorphId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    PointRecord result = IntegrationTestService.getDb()
      .selectFrom(POINT)
      .where(POINT.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

}
