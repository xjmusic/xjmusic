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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DubDubMasterInitialTest {
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
  private Segment segment6;

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
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));

    // Chain "Print #2" has 1 initial segment in dubbing state - DubMaster is complete
    Chain chain2 = store.put(buildChain(
      fake.account1,
      fake.template1,
      "Print #2",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2014-08-12T12:17:02.527142Z")));

    segment6 = store.put(NexusIntegrationTestingFixtures.buildSegment(chain2, 0, SegmentState.DUBBING, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:07.384616Z"), "C minor", 16, 0.55, 130, "chains-1-segments-9f7s89d8a7892", "wav"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment6, ProgramType.Macro, fake.program4_sequence0_binding0));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment6, ProgramType.Main, fake.program5_sequence0_binding0));
    SegmentChoice choice1 = store.put(NexusIntegrationTestingFixtures.buildSegmentChoice(segment6, fake.program35, fake.program35_sequence0, fake.program35_voice0, fake.instrument8));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment6, "Special"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment6, "Wild"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment6, "Pessimism"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment6, "Outlook"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment6, 0.0, "A minor"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment6, 8.0, "D major"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoiceArrangement(choice1));

    // future: insert arrangement of choice1
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2
  }

  @After
  public void tearDown() {

  }

  @Test
  public void dubMasterInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment6);

    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

}
