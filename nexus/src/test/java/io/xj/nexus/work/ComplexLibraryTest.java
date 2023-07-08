// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubTopology;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubClientAccess;
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
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComplexLibraryTest {
  static final Logger LOG = LoggerFactory.getLogger(ComplexLibraryTest.class);
  static final int MARATHON_NUMBER_OF_SEGMENTS = 50;
  static final int MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  static final int MILLIS_PER_SECOND = 1000;
  @Mock
  public HubClient hubClient;
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
  PreviewNexusAdmin previewNexusAdmin;
  long startTime = System.currentTimeMillis();
  AppWorkThread workThread;
  SegmentManager segmentManager;
  CraftWork work;
  @Mock
  LockProvider lockProvider;
  @Mock
  FileStoreProvider fileStoreProvider;

  @Before
  public void setUp() throws Exception {
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    fake.account1 = buildAccount("fish");
    fake.library1 = buildLibrary(fake.account1, "test");
    HubContent content = new HubContent(fake.generatedFixture(3));

    // NOTE: it's critical that the test template has config bufferAheadSeconds=9999 in order to ensure the test fabricates far ahead
    var template = content.getTemplate();
    template.setShipKey("complex_library_test");
    template.setConfig("bufferAheadSeconds=9999\noutputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n");
    content.put(template);

    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    var store = new NexusEntityStoreImpl(entityFactory);
    segmentManager = new SegmentManagerImpl(entityFactory, store);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    var fabricatorFactory = new FabricatorFactoryImpl(
      segmentManager,
      jsonapiPayloadFactory,
      jsonProvider);
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
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl(1, 1);

    // work
    work = new CraftWorkImpl(
      craftFactory,
      entityFactory,
      fabricatorFactory,
      fileStoreProvider,
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
      "playback",
      "complex_library_test",
      "production",
      false,
      "/tmp",
      86400);

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
    assertTrue(hasSegmentsDubbedPastMinimumOffset());
  }

  /**
   * Whether this test is within the time limit
   *
   * @return true if within time limit
   */
  boolean isWithinTimeLimit() {
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
  boolean hasSegmentsDubbedPastMinimumOffset() {
    try {
      var chain = work.getChain();
      if (chain.isEmpty())
        return false;
      return segmentManager.readLastCraftedSegment(HubClientAccess.internal(), chain.get().getId())
        .filter(value -> MARATHON_NUMBER_OF_SEGMENTS <= value.getOffset()).isPresent();

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException ignored) {
      return false;
    }
  }
}
