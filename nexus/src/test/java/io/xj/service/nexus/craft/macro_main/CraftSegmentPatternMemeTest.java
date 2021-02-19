// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.macro_main;

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
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.NexusIntegrationTestingFixtures;
import io.xj.service.nexus.craft.CraftFactory;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import io.xj.service.nexus.work.NexusWorkModule;
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
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private NexusEntityStore store;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Mock
  public HubClient hubClient;
  private NexusIntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config,
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
  }

  /**
   /**
   [#165803886] Segment memes expected to be taken directly of sequence_pattern binding
   <p>
   [#176728582] Macro program sequence should advance after each main program
   */
  @Test
  public void craftSegment() throws Exception {
    // Chain "Test Print #1" has 5 total segments
    Chain chain = store.put(buildChain(fake.account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());

    // Chain "Test Print #1" has this segment that was just crafted
    Segment previousSegment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(1L)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());
    store.put(buildSegmentChoice(previousSegment, Program.Type.Macro, fake.program4_sequence1_binding0));
    store.put(buildSegmentChoice(previousSegment, Program.Type.Main, fake.program5_sequence1_binding0));
    Segment segment = store.put(buildSegment(chain, 2, Segment.State.Planned, Instant.parse(previousSegment.getEndAt()), null, "C", 8, 0.8, 120, "chain-1-waveform-12345", "wav"));

    craftFactory.macroMain(fabricatorFactory.fabricate(HubClientAccess.internal(), segment)).doWork();

    var result = store.getSegment(segment.getId()).orElseThrow();
    assertEquals(Segment.Type.NextMacro, result.getType());
    assertSameItems(Lists.newArrayList("Regret", "Hindsight", "Chunky", "Tangy"),
      Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
  }
}
