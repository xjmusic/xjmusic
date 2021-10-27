// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import com.google.common.collect.ImmutableMap;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.hub.HubTopology;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.NexusApp;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.*;
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
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
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
  @Mock
  public TelemetryProvider telemetryProvider;
  long startTime = System.currentTimeMillis();
  private AppWorkThread workThread;
  private Chain chain1;
  private NexusApp app;
  private SegmentManager segmentManager;

  @Before
  public void setUp() throws Exception {
    Environment env = Environment.from(ImmutableMap.of(
      "APP_PORT", "9043",
      "WORK_CHAIN_MANAGEMENT_ENABLED", "false",
      "WORK_ERASE_SEGMENTS_OLDER_THAN_SECONDS", String.valueOf(MAXIMUM_TEST_WAIT_SECONDS + 300),
      "WORK_CYCLE_MILLIS", "50"
    ));
    var injector = Guice.createInjector(Modules.override(new NexusWorkModule())
      .with(new AbstractModule() {
        @Override
        public void configure() {
          bind(Environment.class).toInstance(env);
          bind(FileStoreProvider.class).toInstance(fileStoreProvider);
          bind(HubClient.class).toInstance(hubClient);
          bind(TelemetryProvider.class).toInstance(telemetryProvider);
        }
      }));
    segmentManager = injector.getInstance(SegmentManager.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    NexusEntityStore test = injector.getInstance(NexusEntityStore.class);
    test.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    fake.account1 = buildAccount("fish");
    fake.library1 = buildLibrary(fake.account1, "test");
    var content = new HubContent(fake.generatedFixture(3));
    when(hubClient.ingest(any(), any())).thenReturn(content);

    // Chain "Test Print #1" is ready to begin
    chain1 = test.put(buildChain(
      fake.account1,
      content.getTemplate(),
      "Test Print #1",
      ChainType.PREVIEW,
      ChainState.FABRICATE,
      Instant.now().minusSeconds(MAXIMUM_TEST_WAIT_SECONDS)));

    app = injector.getInstance(NexusApp.class);

    workThread = new AppWorkThread(app);
  }

  @Test
  public void fabricatesManySegments() throws Exception {
    when(fileStoreProvider.generateKey(any(), any()))
      .thenReturn("chains-1-segments-12345.wav");
    when(fileStoreProvider.streamS3Object(any(), any()))
      .thenAnswer((Answer<InputStream>) invocation -> new FileInputStream(Objects.requireNonNull(
        ComplexLibraryTest.class.getClassLoader().getResource("source_audio/kick1.wav")).getFile()));

    // Start app, wait for work, stop app
    app.start();
    workThread.start();
    while (!hasSegmentsDubbedPastMinimumOffset(chain1.getId()) && isWithinTimeLimit())
      //noinspection BusyWait
      Thread.sleep(MILLIS_PER_SECOND);
    app.finish();
    workThread.interrupt();

    // assertions
    verify(fileStoreProvider, atLeast(MARATHON_NUMBER_OF_SEGMENTS))
      .putS3ObjectFromTempFile(any(), any(), any());
    // FUTURE use a spy to assert actual json payload shipped to S3 for metadata
    verify(fileStoreProvider, atLeast(MARATHON_NUMBER_OF_SEGMENTS))
      .putS3ObjectFromString(any(), any(), any(), any());
    assertTrue(hasSegmentsDubbedPastMinimumOffset(chain1.getId()));
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
   Does a specified Chain contain at least N segments?

   @param chainId to test
   @return true if it has at least N segments
   */
  private boolean hasSegmentsDubbedPastMinimumOffset(UUID chainId) {
    try {
      return segmentManager.readLastDubbedSegment(HubClientAccess.internal(), chainId)
        .filter(value -> MARATHON_NUMBER_OF_SEGMENTS <= value.getOffset()).isPresent();

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException ignored) {
      return false;
    }
  }
}
