// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.transition;

import io.xj.hub.HubContent;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.entity.EntityUtils;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.hub.HubTopology;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildAudio;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildInstrument;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildMeme;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;

/**
 Transition fabrication composited of layered Patterns https://www.pivotaltracker.com/story/show/166481918
 */
@ExtendWith(MockitoExtension.class)
public class CraftTransition_LayeredVoicesTest {
  @Mock
  public HubClient hubClient;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
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
    var store = new NexusEntityStoreImpl(entityFactory);
    fabricatorFactory = new FabricatorFactoryImpl(
      store,
      jsonapiPayloadFactory,
      jsonProvider
    );

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(
      fake.setupFixtureB1().stream().filter(entity -> !EntityUtils.isSame(entity, fake.program35) && !EntityUtils.isChild(entity, fake.program35)),
      customFixtures().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(buildChain(fake.project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, null));
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
      "chains-1-segments-9f7s89d8a7892.wav",
      true));

    // segment just crafted
    // Testing entities for reference
    Segment segment3 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      2,
      2,
      SegmentState.CRAFTED,
      "F Major",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav",
      true));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.Macro, fake.program4_sequence0_binding0));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.Main, fake.program5_sequence0_binding0));

    // segment crafting
    segment4 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      3,
      3,
      SegmentState.CRAFTING,
      "D Major",
      16,
      0.45f,
      120.0f,
      "chains-1-segments-9f7s89d8a7892.wav",
      true));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment4, ProgramType.Macro, fake.program4_sequence0_binding0));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment4, ProgramType.Main, fake.program5_sequence1_binding0));

    for (String memeName : List.of("Cozy", "Classic", "Outlook", "Rosy"))
      store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment4, memeName));

    store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment4, 0.0f, "A minor"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment4, 8.0f, "D Major"));
  }


  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  Collection<Object> customFixtures() {
    Collection<Object> entities = new ArrayList<>();

    // Instrument "808"
    Instrument instrument1 = EntityUtils.add(entities, buildInstrument(fake.library2, InstrumentType.Sweep, InstrumentMode.Transition, InstrumentState.Published, "Bongo Loop"));
    EntityUtils.add(entities, buildMeme(instrument1, "heavy"));
    //
    EntityUtils.add(entities, buildAudio(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.6f, "KICK", "Eb", 1.0f));
    //
    EntityUtils.add(entities, buildAudio(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01f, 1.5f, 120.0f, 0.6f, "SNARE", "Ab", 1.0f));
    //
    EntityUtils.add(entities, buildAudio(instrument1, "Hihat", "iop0803k1k2l3h5a3s2d3f4g.wav", 0.01f, 1.5f, 120.0f, 0.6f, "HIHAT", "Ab", 1.0f));

    return entities;
  }

  @AfterEach
  public void tearDown() {

  }

  @Test
  public void craftTransitionVoiceContinue() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4.getId(), 48000.0f, 2, null);

    craftFactory.transition(fabricator).doWork();

//    Segment result = store.getSegment(segment4.getId()).orElseThrow();
//    assertFalse(store.getAll(result.getId(), SegmentChoice.class).isEmpty());
//    // test vector for persist Audio pick in memory https://www.pivotaltracker.com/story/show/154014731
//    int pickedKick = 0;
//    int pickedSnare = 0;
//    int pickedHihat = 0;
//    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
//    for (SegmentChoiceArrangementPick pick : picks) {
//      if (pick.getInstrumentAudioId().equals(audioKick.getId()))
//        pickedKick++;
//      if (pick.getInstrumentAudioId().equals(audioSnare.getId()))
//        pickedSnare++;
//      if (pick.getInstrumentAudioId().equals(audioHihat.getId()))
//        pickedHihat++;
//    }
//    assertEquals(8, pickedKick);
//    assertEquals(8, pickedSnare);
//    assertEquals(64, pickedHihat);
  }
}
