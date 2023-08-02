// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.beat;

import io.xj.hub.HubTopology;
import io.xj.nexus.hub_client.HubClient;
import io.xj.hub.ingest.HubContent;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.nexus.persistence.Segments;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentMeme;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class CraftBeatProgramVoiceInitialTest {
  @Mock
  public HubClient hubClient;
  @Mock
  public NotificationProvider notificationProvider;
  Chain chain2;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  NexusEntityStore store;
  NexusIntegrationTestingFixtures fake;
  Segment segment0;

  @Before
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    ApiUrlProvider apiUrlProvider = new ApiUrlProvider("");
    craftFactory = new CraftFactoryImpl(apiUrlProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    store = new NexusEntityStoreImpl(entityFactory);
    SegmentManager segmentManager = new SegmentManagerImpl(entityFactory, store);
    fabricatorFactory = new FabricatorFactoryImpl(
      segmentManager,
      jsonapiPayloadFactory,
      jsonProvider
    );

    // Manipulate the underlying entity store; reset before each test
    store.deleteAll();

    // force known beat selection by destroying program 35
    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(
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
      buildTemplate(fake.account1, "Tests")
    ));
  }

  @Test
  public void craftBeatVoiceInitial() throws Exception {
    insertSegment();

    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment0);

    craftFactory.beat(fabricator).doWork();

    Segment result = store.getSegment(segment0.getId()).orElseThrow();
    assertFalse(store.getAll(result.getId(), SegmentChoice.class).isEmpty());
    // test vector for persist Audio pick in memory https://www.pivotaltracker.com/story/show/154014731
  }

  @Test
  public void craftBeatVoiceInitial_okWhenNoBeatChoice() throws Exception {
    insertSegment();
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment0);

    craftFactory.beat(fabricator).doWork();
  }

  /**
   * Insert fixture segment 6, including the beat choice only if specified
   */
  void insertSegment() throws NexusException {
    segment0 = store.put(buildSegment(
      chain2,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.CRAFTING,
      "D Major",
      32,
      0.55,
      130.0,
      "chains-1-segments-9f7s89d8a7892.wav", true));
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
    for (String memeName : List.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(buildSegmentMeme(segment0, memeName));

    store.put(buildSegmentChord(segment0, 0.0, "C minor"));
    store.put(buildSegmentChord(segment0, 8.0, "Db minor"));
  }

}
