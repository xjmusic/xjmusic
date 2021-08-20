// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
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
import io.xj.nexus.NexusException;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.lib.util.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(MockitoJUnitRunner.class)
public class CraftFoundationNextMainTest {
  @Mock
  public HubClient hubClient;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private NexusIntegrationTestingFixtures fake;
  private Chain chain1;
  private Segment segment4;
  private NexusEntityStore store;
  private HubContent sourceMaterial;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault()
      .withValue("program.doTranspose", ConfigValueFactory.fromAnyRef(true))
      .withValue("instrument.isTonal", ConfigValueFactory.fromAnyRef(true));
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
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(NexusIntegrationTestingFixtures.buildChain(fake.account1, "Test Print #1", TemplateType.PRODUCTION, ChainState.FABRICATE, fake.template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
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
      .outputEncoder("wav"));
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
      .storageKey("chains-1-segments-9f7s89d8a7892.wav"));

    // Chain "Test Print #1" has this segment that was just crafted
    Segment segment3 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .offset(2L)
      .state(SegmentState.CRAFTED)
      .beginAt("2017-02-14T12:02:04.000001Z")
      .endAt("2017-02-14T12:02:36.000001Z")
      .key("F Major")
      .type(SegmentType.CONTINUE)
      .total(64)
      .density(0.30)
      .tempo(120.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.MACRO, fake.program4_sequence0_binding0));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.MAIN, fake.program5_sequence1_binding0));

    // Chain "Test Print #1" has a planned segment
    segment4 = store.put(NexusIntegrationTestingFixtures.buildSegment(chain1, 3, SegmentState.PLANNED, Instant.parse("2017-02-14T12:03:08.000001Z"), null, "C", 8, 0.8, 120, "chain-1-waveform-12345", "wav"));
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftFoundationNextMain() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment4);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = store.getSegment(segment4.getId()).orElseThrow();
    assertEquals(SegmentType.NEXTMAIN, result.getType());
    assertEquals("2017-02-14T12:03:15.840157Z", result.getEndAt());
    assertEquals(Integer.valueOf(16), result.getTotal());
    assertEquals("WAV", result.getOutputEncoder());
    assertEquals(0.45, result.getDensity(), 0.01);
    assertEquals("G minor", result.getKey());
    assertEquals(125, result.getTempo(), 0.01);
    // assert memes
    assertSameItems(Lists.newArrayList("HINDSIGHT", "TROPICAL", "COZY", "WILD", "REGRET"),
      Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
    // assert chords
    assertSameItems(Lists.newArrayList("G minor", "Ab minor"),
      Entities.namesOf(store.getAll(result.getId(), SegmentChord.class)));
    // assert choices
    Collection<SegmentChoice> segmentChoices =
      store.getAll(result.getId(), SegmentChoice.class);
    // assert macro choice
    SegmentChoice macroChoice = Segments.findFirstOfType(segmentChoices, ProgramType.MACRO);
    assertEquals(fake.program4_sequence1_binding0.getId(), macroChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    // assert main choice
    SegmentChoice mainChoice = Segments.findFirstOfType(segmentChoices, ProgramType.MAIN);
    assertEquals(fake.program15_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }

  /**
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance
   */
  @Test
  public void craftFoundationNextMain_revertsAndRequeuesOnFailure() throws Exception {
    // Chain "Test Print #1" has a dangling (preceded by another planned segment) planned segment
    Segment segment5 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain1.getId())
      .offset(4L)
      .state(SegmentState.PLANNED)
      .beginAt("2017-02-14T12:03:08.000001Z")
      .key("C")
      .total(8)
      .density(0.8)
      .tempo(120.0)
      .storageKey("chain-1-waveform-12345.wav"));

    assertThrows(NexusException.class, () ->
      fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment5));
  }

}
