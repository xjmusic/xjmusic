// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.idea.IdeaType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.LinkState;
import io.xj.core.tables.records.ChoiceRecord;
import io.xj.core.transport.JSON;

import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableList;
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
import java.util.List;

import static io.xj.core.tables.Choice.CHOICE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ChoiceIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private ChoiceDAO testDAO;
  private final List<ULong> linkIds = ImmutableList.of(ULong.valueOf(1), ULong.valueOf(2), ULong.valueOf(3), ULong.valueOf(4));

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing", User "bill"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");
    IntegrationTestEntity.insertIdea(1, 2, 1, IdeaType.Macro, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(2, 2, 1, IdeaType.Rhythm, "fat beat", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(3, 2, 1, IdeaType.Main, "dope jam", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertIdea(4, 2, 1, IdeaType.Support, "great accompaniment", 0.342, "C#", 0.286);

    // Chain "Test Print #1" has one link
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");

    // Link "Test Print #1" has 4 choices
    IntegrationTestEntity.insertChoice(1, 1, 1, IdeaType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(2, 1, 2, IdeaType.Rhythm, 1, +2);
    IntegrationTestEntity.insertChoice(3, 1, 4, IdeaType.Support, 4, -7);
    IntegrationTestEntity.insertChoice(4, 1, 3, IdeaType.Main, 3, -4);

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
      "roles", "admin"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(1))
      .setIdeaId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("linkId"));
    assertEquals(ULong.valueOf(3), result.get("ideaId"));
    assertEquals(IdeaType.Main, result.get("type"));
    assertEquals(ULong.valueOf(2), result.get("phaseOffset"));
    assertEquals(-3, result.get("transpose"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(1))
      .setIdeaId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLinkID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Choice inputData = new Choice()
      .setIdeaId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithInvalidType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(1))
      .setIdeaId(BigInteger.valueOf(3))
      .setType("BULLSHIT TYPE!")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Choice result = new Choice().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getLinkId());
    assertEquals(ULong.valueOf(2), result.getIdeaId());
    assertEquals(IdeaType.Rhythm, result.getType());
    assertEquals(ULong.valueOf(1), result.getPhaseOffset());
    assertEquals(Integer.valueOf(+2), result.getTranspose());
  }

  @Test
  public void readOne_LinkIdea() throws Exception {
    ChoiceRecord result = testDAO.readOneLinkIdea(Access.internal(), ULong.valueOf(1), ULong.valueOf(2));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getLinkId());
    assertEquals(ULong.valueOf(2), result.getIdeaId());
    assertEquals("Rhythm", result.getType());
    assertEquals(ULong.valueOf(1), result.getPhaseOffset());
    assertEquals(Integer.valueOf(+2), result.getTranspose());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    ChoiceRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readOneLinkType() throws Exception {
    IntegrationTestEntity.insertIdeaMeme(12, 2, "leafy");
    IntegrationTestEntity.insertIdeaMeme(14, 2, "smooth");

    IntegrationTestEntity.insertPhase(10, 2, 0, 64, "intro", 0.5, "C", 121);
    IntegrationTestEntity.insertPhase(11, 2, 1, 64, "drop", 0.5, "C", 121);
    IntegrationTestEntity.insertPhase(12, 2, 2, 64, "break", 0.5, "C", 121);

    Choice result = testDAO.readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), ULong.valueOf(1), IdeaType.Rhythm);

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getIdeaId());
    assertEquals(IdeaType.Rhythm, result.getType());
    assertEquals(ULong.valueOf(1), result.getPhaseOffset());
    assertEquals(Integer.valueOf(2), result.getTranspose());
    assertEquals(ImmutableList.of(ULong.valueOf(0), ULong.valueOf(1), ULong.valueOf(2)), result.getAvailablePhaseOffsets());
  }


  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(4, result.length());

    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals("Macro", actualResult0.get("type"));
    JSONObject result1 = (JSONObject) result.get(1);
    assertEquals("Rhythm", result1.get("type"));
    JSONObject result2 = (JSONObject) result.get(2);
    assertEquals("Support", result2.get("type"));
    JSONObject result3 = (JSONObject) result.get(3);
    assertEquals("Main", result3.get("type"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void readAllInChain() throws Exception {
    Result<ChoiceRecord> result = testDAO.readAllInLinks(Access.internal(), linkIds);

    assertEquals(4, result.size());
  }

  @Test
  public void readAllInChain_nullIfChainNotExist() throws Exception {
    ChoiceRecord result = testDAO.readOne(Access.internal(), ULong.valueOf(12097));

    assertNull(result);
  }

  @Test
  public void readAllInChain_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Result<ChoiceRecord> result = testDAO.readAllInLinks(access, linkIds);

    assertEquals(4, result.size());
  }

  @Test
  public void readAllInChain_emptyIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "73"
    ));

    Result<ChoiceRecord> result = testDAO.readAllInLinks(access, linkIds);
    assertEquals(0, result.size());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(1))
      .setIdeaId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.update(access, ULong.valueOf(2), inputData);

    ChoiceRecord result = IntegrationTestService.getDb()
      .selectFrom(CHOICE)
      .where(CHOICE.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.getLinkId());
    assertEquals(ULong.valueOf(3), result.getIdeaId());
    assertEquals("Main", result.getType());
    assertEquals(ULong.valueOf(2), result.getPhaseOffset());
    assertEquals(Integer.valueOf(-3), result.getTranspose());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLinkID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Choice inputData = new Choice()
      .setIdeaId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(1))
      .setIdeaId(BigInteger.valueOf(3))
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsToChangeLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Choice inputData = new Choice()
      .setLinkId(BigInteger.valueOf(7))
      .setIdeaId(BigInteger.valueOf(3))
      .setType("Main")
      .setPhaseOffset(BigInteger.valueOf(2))
      .setTranspose(-3);

    try {
      testDAO.update(access, ULong.valueOf(2), inputData);

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
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    ChoiceRecord result = IntegrationTestService.getDb()
      .selectFrom(CHOICE)
      .where(CHOICE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfChoiceHasChildRecords() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(1, 1, InstrumentType.Percussive, "This is a percussive voice");
    IntegrationTestEntity.insertInstrument(1, 1, 2, "jams", InstrumentType.Percussive, 0.6);
    IntegrationTestEntity.insertArrangement(1, 1, 1, 1);

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
