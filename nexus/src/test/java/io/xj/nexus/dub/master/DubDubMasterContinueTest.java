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
import io.xj.lib.mixer.InternalResource;
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
import io.xj.nexus.persistence.FilePathProviderImpl;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.nexus.persistence.Segments;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.time.Instant;
import java.util.stream.Collectors;

import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DubDubMasterContinueTest {
  private static final String testResourceFilePath = "source_audio" + File.separator + "kick1.wav";
  @Mock
  public FileStoreProvider fileStoreProvider;
  @Mock
  public NotificationProvider notificationProvider;
  @Mock
  public HubClient hubClient;
  @Mock
  public Mixer mixer;
  @Mock
  public MixerFactory mixerFactory;
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
    DubAudioCache dubAudioCache = new DubAudioCacheImpl(env, cacheItemFactory);
    dubFactory = new DubFactoryImpl(env, dubAudioCache, filePathProvider, fileStoreProvider, mixerFactory);

    // Mock request via HubClient returns fake generated library of hub content
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));

    // Chain "Test Print #1" has 5 total segments
    Chain chain1 = store.put(NexusIntegrationTestingFixtures.buildChain(fake.account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, fake.template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
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
      "chains-1-segments-97898a2sdf7892"));
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
      "chains-1-segments-97898a2sdf7892"));

    // Chain "Test Print #1" has this segment that was just dubbed
    Segment segment3 = store.put(buildSegment(
      chain1,
      SegmentType.CONTINUE,
      2,
      2,
      SegmentState.DUBBED,
      Instant.parse("2017-02-14T12:02:04.000001Z"),
      Instant.parse("2017-02-14T12:02:36.000001Z"),
      "F Major",
      64,
      0.30,
      120.0,
      "chains-1-segments-9f7s89d8a7892.wav"));
    store.put(buildSegmentChoice(
      segment3,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program4,
      fake.program4_sequence1_binding0));
    store.put(buildSegmentChoice(
      segment3,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence0_binding0));

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    segment4 = store.put(buildSegment(chain1, 3, SegmentState.DUBBING, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "D Major", 16, 0.45, 120.0, "chains-1-segments-9f7s89d8a7892", "wav"));
    store.put(buildSegmentChoice(segment4, ProgramType.Macro, fake.program4_sequence1_binding0));
    store.put(buildSegmentChoice(segment4, ProgramType.Main, fake.program5_sequence1_binding0));
    SegmentChoice choice1 = store.put(buildSegmentChoice(segment4, fake.program35, fake.program35_sequence0, fake.program35_voice0, fake.instrument8));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment4, "Cozy"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment4, "Classic"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment4, "Outlook"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentMeme(segment4, "Rosy"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment4, 0.0, "A minor"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChord(segment4, 8.0, "D major"));
    store.put(NexusIntegrationTestingFixtures.buildSegmentChoiceArrangement(choice1));

    // FUTURE: determine new test vector for persist Audio pick in memory https://www.pivotaltracker.com/story/show/154014731
  }

  @After
  public void tearDown() {

  }

  @Test
  public void dubMasterContinue() throws Exception {
    InternalResource testAudioResource = new InternalResource(testResourceFilePath);
    // it's necessary to have two separate streams for this mock of two separate file reads
    FileUtils.openInputStream(testAudioResource.getFile());
    FileUtils.openInputStream(testAudioResource.getFile());

    Fabricator fabricator = fabricatorFactory.fabricate(sourceMaterial, segment4);
    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

}

