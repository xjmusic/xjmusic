// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.phase.Phase;
import io.outright.xj.core.model.phase.PhaseWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.IdeaRecord;
import io.outright.xj.core.tables.records.PhaseRecord;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static io.outright.xj.core.tables.Idea.IDEA;
import static io.outright.xj.core.tables.Phase.PHASE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete ideas
public class PhaseIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private PhaseDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(2, Role.ADMIN);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(3, Role.USER);
    IntegrationTestEntity.insertAccountUser(1, 3);

    // Library "palm tree" has idea "leaves" and idea "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MAIN, "leaves", 0.342, "C#", 110.286);
    IntegrationTestEntity.insertIdea(2, 2, 1, Idea.MAIN, "coconuts", 8.02, "D", 130.2);

    // Idea "leaves" has phases "Ants" and "Caterpillars"
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertPhase(2, 1, 1, 16, "Caterpillars", 0.583, "E major", 140.0);

    // Instantiate the test subject
    testDAO = injector.getInstance(PhaseDAO.class);
  }

  @After
  public void tearDown() throws Exception {
    testDAO = null;
  }

  // TODO cannot create or update a phase to an offset that already exists for that idea

  @Test
  public void create() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setIdeaId(BigInteger.valueOf(2))
        .setName("cannons")
        .setTempo(129.4)
        .setOffset(0)
        .setTotal(16)
      );

    JSONObject actualResult = testDAO.create(access, inputDataWrapper);

    assertNotNull(actualResult);
    assertEquals(0.42, actualResult.get("density"));
    assertEquals("G minor 7", actualResult.get("key"));
    assertEquals(ULong.valueOf(2), actualResult.get("ideaId"));
    assertEquals("cannons", actualResult.get("name"));
    assertEquals(129.4, actualResult.get("tempo"));
    assertEquals(0, actualResult.get("offset"));
    assertEquals(16, actualResult.get("total"));
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutIdeaID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setName("cannons")
        .setTempo(129.4)
        .setOffset(0)
        .setTotal(16)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void create_FailsWithoutKey() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setIdeaId(BigInteger.valueOf(2))
        .setName("cannons")
        .setTempo(129.4)
        .setOffset(0)
        .setTotal(16)
      );

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void readOne() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    JSONObject actualResult = testDAO.readOne(access, ULong.valueOf(2));

    assertNotNull(actualResult);
    assertEquals(ULong.valueOf(2), actualResult.get("id"));
    assertEquals(ULong.valueOf(1), actualResult.get("ideaId"));
    assertEquals("Caterpillars", actualResult.get("name"));
  }

  @Test
  public void readOneAble_FailsWhenUserIsNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "326"
    ));

    JSONObject actualResult = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(actualResult);
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
    assertEquals("Ants", actualResult1.get("name"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("Caterpillars", actualResult2.get("name"));
  }

  @Test
  public void readAllAble_SeesNothingOutsideOfLibrary() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "345"
    ));

    JSONArray actualResultList = testDAO.readAllIn(access, ULong.valueOf(1));

    assertNotNull(actualResultList);
    assertEquals(0, actualResultList.length());
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutIdeaID() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setName("cannons")
        .setTempo(129.4)
        .setOffset(0)
        .setTotal(16)
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsWithoutName() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setIdeaId(BigInteger.valueOf(2))
        .setTempo(129.4)
        .setOffset(0)
        .setTotal(16)
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);
  }

  @Test(expected = BusinessException.class)
  public void update_FailsUpdatingToNonexistentIdea() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setIdeaId(BigInteger.valueOf(57))
        .setName("cannons")
        .setTempo(129.4)
        .setOffset(0)
        .setTotal(16)
      );

    testDAO.update(access, ULong.valueOf(3), inputDataWrapper);

    IdeaRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(IDEA)
      .where(IDEA.ID.eq(ULong.valueOf(3)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("cannons", updatedRecord.getName());
    assertEquals(ULong.valueOf(2), updatedRecord.getLibraryId());
  }

  @Test
  public void update() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setIdeaId(BigInteger.valueOf(1))
        .setOffset(7)
        .setTotal(32)
        .setName("POPPYCOCK")
        .setDensity(0.42)
        .setKey("G major")
        .setTempo(169.0)
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    PhaseRecord updatedRecord = IntegrationTestService.getDb()
      .selectFrom(PHASE)
      .where(PHASE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(updatedRecord);
    assertEquals("POPPYCOCK", updatedRecord.getName());
    assertEquals((Double) 0.42, updatedRecord.getDensity());
    assertEquals((Double) 169.0, updatedRecord.getTempo());
    assertEquals("G major", updatedRecord.getKey());
    assertEquals(ULong.valueOf(7), updatedRecord.getOffset());
    assertEquals(ULong.valueOf(32), updatedRecord.getTotal());
    assertEquals(ULong.valueOf(1), updatedRecord.getIdeaId());
  }

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    PhaseRecord deletedRecord = IntegrationTestService.getDb()
      .selectFrom(PHASE)
      .where(PHASE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(deletedRecord);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "2"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }

  @Test(expected = BusinessException.class)
  public void delete_FailsIfIdeaHasChildRecords() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "userId", "2",
      "roles", "artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertPhaseMeme(1, 1, "mashup");

    try {
      testDAO.delete(access, ULong.valueOf(1));

    } catch (Exception e) {
      PhaseRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(PHASE)
        .where(PHASE.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }

  // TODO [core] test PhaseDAO cannot delete record unless user has account access

}
