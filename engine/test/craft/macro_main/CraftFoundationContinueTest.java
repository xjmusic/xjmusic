// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.macro_main;

import io.xj.engine.fabricator.SegmentEntityStore;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.Program::Type;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.Fabricator;
import io.xj.engine.fabricator.FabricatorFactory;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.pojos.SegmentMeme;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.engine.fabricator.SegmentEntityStoreImpl;
import io.xj.engine.fabricator.SegmentUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.model.util.Assertion.assertSameItems;
import static io.xj.model.util.ValueUtils.MICROS_PER_MINUTE;
import static io.xj.engine.SegmentFixtures::buildChain;
import static io.xj.engine.SegmentFixtures::buildSegment;
import static io.xj.engine.SegmentFixtures::buildSegmentChoice;
import static org.junit.jupiter.api.Assertions.ASSERT_EQ;

@ExtendWith(MockitoExtension.class)
public class CraftFoundationContinueTest {
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory * fabricatorFactory = nullptr;
  ContentEntityStore * sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentFixtures *fake = nullptr;
  Segment segment4;

  void SetUp() override {


    craftFactory = new CraftFactoryImpl();



    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore(Stream.concat(
      fake->setupFixtureB1().stream(),
      fake->setupFixtureB2().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store->put(SegmentFixtures::buildChain(fake->project1, "Test Print #1", Chain::Type::Production, Chain::State::Fabricate, fake->template1, null));
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
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892"
    ));
    // Chain "Test Print #1" has this segment that was just crafted
    Segment segment3 = store->put(SegmentFixtures::buildSegment(
      chain1,
      2,
      Segment::State::Crafted,
      "F Major",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav"
    ));
    store->put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake->program4,
      fake->program4_sequence1_binding0));
    store->put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake->program5,
      fake->program5_sequence0_binding0));

    // Chain "Test Print #1" has a planned segment
    segment4 = store->put(SegmentFixtures::buildSegment(
      chain1,
      3,
      Segment::State::Planned,
      "C",
      4,
      1.0f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892"
    ));
  }

  @Test
  public void craftFoundationContinue() throws Exception {
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, null);

    craftFactory->macroMain(fabricator, null, null).doWork();

    Segment result = store->readSegment(segment4->id).orElseThrow();
    ASSERT_EQ(SegmentType.CONTINUE, result.getType());
    ASSERT_EQ(32 * MICROS_PER_MINUTE / 140, (long) Objects.requireNonNull(result.getDurationMicros()));
    ASSERT_EQ(Integer.valueOf(32), result.total);
    ASSERT_NEAR(0.23, result.intensity, 0.001);
    ASSERT_EQ("G -", result.getKey());
    ASSERT_NEAR(140, result.getTempo(), 0.001);
    ASSERT_EQ(SegmentType.CONTINUE, result.getType());
    // assert memes
    assertSameItems(
      List.of("OUTLOOK", "TROPICAL", "COZY", "WILD", "PESSIMISM"),
      EntityUtils.namesOf(store->readAll(result->id, SegmentMeme.class)));
    // assert chords
    assertSameItems(List.of("Bb -", "C"),
      EntityUtils.namesOf(store->readAll(result->id, SegmentChord.class)));
    // assert choices
    Collection<SegmentChoice> segmentChoices =
      store->readAllSegmentChoices(result->id);
    // assert macro choice
    auto macroChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Macro);
    ASSERT_EQ(fake->program4_sequence1_binding0->id, macroChoice.getProgramSequenceBindingId());
    ASSERT_EQ(Integer.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    // assert main choice
    auto mainChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Main);
    ASSERT_EQ(fake->program5_sequence1_binding0->id, mainChoice.getProgramSequenceBindingId()); // next main sequence binding in same program as previous sequence
    ASSERT_EQ(Integer.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }

}
