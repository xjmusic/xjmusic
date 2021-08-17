// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.detail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainBinding;
import io.xj.api.ChainBindingType;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.api.InstrumentType;
import io.xj.api.ProgramType;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.dao.Segments;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.testing.NexusTestConfiguration;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class CraftDetailInitialTest {
  @Mock
  public HubClient hubClient;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private Segment segment6;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault();
    Environment env = Environment.getDefault();
    var injector = AppConfiguration.inject(config, env,
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
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream(),
      fake.setupFixtureB3().stream(),
      fake.setupFixtureB4_DetailBass().stream()
    ).collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    var chain2 = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .name("Print #2")
      .type(ChainType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2014-08-12T12:17:02.527142Z")
    );
    store.put(new ChainBinding()
      .id(UUID.randomUUID())
      .chainId(chain2.getId())
      .targetId(fake.library2.getId())
      .type(ChainBindingType.LIBRARY)
    );

    // segment crafting
    segment6 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain2.getId())
      .type(SegmentType.INITIAL)
      .offset(0L)
      .delta(0)
      .state(SegmentState.CRAFTING)
      .beginAt("2017-02-14T12:01:00.000001Z")
      .endAt("2017-02-14T12:01:07.384616Z")
      .key("C minor")
      .total(16)
      .density(0.55)
      .tempo(130.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
    );
    store.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment6.getId())
      .id(UUID.randomUUID())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programId(fake.program4.getId())
      .programId(fake.program4_sequence0_binding0.getProgramId())
      .programSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .programType(ProgramType.MACRO)
    );
    store.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment6.getId())
      .id(UUID.randomUUID())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programId(fake.program5.getId())
      .programId(fake.program5_sequence0_binding0.getProgramId())
      .programSequenceBindingId(fake.program5_sequence0_binding0.getId())
      .programType(ProgramType.MAIN)
    );
    for (String memeName : ImmutableList.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(new SegmentMeme()
        .id(UUID.randomUUID())
        .segmentId(segment6.getId())
        .name(memeName)
      );
    SegmentChord chord0 = store.put(new SegmentChord()
      .id(UUID.randomUUID())
      .segmentId(segment6.getId())
      .position(0.0)
      .name("C minor")
    );
    store.put(new SegmentChordVoicing()
      .id(UUID.randomUUID())
      .segmentId(segment6.getId())
      .segmentChordId(chord0.getId())
      .type(InstrumentType.BASS)
      .notes("C2, Eb2, G2")
    );
    SegmentChord chord1 = store.put(new SegmentChord()
      .id(UUID.randomUUID())
      .segmentId(segment6.getId())
      .position(8.0)
      .name("Db minor")
    );
    store.put(new SegmentChordVoicing()
      .id(UUID.randomUUID())
      .segmentId(segment6.getId())
      .segmentChordId(chord1.getId())
      .type(InstrumentType.BASS)
      .notes("Db2, E2, Ab2")
    );
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftDetailInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment6);

    craftFactory.detail(fabricator).doWork();

    // assert choice of detail-type sequence
    Collection<SegmentChoice> choices = store.getAll(segment6.getId(), SegmentChoice.class);
    assertNotNull(Segments.findFirstOfType(choices, ProgramType.DETAIL));

    // [#154464276] Detail Craft v1 -- segment chords voicings belong to chords and segments
    Collection<SegmentChordVoicing> voicings = store.getAll(segment6.getId(), SegmentChordVoicing.class);
    assertEquals(2, voicings.size());
  }
}
