// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dub.master;

import com.google.common.collect.Streams;
import io.xj.hub.HubTopology;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.entity.EntityFactoryImpl;
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
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.ChainManagerImpl;
import io.xj.nexus.persistence.FilePathProviderImpl;
import io.xj.nexus.persistence.NexusEntityStore;
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
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoiceArrangement;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoiceArrangementPick;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentMeme;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DubDubMasterWaveformPrerollTest {
  @Mock
  public FileStoreProvider fileStoreProvider;
  @Mock
  public NotificationProvider notificationProvider;
  @Mock
  public HubClient hubClient;
  @Mock
  public DubAudioCache dubAudioCache;
  private DubFactory dubFactory;
  private FabricatorFactory fabricatorFactory;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private Segment segment6;
  @Mock
  private MixerFactory mixerFactory;
  @Mock
  private Mixer mixer;

  @Before
  public void setUp() throws Exception {
    AppEnvironment env = AppEnvironment.getDefault();
    JsonProvider jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    store = new NexusEntityStoreImpl(entityFactory);
    SegmentManager segmentManager = new SegmentManagerImpl(entityFactory, store);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    ChainManager chainManager = new ChainManagerImpl(
      env,
      entityFactory,
      store,
      segmentManager,
      notificationProvider
    );
    var filePathProvider = new FilePathProviderImpl(env);
    fabricatorFactory = new FabricatorFactoryImpl(
      env,
      chainManager,
      segmentManager,
      jsonapiPayloadFactory,
      jsonProvider,
      filePathProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store.deleteAll();

    // Setup mixer and dub factory
    when(mixerFactory.createMixer(any())).thenReturn(mixer);
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl(env);
    DubAudioCacheItemFactory cacheItemFactory = new DubAudioCacheItemFactoryImpl(env, httpClientProvider);
    dubAudioCache = new DubAudioCacheImpl(env, cacheItemFactory);
    dubFactory = new DubFactoryImpl(env, dubAudioCache, filePathProvider, fileStoreProvider, mixerFactory);

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

    segment6 = store.put(buildSegment(chain2, 0, SegmentState.DUBBING, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:07.384616Z"), "C minor", 16, 0.55, 130, "chains-1-segments-9f7s89d8a7892", "wav"));
    store.put(buildSegmentChoice(segment6, ProgramType.Macro, fake.program4_sequence0_binding0));
    store.put(buildSegmentChoice(segment6, ProgramType.Main, fake.program5_sequence0_binding0));
    SegmentChoice choice1 = store.put(buildSegmentChoice(segment6, fake.program35, fake.program35_sequence0, fake.program35_voice0, fake.instrument8));
    store.put(buildSegmentMeme(segment6, "Special"));
    store.put(buildSegmentMeme(segment6, "Wild"));
    store.put(buildSegmentMeme(segment6, "Pessimism"));
    store.put(buildSegmentMeme(segment6, "Outlook"));
    var chord0 = buildSegmentChord(segment6, 0.0, "A minor");
    store.put(chord0);
    store.put(buildSegmentChord(segment6, 8.0, "D major"));
    SegmentChoiceArrangement arr1 = store.put(buildSegmentChoiceArrangement(choice1));
    store.put(buildSegmentChoiceArrangementPick(arr1, fake.program35_sequence0_pattern0_event0, fake.instrument8_audio8kick, "BOOM"));

    // future: insert arrangement of choice1
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2
  }

  @After
  public void tearDown() {

  }

  @Test
  public void dubMaster_hasWaveformPrerollBeforeSegmentStart() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment6);

    dubFactory.master(fabricator).doWork();

    Segment resultSegment = store.getSegment(segment6.getId()).orElseThrow();
    assertEquals(0.01, resultSegment.getWaveformPreroll(), 0.01);
    // future test: success of dub master continue test
  }
}
