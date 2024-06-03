// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.macro_main;

import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.ProgramType;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.FabricationContentTwoFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.FabricatorFactory;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentMeme;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.engine.fabricator.FabricationEntityStoreImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.model.util.Assertion.assertSameItems;
import static io.xj.engine.FabricationContentTwoFixtures.buildChain;
import static io.xj.engine.FabricationContentTwoFixtures.buildSegment;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CraftSegmentPatternMemeTest {
  final Logger LOG = LoggerFactory.getLogger(CraftSegmentPatternMemeTest.class);
  static final int TEST_REPEAT_ITERATIONS = 14;


  /**
   Test to ensure that the following Macro-Program is based on its first sequence-binding meme
   matching the last sequence-binding meme of the preceding Macro-Program
   <p>
   Segment memes expected to be taken directly of sequence_pattern binding https://github.com/xjmusic/workstation/issues/298
   Macro program sequence should advance after each main program https://github.com/xjmusic/workstation/issues/299
   */
  @Test
  public void craftSegment() throws Exception {
    for (int i = 1; i <= TEST_REPEAT_ITERATIONS; i++) {
      LOG.info("ATTEMPT NUMBER {}", i);

      CraftFactory craftFactory = new CraftFactoryImpl();
      var jsonProvider = new JsonProviderImpl();
      var entityFactory = new EntityFactoryImpl(jsonProvider);
      var store = new FabricationEntityStoreImpl(entityFactory);
      JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
      FabricatorFactory fabricatorFactory = new FabricatorFactoryImpl(
        store,
        jsonapiPayloadFactory,
        jsonProvider
      );
      HubTopology.buildHubApiTopology(entityFactory);
      FabricationTopology.buildFabricationTopology(entityFactory);

      // Manipulate the underlying entity store; reset before each test
      store.clear();

      // Mock request via HubClientFactory returns fake generated library of model content
      FabricationContentTwoFixtures fake = new FabricationContentTwoFixtures();
      HubContent sourceMaterial = new HubContent(Stream.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()
      ).collect(Collectors.toList()));

      // Chain "Test Print #1" has 5 total segments
      Chain chain = store.put(FabricationContentTwoFixtures.buildChain(fake.project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));

      // Preceding Segment
      Segment previousSegment = store.put(FabricationContentTwoFixtures.buildSegment(
        chain,
        1,
        SegmentState.CRAFTING,
        "F Major",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav"
      ));
      store.put(FabricationContentTwoFixtures.buildSegmentChoice(previousSegment, ProgramType.Macro, fake.program4_sequence1_binding0));
      store.put(FabricationContentTwoFixtures.buildSegmentChoice(previousSegment, ProgramType.Main, fake.program5_sequence1_binding0));

      // Following Segment
      Segment segment = store.put(FabricationContentTwoFixtures.buildSegment(chain, 2, SegmentState.PLANNED, "C", 8, 0.8f, 120, "chain-1-waveform-12345"));

      craftFactory.macroMain(fabricatorFactory.fabricate(sourceMaterial, segment.getId(), 48000.0f, 2, null), null, null).doWork();

      var result = store.readSegment(segment.getId()).orElseThrow();
      assertEquals(SegmentType.NEXT_MACRO, result.getType());
      assertSameItems(List.of("REGRET", "HINDSIGHT", "CHUNKY", "TANGY"),
        EntityUtils.namesOf(store.readAll(result.getId(), SegmentMeme.class)));
    }
  }
}
