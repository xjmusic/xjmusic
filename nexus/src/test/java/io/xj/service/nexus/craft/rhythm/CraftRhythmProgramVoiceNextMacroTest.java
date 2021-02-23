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
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentMeme;
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
import io.xj.service.nexus.NexusException;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import io.xj.service.nexus.work.NexusWorkModule;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftRhythmProgramVoiceNextMacroTest {
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
  private InstrumentAudio audioKick;
  private InstrumentAudio audioSnare;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Mock
  public HubClient hubClient;

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
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream(),
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
    Entities.add(entities, buildInstrumentAudioEvent(audioSnare, 1, 1, "SNARE", "Ab", 1.0));

    return entities;
  }


  @Test
  public void craftRhythmVoiceNextMacro() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.rhythm(fabricator).doWork();

    // assert rhythm choice
    Collection<SegmentChoice> segmentChoices = fabricator.getChoices();
    SegmentChoice rhythmChoice = segmentChoices.stream()
      .filter(c -> c.getProgramType().equals(Program.Type.Rhythm)).findFirst().orElseThrow();
    assertTrue(fabricator.getArrangements()
      .stream().anyMatch(a -> a.getSegmentChoiceId().equals(rhythmChoice.getId())));
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(audioKick.getId()))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(audioSnare.getId()))
        pickedSnare++;
    }
    assertEquals(8, pickedKick);
    assertEquals(8, pickedSnare);
  }

  @Test
  public void craftRhythmVoiceNextMacro_okIfNoRhythmChoice() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segments 3 and 4, including the rhythm choice for segment 3 only if specified

   @param excludeRhythmChoiceForSegment3 if desired for the purpose of this test
   */
  private void insertSegments3and4(boolean excludeRhythmChoiceForSegment3) throws NexusException {
    // Chain "Test Print #1" has this segment that was just crafted
    segment3 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(2L)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("Ab minor")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment3.getId())
      .setProgramId(fake.program4.getId())
      .setProgramId(fake.program4_sequence2_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program4_sequence2_binding0.getId())
      .setProgramType(Program.Type.Macro)
            .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment3.getId())
      .setProgramId(fake.program5.getId())
      .setProgramId(fake.program5_sequence1_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program5_sequence1_binding0.getId())
      .setProgramType(Program.Type.Main)
            .build());
    if (!excludeRhythmChoiceForSegment3)
      store.put(SegmentChoice.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(segment3.getId())
        .setProgramId(fake.program35.getId())
        .setProgramType(Program.Type.Rhythm)
                .build());

    // Chain "Test Print #1" has a segment in crafting state - Foundation is complete
    segment4 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setOffset(3L)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("F minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(125.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment4.getId())
      .setProgramId(fake.program3.getId())
      .setProgramId(fake.program4_sequence0_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .setProgramType(Program.Type.Macro)
            .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment4.getId())
      .setProgramId(fake.program15.getId())
      .setProgramId(fake.program15_sequence0_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program15_sequence0_binding0.getId())
      .setProgramType(Program.Type.Main)
            .build());
    for (String memeName : ImmutableList.of("Hindsight", "Chunky", "Regret", "Tangy")) {
      store.put(SegmentMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(segment4.getId()).setName(memeName)
        .build());
    }
    store.put(SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment4.getId()).setPosition(0.0).setName("F minor")
      .build());
    store.put(SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment4.getId()).setPosition(8.0).setName("Gb minor")
      .build());
  }

}
