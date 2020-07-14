// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.work;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubContent;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.NexusHubContentFixtures;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainBinding;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.ChainType;
import io.xj.service.nexus.entity.Segment;
import io.xj.service.nexus.entity.SegmentState;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.service.nexus.testing.NexusTestConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.util.Collection;
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
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MARATHON_NUMBER_OF_SEGMENTS = 7;
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
      .withValue("work.bossDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.chainDelayMillis", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.concurrency", ConfigValueFactory.fromAnyRef(1));
    Injector injector = AppConfiguration.inject(config,
      ImmutableSet.of(Modules.override(new NexusWorkModule())
        .with(new AbstractModule() {
          @Override
          public void configure() {
            bind(FileStoreProvider.class).toInstance(fileStoreProvider);
            bind(HubClient.class).toInstance(hubClient);
          }
        })));
    segmentDAO = injector.getInstance(SegmentDAO.class);
    EntityFactory entityFactory = injector.getInstance(EntityFactory.class);
    HubApp.buildApiTopology(entityFactory);
    NexusApp.buildApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    NexusEntityStore test = injector.getInstance(NexusEntityStore.class);
    test.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    NexusHubContentFixtures fake = new NexusHubContentFixtures();
    when(hubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(fake.generatedFixture(3)));

    // Chain "Test Print #1" is ready to begin
    chain1 = test.put(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.now().minusSeconds(1000), null, null));
    test.put(ChainBinding.create(chain1, fake.library1));

    app = new NexusApp(ImmutableSet.of("io.xj.nexus"), injector);
  }

  @Test
  public void fabricatesManySegments() throws Exception {
    when(fileStoreProvider.generateKey(any(), eq("aac")))
      .thenReturn("chains-1-segments-12345.aac");
    when(fileStoreProvider.streamS3Object(any(), any()))
      .thenAnswer((Answer<InputStream>) invocation -> new FileInputStream(resourceAudioFile()));

    // Start app, wait for work, stop app
    app.start();
    int assertShippedSegmentsMinimum = MARATHON_NUMBER_OF_SEGMENTS;
    while (!hasChainAtLeastSegments(chain1.getId(), assertShippedSegmentsMinimum) && isWithinTimeLimit())
      //noinspection BusyWait
      Thread.sleep(MILLIS_PER_SECOND);
    app.finish();

    // assertions
    verify(fileStoreProvider, atLeast(assertShippedSegmentsMinimum))
      .putS3Object(eq("/tmp/chains-1-segments-12345.aac"), eq("xj-segment-test"), any());
    Collection<Segment> result = segmentDAO.readMany(HubClientAccess.internal(), ImmutableList.of(chain1.getId()));
    assertTrue(assertShippedSegmentsMinimum <= result.size());
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
    return MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > System.currentTimeMillis() - startTime;
  }

  /**
   Does a specified Chain have at least N segments?

   @param chainId   to test
   @param threshold minimum # of segments to qualify
   @return true if has at least N segments
   @throws Exception on failure
   */
  private boolean hasChainAtLeastSegments(UUID chainId, int threshold) throws Exception {
    Collection<Segment> result = segmentDAO.readAllInState(HubClientAccess.internal(), chainId, SegmentState.Dubbed);
    return result.size() >= threshold;
  }
}
