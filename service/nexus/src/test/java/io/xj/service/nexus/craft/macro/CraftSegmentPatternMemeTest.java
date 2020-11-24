// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.macro;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentMeme;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityStoreException;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.NexusIntegrationTestingFixtures;
import io.xj.service.nexus.craft.CraftFactory;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import io.xj.service.nexus.work.NexusWorkModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.lib.util.Assert.assertSameItems;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftSegmentPatternMemeTest {
  private Injector injector;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private NexusIntegrationTestingFixtures fake;
  private Chain chain1;
  private Segment segment1;
  private Segment segment2;
  private Segment segment3;
  private Segment segment4;
  private Segment segment5;
  private NexusEntityStore store;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Mock
  public HubClient hubClient;


  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config,
      ImmutableSet.of(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(HubClient.class).toInstance(hubClient);
          }
        })));
    craftFactory = injector.getInstance(CraftFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    when(hubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(Streams.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()
      ).collect(Collectors.toList())));


    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(buildChain(fake.account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());

    // Chain "Test Print #1" has this segment that was just crafted
    segment1 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(1L)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());
    store.put(buildSegmentChoice(segment1, Program.Type.Macro, fake.program4_sequence1_binding0, 3));
    store.put(buildSegmentChoice(segment1, Program.Type.Main, fake.program5_sequence1_binding0, 5));
  }

  @After
  public void tearDown() {

  }

  /**
   [#165803886] Segment memes expected to be taken directly of sequence_pattern binding
   */
  @Test
  public void craftSegmentMemesDirectlyFromSequenceBindingBinding() throws Exception {
    segment2 = store.put(craftSegment(chain1, 2, Instant.parse("2017-02-14T12:02:36.000001Z")));
    segment3 = store.put(craftSegment(chain1, 3, Instant.parse(segment2.getEndAt())));
    segment4 = store.put(craftSegment(chain1, 4, Instant.parse(segment3.getEndAt())));
    segment5 = store.put(craftSegment(chain1, 5, Instant.parse(segment4.getEndAt())));

    assertEquals(Segment.Type.NextMacro, segment2.getType());
    assertSameItems(Lists.newArrayList("Regret", "Wild", "Hindsight", "Tropical"),
      Entities.namesOf(store.getAll(SegmentMeme.class, Segment.class, ImmutableList.of(segment2.getId()))));

    assertEquals(Segment.Type.Continue, segment3.getType());
    assertSameItems(Lists.newArrayList("Wild", "Hindsight", "Pride", "Shame", "Tropical"),
      Entities.namesOf(store.getAll(SegmentMeme.class, Segment.class, ImmutableList.of(segment3.getId()))));

    assertEquals(Segment.Type.Continue, segment3.getType());
    assertSameItems(Lists.newArrayList("Wild", "Cozy", "Optimism", "Outlook", "Tropical"),
      Entities.namesOf(store.getAll(SegmentMeme.class, Segment.class, ImmutableList.of(segment4.getId()))));

    assertEquals(Segment.Type.Continue, segment3.getType());
    assertSameItems(Lists.newArrayList("Wild", "Cozy", "Pessimism", "Outlook", "Tropical"),
      Entities.namesOf(store.getAll(SegmentMeme.class, Segment.class, ImmutableList.of(segment5.getId()))));
  }

  /**
   Craft a segment in the test

   @param chain  to craft segment in
   @param offset to craft at
   @param from   time
   @return crafted segment
   @throws Exception on failure
   */
  private Segment craftSegment(Chain chain, int offset, Instant from) throws Exception {
    Segment segment = store.put(buildSegment(chain, offset, Segment.State.Planned, from, null, "C", 8, 0.8, 120, "chain-1-waveform-12345", "wav"));
    updateState(segment.getId(), Segment.State.Crafting);
    Segment craftingSegment = injector.getInstance(SegmentDAO.class).readOne(HubClientAccess.internal(), segment.getId());
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), craftingSegment);
    craftFactory.macroMain(fabricator).doWork();
    updateState(segment.getId(), Segment.State.Crafted);
    return store.get(Segment.class, segment.getId()).orElseThrow();
  }

  /**
   Update a Segment to a new State

   @param segmentId of Segment to update
   @param state     to update Segment to
   */
  private void updateState(String segmentId, Segment.State state) throws EntityStoreException {
    store.put(store.get(Segment.class, segmentId).orElseThrow().toBuilder().setState(state).build());
  }

}
