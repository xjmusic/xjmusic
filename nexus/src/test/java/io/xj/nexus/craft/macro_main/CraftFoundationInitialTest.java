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
import io.xj.api.ProgramType;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.api.TemplateType;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTestConfiguration;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.dao.Segments;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.lib.util.Assert.assertSameItems;
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
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream()
    ).collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial planned segment
    Chain chain2 = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("Print #2")
      .templateId(fake.template1.getId())
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2014-08-12T12:17:02.527142Z"));
    segment6 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain2.getId())
      .offset(0L)
      .state(SegmentState.PLANNED)
      .beginAt("2017-02-14T12:01:00.000001Z")
      .key("C")
      .total(8)
      .density(0.8)
      .tempo(120.0)
      .storageKey("chain-1-waveform-12345.wav"));
  }

  @Test
  public void craftFoundationInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment6);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = store.getSegment(segment6.getId()).orElseThrow();
    assertEquals(segment6.getId(), result.getId());
    assertEquals("WAV", result.getOutputEncoder());
    assertEquals(SegmentType.INITIAL, result.getType());
    assertEquals("2017-02-14T12:01:07.384616Z", result.getEndAt());
    assertEquals(Integer.valueOf(16), result.getTotal());
    assertEquals(0.55, result.getDensity(), 0.01);
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
    SegmentChoice macroChoice = Segments.findFirstOfType(segmentChoices, ProgramType.MACRO);
    assertEquals(fake.program4_sequence0_binding0.getId(), macroChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    SegmentChoice mainChoice = Segments.findFirstOfType(segmentChoices, ProgramType.MAIN);
    assertEquals(fake.program5_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }
}
