// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubContent;
import io.xj.hub.enums.UserRoleType;
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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildAccount;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildLibrary;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ComplexLibraryTest {
  static final Logger LOG = LoggerFactory.getLogger(ComplexLibraryTest.class);
  static final int MARATHON_NUMBER_OF_SEGMENTS = 50;
  static final int MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  static final int MILLIS_PER_SECOND = 1000;
  private static final int GENERATED_FIXTURE_COMPLEXITY = 3;
  private final static String audioBaseUrl = "https://audio.xj.io/";
  private final static String shipBaseUrl = "https://ship.xj.io/";
  private final static String hubBaseUrl = "https://lab.xj.io/";
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

  @Mock
  RunnableFuture<HubContent> hubContentProvider;

  @BeforeEach
  public void setUp() throws Exception {
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    fake.account1 = buildAccount("fish");
    fake.library1 = buildLibrary(fake.account1, "test");
    var generatedFixtures = fake.generatedFixture(GENERATED_FIXTURE_COMPLEXITY);
    HubContent content = new HubContent(generatedFixtures.stream().filter(Objects::nonNull).toList());

    var template = content.getTemplate();
    template.setShipKey("complex_library_test");
    template.setConfig("outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n");
    content.put(template);

    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    var store = new NexusEntityStoreImpl(entityFactory);
    segmentManager = new SegmentManagerImpl(store);
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
    // todo mock runnable future when(hubClient.load("complex_library_test", audioBaseUrl)).thenReturn(content);

    // Dependencies
    CraftFactory craftFactory = new CraftFactoryImpl();
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl(1, 1);

    // Access
    var access = new HubClientAccess(List.of(UserRoleType.Internal));

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
      access,
      hubContentProvider,
      hubBaseUrl,
      audioBaseUrl,
      shipBaseUrl,
      InputMode.PRODUCTION,
      OutputMode.PLAYBACK,
      "complex_library_test",
      false,
      "/tmp",
      86400,
      48000.0, 2, 999999
    );

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
      return segmentManager.readLastCraftedSegment(new HubClientAccess(List.of(UserRoleType.Internal)))
        .filter(value -> MARATHON_NUMBER_OF_SEGMENTS <= value.getId()).isPresent();

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException ignored) {
      return false;
    }
  }
}
