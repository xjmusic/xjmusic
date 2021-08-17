// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainBinding;
import io.xj.api.ChainBindingType;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.api.ProgramType;
import io.xj.api.Segment;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.testing.NexusTestConfiguration;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.lib.util.Assert.assertSameItems;
import static io.xj.nexus.NexusIntegrationTestingFixtures.makeChain;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CraftSegmentPatternMemeTest {
  private static final int TEST_REPEAT_ITERATIONS = 14;

  @Mock
  public HubClient hubClient;

  /**
   Test to ensure that the following Macro-Program is based on its first sequence-binding meme
   matching the last sequence-binding meme of the preceding Macro-Program
   <p>
   [#165803886] Segment memes expected to be taken directly of sequence_pattern binding
   [#176728582] Macro program sequence should advance after each main program
   */
  @Test
  public void craftSegment() throws Exception {
    for (int i = 0; i < TEST_REPEAT_ITERATIONS; i++) {
      Config config = NexusTestConfiguration.getDefault();
      Environment env = Environment.getDefault();
      Injector injector = AppConfiguration.inject(config, env,
        ImmutableSet.of(Modules.override(new NexusWorkModule())
          .with(new AbstractModule() {
            @Override
            public void configure() {
              bind(HubClient.class).toInstance(hubClient);
            }
          })));
      CraftFactory craftFactory = injector.getInstance(CraftFactory.class);
      FabricatorFactory fabricatorFactory = injector.getInstance(FabricatorFactory.class);
      var entityFactory = injector.getInstance(EntityFactory.class);
      Topology.buildHubApiTopology(entityFactory);
      Topology.buildNexusApiTopology(entityFactory);

      // Manipulate the underlying entity store; reset before each test
      NexusEntityStore store = injector.getInstance(NexusEntityStore.class);
      store.deleteAll();

      // Mock request via HubClient returns fake generated library of hub content
      NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
      HubContent sourceMaterial = new HubContent(Streams.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()
      ).collect(Collectors.toList()));

      // Chain "Test Print #1" has 5 total segments
      Chain chain = store.put(makeChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
      store.put(new ChainBinding()
        .id(UUID.randomUUID())
        .chainId(chain.getId())
        .targetId(fake.library2.getId())
        .type(ChainBindingType.LIBRARY)
        );

      // Preceding Segment
      Segment previousSegment = store.put(new Segment()
        .id(UUID.randomUUID())
        .chainId(chain.getId())
        .offset(1L)
        .state(SegmentState.CRAFTING)
        .beginAt("2017-02-14T12:02:04.000001Z")
        .endAt("2017-02-14T12:02:36.000001Z")
        .key("F Major")
        .total(64)
        .density(0.30)
        .tempo(120.0)
        .storageKey("chains-1-segments-9f7s89d8a7892.wav")
        );
      store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(previousSegment, ProgramType.MACRO, fake.program4_sequence1_binding0));
      store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(previousSegment, ProgramType.MAIN, fake.program5_sequence1_binding0));

      // Following Segment
      Segment segment = store.put(NexusIntegrationTestingFixtures.makeSegment(chain, 2, SegmentState.PLANNED, Instant.parse(previousSegment.getEndAt()), null, "C", 8, 0.8, 120, "chain-1-waveform-12345", "wav"));

      craftFactory.macroMain(fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment)).doWork();

      var result = store.getSegment(segment.getId()).orElseThrow();
      assertEquals(SegmentType.NEXTMACRO, result.getType());
      assertSameItems(Lists.newArrayList("REGRET", "HINDSIGHT", "CHUNKY", "TANGY"),
        Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
    }
  }
}
