// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import io.xj.hub.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.entity.EntityUtils;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.json.JsonProviderImpl;
import io.xj.nexus.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.*;
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
import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.hub.util.ValueUtils.SECONDS_PER_MINUTE;
import static io.xj.nexus.NexusIntegrationTestingFixtures.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CraftFoundationContinueTest {
  @Mock
  public HubClient hubClient;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  NexusEntityStore store;
  NexusIntegrationTestingFixtures fake;
  Segment segment4;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    craftFactory = new CraftFactoryImpl();
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    store = new NexusEntityStoreImpl(entityFactory);
    SegmentManager segmentManager = new SegmentManagerImpl(store);
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
      "chains-1-segments-9f7s89d8a7892"
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
    store.put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence1_binding0));
    store.put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence0_binding0));

    // Chain "Test Print #1" has a planned segment
    segment4 = store.put(buildSegment(
      chain1,
      3,
      SegmentState.PLANNED,
      "C",
      4,
      1.0f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892"
    ));
  }

  /**
   persist Segment basis as JSON, then read basis JSON during fabrication of any segment that continues a main sequence https://www.pivotaltracker.com/story/show/162361525
   */
  @Test
  public void craftFoundationContinue() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4, 48000.0f, 2);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = store.getSegment(segment4.getId()).orElseThrow();
    assertEquals(SegmentType.CONTINUE, result.getType());
    assertEquals(32 * MICROS_PER_SECOND * SECONDS_PER_MINUTE / 140, (long) Objects.requireNonNull(result.getDurationMicros()));
    assertEquals(Integer.valueOf(32), result.getTotal());
    assertEquals(0.14, result.getDensity(), 0.001);
    assertEquals("G -", result.getKey());
    assertEquals(140, result.getTempo(), 0.001);
    assertEquals(SegmentType.CONTINUE, result.getType());
    // assert memes
    assertSameItems(
      List.of("OUTLOOK", "TROPICAL", "COZY", "WILD", "PESSIMISM"),
      EntityUtils.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
    // assert chords
    assertSameItems(List.of("Bb -", "C"),
      EntityUtils.namesOf(store.getAll(result.getId(), SegmentChord.class)));
    // assert choices
    Collection<SegmentChoice> segmentChoices =
      store.getAll(result.getId(), SegmentChoice.class);
    // assert macro choice
    SegmentChoice macroChoice = SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Macro);
    assertEquals(fake.program4_sequence1_binding0.getId(), macroChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    // assert main choice
    SegmentChoice mainChoice = SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Main);
    assertEquals(fake.program5_sequence1_binding0.getId(), mainChoice.getProgramSequenceBindingId()); // next main sequence binding in same program as previous sequence
    assertEquals(Integer.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }

}
