// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.detail;

import io.xj.engine.fabricator->SegmentEntityStoreImpl;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.Instrument::Type;
import io.xj.model.enums.Program::Type;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator->Fabricator;
import io.xj.engine.fabricator->FabricatorFactory;
import io.xj.engine.fabricator->FabricatorFactoryImpl;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.pojos.SegmentChordVoicing;
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

import static io.xj.engine.SegmentFixtures::buildChain;
import static io.xj.engine.SegmentFixtures::buildSegment;
import static io.xj.engine.SegmentFixtures::buildSegmentChoice;
import static org.junit.jupiter.api.Assertions.ASSERT_EQ;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
public class CraftDetailInitialTest {
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory * fabricatorFactory = nullptr;
  ContentEntityStore * sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  Segment segment6;

  void SetUp() override {


    craftFactory = new CraftFactory();



    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    auto fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore(Stream.concat(
      Stream.concat(
        Stream.concat(fake->setupFixtureB1().stream(),
          fake->setupFixtureB2().stream()),
        fake->setupFixtureB3().stream()),
      fake->setupFixtureB4_DetailBass().stream()
    ).collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    auto chain2 = store->put(buildChain(
      fake->project1,
      fake->template1,
      "Print #2",
      Chain::Type::Production,
      Chain::State::Fabricate
    ));

    // segment crafting
    segment6 = store->put(SegmentFixtures::buildSegment(
      chain2,
      Segment::Type::Initial,
      0,
      0,
      Segment::State::Crafting,
      "C minor",
      16,
      0.55f,
      130.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment6,
      SegmentChoice::DELTA_UNLIMITED,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program4,
      fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
      segment6,
      SegmentChoice::DELTA_UNLIMITED,
      SegmentChoice::DELTA_UNLIMITED,
      fake->program5,
      fake->program5_sequence0_binding0));
    for (std::string memeName : List.of("Special", "Wild", "Pessimism", "Outlook"))
      store->put(SegmentFixtures::buildSegmentMeme(segment6, memeName));
    SegmentChord chord0 = store->put(SegmentFixtures::buildSegmentChord(segment6, 0.0f, "C minor"));
    store->put(SegmentFixtures::buildSegmentChordVoicing(chord0, Instrument::Type::Bass, "C2, Eb2, G2"));
    SegmentChord chord1 = store->put(SegmentFixtures::buildSegmentChord(segment6, 8.0f, "Db minor"));
    store->put(SegmentFixtures::buildSegmentChordVoicing(chord1, Instrument::Type::Bass, "Db2, E2, Ab2"));
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void craftDetailInitial()  {
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment6->id, 48000.0f, 2, null);

    craftFactory->detail(fabricator).doWork();

    // assert choice of detail-type sequence
    Collection<SegmentChoice> choices = store->readAll(segment6->id, SegmentChoice.class);
    assertNotNull(SegmentUtils::findFirstOfType(choices, Program::Type::Detail));

    // Detail Craft v1 -- segment chords voicings belong to chords and segments https://github.com/xjmusic/xjmusic/issues/284
    Collection<SegmentChordVoicing> voicings = store->readAll(segment6->id, SegmentChordVoicing.class);
    ASSERT_EQ(2, voicings.size());
  }
}
