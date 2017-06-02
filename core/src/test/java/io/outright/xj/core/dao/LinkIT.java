// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.dao;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.CancelException;
import io.outright.xj.core.external.amazon.AmazonProvider;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.tables.records.LinkRecord;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.sql.Timestamp;

import static io.outright.xj.core.tables.Link.LINK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LinkIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private LinkDAO testDAO;
  @Mock private AmazonProvider amazonProvider;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // inject mocks
    createInjector();

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);

    // Chain "Test Print #1" has 5 sequential links
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, Link.DUBBING, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(3, 1, 2, Link.CRAFTED, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(4, 1, 3, Link.CRAFTING, Timestamp.valueOf("2017-02-14 12:02:36.000001"), Timestamp.valueOf("2017-02-14 12:03:08.000001"), "E minor", 64, 0.41, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink_Planned(5, 1, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    // Instantiate the test subject
    testDAO = injector.getInstance(LinkDAO.class);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));
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
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(5))
      .setState(Link.PLANNED)
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("chainId"));
    assertEquals(ULong.valueOf(5), result.get("offset"));
    assertEquals(Link.PLANNED, result.get("state"));
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.get("beginAt"));
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.get("endAt"));
    assertEquals(UInteger.valueOf(64), result.get("total"));
    assertEquals(0.74, result.get("density"));
    assertEquals("C# minor 7 b9", result.get("key"));
    assertEquals(120.0, result.get("tempo"));
    assertNotNull(result.get("waveformKey"));
  }

  @Test
  // [#126] Links are always readMany in PLANNED state
  public void create_alwaysInPlannedState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(5))
      .setState(Link.CRAFTING)
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    JSONObject result = JSON.objectFromRecord(testDAO.create(access, inputData));

    assertNotNull(result);
    assertEquals(ULong.valueOf(1), result.get("chainId"));
    assertEquals(ULong.valueOf(5), result.get("offset"));
    assertEquals(Link.PLANNED, result.get("state"));
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.get("beginAt"));
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.get("endAt"));
    assertEquals(UInteger.valueOf(64), result.get("total"));
    assertEquals(0.74, result.get("density"));
    assertEquals("C# minor 7 b9", result.get("key"));
    assertEquals(120.0, result.get("tempo"));
    assertNotNull(result.get("waveformKey"));
  }

  @Test
  public void create_FailsIfNotUniqueChainOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(4))
      .setState(Link.CRAFTING)
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    failure.expect(BusinessException.class);

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(4))
      .setState(Link.CRAFTING)
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    failure.expect(BusinessException.class);
    failure.expectMessage("top-level access is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setOffset(BigInteger.valueOf(4))
      .setState(Link.CRAFTING)
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithInvalidState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(4))
      .setState("mushamush")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-link", "mp3"))
      .thenReturn("chain-1-link-h2a34j5s34fd987gaw3.mp3");

    failure.expect(BusinessException.class);
    failure.expectMessage("'mushamush' is not a valid state (planned,crafting,crafted,dubbing,dubbed,failed)");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    Link result = new Link().setFromRecord(testDAO.readOne(access, ULong.valueOf(2)));

    assertNotNull(result);
    assertEquals(ULong.valueOf(2), result.getId());
    assertEquals(ULong.valueOf(1), result.getChainId());
    assertEquals(ULong.valueOf(1), result.getOffset());
    assertEquals(Link.DUBBING, result.getState());
    assertEquals(Timestamp.valueOf("2017-02-14 12:01:32.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("2017-02-14 12:02:04.000001"), result.getEndAt());
    assertEquals(UInteger.valueOf(64), result.getTotal());
    assertEquals(Double.valueOf(0.85), result.getDensity());
    assertEquals("Db minor", result.getKey());
    assertEquals(Double.valueOf(120.0), result.getTempo());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "326"
    ));

    LinkRecord result = testDAO.readOne(access, ULong.valueOf(1));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "1"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(5, result.length());

    JSONObject result4 = (JSONObject) result.get(4);
    assertEquals(Link.DUBBED, result4.get("state"));
    JSONObject result3 = (JSONObject) result.get(3);
    assertEquals(Link.DUBBING, result3.get("state"));
    JSONObject result2 = (JSONObject) result.get(2);
    assertEquals(Link.CRAFTED, result2.get("state"));
    JSONObject result1 = (JSONObject) result.get(1);
    assertEquals(Link.CRAFTING, result1.get("state"));
    JSONObject actualResult0 = (JSONObject) result.get(0);
    assertEquals(Link.PLANNED, actualResult0.get("state"));
  }

  @Test
  public void readOneInState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));

    LinkRecord result = testDAO.readOneInState(access, ULong.valueOf(1), Link.PLANNED, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    assertNotNull(result);
    assertEquals(ULong.valueOf(5), result.get("id"));
    assertEquals(ULong.valueOf(1), result.get(LINK.CHAIN_ID));
    assertEquals(ULong.valueOf(4), result.get("offset"));
    assertEquals(Link.PLANNED, result.get("state"));
    assertEquals(Timestamp.valueOf("2017-02-14 12:03:08.000001"), result.get(LINK.BEGIN_AT));
    assertNull(result.get(LINK.END_AT));
  }

  @Test
  public void readOneInState_nullIfNoneInChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(2, 1, "Test Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);

    LinkRecord result = testDAO.readOneInState(access, ULong.valueOf(2), Link.PLANNED, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    assertNull(result);
  }

  @Test
  public void readAll_SeesNothingOutsideOfChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "user",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ULong.valueOf(1)));

    assertNotNull(result);
    assertEquals(0, result.length());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(5))
      .setState(Link.DUBBED)
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    testDAO.update(access, ULong.valueOf(2), inputData);

    LinkRecord result = IntegrationTestService.getDb()
      .selectFrom(LINK)
      .where(LINK.ID.eq(ULong.valueOf(2)))
      .fetchOne();
    assertNotNull(result);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(ULong.valueOf(1), result.getChainId());
    assertEquals(Link.DUBBED, result.getState());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.getEndAt());
  }

  @Test
  public void update_failsToTransitionFromDubbingToCrafting() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    failure.expect(CancelException.class);
    failure.expectMessage("transition to crafting not in allowed (dubbing,dubbed,failed)");

    testDAO.updateState(access, ULong.valueOf(2), Link.CRAFTING);
  }

  @Test
  public void update_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setOffset(BigInteger.valueOf(4))
      .setState(Link.CRAFTING)
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test
  public void update_FailsWithInvalidState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(4))
      .setState("what a dumb-ass state")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(BusinessException.class);
    failure.expectMessage("'whatadumbassstate' is not a valid state (planned,crafting,crafted,dubbing,dubbed,failed)");

    testDAO.update(access, ULong.valueOf(2), inputData);
  }

  @Test
  public void update_FailsToChangeChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    Link inputData = new Link()
      .setChainId(BigInteger.valueOf(12))
      .setOffset(BigInteger.valueOf(4))
      .setState(Link.CRAFTING)
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(CancelException.class);
    failure.expectMessage("transition to crafting not in allowed (dubbing,dubbed,failed)");

    try {
      testDAO.update(access, ULong.valueOf(2), inputData);

    } catch (Exception e) {
      LinkRecord result = IntegrationTestService.getDb()
        .selectFrom(LINK)
        .where(LINK.ID.eq(ULong.valueOf(2)))
        .fetchOne();
      assertNotNull(result);
      assertEquals("Db minor", result.getKey());
      assertEquals(ULong.valueOf(1), result.getChainId());
      throw e;
    }
  }

  @Test
  public void delete() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));

    testDAO.delete(access, ULong.valueOf(1));

    LinkRecord result = IntegrationTestService.getDb()
      .selectFrom(LINK)
      .where(LINK.ID.eq(ULong.valueOf(1)))
      .fetchOne();
    assertNull(result);
  }

  @Test
  public void delete_FailsIfLinkHasChildRecords() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "admin"
    ));
    IntegrationTestEntity.insertLinkChord(1, 1, 1.5, "C minor");

    failure.expect(BusinessException.class);
    failure.expectMessage("Found Chord in Link");

    try {
      testDAO.delete(access, ULong.valueOf(1));

    } catch (Exception e) {
      LinkRecord stillExistingRecord = IntegrationTestService.getDb()
        .selectFrom(LINK)
        .where(LINK.ID.eq(ULong.valueOf(1)))
        .fetchOne();
      assertNotNull(stillExistingRecord);
      throw e;
    }
  }
}
