// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubContent;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.http.HttpClientProviderImpl;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.InputMode;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.OutputMode;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.persistence.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.xj.nexus.HubIntegrationTestingFixtures.buildAccount;
import static io.xj.nexus.HubIntegrationTestingFixtures.buildLibrary;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComplexLibraryTest {
  static final Logger LOG = LoggerFactory.getLogger(ComplexLibraryTest.class);
  static final int MARATHON_NUMBER_OF_SEGMENTS = 50;
  static final int MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  static final int MILLIS_PER_SECOND = 1000;
  @Mock
  public HubClient hubClient;
  @Mock
  public NotificationProvider notificationProvider;
  @Mock
  public TelemetryProvider telemetryProvider;
  long startTime = System.currentTimeMillis();
  AppWorkThread workThread;
  SegmentManager segmentManager;
  CraftWork work;
  @Mock
  FileStoreProvider fileStoreProvider;

  @BeforeEach
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

    // Dependencies
    CraftFactory craftFactory = new CraftFactoryImpl();
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
      store,
      notificationProvider,
      segmentManager,
      telemetryProvider,
      InputMode.PRODUCTION,
      OutputMode.PLAYBACK,
      "complex_library_test",
      false,
      "/tmp",
      86400);

    workThread = new AppWorkThread(work);
  }

  @Test
  public void fabricatesManySegments() throws Exception {
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
   Whether this test is within the time limit

   @return true if within time limit
   */
  boolean isWithinTimeLimit() {
    if (MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > System.currentTimeMillis() - startTime)
      return true;
    LOG.error("EXCEEDED TEST TIME LIMIT OF {} SECONDS", MAXIMUM_TEST_WAIT_SECONDS);
    return false;
  }

  /**
   Does the specified chain contain at least N segments?

   @return true if it has at least N segments
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
