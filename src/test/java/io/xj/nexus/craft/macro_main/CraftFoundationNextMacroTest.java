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
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChord;
import io.xj.SegmentMeme;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.dao.SegmentDAO;
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
   matching the the last sequence-binding meme of the preceding Macro-Program
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
      Chain chain1 = store.put(makeChain(fake.account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
      store.put(ChainBinding.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setChainId(chain1.getId())
        .setTargetId(fake.library2.getId())
        .setType(ChainBinding.Type.Library)
        .build());
      store.put(Segment.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setChainId(chain1.getId())
        .setOffset(0)
        .setState(Segment.State.Dubbed)
        .setBeginAt("2017-02-14T12:01:00.000001Z")
        .setEndAt("2017-02-14T12:01:32.000001Z")
        .setKey("D major")
        .setTotal(64)
        .setDensity(0.73)
        .setTempo(120)
        .setStorageKey("chains-1-segments-9f7s89d8a7892")
        .setOutputEncoder("wav")
        .build());
      store.put(Segment.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setChainId(chain1.getId())
        .setOffset(1)
        .setState(Segment.State.Dubbing)
        .setBeginAt("2017-02-14T12:01:32.000001Z")
        .setEndAt("2017-02-14T12:02:04.000001Z")
        .setKey("Db minor")
        .setTotal(64)
        .setDensity(0.85)
        .setTempo(120)
        .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
        .build());

      // Chain "Test Print #1" has this segment that was just crafted
      Segment segment3 = store.put(Segment.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setChainId(chain1.getId())
        .setOffset(2L)
        .setState(Segment.State.Crafted)
        .setBeginAt("2017-02-14T12:02:04.000001Z")
        .setEndAt("2017-02-14T12:02:36.000001Z")
        .setKey("Ab minor")
        .setTotal(64)
        .setDensity(0.30)
        .setTempo(120.0)
        .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
        .build());
      store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment3, Program.Type.Macro, fake.program4_sequence2_binding0));
      store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment3, Program.Type.Main, fake.program5_sequence1_binding0));

      // Chain "Test Print #1" has a planned segment
      Segment segment4 = store.put(NexusIntegrationTestingFixtures.makeSegment(chain1, 3, Segment.State.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"), null, "C", 8, 0.8, 120, "chain-1-waveform-12345", "wav"));

      Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment4);

      craftFactory.macroMain(fabricator).doWork();

      Segment result = store.getSegment(segment4.getId()).orElseThrow();
      assertEquals(Segment.Type.NextMacro, result.getType());
      assertEquals("2017-02-14T12:03:15.840157Z", result.getEndAt());
      assertEquals(16, result.getTotal());
      assertEquals("OGG", result.getOutputEncoder());
      assertEquals(0.45, result.getDensity(), 0.01);
      assertEquals("G minor", result.getKey());
      assertEquals(125, result.getTempo(), 0.01);
      // assert memes
      assertSameItems(
        Lists.newArrayList("Regret", "Chunky", "Hindsight", "Tangy"),
        Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
      // assert chords
      assertSameItems(Lists.newArrayList("Ab minor", "G minor"),
        Entities.namesOf(store.getAll(result.getId(), SegmentChord.class)));
      // assert choices
      Collection<SegmentChoice> segmentChoices =
        store.getAll(result.getId(), SegmentChoice.class);
      // assert macro choice
      SegmentChoice macroChoice = SegmentDAO.findFirstOfType(segmentChoices, Program.Type.Macro);
      assertEquals(fake.program3_sequence0_binding0.getId(), macroChoice.getProgramSequenceBindingId());
      assertEquals(Long.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
      // assert main choice
      SegmentChoice mainChoice = SegmentDAO.findFirstOfType(segmentChoices, Program.Type.Main);
      assertEquals(fake.program15_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
      assertEquals(Long.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
    }
  }
}
