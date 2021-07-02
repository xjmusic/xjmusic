// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentMeme;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.testing.NexusTestConfiguration;
import io.xj.nexus.work.NexusWorkModule;
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
public class CraftRhythmProgramVoiceInitialTest {
  private Chain chain2;
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;
  private Segment segment6;

  @Mock
  public HubClient hubClient;

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

    // force known rhythm selection by destroying program 35
    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB3().stream())
        .filter(entity -> !Entities.isSame(entity, fake.program35) && !Entities.isChild(entity, fake.program35))
        .collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
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
  }

  @Test
  public void craftRhythmVoiceInitial() throws Exception {
    insertSegments();

    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment6);

    craftFactory.rhythm(fabricator).doWork();

    Segment result = store.getSegment(segment6.getId()).orElseThrow();
    assertFalse(store.getAll(result.getId(), SegmentChoice.class).isEmpty());
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedBleep = 0;
    int pickedToot = 0;
    Collection<SegmentChoiceArrangementPick> picks = fabricator.getPicks();
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
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), sourceMaterial, segment6);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the rhythm choice only if specified
   */
  private void insertSegments() throws NexusException {
    // segment crafted
    Segment segment5 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain2.getId())
      .setOffset(2L)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-02-14T12:01:07.384616Z")
      .setEndAt("2017-02-14T12:01:27.384616Z")
      .setKey("D Major")
      .setTotal(32)
      .setDensity(0.55)
      .setTempo(130.0)
      .setStorageKey("chains-1-segments-0970305977172.wav")
      .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment5.getId())
      .setProgramId(fake.program4.getId())
      .setProgramId(fake.program4_sequence0_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .setProgramType(Program.Type.Macro)
            .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment5.getId())
      .setProgramId(fake.program5.getId())
      .setProgramId(fake.program5_sequence0_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program5_sequence0_binding0.getId())
      .setProgramType(Program.Type.Main)
            .build());

    // segment crafting
    segment6 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain2.getId())
      .setOffset(3L)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:07.384616Z")
      .setKey("D Major")
      .setTotal(32)
      .setDensity(0.55)
      .setTempo(130.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment6.getId())
      .setProgramId(fake.program4.getId())
      .setProgramId(fake.program4_sequence0_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .setProgramType(Program.Type.Macro)
            .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment6.getId())
      .setProgramId(fake.program5.getId())
      .setProgramId(fake.program5_sequence0_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program5_sequence0_binding0.getId())
      .setProgramType(Program.Type.Main)
            .build());
    for (String memeName : ImmutableList.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(SegmentMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(segment6.getId()).setName(memeName)
        .build());

    store.put(SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment6.getId()).setPosition(0.0).setName("C minor")
      .build());
    store.put(SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment6.getId()).setPosition(8.0).setName("Db minor")
      .build());
  }

}
