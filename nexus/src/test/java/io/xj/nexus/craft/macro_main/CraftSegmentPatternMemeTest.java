// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import io.xj.hub.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.entity.EntityUtils;
import io.xj.nexus.json.JsonProviderImpl;
import io.xj.nexus.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManagerImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.util.Assertion.assertSameItems;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CraftSegmentPatternMemeTest {
  final Logger LOG = LoggerFactory.getLogger(CraftSegmentPatternMemeTest.class);
  static final int TEST_REPEAT_ITERATIONS = 14;

  @Mock
  public HubClient hubClient;

  /**
   Test to ensure that the following Macro-Program is based on its first sequence-binding meme
   matching the last sequence-binding meme of the preceding Macro-Program
   <p>
   Segment memes expected to be taken directly of sequence_pattern binding https://www.pivotaltracker.com/story/show/165803886
   Macro program sequence should advance after each main program https://www.pivotaltracker.com/story/show/176728582
   */
  @Test
  public void craftSegment() throws Exception {
    for (int i = 1; i <= TEST_REPEAT_ITERATIONS; i++) {
      LOG.info("ATTEMPT NUMBER {}", i);

        CraftFactory craftFactory = new CraftFactoryImpl();
      var jsonProvider = new JsonProviderImpl();
      var entityFactory = new EntityFactoryImpl(jsonProvider);
      var store = new NexusEntityStoreImpl(entityFactory);
      var segmentManager = new SegmentManagerImpl(store);
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

      craftFactory.macroMain(fabricatorFactory.fabricate(sourceMaterial, segment, 48000.0, 2)).doWork();

      var result = store.getSegment(segment.getId()).orElseThrow();
      assertEquals(SegmentType.NEXTMACRO, result.getType());
      assertSameItems(List.of("REGRET", "HINDSIGHT", "CHUNKY", "TANGY"),
        EntityUtils.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
    }
  }
}
