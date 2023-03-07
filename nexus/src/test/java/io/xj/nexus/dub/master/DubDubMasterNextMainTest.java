// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub.master;

import com.google.common.collect.Streams;
import io.xj.hub.HubTopology;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.entity.EntityStoreImpl;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.http.HttpClientProviderImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.mixer.Mixer;
import io.xj.lib.mixer.MixerFactory;
import io.xj.lib.notification.NotificationProvider;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.dub.DubAudioCache;
import io.xj.nexus.dub.DubAudioCacheImpl;
import io.xj.nexus.dub.DubAudioCacheItemFactory;
import io.xj.nexus.dub.DubAudioCacheItemFactoryImpl;
import io.xj.nexus.dub.DubFactory;
import io.xj.nexus.dub.DubFactoryImpl;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.ChainManagerImpl;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.stream.Collectors;

import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoiceArrangement;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DubDubMasterNextMainTest {
  @Mock
  public HubClient hubClient;
  @Mock
  public Mixer mixer;
  @Mock
  public MixerFactory mixerFactory;
  @Mock
  public FileStoreProvider fileStoreProvider;
  @Mock
  public NotificationProvider notificationProvider;
  private DubFactory dubFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    AppEnvironment env = AppEnvironment.getDefault();
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    var store = new NexusEntityStoreImpl(entityFactory);
    SegmentManager segmentManager = new SegmentManagerImpl(entityFactory, store);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    ChainManager chainManager = new ChainManagerImpl(
      env,
      entityFactory,
      store,
      segmentManager,
      notificationProvider
    );
    EntityStore entityStore = new EntityStoreImpl();
    fabricatorFactory = new FabricatorFactoryImpl(
      env,
      chainManager,
            segmentManager,
      jsonapiPayloadFactory,
      jsonProvider
    );
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store.deleteAll();

    // Setup mixer and dub factory
    when(mixerFactory.createMixer(any())).thenReturn(mixer);
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl(env);
    DubAudioCacheItemFactory cacheItemFactory = new DubAudioCacheItemFactoryImpl(env, httpClientProvider);
    DubAudioCache dubAudioCache = new DubAudioCacheImpl(env, cacheItemFactory);
    dubFactory = new DubFactoryImpl(env, dubAudioCache, fileStoreProvider, mixerFactory);

    // Mock request via HubClient returns fake generated library of hub content
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream(),
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    store.put(buildSegment(
      chain1,
      SegmentType.INITIAL,
      0,
      0,
      SegmentState.DUBBED,
      Instant.parse("2017-02-14T12:01:00.000001Z"),
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      "D major",
      64,
      0.73,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      "wav"));
    store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      1,
      1,
      SegmentState.DUBBING,
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      Instant.parse("2017-02-14T12:02:04.000001Z"),
      "Db minor",
      64,
      0.85,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      "wav"));

    // Chain "Test Print #1" has this segment that was just dubbed
    Segment segment3 = store.put(NexusIntegrationTestingFixtures.buildSegment(chain1, 2, SegmentState.DUBBED, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "F Major", 64, 0.30, 120.0, "chains-1-segments-9f7s89d8a7892", "wav"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.Macro, fake.program4_sequence0_binding0));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment3, ProgramType.Main, fake.program5_sequence1_binding0));

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    segment4 = store.put(NexusIntegrationTestingFixtures.buildSegment(chain1, 3, SegmentState.DUBBING, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "G minor", 16, 0.45, 120.0, "chains-1-segments-9f7s89d8a7892", "wav"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment4, ProgramType.Macro, fake.program4_sequence1_binding0));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment4, ProgramType.Main, fake.program15_sequence0_binding0));
    SegmentChoice choice1 = store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment4, fake.program35, fake.program35_sequence0, fake.program35_voice0, fake.instrument8));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment4, "Regret"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment4, "Sky"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment4, "Hindsight"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment4, "Tropical"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment4, 0.0, "G minor"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment4, 8.0, "Ab minor"));
    store.put(buildSegmentChoiceArrangement(choice1));

    // future: insert arrangement of choice
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2
  }

  @After
  public void tearDown() {

  }


  @Test
  public void dubMasterNextMain() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4);

    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

}
