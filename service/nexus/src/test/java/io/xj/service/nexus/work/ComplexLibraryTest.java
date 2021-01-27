// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.work;

import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.util.Value;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.NexusIntegrationTestingFixtures;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComplexLibraryTest {
  private static final Logger log = LoggerFactory.getLogger(ComplexLibraryTest.class);
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MARATHON_NUMBER_OF_SEGMENTS = 100;
  private static final int MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  long startTime = System.currentTimeMillis();
  private NexusApp app;
  private Chain chain1;
  private SegmentDAO segmentDAO;

  @Mock
  public HubClient hubClient;

  @Mock
  public FileStoreProvider fileStoreProvider;

  @Before
  public void setUp() throws Exception {
    Config config = NexusTestConfiguration.getDefault()
      .withValue("app.port", ConfigValueFactory.fromAnyRef(9043))
      .withValue("datadog.statsd.hostname", ConfigValueFactory.fromAnyRef("localhost"))
      .withValue("work.bossDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.chainDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.concurrency", ConfigValueFactory.fromAnyRef(1));
    var injector = AppConfiguration.inject(config,
      ImmutableSet.of(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(FileStoreProvider.class).toInstance(fileStoreProvider);
            bind(HubClient.class).toInstance(hubClient);
          }
        })));
    segmentDAO = injector.getInstance(SegmentDAO.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    NexusEntityStore test = injector.getInstance(NexusEntityStore.class);
    test.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    NexusIntegrationTestingFixtures fake = new NexusIntegrationTestingFixtures();
    when(hubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(fake.generatedFixture(3)));

    // Chain "Test Print #1" is ready to begin
    chain1 = test.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(fake.account1.getId())
      .setName("Test Print #1")
      .setType(Chain.Type.Preview)
      .setState(Chain.State.Fabricate)
      .setStartAt(Value.formatIso8601UTC(Instant.now().minusSeconds(MAXIMUM_TEST_WAIT_SECONDS)))
      .build());

    test.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain1.getId())
      .setTargetId(fake.library1.getId())
      .setType(ChainBinding.Type.Library)
      .build());

    app = new NexusApp(injector);
  }

  @Test
  public void fabricatesManySegments() throws Exception {
    when(fileStoreProvider.generateKey(any()))
      .thenReturn("chains-1-segments-12345");
    when(fileStoreProvider.streamS3Object(any(), any()))
      .thenAnswer((Answer<InputStream>) invocation -> new FileInputStream(resourceAudioFile()));

    // Start app, wait for work, stop app
    app.start();
    while (!hasSegmentsDubbedPastMinimumOffset(chain1.getId()) && isWithinTimeLimit())
      //noinspection BusyWait
      Thread.sleep(MILLIS_PER_SECOND);
    app.finish();

    // assertions
    verify(fileStoreProvider, atLeast(MARATHON_NUMBER_OF_SEGMENTS))
      .putS3ObjectFromTempFile(eq("/tmp/chains-1-segments-12345.aac"), eq("xj-segment-test"), eq("chains-1-segments-12345.aac"));
    // FUTURE use a spy to assert actual json payload shipped to S3 for metadata
    verify(fileStoreProvider, atLeast(MARATHON_NUMBER_OF_SEGMENTS))
      .putS3ObjectFromString(any(), eq("xj-segment-test"), eq("chains-1-segments-12345.json"));
    assertTrue(hasSegmentsDubbedPastMinimumOffset(chain1.getId()));
  }

  /**
   get a file of java resources

   @return File
   @throws FileNotFoundException if resource does not exist
   */
  private File resourceAudioFile() throws FileNotFoundException {
    ClassLoader classLoader = ComplexLibraryTest.class.getClassLoader();
    URL resource = classLoader.getResource("source_audio/kick1.wav");
    if (Objects.isNull(resource))
      throw new FileNotFoundException(String.format("Failed to load resource: %s", "source_audio/kick1.wav"));
    return new File(resource.getFile());
  }


  /**
   Whether this test is within the time limit

   @return true if within time limit
   */
  private boolean isWithinTimeLimit() {
    if (MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > System.currentTimeMillis() - startTime)
      return true;
    log.error("EXCEEDED TEST TIME LIMIT OF {} SECONDS", MAXIMUM_TEST_WAIT_SECONDS);
    return false;
  }

  /**
   Does a specified Chain have at least N segments?

   @param chainId to test
   @return true if has at least N segments
   */
  private boolean hasSegmentsDubbedPastMinimumOffset(String chainId) {
    try {
      return MARATHON_NUMBER_OF_SEGMENTS <= segmentDAO.readLastDubbedSegment(HubClientAccess.internal(), chainId).getOffset();
    } catch (DAOPrivilegeException | DAOFatalException | DAOExistenceException ignored) {
      return false;
    }
  }
}
