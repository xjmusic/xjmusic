// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import io.xj.hub.HubTopology;
import io.xj.nexus.hub_client.HubClient;
import io.xj.hub.ingest.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import io.xj.test_fixtures.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManagerImpl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.lib.util.Assertion.assertSameItems;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CraftSegmentPatternMemeTest {
  final Logger LOG = LoggerFactory.getLogger(CraftSegmentPatternMemeTest.class);
  static final int TEST_REPEAT_ITERATIONS = 14;

  @Mock
  public HubClient hubClient;
  @Mock
  public NotificationProvider notificationProvider;

  /**
   * Test to ensure that the following Macro-Program is based on its first sequence-binding meme
   * matching the last sequence-binding meme of the preceding Macro-Program
   * <p>
   * Segment memes expected to be taken directly of sequence_pattern binding https://www.pivotaltracker.com/story/show/165803886
   * Macro program sequence should advance after each main program https://www.pivotaltracker.com/story/show/176728582
   */
  @Test
  public void craftSegment() throws Exception {
    for (int i = 1; i <= TEST_REPEAT_ITERATIONS; i++) {
      LOG.info("ATTEMPT NUMBER {}", i);

      ApiUrlProvider apiUrlProvider = new ApiUrlProvider("");
      CraftFactory craftFactory = new CraftFactoryImpl(apiUrlProvider);
      var jsonProvider = new JsonProviderImpl();
      var entityFactory = new EntityFactoryImpl(jsonProvider);
      var store = new NexusEntityStoreImpl(entityFactory);
      var segmentManager = new SegmentManagerImpl(entityFactory, store);
      JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
      FabricatorFactory fabricatorFactory = new FabricatorFactoryImpl(
        segmentManager,
        jsonapiPayloadFactory,
        jsonProvider
      );
      HubTopology.buildHubApiTopology(entityFactory);
      NexusTopology.buildNexusApiTopology(entityFactory);

      // Manipulate the underlying entity store; reset before each test
      store.deleteAll();

      // Mock request via HubClient returns fake generated library of hub content
      NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
      HubContent sourceMaterial = new HubContent(Stream.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()
      ).collect(Collectors.toList()));

      // Chain "Test Print #1" has 5 total segments
      Chain chain = store.put(buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));

      // Preceding Segment
      Segment previousSegment = store.put(buildSegment(
        chain,
        1,
        SegmentState.CRAFTING,
        "F Major",
        64,
        0.30,
        120.0,
        "chains-1-segments-9f7s89d8a7892.wav"
      ));
      store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(previousSegment, ProgramType.Macro, fake.program4_sequence1_binding0));
      store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(previousSegment, ProgramType.Main, fake.program5_sequence1_binding0));

      // Following Segment
      Segment segment = store.put(buildSegment(chain, 2, SegmentState.PLANNED, "C", 8, 0.8, 120, "chain-1-waveform-12345"));

      craftFactory.macroMain(fabricatorFactory.fabricate(sourceMaterial, segment)).doWork();

      var result = store.getSegment(segment.getId()).orElseThrow();
      assertEquals(SegmentType.NEXTMACRO, result.getType());
      assertSameItems(List.of("REGRET", "HINDSIGHT", "CHUNKY", "TANGY"),
        Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
    }
  }
}
