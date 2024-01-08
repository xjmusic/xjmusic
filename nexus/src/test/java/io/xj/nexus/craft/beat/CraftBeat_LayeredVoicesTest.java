// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.craft.beat;

import io.xj.hub.HubContent;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.entity.EntityUtils;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.json.JsonProviderImpl;
import io.xj.nexus.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.NexusEntityStore;
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
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildEvent;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildInstrument;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildMeme;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildPattern;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildProgram;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildSequence;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildTrack;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildVoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentMeme;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 Beat fabrication composited of layered Patterns https://www.pivotaltracker.com/story/show/166481918
 */
@ExtendWith(MockitoExtension.class)
public class CraftBeat_LayeredVoicesTest {
  @Mock
  public HubClient hubClient;
  CraftFactory craftFactory;
  FabricatorFactory fabricatorFactory;
  HubContent sourceMaterial;
  InstrumentAudio audioHihat;
  InstrumentAudio audioKick;
  InstrumentAudio audioSnare;
  NexusEntityStore store;
  NexusIntegrationTestingFixtures fake;
  Program program42;
  Segment segment4;

  @BeforeEach
  public void setUp() throws Exception {
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    craftFactory = new CraftFactoryImpl();
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    store = new NexusEntityStoreImpl(entityFactory);
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
      "chains-1-segments-9f7s89d8a7892.wav", true));

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
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(segment3, ProgramType.Macro, fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(segment3, ProgramType.Main, fake.program5_sequence0_binding0));
    store.put(buildSegmentChoice(segment3, program42));

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
      "chains-1-segments-9f7s89d8a7892.wav", true));
    store.put(buildSegmentChoice(segment4, ProgramType.Macro, fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(segment4, ProgramType.Main, fake.program5_sequence1_binding0));

    for (String memeName : List.of("Cozy", "Classic", "Outlook", "Rosy"))
      store.put(buildSegmentMeme(segment4, memeName));

    store.put(buildSegmentChord(segment4, 0.0f, "A minor"));
    store.put(buildSegmentChord(segment4, 8.0f, "D Major"));
  }


  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  Collection<Object> customFixtures() {
    Collection<Object> entities = new ArrayList<>();

    // Instrument "808"
    Instrument instrument1 = EntityUtils.add(entities, buildInstrument(fake.library2, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "808 Drums"));
    EntityUtils.add(entities, buildMeme(instrument1, "heavy"));
    //
    audioKick = EntityUtils.add(entities, buildAudio(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.6f, "KICK", "Eb", 1.0f));
    //
    audioSnare = EntityUtils.add(entities, buildAudio(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01f, 1.5f, 120.0f, 0.6f, "SNARE", "Ab", 1.0f));
    //
    audioHihat = EntityUtils.add(entities, buildAudio(instrument1, "Hihat", "iop0803k1k2l3h5a3s2d3f4g.wav", 0.01f, 1.5f, 120.0f, 0.6f, "HIHAT", "Ab", 1.0f));

    // A basic beat from scratch with layered voices
    program42 = EntityUtils.add(entities, buildProgram(fake.library2, ProgramType.Beat, ProgramState.Published, "Basic Beat", "C", 121f, 0.6f));
    EntityUtils.add(entities, buildMeme(program42, "Basic"));
    ProgramVoice program42_locomotion = EntityUtils.add(entities, buildVoice(program42, InstrumentType.Drum, "Locomotion"));
    ProgramVoice program42_kickSnare = EntityUtils.add(entities, buildVoice(program42, InstrumentType.Drum, "BoomBap"));
    var sequence35a = EntityUtils.add(entities, buildSequence(program42, 16, "Base", 0.5f, "C"));
    //
    var pattern35a1 = EntityUtils.add(entities, buildPattern(sequence35a, program42_locomotion, 1, "Hi-hat"));
    var trackHihat = EntityUtils.add(entities, buildTrack(program42_locomotion, "HIHAT"));
    EntityUtils.add(entities, buildEvent(pattern35a1, trackHihat, 0.0f, 1.0f, "C2", 1.0f));
    EntityUtils.add(entities, buildEvent(pattern35a1, trackHihat, 0.25f, 1.0f, "G5", 0.4f));
    EntityUtils.add(entities, buildEvent(pattern35a1, trackHihat, 0.5f, 1.0f, "C2", 0.6f));
    EntityUtils.add(entities, buildEvent(pattern35a1, trackHihat, 0.75f, 1.0f, "C2", 0.3f));
    //
    var pattern35a2 = EntityUtils.add(entities, buildPattern(sequence35a, program42_kickSnare, 4, "Kick/Snare"));
    var trackKick = EntityUtils.add(entities, buildTrack(program42_kickSnare, "KICK"));
    var trackSnare = EntityUtils.add(entities, buildTrack(program42_kickSnare, "SNARE"));
    EntityUtils.add(entities, buildEvent(pattern35a2, trackKick, 0.0f, 1.0f, "B5", 0.9f));
    EntityUtils.add(entities, buildEvent(pattern35a2, trackSnare, 1.0f, 1.0f, "D2", 1.0f));
    EntityUtils.add(entities, buildEvent(pattern35a2, trackKick, 2.5f, 1.0f, "E4", 0.7f));
    EntityUtils.add(entities, buildEvent(pattern35a2, trackSnare, 3.0f, 1.0f, "c3", 0.5f));

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
    assertFalse(store.readAll(result.getId(), SegmentChoice.class).isEmpty());
    // test vector for persist Audio pick in memory https://www.pivotaltracker.com/story/show/154014731
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
