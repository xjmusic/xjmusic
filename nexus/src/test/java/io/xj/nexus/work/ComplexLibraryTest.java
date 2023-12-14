// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubContent;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.audio_cache.AudioCache;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.json.JsonProviderImpl;
import io.xj.nexus.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.persistence.*;
import io.xj.nexus.telemetry.Telemetry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildAccount;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildLibrary;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class ComplexLibraryTest {
  static final Logger LOG = LoggerFactory.getLogger(ComplexLibraryTest.class);
  static final int MARATHON_NUMBER_OF_SEGMENTS = 50;
  static final int MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  static final int MILLIS_PER_SECOND = 1000;
  private static final int GENERATED_FIXTURE_COMPLEXITY = 3;
  private static final long WORK_CYCLE_MILLIS = 120;
  long startTime = System.currentTimeMillis();
  SegmentManager segmentManager;
  CraftWork work;

  @Mock
  public HubClient hubClient;

  @Mock
  Telemetry telemetry;

  @Mock
  AudioCache audioCache;

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

    // Dependencies
    CraftFactory craftFactory = new CraftFactoryImpl();

    // work
    work = new CraftWorkImpl(
      telemetry,
      craftFactory,
      fabricatorFactory,
      segmentManager,
      store,
      audioCache,
      content,
      100,
      100,
      10,
      48000,
      2);
  }

  @Test
  public void fabricatesManySegments() throws Exception {
    while (!hasSegmentsDubbedPastMinimumOffset() && isWithinTimeLimit()) {
      work.runCycle(1000000 * MICROS_PER_SECOND, 1000000 * MICROS_PER_SECOND);
      //noinspection BusyWait
      Thread.sleep(WORK_CYCLE_MILLIS);
    }

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
      return segmentManager.readLastCraftedSegment()
        .filter(value -> MARATHON_NUMBER_OF_SEGMENTS <= value.getId()).isPresent();

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException ignored) {
      return false;
    }
  }
}
