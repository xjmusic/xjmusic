// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.detail;

import com.google.common.collect.ImmutableList;
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
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.hub.Topology;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
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
import java.util.stream.Collectors;

import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChordVoicing;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentMeme;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class CraftDetailNextMainTest {
  @Mock
  public HubClient hubClient;
  private Chain chain1;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Environment env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new NexusWorkModule())
      .with(new AbstractModule() {
        @Override
        public void configure() {
          bind(Config.class).toInstance(config);
          bind(Environment.class).toInstance(env);
          bind(HubClient.class).toInstance(hubClient);
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
      fake.setupFixtureB2().stream(),
      fake.setupFixtureB3().stream(),
      fake.setupFixtureB4_DetailBass().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(NexusIntegrationTestingFixtures.buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(buildSegment(
      chain1,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.DUBBED,
      Instant.parse("2017-02-14T12:01:00.000001Z"),
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      "D major",
      64,
      0.73,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      "wav"));
    store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      1,
      1,
      SegmentState.DUBBING,
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      Instant.parse("2017-02-14T12:02:04.000001Z"),
      "Db minor",
      64,
      0.85,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav"));
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftDetailNextMain() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment4);

    craftFactory.detail(fabricator).doWork();

// assert choice of detail-type sequence
    Collection<SegmentChoice> segmentChoices =
      store.getAll(segment4.getId(), SegmentChoice.class);
    assertNotNull(Segments.findFirstOfType(segmentChoices, ProgramType.Detail));
  }

  @Test
  public void craftDetailNextMain_okEvenWithoutPreviousSegmentDetailChoice() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment4);

    craftFactory.detail(fabricator).doWork();

    // assert choice of detail-type sequence
    Collection<SegmentChoice> segmentChoices =
      store.getAll(segment4.getId(), SegmentChoice.class);
    assertNotNull(Segments.findFirstOfType(segmentChoices, ProgramType.Detail));
  }

  /**
   Insert fixture segments 3 and 4, including the detail choice for segment 3 only if specified

   @param excludeDetailChoiceForSegment3 if desired for the purpose of this test
   */
  private void insertSegments3and4(boolean excludeDetailChoiceForSegment3) throws NexusException {
    // segment just crafted
    // Testing entities for reference
    Segment segment3 = store.put(buildSegment(
      chain1,
      SegmentType.NEXTMAIN,
      2,
      2,
      SegmentState.CRAFTED,
      Instant.parse("2017-02-14T12:02:04.000001Z"),
      Instant.parse("2017-02-14T12:02:36.000001Z"),
      "F Major",
      64,
      0.30,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav"));
    store.put(buildSegmentChoice(
      segment3,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment3,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program15,
      fake.program15_sequence1_binding0));
    if (!excludeDetailChoiceForSegment3)
      store.put(buildSegmentChoice(
        segment3,
        Segments.DELTA_UNLIMITED,
        Segments.DELTA_UNLIMITED,
        fake.program10));

    // segment crafting
    segment4 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      3,
      3,
      SegmentState.CRAFTING,
      Instant.parse("2017-02-14T12:03:08.000001Z"),
      Instant.parse("2017-02-14T12:03:15.836735Z"),
      "G minor",
      16,
      0.45,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav"));
    store.put(buildSegmentChoice(
      segment4,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence1_binding0));
    store.put(buildSegmentChoice(
      segment4,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program15,
      fake.program15_sequence0_binding0));
    for (String memeName : ImmutableList.of("Regret", "Sky", "Hindsight", "Tropical"))
      store.put(buildSegmentMeme(segment4, memeName));

    SegmentChord chord0 = store.put(buildSegmentChord(segment4, 0.0, "G minor"));
    store.put(buildSegmentChordVoicing(chord0, InstrumentType.Bass, "G2, Bb2, D3"));
    SegmentChord chord1 = store.put(buildSegmentChord(segment4, 8.0, "Ab minor"));
    store.put(buildSegmentChordVoicing(chord1, InstrumentType.Bass, "Ab2, C3, Eb3"));
  }


}
