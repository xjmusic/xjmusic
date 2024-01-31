// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import io.xj.hub.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.entity.EntityUtils;
import io.xj.nexus.fabricator.FabricationFatalException;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.hub.HubTopology;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.util.Assertion.assertSameItems;
import static io.xj.hub.util.ValueUtils.MICROS_PER_MINUTE;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class CraftFoundationNextMainTest {
  @Mock
  public HubClient hubClient;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  NexusIntegrationTestingFixtures fake;
  Chain chain1;
  Segment segment4;
  NexusEntityStore store;
  HubContent sourceMaterial;

  @BeforeEach
  public void setUp() throws Exception {
    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    store = new NexusEntityStoreImpl(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );
    craftFactory = new CraftFactoryImpl();
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(NexusIntegrationTestingFixtures.buildChain(fake.project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
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
      "F Major",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.Macro, fake.program4_sequence0_binding0));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.Main, fake.program5_sequence1_binding0));

    // Chain "Test Print #1" has a planned segment
    segment4 = store.put(buildSegment(chain1, 3, SegmentState.PLANNED, "C", 8, 0.8f, 120, "chain-1-waveform-12345"));
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void craftFoundationNextMain() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);

    craftFactory.macroMain(fabricator, null, null).doWork();

    Segment result = store.readSegment(segment4.getId()).orElseThrow();
    assertEquals(SegmentType.NEXT_MAIN, result.getType());
    assertEquals(16 * MICROS_PER_MINUTE / 140, (long) Objects.requireNonNull(result.getDurationMicros()));
    assertEquals(Integer.valueOf(16), result.getTotal());
    assertEquals(0.1, result.getIntensity(), 0.01);
    assertEquals("G -", result.getKey());
    assertEquals(140, result.getTempo(), 0.01);
    // assert memes
    assertSameItems(List.of("HINDSIGHT", "TROPICAL", "COZY", "WILD", "REGRET"),
      EntityUtils.namesOf(store.readAll(result.getId(), SegmentMeme.class)));
    // assert chords
    assertSameItems(List.of("G -", "Ab -"),
      EntityUtils.namesOf(store.readAll(result.getId(), SegmentChord.class)));
    // assert choices
    Collection<SegmentChoice> segmentChoices =
      store.readAll(result.getId(), SegmentChoice.class);
    // assert macro choice
    SegmentChoice macroChoice = SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Macro);
    assertEquals(fake.program4_sequence1_binding0.getId(), macroChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    // assert main choice
    SegmentChoice mainChoice = SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Main);
    assertEquals(fake.program15_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }

  /**
   Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance https://www.pivotaltracker.com/story/show/158610991
   */
  @Test
  public void craftFoundationNextMain_revertsAndRequeueOnFailure() throws Exception {
    // Chain "Test Print #1" has a dangling (preceded by another planned segment) planned segment
    Segment segment5 = store.put(buildSegment(
      chain1,
      4,
      SegmentState.PLANNED,
      "C",
      8,
      0.8f,
      120.0f,
      "chain-1-waveform-12345.wav"
    ));

    assertThrows(FabricationFatalException.class, () ->
      fabricatorFactory.fabricate(sourceMaterial, segment5.getId(), 48000.0f, 2, null));
  }

}
