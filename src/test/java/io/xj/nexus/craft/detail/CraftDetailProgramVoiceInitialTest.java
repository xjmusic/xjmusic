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
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.nexus.NexusException;
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
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class)
public class CraftDetailProgramVoiceInitialTest {
  @Mock
  public HubClient hubClient;
  private Chain chain2;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;
  private Segment segment1;

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

    // force known detail selection by destroying program 35
    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB3().stream(),
      fake.setupFixtureB4_DetailBass().stream())
      .filter(entity -> !Entities.isSame(entity, fake.program35) && !Entities.isChild(entity, fake.program35))
      .collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(fake.account1.getId())
      .config("choiceDeltaEnabled=false")
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
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftDetailVoiceInitial() throws Exception {
    insertSegments();

    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment1);

    craftFactory.detail(fabricator).doWork();

    assertFalse(fabricator.getChoices().isEmpty());
    // test vector for [#154014731] persist Audio pick in memory
    int pickedBloop = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fake.instrument9_audio8.getId()))
        pickedBloop++;
    }
    assertEquals(32, pickedBloop);
  }

  @Test
  public void craftDetailVoiceInitial_okWhenNoDetailChoice() throws Exception {
    insertSegments();
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment1);

    craftFactory.detail(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the detail choice only if specified
   */
  private void insertSegments() throws NexusException {
    // segment crafted
    Segment segment0 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain2.getId())
      .type(SegmentType.INITIAL)
      .offset(0L)
      .state(SegmentState.CRAFTED)
      .beginAt("2017-02-14T12:01:07.384616Z")
      .endAt("2017-02-14T12:01:27.384616Z")
      .key("D Major")
      .total(32)
      .density(0.55)
      .tempo(130.0)
      .storageKey("chains-1-segments-0970305977172.wav")
      );
    store.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment0.getId())
      .programId(fake.program4.getId())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .programType(ProgramType.MACRO)
      );
    store.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment0.getId())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programId(fake.program5.getId())
      .programSequenceBindingId(fake.program5_sequence0_binding0.getId())
      .programType(ProgramType.MAIN)
      );

    // segment crafting
    segment1 = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain2.getId())
      .offset(1L)
      .type(SegmentType.CONTINUE)
      .state(SegmentState.CRAFTING)
      .beginAt("2017-02-14T12:01:00.000001Z")
      .endAt("2017-02-14T12:01:07.384616Z")
      .key("D Major")
      .total(32)
      .density(0.55)
      .tempo(130.0)
      .storageKey("chains-1-segments-9f7s89d8a7892.wav")
      );
    store.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment1.getId())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .id(UUID.randomUUID())
      .programId(fake.program4.getId())
      .programId(fake.program4_sequence0_binding0.getProgramId())
      .programSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .programType(ProgramType.MACRO)
      );
    store.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment1.getId())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .id(UUID.randomUUID())
      .programId(fake.program5.getId())
      .programId(fake.program5_sequence0_binding0.getProgramId())
      .programSequenceBindingId(fake.program5_sequence0_binding0.getId())
      .programType(ProgramType.MAIN)
      );
    for (String memeName : ImmutableList.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(new SegmentMeme()
        .id(UUID.randomUUID())
        .segmentId(segment1.getId()).name(memeName)
        );
    SegmentChord chord0 = store.put(new SegmentChord()
      .id(UUID.randomUUID())
      .segmentId(segment1.getId())
      .position(0.0)
      .name("C minor")
      );
    store.put(new SegmentChordVoicing()
      .id(UUID.randomUUID())
      .segmentId(segment1.getId())
      .segmentChordId(chord0.getId())
      .type(InstrumentType.BASS)
      .notes("C2, Eb2, G2")
      );
    SegmentChord chord1 = store.put(new SegmentChord()
      .id(UUID.randomUUID())
      .segmentId(segment1.getId())
      .position(8.0)
      .name("Db minor")
      );
    store.put(new SegmentChordVoicing()
      .id(UUID.randomUUID())
      .segmentId(segment1.getId())
      .segmentChordId(chord1.getId())
      .type(InstrumentType.BASS)
      .notes("Db2, E2, Ab2")
      );
  }

}
