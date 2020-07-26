// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.macro;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.MemeEntity;
import io.xj.lib.entity.common.ChordEntity;
import io.xj.lib.mixer.OutputEncoder;
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
import io.xj.service.nexus.entity.SegmentType;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.nexus.persistence.NexusEntityStore;
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
import java.util.stream.Collectors;

import static io.xj.lib.util.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftFoundationNextMacroTest {
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
        fake.setupFixtureB2().stream()
      ).collect(Collectors.toList())));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(ChainBinding.create(chain1, fake.library2));
    segment1 = store.put(Segment.create(chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    segment2 = store.put(Segment.create(chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

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
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav"));
    store.put(SegmentChoice.create(segment3, ProgramType.Macro, fake.program4_sequence2_binding0, 3));
    store.put(SegmentChoice.create(segment3, ProgramType.Main, fake.program5_sequence1_binding0, 1));

    // Chain "Test Print #1" has a planned segment
    segment4 = store.put(Segment.create(chain1, 3L, SegmentState.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"), null, "C", 8, 0.8, 120, "chain-1-waveform-12345.wav"));
  }

  @Test
  public void craftFoundationNextMacro() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = store.get(Segment.class, segment4.getId()).orElseThrow();
    assertEquals(SegmentType.NextMacro, result.getType());
    assertEquals("2017-02-14T12:03:15.840157Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(16), result.getTotal());
    assertEquals(OutputEncoder.AAC, result.getOutputEncoder());
    assertEquals(Double.valueOf(0.45), result.getDensity());
    assertEquals("Db minor", result.getKey());
    assertEquals(Double.valueOf(125), result.getTempo());
    // assert memes
    assertSameItems(
      Lists.newArrayList("Regret", "Chunky", "Hindsight", "Tangy"),
      store.getAll(SegmentMeme.class, Segment.class, ImmutableList.of(result.getId()))
        .stream().map(MemeEntity::getName).collect(Collectors.toList()));
    // assert chords
    assertSameItems(Lists.newArrayList("Db minor", "D minor"),
      store.getAll(SegmentChord.class, Segment.class, ImmutableList.of(result.getId()))
        .stream().map(ChordEntity::getName).collect(Collectors.toList()));
    // assert choices
    Collection<SegmentChoice> segmentChoices =
      store.getAll(SegmentChoice.class, Segment.class, ImmutableList.of(result.getId()));
    // assert macro choice
    SegmentChoice macroChoice = SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Macro);
    assertEquals(fake.program3_sequence0_binding0.getId(), macroChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), macroChoice.getTranspose());
    assertEquals(Long.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    // assert main choice
    SegmentChoice mainChoice = SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Main);
    assertEquals(fake.program15_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(-6), mainChoice.getTranspose());
    assertEquals(Long.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }

}
