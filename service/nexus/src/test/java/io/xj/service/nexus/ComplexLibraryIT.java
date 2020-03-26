// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.lib.app.AppConfiguration;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.SegmentDAO;
import io.xj.service.hub.persistence.AmazonProvider;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.ChainType;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentState;
import io.xj.service.hub.model.Work;
import io.xj.service.hub.model.WorkType;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.IntegrationTestModule;
import io.xj.service.hub.testing.IntegrationTestProvider;
import io.xj.service.hub.testing.InternalResources;
import io.xj.service.nexus.craft.CraftModule;
import io.xj.service.nexus.dub.DubModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComplexLibraryIT {
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MARATHON_NUMBER_OF_SEGMENTS = 7;
  private static final int MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  long startTime = System.currentTimeMillis();
  @Mock
  AmazonProvider amazonProvider;
  private NexusApp app;

  private IntegrationTestingFixtures fake;
  private Injector injector;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault()
      .withValue("app.port", ConfigValueFactory.fromAnyRef(9043));
    injector = AppConfiguration.inject(config, ImmutableSet.of(Modules.override(new HubModule(), new NexusModule(), new CraftModule(), new DubModule(), new IntegrationTestModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      })));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);


    // reset to shared fixtures
    test.reset();
    fake.insertGeneratedFixture(3);

    // Chain "Test Print #1" is ready to begin
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.now().minusSeconds(1000), null, null));
    test.insert(ChainBinding.create(fake.chain1, fake.library1));

    app = new NexusApp(ImmutableSet.of("io.xj.nexus"), injector);
  }

  @After
  public void tearDown() {
    app.stop();
    test.shutdown();
  }

  @Test
  public void fabricatesManySegments() throws Exception {
    when(amazonProvider.generateKey(String.format("chains-%s-segments", fake.chain1.getId()), "aac"))
      .thenReturn("chains-1-segments-12345.aac");
    when(amazonProvider.streamS3Object(any(), any()))
      .thenAnswer((Answer<InputStream>) invocation -> new FileInputStream(InternalResources.resourceFile("source_audio/kick1.wav")));
    app.getWorkManager().startChainFabrication(fake.chain1.getId());
    assertTrue(hasRemainingWork());

    // Start app, wait for work, stop app
    app.start();
    int assertShippedSegmentsMinimum = MARATHON_NUMBER_OF_SEGMENTS;
    while (!hasChainAtLeastSegments(fake.chain1.getId(), assertShippedSegmentsMinimum) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }
    app.getWorkManager().stopChainFabrication(fake.chain1.getId());

    // assertions
    verify(amazonProvider, atLeast(assertShippedSegmentsMinimum))
      .putS3Object(eq("/tmp/chains-1-segments-12345.aac"), eq("xj-segment-test"), any());
    Collection<Segment> result = injector.getInstance(SegmentDAO.class)
      .readMany(Access.internal(), ImmutableList.of(fake.chain1.getId()));
    assertTrue(assertShippedSegmentsMinimum <= result.size());
  }

  /**
   Whether this test is within the time limit

   @return true if within time limit
   */
  private boolean isWithinTimeLimit() {
    return MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > System.currentTimeMillis() - startTime;
  }

  /**
   Whether there is active work of a particular type

   @return true if there is work remaining
   */
  private boolean hasRemainingWork() throws Exception {
    int total = 0;
    for (Work work : app.getWorkManager().readAllWork())
      if (WorkType.ChainFabricate == work.getType()) total++;
    return 0 < total;
  }

  /**
   Does a specified Chain have at least N segments?

   @param chainId   to test
   @param threshold minimum # of segments to qualify
   @return true if has at least N segments
   @throws Exception on failure
   */
  private boolean hasChainAtLeastSegments(UUID chainId, int threshold) throws Exception {
    Collection<Segment> result = injector.getInstance(SegmentDAO.class).readAllInState(Access.internal(), chainId, SegmentState.Dubbed);
    return result.size() >= threshold;
  }


}
