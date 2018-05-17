// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.user_auth.UserAuthType;
import io.xj.core.model.user_role.UserRoleType;
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
import java.sql.Timestamp;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// future test: permissions of different users to readMany vs. create vs. update or delete segment memes
public class SegmentMemeIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private SegmentMemeDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    // Chain "Test Print #1" has 5 sequential segments
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(3, 1, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(4, 1, 3, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:02:36.000001"), Timestamp.valueOf("2017-02-14 12:03:08.000001"), "E minor", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment_Planned(5, 1, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.User);
    IntegrationTestEntity.insertUserRole(2, 2, UserRoleType.Admin);
    IntegrationTestEntity.insertAccountUser(3, 1, 2);
    IntegrationTestEntity.insertUserAuth(102, 2, UserAuthType.Google, "external_access_token_123", "external_refresh_token_123", "22222");
    IntegrationTestEntity.insertUserAccessToken(2, 102, "this-is-my-actual-access-token");

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(4, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(5, 1, 3);

    // Bill has a "user" role but no account membership
    IntegrationTestEntity.insertUser(4, "bill", "bill@email.com", "http://pictures.com/bill.gif");
    IntegrationTestEntity.insertUserRole(6, 4, UserRoleType.User);

    // Segment #1 memes
    IntegrationTestEntity.insertSegmentMeme(1, 1, "Cool");
    IntegrationTestEntity.insertSegmentMeme(2, 1, "Dark");

    // Segment #2 memes
    IntegrationTestEntity.insertSegmentMeme(3, 2, "Warm");

    // Instantiate the test subject
    testDAO = injector.getInstance(SegmentMemeDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "userId", "2",
      "roles", "Artist",
      "accounts", "1"
    ));
    SegmentMeme inputData = new SegmentMeme()
      .setSegmentId(BigInteger.valueOf(1L))
      .setName("  !!2gnarLY    ");

    SegmentMeme result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
    assertEquals("Gnarly", result.getName());
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutSegmentID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SegmentMeme inputData = new SegmentMeme()
      .setName("gnarly");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    SegmentMeme inputData = new SegmentMeme()
      .setSegmentId(BigInteger.valueOf(1L));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    SegmentMeme result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getSegmentId());
    assertEquals("Dark", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    SegmentMeme result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(2L, (long) result.length());
    JSONObject result1 = (JSONObject) result.get(0);
    assertEquals("Cool", result1.get("name"));
    JSONObject result2 = (JSONObject) result.get(1);
    assertEquals("Dark", result2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(0L, (long) result.length());
  }

  @Test
  public void readAllInSegments() throws Exception {
    Collection<SegmentMeme> result = testDAO.readAllInSegments(Access.internal(), ImmutableList.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L), BigInteger.valueOf(3L), BigInteger.valueOf(4L)));

    assertEquals(3L, (long) result.size());
  }

  @Test
  public void readAllInSegments_nullIfChainNotExist() throws Exception {
    SegmentMeme result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12097L));

    assertNull(result);
  }

  @Test
  public void readAllInSegments_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<SegmentMeme> result = testDAO.readAllInSegments(access, ImmutableList.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L), BigInteger.valueOf(3L), BigInteger.valueOf(4L)));

    assertEquals(3L, (long) result.size());
  }

  @Test
  public void readAllInSegments_failsIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "73"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (4) segments in chain(s) to which user has access is required");

    testDAO.readAllInSegments(access, ImmutableList.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L), BigInteger.valueOf(3L), BigInteger.valueOf(4L)));
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    testDAO.destroy(access, BigInteger.valueOf(1L));

    SegmentMeme result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNull(result);
  }
}
