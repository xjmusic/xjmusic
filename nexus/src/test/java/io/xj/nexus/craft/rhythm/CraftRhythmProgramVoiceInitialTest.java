// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.rhythm;

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
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.hub.HubTopology;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusException;
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

import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentMeme;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class CraftRhythmProgramVoiceInitialTest {
  @Mock
  public HubClient hubClient;
  private Chain chain2;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;
  private Segment segment0;

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
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // force known rhythm selection by destroying program 35
    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB3().stream())
      .filter(entity -> !Entities.isSame(entity, fake.program35) && !Entities.isChild(entity, fake.program35))
      .collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = store.put(buildChain(
      fake.account1,
      "Print #2",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      buildTemplate(fake.account1, "Tests"),
      Instant.parse("2014-08-12T12:17:02.527142Z")));
  }

  @Test
  public void craftRhythmVoiceInitial() throws Exception {
    insertSegment();

    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment0);

    craftFactory.rhythm(fabricator).doWork();

    Segment result = store.getSegment(segment0.getId()).orElseThrow();
    assertFalse(store.getAll(result.getId(), SegmentChoice.class).isEmpty());
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedBleep = 0;
    int pickedToot = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8kick.getId()))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8snare.getId()))
        pickedSnare++;
      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8bleep.getId()))
        pickedBleep++;
      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8toot.getId()))
        pickedToot++;
    }
    assertEquals(16, pickedKick);
    assertEquals(16, pickedSnare);
  }

  @Test
  public void craftRhythmVoiceInitial_okWhenNoRhythmChoice() throws Exception {
    insertSegment();
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment0);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the rhythm choice only if specified
   */
  private void insertSegment() throws NexusException {
    segment0 = store.put(buildSegment(
      chain2,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.CRAFTING,
      Instant.parse("2017-02-14T12:01:00.000001Z"),
      Instant.parse("2017-02-14T12:01:07.384616Z"),
      "D Major",
      32,
      0.55,
      130.0,
      "chains-1-segments-9f7s89d8a7892.wav"));
    store.put(buildSegmentChoice(
      segment0,
      0,
      Segments.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment0,
      0,
      Segments.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence0_binding0));
    for (String memeName : ImmutableList.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(buildSegmentMeme(segment0, memeName));

    store.put(buildSegmentChord(segment0, 0.0, "C minor"));
    store.put(buildSegmentChord(segment0, 8.0, "Db minor"));
  }

}
