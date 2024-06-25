// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.beat;

import io.xj.engine.ContentFixtures;
import io.xj.engine.SegmentFixtures;
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
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.model.ContentEntityStore;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.Instrument::Mode;
import io.xj.model.enums.Instrument::State;
import io.xj.model.enums.Instrument::Type;
import io.xj.model.enums.Program::State;
import io.xj.model.enums.Program::Type;
import io.xj.model.json.JsonProvider;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramVoice;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.engine.SegmentFixtures::buildSegmentChoice;
import static org.junit.jupiter.api.Assertions.ASSERT_EQ;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 Beat fabrication composited of layered Patterns https://github.com/xjmusic/xjmusic/issues/267
 */
@ExtendWith(MockitoExtension.class)
public class CraftBeat_LayeredVoicesTest {
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory * fabricatorFactory = nullptr;
  ContentEntityStore * sourceMaterial = nullptr;
  InstrumentAudio audioHihat;
  InstrumentAudio audioKick;
  InstrumentAudio audioSnare;
  SegmentEntityStore *store = nullptr;
  ContentFixtures *fake = nullptr;
  Program program42;
  Segment segment4;

  void SetUp() override {


    craftFactory = new CraftFactory();



    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore();
      fake->setupFixtureB1().stream().filter(entity -> !EntityUtils.isSame(entity, fake->program35) && !EntityUtils.isChild(entity, fake->program35)),
      setupCustomFixtures();


    // Chain "Test Print #1" has 5 total segments
    const auto chain1 = store->put(SegmentFixtures::buildChain(fake->project1, "Test Print #1", Chain::Type::Production, Chain::State::Fabricate, fake->template1, ""));
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
      1,
      Segment::State::Crafting,
      "Db minor",
      64,
      0.85f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));

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
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, Program::Type::Macro, fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, Program::Type::Main, fake->program5_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, program42));

    // segment crafting
    segment4 = store->put(SegmentFixtures::buildSegment(
      chain1,
      Segment::Type::Continue,
      3,
      3,
      Segment::State::Crafting,
      "D Major",
      16,
      0.45f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(segment4, Program::Type::Macro, fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(segment4, Program::Type::Main, fake->program5_sequence1_binding0));

    for (std::string memeName : std::set<std::string>({"Cozy", "Classic", "Outlook", "Rosy"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment4, memeName));

    store->put(SegmentFixtures::buildSegmentChord(segment4, 0.0f, "A minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment4, 8.0f, "D Major"));
  }


  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  void setupCustomFixtures() const {


    // Instrument "808"
    Instrument instrument1 = sourceMaterial->put(ContentFixtures::buildInstrument(fake->library2, Instrument::Type::Drum, Instrument::Mode::Event, Instrument::State::Published, "808 Drums"));
    sourceMaterial->put(ContentFixtures::buildMeme(instrument1, "heavy"));
    //
    audioKick = sourceMaterial->put(ContentFixtures::buildAudio(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.6f, "KICK", "Eb", 1.0f));
    //
    audioSnare = sourceMaterial->put(ContentFixtures::buildAudio(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01f, 1.5f, 120.0f, 0.6f, "SNARE", "Ab", 1.0f));
    //
    audioHihat = sourceMaterial->put(ContentFixtures::buildAudio(instrument1, "Hihat", "iop0803k1k2l3h5a3s2d3f4g.wav", 0.01f, 1.5f, 120.0f, 0.6f, "HIHAT", "Ab", 1.0f));

    // A basic beat from scratch with layered voices
    program42 = sourceMaterial->put(ContentFixtures::buildProgram(fake->library2, Program::Type::Beat, Program::State::Published, "Basic Beat", "C", 121f));
    sourceMaterial->put(ContentFixtures::buildMeme(program42, "Basic"));
    ProgramVoice program42_locomotion = sourceMaterial->put(ContentFixtures::buildVoice(program42, Instrument::Type::Drum, "Locomotion"));
    ProgramVoice program42_kickSnare = sourceMaterial->put(ContentFixtures::buildVoice(program42, Instrument::Type::Drum, "BoomBap"));
    auto sequence35a = sourceMaterial->put(ContentFixtures::buildSequence(program42, 16, "Base", 0.5f, "C"));
    //
    auto pattern35a1 = sourceMaterial->put(ContentFixtures::buildPattern(sequence35a, program42_locomotion, 1, "Hi-hat"));
    auto trackHihat = sourceMaterial->put(ContentFixtures::buildTrack(program42_locomotion, "HIHAT"));
    sourceMaterial->put(ContentFixtures::buildEvent(pattern35a1, trackHihat, 0.0f, 1.0f, "C2", 1.0f));
    sourceMaterial->put(ContentFixtures::buildEvent(pattern35a1, trackHihat, 0.25f, 1.0f, "G5", 0.4f));
    sourceMaterial->put(ContentFixtures::buildEvent(pattern35a1, trackHihat, 0.5f, 1.0f, "C2", 0.6f));
    sourceMaterial->put(ContentFixtures::buildEvent(pattern35a1, trackHihat, 0.75f, 1.0f, "C2", 0.3f));
    //
    auto pattern35a2 = sourceMaterial->put(ContentFixtures::buildPattern(sequence35a, program42_kickSnare, 4, "Kick/Snare"));
    auto trackKick = sourceMaterial->put(ContentFixtures::buildTrack(program42_kickSnare, "KICK"));
    auto trackSnare = sourceMaterial->put(ContentFixtures::buildTrack(program42_kickSnare, "SNARE"));
    sourceMaterial->put(ContentFixtures::buildEvent(pattern35a2, trackKick, 0.0f, 1.0f, "B5", 0.9f));
    sourceMaterial->put(ContentFixtures::buildEvent(pattern35a2, trackSnare, 1.0f, 1.0f, "D2", 1.0f));
    sourceMaterial->put(ContentFixtures::buildEvent(pattern35a2, trackKick, 2.5f, 1.0f, "E4", 0.7f));
    sourceMaterial->put(ContentFixtures::buildEvent(pattern35a2, trackSnare, 3.0f, 1.0f, "c3", 0.5f));

    return entities;
  }

  void TearDown() override {

  }

  @Test
  public void craftBeatVoiceContinue()  {
    auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, std::nullopt);

    craftFactory->beat(fabricator).doWork();

    auto result = store->readSegment(segment4->id).value();
    assertFalse(store->readAllSegmentChoices(result->id).empty());

    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedHihat = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator->getPicks();
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(audioKick->id))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(audioSnare->id))
        pickedSnare++;
      if (pick.getInstrumentAudioId().equals(audioHihat->id))
        pickedHihat++;
    }
    ASSERT_EQ(8, pickedKick);
    ASSERT_EQ(8, pickedSnare);
    ASSERT_EQ(64, pickedHihat);
  }
}
