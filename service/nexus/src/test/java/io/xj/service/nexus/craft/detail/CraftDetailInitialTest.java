// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.detail;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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
import io.xj.lib.entity.EntityFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.NexusIntegrationTestingFixtures;
import io.xj.service.nexus.craft.CraftFactory;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.nexus.persistence.NexusEntityStore;
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

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftDetailInitialTest {
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private NexusEntityStore store;

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Mock
  public HubClient hubClient;
  private Segment segment6;

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
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    when(hubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(Streams.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream(),
        fake.setupFixtureB3().stream(),
        fake.setupFixtureB4_Detail().stream()
      ).collect(Collectors.toList())));

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    var chain2 = store.put(Chain.newBuilder()
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

    // segment crafting
    segment6 = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain2.getId())
      .setOffset(0L)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:07.384616Z")
      .setKey("C minor")
      .setTotal(16)
      .setDensity(0.55)
      .setTempo(130.0)
      .setStorageKey("chains-1-segments-9f7s89d8a7892.wav")
      .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment6.getId())
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program4.getId())
      .setProgramId(fake.program4_sequence0_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program4_sequence0_binding0.getId())
      .setProgramType(Program.Type.Macro)
      .setTranspose(0)
      .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment6.getId())
      .setId(UUID.randomUUID().toString())
      .setProgramId(fake.program5.getId())
      .setProgramId(fake.program5_sequence0_binding0.getProgramId())
      .setProgramSequenceBindingId(fake.program5_sequence0_binding0.getId())
      .setProgramType(Program.Type.Main)
      .setTranspose(-6)
      .build());
    for (String memeName : ImmutableList.of("Special", "Wild", "Pessimism", "Outlook"))
      store.put(SegmentMeme.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(segment6.getId())
        .setName(memeName)
        .build());
    store.put(SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment6.getId())
      .setPosition(0.0)
      .setName("C minor")
      .build());
    store.put(SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment6.getId())
      .setPosition(8.0)
      .setName("Db minor")
      .build());
  }

  @After
  public void tearDown() {

  }

  @Test
  public void craftDetailInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment6);

    craftFactory.detail(fabricator).doWork();

    // assert choice of detail-type sequence
    Collection<SegmentChoice> segmentChoices =
      store.getAll(SegmentChoice.class, Segment.class, ImmutableList.of(segment6.getId()));
    assertNotNull(SegmentDAO.findFirstOfType(segmentChoices, Program.Type.Detail));
  }
}
