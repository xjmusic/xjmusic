// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.hub.HubTopology;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTestConfiguration;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.Segments;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import static io.xj.lib.util.Assert.assertSameItems;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CraftFoundationInitialTest {
  @Mock
  public HubClient hubClient;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;
  private Segment segment6;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Environment env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new NexusWorkModule())
      .with(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Config.class).toInstance(config);
          bind(Environment.class).toInstance(env);
        }
      }));
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream()
    ).collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial planned segment
    Chain chain2 = store.put(buildChain(
      fake.account1,
      fake.template1,
      "Print #2",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2014-08-12T12:17:02.527142Z")));
    segment6 = store.put(buildSegment(
      chain2,
      0,
      SegmentState.PLANNED,
      Instant.parse("2017-02-14T12:01:00.000001Z"),
      null,
      "C",
      8,
      0.8,
      120.0,
      "chain-1-waveform-12345.wav",
      "ogg"));
  }

  @Test
  public void craftFoundationInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment6);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = store.getSegment(segment6.getId()).orElseThrow();
    assertEquals(segment6.getId(), result.getId());
    assertEquals("WAV", result.getOutputEncoder());
    assertEquals(SegmentType.INITIAL, result.getType());
    assertEquals("2017-02-14T12:01:07.384616Z", result.getEndAt());
    assertEquals(Integer.valueOf(16), result.getTotal());
    assertEquals(0.2, result.getDensity(), 0.01);
    assertEquals("G major", result.getKey());
    assertEquals(130.0, result.getTempo(), 0.01);
    // assert memes
    assertSameItems(
      Lists.newArrayList("TROPICAL", "WILD", "OUTLOOK", "OPTIMISM"),
      Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
    // assert chords
    assertSameItems(Lists.newArrayList("G major", "Ab minor"),
      Entities.namesOf(store.getAll(result.getId(), SegmentChord.class)));
    // assert choices
    Collection<SegmentChoice> segmentChoices =
      store.getAll(result.getId(), SegmentChoice.class);
    SegmentChoice macroChoice = Segments.findFirstOfType(segmentChoices, ProgramType.Macro);
    assertEquals(fake.program4_sequence0_binding0.getId(), macroChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    SegmentChoice mainChoice = Segments.findFirstOfType(segmentChoices, ProgramType.Main);
    assertEquals(fake.program5_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }
}
