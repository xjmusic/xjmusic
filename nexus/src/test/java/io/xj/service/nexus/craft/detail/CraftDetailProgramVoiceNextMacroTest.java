// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.detail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Instrument;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftDetailProgramVoiceNextMacroTest {
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private NexusIntegrationTestingFixtures fake;
  private Chain chain1;
  private Segment segment4;
  private NexusEntityStore store;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Mock
  public HubClient hubClient;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config,
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
        fake.setupFixtureB4_DetailBass().stream()
      ).collect(Collectors.toList())));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(buildChain(fake.account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    store.put(Segment.newBuilder()
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
    store.put(Segment.newBuilder()
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

  @Test
  public void craftDetailVoiceNextMacro() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.detail(fabricator).doWork();

    // assert detail choice
    Collection<SegmentChoice> segmentChoices = fabricator.getChoices();
    SegmentChoice rhythmChoice = segmentChoices.stream()
      .filter(c -> c.getProgramType().equals(Program.Type.Detail)).findFirst().orElseThrow();
    assertTrue(fabricator.getArrangements()
      .stream().anyMatch(a -> a.getSegmentChoiceId().equals(rhythmChoice.getId())));
    // test vector for [#154014731] persist Audio pick in memory
    int pickedBloop = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fake.instrument9_audio8.getId()))
        pickedBloop++;
    }
    assertEquals(16, pickedBloop);
  }

  @Test
  public void craftDetailVoiceNextMacro_okIfNoDetailChoice() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.detail(fabricator).doWork();
  }

  /**
   Insert fixture segments 3 and 4, including the detail choice for segment 3 only if specified

   @param excludeDetailChoiceForSegment3 if desired for the purpose of this test
   */
  private void insertSegments3and4(boolean excludeDetailChoiceForSegment3) throws NexusException {
    // Chain "Test Print #1" has this segment that was just crafted
    Segment segment3 = store.put(Segment.newBuilder()
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
    if (!excludeDetailChoiceForSegment3)
      store.put(SegmentChoice.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(segment3.getId())
        .setProgramId(fake.program10.getId())
        .setProgramType(Program.Type.Detail)
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
    SegmentChord chord0 = store.put(SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment4.getId())
      .setPosition(0.0)
      .setName("F minor")
      .build());
    store.put(SegmentChordVoicing.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment4.getId())
      .setSegmentChordId(chord0.getId())
      .setType(Instrument.Type.Bass)
      .setNotes("F2, Ab2, B2")
      .build());
    SegmentChord chord1 = store.put(SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment4.getId())
      .setPosition(8.0)
      .setName("Gb minor")
      .build());
    store.put(SegmentChordVoicing.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment4.getId())
      .setSegmentChordId(chord1.getId())
      .setType(Instrument.Type.Bass)
      .setNotes("Gb2, A2, Db3")
      .build());
  }

}
