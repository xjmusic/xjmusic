// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.Account;
import io.xj.Chain;
import io.xj.Segment;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
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
import java.util.UUID;

import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildAccount;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildHubClientAccess;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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
    var injector = AppConfiguration.inject(config, ImmutableSet.of(new NexusDAOModule()));
    entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // test subject
    testDAO = injector.getInstance(SegmentDAO.class);

    // Account "Testing" has chain "Test Print #1"
    account1 = buildAccount("Testing");
    chain3 = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account1.getId())
      .setName("Test Print #1")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .build());

    // Chain "Test Print #1" has 5 sequential segments
    segment1 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(0L)
      .setState(Segment.State.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .build());
    segment2 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(1L)
      .setState(Segment.State.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setWaveformPreroll(1.523)
      .setEndAt("2017-02-14T12:02:04.000001Z")
      .build());
    segment3 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(2L)
      .setState(Segment.State.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .build());
    segment4 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(3L)
      .setState(Segment.State.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setEndAt("2017-02-14T12:03:08.000001Z")
      .build());
    segment5 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setState(Segment.State.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892")
      .setOutputEncoder("wav")
      .build());
  }

  /**
   [#162361712] Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation
   */
  @Test
  public void create() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Segment inputData = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(5L)
      .setState(Segment.State.Planned)
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setWaveformPreroll(2.898)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setKey("C# minor 7 b9")
      .setTempo(120.0)
      .build();

    when(fileStoreProvider.generateKey("chains-1-segments"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    Segment result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(5L, result.getOffset());
    assertEquals(Segment.State.Planned, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt());
    assertEquals(64, result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals(2.898, result.getWaveformPreroll(), 0.01);
    assertNotNull(result.getStorageKey());
  }

  /**
   [#162361712] Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation
   [#126] Segments are always readMany in PLANNED state
   */
  @Test
  public void create_alwaysInPlannedState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Segment inputData = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(5L)
      .setState(Segment.State.Crafting)
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0)
      .build();

    when(fileStoreProvider.generateKey("chains-1-segments"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    Segment result = testDAO.create(access, inputData);

    assertNotNull(result);
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(5L, result.getOffset());
    assertEquals(Segment.State.Planned, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt());
    assertEquals(64, result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.1);
    assertNotNull(result.getStorageKey());
  }

  @Test
  public void create_FailsIfNotUniqueChainOffset() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Segment inputData = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(4L)
      .setState(Segment.State.Crafting)
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0)
      .build();

    when(fileStoreProvider.generateKey("chains-1-segments"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Found Segment at same offset in Chain");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutTopLevelAccess() throws Exception {
    HubClientAccess access = buildHubClientAccess("User");
    Segment inputData = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(4L)
      .setState(Segment.State.Crafting)
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0).build();

    when(fileStoreProvider.generateKey("chains-1-segments"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("top-level access is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void create_FailsWithoutChainID() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Segment inputData = Segment.newBuilder()
      .setOffset(4L)
      .setState(Segment.State.Crafting)
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0).build();

    when(fileStoreProvider.generateKey("chains-1-segments"))
      .thenReturn("chains-1-segments-h2a34j5s34fd987gaw3.ogg");

    failure.expect(DAOValidationException.class);

    failure.expectMessage("Chain ID is required");

    testDAO.create(access, inputData);
  }

  @Test
  public void readOne() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    Segment result = testDAO.readOne(access, segment2.getId());

    assertNotNull(result);
    assertEquals(segment2.getId(), result.getId());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(1L, result.getOffset());
    assertEquals(Segment.State.Dubbing, result.getState());
    assertEquals("2017-02-14T12:01:32.000001Z", result.getBeginAt());
    assertEquals("2017-02-14T12:02:04.000001Z", result.getEndAt());
    assertEquals(64, result.getTotal());
    assertEquals(0.85, result.getDensity(), 0.01);
    assertEquals("Db minor", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals(1.523, result.getWaveformPreroll(), 0.01);
  }

  @Test
  public void readOne_failsWhenUserIsNotInChain() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build()), "User");
    failure.expect(DAOPrivilegeException.class);
    failure.expectMessage("access is required");

    testDAO.readOne(access, segment1.getId());
  }

  @Test
  public void readMany() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    Collection<Segment> result = testDAO.readMany(access, ImmutableList.of(chain3.getId()));

    assertNotNull(result);
    assertEquals(5L, result.size());
    Iterator<Segment> it = result.iterator();

    Segment result0 = it.next();
    assertEquals(Segment.State.Dubbed, result0.getState());

    Segment result1 = it.next();
    assertEquals(Segment.State.Dubbing, result1.getState());

    Segment result2 = it.next();
    assertEquals(Segment.State.Crafted, result2.getState());

    Segment result3 = it.next();
    assertEquals(Segment.State.Crafting, result3.getState());

    Segment result4 = it.next();
    assertEquals(Segment.State.Planned, result4.getState());
  }

  /**
   [#173806948] List of Segments returned should not be more than a dozen or so
   */
  @Test
  public void readMany_doesNotExceedReadLimit() throws Exception {
    for (int i = 0; i < 20; i++)
      store.put(Segment.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setChainId(chain3.getId())
        .setOffset(4L)
        .setState(Segment.State.Crafting)
        .setBeginAt("1995-04-28T11:23:00.000001Z")
        .setEndAt("1995-04-28T11:23:32.000001Z")
        .setTotal(64)
        .setDensity(0.74)
        .setKey("C# minor 7 b9")
        .setTempo(120.0)
        .build());
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    Collection<Segment> result = testDAO.readMany(access, ImmutableList.of(chain3.getId()));

    assertNotNull(result);
    assertEquals(12L, result.size());
  }

  /**
   [#173806948] List of Segments returned should not be more than a dozen or so
   */
  @Test
  public void readMany_byChainEmbedKey_doesNotExceedReadLimit() throws Exception {
    chain5 = store.put(buildChain(account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "barnacles"));
    for (int i = 0; i < 20; i++)
      store.put(Segment.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setChainId(chain5.getId())
        .setOffset(4L)
        .setState(Segment.State.Crafting)
        .setBeginAt("1995-04-28T11:23:00.000001Z")
        .setEndAt("1995-04-28T11:23:32.000001Z")
        .setTotal(64)
        .setDensity(0.74)
        .setKey("C# minor 7 b9")
        .setTempo(120.0)
        .build());

    Collection<Segment> result = testDAO.readManyByEmbedKey(HubClientAccess.internal(), "barnacles");

    assertNotNull(result);
    assertEquals(12L, result.size());
  }

  @Test
  public void readMany_byChainEmbedKey() throws Exception {
    chain5 = store.put(buildChain(account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "barnacles"));
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setOffset(0L)
      .setState(Segment.State.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .build()
    );
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setOffset(1L)
      .setState(Segment.State.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .build()
    );
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setOffset(2L)
      .setState(Segment.State.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .build()
    );
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setOffset(3L)
      .setState(Segment.State.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .build()
    );
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setState(Segment.State.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());

    Collection<Segment> result = testDAO.readManyByEmbedKey(HubClientAccess.internal(), "barnacles");

    assertEquals(5L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment actualResult0 = it.next();
    assertEquals(Segment.State.Planned, actualResult0.getState());
    Segment result1 = it.next();
    assertEquals(Segment.State.Crafting, result1.getState());
    Segment result2 = it.next();
    assertEquals(Segment.State.Crafted, result2.getState());
    Segment result3 = it.next();
    assertEquals(Segment.State.Dubbing, result3.getState());
    Segment result4 = it.next();
    assertEquals(Segment.State.Dubbed, result4.getState());
  }

  @Test
  public void readManyFromOffset() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    Collection<Segment> result = testDAO.readManyFromOffset(access, chain3.getId(), 2L);

    assertEquals(3L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment actualResult0 = it.next();
    assertEquals(Segment.State.Crafted, actualResult0.getState());
    Segment result1 = it.next();
    assertEquals(Segment.State.Crafting, result1.getState());
    Segment result2 = it.next();
    assertEquals(Segment.State.Planned, result2.getState());
  }

  @Test
  public void readManyFromToOffset() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    Collection<Segment> result = testDAO.readManyFromToOffset(access, chain3.getId(), 2L, 3L);

    assertEquals(2L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment result1 = it.next();
    assertEquals(Segment.State.Crafted, result1.getState());
    Segment result2 = it.next();
    assertEquals(Segment.State.Crafting, result2.getState());
  }

  @Test
  public void readManyFromToOffset_acceptsNegativeOffsets_returnsEmptyCollection() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    Collection<Segment> result = testDAO.readManyFromToOffset(access, chain3.getId(), -1L, -1L);

    assertEquals(0L, result.size());
  }

  @Test
  public void readManyFromOffset_byChainEmbedKey() throws Exception {
    chain5 = store.put(buildChain(account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "barnacles"));
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setOffset(0L)
      .setState(Segment.State.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .build());
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setOffset(1L)
      .setState(Segment.State.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .build());
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setOffset(2L)
      .setState(Segment.State.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .build());
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setOffset(3L)
      .setState(Segment.State.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .build());
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setState(Segment.State.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());

    Collection<Segment> result = testDAO.readManyFromOffsetByEmbedKey(HubClientAccess.internal(), "barnacles", 2L);

    assertEquals(3L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment actualResult0 = it.next();
    assertEquals(Segment.State.Crafted, actualResult0.getState());
    Segment result1 = it.next();
    assertEquals(Segment.State.Crafting, result1.getState());
    Segment result2 = it.next();
    assertEquals(Segment.State.Planned, result2.getState());
  }

  @Test
  public void readManyFromSecondsUTC() throws Exception {
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    Collection<Segment> result = testDAO.readManyFromSecondsUTC(access, chain3.getId(), 1487073724L);

    assertEquals(3L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment actualResult0 = it.next();
    assertEquals(Segment.State.Dubbing, actualResult0.getState());
    Segment result1 = it.next();
    assertEquals(Segment.State.Crafted, result1.getState());
    Segment result2 = it.next();
    assertEquals(Segment.State.Crafting, result2.getState());
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
    chain5 = store.put(buildChain(account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, beginAt, null, "barnacles"));
    for (int offset = 0; offset < numSegmentsToGenerate; offset++) {
      Instant endAt = beginAt.plusMillis(1000 * total * 60 / tempo);
      store.put(buildSegment(chain5, offset, Segment.State.Dubbed,
        beginAt, endAt, "D major", total, 0.73, tempo,
        "chains-1-segments-9f7s89d8a7892.wav", "AAC"));
      beginAt = endAt;
    }
    HubClientAccess access = buildHubClientAccess(ImmutableList.of(account1), "User,Engineer");

    Collection<Segment> result = testDAO.readManyFromSecondsUTC(access, chain5.getId(), fromSecondsUTC + 1);

    assertEquals(12L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment firstReturnedSegment = it.next();
    assertEquals(Instant.ofEpochSecond(fromSecondsUTC),
      Instant.parse(firstReturnedSegment.getBeginAt()));
  }

  @Test
  public void readManyFromSecondsUTC_byChainEmbedKey() throws Exception {
    chain5 = store.put(buildChain(account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "barnacles"));
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setOffset(0L)
      .setState(Segment.State.Dubbed)
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .build());
    store.put(Segment.newBuilder().setChainId(chain5.getId())
      .setId(UUID.randomUUID().toString())
      .setOffset(1L)
      .setState(Segment.State.Dubbing)
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z")
      .build());
    store.put(Segment.newBuilder().setChainId(chain5.getId())
      .setId(UUID.randomUUID().toString())
      .setOffset(2L)
      .setState(Segment.State.Crafted)
      .setKey("F major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .build());
    store.put(Segment.newBuilder().setChainId(chain5.getId())
      .setId(UUID.randomUUID().toString())
      .setOffset(3L)
      .setState(Segment.State.Crafting)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setBeginAt("2017-02-14T12:02:36.000001Z")
      .setEndAt("2017-02-14T12:03:08.000001Z")
      .build());
    store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain5.getId())
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setOffset(4L)
      .setState(Segment.State.Planned)
      .setKey("E minor")
      .setTotal(64)
      .setDensity(0.41)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());

    Collection<Segment> result = testDAO.readManyFromSecondsUTCbyEmbedKey(HubClientAccess.internal(), "barnacles", 1487073724L);

    assertEquals(3L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment result0 = it.next();
    assertEquals(Segment.State.Dubbing, result0.getState());
    Segment result1 = it.next();
    assertEquals(Segment.State.Crafted, result1.getState());
    Segment result2 = it.next();
    assertEquals(Segment.State.Crafting, result2.getState());
  }

  @Test
  public void readOneInState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");

    Segment result = testDAO.readOneInState(access, chain3.getId(), Segment.State.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"));

    assertEquals(segment5.getId(), result.getId());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(4L, result.getOffset());
    assertEquals(Segment.State.Planned, result.getState());
    assertEquals("2017-02-14T12:03:08.000001Z", result.getBeginAt());
    assertEquals("", result.getEndAt());
  }

  @Test
  public void readOneInState_failIfNoneInChain() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");
    buildChain(account1, "Test Print #2", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);
    failure.expect(DAOExistenceException.class);
    failure.expectMessage("Found no Segment");

    testDAO.readOneInState(access, segment2.getId(), Segment.State.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"));
  }

  @Test
  public void update() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Segment inputData = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(5L)
      .setState(Segment.State.Dubbed)
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setTotal(64)
      .setDensity(0.74)
      .setWaveformPreroll(0.0123)
      .setKey("C# minor 7 b9")
      .setTempo(120.0)
      .build();

    testDAO.update(access, segment2.getId(), inputData);

    Segment result = testDAO.readOne(HubClientAccess.internal(), segment2.getId());
    assertNotNull(result);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(Segment.State.Dubbed, result.getState());
    assertEquals(0.0123, result.getWaveformPreroll(), 0.001);
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt());
  }

  /**
   [#162361525] persist Segment content as JSON, then read prior Segment JSON
   */
  @Test
  public void persistPriorSegmentContent() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    segment4 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(5L)
      .setState(Segment.State.Dubbed)
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .setTempo(120.0)
      .build());

    testDAO.update(access, segment4.getId(), segment4);

    Segment result = testDAO.readOne(HubClientAccess.internal(), segment2.getId());
    assertNotNull(result);
  }

  @Test
  public void update_failsToTransitionFromDubbingToCrafting() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");

    failure.expect(DAOValidationException.class);
    failure.expectMessage("transition to Crafting not in allowed");

    testDAO.update(access, segment2.getId(),
      entityFactory.clone(segment2).toBuilder().setState(Segment.State.Crafting).build());
  }

  @Test
  public void update_FailsWithoutChainID() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Segment inputData = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setOffset(4L)
      .setState(Segment.State.Crafting)
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0)
      .build());

    failure.expect(DAOValidationException.class);
    failure.expectMessage("Chain ID is required");

    testDAO.update(access, segment2.getId(), inputData);
  }

  @Test
  public void update_FailsToChangeChain() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");
    Segment inputData = Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain3.getId())
      .setOffset(4L)
      .setState(Segment.State.Crafting)
      .setBeginAt("1995-04-28T11:23:00.000001Z")
      .setEndAt("1995-04-28T11:23:32.000001Z")
      .setTotal(64)
      .setDensity(0.74)
      .setKey("C# minor 7 b9")
      .setTempo(120.0)
      .build();

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
    HubClientAccess access = buildHubClientAccess("Admin");

    testDAO.destroy(access, segment1.getId());
  }

  @Test
  public void destroy_allChildEntities() throws Exception {
    HubClientAccess access = buildHubClientAccess("Admin");

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
