// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment_chord;

import io.xj.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class SegmentChordTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SegmentChord()
      .setName("C# minor")
      .setSegmentId(BigInteger.valueOf(1235L))
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new SegmentChord()
      .setSegmentId(BigInteger.valueOf(1235L))
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutSegmentID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Segment ID is required");

    new SegmentChord()
      .setName("C# minor")
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Position is required");

    new SegmentChord()
      .setName("C# minor")
      .setSegmentId(BigInteger.valueOf(1235L))
      .validate();
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void position_rounded() throws Exception {
    assertEquals(1.25, new SegmentChord().setPosition(1.25179957).getPosition(), 0.0000001);
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
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_chord.SegmentChord;
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

// future test: permissions of different users to readMany vs. create vs. update or delete segment entities
public class SegmentChordIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private SegmentChordDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Instant.parse("2014-08-12 12:17:02.527142"), Instant.parse("2014-09-11 12:17:01.047563"), null);

    // Chain "Test Print #1" has 5 sequential segments
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14 12:01:00.000001"), Instant.parse("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-9f7s89d8a7892.wav", new JSONObject());

    // Segment "Caterpillars" has entities "D# minor" and "D major"
    IntegrationTestEntity.insertSegmentChord(1, 1, (double) 0, "D# minor");
    IntegrationTestEntity.insertSegmentChord(2, 1, 4.0, "D major");

    // Instantiate the test subject
    testDAO = injector.getInstance(SegmentChordDAO.class);
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
    SegmentChord inputData = new SegmentChord()
      .setPosition(4.0)
      .setName("G minor 7")
      .setSegmentId(BigInteger.valueOf(1L));

    JSONObject result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(4.0, result.get("position"));
    assertEquals("G minor 7", result.get("name"));
    assertEquals(1, result.get("segmentId"));
  }

  @Test(expected = BusinessException.class)
  public void create_failsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SegmentChord inputData = new SegmentChord()
      .setPosition(4.0)
      .setName("G minor 7")
      .setSegmentId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutSegmentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    SegmentChord inputData = new SegmentChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    SegmentChord inputData = new SegmentChord()
      .setPosition(4.0)
      .setSegmentId(BigInteger.valueOf(2L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    SegmentChord result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
    assertEquals("D major", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    SegmentChord result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(2L, (long) result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("D# minor", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("D major", result2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(0L, (long) result.length());
  }

  @Test
  public void readAllInSegments() throws Exception {
    Collection<SegmentChord> result = testDAO.readAllInSegments(Access.internal(), ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, (long) result.size());
  }

  @Test
  public void readAllInSegments_nullIfChainNotExist() throws Exception {
    SegmentChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12097L));

    assertNull(result);
  }

  @Test
  public void readAllInSegments_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<SegmentChord> result = testDAO.readAllInSegments(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(2L, (long) result.size());
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

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutSegmentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SegmentChord inputData = new SegmentChord()
      .setPosition(4.0)
      .setName("G minor 7");

    testDAO.update(access, BigInteger.valueOf(3L), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SegmentChord inputData = new SegmentChord()
      .setPosition(4.0)
      .setSegmentId(BigInteger.valueOf(2L));

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentSegment() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SegmentChord inputData = new SegmentChord()
      .setPosition(4.0)
      .setSegmentId(BigInteger.valueOf(57L))
      .setName("D minor");

    try {
      testDAO.update(access, BigInteger.valueOf(2L), inputData);

    } catch (Exception e) {
      SegmentChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      assertEquals("D major", result.getName());
      assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    SegmentChord inputData = new SegmentChord()
      .setSegmentId(BigInteger.valueOf(1L))
      .setName("POPPYCOCK")
      .setPosition(4.0);

    testDAO.update(access, BigInteger.valueOf(1L), inputData);

    SegmentChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals(Double.valueOf(4.0), result.getPosition());
    assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SegmentChord inputData = new SegmentChord()
      .setSegmentId(BigInteger.valueOf(1L))
      .setName("POPPYCOCK")
      .setPosition(4.0);

    testDAO.update(access, BigInteger.valueOf(1L), inputData);
  }

  // future test: DAO cannot update Sequence to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));

    SegmentChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }

}

 */
