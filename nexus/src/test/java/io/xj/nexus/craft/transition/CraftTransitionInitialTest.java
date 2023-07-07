// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.transition;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import io.xj.hub.HubTopology;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubContent;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.stream.Collectors;

import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;

@RunWith(MockitoJUnitRunner.class)
public class CraftTransitionInitialTest {
  @Mock
  public HubClient hubClient;
  @Mock
  public NotificationProvider notificationProvider;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private Segment segment6;

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

    // Mock request via HubClient returns fake generated library of hub content
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream(),
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    var chain2 = store.put(buildChain(
      fake.account1,
      "Print #2",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      buildTemplate(fake.account1, "Test")
    ));

    // segment crafting
    segment6 = store.put(buildSegment(
      chain2,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.CRAFTING,
      "C minor",
      16,
      0.55,
      130.0,
      "chains-1-segments-9f7s89d8a7892.wav",
      true));
    store.put(buildSegmentChoice(
      segment6,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment6,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence0_binding0));
    for (String memeName : ImmutableList.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment6, memeName));

    store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment6, 0.0, "C minor"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment6, 8.0, "Db minor"));
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftTransitionInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment6);

    craftFactory.transition(fabricator).doWork();

//    // assert choice of transition-type sequence
//    Collection<SegmentChoice> segmentChoices =
//      store.getAll(segment6.getId(), SegmentChoice.class);
//    assertNotNull(Segments.findFirstOfType(segmentChoices, InstrumentType.Sweep, InstrumentMode.Transition));
  }
}
