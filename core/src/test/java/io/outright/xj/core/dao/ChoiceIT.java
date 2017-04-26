// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.instrument.Instrument;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.choice.ChoiceWrapper;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.tables.records.ChoiceRecord;

import org.jooq.types.UInteger;
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

import static io.outright.xj.core.tables.Choice.CHOICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ChoiceIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private ChoiceDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing", User "bill"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MACRO, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(2, 2, 1, Idea.RHYTHM, "fat beat", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(3, 2, 1, Idea.MAIN, "dope jam", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(4, 2, 1, Idea.SUPPORT, "great accompaniment", 0.342, "C#", 0.286);

    // Chain "Test Print #1" has one link
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120);

    // Link "Test Print #1" has 4 choices
    IntegrationTestEntity.insertChoice(1, 1, 1, Choice.MACRO, 2, -5);
    IntegrationTestEntity.insertChoice(2, 1, 2, Choice.RHYTHM, 1, +2);
    IntegrationTestEntity.insertChoice(3, 1, 4, Choice.SUPPORT, 4, -7);
    IntegrationTestEntity.insertChoice(4, 1, 3, Choice.MAIN, 3, -4);

    // Instantiate the test subject
    testDAO = injector.getInstance(ChoiceDAO.class);
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
    ChoiceWrapper inputDataWrapper = new ChoiceWrapper()
      .setChoice(new Choice()
        .setLinkId(BigInteger.valueOf(1))
        .setIdeaId(BigInteger.valueOf(3))
        .setType(Choice.MAIN)
        .setPhaseOffset(BigInteger.valueOf(2))
        .setTranspose(-3)
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("linkId"));
    assertEquals(ULong.valueOf(3), result.get("ideaId"));
    assertEquals(Choice.MAIN, result.get("type"));
    assertEquals(ULong.valueOf(2), result.get("phaseOffset"));
    assertEquals(-3, result.get("transpose"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user"
    ));
    ChoiceWrapper inputDataWrapper = new ChoiceWrapper()
      .setChoice(new Choice()
        .setLinkId(BigInteger.valueOf(1))
        .setIdeaId(BigInteger.valueOf(3))
        .setType(Choice.MAIN)
        .setPhaseOffset(BigInteger.valueOf(2))
        .setTranspose(-3)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLinkID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChoiceWrapper inputDataWrapper = new ChoiceWrapper()
      .setChoice(new Choice()
        .setIdeaId(BigInteger.valueOf(3))
        .setType(Choice.MAIN)
        .setPhaseOffset(BigInteger.valueOf(2))
        .setTranspose(-3)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithInvalidType() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChoiceWrapper inputDataWrapper = new ChoiceWrapper()
      .setChoice(new Choice()
        .setLinkId(BigInteger.valueOf(1))
        .setIdeaId(BigInteger.valueOf(3))
        .setType("BULLSHIT TYPE!")
        .setPhaseOffset(BigInteger.valueOf(2))
        .setTranspose(-3)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(2));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.get("id"));
    assertEquals(ULong.valueOf(1), result.get("linkId"));
    assertEquals(ULong.valueOf(2), result.get("ideaId"));
    assertEquals(Choice.RHYTHM, result.get("type"));
    assertEquals(UInteger.valueOf(1), result.get("phaseOffset"));
    assertEquals(+2, result.get("transpose"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLink() throws Exception {
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
    assertEquals(4, actualResultList.length());

    JSONObject actualResult0 = (JSONObject) actualResultList.get(0);
    assertEquals(Choice.MACRO, actualResult0.get("type"));
    JSONObject actualResult1 = (JSONObject) actualResultList.get(1);
    assertEquals(Choice.RHYTHM, actualResult1.get("type"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(2);
    assertEquals(Choice.SUPPORT, actualResult2.get("type"));
    JSONObject actualResult3 = (JSONObject) actualResultList.get(3);
    assertEquals(Choice.MAIN, actualResult3.get("type"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLink() throws Exception {
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
    ChoiceWrapper inputDataWrapper = new ChoiceWrapper()
      .setChoice(new Choice()
        .setLinkId(BigInteger.valueOf(1))
        .setIdeaId(BigInteger.valueOf(3))
        .setType(Choice.MAIN)
        .setPhaseOffset(BigInteger.valueOf(2))
        .setTranspose(-3)
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    ChoiceRecord result = IntegrationTestService.getDb()
      .selectFrom(CHOICE)
      .where(CHOICE.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.getLinkId());
    assertEquals(ULong.valueOf(3), result.getIdeaId());
    assertEquals(Choice.MAIN, result.getType());
    assertEquals(UInteger.valueOf(2), result.getPhaseOffset());
    assertEquals(Integer.valueOf(-3), result.getTranspose());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLinkID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChoiceWrapper inputDataWrapper = new ChoiceWrapper()
      .setChoice(new Choice()
        .setIdeaId(BigInteger.valueOf(3))
        .setType(Choice.MAIN)
        .setPhaseOffset(BigInteger.valueOf(2))
        .setTranspose(-3)
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutType() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChoiceWrapper inputDataWrapper = new ChoiceWrapper()
      .setChoice(new Choice()
        .setLinkId(BigInteger.valueOf(1))
        .setIdeaId(BigInteger.valueOf(3))
        .setPhaseOffset(BigInteger.valueOf(2))
        .setTranspose(-3)
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsToChangeLink() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    ChoiceWrapper inputDataWrapper = new ChoiceWrapper()
      .setChoice(new Choice()
        .setLinkId(BigInteger.valueOf(7))
        .setIdeaId(BigInteger.valueOf(3))
        .setType(Choice.MAIN)
        .setPhaseOffset(BigInteger.valueOf(2))
        .setTranspose(-3)
      );

    try {
      testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    } catch (Exception e) {
      ChoiceRecord result = IntegrationTestService.getDb()
        .selectFrom(CHOICE)
        .where(CHOICE.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(result);
      assertEquals(ULong.valueOf(1), result.getLinkId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    ChoiceRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(CHOICE)
      .where(CHOICE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfChoiceHasChildRecords() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(1, 1, Voice.PERCUSSIVE, "This is a percussive voice");
    IntegrationTestEntity.insertInstrument(1, 1, 2, "jams", Instrument.PERCUSSIVE, 0.6);
    IntegrationTestEntity.insertArrangement(1, 1,1,1);

    try {
      testDAO.delete(access, ULong.valueOf(1));

    } catch (Exception e) {
      ChoiceRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(CHOICE)
        .where(CHOICE.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }
}
