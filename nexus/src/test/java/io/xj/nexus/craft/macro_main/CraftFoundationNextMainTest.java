// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChord;
import io.xj.SegmentMeme;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.testing.NexusTestConfiguration;
import io.xj.nexus.work.NexusWorkModule;
import io.xj.hub.HubApp;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubClientAccess;
import io.xj.hub.client.HubContent;
import io.xj.nexus.NexusApp;
import io.xj.nexus.persistence.NexusEntityStore;
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

import static io.xj.lib.util.Assert.assertSameItems;
import static io.xj.nexus.NexusIntegrationTestingFixtures.makeChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.makeSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.makeSegmentChoice;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftFoundationNextMainTest {
  private Injector injector;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private NexusIntegrationTestingFixtures fake;
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
    Config config = NexusTestConfiguration.getDefault()
            .withValue("program.doTranspose", ConfigValueFactory.fromAnyRef(true))
            .withValue("instrument.isTonal", ConfigValueFactory.fromAnyRef(true));
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
                    fake.setupFixtureB2().stream()
            ).collect(Collectors.toList())));

    // Chain "Test Print #1" has 5 total segments
    chain1 = store.put(NexusIntegrationTestingFixtures.makeChain(fake.account1, "Test Print #1", Chain.Type.Production, Chain.State.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
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

    // Chain "Test Print #1" has this segment that was just crafted
    segment3 = store.put(Segment.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setChainId(chain1.getId())
            .setOffset(2L)
            .setState(Segment.State.Crafted)
            .setBeginAt("2017-02-14T12:02:04.000001Z")
            .setEndAt("2017-02-14T12:02:36.000001Z")
            .setKey("F Major")
            .setType(Segment.Type.Continue)
            .setTotal(64)
            .setDensity(0.30)
            .setTempo(120.0)
            .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
            .build());
    store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment3, Program.Type.Macro, fake.program4_sequence0_binding0));
    store.put(NexusIntegrationTestingFixtures.makeSegmentChoice(segment3, Program.Type.Main, fake.program5_sequence1_binding0));

    // Chain "Test Print #1" has a planned segment
    segment4 = store.put(NexusIntegrationTestingFixtures.makeSegment(chain1, 3, Segment.State.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"), null, "C", 8, 0.8, 120, "chain-1-waveform-12345", "wav"));
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftFoundationNextMain() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = store.getSegment(segment4.getId()).orElseThrow();
    assertEquals(Segment.Type.NextMain, result.getType());
    assertEquals("2017-02-14T12:03:15.840157Z", result.getEndAt());
    assertEquals(16, result.getTotal());
    assertEquals("AAC", result.getOutputEncoder());
    assertEquals(0.45, result.getDensity(), 0.01);
    assertEquals("G minor", result.getKey());
    assertEquals(125, result.getTempo(), 0.01);
    // assert memes
    assertSameItems(Lists.newArrayList("Hindsight", "Tropical", "Cozy", "Wild", "Regret"),
            Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
    // assert chords
    assertSameItems(Lists.newArrayList("G minor", "Ab minor"),
            Entities.namesOf(store.getAll(result.getId(), SegmentChord.class)));
    // assert choices
    Collection<SegmentChoice> segmentChoices =
            store.getAll(result.getId(), SegmentChoice.class);
    // assert macro choice
    SegmentChoice macroChoice = SegmentDAO.findFirstOfType(segmentChoices, Program.Type.Macro);
    assertEquals(fake.program4_sequence1_binding0.getId(), macroChoice.getProgramSequenceBindingId());
        assertEquals(Long.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    // assert main choice
    SegmentChoice mainChoice = SegmentDAO.findFirstOfType(segmentChoices, Program.Type.Main);
    assertEquals(fake.program15_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
        assertEquals(Long.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }

  /**
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance
   */
  @Test
  public void craftFoundationNextMain_revertsAndRequeuesOnFailure() throws Exception {
    // Chain "Test Print #1" has a dangling (preceded by another planned segment) planned segment
    Segment segment5 = store.put(Segment.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setChainId(chain1.getId())
            .setOffset(4L)
            .setState(Segment.State.Planned)
            .setBeginAt("2017-02-14T12:03:08.000001Z")
            .setKey("C")
            .setTotal(8)
            .setDensity(0.8)
            .setTempo(120)
            .setStorageKey("chain-1-waveform-12345.wav")
            .build());

    failure.expect(NexusException.class);

    fabricatorFactory.fabricate(HubClientAccess.internal(), segment5);
  }

}
