// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubTopology;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubClientException;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.*;
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
import static org.mockito.Mockito.*;

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
  public TelemetryProvider telemetryProvider;
  long startTime = System.currentTimeMillis();
  private AppWorkThread workThread;
  private ChainManager chainManager;
  private SegmentManager segmentManager;
  private NexusWork work;
  private HubContent content;

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

    Environment env = Environment.from(ImmutableMap.of(
      "APP_PORT", "9043",
      "SHIP_KEY", content.getTemplate().getShipKey(),
      "WORK_REHYDRATION_ENABLED", "false",
      "WORK_ERASE_SEGMENTS_OLDER_THAN_SECONDS", String.valueOf(MAXIMUM_TEST_WAIT_SECONDS + 300),
      "WORK_CYCLE_MILLIS", "50"
    ));
    var injector = Guice.createInjector(Modules.override(new NexusWorkModule())
      .with(new AbstractModule() {
        @Override
        public void configure() {
          bind(Environment.class).toInstance(env);
          bind(FileStoreProvider.class).toInstance(fileStoreProvider);
          bind(HttpClientProvider.class).toInstance(httpClientProvider);
          bind(HubClient.class).toInstance(hubClient);
          bind(TelemetryProvider.class).toInstance(telemetryProvider);
        }
      }));
    segmentManager = injector.getInstance(SegmentManager.class);
    chainManager = injector.getInstance(ChainManager.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    NexusEntityStore test = injector.getInstance(NexusEntityStore.class);
    test.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    when(hubClient.load(any())).thenReturn(content);
    when(httpClientProvider.getClient()).thenReturn(httpClient);
    when(httpClient.execute(any())).thenReturn(httpResponse);
    when(httpResponse.getEntity()).thenReturn(httpResponseEntity);

    work = injector.getInstance(NexusWork.class);

    workThread = new AppWorkThread(work);
  }

  @Test
  public void fabricatesManySegments() throws Exception {
    when(fileStoreProvider.generateKey(any(), any()))
      .thenReturn("chains-1-segments-12345.wav");
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
      .putS3ObjectFromTempFile(any(), any(), any(), any());
    // FUTURE use a spy to assert actual json payload shipped to S3 for metadata
    verify(fileStoreProvider, atLeast(MARATHON_NUMBER_OF_SEGMENTS))
      .putS3ObjectFromString(any(), any(), any(), any(), any());
    assertTrue(hasSegmentsDubbedPastMinimumOffset());
  }

  /**
   Whether this test is within the time limit

   @return true if within time limit
   */
  private boolean isWithinTimeLimit() {
    if (MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > System.currentTimeMillis() - startTime)
      return true;
    LOG.error("EXCEEDED TEST TIME LIMIT OF {} SECONDS", MAXIMUM_TEST_WAIT_SECONDS);
    return false;
  }

  /**
   Does the specified chain contain at least N segments?

   @return true if it has at least N segments
   */
  private boolean hasSegmentsDubbedPastMinimumOffset() {
    try {
      var chain = chainManager.readOneByShipKey(content.getTemplate().getShipKey());
      return segmentManager.readLastDubbedSegment(HubClientAccess.internal(), chain.getId())
        .filter(value -> MARATHON_NUMBER_OF_SEGMENTS <= value.getOffset()).isPresent();

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException | HubClientException ignored) {
      return false;
    }
  }
}
