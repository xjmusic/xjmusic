// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.exception.CoreException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import org.jooq.impl.DSL;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.time.Instant;
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
public class SegmentIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  AmazonProvider amazonProvider;
  private SegmentDAO testDAO;

  @Before
  public void setUp() throws Exception {
    reset();

    injector = Guice.createInjector(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));

    // test subject
    testDAO = injector.getInstance(SegmentDAO.class);

    // configs
    System.setProperty("segment.file.bucket", "xj-segment-test");

    // Account "Testing" has chain "Test Print #1"
    insert(newAccount(1, "Testing"));
    insert(newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now()));

    // Chain "Test Print #1" has 5 sequential segments
    insert(segmentFactory.newSegment(BigInteger.valueOf(1))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(2))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(3))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setEndAt("2017-02-14T12:03:08.000001Z"));
    insert(newSegment(5, 1, 4, Instant.parse("2017-02-14T12:03:08.000001Z")));
  }

  @After
  public void tearDown() {
    System.clearProperty("segment.file.bucket");
  }

  /**
   [#162361712] Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation
   */
  @Test
  public void create() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(5L)
      .setState("Planned")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chains-1-segments", "ogg"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    Segment result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(Long.valueOf(5L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt().toString());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.01);
    assertNull(result.getWaveformKey());
  }

  /**
   [#162361712] Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation
   [#126] Segments are always readMany in PLANNED state
   */
  @Test
  public void create_alwaysInPlannedState() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(5L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chains-1-segments", "ogg"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    Segment result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(Long.valueOf(5L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt().toString());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.1);
    assertNull(result.getWaveformKey());
  }

  @Test
  public void create_FailsIfNotUniqueChainOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(4L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chains-1-segments", "ogg"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    failure.expect(CoreException.class);
    failure.expectMessage("Found Segment at same offset in Chain");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User"
    ));
    Segment inputData = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(4L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chains-1-segments", "ogg"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    failure.expect(CoreException.class);
    failure.expectMessage("top-level access is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setOffset(4L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chains-1-segments", "ogg"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    failure.expect(CoreException.class);
    failure.expectMessage("Chain ID is required");

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
    assertEquals(Long.valueOf(1L), result.getOffset());
    assertEquals(SegmentState.Dubbing, result.getState());
    assertEquals("2017-02-14T12:01:32.000001Z", result.getBeginAt().toString());
    assertEquals("2017-02-14T12:02:04.000001Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(Double.valueOf(0.85), result.getDensity());
    assertEquals("Db minor", result.getKey());
    assertEquals(Double.valueOf(120.0), result.getTempo());
  }

  @Test
  public void readOne_failsWhenUserIsNotInChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "326"
    ));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, BigInteger.valueOf(1L));
  }

  @Test
  public void readAll() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Segment> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertNotNull(result);
    assertEquals(5L, result.size());
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
    insert(newChain(5, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "JamSandwich", now()));
    insert(segmentFactory.newSegment(BigInteger.valueOf(51))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2017-02-14T12:01:00.000001Z")
      .setUpdatedAt("2017-02-14T12:01:32.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(52))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2017-02-14T12:01:32.000001Z")
      .setUpdatedAt("2017-02-14T12:02:04.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(53))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2017-02-14T12:02:04.000001Z")
      .setUpdatedAt("2017-02-14T12:02:36.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(54))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2017-02-14T12:02:36.000001Z")
      .setUpdatedAt("2017-02-14T12:03:08.000001Z"));
    insert(newSegment(55, 5, 4, Instant.parse("2017-02-14T12:03:08.000001Z")));

    Collection<Segment> result = testDAO.readAll("JamSandwich");

    assertEquals(5L, result.size());
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

    Collection<Segment> result = testDAO.readAllFromOffset(access, BigInteger.valueOf(1L), 2L);

    assertEquals(3L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment actualResult0 = it.next();
    assertEquals(SegmentState.Planned, actualResult0.getState());
    Segment result1 = it.next();
    assertEquals(SegmentState.Crafting, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.Crafted, result2.getState());
  }

  @Test
  public void readAllFromToOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Segment> result = testDAO.readAllFromToOffset(access, BigInteger.valueOf(1L), 2L, 3L);

    assertEquals(2L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment result1 = it.next();
    assertEquals(SegmentState.Crafting, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.Crafted, result2.getState());
  }

  @Test
  public void readAllFromToOffset_acceptsNegativeOffsets_returnsEmptyCollection() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "1"
    ));

    Collection<Segment> result = testDAO.readAllFromToOffset(access, BigInteger.valueOf(1L), -1L, -1L);

    assertEquals(0L, result.size());
  }

  @Test
  public void readAllFromOffset_byChainEmbedKey() throws Exception {
    insert(newChain(5, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "JamSandwich", now()));
    insert(segmentFactory.newSegment(BigInteger.valueOf(51))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2017-02-14T12:01:00.000001Z")
      .setUpdatedAt("2017-02-14T12:01:32.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(52))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2017-02-14T12:01:32.000001Z")
      .setUpdatedAt("2017-02-14T12:02:04.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(53))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2017-02-14T12:02:04.000001Z")
      .setUpdatedAt("2017-02-14T12:02:36.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(54))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setCreatedAt("2017-02-14T12:02:36.000001Z")
      .setUpdatedAt("2017-02-14T12:03:08.000001Z"));
    insert(newSegment(55, 5, 4, Instant.parse("2017-02-14T12:03:08.000001Z")));

    Collection<Segment> result = testDAO.readAllFromOffset("JamSandwich", 2L);

    assertEquals(3L, result.size());
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

    Collection<Segment> result = testDAO.readAllFromSecondsUTC(access, BigInteger.valueOf(1L), 1487073724L);

    assertEquals(3L, result.size());
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
    insert(newChain(5, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "JamSandwich", now()));
    insert(segmentFactory.newSegment(BigInteger.valueOf(51))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(52))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(53))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z"));
    insert(segmentFactory.newSegment(BigInteger.valueOf(54))
      .setChainId(BigInteger.valueOf(5))
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setEndAt("2017-02-14T12:03:08.000001Z"));
    insert(newSegment(55, 5, 4, Instant.parse("2017-02-14T12:03:08.000001Z")));

    Collection<Segment> result = testDAO.readAllFromSecondsUTC("JamSandwich", 1487073724L);

    assertEquals(3L, result.size());
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

    Segment result = testDAO.readOneInState(access, BigInteger.valueOf(1L), SegmentState.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"));

    assertEquals(BigInteger.valueOf(5L), result.getId());
    assertEquals(BigInteger.valueOf(1L), result.getChainId());
    assertEquals(Long.valueOf(4L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals("2017-02-14T12:03:08.000001Z", result.getBeginAt().toString());
    assertNull(result.getEndAt());
  }

  @Test
  public void readOneInState_failIfNoneInChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "internal"
    ));
    insert(newChain(2, 1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now()));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOneInState(access, BigInteger.valueOf(2L), SegmentState.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "User",
      "accounts", "345"
    ));

    Collection<Segment> result = testDAO.readMany(access, ImmutableList.of(BigInteger.valueOf(1L)));

    assertEquals(0L, result.size());
  }

  @Test
  public void update() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(5L)
      .setState("Dubbed")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
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
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt().toString());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt().toString());
  }

  /**
   [#162361525] persist Segment content as JSON, then read prior Segment JSON
   */
  @Test
  public void persistPriorSegmentContent() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    segment4 = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1L))
      .setOffset(5L)
      .setState("Dubbed")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);
    segment4.putReport("funky", "chicken");

    testDAO.update(access, BigInteger.valueOf(2L), segment4);

    Segment result = testDAO.readOne(Access.internal(), BigInteger.valueOf(2L));
    assertNotNull(result);
    assertEquals("chicken", result.getReport().get("funky"));
  }

  @Test
  public void update_failsToTransitionFromDubbingToCrafting() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));

    failure.expect(CoreException.class);
    failure.expectMessage("transition to Crafting not in allowed");

    testDAO.updateState(access, BigInteger.valueOf(2L), SegmentState.Crafting);
  }

  @Test
  public void update_FailsWithoutChainID() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setOffset(4L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(CoreException.class);
    failure.expectMessage("Chain ID is required");

    testDAO.update(access, BigInteger.valueOf(2L), inputData);
  }

  @Test
  public void update_FailsToChangeChain() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Admin"
    ));
    Segment inputData = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(12L))
      .setOffset(4L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(CoreException.class);
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
    db.update(CHAIN)
      .set(CHAIN.STATE, "Erase")
      .execute();

    testDAO.destroy(Access.internal(), BigInteger.valueOf(1L));

    assertNotExist(testDAO, BigInteger.valueOf(1L));
  }

  @Test
  public void destroy_succeedsEvenIfSegmentHasNullWaveformKey() throws Exception {
    db.update(CHAIN)
      .set(CHAIN.STATE, "Erase")
      .execute();
    db.update(SEGMENT)
      .set(SEGMENT.WAVEFORM_KEY, DSL.value((String) null))
      .execute();

    testDAO.destroy(Access.internal(), BigInteger.valueOf(1L));

    verify(amazonProvider, never()).deleteS3Object("xj-segment-test", null);

    assertNotExist(testDAO, BigInteger.valueOf(1L));
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
    insertFixtureC();

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
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chains-1-segments-9f7s89d8a7892.wav");

    // Assert annihilation
    assertNotExist(testDAO, BigInteger.valueOf(17L));
  }

  @Test
  public void revert_emptiesContent() throws Exception {
    insertFixtureC();

    testDAO.revert(Access.internal(), BigInteger.valueOf(17L));

    Segment result = testDAO.readOne(Access.internal(), BigInteger.valueOf(17L));
    assertEquals(0, result.getChoices().size());
  }

}
