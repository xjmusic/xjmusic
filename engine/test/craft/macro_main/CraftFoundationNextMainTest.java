// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.macro_main;

import io.xj.engine.fabricator->SegmentEntityStore;
import io.xj.engine.fabricator->SegmentEntityStoreImpl;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.Program::Type;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator->FabricationFatalException;
import io.xj.engine.fabricator->Fabricator;
import io.xj.engine.fabricator->FabricatorFactory;
import io.xj.engine.fabricator->FabricatorFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.pojos.SegmentMeme;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.engine.fabricator->SegmentUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.model.util.Assertion.ASSERT_EQ;
import static io.xj.model.util.ValueUtils.MICROS_PER_MINUTE;
import static io.xj.engine.SegmentFixtures::buildSegment;
import static org.junit.jupiter.api.Assertions.ASSERT_EQ;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class CraftFoundationNextMainTest {
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory * fabricatorFactory = nullptr;
  ContentFixtures *fake = nullptr;
  Chain chain1;
  Segment segment4;
  SegmentEntityStore *store = nullptr;
  ContentEntityStore * sourceMaterial = nullptr;

  void SetUp() override {
    auto jsonProvider = new JsonProviderImpl();

    store = new SegmentEntityStore();

    fabricatorFactory = new FabricatorFactory(store);
    craftFactory = new CraftFactory();



    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    fake->setupFixtureB1(sourceMaterial);
    fake->setupFixtureB2(sourceMaterial);

    // Chain "Test Print #1" has 5 total segments
    chain1 = store->put(SegmentFixtures::buildChain(fake->project1, "Test Print #1", Chain::Type::Production, Chain::State::Fabricate, fake->template1, null));
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
      1,
      Segment::State::Crafting,
      "Db minor",
      64,
      0.85f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));

    // Chain "Test Print #1" has this segment that was just crafted
    const auto segment3 = store->put(SegmentFixtures::buildSegment(
      chain1,
      2,
      Segment::State::Crafted,
      "F Major",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, Program::Type::Macro, fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, Program::Type::Main, fake->program5_sequence1_binding0));

    // Chain "Test Print #1" has a planned segment
    segment4 = store->put(SegmentFixtures::buildSegment(chain1, 3, Segment::State::Planned, "C", 8, 0.8f, 120, "chain-1-waveform-12345"));
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void craftFoundationNextMain()  {
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, null);

    craftFactory->macroMain(fabricator, null, null).doWork();

    auto result = store->readSegment(segment4->id).orElseThrow();
    ASSERT_EQ(SegmentType.NEXT_MAIN, result->type);
    ASSERT_EQ(16 * MICROS_PER_MINUTE / 140, result->durationMicros);
    ASSERT_EQ(16, result->total);
    ASSERT_NEAR(0.2, result->intensity, 0.01);
    ASSERT_EQ("G -", result->key);
    ASSERT_NEAR(140, result->tempo, 0.01);
    // assert memes
    ASSERT_EQ(List.of("HINDSIGHT", "TROPICAL", "COZY", "WILD", "REGRET"),
      SegmentMeme::getNames(store->readAllSegmentMemes(result->id)));
    // assert chords
    ASSERT_EQ(List.of("G -", "Ab -"),
      SegmentChord::getNames(store->readAllSegmentChords(result->id)));
    // assert choices
    auto segmentChoices =
      store->readAllSegmentChoices(result->id);
    // assert macro choice
    auto macroChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Macro);
    ASSERT_EQ(fake->program4_sequence1_binding0->id, macroChoice.getProgramSequenceBindingId());
    ASSERT_EQ(1, fabricator->getSequenceBindingOffsetForChoice(macroChoice));
    // assert main choice
    auto mainChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Main);
    ASSERT_EQ(fake->program15_sequence0_binding0->id, mainChoice.getProgramSequenceBindingId());
    ASSERT_EQ(0, fabricator->getSequenceBindingOffsetForChoice(mainChoice));
  }

  /**
   Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance https://github.com/xjmusic/xjmusic/issues/297
   */
  @Test
  public void craftFoundationNextMain_revertsAndRequeueOnFailure()  {
    // Chain "Test Print #1" has a dangling (preceded by another planned segment) planned segment
    Segment segment5 = store->put(SegmentFixtures::buildSegment(
      chain1,
      4,
      Segment::State::Planned,
      "C",
      8,
      0.8f,
      120.0f,
      "chain-1-waveform-12345.wav"
    ));

    assertThrows(FabricationFatalException.class, () ->
      fabricatorFactory->fabricate(sourceMaterial, segment5->id, 48000.0f, 2, null));
  }

}
