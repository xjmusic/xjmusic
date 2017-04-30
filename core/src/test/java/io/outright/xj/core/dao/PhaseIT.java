// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.phase.Phase;
import io.outright.xj.core.model.phase.PhaseWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.PhaseRecord;

import org.jooq.impl.DSL;
import org.jooq.types.ULong;

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

import static io.outright.xj.core.tables.Phase.PHASE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

// TODO [core] test permissions of different users to read vs. create vs. update or delete phases
public class PhaseIT {
  private Injector injector = Guice.createInjector(new CoreModule());
  private PhaseDAO testDAO;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, Role.ADMIN);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(2, 3, Role.USER);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "palm tree" has idea "leaves" and idea "coconuts"
    IntegrationTestEntity.insertLibrary(1, 1, "palm tree");
    IntegrationTestEntity.insertIdea(1, 2, 1, Idea.MAIN, "leaves", 0.342, "C#", 110.286);
    IntegrationTestEntity.insertIdea(2, 2, 1, Idea.MACRO, "coconuts", 8.02, "D", 130.2);

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
        .setOffset(16)
        .setTotal(16)
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(0.42, result.get("density"));
    assertEquals("G minor 7", result.get("key"));
    assertEquals(ULong.valueOf(2), result.get("ideaId"));
    assertEquals("cannons", result.get("name"));
    assertEquals(129.4, result.get("tempo"));
    assertEquals(16, result.get("offset"));
    assertEquals(16, result.get("total"));
  }

  @Test
  public void create_TotalNotRequiredForMacroIdeaPhase() throws Exception {
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
        .setOffset(16)
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(DSL.val((String) null), result.get("total"));
  }

  @Test()
  public void create_TotalIsRequiredForNonMacroTypeIdeaPhase() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setIdeaId(BigInteger.valueOf(1))
        .setName("cannons")
        .setTempo(129.4)
        .setOffset(16)
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type idea, total (# beats) is required");

    testDAO.create(access, inputDataWrapper);
  }

  @Test()
  public void create_TotalMustBeGreaterThanZeroForNonMacroTypeIdeaPhase() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setIdeaId(BigInteger.valueOf(1))
        .setName("cannons")
        .setTempo(129.4)
        .setOffset(16)
        .setTotal(0)
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type idea, total (# beats) must be greater than zero");

    testDAO.create(access, inputDataWrapper);
  }

  @Test
  public void create_NullOptionalFieldsAllowed() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(null)
        .setKey(null)
        .setIdeaId(BigInteger.valueOf(2))
        .setName(null)
        .setTempo(null)
        .setOffset(0)
        .setTotal(16)
      );

    JSONObject result = testDAO.create(access, inputDataWrapper);

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.get("ideaId"));
    assertEquals(DSL.val((String) null), result.get("density"));
    assertEquals(DSL.val((String) null), result.get("key"));
    assertEquals(DSL.val((String) null), result.get("name"));
    assertEquals(DSL.val((String) null), result.get("tempo"));
    assertEquals(0, result.get("offset"));
    assertEquals(16, result.get("total"));
  }

  @Test()
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

    failure.expect(BusinessException.class);
    failure.expectMessage("Idea ID is required");

    testDAO.create(access, inputDataWrapper);
  }

  @Test()
  public void create_FailsWithoutOffset() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setIdeaId(BigInteger.valueOf(2))
        .setKey("G minor 7")
        .setName("cannons")
        .setTempo(129.4)
        .setTotal(16)
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

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
    assertEquals(ULong.valueOf(1), result.get("ideaId"));
    assertEquals("Caterpillars", result.get("name"));
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
    assertEquals("Ants", actualResult1.get("name"));
    JSONObject actualResult2 = (JSONObject) actualResultList.get(1);
    assertEquals("Caterpillars", actualResult2.get("name"));
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

  // TODO: [core] test DAO cannot update Idea to a User or Library not owned by current session

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
        .setName(null)
        .setDensity(null)
        .setKey("")
        .setTempo((double) 0)
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);

    PhaseRecord result = IntegrationTestService.getDb()
      .selectFrom(PHASE)
      .where(PHASE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNotNull(result);
    assertEquals(null, result.getName());
    assertEquals(null, result.getDensity());
    assertEquals(null, result.getTempo());
    assertEquals(null, result.getKey());
    assertEquals(ULong.valueOf(7), result.getOffset());
    assertEquals(ULong.valueOf(32), result.getTotal());
    assertEquals(ULong.valueOf(1), result.getIdeaId());
  }

  @Test()
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

    failure.expect(BusinessException.class);
    failure.expectMessage("Idea ID is required");

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);
  }

  @Test
  public void update_TotalNotRequiredForMacroIdeaPhase() throws Exception {
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
        .setOffset(16)
      );

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);
  }

  @Test()
  public void update_TotalIsRequiredForNonMacroTypeIdeaPhase() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setIdeaId(BigInteger.valueOf(1))
        .setName("cannons")
        .setTempo(129.4)
        .setOffset(16)
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type idea, total (# beats) is required");

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);
  }

  @Test()
  public void update_TotalMustBeGreaterThanZeroForNonMacroTypeIdeaPhase() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));
    PhaseWrapper inputDataWrapper = new PhaseWrapper()
      .setPhase(new Phase()
        .setDensity(0.42)
        .setKey("G minor 7")
        .setIdeaId(BigInteger.valueOf(1))
        .setName("cannons")
        .setTempo(129.4)
        .setOffset(16)
        .setTotal(0)
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("for a phase of a non-macro-type idea, total (# beats) must be greater than zero");

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);
  }

  @Test()
  public void update_FailsWithoutOffset() throws Exception {
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
        .setTotal(16)
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    testDAO.update(access, ULong.valueOf(1), inputDataWrapper);
  }

  @Test()
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
        .setName("Smash!")
        .setTempo(129.4)
        .setOffset(0)
        .setTotal(16)
      );

    failure.expect(BusinessException.class);
    failure.expectMessage("Idea must exist");

    try {
      testDAO.update(access, ULong.valueOf(2), inputDataWrapper);

    } catch (Exception e) {
      PhaseRecord result = IntegrationTestService.getDb()
        .selectFrom(PHASE)
        .where(PHASE.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("Caterpillars", result.getName());
      assertEquals(ULong.valueOf(1), result.getIdeaId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "1"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    PhaseRecord result = IntegrationTestService.getDb()
      .selectFrom(PHASE)
      .where(PHASE.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test(expected = BusinessException.class)
  public void delete_failsIfNotInAccount() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "roles", "artist",
      "accounts", "2"
    ));

    testDAO.delete(access, ULong.valueOf(1));
  }

  @Test()
  public void delete_FailsIfIdeaHasChildRecords() throws Exception {
    AccessControl access = new AccessControl(ImmutableMap.of(
      "userId", "2",
      "roles", "artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertPhaseMeme(1, 1, "mashup");

    failure.expect(BusinessException.class);
    failure.expectMessage("Found PhaseMeme");

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
