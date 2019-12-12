// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.dao;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.exception.CoreException;
import io.xj.core.external.AmazonProvider;
import io.xj.core.model.Account;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentState;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.Assert;
import io.xj.core.testing.IntegrationTestProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;

import static io.xj.core.Tables.CHAIN;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SegmentIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  AmazonProvider amazonProvider;
  private SegmentDAO testDAO;

  private IntegrationTestProvider test;
  private IntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      })));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    test.reset();

    // test subject
    testDAO = injector.getInstance(SegmentDAO.class);

    // configs
    System.setProperty("segment.file.bucket", "xj-segment-test");

    // Account "Testing" has chain "Test Print #1"
    fake.account1 = test.insert(Account.create("Testing"));
    fake.chain3 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));

    // Chain "Test Print #1" has 5 sequential segments
    fake.segment1 = test.insert(Segment.create()
      .setChainId(fake.chain3.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z"));
    fake.segment2 = test.insert(Segment.create()
      .setChainId(fake.chain3.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z"));
    fake.segment3 = test.insert(Segment.create()
      .setChainId(fake.chain3.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z"));
    fake.segment4 = test.insert(Segment.create()
      .setChainId(fake.chain3.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setEndAt("2017-02-14T12:03:08.000001Z"));
    fake.segment5 = test.insert(Segment.create()
      .setChainId(fake.chain3.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setStateEnum(SegmentState.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  /**
   [#162361712] Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation
   */
  @Test
  public void create() throws Exception {
    Access access = Access.create("Admin");
    Segment inputData = Segment.create()
      .setChainId(fake.chain3.getId())
      .setOffset(5L)
      .setState("Planned")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chains-1-segments", "ogg"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    Segment result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(5L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt().toString());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.01);
    assertNotNull(result.getWaveformKey());
  }

  /**
   [#162361712] Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation
   [#126] Segments are always readMany in PLANNED state
   */
  @Test
  public void create_alwaysInPlannedState() throws Exception {
    Access access = Access.create("Admin");
    Segment inputData = Segment.create()
      .setChainId(fake.chain3.getId())
      .setOffset(5L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(amazonProvider.generateKey("chains-1-segments", "ogg"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    Segment result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(fake.chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(5L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt().toString());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.1);
    assertNotNull(result.getWaveformKey());
  }

  @Test
  public void create_FailsIfNotUniqueChainOffset() throws Exception {
    Access access = Access.create("Admin");
    Segment inputData = Segment.create()
      .setChainId(fake.chain3.getId())
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
    Access access = Access.create("User");
    Segment inputData = Segment.create()
      .setChainId(fake.chain3.getId())
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
    Access access = Access.create("Admin");
    Segment inputData = Segment.create()
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
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Segment result = testDAO.readOne(access, fake.segment2.getId());

    assertNotNull(result);
    assertEquals(fake.segment2.getId(), result.getId());
    assertEquals(fake.chain3.getId(), result.getChainId());
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
    Access access = Access.create(ImmutableList.of(Account.create()), "User");
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOne(access, fake.segment1.getId());
  }

  @Test
  public void readAll() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<Segment> result = testDAO.readMany(access, ImmutableList.of(fake.chain3.getId()));

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
    fake.chain5 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "JamSandwich"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setCreatedAt("2017-02-14T12:01:00.000001Z")
      .setUpdatedAt("2017-02-14T12:01:32.000001Z"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setCreatedAt("2017-02-14T12:01:32.000001Z")
      .setUpdatedAt("2017-02-14T12:02:04.000001Z"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setCreatedAt("2017-02-14T12:02:04.000001Z")
      .setUpdatedAt("2017-02-14T12:02:36.000001Z"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setCreatedAt("2017-02-14T12:02:36.000001Z")
      .setUpdatedAt("2017-02-14T12:03:08.000001Z"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setStateEnum(SegmentState.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

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
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<Segment> result = testDAO.readAllFromOffset(access, fake.chain3.getId(), 2L);

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
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<Segment> result = testDAO.readAllFromToOffset(access, fake.chain3.getId(), 2L, 3L);

    assertEquals(2L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment result1 = it.next();
    assertEquals(SegmentState.Crafting, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.Crafted, result2.getState());
  }

  @Test
  public void readAllFromToOffset_acceptsNegativeOffsets_returnsEmptyCollection() throws Exception {
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<Segment> result = testDAO.readAllFromToOffset(access, fake.chain3.getId(), -1L, -1L);

    assertEquals(0L, result.size());
  }

  @Test
  public void readAllFromOffset_byChainEmbedKey() throws Exception {
    fake.chain5 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "JamSandwich"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setCreatedAt("2017-02-14T12:01:00.000001Z")
      .setUpdatedAt("2017-02-14T12:01:32.000001Z"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setCreatedAt("2017-02-14T12:01:32.000001Z")
      .setUpdatedAt("2017-02-14T12:02:04.000001Z"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setCreatedAt("2017-02-14T12:02:04.000001Z")
      .setUpdatedAt("2017-02-14T12:02:36.000001Z"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setCreatedAt("2017-02-14T12:02:36.000001Z")
      .setUpdatedAt("2017-02-14T12:03:08.000001Z"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setStateEnum(SegmentState.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

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
    Access access = Access.create(ImmutableList.of(fake.account1), "User");

    Collection<Segment> result = testDAO.readAllFromSecondsUTC(access, fake.chain3.getId(), 1487073724L);

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
    fake.chain5 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "JamSandwich"));
    test.insert(Segment.create().setChainId(fake.chain5.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z"));
    test.insert(Segment.create().setChainId(fake.chain5.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z"));
    test.insert(Segment.create().setChainId(fake.chain5.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z"));
    test.insert(Segment.create().setChainId(fake.chain5.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setEndAt("2017-02-14T12:03:08.000001Z"));
    test.insert(Segment.create()
      .setChainId(fake.chain5.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setStateEnum(SegmentState.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));

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
    Access access = Access.create("Internal");

    Segment result = testDAO.readOneInState(access, fake.chain3.getId(), SegmentState.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"));

    assertEquals(fake.segment5.getId(), result.getId());
    assertEquals(fake.chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(4L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals("2017-02-14T12:03:08.000001Z", result.getBeginAt().toString());
    assertNull(result.getEndAt());
  }

  @Test
  public void readOneInState_failIfNoneInChain() throws Exception {
    Access access = Access.create("Internal");
    test.insert(Chain.create(fake.account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    failure.expect(CoreException.class);
    failure.expectMessage("does not exist");

    testDAO.readOneInState(access, fake.segment2.getId(), SegmentState.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"));
  }

  @Test
  public void readAll_SeesNothingOutsideOfChain() throws Exception {
    Access access = Access.create(ImmutableList.of(Account.create()), "User");

    Collection<Segment> result = testDAO.readMany(access, ImmutableList.of(fake.segment1.getId()));

    assertEquals(0L, result.size());
  }

  @Test
  public void update() throws Exception {
    Access access = Access.create("Admin");
    Segment inputData = Segment.create()
      .setChainId(fake.chain3.getId())
      .setOffset(5L)
      .setState("Dubbed")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    testDAO.update(access, fake.segment2.getId(), inputData);

    Segment result = testDAO.readOne(Access.internal(), fake.segment2.getId());
    assertNotNull(result);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(fake.chain3.getId(), result.getChainId());
    assertEquals(SegmentState.Dubbed, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt().toString());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt().toString());
  }

  /**
   [#162361525] persist Segment content as JSON, then read prior Segment JSON
   */
  @Test
  public void persistPriorSegmentContent() throws Exception {
    Access access = Access.create("Admin");
    fake.segment4 = Segment.create()
      .setChainId(fake.chain3.getId())
      .setOffset(5L)
      .setState("Dubbed")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav")
      .setTempo(120.0);

    testDAO.update(access, fake.segment2.getId(), fake.segment4);

    Segment result = testDAO.readOne(Access.internal(), fake.segment2.getId());
    assertNotNull(result);
  }

  @Test
  public void update_failsToTransitionFromDubbingToCrafting() throws Exception {
    Access access = Access.create("Admin");

    failure.expect(CoreException.class);
    failure.expectMessage("transition to Crafting not in allowed");

    testDAO.updateState(access, fake.segment2.getId(), SegmentState.Crafting);
  }

  @Test
  public void update_FailsWithoutChainID() throws Exception {
    Access access = Access.create("Admin");
    Segment inputData = Segment.create()
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

    testDAO.update(access, fake.segment2.getId(), inputData);
  }

  @Test
  public void update_FailsToChangeChain() throws Exception {
    Access access = Access.create("Admin");
    Segment inputData = Segment.create()
      .setChainId(fake.chain3.getId())
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
      testDAO.update(access, fake.segment2.getId(), inputData);

    } catch (Exception e) {
      Segment result = testDAO.readOne(Access.internal(), fake.segment2.getId());
      assertNotNull(result);
      assertEquals("Db minor", result.getKey());
      assertEquals(fake.chain3.getId(), result.getChainId());
      throw e;
    }
  }

  @Test
  public void destroy() throws Exception {
    test.getDSL().update(CHAIN)
      .set(CHAIN.STATE, "Erase")
      .execute();

    testDAO.destroy(Access.internal(), fake.segment1.getId());

    Assert.assertNotExist(testDAO, fake.segment1.getId());
  }

  @Test
  public void destroy_okRegardlessOfChainState() throws Exception {
    Access access = Access.create("Admin");

    testDAO.destroy(access, fake.segment1.getId());
  }

  @Test
  public void destroy_allChildEntities() throws Exception {
    fake.insertFixtureC();

    // FUTURE: determine new test vector for [#154014731] persist Audio pick in memory

    Access access = Access.create("Admin");

    //
    // Go!
    testDAO.destroy(access, fake.segment1.getId());
    //
    //

    // [#263] expect request to delete segment waveform of Amazon S3
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chains-1-segments-9f7s89d8a7892.wav");

    // Assert annihilation
    Assert.assertNotExist(testDAO, fake.segment1.getId());
  }

  // TODO test revert deletes all related entities

}
