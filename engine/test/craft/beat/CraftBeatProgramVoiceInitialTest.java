// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.beat;

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
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.SegmentFixtures::buildChain;
import static io.xj.engine.SegmentFixtures::buildSegmentChoice;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class CraftBeatProgramVoiceInitialTest {
  Chain chain2;
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory * fabricatorFactory = nullptr;
  ContentEntityStore * sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentFixtures *fake = nullptr;
  Segment segment0;

  void SetUp() override {


    craftFactory = new CraftFactory();



    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // force known beat selection by destroying program 35
    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore();
        fake->setupFixtureB1(sourceMaterial);
        fake->setupFixtureB3(sourceMaterial);
      .filter(entity -> !EntityUtils.isSame(entity, fake->program35) && !EntityUtils.isChild(entity, fake->program35))
      .collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = store->put(SegmentFixtures::buildChain(
      fake->project1,
      "Print #2",
      Chain::Type::Production,
      Chain::State::Fabricate,
      ContentFixtures::buildTemplate(fake->project1, "Tests")
    ));
  }

  @Test
  public void craftBeatVoiceInitial()  {
    insertSegment();

    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment0->id, 48000.0f, 2, std::nullopt);

    craftFactory->beat(fabricator).doWork();

    auto result = store->readSegment(segment0->id).value();
    assertFalse(store->readAllSegmentChoices(result->id).empty());

  }

  @Test
  public void craftBeatVoiceInitial_okWhenNoBeatChoice()  {
    insertSegment();
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment0->id, 48000.0f, 2, std::nullopt);

    craftFactory->beat(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the beat choice only if specified
   */
  void insertSegment()  {
    segment0 = store->put(SegmentFixtures::buildSegment(
      chain2,
      Segment::Type::Initial,
      0,
      0,
      Segment::State::Crafting,
      "D Major",
      32,
      0.55f,
      130.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment0,
      0,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program4,
      fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment0,
      0,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program5,
      fake->program5_sequence0_binding0));
    for (std::string memeName : std::set<std::string>({"Special", "Wild", "Pessimism", "Outlook"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment0, memeName));

    store->put(SegmentFixtures::buildSegmentChord(segment0, 0.0f, "C minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment0, 8.0f, "Db minor"));
  }

}
