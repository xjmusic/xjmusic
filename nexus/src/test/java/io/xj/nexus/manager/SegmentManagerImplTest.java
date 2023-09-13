// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.manager;

import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.*;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildHubClientAccess;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SegmentManagerImplTest {
  Account account1;
  Chain chain3;
  NexusEntityStore store;
  Segment segment1;
  Segment segment2;
  Segment segment4;
  Segment segment5;
  SegmentManager testService;
  Template template1;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store
    store = new NexusEntityStoreImpl(entityFactory);
    store.deleteAll();

    // test subject
    testService = new SegmentManagerImpl(store);

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
      .state(ChainState.FABRICATE));

    // Chain "Test Print #1" has 5 sequential segments
    segment1 = store.put(new Segment()
      .id(0)
      .chainId(chain3.getId())
      .delta(0)
      .type(SegmentType.INITIAL)
      .state(SegmentState.CRAFTED)
      .key("D major")
      .total(64)
      .density(0.73)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAtChainMicros(0L)
      .durationMicros(32 * MICROS_PER_SECOND));
    segment2 = store.put(new Segment()
      .id(1)
      .chainId(chain3.getId())
      .delta(64)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTING)
      .key("Db minor")
      .total(64)
      .density(0.85)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAtChainMicros(32 * MICROS_PER_SECOND)
      .waveformPreroll(1.523)
      .durationMicros(32 * MICROS_PER_SECOND));
    store.put(new Segment()
      .id(2)
      .chainId(chain3.getId())
      .delta(256)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTED)
      .key("F major")
      .total(64)
      .density(0.30)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAtChainMicros(2 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND));
    segment4 = store.put(new Segment()
      .id(3)
      .chainId(chain3.getId())
      .state(SegmentState.CRAFTING)
      .key("E minor")
      .total(64)
      .delta(192)
      .type(SegmentType.CONTINUE)
      .density(0.41)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .beginAtChainMicros(3 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND));
    segment5 = store.put(new Segment()
      .id(4)
      .chainId(chain3.getId())
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .delta(245)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.PLANNED)
      .key("E minor")
      .total(64)
      .density(0.41)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892"));
  }

  /**
   Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation https://www.pivotaltracker.com/story/show/162361712
   */
  @Test
  public void create() throws Exception {
    Segment inputData = new Segment()
      .id(5)
      .chainId(chain3.getId())
      .state(SegmentState.PLANNED)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAtChainMicros(5 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
      .total(64)
      .density(0.74)
      .waveformPreroll(2.898)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .key("C# minor 7 b9")
      .tempo(120.0);

    Segment result = testService.create(inputData);

    assertNotNull(result);
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(5, result.getId());
    assertEquals(SegmentState.PLANNED, result.getState());
    assertEquals(5 * 32 * MICROS_PER_SECOND, (long) result.getBeginAtChainMicros());
    assertEquals(32 * MICROS_PER_SECOND, (long) Objects.requireNonNull(result.getDurationMicros()));
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals(2.898, result.getWaveformPreroll(), 0.01);
    assertNotNull(result.getStorageKey());
  }

  /**
   Segment waveform_key is set by fabricator (which knows the chain configuration) NOT on creation https://www.pivotaltracker.com/story/show/162361712
   [#126] Segments are always readMany in PLANNED state
   */
  @Test
  public void create_alwaysInPlannedState() throws Exception {
    Segment inputData = new Segment()
      .chainId(chain3.getId())
      .id(5)
      .state(SegmentState.CRAFTING)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAtChainMicros(5 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      .total(64)
      .density(0.74)
      .key("C# minor 7 b9")
      .tempo(120.0);

    Segment result = testService.create(inputData);

    assertNotNull(result);
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(5, result.getId());
    assertEquals(SegmentState.PLANNED, result.getState());
    assertEquals(5 * 32 * MICROS_PER_SECOND, (long) result.getBeginAtChainMicros());
    assertEquals(32 * MICROS_PER_SECOND, (long) Objects.requireNonNull(result.getDurationMicros()));
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.74, result.getDensity(), 0.01);
    assertEquals("C# minor 7 b9", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.1);
    assertNotNull(result.getStorageKey());
  }

  @Test
  public void create_FailsIfNotUniqueChainOffset() {
    Segment inputData = new Segment()
      .chainId(chain3.getId())
      .id(4)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTING)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
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
      .id(4)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTING)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
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
    assertEquals(1, result.getId());
    assertEquals(SegmentState.CRAFTING, result.getState());
    assertEquals(32 * MICROS_PER_SECOND, (long) result.getBeginAtChainMicros());
    assertEquals(32 * MICROS_PER_SECOND, (long) Objects.requireNonNull(result.getDurationMicros()));
    assertEquals(Integer.valueOf(64), result.getTotal());
    assertEquals(0.85, result.getDensity(), 0.01);
    assertEquals("Db minor", result.getKey());
    assertEquals(120.0, result.getTempo(), 0.01);
    assertEquals(1.523, result.getWaveformPreroll(), 0.01);
  }

  @Test
  public void readMany() {

    Collection<Segment> result = testService.readAll();

    assertNotNull(result);
    assertEquals(5L, result.size());
    Iterator<Segment> it = result.iterator();

    Segment result0 = it.next();
    assertEquals(SegmentState.CRAFTED, result0.getState());

    Segment result1 = it.next();
    assertEquals(SegmentState.CRAFTING, result1.getState());

    Segment result2 = it.next();
    assertEquals(SegmentState.CRAFTED, result2.getState());

    Segment result3 = it.next();
    assertEquals(SegmentState.CRAFTING, result3.getState());

    Segment result4 = it.next();
    assertEquals(SegmentState.PLANNED, result4.getState());
  }

  /**
   List of Segments returned should not be more than a dozen or so https://www.pivotaltracker.com/story/show/173806948
   */
  @Test
  public void readMany_hasNoLimit() throws Exception {
    Chain chain5 = store.put(buildChain(account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, template1, "barnacles"));
    for (int i = 0; i < 20; i++)
      store.put(new Segment()
        .chainId(chain5.getId())
        .id(i)
        .state(SegmentState.CRAFTING)
        .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
        .durationMicros(32 * MICROS_PER_SECOND)
        .total(64)
        .density(0.74)
        .key("C# minor 7 b9")
        .tempo(120.0));

    Collection<Segment> result = testService.readAll();

    assertNotNull(result);
    assertEquals(20L, result.size());
  }

  @Test
  public void readManyFromToOffset() throws Exception {
  Collection<Segment> result = testService.readManyFromToOffset(2, 3);

    assertEquals(2L, result.size());
    Iterator<Segment> it = result.iterator();
    Segment result1 = it.next();
    assertEquals(SegmentState.CRAFTED, result1.getState());
    Segment result2 = it.next();
    assertEquals(SegmentState.CRAFTING, result2.getState());
  }

  @Test
  public void readManyFromToOffset_acceptsNegativeOffsets_returnsEmptyCollection() throws Exception {
    Collection<Segment> result = testService.readManyFromToOffset(-1, -1);

    assertEquals(0L, result.size());
  }

  @Test
  public void readManyFromToOffset_trimsIfEndOffsetOutOfBounds() throws Exception {
    Collection<Segment> result = testService.readManyFromToOffset(2, 12);

    assertEquals(3L, result.size());
  }

  @Test
  public void readManyFromToOffset_onlyOneIfEndOffsetSameAsStart() throws Exception {
    Collection<Segment> result = testService.readManyFromToOffset(2, 2);

    assertEquals(1L, result.size());
  }

  @Test
  public void readManyFromToOffset_emptyIfStartOffsetOutOfBounds() throws Exception {
    Collection<Segment> result = testService.readManyFromToOffset(14, 17);

    assertEquals(0, result.size());
  }

  @Test
  public void readOneInState() throws Exception {
    HubClientAccess access = buildHubClientAccess("Internal");

    Segment result = testService.readFirstInState(access, SegmentState.PLANNED, 4 * 32 * MICROS_PER_SECOND);

    assertEquals(segment5.getId(), result.getId());
    assertEquals(chain3.getId(), result.getChainId());
    assertEquals(4, result.getId());
    assertEquals(SegmentState.PLANNED, result.getState());
    assertEquals(4 * 32 * MICROS_PER_SECOND, (long) result.getBeginAtChainMicros());
    assertNull(result.getDurationMicros());
  }

  @Test
  public void readOneInState_failIfNoneInChain() {
    HubClientAccess access = buildHubClientAccess("Internal");
    buildChain(account1, "Test Print #2", ChainType.PRODUCTION, ChainState.FABRICATE, template1, null);

    Exception thrown = assertThrows(ManagerExistenceException.class, () ->
      testService.readFirstInState(access, SegmentState.PLANNED, 2 * 32 * MICROS_PER_SECOND));

    assertTrue(thrown.getMessage().contains("Found no Segment"));
  }

  @Test
  public void update() throws Exception {
    Segment inputData = new Segment()
      .id(5)
      .chainId(chain3.getId())
      .state(SegmentState.CRAFTED)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
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
    assertEquals(SegmentState.CRAFTED, result.getState());
    assertEquals(0.0123, result.getWaveformPreroll(), 0.001);
    assertEquals(4 * 32 * MICROS_PER_SECOND, (long) result.getBeginAtChainMicros());
    assertEquals(32 * MICROS_PER_SECOND, (long) Objects.requireNonNull(result.getDurationMicros()));
  }

  /**
   persist Segment content as JSON, then read prior Segment JSON https://www.pivotaltracker.com/story/show/162361525
   */
  @Test
  public void persistPriorSegmentContent() throws Exception {
    segment4 = store.put(new Segment()
      .id(5)
      .type(SegmentType.CONTINUE)
      .delta(0)
      .chainId(chain3.getId())
      .state(SegmentState.CRAFTED)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
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
    Segment inputData = new Segment()
      .id(4)
      .chainId(segment5.getChainId())
      .state(SegmentState.CRAFTED)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
      .total(64)
      .density(0.74)
      .key("C# minor 7 b9")
      .tempo(120.0);

    Exception thrown = assertThrows(ManagerValidationException.class, () ->
      testService.update(segment5.getId(), inputData));

    assertTrue(thrown.getMessage().contains("transition to Crafted not in allowed"));
  }

  @Test
  public void update_FailsWithoutChainID() throws Exception {
    Segment inputData = store.put(new Segment()
      .id(4)
      .state(SegmentState.CRAFTING)
      .delta(0)
      .type(SegmentType.CONTINUE)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
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
      .id(4)
      .chainId(UUID.randomUUID())
      .delta(0)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTING)
      .beginAtChainMicros(4 * 32 * MICROS_PER_SECOND)
      .durationMicros(32 * MICROS_PER_SECOND)
      .total(64)
      .density(0.74)
      .key("C# minor 7 b9")
      .tempo(120.0);

    Exception thrown = assertThrows(ManagerValidationException.class, () ->
      testService.update(segment2.getId(), inputData));

    assertTrue(thrown.getMessage().contains("cannot change chainId create a segment"));
    Segment result = testService.readOne(segment2.getId());
    assertNotNull(result);
    assertEquals("Db minor", result.getKey());
    assertEquals(chain3.getId(), result.getChainId());
  }
}
