// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.detail_hook;

import io.xj.engine.FabricationContentOneFixtures;
import io.xj.engine.FabricationContentTwoFixtures;
import io.xj.engine.FabricationException;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.FabricationEntityStore;
import io.xj.engine.fabricator.FabricationEntityStoreImpl;
import io.xj.engine.fabricator.Fabricator;
import io.xj.engine.fabricator.FabricatorFactory;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.InstrumentMode;
import io.xj.model.enums.InstrumentState;
import io.xj.model.enums.InstrumentType;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.FabricationContentTwoFixtures.buildChain;
import static io.xj.engine.FabricationContentTwoFixtures.buildSegmentChoice;

@ExtendWith(MockitoExtension.class)
public class CraftHookProgramVoiceNextMainTest {
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  FabricationContentTwoFixtures fake;
  Chain chain1;
  Segment segment4;
  FabricationEntityStore store;
  InstrumentAudio audioKick;
  InstrumentAudio audioSnare;
  HubContent sourceMaterial;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    craftFactory = new CraftFactoryImpl();
    HubTopology.buildHubApiTopology(entityFactory);
    FabricationTopology.buildFabricationTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    store = new FabricationEntityStoreImpl(entityFactory);
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new FabricationContentTwoFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      Stream.concat(fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()),
      customFixtures().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(buildChain(
      fake.project1,
      fake.template1,
      "Test Print #1",
      ChainType.PRODUCTION,
      ChainState.FABRICATE
    ));
    store.put(FabricationContentTwoFixtures.buildSegment(
      chain1,
      0,
      SegmentState.CRAFTED,
      "D major",
      64,
      0.73f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892"
    ));
    store.put(FabricationContentTwoFixtures.buildSegment(
      chain1,
      SegmentType.CONTINUE,
      1,
      1,
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
  }

  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  Collection<Object> customFixtures() {
    Collection<Object> entities = new ArrayList<>();

    // Instrument "808"
    Instrument instrument1 = EntityUtils.add(entities,
      FabricationContentOneFixtures.buildInstrument(fake.library2, InstrumentType.Hook, InstrumentMode.Event, InstrumentState.Published, "Bongo Loop"));
    EntityUtils.add(entities, FabricationContentOneFixtures.buildInstrumentMeme(instrument1, "heavy"));
    //
    audioKick = EntityUtils.add(entities, FabricationContentOneFixtures.buildInstrumentAudio(
      instrument1,
      "Kick",
      "19801735098q47895897895782138975898.wav",
      0.01f,
      2.123f,
      120.0f,
      0.6f,
      "KICK",
      "Eb",
      1.0f));
    //
    audioSnare = EntityUtils.add(entities, FabricationContentOneFixtures.buildInstrumentAudio(
      instrument1,
      "Snare",
      "a1g9f8u0k1v7f3e59o7j5e8s98.wav",
      0.01f,
      1.5f,
      120.0f,
      0.6f,
      "SNARE",
      "Ab",
      1.0f));

    return entities;
  }

  @Test
  public void craftHookVoiceNextMain() throws Exception {
    insertSegments3and4();
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);

    craftFactory.detail(fabricator).doWork();
  }

  /**
   Insert fixture segments 3 and 4, including the hook choice for segment 3 only if specified
   */
  void insertSegments3and4() throws FabricationException {
    // segment just crafted
    // Testing entities for reference
    Segment segment3 = store.put(FabricationContentTwoFixtures.buildSegment(
      chain1,
      SegmentType.CONTINUE,
      2,
      2,
      SegmentState.CRAFTED,
      "F Major",
      64,
      0.30f,
      120.0f,
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
      fake.program15,
      fake.program15_sequence1_binding0));

    // segment crafting
    segment4 = store.put(FabricationContentTwoFixtures.buildSegment(
      chain1,
      SegmentType.NEXT_MAIN,
      0,
      3,
      SegmentState.CRAFTING,
      "G minor",
      16,
      0.45f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(
      segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence1_binding0));
    store.put(buildSegmentChoice(
      segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program15,
      fake.program15_sequence0_binding0));
    for (String memeName : List.of("Regret", "Sky", "Hindsight", "Tropical"))
      store.put(FabricationContentTwoFixtures.buildSegmentMeme(segment4, memeName));

    store.put(FabricationContentTwoFixtures.buildSegmentChord(segment4, 0.0f, "G minor"));
    store.put(FabricationContentTwoFixtures.buildSegmentChord(segment4, 8.0f, "Ab minor"));
  }

}
