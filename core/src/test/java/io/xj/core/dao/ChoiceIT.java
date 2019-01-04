// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.transport.JSON;
import org.json.JSONArray;
import org.json.JSONObject;
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
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private ChoiceDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "Testing", User "bill"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Macro, SequenceState.Published, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertSequence(2, 2, 1, SequenceType.Rhythm, SequenceState.Published, "fat beat", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertSequence(3, 2, 1, SequenceType.Main, SequenceState.Published, "dope jam", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertSequence(4, 2, 1, SequenceType.Detail, SequenceState.Published, "great accompaniment", 0.342, "C#", 0.286);

    // Chain "Test Print #1" has one segment
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-97898asdf7892.wav", new JSONObject());

    // Segment "Test Print #1" has 4 choices
    IntegrationTestEntity.insertChoice(1, 1, 1, SequenceType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(2, 1, 2, SequenceType.Rhythm, 1, +2);
    IntegrationTestEntity.insertChoice(3, 1, 4, SequenceType.Detail, 4, -7);
    IntegrationTestEntity.insertChoice(4, 1, 3, SequenceType.Main, 3, -4);

    // Instantiate the test subject
    testDAO = injector.getInstance(ChoiceDAO.class);
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setSegmentId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(3L))
      .setType("Main")
      .setSequencePatternOffset(BigInteger.valueOf(2L))
      .setTranspose(-3);

    Choice result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
    assertEquals(BigInteger.valueOf(3L), result.getSequenceId());
    assertEquals(SequenceType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2L), result.getSequencePatternOffset());
    assertEquals(Integer.valueOf(-3), result.getTranspose());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User"
    ));
    Choice inputData = new Choice()
      .setSegmentId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(3L))
      .setType("Main")
      .setSequencePatternOffset(BigInteger.valueOf(2L))
      .setTranspose(-3);

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutSegmentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setSequenceId(BigInteger.valueOf(3L))
      .setType("Main")
      .setSequencePatternOffset(BigInteger.valueOf(2L))
      .setTranspose(-3);

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithInvalidType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setSegmentId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(3L))
      .setType("BULLSHIT TYPE!")
      .setSequencePatternOffset(BigInteger.valueOf(2L))
      .setTranspose(-3);

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Choice result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
    assertEquals(BigInteger.valueOf(2L), result.getSequenceId());
    assertEquals(SequenceType.Rhythm, result.getType());
    assertEquals(BigInteger.valueOf(1L), result.getSequencePatternOffset());
    assertEquals(Integer.valueOf(+2), result.getTranspose());
  }

  @Test
  public void readOne_SegmentSequence() throws Exception {
    Choice result = testDAO.readOneSegmentSequence(Access.internal(), BigInteger.valueOf(1L), BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
    assertEquals(BigInteger.valueOf(2L), result.getSequenceId());
    assertEquals(SequenceType.Rhythm, result.getType());
    assertEquals(BigInteger.valueOf(1L), result.getSequencePatternOffset());
    assertEquals(Integer.valueOf(+2), result.getTranspose());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Choice result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readOneSegmentTypeWithAvailablePatternOffsets() throws Exception {
    IntegrationTestEntity.insertSequenceMeme(12, 2, "leafy");
    IntegrationTestEntity.insertSequenceMeme(14, 2, "smooth");

    IntegrationTestEntity.insertPattern(10, 2, PatternType.Loop, PatternState.Published, 64, "intro", 0.5, "C", 121.0);
    IntegrationTestEntity.insertPattern(11, 2, PatternType.Loop, PatternState.Published, 64, "drop", 0.5, "C", 121.0);
    IntegrationTestEntity.insertPattern(12, 2, PatternType.Loop, PatternState.Published, 64, "break", 0.5, "C", 121.0);

    Choice result = testDAO.readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), BigInteger.valueOf(1L), SequenceType.Rhythm);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getSequenceId());
    assertEquals(SequenceType.Rhythm, result.getType());
    assertEquals(BigInteger.valueOf(1L), result.getSequencePatternOffset());
    assertEquals(Integer.valueOf(2), result.getTranspose());
    assertEquals(ImmutableList.of(BigInteger.valueOf(0L)), result.getAvailablePatternOffsets());
    assertEquals(BigInteger.valueOf(0L), result.getMaxAvailablePatternOffset());
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(4L, result.length());

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
  public void readAll_SeesNothingOutsideOfSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(0L, result.length());
  }

  @Test
  public void readAllInSegments() throws Exception {
    Collection<Choice> result = testDAO.readAllInSegments(Access.internal(), ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(4L, result.size());
  }

  @Test
  public void readOne_nullIfChainNotExist() throws Exception {
    Choice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12097L));

    assertNull(result);
  }

  @Test
  public void readAllInSegments_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Choice> result = testDAO.readAllInSegments(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(4L, result.size());
  }

  @Test
  public void readAllInSegments_failsIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "73"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (1) segments in chain(s) to which user has access is required");

    testDAO.readAllInSegments(access, ImmutableList.of(BigInteger.valueOf(1L)));
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setSegmentId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(3L))
      .setType("Main")
      .setSequencePatternOffset(BigInteger.valueOf(2L))
      .setTranspose(-3);

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Choice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
    assertEquals(BigInteger.valueOf(3L), result.getSequenceId());
    assertEquals(SequenceType.Main, result.getType());
    assertEquals(BigInteger.valueOf(2L), result.getSequencePatternOffset());
    assertEquals(Integer.valueOf(-3), result.getTranspose());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutSegmentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setSequenceId(BigInteger.valueOf(3L))
      .setType("Main")
      .setSequencePatternOffset(BigInteger.valueOf(2L))
      .setTranspose(-3);

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setSegmentId(BigInteger.valueOf(1L))
      .setSequenceId(BigInteger.valueOf(3L))
      .setSequencePatternOffset(BigInteger.valueOf(2L))
      .setTranspose(-3);

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsToChangeSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Choice inputData = new Choice()
      .setSegmentId(BigInteger.valueOf(7L))
      .setSequenceId(BigInteger.valueOf(3L))
      .setType("Main")
      .setSequencePatternOffset(BigInteger.valueOf(2L))
      .setTranspose(-3);

    try {
      testDAO.update(access, BigInteger.valueOf(2L), inputData);

    } catch (Exception e) {
      Choice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    Choice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfChoiceHasChilds() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    IntegrationTestEntity.insertPatternSequencePattern(1, 1, PatternType.Main, PatternState.Published, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(1, 1, InstrumentType.Percussive, "This is a percussive voice");
    IntegrationTestEntity.insertInstrument(1, 1, 2, "jams", InstrumentType.Percussive, 0.6);
    IntegrationTestEntity.insertArrangement(1, 1, 1, 1);

    try {
      testDAO.destroy(access, BigInteger.valueOf(1L));

    } catch (Exception e) {
      Choice result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
      assertNotNull(result);
      throw e;
    }
  }
}
