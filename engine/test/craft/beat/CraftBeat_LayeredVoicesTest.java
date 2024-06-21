// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.beat;

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
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.model.HubContent;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 Beat fabrication composited of layered Patterns https://github.com/xjmusic/xjmusic/issues/267
 */
@ExtendWith(MockitoExtension.class)
public class CraftBeat_LayeredVoicesTest {
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  InstrumentAudio audioHihat;
  InstrumentAudio audioKick;
  InstrumentAudio audioSnare;
  SegmentEntityStore store;
  SegmentFixtures fake;
  Program program42;
  Segment segment4;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    auto entityFactory = new EntityFactoryImpl(jsonProvider);
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
      fake.setupFixtureB1().stream().filter(entity -> !EntityUtils.isSame(entity, fake.program35) && !EntityUtils.isChild(entity, fake.program35)),
      customFixtures().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(SegmentFixtures::buildChain(fake.project1, "Test Print #1", Chain::Type::Production, Chain::State::Fabricate, fake.template1, null));
    store.put(SegmentFixtures::buildSegment(
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
    store.put(SegmentFixtures::buildSegment(
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

    // segment just crafted
    // Testing entities for reference
    Segment segment3 = store.put(SegmentFixtures::buildSegment(
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
    store.put(buildSegmentChoice(segment3, Program::Type::Macro, fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(segment3, Program::Type::Main, fake.program5_sequence0_binding0));
    store.put(SegmentFixtures::buildSegmentChoice(segment3, program42));

    // segment crafting
    segment4 = store.put(SegmentFixtures::buildSegment(
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
    store.put(buildSegmentChoice(segment4, Program::Type::Macro, fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(segment4, Program::Type::Main, fake.program5_sequence1_binding0));

    for (std::string memeName : List.of("Cozy", "Classic", "Outlook", "Rosy"))
      store.put(SegmentFixtures::buildSegmentMeme(segment4, memeName));

    store.put(SegmentFixtures::buildSegmentChord(segment4, 0.0f, "A minor"));
    store.put(SegmentFixtures::buildSegmentChord(segment4, 8.0f, "D Major"));
  }


  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  Collection<Object> customFixtures() {
    Collection<Object> entities = new ArrayList<>();

    // Instrument "808"
    Instrument instrument1 = EntityUtils.add(entities, ContentFixtures::buildInstrument(fake.library2, Instrument::Type::Drum, Instrument::Mode::Event, Instrument::State::Published, "808 Drums"));
    EntityUtils.add(entities, ContentFixtures::buildMeme(instrument1, "heavy"));
    //
    audioKick = EntityUtils.add(entities, ContentFixtures::buildAudio(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.6f, "KICK", "Eb", 1.0f));
    //
    audioSnare = EntityUtils.add(entities, ContentFixtures::buildAudio(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01f, 1.5f, 120.0f, 0.6f, "SNARE", "Ab", 1.0f));
    //
    audioHihat = EntityUtils.add(entities, ContentFixtures::buildAudio(instrument1, "Hihat", "iop0803k1k2l3h5a3s2d3f4g.wav", 0.01f, 1.5f, 120.0f, 0.6f, "HIHAT", "Ab", 1.0f));

    // A basic beat from scratch with layered voices
    program42 = EntityUtils.add(entities, ContentFixtures::buildProgram(fake.library2, Program::Type::Beat, Program::State::Published, "Basic Beat", "C", 121f));
    EntityUtils.add(entities, ContentFixtures::buildMeme(program42, "Basic"));
    ProgramVoice program42_locomotion = EntityUtils.add(entities, ContentFixtures::buildVoice(program42, Instrument::Type::Drum, "Locomotion"));
    ProgramVoice program42_kickSnare = EntityUtils.add(entities, ContentFixtures::buildVoice(program42, Instrument::Type::Drum, "BoomBap"));
    auto sequence35a = EntityUtils.add(entities, ContentFixtures::buildSequence(program42, 16, "Base", 0.5f, "C"));
    //
    auto pattern35a1 = EntityUtils.add(entities, ContentFixtures::buildPattern(sequence35a, program42_locomotion, 1, "Hi-hat"));
    auto trackHihat = EntityUtils.add(entities, ContentFixtures::buildTrack(program42_locomotion, "HIHAT"));
    EntityUtils.add(entities, ContentFixtures::buildEvent(pattern35a1, trackHihat, 0.0f, 1.0f, "C2", 1.0f));
    EntityUtils.add(entities, ContentFixtures::buildEvent(pattern35a1, trackHihat, 0.25f, 1.0f, "G5", 0.4f));
    EntityUtils.add(entities, ContentFixtures::buildEvent(pattern35a1, trackHihat, 0.5f, 1.0f, "C2", 0.6f));
    EntityUtils.add(entities, ContentFixtures::buildEvent(pattern35a1, trackHihat, 0.75f, 1.0f, "C2", 0.3f));
    //
    auto pattern35a2 = EntityUtils.add(entities, ContentFixtures::buildPattern(sequence35a, program42_kickSnare, 4, "Kick/Snare"));
    auto trackKick = EntityUtils.add(entities, ContentFixtures::buildTrack(program42_kickSnare, "KICK"));
    auto trackSnare = EntityUtils.add(entities, ContentFixtures::buildTrack(program42_kickSnare, "SNARE"));
    EntityUtils.add(entities, ContentFixtures::buildEvent(pattern35a2, trackKick, 0.0f, 1.0f, "B5", 0.9f));
    EntityUtils.add(entities, ContentFixtures::buildEvent(pattern35a2, trackSnare, 1.0f, 1.0f, "D2", 1.0f));
    EntityUtils.add(entities, ContentFixtures::buildEvent(pattern35a2, trackKick, 2.5f, 1.0f, "E4", 0.7f));
    EntityUtils.add(entities, ContentFixtures::buildEvent(pattern35a2, trackSnare, 3.0f, 1.0f, "c3", 0.5f));

    return entities;
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void craftBeatVoiceContinue() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);

    craftFactory.beat(fabricator).doWork();

    Segment result = store.readSegment(segment4.getId()).orElseThrow();
    assertFalse(store.readAll(result.getId(), SegmentChoice.class).empty());

    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedHihat = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(audioKick.getId()))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(audioSnare.getId()))
        pickedSnare++;
      if (pick.getInstrumentAudioId().equals(audioHihat.getId()))
        pickedHihat++;
    }
    assertEquals(8, pickedKick);
    assertEquals(8, pickedSnare);
    assertEquals(64, pickedHihat);
  }
}
