// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
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
import io.xj.service.nexus.persistence.NexusEntityStoreException;
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
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftRhythmProgramVoiceInitialTest {
  private Injector injector;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private NexusHubContentFixtures fake;
  private Chain chain1;
  private Segment segment1;
  private Segment segment2;
  private Segment segment3;
  private Segment segment4;
  private NexusEntityStore store;
  private Chain chain2;
  private Segment segment6;
  private HubContent hubContent;
  private Segment segment5;

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
    EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // force known rhythm selection by destroying program 35
    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusHubContentFixtures();
    when(hubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(Streams.concat(
        fake.setupFixtureB1(true).stream(),
        fake.setupFixtureB3().stream())
        .filter(entity -> !entity.isSame(fake.program35) && !entity.isChild(fake.program35))
        .collect(Collectors.toList())));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = store.put(Chain.create(fake.account1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(ChainBinding.create(chain2, fake.library2));
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftRhythmVoiceInitial() throws Exception {
    insertSegments();

    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment6);

    craftFactory.rhythm(fabricator).doWork();

    Segment result = store.get(Segment.class, segment6.getId()).orElseThrow();
    assertFalse(store.getAll(SegmentChoice.class, Segment.class, ImmutableList.of(result.getId())).isEmpty());
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedBleep = 0;
    int pickedToot = 0;
    Collection<SegmentChoiceArrangementPick> picks = store.getAll(SegmentChoiceArrangementPick.class,
      Segment.class, ImmutableList.of(result.getId()));
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8kick.getId()))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8snare.getId()))
        pickedSnare++;
      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8bleep.getId()))
        pickedBleep++;
      if (pick.getInstrumentAudioId().equals(fake.instrument8_audio8toot.getId()))
        pickedToot++;
    }
    assertEquals(12, pickedKick);
    assertEquals(12, pickedSnare);
    assertEquals(4, pickedBleep);
    assertEquals(4, pickedToot);
  }

  @Test
  public void craftRhythmVoiceInitial_okWhenNoRhythmChoice() throws Exception {
    insertSegments();
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment6);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the rhythm choice only if specified
   */
  private void insertSegments() throws NexusEntityStoreException {
    // segment crafted
    segment5 = store.put(Segment.create()
      .setChainId(chain2.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:01:07.384616Z")
      .setEndAt("2017-02-14T12:01:27.384616Z")
      .setKey("D Major")
      .setTotal(32)
      .setDensity(0.55)
      .setTempo(130.0)
      .setStorageKey("chains-1-segments-0970305977172.wav"));
    store.put(SegmentChoice.create().setSegmentId(segment5.getId())
      .setProgramId(fake.program4.getId())
      .setProgramSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(0));
    store.put(SegmentChoice.create().setSegmentId(segment5.getId())
      .setProgramId(fake.program5.getId())
      .setProgramSequenceBindingId(fake.program5_sequence0_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-6));

    // segment crafting
    segment6 = store.put(Segment.create()
      .setChainId(chain2.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:07.384616Z")
      .setKey("D Major")
      .setTotal(32)
      .setDensity(0.55)
      .setTempo(130.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));
    store.put(SegmentChoice.create().setSegmentId(segment6.getId())
      .setProgramId(fake.program4.getId())
      .setProgramSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(0));
    store.put(SegmentChoice.create().setSegmentId(segment6.getId())
      .setProgramId(fake.program5.getId())
      .setProgramSequenceBindingId(fake.program5_sequence0_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-6));
    for (String memeName : ImmutableList.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(SegmentMeme.create(segment6, memeName));

    store.put(SegmentChord.create(segment6, 0.0, "C minor"));
    store.put(SegmentChord.create(segment6, 8.0, "Db minor"));
  }

}
