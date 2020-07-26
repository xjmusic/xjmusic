// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.entity.Account;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.ChainType;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentState;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.testing.NexusTestConfiguration;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SegmentDAOImplTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  FileStoreProvider fileStoreProvider;
  private NexusEntityStore store;
  private SegmentDAO testDAO;
  private Account account1;
  private Chain chain3;
  private Segment segment1;
  private Segment segment2;
  private Segment segment3;
  private Segment segment4;
  private Segment segment5;
  private Segment segment6;
  private Chain chain5;
  private EntityFactory entityFactory;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault()
      .withValue("segment.limitReadSize", ConfigValueFactory.fromAnyRef(12));
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new NexusDAOModule()));
    entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // test subject
    testDAO = injector.getInstance(SegmentDAO.class);

    // Account "Testing" has chain "Test Print #1"
    account1 = Account.create("Testing");
    chain3 = store.put(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));

    // Chain "Test Print #1" has 5 sequential segments
    segment1 = store.put(Segment.create()
      .setChainId(chain3.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z"));
    segment2 = store.put(Segment.create()
      .setChainId(chain3.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setWaveformPreroll(1.523)
      .setEndAt("2017-02-14T12:02:04.000001Z"));
    segment3 = store.put(Segment.create()
      .setChainId(chain3.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z"));
    segment4 = store.put(Segment.create()
      .setChainId(chain3.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setEndAt("2017-02-14T12:03:08.000001Z"));
    segment5 = store.put(Segment.create()
      .setChainId(chain3.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setStateEnum(SegmentState.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));
  }

  /**
   [#162361712] Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation
   */
  @Test
  public void create() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Segment inputData = Segment.create()
      .setChainId(chain3.getId())
      .setOffset(5L)
      .setState("Planned")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setWaveformPreroll(2.898)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(fileStoreProvider.generateKey("chains-1-segments"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    Segment result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(5L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt().toString());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals(Double.valueOf(2.898), result.getWaveformPreroll());
    assertNotNull(result.getStorageKey());
  }

  /**
   [#162361712] Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation
   [#126] Segments are always readMany in PLANNED state
   */
  @Test
  public void create_alwaysInPlannedState() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Segment inputData = Segment.create()
      .setChainId(chain3.getId())
      .setOffset(5L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(fileStoreProvider.generateKey("chains-1-segments"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    Segment result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(5L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt().toString());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.1);
    assertNotNull(result.getStorageKey());
  }

  @Test
  public void create_FailsIfNotUniqueChainOffset() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Segment inputData = Segment.create()
      .setChainId(chain3.getId())
      .setOffset(4L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(fileStoreProvider.generateKey("chains-1-segments"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Found Segment at same offset in Chain");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    HubClientAccess access = HubClientAccess.create("User");
    Segment inputData = Segment.create()
      .setChainId(chain3.getId())
      .setOffset(4L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(fileStoreProvider.generateKey("chains-1-segments"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("top-level access is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutChainID() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Segment inputData = Segment.create()
      .setOffset(4L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    when(fileStoreProvider.generateKey("chains-1-segments"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    failure.expect(DAOValidationException.class);

    failure.expectMessage("Chain ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    Segment result = testDAO.readOne(access, segment2.getId());

    assertNotNull(result);
    assertEquals(segment2.getId(), result.getId());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(1L), result.getOffset());
    assertEquals(SegmentState.Dubbing, result.getState());
    assertEquals("2017-02-14T12:01:32.000001Z", result.getBeginAt().toString());
    assertEquals("2017-02-14T12:02:04.000001Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(Double.valueOf(0.85), result.getDensity());
    assertEquals("Db minor", result.getKey());
    assertEquals(Double.valueOf(120.0), result.getTempo());
    assertEquals(Double.valueOf(1.523), result.getWaveformPreroll());
  }

  @Test
  public void readOne_failsWhenUserIsNotInChain() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(Account.create()), "User");
    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("access is required");

    testDAO.readOne(access, segment1.getId());
  }

  @Test
  public void readMany() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    Collection<Segment> result = testDAO.readMany(access, ImmutableList.of(chain3.getId()));

    assertNotNull(result);
    assertEquals(5L, result.size());
    Iterator<Segment> it = result.iterator();

    Segment result0 = it.next();
    assertEquals(SegmentState.Dubbed, result0.getState());

    Segment result1 = it.next();
    assertEquals(SegmentState.Dubbing, result1.getState());

    Segment result2 = it.next();
    assertEquals(SegmentState.Crafted, result2.getState());

    Segment result3 = it.next();
    assertEquals(SegmentState.Crafting, result3.getState());

    Segment result4 = it.next();
    assertEquals(SegmentState.Planned, result4.getState());
  }

  /**
   [#173806948] List of Segments returned should not be more than a dozen or so
   */
  @Test
  public void readMany_doesNotExceedReadLimit() throws Exception {
    for (int i = 0; i < 20; i++)
      store.put(Segment.create()
        .setChainId(chain3.getId())
        .setOffset(4L)
        .setState("Crafting")
        .setBeginAt("1995-04-28T11:23:00.000001Z")
        .setEndAt("1995-04-28T11:23:32.000001Z")
        .setTotal(64)
        .setDensity(0.74)
        .setKey("C# minor 7 b9")
        .setTempo(120.0));
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    Collection<Segment> result = testDAO.readMany(access, ImmutableList.of(chain3.getId()));

    assertNotNull(result);
    assertEquals(12L, result.size());
  }

  /**
   [#173806948] List of Segments returned should not be more than a dozen or so
   */
  @Test
  public void readMany_byChainEmbedKey_doesNotExceedReadLimit() throws Exception {
    chain5 = store.put(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "Barnacles"));
    for (int i = 0; i < 20; i++)
      store.put(Segment.create()
        .setChainId(chain5.getId())
        .setOffset(4L)
        .setState("Crafting")
        .setBeginAt("1995-04-28T11:23:00.000001Z")
        .setEndAt("1995-04-28T11:23:32.000001Z")
        .setTotal(64)
        .setDensity(0.74)
        .setKey("C# minor 7 b9")
        .setTempo(120.0));
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    Collection<Segment> result = testDAO.readMany(HubClientAccess.internal(), "barnacles");

    assertNotNull(result);
    assertEquals(12L, result.size());
  }

  @Test
  public void readMany_byChainEmbedKey() throws Exception {
    chain5 = store.put(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "Barnacles"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setCreatedAt("2017-02-14T12:01:00.000001Z")
      .setUpdatedAt("2017-02-14T12:01:32.000001Z"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setCreatedAt("2017-02-14T12:01:32.000001Z")
      .setUpdatedAt("2017-02-14T12:02:04.000001Z"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setCreatedAt("2017-02-14T12:02:04.000001Z")
      .setUpdatedAt("2017-02-14T12:02:36.000001Z"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setCreatedAt("2017-02-14T12:02:36.000001Z")
      .setUpdatedAt("2017-02-14T12:03:08.000001Z"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setStateEnum(SegmentState.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Collection<Segment> result = testDAO.readMany(HubClientAccess.internal(), "barnacles");

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
  public void readManyFromOffset() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    Collection<Segment> result = testDAO.readManyFromOffset(access, chain3.getId(), 2L);

    assertEquals(3L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment actualResult0 = it.next();
    assertEquals(SegmentState.Crafted, actualResult0.getState());
    Segment result1 = it.next();
    assertEquals(SegmentState.Crafting, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.Planned, result2.getState());
  }

  @Test
  public void readManyFromToOffset() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    Collection<Segment> result = testDAO.readManyFromToOffset(access, chain3.getId(), 2L, 3L);

    assertEquals(2L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment result1 = it.next();
    assertEquals(SegmentState.Crafted, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.Crafting, result2.getState());
  }

  @Test
  public void readManyFromToOffset_acceptsNegativeOffsets_returnsEmptyCollection() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    Collection<Segment> result = testDAO.readManyFromToOffset(access, chain3.getId(), -1L, -1L);

    assertEquals(0L, result.size());
  }

  @Test
  public void readManyFromOffset_byChainEmbedKey() throws Exception {
    chain5 = store.put(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "Barnacles"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setCreatedAt("2017-02-14T12:01:00.000001Z")
      .setUpdatedAt("2017-02-14T12:01:32.000001Z"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setCreatedAt("2017-02-14T12:01:32.000001Z")
      .setUpdatedAt("2017-02-14T12:02:04.000001Z"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setCreatedAt("2017-02-14T12:02:04.000001Z")
      .setUpdatedAt("2017-02-14T12:02:36.000001Z"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setCreatedAt("2017-02-14T12:02:36.000001Z")
      .setUpdatedAt("2017-02-14T12:03:08.000001Z"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setStateEnum(SegmentState.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Collection<Segment> result = testDAO.readManyFromOffset(HubClientAccess.internal(), "barnacles", 2L);

    assertEquals(3L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment actualResult0 = it.next();
    assertEquals(SegmentState.Crafted, actualResult0.getState());
    Segment result1 = it.next();
    assertEquals(SegmentState.Crafting, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.Planned, result2.getState());
  }

  @Test
  public void readManyFromSecondsUTC() throws Exception {
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    Collection<Segment> result = testDAO.readManyFromSecondsUTC(access, chain3.getId(), 1487073724L);

    assertEquals(3L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment actualResult0 = it.next();
    assertEquals(SegmentState.Dubbing, actualResult0.getState());
    Segment result1 = it.next();
    assertEquals(SegmentState.Crafted, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.Crafting, result2.getState());
  }

  /**
   [#170299748] Player should always load what it needs next--
   currently possible to load too far into the future, causing playback delay
   */
  @Test
  public void readManyFromSecondsUTC_limitedFromNow_notLatestSegment() throws Exception {
    long fromSecondsUTC = 1487073724L;
    Instant beginAt = Instant.ofEpochSecond(fromSecondsUTC);
    int numSegmentsToGenerate = 50;
    int total = 16;
    int tempo = 120;
    chain5 = store.put(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, beginAt, null, "Barnacles"));
    for (int offset = 0; offset < numSegmentsToGenerate; offset++) {
      Instant endAt = beginAt.plusMillis(1000 * total * 60 / tempo);
      store.put(Segment.create(chain5, offset, SegmentState.Dubbed,
        beginAt, endAt, "D major", total, 0.73, tempo,
        "chains-1-segments-9f7s89d8a7892.wav"));
      beginAt = endAt;
    }
    HubClientAccess access = HubClientAccess.create(ImmutableList.of(account1), "User");

    Collection<Segment> result = testDAO.readManyFromSecondsUTC(access, chain5.getId(), fromSecondsUTC + 1);

    assertEquals(12L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment firstReturnedSegment = it.next();
    assertEquals(Instant.ofEpochSecond(fromSecondsUTC), firstReturnedSegment.getBeginAt());
  }

  @Test
  public void readManyFromSecondsUTC_byChainEmbedKey() throws Exception {
    chain5 = store.put(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "barnacles"));
    store.put(Segment.create().setChainId(chain5.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z"));
    store.put(Segment.create().setChainId(chain5.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z"));
    store.put(Segment.create().setChainId(chain5.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z"));
    store.put(Segment.create().setChainId(chain5.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setEndAt("2017-02-14T12:03:08.000001Z"));
    store.put(Segment.create()
      .setChainId(chain5.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setStateEnum(SegmentState.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));

    Collection<Segment> result = testDAO.readManyFromSecondsUTC(HubClientAccess.internal(), "barnacles", 1487073724L);

    assertEquals(3L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment result0 = it.next();
    assertEquals(SegmentState.Dubbing, result0.getState());
    Segment result1 = it.next();
    assertEquals(SegmentState.Crafted, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.Crafting, result2.getState());
  }

  @Test
  public void readOneInState() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");

    Segment result = testDAO.readOneInState(access, chain3.getId(), SegmentState.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"));

    assertEquals(segment5.getId(), result.getId());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(4L), result.getOffset());
    assertEquals(SegmentState.Planned, result.getState());
    assertEquals("2017-02-14T12:03:08.000001Z", result.getBeginAt().toString());
    assertNull(result.getEndAt());
  }

  @Test
  public void readOneInState_failIfNoneInChain() throws Exception {
    HubClientAccess access = HubClientAccess.create("Internal");
    Chain.create(account1, "Test Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);
    failure.expect(DAOExistenceException.class);
    failure.expectMessage("Found no Segment");

    testDAO.readOneInState(access, segment2.getId(), SegmentState.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"));
  }

  @Test
  public void update() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Segment inputData = Segment.create()
      .setChainId(chain3.getId())
      .setOffset(5L)
      .setState("Dubbed")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setTotal(64)
      .setDensity(0.74)
      .setWaveformPreroll(0.0123)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    testDAO.update(access, segment2.getId(), inputData);

    Segment result = testDAO.readOne(HubClientAccess.internal(), segment2.getId());
    assertNotNull(result);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(SegmentState.Dubbed, result.getState());
    assertEquals(Double.valueOf(0.0123), result.getWaveformPreroll());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt().toString());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt().toString());
  }

  /**
   [#162361525] persist Segment content as JSON, then read prior Segment JSON
   */
  @Test
  public void persistPriorSegmentContent() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    segment4 = store.put(Segment.create()
      .setChainId(chain3.getId())
      .setOffset(5L)
      .setState("Dubbed")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setTempo(120.0));

    testDAO.update(access, segment4.getId(), segment4);

    Segment result = testDAO.readOne(HubClientAccess.internal(), segment2.getId());
    assertNotNull(result);
  }

  @Test
  public void update_failsToTransitionFromDubbingToCrafting() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("transition to Crafting not in allowed");

    testDAO.update(access, segment2.getId(),
      entityFactory.clone(segment2).setStateEnum(SegmentState.Crafting));
  }

  @Test
  public void update_FailsWithoutChainID() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Segment inputData = store.put(Segment.create()
      .setOffset(4L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0));

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain ID is required");

    testDAO.update(access, segment2.getId(), inputData);
  }

  @Test
  public void update_FailsToChangeChain() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");
    Segment inputData = Segment.create()
      .setChainId(chain3.getId())
      .setOffset(4L)
      .setState("Crafting")
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0);

    failure.expect(DAOValidationException.class);
    failure.expectMessage("transition to Crafting not in allowed");

    try {
      testDAO.update(access, segment2.getId(), inputData);

    } catch (Exception e) {
      Segment result = testDAO.readOne(HubClientAccess.internal(), segment2.getId());
      assertNotNull(result);
      assertEquals("Db minor", result.getKey());
      assertEquals(chain3.getId(), result.getChainId());
      throw e;
    }
  }

  @Test
  public void destroy() throws Exception {
/*
// TODO use Mockito to provide content that would have been ingested from Hub, and assert results
    test.getDSL().update(CHAIN)
      .set(CHAIN.STATE, "Erase")
      .execute();
*/

    testDAO.destroy(HubClientAccess.internal(), segment1.getId());

    try {
      testDAO.readOne(HubClientAccess.internal(), segment1.getId());
      fail();
    } catch (DAOExistenceException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void destroy_okRegardlessOfChainState() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");

    testDAO.destroy(access, segment1.getId());
  }

  @Test
  public void destroy_allChildEntities() throws Exception {
    HubClientAccess access = HubClientAccess.create("Admin");

    //
    // Go!
    testDAO.destroy(access, segment1.getId());
    //
    //

    // Assert annihilation
    try {
      testDAO.readOne(HubClientAccess.internal(), segment1.getId());
      fail();
    } catch (DAOExistenceException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  // TODO test revert deletes all related entities

}
