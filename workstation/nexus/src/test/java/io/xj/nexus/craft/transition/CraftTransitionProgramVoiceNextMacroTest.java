// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.transition;

import io.xj.hub.HubContent;
import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.entity.EntityUtils;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.fabricator.FabricationEntityStore;
import io.xj.nexus.fabricator.FabricationEntityStoreImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildAudio;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildInstrument;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildMeme;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentMeme;

@ExtendWith(MockitoExtension.class)
public class CraftTransitionProgramVoiceNextMacroTest {
  Chain chain1;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  InstrumentAudio audioKick;
  InstrumentAudio audioSnare;
  FabricationEntityStore store;
  NexusIntegrationTestingFixtures fake;
  Segment segment4;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    craftFactory = new CraftFactoryImpl();
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    store = new FabricationEntityStoreImpl(entityFactory);
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // Mock request via HubClientFactory returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      Stream.concat(fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream()),
      customFixtures().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(NexusIntegrationTestingFixtures.buildChain(fake.project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
    store.put(buildSegment(
      chain1,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.CRAFTED,
      "D major",
      64,
      0.73f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892",
      true));
    store.put(buildSegment(
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
  }

  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  Collection<Object> customFixtures() {
    Collection<Object> entities = new ArrayList<>();

    // Instrument "808"
    Instrument instrument1 = EntityUtils.add(entities, buildInstrument(fake.library2, InstrumentType.Transition, InstrumentMode.Event, InstrumentState.Published, "Bongo Loop"));
    EntityUtils.add(entities, buildMeme(instrument1, "heavy"));
    //
    audioKick = EntityUtils.add(entities, buildAudio(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.6f, "KICK", "Eb", 1.0f));
    //
    audioSnare = EntityUtils.add(entities, buildAudio(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01f, 1.5f, 120.0f, 0.6f, "SNARE", "Ab", 1.0f));

    return entities;
  }


  @Test
  public void craftTransitionVoiceNextMacro() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);

    craftFactory.transition(fabricator).doWork();

//    // assert transition choice
//    Collection<SegmentChoice> segmentChoices = fabricator.getChoices();
//    SegmentChoice transitionChoice = segmentChoices.stream()
//      .filter(c -> c.getInstrumentType().equals(InstrumentType.Transition, InstrumentMode.Event)).findFirst().orElseThrow();
//    assertTrue(fabricator.getArrangements()
//      .stream().anyMatch(a -> a.getSegmentChoiceId().equals(transitionChoice.getId())));
//    
//    int pickedKick = 0;
//    int pickedSnare = 0;
//    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
//    for (SegmentChoiceArrangementPick pick : picks) {
//      if (pick.getInstrumentAudioId().equals(audioKick.getId()))
//        pickedKick++;
//      if (pick.getInstrumentAudioId().equals(audioSnare.getId()))
//        pickedSnare++;
//    }
//    assertEquals(8, pickedKick);
//    assertEquals(8, pickedSnare);
  }

  /**
   Insert fixture segments 3 and 4, including the transition choice for segment 3 only if specified

   @param excludeTransitionChoiceForSegment3 if desired for the purpose of this test
   */
  void insertSegments3and4(boolean excludeTransitionChoiceForSegment3) throws NexusException {
    // Chain "Test Print #1" has this segment that was just crafted
    Segment segment3 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      2,
      2,
      SegmentState.CRAFTED,
      "Ab minor",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence2_binding0));
    store.put(buildSegmentChoice(
      segment3,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence1_binding0));
    if (!excludeTransitionChoiceForSegment3)
      store.put(buildSegmentChoice(
        segment3,
        Segment.DELTA_UNLIMITED,
        Segment.DELTA_UNLIMITED,
        fake.program35,
        InstrumentType.Transition, InstrumentMode.Event));

    // Chain "Test Print #1" has a segment in crafting state - Foundation is complete
    segment4 = store.put(buildSegment(
      chain1,
      SegmentType.NEXT_MACRO,
      3,
      0,
      SegmentState.CRAFTING,
      "F minor",
      16,
      0.45f,
      125.0f,
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(
      segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program3,
      fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(
      segment4,
      Segment.DELTA_UNLIMITED,
      Segment.DELTA_UNLIMITED,
      fake.program15,
      fake.program15_sequence0_binding0));
    for (String memeName : List.of("Hindsight", "Chunky", "Regret", "Tangy"))
      store.put(buildSegmentMeme(segment4, memeName));
    store.put(buildSegmentChord(segment4, 0.0f, "F minor"));
    store.put(buildSegmentChord(segment4, 8.0f, "Gb minor"));
  }

}
