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
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.entity.Instrument;
import io.xj.service.hub.entity.InstrumentAudio;
import io.xj.service.hub.entity.InstrumentAudioEvent;
import io.xj.service.hub.entity.InstrumentMeme;
import io.xj.service.hub.entity.InstrumentState;
import io.xj.service.hub.entity.InstrumentType;
import io.xj.service.hub.entity.ProgramType;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.NexusHubContentFixtures;
import io.xj.service.nexus.craft.CraftFactory;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainBinding;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.ChainType;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentChoice;
import io.xj.service.nexus.entity.SegmentChoiceArrangementPick;
import io.xj.service.nexus.entity.SegmentChord;
import io.xj.service.nexus.entity.SegmentMeme;
import io.xj.service.nexus.entity.SegmentState;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.lib.entity.EntityStoreException;
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
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftRhythmProgramVoiceNextMainTest {
  private Injector injector;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private ProgramDAO programDAO;
  private NexusHubContentFixtures fake;
  private Chain chain1;
  private Segment segment1;
  private Segment segment2;
  private Segment segment3;
  private Segment segment4;
  private NexusEntityStore store;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Mock
  public HubClient hubClient;
  private Instrument instrument1;
  private InstrumentAudio audioKick;
  private InstrumentAudio audioSnare;

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
    EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusHubContentFixtures();
    when(hubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(Streams.concat(
        fake.setupFixtureB1(true).stream(),
        fake.setupFixtureB2().stream(),
        customFixtures().stream()
      ).collect(Collectors.toList())));

// Chain "Test Print #1" has 5 total segments
    chain1 = store.put(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(ChainBinding.create(chain1, fake.library2));
    segment1 = store.put(Segment.create(chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    segment2 = store.put(Segment.create(chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));
  }

  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  private Collection<Entity> customFixtures() {
    Collection<Entity> entities = Lists.newArrayList();

    // Instrument "808"
    instrument1 = Entities.add(entities, Instrument.create(fake.user3, fake.library2, InstrumentType.Percussive, InstrumentState.Published, "808 Drums"));
    Entities.add(entities, InstrumentMeme.create(instrument1, "heavy"));
    //
    audioKick = Entities.add(entities, InstrumentAudio.create(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, 0.6));
    Entities.add(entities, InstrumentAudioEvent.create(audioKick, 0, 1, "KICK", "Eb", 1.0));
    //
    audioSnare = Entities.add(entities, InstrumentAudio.create(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01, 1.5, 120.0, 1200, 0.6));
    Entities.add(entities, InstrumentAudioEvent.create(audioSnare, 1, 1, "SNARE", "Ab", 1.0));

    return entities;
  }

  @Test
  public void craftRhythmVoiceNextMain() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.rhythm(fabricator).doWork();

    assertNotNull(fabricator.getArrangements(fabricator.getCurrentRhythmChoice()));

    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    Collection<SegmentChoiceArrangementPick> picks = store.getAll(SegmentChoiceArrangementPick.class,
      Segment.class, ImmutableList.of(segment4.getId()));
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
  public void craftRhythmVoiceNextMain_okIfNoRhythmChoice() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segments 3 and 4, including the rhythm choice for segment 3 only if specified

   @param excludeRhythmChoiceForSegment3 if desired for the purpose of this test
   */
  private void insertSegments3and4(boolean excludeRhythmChoiceForSegment3) throws EntityStoreException {
    // segment just crafted
    // Testing entities for reference
    segment3 = store.put(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));
    store.put(SegmentChoice.create().setSegmentId(segment3.getId())
      .setProgramId(fake.program4.getId())
      .setProgramSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(3));
    store.put(SegmentChoice.create().setSegmentId(segment3.getId())
      .setProgramId(fake.program15.getId())
      .setProgramSequenceBindingId(fake.program15_sequence1_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-4));
    if (!excludeRhythmChoiceForSegment3)
      store.put(SegmentChoice.create().setSegmentId(segment3.getId())
        .setProgramId(fake.program35.getId())
        .setTypeEnum(ProgramType.Rhythm)
        .setTranspose(-5));

    // segment crafting
    segment4 = store.put(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("G minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(120.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));
    store.put(SegmentChoice.create().setSegmentId(segment4.getId())
      .setProgramId(fake.program4.getId())
      .setProgramSequenceBindingId(fake.program4_sequence1_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(3));
    store.put(SegmentChoice.create().setSegmentId(segment4.getId())
      .setProgramId(fake.program15.getId())
      .setProgramSequenceBindingId(fake.program15_sequence0_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(0));
    for (String memeName : ImmutableList.of("Regret", "Sky", "Hindsight", "Tropical"))
      store.put(SegmentMeme.create(segment4, memeName));

    store.put(SegmentChord.create(segment4, 0.0, "G minor"));
    store.put(SegmentChord.create(segment4, 8.0, "Ab minor"));
  }

}
