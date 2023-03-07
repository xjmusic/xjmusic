// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.manager;

import com.google.common.collect.ImmutableList;
import io.xj.hub.HubTopology;
import io.xj.hub.client.HubClientAccess;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerValidationException;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildHubClientAccess;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class SegmentManagerImplTest {
  private Account account1;
  private Chain chain3;
  private EntityFactory entityFactory;
  private NexusEntityStore store;
  private Segment segment1;
  private Segment segment2;
  private Segment segment4;
  private Segment segment5;
  private SegmentManager testService;
  private Template template1;

  @Before
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store
    store = new NexusEntityStoreImpl(entityFactory);
    store.deleteAll();

    // test subject
    testService = new SegmentManagerImpl(entityFactory, store);

    // Account "Testing" has a chain "Test Print #1"
    account1 = buildAccount("Testing");
    Library library1 = buildLibrary(account1, "test");
    template1 = buildTemplate(account1, "Test Template 1", "test1");
    buildTemplateBinding(template1, library1);

    chain3 = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(account1.getId())
      .name("Test Print #1")
      .type(ChainType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2014-08-12T12:17:02.527142Z"));

    // Chain "Test Print #1" has 5 sequential segments
    segment1 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(0L)
      .delta(0)
      .type(SegmentType.INITIAL)
      .state(SegmentState.DUBBED)
      .key("D major")
      .total(64)
      .density(0.73)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAt("2017-02-14T12:01:00.000001Z")
      .endAt("2017-02-14T12:01:32.000001Z"));
    segment2 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(1L)
      .delta(64)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.DUBBING)
      .key("Db minor")
      .total(64)
      .density(0.85)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAt("2017-02-14T12:01:32.000001Z")
      .waveformPreroll(1.523)
      .endAt("2017-02-14T12:02:04.000001Z"));
    store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(2L)
      .delta(256)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTED)
      .key("F major")
      .total(64)
      .density(0.30)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAt("2017-02-14T12:02:04.000001Z")
      .endAt("2017-02-14T12:02:36.000001Z"));
    segment4 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(3L)
      .state(SegmentState.CRAFTING)
      .key("E minor")
      .total(64)
      .delta(192)
      .type(SegmentType.CONTINUE)
      .density(0.41)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAt("2017-02-14T12:02:36.000001Z")
      .endAt("2017-02-14T12:03:08.000001Z"));
    segment5 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .beginAt("2017-02-14T12:03:08.000001Z")
      .offset(4L)
      .delta(245)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.PLANNED)
      .key("E minor")
      .total(64)
      .density(0.41)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892")
      .outputEncoder("wav"));
  }

  /**
   * Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation https://www.pivotaltracker.com/story/show/162361712
   */
  @Test
  public void create() throws Exception {
    Segment inputData = new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(5L)
      .state(SegmentState.PLANNED)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAt("1995-04-28T11:23:00.000001Z")
      .endAt("1995-04-28T11:23:32.000001Z")
      .total(64)
      .density(0.74)
      .waveformPreroll(2.898)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .key("C# minor 7 b9")
      .tempo(120.0);

    Segment result = testService.create(inputData);

    assertNotNull(result);
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(5), result.getOffset());
    assertEquals(SegmentState.PLANNED, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals(2.898, result.getWaveformPreroll(), 0.01);
    assertNotNull(result.getStorageKey());
  }

  /**
   * Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation https://www.pivotaltracker.com/story/show/162361712
   * [#126] Segments are always readMany in PLANNED state
   */
  @Test
  public void create_alwaysInPlannedState() throws Exception {
    Segment inputData = new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(5L)
      .state(SegmentState.CRAFTING)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAt("1995-04-28T11:23:00.000001Z")
      .endAt("1995-04-28T11:23:32.000001Z")
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .total(64)
      .density(0.74)
      .key("C# minor 7 b9")
      .tempo(120.0);

    Segment result = testService.create(inputData);

    assertNotNull(result);
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(5), result.getOffset());
    assertEquals(SegmentState.PLANNED, result.getState());
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.1);
    assertNotNull(result.getStorageKey());
  }

  @Test
  public void create_FailsIfNotUniqueChainOffset() {
    Segment inputData = new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(4L)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTING)
      .beginAt("1995-04-28T11:23:00.000001Z")
      .endAt("1995-04-28T11:23:32.000001Z")
      .total(64)
      .density(0.74)
      .key("C# minor 7 b9")
      .tempo(120.0);

    Exception thrown = assertThrows(ManagerValidationException.class, () ->
      testService.create(inputData));

    assertEquals("Found Segment at same offset in Chain!", thrown.getMessage());
  }

  @Test
  public void create_FailsWithoutChainID() {
    Segment inputData = new Segment()
      .offset(4L)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTING)
      .beginAt("1995-04-28T11:23:00.000001Z")
      .endAt("1995-04-28T11:23:32.000001Z")
      .total(64)
      .density(0.74)
      .key("C# minor 7 b9")
      .tempo(120.0);

    Exception thrown = assertThrows(ManagerValidationException.class, () ->
      testService.create(inputData));

    assertEquals("Chain ID is required.", thrown.getMessage());
  }

  @Test
  public void readOne() throws Exception {
    Segment result = testService.readOne(segment2.getId());

    assertNotNull(result);
    assertEquals(segment2.getId(), result.getId());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(1L), result.getOffset());
    assertEquals(SegmentState.DUBBING, result.getState());
    assertEquals("2017-02-14T12:01:32.000001Z", result.getBeginAt());
    assertEquals("2017-02-14T12:02:04.000001Z", result.getEndAt());
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.85, result.getDensity(), 0.01);
    assertEquals("Db minor", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals(1.523, result.getWaveformPreroll(), 0.01);
  }

  @Test
  public void readMany() throws Exception {

    Collection<Segment> result = testService.readMany(ImmutableList.of(chain3.getId()));

    assertNotNull(result);
    assertEquals(5L, result.size());
    Iterator<Segment> it = result.iterator();

    Segment result0 = it.next();
    assertEquals(SegmentState.DUBBED, result0.getState());

    Segment result1 = it.next();
    assertEquals(SegmentState.DUBBING, result1.getState());

    Segment result2 = it.next();
    assertEquals(SegmentState.CRAFTED, result2.getState());

    Segment result3 = it.next();
    assertEquals(SegmentState.CRAFTING, result3.getState());

    Segment result4 = it.next();
    assertEquals(SegmentState.PLANNED, result4.getState());
  }

  /**
   * List of Segments returned should not be more than a dozen or so https://www.pivotaltracker.com/story/show/173806948
   */
  @Test
  public void readMany_hasNoLimit() throws Exception {
    Chain chain5 = store.put(buildChain(account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, "barnacles"));
    for (int i = 0; i < 20; i++)
      store.put(new Segment()
        .id(UUID.randomUUID())
        .chainId(chain5.getId())
        .offset(4L)
        .state(SegmentState.CRAFTING)
        .beginAt("1995-04-28T11:23:00.000001Z")
        .endAt("1995-04-28T11:23:32.000001Z")
        .total(64)
        .density(0.74)
        .key("C# minor 7 b9")
        .tempo(120.0));

    Collection<Segment> result = testService.readMany(ImmutableList.of(chain5.getId()));

    assertNotNull(result);
    assertEquals(20L, result.size());
  }

  @Test
  public void readManyFromToOffset() throws Exception {
    Collection<Segment> result = testService.readManyFromToOffset(chain3.getId(), 2L, 3L);

    assertEquals(2L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment result1 = it.next();
    assertEquals(SegmentState.CRAFTED, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.CRAFTING, result2.getState());
  }

  @Test
  public void readManyFromToOffset_acceptsNegativeOffsets_returnsEmptyCollection() throws Exception {
    Collection<Segment> result = testService.readManyFromToOffset(chain3.getId(), -1L, -1L);

    assertEquals(0L, result.size());
  }

  @Test
  public void readOneInState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");

    Segment result = testService.readOneInState(access, chain3.getId(), SegmentState.PLANNED, Instant.parse("2017-02-14T12:03:08.000001Z"));

    assertEquals(segment5.getId(), result.getId());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(Long.valueOf(4), result.getOffset());
    assertEquals(SegmentState.PLANNED, result.getState());
    assertEquals("2017-02-14T12:03:08.000001Z", result.getBeginAt());
    assertNull(result.getEndAt());
  }

  @Test
  public void readOneInState_failIfNoneInChain() {
    HubClientAccess access = buildHubClientAccess("Internal");
    buildChain(account1, "Test Print #2", ChainType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);

    Exception thrown = assertThrows(ManagerExistenceException.class, () ->
      testService.readOneInState(access, segment2.getId(), SegmentState.PLANNED, Instant.parse("2017-02-14T12:03:08.000001Z")));

    assertTrue(thrown.getMessage().contains("Found no Segment"));
  }

  @Test
  public void update() throws Exception {
    Segment inputData = new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(5L)
      .state(SegmentState.DUBBED)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAt("1995-04-28T11:23:00.000001Z")
      .endAt("1995-04-28T11:23:32.000001Z")
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .total(64)
      .density(0.74)
      .waveformPreroll(0.0123)
      .key("C# minor 7 b9")
      .tempo(120.0);

    testService.update(segment2.getId(), inputData);

    Segment result = testService.readOne(segment2.getId());
    assertNotNull(result);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(SegmentState.DUBBED, result.getState());
    assertEquals(0.0123, result.getWaveformPreroll(), 0.001);
    assertEquals("1995-04-28T11:23:00.000001Z", result.getBeginAt());
    assertEquals("1995-04-28T11:23:32.000001Z", result.getEndAt());
  }

  /**
   * persist Segment content as JSON, then read prior Segment JSON https://www.pivotaltracker.com/story/show/162361525
   */
  @Test
  public void persistPriorSegmentContent() throws Exception {
    segment4 = store.put(new Segment()
      .id(UUID.randomUUID())
      .type(SegmentType.CONTINUE)
      .delta(0)
      .chainId(chain3.getId())
      .offset(5L)
      .state(SegmentState.DUBBED)
      .beginAt("1995-04-28T11:23:00.000001Z")
      .endAt("1995-04-28T11:23:32.000001Z")
      .total(64)
      .density(0.74)
      .key("C# minor 7 b9")
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .tempo(120.0));

    testService.update(segment4.getId(), segment4);

    Segment result = testService.readOne(segment2.getId());
    assertNotNull(result);
  }

  @Test
  public void update_failsToTransitionFromDubbingToCrafting() {

    Exception thrown = assertThrows(ManagerValidationException.class, () ->
      testService.update(segment2.getId(),
        entityFactory.clone(segment2).state(SegmentState.CRAFTING)));

    assertTrue(thrown.getMessage().contains("transition to Crafting not in allowed"));
  }

  @Test
  public void update_FailsWithoutChainID() throws Exception {
    Segment inputData = store.put(new Segment()
      .id(UUID.randomUUID())
      .offset(4L)
      .state(SegmentState.CRAFTING)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAt("1995-04-28T11:23:00.000001Z")
      .endAt("1995-04-28T11:23:32.000001Z")
      .total(64)
      .density(0.74)
      .key("C# minor 7 b9")
      .tempo(120.0));

    Exception thrown = assertThrows(ManagerValidationException.class, () ->
      testService.update(segment2.getId(), inputData));

    assertEquals("Chain ID is required.", thrown.getMessage());
  }

  @Test
  public void update_FailsToChangeChain() throws Exception {
    Segment inputData = new Segment()
      .id(UUID.randomUUID())
      .chainId(chain3.getId())
      .offset(4L)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTING)
      .beginAt("1995-04-28T11:23:00.000001Z")
      .endAt("1995-04-28T11:23:32.000001Z")
      .total(64)
      .density(0.74)
      .key("C# minor 7 b9")
      .tempo(120.0);

    Exception thrown = assertThrows(ManagerValidationException.class, () ->
      testService.update(segment2.getId(), inputData));

    assertTrue(thrown.getMessage().contains("transition to Crafting not in allowed"));
    Segment result = testService.readOne(segment2.getId());
    assertNotNull(result);
    assertEquals("Db minor", result.getKey());
    assertEquals(chain3.getId(), result.getChainId());
  }

  @Test
  public void destroy() throws Exception {
    // FUTURE use Mockito to provide content that would have been ingested from Hub, and assert results

    testService.destroy(segment1.getId());

    try {
      testService.readOne(segment1.getId());
      fail();
    } catch (ManagerExistenceException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  @Test
  public void destroy_okRegardlessOfChainState() throws Exception {

    testService.destroy(segment1.getId());
  }

  @Test
  public void destroy_allChildEntities() throws Exception {

    //
    // Go!
    testService.destroy(segment1.getId());
    //
    //

    // Assert annihilation
    try {
      testService.readOne(segment1.getId());
      fail();
    } catch (ManagerExistenceException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }

  // FUTURE test revert deletes all related entities

}
