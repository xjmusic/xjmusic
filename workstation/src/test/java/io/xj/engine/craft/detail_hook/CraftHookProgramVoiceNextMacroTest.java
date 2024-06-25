// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.craft.detail_hook;

import io.xj.engine.ContentFixtures;
import io.xj.engine.FabricationException;
import io.xj.engine.FabricationTopology;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.*;
import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.*;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.pojos.Segment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.SegmentFixtures.buildSegmentChoice;

@ExtendWith(MockitoExtension.class)
public class CraftHookProgramVoiceNextMacroTest {
  Chain chain1;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  InstrumentAudio audioKick;
  InstrumentAudio audioSnare;
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
      customFixtures().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(SegmentFixtures.buildChain(fake.project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
    store.put(SegmentFixtures.buildSegment(
      chain1,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.CRAFTED,
      "D major",
      64,
      0.73f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892",
      true));
    store.put(SegmentFixtures.buildSegment(
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
    Instrument instrument1 = EntityUtils.add(entities, ContentFixtures.buildInstrument(fake.library2, InstrumentType.Hook, InstrumentMode.Event, InstrumentState.Published, "Bongo Loop"));
    EntityUtils.add(entities, ContentFixtures.buildMeme(instrument1, "heavy"));
    //
    audioKick = EntityUtils.add(entities, ContentFixtures.buildAudio(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.6f, "KICK", "Eb", 1.0f));
    //
    audioSnare = EntityUtils.add(entities, ContentFixtures.buildAudio(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01f, 1.5f, 120.0f, 0.6f, "SNARE", "Ab", 1.0f));

    return entities;
  }

  /**
   Insert fixture segments 3 and 4, including the hook choice for segment 3 only if specified

   @param excludeHookChoiceForSegment3 if desired for the purpose of this test
   */
  void insertSegments3and4(boolean excludeHookChoiceForSegment3) throws FabricationException {
    // Chain "Test Print #1" has this segment that was just crafted
    Segment segment3 = store.put(SegmentFixtures.buildSegment(
      chain1,
      SegmentType.CONTINUE,
      2,
      2,
      SegmentState.CRAFTED,
      "Ab minor",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence2_binding0));
    store.put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence1_binding0));
    if (!excludeHookChoiceForSegment3)
      store.put(buildSegmentChoice(
        segment3,
        Segment.DELTA_UNLIMITED,
        Segment.DELTA_UNLIMITED,
        fake.program35,
        InstrumentType.Hook,
        InstrumentMode.Loop));

    // Chain "Test Print #1" has a segment in crafting state - Foundation is complete
    segment4 = store.put(SegmentFixtures.buildSegment(
      chain1,
      SegmentType.NEXT_MACRO,
      3,
      0,
      SegmentState.CRAFTING,
      "F minor",
      16,
      0.45f,
      125.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(
      segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program3,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program15,
      fake.program15_sequence0_binding0));
    for (String memeName : List.of("Hindsight", "Chunky", "Regret", "Tangy"))
      store.put(SegmentFixtures.buildSegmentMeme(segment4, memeName));
    store.put(SegmentFixtures.buildSegmentChord(segment4, 0.0f, "F minor"));
    store.put(SegmentFixtures.buildSegmentChord(segment4, 8.0f, "Gb minor"));
  }

  @Test
  public void craftHookVoiceNextMacro() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);

    craftFactory.detail(fabricator).doWork();
  }

}
