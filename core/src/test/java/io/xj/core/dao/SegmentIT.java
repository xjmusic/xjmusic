// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.CancelException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.transport.JSON;

import org.jooq.impl.DSL;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.json.JSONArray;
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
import java.util.Collection;
import java.util.Iterator;

import static io.xj.core.Tables.CHAIN;
import static io.xj.core.tables.Segment.SEGMENT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SegmentIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  @Mock AmazonProvider amazonProvider;
  private Injector injector;
  private SegmentDAO testDAO;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // inject mocks
    createInjector();

    // configs
    System.setProperty("segment.file.bucket", "xj-segment-test");

    // Account "Testing" has chain "Test Print #1"
    IntegrationTestEntity.insertAccount(1, "Testing");
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    // Chain "Test Print #1" has 5 sequential segments
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(3, 1, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(4, 1, 3, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:02:36.000001"), Timestamp.valueOf("2017-02-14 12:03:08.000001"), "E minor", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment_Planned(5, 1, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    // Instantiate the test subject
    testDAO = injector.getInstance(SegmentDAO.class);
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
    injector = null;

    System.clearProperty("segment.file.bucket");
  }

  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = new Segment()
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(5L))
      .setState("Planned")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-segment", "ogg"))
      .thenReturn("chain-1-segment-h2a34j5s34fd987gaw3.ogg");

    Segment result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(5L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.getEndAt());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.01);
    assertNotNull(result.getWaveformKey());
  }

  @Test
  // [#126] Segments are always readMany in PLANNED state
  public void create_alwaysInPlannedState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = new Segment()
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(5L))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-segment", "ogg"))
      .thenReturn("chain-1-segment-h2a34j5s34fd987gaw3.ogg");

    Segment result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(5L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.getEndAt());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.1);
    assertNotNull(result.getWaveformKey());
  }

  @Test
  public void create_FailsIfNotUniqueChainOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = new Segment()
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(4L))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-segment", "ogg"))
      .thenReturn("chain-1-segment-h2a34j5s34fd987gaw3.ogg");

    failure.expect(BusinessException.class);
    failure.expectMessage("Found Segment at same offset in Chain");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User"
    ));
    Segment inputData = new Segment()
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(4L))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-segment", "ogg"))
      .thenReturn("chain-1-segment-h2a34j5s34fd987gaw3.ogg");

    failure.expect(BusinessException.class);
    failure.expectMessage("top-level access is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = new Segment()
      .setOffset(BigInteger.valueOf(4L))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-segment", "ogg"))
      .thenReturn("chain-1-segment-h2a34j5s34fd987gaw3.ogg");

    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithInvalidState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = new Segment()
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(4L))
      .setState("mushamush")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chain-1-segment", "ogg"))
      .thenReturn("chain-1-segment-h2a34j5s34fd987gaw3.ogg");

    failure.expect(BusinessException.class);
    failure.expectMessage("'mushamush' is not a valid state");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Segment result = testDAO.readOne(access, BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(2L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(1L), result.getOffset());
    assertEquals(SegmentState.Dubbing, result.getState());
    assertEquals(Timestamp.valueOf("2017-02-14 12:01:32.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("2017-02-14 12:02:04.000001"), result.getEndAt());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(Double.valueOf(0.85), result.getDensity());
    assertEquals("Db minor", result.getKey());
    assertEquals(Double.valueOf(120.0), result.getTempo());
  }

  @Test
  public void readOne_FailsWhenUserIsNotInChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));

    Segment result = testDAO.readOne(access, BigInteger.valueOf(1L));

    assertNull(result);
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Segment> result = testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(5L, (long) result.size());
    Iterator<Segment> it = result.iterator();

    Segment actualResult0 = it.next();
    assertEquals(SegmentState.Planned, actualResult0.getState());

    Segment result1 = it.next();
    assertEquals(SegmentState.Crafting, result1.getState());

    Segment result2 = it.next();
    assertEquals(SegmentState.Crafted, result2.getState());

    Segment result3 = it.next();
    assertEquals(SegmentState.Dubbing, result3.getState());

    Segment result4 = it.next();
    assertEquals(SegmentState.Dubbed, result4.getState());
  }

  @Test
  public void readAll_byChainEmbedKey() throws Exception {
    IntegrationTestEntity.insertChain(5, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, "JamSandwich");
    IntegrationTestEntity.insertSegment(51, 5, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(52, 5, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(53, 5, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(54, 5, 3, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:02:36.000001"), Timestamp.valueOf("2017-02-14 12:03:08.000001"), "E minor", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment_Planned(55, 5, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    Collection<Segment> result = testDAO.readAll("JamSandwich");

    assertNotNull(result);
    assertEquals(5L, (long) result.size());
    Iterator<Segment> it = result.iterator();

    Segment actualResult0 = it.next();
    assertEquals(SegmentState.Planned, actualResult0.getState());

    Segment result1 = it.next();
    assertEquals(SegmentState.Crafting, result1.getState());

    Segment result2 = it.next();
    assertEquals(SegmentState.Crafted, result2.getState());

    Segment result3 = it.next();
    assertEquals(SegmentState.Dubbing, result3.getState());

    Segment result4 = it.next();
    assertEquals(SegmentState.Dubbed, result4.getState());
  }

  @Test
  public void readAllFromOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Segment> result = testDAO.readAllFromOffset(access, BigInteger.valueOf(1L), BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(3L, (long) result.size());
    Iterator<Segment> it = result.iterator();

    Segment actualResult0 = it.next();
    assertEquals(SegmentState.Planned, actualResult0.getState());

    Segment result1 = it.next();
    assertEquals(SegmentState.Crafting, result1.getState());

    Segment result2 = it.next();
    assertEquals(SegmentState.Crafted, result2.getState());
  }

  @Test
  public void readAllFromOffset_byChainEmbedKey() throws Exception {
    IntegrationTestEntity.insertChain(5, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, "JamSandwich");
    IntegrationTestEntity.insertSegment(51, 5, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(52, 5, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(53, 5, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(54, 5, 3, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:02:36.000001"), Timestamp.valueOf("2017-02-14 12:03:08.000001"), "E minor", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment_Planned(55, 5, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    Collection<Segment> result = testDAO.readAllFromOffset("JamSandwich", BigInteger.valueOf(2L));

    assertNotNull(result);
    assertEquals(3L, (long) result.size());
    Iterator<Segment> it = result.iterator();

    Segment actualResult0 = it.next();
    assertEquals(SegmentState.Planned, actualResult0.getState());

    Segment result1 = it.next();
    assertEquals(SegmentState.Crafting, result1.getState());

    Segment result2 = it.next();
    assertEquals(SegmentState.Crafted, result2.getState());

  }

  @Test
  public void readAllFromSecondsUTC() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Segment> result = testDAO.readAllFromSecondsUTC(access, BigInteger.valueOf(1L), BigInteger.valueOf(1487102524L));

    assertNotNull(result);
    assertEquals(3L, (long) result.size());
    Iterator<Segment> it = result.iterator();

    Segment actualResult0 = it.next();
    assertEquals(SegmentState.Crafting, actualResult0.getState());

    Segment result1 = it.next();
    assertEquals(SegmentState.Crafted, result1.getState());

    Segment result2 = it.next();
    assertEquals(SegmentState.Dubbing, result2.getState());
  }

  @Test
  public void readAllFromSecondsUTC_byChainEmbedKey() throws Exception {
    IntegrationTestEntity.insertChain(5, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, "JamSandwich");
    IntegrationTestEntity.insertSegment(51, 5, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(52, 5, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(53, 5, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(54, 5, 3, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:02:36.000001"), Timestamp.valueOf("2017-02-14 12:03:08.000001"), "E minor", 64, 0.41, 120.0, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment_Planned(55, 5, 4, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    Collection<Segment> result = testDAO.readAllFromSecondsUTC("JamSandwich", BigInteger.valueOf(1487102524L));

    assertNotNull(result);
    assertEquals(3L, (long) result.size());
    Iterator<Segment> it = result.iterator();

    Segment actualResult0 = it.next();
    assertEquals(SegmentState.Crafting, actualResult0.getState());

    Segment result1 = it.next();
    assertEquals(SegmentState.Crafted, result1.getState());

    Segment result2 = it.next();
    assertEquals(SegmentState.Dubbing, result2.getState());

  }

  @Test
  public void readOneInState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));

    Segment result = testDAO.readOneInState(access, BigInteger.valueOf(1L), SegmentState.Planned, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(5L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(BigInteger.valueOf(4L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals(Timestamp.valueOf("2017-02-14 12:03:08.000001"), result.getBeginAt());
    assertNull(result.getEndAt());
  }

  @Test
  public void readOneInState_nullIfNoneInChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    IntegrationTestEntity.insertChain(2, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    Segment result = testDAO.readOneInState(access, BigInteger.valueOf(2L), SegmentState.Planned, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    assertNull(result);
  }

  @Test
  public void readAll_SeesNothingOutsideOfChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    JSONArray result = JSON.arrayOf(testDAO.readAll(access, ImmutableList.of(BigInteger.valueOf(1L))));

    assertNotNull(result);
    assertEquals(0L, (long) result.length());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = new Segment()
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(5L))
      .setState("Dubbed")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    testDAO.update(access, BigInteger.valueOf(2L), inputData);

    Segment result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(SegmentState.Dubbed, result.getState());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:00.000001"), result.getBeginAt());
    assertEquals(Timestamp.valueOf("1995-04-28 11:23:32.000001"), result.getEndAt());
  }

  @Test
  public void update_failsToTransitionFromDubbingToCrafting() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    failure.expect(CancelException.class);
    failure.expectMessage("transition to Crafting not in allowed");

    testDAO.updateState(access, BigInteger.valueOf(2L), SegmentState.Crafting);
  }

  @Test
  public void update_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = new Segment()
      .setOffset(BigInteger.valueOf(4L))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_FailsWithInvalidState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = new Segment()
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(BigInteger.valueOf(4L))
      .setState("what a dumb-ass state")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(BusinessException.class);
    failure.expectMessage("'what a dumb-ass state' is not a valid state");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_FailsToChangeChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = new Segment()
      .setChainId(BigInteger.valueOf(12L))
      .setOffset(BigInteger.valueOf(4L))
      .setState("Crafting")
      .setBeginAt("1995-04-28 11:23:00.000001")
      .setEndAt("1995-04-28 11:23:32.000001")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(CancelException.class);
    failure.expectMessage("transition to Crafting not in allowed");

    try {
      testDAO.update(access, BigInteger.valueOf(2L), inputData);

    } catch (Exception e) {
      Segment result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
      assertNotNull(result);
      assertEquals("Db minor", result.getKey());
      assertEquals(BigInteger.valueOf(1L), result.getChainId());
      throw e;
    }
  }

  @Test
  public void destroy() throws Exception {
    IntegrationTestService.getDb().update(CHAIN)
      .set(CHAIN.STATE, "Erase")
      .execute();

    testDAO.destroy(Access.internal(), BigInteger.valueOf(1L));

    Segment result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNull(result);
  }

  @Test
  public void destroy_succeedsEvenIfSegmentHasNullWaveformKey() throws Exception {
    IntegrationTestService.getDb().update(CHAIN)
      .set(CHAIN.STATE, "Erase")
      .execute();
    IntegrationTestService.getDb().update(SEGMENT)
      .set(SEGMENT.WAVEFORM_KEY, DSL.value((String) null))
      .execute();

    testDAO.destroy(Access.internal(), BigInteger.valueOf(1L));

    verify(amazonProvider, never()).deleteS3Object("xj-segment-test", null);

    Segment result = testDAO.readOne(Access.internal(), BigInteger.valueOf(1L));
    assertNull(result);
  }

  @Test
  public void destroy_okRegardlessOfChainState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    testDAO.destroy(access, BigInteger.valueOf(1L));
  }

  @Test
  public void destroy_allChildEntities() throws Exception {
    // User "bill"
    IntegrationTestEntity.insertUser(2, "bill", "bill@email.com", "http://pictures.com/bill.gif");

    // Library "test sounds"
    IntegrationTestEntity.insertLibrary(1, 1, "test sounds");
    IntegrationTestEntity.insertSequence(1, 2, 1, SequenceType.Macro, SequenceState.Published, "epic concept", 0.342, "C#", 0.286);
    IntegrationTestEntity.insertPattern(1, 1, PatternType.Macro, PatternState.Published, 0, 16, "Ants", 0.583, "D minor", 120.0);
    IntegrationTestEntity.insertVoice(8, 1, InstrumentType.Percussive, "This is a percussive voice");
    IntegrationTestEntity.insertPatternEvent(1, 1, 8, (double) 0, 1.0, "KICK", "C", 0.8, 1.0);

    // Library has Instrument with Audio
    IntegrationTestEntity.insertInstrument(9, 1, 2, "jams", InstrumentType.Percussive, 0.6);
    IntegrationTestEntity.insertAudio(1, 9, "Published", "Kick", "https://static.xj.io/instrument/percussion/808/kick1.wav", 0.01, 2.123, 120.0, 440.0);

    // Chain "Test Print #1" has one segment
    IntegrationTestEntity.insertChain(3, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Timestamp.valueOf("2014-08-12 12:17:02.527142"), Timestamp.valueOf("2014-09-11 12:17:01.047563"), null);
    IntegrationTestEntity.insertSegment(17, 3, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120.0, "chain-1-segment-97898asdf7892.ogg");

    // Segment Meme
    IntegrationTestEntity.insertSegmentMeme(25, 17, "Jams");

    // Segment Chord
    IntegrationTestEntity.insertSegmentChord(25, 17, (double) 0, "D major 7 b9");

    // Segment Message
    IntegrationTestEntity.insertSegmentMessage(25, 17, MessageType.Warning, "Consider yourself warned");

    // Choice
    IntegrationTestEntity.insertChoice(1, 17, 1, SequenceType.Macro, 2, -5);

    // Arrangement
    IntegrationTestEntity.insertArrangement(1, 1, 8, 9);

    // FUTURE: determine new test vector for [#154014731] persist Audio pick in memory

    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    //
    // Go!
    testDAO.destroy(access, BigInteger.valueOf(17L));
    //
    //

    // [#263] expect request to delete segment waveform from Amazon S3
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chain-1-segment-97898asdf7892.ogg");

    // Assert destroyed Segment
    assertNull(testDAO.readOne(Access.internal(), BigInteger.valueOf(17L)));

    // Assert destroyed Segment Meme
    assertNull(injector.getInstance(SegmentMemeDAO.class).readOne(Access.internal(), BigInteger.valueOf(25L)));

    // Assert destroyed Segment Chord
    assertNull(injector.getInstance(SegmentChordDAO.class).readOne(Access.internal(), BigInteger.valueOf(25L)));

    // Assert destroyed Segment Message
    assertNull(injector.getInstance(SegmentMessageDAO.class).readOne(Access.internal(), BigInteger.valueOf(25L)));

    // Assert destroyed Arrangement
    assertNull(injector.getInstance(ArrangementDAO.class).readOne(Access.internal(), BigInteger.valueOf(1L)));

    // Assert destroyed Choice
    assertNull(injector.getInstance(ChoiceDAO.class).readOne(Access.internal(), BigInteger.valueOf(1L)));

  }


}
