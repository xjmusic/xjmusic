// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.detail;

import io.xj.hub.HubContent;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.nexus.entity.EntityFactoryImpl;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.nexus.NexusIntegrationTestingFixtures.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class CraftDetailContinueTest {
  @Mock
  public HubClient hubClient;
  Chain chain1;
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
      Stream.concat(fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()),
      fake.setupFixtureB4_DetailBass().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" is fabricating segments
    chain1 = store.put(buildChain(
      fake.account1,
      fake.template1,
      "Test Print #1",
      ChainType.PRODUCTION,
      ChainState.FABRICATE
    ));
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
      1,
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      true));
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void craftDetailContinue() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4, 48000.0, 2);

    craftFactory.detail(fabricator).doWork();
    // assert choice of detail-type sequence
    Collection<SegmentChoice> segmentChoices =
      store.getAll(segment4.getId(), SegmentChoice.class);
    assertNotNull(SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Detail));
  }

  @Test
  public void craftDetailContinue_okEvenWithoutPreviousSegmentDetailChoice() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4, 48000.0, 2);
    craftFactory.detail(fabricator).doWork();

    // assert choice of detail-type sequence
    Collection<SegmentChoice> segmentChoices =
      store.getAll(segment4.getId(), SegmentChoice.class);
    assertNotNull(SegmentUtils.findFirstOfType(segmentChoices, ProgramType.Detail));
  }

  /**
   Insert fixture segments 3 and 4, including the detail choice for segment 3 only if specified

   @param excludeDetailChoiceForSegment3 if desired for the purpose of this test
   */
  void insertSegments3and4(boolean excludeDetailChoiceForSegment3) throws Exception {
    // segment just crafted
    Segment segment3 = store.put(buildSegment(chain1,
      SegmentType.CONTINUE,
      2,
      0,
      SegmentState.CRAFTED,
      "F Major",
      64,
      0.30,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      true));
    store.put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence0_binding0));
    if (!excludeDetailChoiceForSegment3)
      store.put(buildSegmentChoice(
        segment3,
        Segment.DELTA_UNLIMITED,
        Segment.DELTA_UNLIMITED,
        fake.program10));

    // segment crafting
    segment4 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      3,
      16,
      SegmentState.CRAFTING,
      "D Major",
      16,
      0.45,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      true));
    store.put(buildSegmentChoice(
      segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence1_binding0));
    for (String memeName : List.of("Cozy", "Classic", "Outlook", "Rosy"))
      store.put(buildSegmentMeme(segment4, memeName));
    SegmentChord chord0 = store.put(buildSegmentChord(segment4, 0.0, "A minor"));
    store.put(buildSegmentChordVoicing(chord0, InstrumentType.Bass, "A2, C3, E3"));
    SegmentChord chord1 = store.put(buildSegmentChord(segment4, 8.0, "D major"));
    store.put(buildSegmentChordVoicing(chord1, InstrumentType.Bass, "D2, F#2, A2"));
  }
}
