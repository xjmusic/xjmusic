// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.api.Chain;
import io.xj.api.ChainBinding;
import io.xj.api.ChainBindingType;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.api.ProgramType;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChord;
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
import io.xj.nexus.dao.Segments;
import io.xj.nexus.fabricator.Fabricator;
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
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.lib.util.Assert.assertSameItems;
import static io.xj.nexus.NexusIntegrationTestingFixtures.makeChain;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CraftFoundationNextMacroTest {
  private static final int TEST_REPEAT_ITERATIONS = 14;

  @Mock
  public HubClient hubClient;


  /**
   Test to ensure that the following Macro-Program is based on its first sequence-binding meme
   matching the last sequence-binding meme of the preceding Macro-Program
   */
  @Test
  public void craftFoundationNextMacro() throws Exception {
    for (int i = 0; i < TEST_REPEAT_ITERATIONS; i++) {
      Config config = NexusTestConfiguration.getDefault()
        .withValue("program.doTranspose", ConfigValueFactory.fromAnyRef(true))
        .withValue("instrument.isTonal", ConfigValueFactory.fromAnyRef(true));
      Environment env = Environment.getDefault();
      Injector injector = AppConfiguration.inject(config, env,
        ImmutableSet.of(Modules.override(new NexusWorkModule())
          .with(new AbstractModule() {
            @Override
            public void configure() {
              bind(HubClient.class).toInstance(hubClient);
            }
          })));
      FabricatorFactory fabricatorFactory = injector.getInstance(FabricatorFactory.class);
      CraftFactory craftFactory = injector.getInstance(CraftFactory.class);
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
      Chain chain1 = store.put(makeChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
      store.put(new ChainBinding()
        .id(UUID.randomUUID())
        .chainId(chain1.getId())
        .targetId(fake.library2.getId())
        .type(ChainBindingType.LIBRARY)
        );
      store.put(new Segment()
        .id(UUID.randomUUID())
        .chainId(chain1.getId())
        .offset(0L)
        .state(SegmentState.DUBBED)
        .beginAt("2017-02-14T12:01:00.000001Z")
        .endAt("2017-02-14T12:01:32.000001Z")
        .key("D major")
        .total(64)
        .density(0.73)
        .tempo(120.0)
        .storageKey("chains-1-segments-9f7s89d8a7892")
        .outputEncoder("wav")
        );
      store.put(new Segment()
        .id(UUID.randomUUID())
        .chainId(chain1.getId())
        .offset(1L)
        .state(SegmentState.DUBBING)
        .beginAt("2017-02-14T12:01:32.000001Z")
        .endAt("2017-02-14T12:02:04.000001Z")
        .key("Db minor")
        .total(64)
        .density(0.85)
        .tempo(120.0)
        .storageKey("chains-1-segments-9f7s89d8a7892.wav")
        );

      // Chain "Test Print #1" has this segment that was just crafted
      Segment segment3 = store.put(new Segment()
        .id(UUID.randomUUID())
        .chainId(chain1.getId())
        .offset(2L)
        .state(SegmentState.CRAFTED)
        .beginAt("2017-02-14T12:02:04.000001Z")
        .endAt("2017-02-14T12:02:36.000001Z")
        .key("Ab minor")
        .total(64)
        .density(0.30)
        .tempo(120.0)
        .storageKey("chains-1-segments-9f7s89d8a7892.wav")
        );
      store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment3, ProgramType.MACRO, fake.program4_sequence2_binding0));
      store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment3, ProgramType.MAIN, fake.program5_sequence1_binding0));

      // Chain "Test Print #1" has a planned segment
      Segment segment4 = store.put(NexusIntegrationTestingFixtures.makeSegment(chain1, 3, SegmentState.PLANNED, Instant.parse("2017-02-14T12:03:08.000001Z"), null, "C", 8, 0.8, 120, "chain-1-waveform-12345", "wav"));

      Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment4);

      craftFactory.macroMain(fabricator).doWork();

      Segment result = store.getSegment(segment4.getId()).orElseThrow();
      assertEquals(SegmentType.NEXTMACRO, result.getType());
      assertEquals("2017-02-14T12:03:15.840157Z", result.getEndAt());
      assertEquals(Integer.valueOf(16), result.getTotal());
      assertEquals("OGG", result.getOutputEncoder());
      assertEquals(0.45, result.getDensity(), 0.01);
      assertEquals("G minor", result.getKey());
      assertEquals(125, result.getTempo(), 0.01);
      // assert memes
      assertSameItems(
        Lists.newArrayList("REGRET", "CHUNKY", "HINDSIGHT", "TANGY"),
        Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
      // assert chords
      assertSameItems(Lists.newArrayList("Ab minor", "G minor"),
        Entities.namesOf(store.getAll(result.getId(), SegmentChord.class)));
      // assert choices
      Collection<SegmentChoice> segmentChoices =
        store.getAll(result.getId(), SegmentChoice.class);
      // assert macro choice
      SegmentChoice macroChoice = Segments.findFirstOfType(segmentChoices, ProgramType.MACRO);
      assertEquals(fake.program3_sequence0_binding0.getId(), macroChoice.getProgramSequenceBindingId());
      assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
      // assert main choice
      SegmentChoice mainChoice = Segments.findFirstOfType(segmentChoices, ProgramType.MAIN);
      assertEquals(fake.program15_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
      assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
    }
  }
}
