// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.hook;

import io.xj.hub.HubContent;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.nexus.persistence.Segments;
import io.xj.test_fixtures.NexusIntegrationTestingFixtures;
import org.junit.After;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegmentMeme;

@ExtendWith(MockitoExtension.class)
public class CraftHookNextMacroTest {
  @Mock
  public HubClient hubClient;
  @Mock
  public NotificationProvider notificationProvider;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  NexusIntegrationTestingFixtures fake;
  Chain chain1;
  Segment segment4;
  NexusEntityStore store;
  HubContent sourceMaterial;

  @BeforeEach
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

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      Stream.concat(fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()),
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(NexusIntegrationTestingFixtures.buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
    store.put(buildSegment(
      chain1,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.CRAFTED,
      "D major",
      64,
      0.73,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      true));
    store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      1,
      0,
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav",
      true));
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftHookNextMacro() throws Exception {
    insertSegments3and4();
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4);

    craftFactory.hook(fabricator).doWork();

//    // assert choice of hook-type sequence
//    Collection<SegmentChoice> segmentChoices =
//      store.getAll(segment4.getId(), SegmentChoice.class);
//    assertNotNull(Segments.findFirstOfType(segmentChoices, InstrumentType.Hook));
  }

  /**
   * Insert fixture segments 3 and 4, including the hook choice for segment 3 only if specified
   */
  void insertSegments3and4() throws NexusException {
    // Chain "Test Print #1" has this segment that was just crafted
    Segment segment3 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      2,
      2,
      SegmentState.CRAFTED,
      "Ab minor",
      64,
      0.30,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav", true));
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

    // Chain "Test Print #1" has a segment in crafting state - Foundation is complete
    segment4 = store.put(buildSegment(
      chain1,
      SegmentType.NEXTMACRO,
      3,
      0,
      SegmentState.CRAFTING,
      "F minor",
      16,
      0.45,
      125.0,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(
      segment4,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment4,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program15,
      fake.program15_sequence0_binding0));
    for (String memeName : List.of("Hindsight", "Chunky", "Regret", "Tangy"))
      store.put(buildSegmentMeme(segment4, memeName));
    store.put(buildSegmentChord(segment4, 0.0, "F minor"));
    store.put(buildSegmentChord(segment4, 8.0, "Gb minor"));
  }


}
