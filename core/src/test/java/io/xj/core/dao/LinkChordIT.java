// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.link_chord.LinkChord;
import io.xj.core.tables.records.LinkChordRecord;
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

import static io.xj.core.Tables.LINK_CHORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to readMany vs. create vs. update or delete link chords
public class LinkChordIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  private LinkChordDAO testDAO;
  private final List<ULong> linkIds = ImmutableList.of(ULong.valueOf(1), ULong.valueOf(2), ULong.valueOf(3), ULong.valueOf(4));

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Ready, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));

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

  // TODO cannot create or update a link to an offset that already exists for that idea

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(0.42)
      .setName("G minor 7")
      .setLinkId(BigInteger.valueOf(1));

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(0.42, result.get("position"));
    assertEquals("G minor 7", result.get("name"));
    assertEquals(ULong.valueOf(1), result.get("linkId"));
  }

  @Test(expected = BusinessException.class)
  public void create_failsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(0.42)
      .setName("G minor 7")
      .setLinkId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLinkID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(0.42)
      .setName("G minor 7");

    testDAO.create(access, inputData);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(0.42)
      .setLinkId(BigInteger.valueOf(2));

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    LinkChord result = new LinkChord().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getLinkId());
    assertEquals("D major", result.getName());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    LinkChordRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

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
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void readAllInChain() throws Exception {
    Result<LinkChordRecord> result = testDAO.readAllInLinks(Access.internal(), linkIds);

    assertEquals(2, result.size());
  }

  @Test
  public void readAllInChain_nullIfChainNotExist() throws Exception {
    LinkChordRecord result = testDAO.readOne(Access.internal(), ULong.valueOf(12097));

    assertNull(result);
  }

  @Test
  public void readAllInChain_okIfUserInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Result<LinkChordRecord> result = testDAO.readAllInLinks(access, linkIds);

    assertEquals(2, result.size());
  }

  @Test
  public void readAllInChain_emptyIfUserNotInAccount() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "73"
    ));

    Result<LinkChordRecord> result = testDAO.readAllInLinks(access, linkIds);
    assertEquals(0, result.size());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLinkID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(0.42)
      .setName("G minor 7");

    testDAO.update(access, ULong.valueOf(3), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(0.42)
      .setLinkId(BigInteger.valueOf(2));

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentLink() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    LinkChord inputData = new LinkChord()
      .setPosition(0.42)
      .setLinkId(BigInteger.valueOf(57))
      .setName("D minor");

    try {
      testDAO.update(access, ULong.valueOf(2), inputData);

    } catch (Exception e) {
      LinkChordRecord result = IntegrationTestService.getDb()
        .selectFrom(LINK_CHORD)
        .where(LINK_CHORD.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("D major", result.getName());
      assertEquals(ULong.valueOf(1), result.getLinkId());
      throw e;
    }
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    LinkChord inputData = new LinkChord()
      .setLinkId(BigInteger.valueOf(1))
      .setName("POPPYCOCK")
      .setPosition(0.42);

    testDAO.update(access, ULong.valueOf(1), inputData);

    LinkChordRecord result = IntegrationTestService.getDb()
      .selectFrom(LINK_CHORD)
      .where(LINK_CHORD.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("POPPYCOCK", result.getName());
    assertEquals((Double) 0.42, result.getPosition());
    assertEquals(ULong.valueOf(1), result.getLinkId());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    LinkChord inputData = new LinkChord()
      .setLinkId(BigInteger.valueOf(1))
      .setName("POPPYCOCK")
      .setPosition(0.42);

    testDAO.update(access, ULong.valueOf(1), inputData);
  }

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    LinkChordRecord result = IntegrationTestService.getDb()
      .selectFrom(LINK_CHORD)
      .where(LINK_CHORD.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }

}
