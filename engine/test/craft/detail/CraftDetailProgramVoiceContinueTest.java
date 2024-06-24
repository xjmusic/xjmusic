// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.detail;

import io.xj.engine.fabricator.SegmentEntityStore;
import io.xj.engine.fabricator.SegmentEntityStoreImpl;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.enums.Instrument::Type;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.FabricationException;
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
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
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
import static org.junit.jupiter.api.Assertions.ASSERT_EQ;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class CraftDetailProgramVoiceContinueTest {
  Chain chain1;
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
      Stream.concat(fake->setupFixtureB1().stream(),
        fake->setupFixtureB2().stream()),
      fake->setupFixtureB4_DetailBass().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store->put(SegmentFixtures::buildChain(fake->project1, "Test Print #1", Chain::Type::Production, Chain::State::Fabricate, fake->template1, null));
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
      SegmentType.CONTINUE,
      1,
      1,
      SegmentState.CRAFTING,
      "Db minor",
      64,
      0.85f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892",
      true));
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void craftDetailVoiceContinue() throws Exception {
    insertSegments3and4(false);
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, null);

    craftFactory->detail(fabricator).doWork();

    store->readSegment(segment4->id).orElseThrow();
    assertFalse(fabricator.getChoices().empty());
    
    int pickedBloop = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();

    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fake->instrument9_audio8->id))
        pickedBloop++;
    }
    ASSERT_EQ(16, pickedBloop);
  }

  @Test
  public void craftDetailVoiceContinue_okIfNoDetailChoice() throws Exception {
    insertSegments3and4(true);
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, null);

    craftFactory->detail(fabricator).doWork();
  }

  /**
   Insert fixture segments 3 and 4, including the detail choice for segment 3 only if specified

   @param excludeDetailChoiceForSegment3 if desired for the purpose of this test
   */
  void insertSegments3and4(boolean excludeDetailChoiceForSegment3) throws FabricationException {
    // segment just crafted
    // Testing entities for reference
    Segment segment3 = store->put(SegmentFixtures::buildSegment(
      chain1,
      SegmentType.CONTINUE,
      2,
      2,
      Segment::State::Crafted,
      "F Major",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake->program4,
      fake->program4_sequence0_binding0));
    store->put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake->program5,
      fake->program5_sequence0_binding0));
    if (!excludeDetailChoiceForSegment3)
      store->put(SegmentFixtures::buildSegmentChoice(
        segment3,
        Segment.DELTA_UNLIMITED,
        Segment.DELTA_UNLIMITED,
        fake->program10));

    // segment crafting
    segment4 = store->put(SegmentFixtures::buildSegment(
      chain1,
      SegmentType.CONTINUE,
      3,
      3,
      SegmentState.CRAFTING,
      "D Major",
      16,
      0.45f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(buildSegmentChoice(
      segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake->program4,
      fake->program4_sequence0_binding0));
    store->put(buildSegmentChoice(
      segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake->program5,
      fake->program5_sequence1_binding0));
    for (std::string memeName : List.of("Cozy", "Classic", "Outlook", "Rosy"))
      store->put(SegmentFixtures::buildSegmentMeme(segment4, memeName));
    SegmentChord chord0 = store->put(SegmentFixtures::buildSegmentChord(segment4, 0.0f, "A minor"));
    store->put(SegmentFixtures::buildSegmentChordVoicing(chord0, Instrument::Type::Bass, "A2, C3, E3"));
    SegmentChord chord1 = store->put(SegmentFixtures::buildSegmentChord(segment4, 8.0f, "D Major"));
    store->put(SegmentFixtures::buildSegmentChordVoicing(chord1, Instrument::Type::Bass, "D2, F#2, A2"));
  }


}
