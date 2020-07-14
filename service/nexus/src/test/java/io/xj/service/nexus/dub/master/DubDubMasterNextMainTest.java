// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub.master;

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
import io.xj.service.nexus.dub.DubFactory;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainBinding;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.ChainType;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentChoice;
import io.xj.service.nexus.entity.SegmentChoiceArrangement;
import io.xj.service.nexus.entity.SegmentChord;
import io.xj.service.nexus.entity.SegmentMeme;
import io.xj.service.nexus.entity.SegmentState;
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

import java.time.Instant;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DubDubMasterNextMainTest {
  private DubFactory dubFactory;
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
    Injector injector = AppConfiguration.inject(config,
      ImmutableSet.of(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(HubClient.class).toInstance(hubClient);
          }
        })));
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    dubFactory = injector.getInstance(DubFactory.class);
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

    // Chain "Test Print #1" has this segment that was just dubbed
    segment3 = store.put(Segment.create(chain1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "F Major", 64, 0.30, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    store.put(SegmentChoice.create(segment3, ProgramType.Macro, fake.program4_sequence0_binding0, 3));
    store.put(SegmentChoice.create(segment3, ProgramType.Main, fake.program5_sequence1_binding0, -4));

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    segment4 = store.put(Segment.create(chain1, 3, SegmentState.Dubbing, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "G minor", 16, 0.45, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    store.put(SegmentChoice.create(segment4, ProgramType.Macro, fake.program4_sequence1_binding0, 3));
    store.put(SegmentChoice.create(segment4, ProgramType.Main, fake.program15_sequence0_binding0, -5));
    SegmentChoice choice1 = store.put(SegmentChoice.create(segment4, ProgramType.Rhythm, fake.program35, -5));
    store.put(SegmentMeme.create(segment4, "Regret"));
    store.put(SegmentMeme.create(segment4, "Sky"));
    store.put(SegmentMeme.create(segment4, "Hindsight"));
    store.put(SegmentMeme.create(segment4, "Tropical"));
    store.put(SegmentChord.create(segment4, 0.0, "G minor"));
    store.put(SegmentChord.create(segment4, 8.0, "Ab minor"));
    store.put(SegmentChoiceArrangement.create(choice1, fake.program35_voice0, fake.instrument8));

    // future: insert arrangement of choice
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2
  }

  @After
  public void tearDown() {

  }


  @Test
  public void dubMasterNextMain() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment4);

    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

}
