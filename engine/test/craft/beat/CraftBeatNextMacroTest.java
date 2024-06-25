// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.beat;

import io.xj.engine.fabricator->SegmentEntityStoreImpl;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.Program::Type;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.FabricationException;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator->Fabricator;
import io.xj.engine.fabricator->FabricatorFactory;
import io.xj.engine.fabricator->FabricatorFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.engine.fabricator->SegmentEntityStore;
import io.xj.engine.fabricator->SegmentUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.SegmentFixtures::buildSegment;
import static io.xj.engine.SegmentFixtures::buildSegmentChoice;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class CraftBeatNextMacroTest {
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory * fabricatorFactory = nullptr;
  ContentFixtures *fake = nullptr;
  Chain chain1;
  Segment segment4;
  SegmentEntityStore *store = nullptr;
  ContentEntityStore * sourceMaterial = nullptr;

  void SetUp() override {


    craftFactory = new CraftFactory();



    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore();
      fake->setupFixtureB1(sourceMaterial);
        fake->setupFixtureB2(sourceMaterial);
      fake->setupFixtureB3(sourceMaterial);


    // Chain "Test Print #1" has 5 total segments
    chain1 = store->put(SegmentFixtures::buildChain(&fake->project1, "Test Print #1", Chain::Type::Production, Chain::State::Fabricate, &fake->template1, ""));
    store->put(SegmentFixtures::buildSegment(
      chain1,
      Segment::Type::Initial,
      0,
      0,
      Segment::State::Crafted,
      "D major",
      64,
      0.73f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892",
      true));
    store->put(SegmentFixtures::buildSegment(
      chain1,
      Segment::Type::Continue,
      1,
      0,
      Segment::State::Crafting,
      "Db minor",
      64,
      0.85f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
  }

  void TearDown() override {

  }

  @Test
  public void craftBeatNextMacro()  {
    insertSegments3and4(true);
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, std::nullopt);

    craftFactory->beat(fabricator).doWork();

    // assert choice of beat-type sequence
    auto segmentChoices =
      store->readAll(segment4->id, SegmentChoice.class);
    assertNotNull(SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Beat));
  }

  @Test
  public void craftBeatNextMacro_okEvenWithoutPreviousSegmentBeatChoice()  {
    insertSegments3and4(false);
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, std::nullopt);

    craftFactory->beat(fabricator).doWork();

    // assert choice of beat-type sequence
    auto segmentChoices =
      store->readAll(segment4->id, SegmentChoice.class);
    assertNotNull(SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Beat));
  }

  /**
   Insert fixture segments 3 and 4, including the beat choice for segment 3 only if specified

   @param excludeBeatChoiceForSegment3 if desired for the purpose of this test
   */
  void insertSegments3and4(bool excludeBeatChoiceForSegment3)  {
    // Chain "Test Print #1" has this segment that was just crafted
    const auto segment3 = store->put(SegmentFixtures::buildSegment(
      chain1,
      Segment::Type::Continue,
      2,
      2,
      Segment::State::Crafted,
      "Ab minor",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment3,
      SegmentChoice::DELTA_UNLIMITED,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program4,
      fake->program4_sequence2_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment3,
      SegmentChoice::DELTA_UNLIMITED,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program5,
      fake->program5_sequence1_binding0));
    if (!excludeBeatChoiceForSegment3)
      store->put(SegmentFixtures::buildSegmentChoice(
        segment3,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        fake->program35));

    // Chain "Test Print #1" has a segment in crafting state - Foundation is complete
    segment4 = store->put(SegmentFixtures::buildSegment(
      chain1,
      Segment::Type::NextMacro,
      3,
      0,
      Segment::State::Crafting,
      "F minor",
      16,
      0.45f,
      125.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment4,
      SegmentChoice::DELTA_UNLIMITED,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program4,
      fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment4,
      SegmentChoice::DELTA_UNLIMITED,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program15,
      fake->program15_sequence0_binding0));
    for (std::string memeName : std::set<std::string>({"Hindsight", "Chunky", "Regret", "Tangy"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment4, memeName));
    store->put(SegmentFixtures::buildSegmentChord(segment4, 0.0f, "F minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment4, 8.0f, "Gb minor"));
  }


}
