// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.detail_perc_loop;

import io.xj.engine.FabricationException;
import io.xj.engine.FabricationTopology;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.*;
import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.pojos.Segment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.SegmentFixtures.buildSegmentChoice;

@ExtendWith(MockitoExtension.class)
public class CraftPercLoopNextMainTest {
  Chain chain1;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  SegmentEntityStore store;
  SegmentFixtures fake;
  Segment segment4;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    craftFactory = new CraftFactoryImpl();
    HubTopology.buildHubApiTopology(entityFactory);
    FabricationTopology.buildFabricationTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    store = new SegmentEntityStoreImpl(entityFactory);
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new SegmentFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      Stream.concat(fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()),
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(SegmentFixtures.buildChain(fake.project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
    store.put(SegmentFixtures.buildSegment(
      chain1,
      0,
      SegmentState.CRAFTED,
      "D major",
      64,
      0.73f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892"
    ));
    store.put(SegmentFixtures.buildSegment(
      chain1,
      1,
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));
  }

  @AfterEach
  public void tearDown() {

  }

  /**
   Insert fixture segments 3 and 4, including the percLoop choice for segment 3 only if specified
   */
  void insertSegments3and4() throws FabricationException {
    // segment just crafted
    // Testing entities for reference
    Segment segment3 = store.put(SegmentFixtures.buildSegment(
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
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program15,
      fake.program15_sequence1_binding0));

    // segment crafting
    segment4 = store.put(SegmentFixtures.buildSegment(
      chain1,
      SegmentType.NEXT_MAIN,
      3,
      0,
      SegmentState.CRAFTING,
      "G minor",
      16,
      0.45f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence1_binding0));
    store.put(buildSegmentChoice(segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program15,
      fake.program15_sequence0_binding0));
    for (String memeName : List.of("Regret", "Sky", "Hindsight", "Tropical")) {
      store.put(SegmentFixtures.buildSegmentMeme(segment4, memeName));
    }
    store.put(SegmentFixtures.buildSegmentChord(segment4, 0.0f, "G minor"));
    store.put(SegmentFixtures.buildSegmentChord(segment4, 8.0f, "Ab minor"));
  }

  @Test
  public void craftPercLoopNextMain_okEvenWithoutPreviousSegmentPercLoopChoice() throws Exception {
    insertSegments3and4();
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);

    craftFactory.detail(fabricator).doWork();
  }

}
