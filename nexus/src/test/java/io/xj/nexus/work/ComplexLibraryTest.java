// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubTopology;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubClientAccess;
import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubContent;
import io.xj.hub.service.PreviewNexusAdmin;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.http.HttpClientProviderImpl;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.lock.LockProvider;
import io.xj.lib.mixer.Mixer;
import io.xj.lib.mixer.MixerFactory;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.dub.DubAudioCache;
import io.xj.nexus.dub.DubAudioCacheImpl;
import io.xj.nexus.dub.DubAudioCacheItemFactory;
import io.xj.nexus.dub.DubAudioCacheItemFactoryImpl;
import io.xj.nexus.dub.DubFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.ChainManagerImpl;
import io.xj.nexus.persistence.FilePathProviderImpl;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComplexLibraryTest {
  private static final Logger LOG = LoggerFactory.getLogger(ComplexLibraryTest.class);
  private static final int MARATHON_NUMBER_OF_SEGMENTS = 50;
  private static final int MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  private static final int MILLIS_PER_SECOND = 1000;
  @Mock
  public HubClient hubClient;
  @Mock(lenient = true)
  public FileStoreProvider fileStoreProvider;
  @Mock(lenient = true)
  public HttpClientProvider httpClientProvider;
  @Mock(lenient = true)
  public CloseableHttpClient httpClient;
  @Mock(lenient = true)
  public CloseableHttpResponse httpResponse;
  @Mock(lenient = true)
  public HttpEntity httpResponseEntity;
  @Mock
  public NotificationProvider notificationProvider;
  @Mock
  public TelemetryProvider telemetryProvider;
  @Mock
  private PreviewNexusAdmin previewNexusAdmin;
  @Mock
  private MixerFactory mixerFactory;
  @Mock
  private Mixer mixer;
  long startTime = System.currentTimeMillis();
  private AppWorkThread workThread;
  private ChainManager chainManager;
  private SegmentManager segmentManager;
  private NexusWork work;
  private HubContent content;
  @Mock
  private LockProvider lockProvider;

  @Before
  public void setUp() throws Exception {
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    fake.account1 = buildAccount("fish");
    fake.library1 = buildLibrary(fake.account1, "test");
    content = new HubContent(fake.generatedFixture(3));

    // NOTE: it's critical that the test template has config bufferAheadSeconds=9999 in order to ensure the test fabricates far ahead
    var template = content.getTemplate();
    template.setConfig("bufferAheadSeconds=9999\noutputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n");
    content.put(template);

    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    var store = new NexusEntityStoreImpl(entityFactory);
    segmentManager = new SegmentManagerImpl(entityFactory, store);
    chainManager = new ChainManagerImpl(
      entityFactory,
      store,
      segmentManager,
      notificationProvider, 1, 1
    );
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    var filePathProvider = new FilePathProviderImpl("/tmp/");
    var fabricatorFactory = new FabricatorFactoryImpl(
      chainManager,
      segmentManager,
      jsonapiPayloadFactory,
      jsonProvider,
      filePathProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    NexusEntityStore test = new NexusEntityStoreImpl(entityFactory);
    test.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    when(hubClient.load(any())).thenReturn(content);
    when(httpClientProvider.getClient()).thenReturn(httpClient);
    when(httpClient.execute(any())).thenReturn(httpResponse);
    when(httpResponse.getEntity()).thenReturn(httpResponseEntity);

    // Dependencies
    ApiUrlProvider apiUrlProvider = new ApiUrlProvider("http://localhost:8080/");
    CraftFactory craftFactory = new CraftFactoryImpl(apiUrlProvider);
    when(mixerFactory.createMixer(any())).thenReturn(mixer);
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl(1, 1);
    DubAudioCacheItemFactory cacheItemFactory = new DubAudioCacheItemFactoryImpl(httpClientProvider, "xj-prod-audio", "https://audio.xj.io/");
    DubAudioCache dubAudioCache = new DubAudioCacheImpl(cacheItemFactory, "/tmp/");
    var dubFactory = new DubFactoryImpl(dubAudioCache, filePathProvider, fileStoreProvider, mixerFactory);

    // work
    work = new NexusWorkImpl(
      chainManager,
      craftFactory,
      dubFactory,
      entityFactory,
      fabricatorFactory,
      httpClientProvider,
      hubClient,
      jsonapiPayloadFactory,
      jsonProvider,
      lockProvider,
      store,
      notificationProvider,
      previewNexusAdmin,
      segmentManager,
      telemetryProvider,
      "production",
      content.getTemplate().getShipKey(),
      false,
      1,
      50,
      MAXIMUM_TEST_WAIT_SECONDS + 300,
      1,
      1,
      1,
      false,
      false,
      1,
      false,
      1,
      "https://ship.xj.io/",
      "xj-prod-ship",
      1,
      "");

    workThread = new AppWorkThread(work);
  }

  @Test
  public void fabricatesManySegments() throws Exception {
    when(httpResponseEntity.getContent())
      .thenAnswer((Answer<InputStream>) invocation -> new FileInputStream(Objects.requireNonNull(
        ComplexLibraryTest.class.getClassLoader().getResource("source_audio/kick1.wav")).getFile()));

    // Start app, wait for work, stop app
    workThread.start();
    while (!hasSegmentsDubbedPastMinimumOffset() && isWithinTimeLimit())
      //noinspection BusyWait
      Thread.sleep(MILLIS_PER_SECOND);
    work.finish();

    // assertions
    verify(fileStoreProvider, atLeast(MARATHON_NUMBER_OF_SEGMENTS))
      .putS3ObjectFromTempFile(any(), any(), any(), any(), any());
    assertTrue(hasSegmentsDubbedPastMinimumOffset());
  }

  /**
   * Whether this test is within the time limit
   *
   * @return true if within time limit
   */
  private boolean isWithinTimeLimit() {
    if (MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > System.currentTimeMillis() - startTime)
      return true;
    LOG.error("EXCEEDED TEST TIME LIMIT OF {} SECONDS", MAXIMUM_TEST_WAIT_SECONDS);
    return false;
  }

  /**
   * Does the specified chain contain at least N segments?
   *
   * @return true if it has at least N segments
   */
  private boolean hasSegmentsDubbedPastMinimumOffset() {
    try {
      var chain = chainManager.readOneByShipKey(content.getTemplate().getShipKey());
      return segmentManager.readLastDubbedSegment(HubClientAccess.internal(), chain.getId())
        .filter(value -> MARATHON_NUMBER_OF_SEGMENTS <= value.getOffset()).isPresent();

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException |
             HubClientException ignored) {
      return false;
    }
  }
}
