// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import io.xj.hub.HubContent;
import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.entity.EntityUtils;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.util.Assertion.assertSameItems;
import static io.xj.hub.util.ValueUtils.MICROS_PER_MINUTE;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CraftFoundationNextMacroTest {
  static final int TEST_REPEAT_ITERATIONS = 14;


  /**
   Test to ensure that the following Macro-Program is based on its first sequence-binding meme
   matching the last sequence-binding meme of the preceding Macro-Program
   */
  @Test
  public void craftFoundationNextMacro() throws Exception {
    for (int i = 0; i < TEST_REPEAT_ITERATIONS; i++) {
      JsonProvider jsonProvider = new JsonProviderImpl();
      var entityFactory = new EntityFactoryImpl(jsonProvider);
      var craftFactory = new CraftFactoryImpl();
      HubTopology.buildHubApiTopology(entityFactory);
      NexusTopology.buildNexusApiTopology(entityFactory);
      JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
      var store = new NexusEntityStoreImpl(entityFactory);
      var fabricatorFactory = new FabricatorFactoryImpl(
        store,
        jsonapiPayloadFactory,
        jsonProvider
      );

      // Manipulate the underlying entity store; reset before each test
      store.clear();

      // Mock request via HubClientFactory returns fake generated library of hub content
      NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
      HubContent sourceMaterial = new HubContent(Stream.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()
      ).collect(Collectors.toList()));

      // Chain "Test Print #1" has 5 total segments
      Chain chain1 = store.put(buildChain(fake.project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
      store.put(buildSegment(
        chain1,
        0,
        SegmentState.CRAFTED,
        "D major",
        64,
        0.73f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892"
      ));
      store.put(buildSegment(
        chain1,
        1,
        SegmentState.CRAFTING,
        "Db minor",
        64,
        0.85f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav"
      ));

      // Chain "Test Print #1" has this segment that was just crafted
      Segment segment3 = store.put(buildSegment(
        chain1,
        2,
        SegmentState.CRAFTED,
        "Ab minor",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav"
      ));
      store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.Macro, fake.program4_sequence2_binding0));
      store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.Main, fake.program5_sequence1_binding0));

      // Chain "Test Print #1" has a planned segment
      Segment segment4 = store.put(buildSegment(chain1, 3, SegmentState.PLANNED, "C", 8, 0.8f, 120, "chain-1-waveform-12345"));

      Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);

      craftFactory.macroMain(fabricator, null, null).doWork();

      Segment result = store.readSegment(segment4.getId()).orElseThrow();
      assertEquals(SegmentType.NEXT_MACRO, result.getType());
      assertEquals(16 * MICROS_PER_MINUTE / 140, (long) Objects.requireNonNull(result.getDurationMicros()));
      assertEquals(Integer.valueOf(16), result.getTotal());
      assertEquals(0.45, result.getIntensity(), 0.01);
      assertEquals("G -", result.getKey());
      assertEquals(140, result.getTempo(), 0.01);
      // assert memes
      assertSameItems(
        List.of("REGRET", "CHUNKY", "HINDSIGHT", "TANGY"),
        EntityUtils.namesOf(store.readAll(result.getId(), SegmentMeme.class)));
      // assert chords
      assertSameItems(List.of("Ab -", "G -"),
        EntityUtils.namesOf(store.readAll(result.getId(), SegmentChord.class)));
      // assert choices
      Collection<SegmentChoice> segmentChoices =
        store.readAll(result.getId(), SegmentChoice.class);
      // assert macro choice
      SegmentChoice macroChoice = SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Macro);
      assertEquals(fake.program3_sequence0_binding0.getId(), macroChoice.getProgramSequenceBindingId());
      assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
      // assert main choice
      SegmentChoice mainChoice = SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Main);
      assertEquals(fake.program15_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
      assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
    }
  }
}
