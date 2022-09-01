// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.beat;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.nexus.model.*;
import io.xj.hub.HubTopology;
import io.xj.hub.enums.*;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.stream.Collectors;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.nexus.NexusIntegrationTestingFixtures.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 https://www.pivotaltracker.com/story/show/166481918 Beat fabrication composited of layered Patterns
 */
@RunWith(MockitoJUnitRunner.class)
public class CraftBeat_LayeredVoicesTest {
  @Mock
  public HubClient hubClient;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private InstrumentAudio audioHihat;
  private InstrumentAudio audioKick;
  private InstrumentAudio audioSnare;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;
  private Program program42;
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    Environment env = Environment.getDefault();
    var injector = Guice.createInjector(Modules.override(new NexusWorkModule())
      .with(new AbstractModule() {
        @Override
        protected void configure() {
          bind(Environment.class).toInstance(env);
        }
      }));
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream().filter(entity -> !Entities.isSame(entity, fake.program35) && !Entities.isChild(entity, fake.program35)),
      customFixtures().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(buildSegment(
      chain1,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.DUBBED,
      Instant.parse("2017-02-14T12:01:00.000001Z"),
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      "D major",
      64,
      0.73,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      "wav"));
    store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      1,
      1,
      SegmentState.DUBBING,
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      Instant.parse("2017-02-14T12:02:04.000001Z"),
      "Db minor",
      64,
      0.85,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav"));

    // segment just crafted
    // Testing entities for reference
    Segment segment3 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      2,
      2,
      SegmentState.CRAFTED,
      Instant.parse("2017-02-14T12:02:04.000001Z"),
      Instant.parse("2017-02-14T12:02:36.000001Z"),
      "F Major",
      64,
      0.30,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav"));
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
      Instant.parse("2017-02-14T12:03:08.000001Z"),
      Instant.parse("2017-02-14T12:03:15.836735Z"),
      "D Major",
      16,
      0.45,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav"));
    store.put(buildSegmentChoice(segment4, ProgramType.Macro, fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(segment4, ProgramType.Main, fake.program5_sequence1_binding0));

    for (String memeName : ImmutableList.of("Cozy", "Classic", "Outlook", "Rosy"))
      store.put(buildSegmentMeme(segment4, memeName));

    store.put(buildSegmentChord(segment4, 0.0, "A minor"));
    store.put(buildSegmentChord(segment4, 8.0, "D Major"));
  }


  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  private Collection<Object> customFixtures() {
    Collection<Object> entities = Lists.newArrayList();

    // Instrument "808"
    Instrument instrument1 = Entities.add(entities, buildInstrument(fake.library2, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "808 Drums"));
    Entities.add(entities, buildMeme(instrument1, "heavy"));
    //
    audioKick = Entities.add(entities, buildAudio(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.6f, "KICK", "Eb", 1.0f));
    //
    audioSnare = Entities.add(entities, buildAudio(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01f, 1.5f, 120.0f, 0.6f, "SNARE", "Ab", 1.0f));
    //
    audioHihat = Entities.add(entities, buildAudio(instrument1, "Hihat", "iop0803k1k2l3h5a3s2d3f4g.wav", 0.01f, 1.5f, 120.0f, 0.6f, "HIHAT", "Ab", 1.0f));

    // A basic beat from scratch with layered voices
    program42 = Entities.add(entities, buildProgram(fake.library2, ProgramType.Beat, ProgramState.Published, "Basic Beat", "C", 121f, 0.6f));
    Entities.add(entities, buildMeme(program42, "Basic"));
    ProgramVoice program42_locomotion = Entities.add(entities, buildVoice(program42, InstrumentType.Drum, "Locomotion"));
    ProgramVoice program42_kickSnare = Entities.add(entities, buildVoice(program42, InstrumentType.Drum, "BoomBap"));
    var sequence35a = Entities.add(entities, buildSequence(program42, 16, "Base", 0.5f, "C"));
    //
    var pattern35a1 = Entities.add(entities, buildPattern(sequence35a, program42_locomotion, 1, "Hi-hat"));
    var trackHihat = Entities.add(entities, buildTrack(program42_locomotion, "HIHAT"));
    Entities.add(entities, buildEvent(pattern35a1, trackHihat, 0.0f, 1.0f, "C2", 1.0f));
    Entities.add(entities, buildEvent(pattern35a1, trackHihat, 0.25f, 1.0f, "G5", 0.4f));
    Entities.add(entities, buildEvent(pattern35a1, trackHihat, 0.5f, 1.0f, "C2", 0.6f));
    Entities.add(entities, buildEvent(pattern35a1, trackHihat, 0.75f, 1.0f, "C2", 0.3f));
    //
    var pattern35a2 = Entities.add(entities, buildPattern(sequence35a, program42_kickSnare, 4, "Kick/Snare"));
    var trackKick = Entities.add(entities, buildTrack(program42_kickSnare, "KICK"));
    var trackSnare = Entities.add(entities, buildTrack(program42_kickSnare, "SNARE"));
    Entities.add(entities, buildEvent(pattern35a2, trackKick, 0.0f, 1.0f, "B5", 0.9f));
    Entities.add(entities, buildEvent(pattern35a2, trackSnare, 1.0f, 1.0f, "D2", 1.0f));
    Entities.add(entities, buildEvent(pattern35a2, trackKick, 2.5f, 1.0f, "E4", 0.7f));
    Entities.add(entities, buildEvent(pattern35a2, trackSnare, 3.0f, 1.0f, "c3", 0.5f));

    return entities;
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftBeatVoiceContinue() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4);

    craftFactory.beat(fabricator).doWork();

    Segment result = store.getSegment(segment4.getId()).orElseThrow();
    assertFalse(store.getAll(result.getId(), SegmentChoice.class).isEmpty());
    // test vector for https://www.pivotaltracker.com/story/show/154014731 persist Audio pick in memory
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
