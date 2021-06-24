// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.macro_main;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChord;
import io.xj.SegmentMeme;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.testing.NexusTestConfiguration;
import io.xj.nexus.work.NexusWorkModule;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.lib.util.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftFoundationInitialTest {
  private Injector injector;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private NexusIntegrationTestingFixtures fake;
  private NexusEntityStore store;
  private Chain chain2;
  private Segment segment6;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Mock
  public HubClient hubClient;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Environment env = Environment.getDefault();
    injector = AppConfiguration.inject(config, env,
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
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    when(hubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(Streams.concat(
        fake.setupFixtureB1().stream()
      ).collect(Collectors.toList())));

    // Chain "Print #2" has 1 initial planned segment
    chain2 = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("Print #2")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2014-08-12T12:17:02.527142Z")
      .build());
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain2.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    segment6 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain2.getId())
      .setOffset(0L)
      .setState(Segment.State.Planned)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setKey("C")
      .setTotal(8)
      .setDensity(0.8)
      .setTempo(120)
      .setStorageKey("chain-1-waveform-12345.wav")
      .build());
  }

  @Test
  public void craftFoundationInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment6);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = store.getSegment(segment6.getId()).orElseThrow();
    assertEquals(segment6.getId(), result.getId());
    assertEquals("AAC", result.getOutputEncoder());
    assertEquals(Segment.Type.Initial, result.getType());
    assertEquals("2017-02-14T12:01:07.384616Z", result.getEndAt());
    assertEquals(16, result.getTotal());
    assertEquals(0.55, result.getDensity(), 0.01);
    assertEquals("G major", result.getKey());
    assertEquals(130.0, result.getTempo(), 0.01);
    // assert memes
    assertSameItems(
      Lists.newArrayList("Tropical", "Wild", "Outlook", "Optimism"),
      Entities.namesOf(store.getAll(result.getId(), SegmentMeme.class)));
    // assert chords
    assertSameItems(Lists.newArrayList("G major", "Ab minor"),
      Entities.namesOf(store.getAll(result.getId(), SegmentChord.class)));
    // assert choices
    Collection<SegmentChoice> segmentChoices =
      store.getAll(result.getId(), SegmentChoice.class);
    SegmentChoice macroChoice = SegmentDAO.findFirstOfType(segmentChoices, Program.Type.Macro);
    assertEquals(fake.program4_sequence0_binding0.getId(), macroChoice.getProgramSequenceBindingId());
        assertEquals(Long.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    SegmentChoice mainChoice = SegmentDAO.findFirstOfType(segmentChoices, Program.Type.Main);
    assertEquals(fake.program5_sequence0_binding0.getId(), mainChoice.getProgramSequenceBindingId());
        assertEquals(Long.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }
}
