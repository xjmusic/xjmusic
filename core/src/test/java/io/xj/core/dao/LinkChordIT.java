// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.link_chord.LinkChord;
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

// future test: permissions of different users to readMany vs. create vs. update or delete link chords
public class LinkChordIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule());
  private LinkChordDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);

    // Chain "Test Print #1" has 5 sequential links
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");

    // Link "Caterpillars" has chords "D# minor" and "D major"
    IntegrationTestEntity.insertLinkChord(1, 1, 0, "D# minor");
    IntegrationTestEntity.insertLinkChord(2, 1, 4, "D major");

    // Instantiate the test subject
    testDAO = injector.getInstance(LinkChordDAO.class);
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
    LinkChord inputData = new LinkChord()
      .setPosition(4)
      .setName("G minor 7")
      .setLinkId(BigInteger.valueOf(1));

    JSONObject result = JSON.objectFrom(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(4, result.get("position"));
    assertEquals("G minor 7", result.get("name"));
    assertEquals(1, result.get("linkId"));
  }

  @Test(expected = BusinessException.class)
  public void create_failsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(4)
      .setName("G minor 7")
      .setLinkId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLinkID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(4)
      .setName("G minor 7");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(4)
      .setLinkId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    LinkChord result = testDAO.readOne(access, BigInteger.valueOf(2));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2), result.getId());
    assertEquals(BigInteger.valueOf(1), result.getLinkId());
    assertEquals("D major", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "326"
    ));

    LinkChord result = testDAO.readOne(access, BigInteger.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(2, result.length());
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

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1))));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void readAllInLinks() throws Exception {
    Collection<LinkChord> result = testDAO.readAllInLinks(Access.internal(), ImmutableList.of(BigInteger.valueOf(1)));

    assertEquals(2, result.size());
  }

  @Test
  public void readAllInLinks_nullIfChainNotExist() throws Exception {
    LinkChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(12097));

    assertNull(result);
  }

  @Test
  public void readAllInLinks_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<LinkChord> result = testDAO.readAllInLinks(access, ImmutableList.of(BigInteger.valueOf(1)));

    assertEquals(2, result.size());
  }

  @Test
  public void readAllInLinks_failsIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "73"
    ));

    failure.expect(BusinessException.class);
    failure.expectMessage("exactly the provided count (1) links in chain(s) to which user has access is required");

    testDAO.readAllInLinks(access, ImmutableList.of(BigInteger.valueOf(1)));
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLinkID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(4)
      .setName("G minor 7");

    testDAO.update(access, BigInteger.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(4)
      .setLinkId(BigInteger.valueOf(2));

    testDAO.update(access, BigInteger.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(4)
      .setLinkId(BigInteger.valueOf(57))
      .setName("D minor");

    try {
      testDAO.update(access, BigInteger.valueOf(2), inputData);

    } catch (Exception e) {
      LinkChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2));
      assertNotNull(result);
      assertEquals("D major", result.getName());
      assertEquals(BigInteger.valueOf(1), result.getLinkId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    LinkChord inputData = new LinkChord()
      .setLinkId(BigInteger.valueOf(1))
      .setName("POPPYCOCK")
      .setPosition(4);

    testDAO.update(access, BigInteger.valueOf(1), inputData);

    LinkChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals((Integer) 4, result.getPosition());
    assertEquals(BigInteger.valueOf(1), result.getLinkId());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    LinkChord inputData = new LinkChord()
      .setLinkId(BigInteger.valueOf(1))
      .setName("POPPYCOCK")
      .setPosition(4);

    testDAO.update(access, BigInteger.valueOf(1), inputData);
  }

  // future test: DAO cannot update Pattern to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));

    LinkChord result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1));
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1));
  }

}
