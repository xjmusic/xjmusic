// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.detail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.api.*;
import io.xj.hub.HubTopology;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.Segments;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import static io.xj.nexus.NexusIntegrationTestingFixtures.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class CraftDetailProgramVoiceNextMacroTest {
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
    Environment env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new NexusWorkModule())
      .with(new AbstractModule() {
        @Override
        public void configure() {
          bind(Environment.class).toInstance(env);
          bind(HubClient.class).toInstance(hubClient);
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
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream(),
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

  @Test
  public void craftDetailVoiceNextMacro() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4);

    craftFactory.detail(fabricator).doWork();

    // assert detail choice
    Collection<SegmentChoice> segmentChoices = fabricator.getChoices();
    SegmentChoice beatChoice = segmentChoices.stream()
      .filter(c -> c.getProgramType().equals(ProgramType.Detail.toString())).findFirst().orElseThrow();
    assertTrue(fabricator.getArrangements()
      .stream().anyMatch(a -> a.getSegmentChoiceId().equals(beatChoice.getId())));
    // test vector for [#154014731] persist Audio pick in memory
    int pickedBloop = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fake.instrument9_audio8.getId()))
        pickedBloop++;
    }
    assertEquals(16, pickedBloop);
  }

  @Test
  public void craftDetailVoiceNextMacro_okIfNoDetailChoice() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4);

    craftFactory.detail(fabricator).doWork();
  }

  /**
   Insert fixture segments 3 and 4, including the detail choice for segment 3 only if specified

   @param excludeDetailChoiceForSegment3 if desired for the purpose of this test
   */
  private void insertSegments3and4(boolean excludeDetailChoiceForSegment3) throws NexusException {
    // Chain "Test Print #1" has this segment that was just crafted
    Segment segment3 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      2,
      2,
      SegmentState.CRAFTED,
      Instant.parse("2017-02-14T12:02:04.000001Z"),
      Instant.parse("2017-02-14T12:02:36.000001Z"),
      "Ab minor",
      64,
      0.30,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav"));
    store.put(buildSegmentChoice(
      segment3,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence2_binding0));
    store.put(buildSegmentChoice(
      segment3,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence1_binding0));
    if (!excludeDetailChoiceForSegment3)
      store.put(buildSegmentChoice(
        segment3,
        Segments.DELTA_UNLIMITED,
        Segments.DELTA_UNLIMITED,
        fake.program10));

    // Chain "Test Print #1" has a segment in crafting state - Foundation is complete
    segment4 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      3,
      3,
      SegmentState.CRAFTING,
      Instant.parse("2017-02-14T12:03:08.000001Z"),
      Instant.parse("2017-02-14T12:03:15.836735Z"),
      "F minor",
      16,
      0.45,
      125.0,
      "chains-1-segments-9f7s89d8a7892.wav"));
    store.put(buildSegmentChoice(
      segment4,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program3,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment4,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program15,
      fake.program15_sequence0_binding0));
    for (String memeName : ImmutableList.of("Hindsight", "Chunky", "Regret", "Tangy"))
      store.put(buildSegmentMeme(segment4, memeName));
    SegmentChord chord0 = store.put(buildSegmentChord(segment4, 0.0, "F minor"));
    store.put(buildSegmentChordVoicing(chord0, InstrumentType.Bass, "F2, Ab2, B2"));
    SegmentChord chord1 = store.put(buildSegmentChord(segment4, 8.0, "Gb minor"));
    store.put(buildSegmentChordVoicing(chord1, InstrumentType.Bass, "Gb2, A2, Db3"));
  }

}
