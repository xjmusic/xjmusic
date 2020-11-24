// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramVoice;
import io.xj.ProgramVoiceTrack;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.NexusIntegrationTestingFixtures;
import io.xj.service.nexus.craft.CraftFactory;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import io.xj.service.nexus.work.NexusWorkModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildInstrument;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildInstrumentAudio;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildInstrumentAudioEvent;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildInstrumentMeme;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgram;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramMeme;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramSequence;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramSequencePattern;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramSequencePatternEvent;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramVoice;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildProgramVoiceTrack;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.service.nexus.NexusIntegrationTestingFixtures.buildSegmentMeme;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

/**
 [#166481918] Rhythm fabrication composited of layered Patterns
 */
@RunWith(MockitoJUnitRunner.class)
public class CraftRhythm_LayeredVoicesTest {
  private Injector injector;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private ProgramDAO programDAO;
  private NexusIntegrationTestingFixtures fake;
  private Chain chain1;
  private Segment segment1;
  private Segment segment2;
  private Segment segment3;
  private Segment segment4;
  private NexusEntityStore store;
  private Instrument instrument1;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Mock
  public HubClient hubClient;
  private InstrumentAudio audioKick;
  private InstrumentAudio audioSnare;
  private InstrumentAudio audioHihat;
  private Program program42;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config,
      ImmutableSet.of(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(HubClient.class).toInstance(hubClient);
          }
        })));
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    when(hubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(Streams.concat(
        fake.setupFixtureB1().stream().filter(entity -> !Entities.isSame(entity, fake.program35) && !Entities.isChild(entity, fake.program35)),
        customFixtures().stream()
      ).collect(Collectors.toList())));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(buildChain(fake.account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    segment1 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(0)
      .setState(Segment.State.Dubbed)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:32.000001Z")
      .setKey("D major")
      .setTotal(64)
      .setDensity(0.73)
      .setTempo(120)
      .setStorageKey("chains-1-segments-9f7s89d8a7892")
      .setOutputEncoder("wav")
      .build());
    segment2 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(1)
      .setState(Segment.State.Dubbing)
      .setBeginAt("2017-02-14T12:01:32.000001Z")
      .setEndAt("2017-02-14T12:02:04.000001Z")
      .setKey("Db minor")
      .setTotal(64)
      .setDensity(0.85)
      .setTempo(120)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());

    // segment just crafted
    // Testing entities for reference
    segment3 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(2L)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());
    store.put(buildSegmentChoice(segment3, Program.Type.Macro, fake.program4_sequence0_binding0, 3));
    store.put(buildSegmentChoice(segment3, Program.Type.Main, fake.program5_sequence0_binding0, 5));
    store.put(buildSegmentChoice(segment3, Program.Type.Rhythm, program42, 5));

    // segment crafting
    segment4 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(3L)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("D Major")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());
    store.put(buildSegmentChoice(segment4, Program.Type.Macro, fake.program4_sequence0_binding0, 3));
    store.put(buildSegmentChoice(segment4, Program.Type.Main, fake.program5_sequence1_binding0, -5));

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
    instrument1 = Entities.add(entities, buildInstrument(fake.library2, Instrument.Type.Percussive, Instrument.State.Published, "808 Drums"));
    Entities.add(entities, buildInstrumentMeme(instrument1, "heavy"));
    //
    audioKick = Entities.add(entities, buildInstrumentAudio(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, 0.6));
    Entities.add(entities, buildInstrumentAudioEvent(audioKick, 0, 1, "KICK", "Eb", 1.0));
    //
    audioSnare = Entities.add(entities, buildInstrumentAudio(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01, 1.5, 120.0, 1200, 0.6));
    Entities.add(entities, buildInstrumentAudioEvent(audioSnare, 0, 1, "SNARE", "Ab", 1.0));
    //
    audioHihat = Entities.add(entities, buildInstrumentAudio(instrument1, "Hihat", "iop0803k1k2l3h5a3s2d3f4g.wav", 0.01, 1.5, 120.0, 1200, 0.6));
    Entities.add(entities, buildInstrumentAudioEvent(audioHihat, 0, 1, "HIHAT", "Ab", 1.0));

    // A basic beat from scratch with layered voices
    program42 = Entities.add(entities, buildProgram(fake.library2, Program.Type.Rhythm, Program.State.Published, "Basic Beat", "C", 121, 0.6));
    Entities.add(entities, buildProgramMeme(program42, "Basic"));
    var locomotion = Entities.add(entities, buildProgramVoice(program42, Instrument.Type.Percussive, "Locomotion"));
    var kickSnare = Entities.add(entities, buildProgramVoice(program42, Instrument.Type.Percussive, "BoomBap"));
    var sequence35a = Entities.add(entities, buildProgramSequence(program42, 16, "Base", 0.5, "C", 110.3));
    //
    var pattern35a1 = Entities.add(entities, buildProgramSequencePattern(sequence35a, locomotion, ProgramSequencePattern.Type.Loop, 1, "Hi-hat"));
    var trackHihat = Entities.add(entities, buildProgramVoiceTrack(locomotion, "HIHAT"));
    Entities.add(entities, buildProgramSequencePatternEvent(pattern35a1, trackHihat, 0.0, 1.0, "C2", 1.0));
    Entities.add(entities, buildProgramSequencePatternEvent(pattern35a1, trackHihat, 0.25, 1.0, "G5", 0.4));
    Entities.add(entities, buildProgramSequencePatternEvent(pattern35a1, trackHihat, 0.5, 1.0, "C2", 0.6));
    Entities.add(entities, buildProgramSequencePatternEvent(pattern35a1, trackHihat, 0.75, 1.0, "C2", 0.3));
    //
    var pattern35a2 = Entities.add(entities, buildProgramSequencePattern(sequence35a, kickSnare, ProgramSequencePattern.Type.Loop, 4, "Kick/Snare"));
    var trackKick = Entities.add(entities, buildProgramVoiceTrack(kickSnare, "KICK"));
    var trackSnare = Entities.add(entities, buildProgramVoiceTrack(kickSnare, "SNARE"));
    Entities.add(entities, buildProgramSequencePatternEvent(pattern35a2, trackKick, 0.0, 1.0, "B5", 0.9));
    Entities.add(entities, buildProgramSequencePatternEvent(pattern35a2, trackSnare, 1.0, 1.0, "D2", 1.0));
    Entities.add(entities, buildProgramSequencePatternEvent(pattern35a2, trackKick, 2.5, 1.0, "E4", 0.7));
    Entities.add(entities, buildProgramSequencePatternEvent(pattern35a2, trackSnare, 3.0, 1.0, "c3", 0.5));

    return entities;
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftRhythmVoiceContinue() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.rhythm(fabricator).doWork();

    Segment result = store.get(Segment.class, segment4.getId()).orElseThrow();
    assertFalse(store.getAll(SegmentChoice.class, Segment.class, ImmutableList.of(result.getId())).isEmpty());
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedHihat = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator.getSegmentChoiceArrangementPicks();
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
