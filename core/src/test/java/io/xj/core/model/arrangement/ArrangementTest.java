// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.arrangement;

import io.xj.core.exception.CoreException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.UUID;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class ArrangementTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Arrangement()
      .setVoiceId(BigInteger.valueOf(354L))
      .setChoiceUuid(UUID.randomUUID())
      .setInstrumentId(BigInteger.valueOf(432L))
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutSegmentID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Segment ID is required");

    new Arrangement()
      .setVoiceId(BigInteger.valueOf(354L))
      .setChoiceUuid(UUID.randomUUID())
      .setInstrumentId(BigInteger.valueOf(432L))
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Voice ID is required");

    new Arrangement()
      .setChoiceUuid(UUID.randomUUID())
      .setInstrumentId(BigInteger.valueOf(432L))
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutChoiceID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Choice ID is required");

    new Arrangement()
      .setVoiceId(BigInteger.valueOf(354L))
      .setInstrumentId(BigInteger.valueOf(432L))
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Instrument ID is required");

    new Arrangement()
      .setVoiceId(BigInteger.valueOf(354L))
      .setChoiceUuid(UUID.randomUUID())
      .setSegmentId(BigInteger.valueOf(25))
      .validate();
  }

}


/*

TODO implement the following as unit tests

   TODO implement test for aggregate()

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
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
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
import java.time.Instant;
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
    IntegrationTestEntity.reset();

    // Account "Testing", User "bill"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Macro, SequenceState.Published, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPatternAndSequencePattern(1, 1, PatternType.Macro, PatternState.Published, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(8, 1, InstrumentType.Percussive, "This is a percussive voice");

    // Library has Instrument
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", InstrumentType.Percussive, 0.6);

    // Chain "Test Print #1" has one segment
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12 12:17:02.527142"), Instant.parse("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:00.000001"), Instant.parse("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());

    // Segment "Test Print #1" has 4 choices
    IntegrationTestEntity.insertChoice(7, 1, 1, SequenceType.Macro, 2, -5);

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
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceUuid(BigInteger.valueOf(7L))
      .setVoiceId(BigInteger.valueOf(8L))
      .setInstrumentId(BigInteger.valueOf(9L));

    Arrangement result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(7L), result.getChoiceUuid());
    assertEquals(BigInteger.valueOf(8L), result.getVoiceId());
    assertEquals(BigInteger.valueOf(9L), result.getInstrumentId());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceUuid(BigInteger.valueOf(7L))
      .setVoiceId(BigInteger.valueOf(8L))
      .setInstrumentId(BigInteger.valueOf(9L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutChoiceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setVoiceId(BigInteger.valueOf(8L))
      .setInstrumentId(BigInteger.valueOf(9L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutVoiceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceUuid(BigInteger.valueOf(7L))
      .setInstrumentId(BigInteger.valueOf(9L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutInstrumentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceUuid(BigInteger.valueOf(7L))
      .setVoiceId(BigInteger.valueOf(8L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne_asSetToModel() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Arrangement result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(7L), result.getChoiceUuid());
    assertEquals(BigInteger.valueOf(8L), result.getVoiceId());
    assertEquals(BigInteger.valueOf(9L), result.getInstrumentId());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInChoice() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Arrangement result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(7L))));

    assertNotNull(result);
    assertEquals(1L, (long) result.length());

    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals(8, actualResult0.get("voiceId"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfChoice() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0L, (long) result.length());
  }

  /**
   [#154118202] FIX: Artist should have access to view and listen to Chain from Account
   *
@Test
public void readAllInSegments_adminAccess() throws Exception {
  IntegrationTestEntity.insertSegment(101, 1, 1, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:30.000001"), Instant.parse("2017-02-14 12:01:40.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());
  IntegrationTestEntity.insertSegment(102, 1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:40.000001"), Instant.parse("2017-02-14 12:01:50.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());
  IntegrationTestEntity.insertSegment(103, 1, 3, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:50.000001"), Instant.parse("2017-02-14 12:02:00.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());
  IntegrationTestEntity.insertChoice(201, 101, 1, SequenceType.Macro, 2, -5);
  IntegrationTestEntity.insertChoice(202, 102, 1, SequenceType.Macro, 2, -5);
  IntegrationTestEntity.insertChoice(203, 103, 1, SequenceType.Macro, 2, -5);
  IntegrationTestEntity.insertArrangement(301, 201, 8, 9);
  IntegrationTestEntity.insertArrangement(302, 202, 8, 9);
  IntegrationTestEntity.insertArrangement(303, 203, 8, 9);

  Access access = new Access(ImmutableMap.of(
    "roles", "Admin"
  ));

  Collection<Arrangement> result = testDAO.readAllInSegments(access, ImmutableList.of(BigInteger.valueOf(101L), BigInteger.valueOf(102L)));

  assertNotNull(result);
  assertEquals(2L, (long) result.size());
}

  /**
   [#154118202] FIX: Artist should have access to view and listen to Chain from Account
   *
  @Test
  public void readAllInSegments_regularUserAccess() throws Exception {
    IntegrationTestEntity.insertSegment(101, 1, 1, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:30.000001"), Instant.parse("2017-02-14 12:01:40.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());
    IntegrationTestEntity.insertSegment(102, 1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:40.000001"), Instant.parse("2017-02-14 12:01:50.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());
    IntegrationTestEntity.insertSegment(103, 1, 3, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:50.000001"), Instant.parse("2017-02-14 12:02:00.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());
    IntegrationTestEntity.insertChoice(201, 101, 1, SequenceType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(202, 102, 1, SequenceType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(203, 103, 1, SequenceType.Macro, 2, -5);
    IntegrationTestEntity.insertArrangement(301, 201, 8, 9);
    IntegrationTestEntity.insertArrangement(302, 202, 8, 9);
    IntegrationTestEntity.insertArrangement(303, 203, 8, 9);

    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Arrangement> result = testDAO.readAllInSegments(access, ImmutableList.of(BigInteger.valueOf(101L), BigInteger.valueOf(102L)));

    assertNotNull(result);
    assertEquals(2L, (long) result.size());
  }

  /**
   [#154118202] FIX: Artist should have access to view and listen to Chain from Account
   *
  @Test
  public void readAllInSegments_regularUserAccess_failsIfSegmentOutsideAccount() throws Exception {
    IntegrationTestEntity.insertSegment(101, 1, 1, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:30.000001"), Instant.parse("2017-02-14 12:01:40.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());
    IntegrationTestEntity.insertSegment(102, 1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:40.000001"), Instant.parse("2017-02-14 12:01:50.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());
    IntegrationTestEntity.insertChoice(201, 101, 1, SequenceType.Macro, 2, -5);
    IntegrationTestEntity.insertChoice(202, 102, 1, SequenceType.Macro, 2, -5);
    IntegrationTestEntity.insertArrangement(301, 201, 8, 9);
    IntegrationTestEntity.insertArrangement(302, 202, 8, 9);
    IntegrationTestEntity.insertAccount(79, "Account that user does not have access to");
    IntegrationTestEntity.insertChain(7903, 79, "chain that user does not have access to", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12 12:17:02.527142"), Instant.parse("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertSegment(7003, 7903, 3, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:50.000001"), Instant.parse("2017-02-14 12:02:00.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());
    IntegrationTestEntity.insertChoice(8003, 7003, 1, SequenceType.Macro, 2, -5);
    IntegrationTestEntity.insertArrangement(9003, 8003, 8, 9);
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (3) segments in chain(s) to which user has access is required");

    testDAO.readAllInSegments(access, ImmutableList.of(BigInteger.valueOf(101L), BigInteger.valueOf(102L), BigInteger.valueOf(7003L)));
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceUuid(BigInteger.valueOf(7L))
      .setVoiceId(BigInteger.valueOf(8L))
      .setInstrumentId(BigInteger.valueOf(9L));

    testDAO.update(access, BigInteger.valueOf(1L), inputData);

    Arrangement result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
    assertEquals(BigInteger.valueOf(7L), result.getChoiceUuid());
    assertEquals(BigInteger.valueOf(8L), result.getVoiceId());
    assertEquals(BigInteger.valueOf(9L), result.getInstrumentId());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutChoiceID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setVoiceId(BigInteger.valueOf(8L))
      .setInstrumentId(BigInteger.valueOf(9L));

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsToChangeChoice() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Arrangement inputData = new Arrangement()
      .setChoiceUuid(BigInteger.valueOf(12L))
      .setVoiceId(BigInteger.valueOf(8L))
      .setInstrumentId(BigInteger.valueOf(9L));

    try {
      testDAO.update(access, BigInteger.valueOf(1L), inputData);

    } catch (Exception e) {
      Arrangement result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
      assertNotNull(result);
      assertEquals(BigInteger.valueOf(7L), result.getChoiceUuid());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    Arrangement result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNull(result);
  }

}

 */
