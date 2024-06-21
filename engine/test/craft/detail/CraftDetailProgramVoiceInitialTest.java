// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft.detail;

import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.entity.EntityUtils;
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
import io.xj.engine.fabricator.SegmentEntityStore;
import io.xj.engine.fabricator.SegmentEntityStoreImpl;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@ExtendWith(MockitoExtension.class)
public class CraftDetailProgramVoiceInitialTest {
  Chain chain2;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  SegmentEntityStore store;
  SegmentFixtures fake;
  Segment segment1;

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

    // force known detail selection by destroying program 35
    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new SegmentFixtures();
    sourceMaterial = new HubContent(Stream.concat(
        Stream.concat(fake.setupFixtureB1().stream(),
          fake.setupFixtureB3().stream()),
        fake.setupFixtureB4_DetailBass().stream())
      .filter(entity -> !EntityUtils.isSame(entity, fake.program35) && !EntityUtils.isChild(entity, fake.program35))
      .collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = store.put(buildChain(
      fake.project1,
      fake.template1,
      "Print #2",
      Chain::Type::Production,
      Chain::State::Fabricate
    ));
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void craftDetailVoiceInitial() throws Exception {
    insertSegments();

    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment1.getId(), 48000.0f, 2, null);

    craftFactory.detail(fabricator).doWork();

    assertFalse(fabricator.getChoices().empty());
    
    int pickedBloop = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fake.instrument9_audio8.getId()))
        pickedBloop++;
    }
    assertEquals(32, pickedBloop);
  }

  @Test
  public void craftDetailVoiceInitial_okWhenNoDetailChoice() throws Exception {
    insertSegments();
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment1.getId(), 48000.0f, 2, null);

    craftFactory.detail(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the detail choice only if specified
   */
  void insertSegments() throws FabricationException {
    // segment crafted
    Segment segment0 = store.put(SegmentFixtures::buildSegment(
      chain2,
      Segment::Type::Initial,
      0,
      0,
      Segment::State::Crafted,
      "D Major",
      32,
      0.55f,
      130.0f,
      "chains-1-segments-0970305977172.wav", true));
    store.put(buildSegmentChoice(
      segment0,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment0,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence0_binding0));

    segment1 = store.put(SegmentFixtures::buildSegment(
      chain2,
      SegmentType.CONTINUE,
      1,
      1,
      SegmentState.CRAFTING,
      "D Major",
      32,
      0.55f,
      130.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(
      segment1,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment1,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence0_binding0));
    for (std::string memeName : List.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(SegmentFixtures::buildSegmentMeme(segment1, memeName));
    SegmentChord chord0 = store.put(SegmentFixtures::buildSegmentChord(segment1, 0.0f, "C minor"));
    store.put(SegmentFixtures::buildSegmentChordVoicing(chord0, Instrument::Type::Bass, "C2, Eb2, G2"));
    SegmentChord chord1 = store.put(SegmentFixtures::buildSegmentChord(segment1, 8.0f, "Db minor"));
    store.put(SegmentFixtures::buildSegmentChordVoicing(chord1, Instrument::Type::Bass, "Db2, E2, Ab2"));
  }

}
