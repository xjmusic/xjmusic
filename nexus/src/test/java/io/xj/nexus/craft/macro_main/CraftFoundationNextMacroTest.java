// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import io.xj.hub.HubTopology;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
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
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.nexus.persistence.Segments;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.lib.util.Assertion.assertSameItems;
import static io.xj.lib.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.lib.util.ValueUtils.SECONDS_PER_MINUTE;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class CraftFoundationNextMacroTest {
  static final int TEST_REPEAT_ITERATIONS = 14;
  @Mock
  public HubClient hubClient;
  @Mock
  public NotificationProvider notificationProvider;


  /**
   * Test to ensure that the following Macro-Program is based on its first sequence-binding meme
   * matching the last sequence-binding meme of the preceding Macro-Program
   */
  @Test
  public void craftFoundationNextMacro() throws Exception {
    for (int i = 0; i < TEST_REPEAT_ITERATIONS; i++) {
      JsonProvider jsonProvider = new JsonProviderImpl();
      var entityFactory = new EntityFactoryImpl(jsonProvider);
      ApiUrlProvider apiUrlProvider = new ApiUrlProvider("");
      var craftFactory = new CraftFactoryImpl(apiUrlProvider);
      HubTopology.buildHubApiTopology(entityFactory);
      NexusTopology.buildNexusApiTopology(entityFactory);
      JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
      var store = new NexusEntityStoreImpl(entityFactory);
      SegmentManager segmentManager = new SegmentManagerImpl(entityFactory, store);
      var fabricatorFactory = new FabricatorFactoryImpl(
        segmentManager,
        jsonapiPayloadFactory,
        jsonProvider
      );

      // Manipulate the underlying entity store; reset before each test
      store.deleteAll();

      // Mock request via HubClient returns fake generated library of hub content
      NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
      HubContent sourceMaterial = new HubContent(Stream.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()
      ).collect(Collectors.toList()));

      // Chain "Test Print #1" has 5 total segments
      Chain chain1 = store.put(buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
      store.put(buildSegment(
        chain1,
        0,
        SegmentState.CRAFTED,
        "D major",
        64,
        0.73,
        120.0,
        "chains-1-segments-9f7s89d8a7892"
      ));
      store.put(buildSegment(
        chain1,
        1,
        SegmentState.CRAFTING,
        "Db minor",
        64,
        0.85,
        120.0,
        "chains-1-segments-9f7s89d8a7892.wav"
      ));

      // Chain "Test Print #1" has this segment that was just crafted
      Segment segment3 = store.put(buildSegment(
        chain1,
        2,
        SegmentState.CRAFTED,
        "Ab minor",
        64,
        0.30,
        120.0,
        "chains-1-segments-9f7s89d8a7892.wav"
      ));
      store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.Macro, fake.program4_sequence2_binding0));
      store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.Main, fake.program5_sequence1_binding0));

      // Chain "Test Print #1" has a planned segment
      Segment segment4 = store.put(buildSegment(chain1, 3, SegmentState.PLANNED, "C", 8, 0.8, 120, "chain-1-waveform-12345"));

      Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4);

      craftFactory.macroMain(fabricator).doWork();

      Segment result = store.getSegment(segment4.getId()).orElseThrow();
      assertEquals(SegmentType.NEXTMACRO, result.getType());
      assertEquals(16 * MICROS_PER_SECOND * SECONDS_PER_MINUTE / 140, (long) Objects.requireNonNull(result.getDurationMicros()));
      assertEquals(Integer.valueOf(16), result.getTotal());
      assertEquals(0.1, result.getDensity(), 0.01);
      assertEquals("G -", result.getKey());
      assertEquals(140, result.getTempo(), 0.01);
      // assert memes
      assertSameItems(
        List.of("REGRET", "CHUNKY", "HINDSIGHT", "TANGY"),
        Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
      // assert chords
      assertSameItems(List.of("Ab -", "G -"),
        Entities.namesOf(store.getAll(result.getId(), SegmentChord.class)));
      // assert choices
      Collection<SegmentChoice> segmentChoices =
        store.getAll(result.getId(), SegmentChoice.class);
      // assert macro choice
      SegmentChoice macroChoice = Segments.findFirstOfType(segmentChoices, ProgramType.Macro);
      assertEquals(fake.program3_sequence0_binding0.getId(), macroChoice.getProgramSequenceBindingId());
      assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
      // assert main choice
      SegmentChoice mainChoice = Segments.findFirstOfType(segmentChoices, ProgramType.Main);
      assertEquals(fake.program15_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
      assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
    }
  }
}
