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

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftRhythmNextMacroTest {
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

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusHubContentFixtures();
    when(hubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(Streams.concat(
        fake.setupFixtureB1(true).stream(),
        fake.setupFixtureB2().stream(),
        fake.setupFixtureB3().stream()
      ).collect(Collectors.toList())));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(ChainBinding.create(chain1, fake.library2));
    segment1 = store.put(Segment.create(chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    segment2 = store.put(Segment.create(chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftRhythmNextMacro() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.rhythm(fabricator).doWork();

    // assert choice of rhythm-type sequence
    Collection<SegmentChoice> segmentChoices =
      store.getAll(SegmentChoice.class, Segment.class, ImmutableList.of(segment4.getId()));
    assertNotNull(SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Rhythm));
  }

  @Test
  public void craftRhythmNextMacro_okEvenWithoutPreviousSegmentRhythmChoice() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.rhythm(fabricator).doWork();

    // assert choice of rhythm-type sequence
    Collection<SegmentChoice> segmentChoices =
      store.getAll(SegmentChoice.class, Segment.class, ImmutableList.of(segment4.getId()));
    assertNotNull(SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Rhythm));
  }

  /**
   Insert fixture segments 3 and 4, including the rhythm choice for segment 3 only if specified

   @param excludeRhythmChoiceForSegment3 if desired for the purpose of this test
   */
  private void insertSegments3and4(boolean excludeRhythmChoiceForSegment3) throws NexusEntityStoreException {
    // Chain "Test Print #1" has this segment that was just crafted
    segment3 = store.put(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("Ab minor")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    store.put(SegmentChoice.create().setSegmentId(segment3.getId())
      .setProgramId(fake.program4.getId())
      .setProgramSequenceBindingId(fake.program4_sequence2_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(3));
    store.put(SegmentChoice.create().setSegmentId(segment3.getId())
      .setProgramId(fake.program5.getId())
      .setProgramSequenceBindingId(fake.program5_sequence1_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(1));
    if (!excludeRhythmChoiceForSegment3)
      store.put(SegmentChoice.create().setSegmentId(segment3.getId())
        .setProgramId(fake.program35.getId())
        .setTypeEnum(ProgramType.Rhythm)
        .setTranspose(-4));

    // Chain "Test Print #1" has a segment in crafting state - Foundation is complete
    segment4 = store.put(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("F minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(125.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    store.put(SegmentChoice.create().setSegmentId(segment4.getId())
      .setProgramId(fake.program3.getId())
      .setProgramSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(4));
    store.put(SegmentChoice.create().setSegmentId(segment4.getId())
      .setProgramId(fake.program15.getId())
      .setProgramSequenceBindingId(fake.program15_sequence0_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-2));
    for (String memeName : ImmutableList.of("Hindsight", "Chunky", "Regret", "Tangy"))
      store.put(SegmentMeme.create(segment4, memeName));
    store.put(SegmentChord.create()
      .setSegmentId(segment4.getId())
      .setPosition(0.0)
      .setName("F minor"));
    store.put(SegmentChord.create()
      .setSegmentId(segment4.getId())
      .setPosition(8.0)
      .setName("Gb minor"));
  }


}
