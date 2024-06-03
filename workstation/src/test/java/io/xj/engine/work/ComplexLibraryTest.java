// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.work;

import io.xj.model.HubContent;
import io.xj.model.HubTopology;
import io.xj.model.entity.EntityFactoryImpl;
import io.xj.model.json.JsonProviderImpl;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.engine.FabricationException;
import io.xj.engine.FabricationContentTwoFixtures;
import io.xj.engine.FabricationTopology;
import io.xj.engine.audio.AudioCache;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.craft.CraftFactoryImpl;
import io.xj.engine.fabricator.FabricatorFactoryImpl;
import io.xj.engine.fabricator.FabricationEntityStore;
import io.xj.engine.fabricator.FabricationEntityStoreImpl;
import io.xj.engine.fabricator.SegmentUtils;
import io.xj.engine.telemetry.Telemetry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static io.xj.model.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.engine.FabricationContentOneFixtures.buildLibrary;
import static io.xj.engine.FabricationContentOneFixtures.buildProject;
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
  FabricationEntityStore entityStore;
  CraftWork work;

  @Mock
  Telemetry telemetry;

  @Mock
  AudioCache audioCache;

  @BeforeEach
  public void setUp() throws Exception {
    FabricationContentTwoFixtures fake = new FabricationContentTwoFixtures();
    fake.project1 = FabricationContentOneFixtures.buildProject("fish");
    fake.library1 = FabricationContentOneFixtures.buildLibrary(fake.project1, "test");
    var generatedFixtures = fake.generatedFixture(GENERATED_FIXTURE_COMPLEXITY);
    HubContent content = new HubContent(generatedFixtures.stream().filter(Objects::nonNull).toList());

    var template = content.getTemplates().stream().findFirst().orElseThrow();
    template.setShipKey("complex_library_test");
    template.setConfig("outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n");
    content.put(template);

    var jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    entityStore = new FabricationEntityStoreImpl(entityFactory);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    var fabricatorFactory = new FabricatorFactoryImpl(
      entityStore,
      jsonapiPayloadFactory,
      jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    FabricationTopology.buildFabricationTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    FabricationEntityStore test = new FabricationEntityStoreImpl(entityFactory);
    test.clear();

    // Dependencies
    CraftFactory craftFactory = new CraftFactoryImpl();

    // work
    work = new CraftWorkImpl(
      telemetry,
      craftFactory,
      fabricatorFactory,
      entityStore,
      audioCache,
      100,
      100,
      10,
      48000,
      2,
      content);
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
  boolean hasSegmentsDubbedPastMinimumOffset() throws FabricationException {
    var chain = work.getChain();
    if (chain.isEmpty())
      return false;
    return SegmentUtils.getLastCrafted(entityStore.readAllSegments())
      .filter(value -> MARATHON_NUMBER_OF_SEGMENTS <= value.getId()).isPresent();
  }
}
