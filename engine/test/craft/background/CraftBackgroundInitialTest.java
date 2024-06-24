// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.background;

import io.xj.engine.ContentFixtures;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.SegmentEntityStore;
import io.xj.engine.fabricator.SegmentEntityStoreImpl;
import io.xj.engine.fabricator.Fabricator;
import io.xj.engine.fabricator.FabricatorFactory;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.SegmentFixtures::buildChain;
import static io.xj.engine.SegmentFixtures::buildSegmentChoice;

@ExtendWith(MockitoExtension.class)
public class CraftBackgroundInitialTest {
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory * fabricatorFactory = nullptr;
  ContentEntityStore * sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  Segment segment6;

  void SetUp() override {


    craftFactory = new CraftFactoryImpl();



    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    SegmentFixtures fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore(Stream.concat(
      Stream.concat(fake->setupFixtureB1().stream(),
        fake->setupFixtureB2().stream()),
      fake->setupFixtureB3().stream()
    ).collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    auto chain2 = store->put(buildChain(
      fake->project1,
      "Print #2",
      Chain::Type::Production,
      Chain::State::Fabricate,
      ContentFixtures::buildTemplate(fake->project1, "Test")
    ));

    // segment crafting
    segment6 = store->put(SegmentFixtures::buildSegment(
      chain2,
      Segment::Type::Initial,
      0,
      0,
      SegmentState.CRAFTING,
      "C minor",
      16,
      0.55f,
      130.0f,
      "chains-1-segments-9f7s89d8a7892.wav",
      true));
    store->put(buildSegmentChoice(
      segment6,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake->program4,
      fake->program4_sequence0_binding0));
    store->put(buildSegmentChoice(
      segment6,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake->program5,
      fake->program5_sequence0_binding0));
    for (std::string memeName : List.of("Special", "Wild", "Pessimism", "Outlook"))
      store->put(SegmentFixtures::buildSegmentMeme(segment6, memeName));

    store->put(SegmentFixtures::buildSegmentChord(segment6, 0.0f, "C minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment6, 8.0f, "Db minor"));
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void craftBackgroundInitial() throws Exception {
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment6->id, 48000.0f, 2, null);

    craftFactory->background(fabricator).doWork();

//    // assert choice of background-type sequence
//    Collection<SegmentChoice> segmentChoices =
//      store->getAll(segment6->id, SegmentChoice.class);
//    assertNotNull(Segments.findFirstOfType(segmentChoices, Instrument::Type::Background));
  }
}
