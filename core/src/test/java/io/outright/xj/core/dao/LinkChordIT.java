// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link_chord.LinkChord;
import io.outright.xj.core.model.link_chord.LinkChordWrapper;
import io.outright.xj.core.tables.records.LinkChordRecord;

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

import static io.outright.xj.core.Tables.LINK_CHORD;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete link chords
public class LinkChordIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private LinkChordDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.READY, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"));

    // Chain "Test Print #1" has 5 sequential links
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120);

    // Link "Caterpillars" has chords "C minor" and "D major"
    IntegrationTestEntity.insertLinkChord(1, 1, 0, "C minor");
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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    LinkChordWrapper inputDataWrapper = new LinkChordWrapper()
      .setLinkChord(new LinkChord()
        .setPosition(0.42)
        .setName("G minor 7")
        .setLinkId(BigInteger.valueOf(1))
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(0.42, result.get("position"));
    assertEquals("G minor 7", result.get("name"));
    assertEquals(ULong.valueOf(1), result.get("linkId"));
  }

  @Test(expected = BusinessException.class)
  public void create_failsWithoutTopLevelAccess() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    LinkChordWrapper inputDataWrapper = new LinkChordWrapper()
      .setLinkChord(new LinkChord()
        .setPosition(0.42)
        .setName("G minor 7")
        .setLinkId(BigInteger.valueOf(2))
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutLinkID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    LinkChordWrapper inputDataWrapper = new LinkChordWrapper()
      .setLinkChord(new LinkChord()
        .setPosition(0.42)
        .setName("G minor 7")
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutName() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));
    LinkChordWrapper inputDataWrapper = new LinkChordWrapper()
      .setLinkChord(new LinkChord()
        .setPosition(0.42)
        .setLinkId(BigInteger.valueOf(2))
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(2));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.get("id"));
    assertEquals(ULong.valueOf(1), result.get("linkId"));
    assertEquals("D major", result.get("name"));
  }

  @Test
  public void readOne_FailsWhenUserIsNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    JSONObject result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(2, actualResultList.length());
    JSONObject actualResult1 = (JSONObject) actualResultList.get(0);
    assertEquals("C minor", actualResult1.get("name"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("D major", actualResult2.get("name"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfLibrary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(0, actualResultList.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutLinkID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    LinkChordWrapper inputDataWrapper = new LinkChordWrapper()
      .setLinkChord(new LinkChord()
        .setPosition(0.42)
        .setName("G minor 7")
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    LinkChordWrapper inputDataWrapper = new LinkChordWrapper()
      .setLinkChord(new LinkChord()
        .setPosition(0.42)
        .setLinkId(BigInteger.valueOf(2))
      );

    testDAO.update(access, ULong.valueOf(2), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentLink() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    LinkChordWrapper inputDataWrapper = new LinkChordWrapper()
      .setLinkChord(new LinkChord()
        .setPosition(0.42)
        .setLinkId(BigInteger.valueOf(57))
        .setName("D minor")
      );

    try {
      testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
      ));
    LinkChordWrapper inputDataWrapper = new LinkChordWrapper()
      .setLinkChord(new LinkChord()
        .setLinkId(BigInteger.valueOf(1))
        .setName("POPPYCOCK")
        .setPosition(0.42)
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

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
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    LinkChordWrapper inputDataWrapper = new LinkChordWrapper()
      .setLinkChord(new LinkChord()
        .setLinkId(BigInteger.valueOf(1))
        .setName("POPPYCOCK")
        .setPosition(0.42)
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);
  }

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    LinkChordRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(LINK_CHORD)
      .where(LINK_CHORD.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsWithoutTopLevelAccess() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }

}
