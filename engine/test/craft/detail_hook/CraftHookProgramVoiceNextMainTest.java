// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.detail_hook;

import io.xj.engine.ContentFixtures;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationException;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator->SegmentEntityStore;
import io.xj.engine.fabricator->SegmentEntityStoreImpl;
import io.xj.engine.fabricator->Fabricator;
import io.xj.engine.fabricator->FabricatorFactory;
import io.xj.engine.fabricator->FabricatorFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.Instrument::Mode;
import io.xj.model.enums.Instrument::State;
import io.xj.model.enums.Instrument::Type;
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

import static io.xj.engine.SegmentFixtures::buildChain;
import static io.xj.engine.SegmentFixtures::buildSegmentChoice;

@ExtendWith(MockitoExtension.class)
public class CraftHookProgramVoiceNextMainTest {
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory * fabricatorFactory = nullptr;
  ContentFixtures *fake = nullptr;
  Chain chain1;
  Segment segment4;
  SegmentEntityStore *store = nullptr;
  InstrumentAudio audioKick;
  InstrumentAudio audioSnare;
  ContentEntityStore * sourceMaterial = nullptr;

  void SetUp() override {


    craftFactory = new CraftFactory();



    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore(Stream.concat(
      Stream.concat(fake->setupFixtureB1().stream(),
        fake->setupFixtureB2().stream()),
      customFixtures().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store->put(SegmentFixtures::buildChain(
      fake->project1,
      fake->template1,
      "Test Print #1",
      Chain::Type::Production,
      Chain::State::Fabricate
    ));
    store->put(SegmentFixtures::buildSegment(
      chain1,
      0,
      Segment::State::Crafted,
      "D major",
      64,
      0.73f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892"
    ));
    store->put(SegmentFixtures::buildSegment(
      chain1,
      Segment::Type::Continue,
      1,
      1,
      Segment::State::Crafting,
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
      ContentFixtures::buildInstrument(fake->library2, Instrument::Type::Hook, Instrument::Mode::Event, Instrument::State::Published, "Bongo Loop"));
    EntityUtils.add(entities, ContentFixtures::buildInstrumentMeme(instrument1, "heavy"));
    //
    audioKick = EntityUtils.add(entities, ContentFixtures::buildInstrumentAudio(
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
    audioSnare = EntityUtils.add(entities, ContentFixtures::buildInstrumentAudio(
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
  public void craftHookVoiceNextMain()  {
    insertSegments3and4();
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, null);

    craftFactory->detail(fabricator).doWork();
  }

  /**
   Insert fixture segments 3 and 4, including the hook choice for segment 3 only if specified
   */
  void insertSegments3and4() throws FabricationException {
    // segment just crafted
    // Testing entities for reference
    const auto segment3 = store->put(SegmentFixtures::buildSegment(
      chain1,
      Segment::Type::Continue,
      2,
      2,
      Segment::State::Crafted,
      "F Major",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892",
      true));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment3,
      SegmentChoice::DELTA_UNLIMITED,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program4,
      fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment3,
      SegmentChoice::DELTA_UNLIMITED,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program15,
      fake->program15_sequence1_binding0));

    // segment crafting
    segment4 = store->put(SegmentFixtures::buildSegment(
      chain1,
      Segment::Type::NextMain,
      0,
      3,
      Segment::State::Crafting,
      "G minor",
      16,
      0.45f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment4,
      SegmentChoice::DELTA_UNLIMITED,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program4,
      fake->program4_sequence1_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment4,
      SegmentChoice::DELTA_UNLIMITED,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program15,
      fake->program15_sequence0_binding0));
    for (std::string memeName : List.of("Regret", "Sky", "Hindsight", "Tropical"))
      store->put(SegmentFixtures::buildSegmentMeme(segment4, memeName));

    store->put(SegmentFixtures::buildSegmentChord(segment4, 0.0f, "G minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment4, 8.0f, "Ab minor"));
  }

}
