//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.special;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.app.App;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkType;
import io.xj.core.util.TimestampUTC;
import io.xj.craft.CraftModule;
import io.xj.dub.DubModule;
import io.xj.worker.BaseIT;
import io.xj.worker.WorkerModule;
import net.greghaines.jesque.worker.JobFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ComplexLibraryIT extends BaseIT {
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MARATHON_NUMBER_OF_SEGMENTS = 12;
  private static final int MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  long startTime = System.currentTimeMillis();
  @Mock
  AmazonProvider amazonProvider;
  private Injector injector;
  private App app;

  @Before
  public void setUp() throws Exception {
    createInjector();

    // reset to shared fixtures
    IntegrationTestEntity.reset();
    IntegrationTestEntity.insertLibraryGenerated(3);

    // Chain "Test Print #1" is ready to begin
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, TimestampUTC.nowMinusSeconds(1000), null, null);

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 1);

    // Config
    System.setProperty("app.port", "9043");
    System.setProperty("audio.file.bucket", "xj-segment-test");
    System.setProperty("segment.file.bucket", "xj-segment-test");
    System.setProperty("work.concurrency", "1");

    // Server App
    app = injector.getInstance(App.class);
    app.configureServer("io.xj.worker");

    // Attach Job Factory to App
    JobFactory jobFactory = injector.getInstance(JobFactory.class);
    app.setJobFactory(jobFactory);
  }

  @After
  public void tearDown() {
    System.clearProperty("app.port");
    System.clearProperty("audio.file.bucket");
    System.clearProperty("segment.file.bucket");
    System.clearProperty("work.concurrency");
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new WorkerModule(), new CraftModule(), new DubModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));
  }

  @Test
  public void fabricatesManySegments() throws Exception {
    when(amazonProvider.generateKey("chain-1-segment", "ogg"))
      .thenReturn("chain-1-segment-12345.ogg");
    app.getWorkManager().startChainFabrication(BigInteger.valueOf(1));
    assertTrue(hasRemainingWork(WorkType.ChainFabricate));

    // Start app, wait for work, stop app
    app.start();
    int assertShippedSegmentsMinimum = MARATHON_NUMBER_OF_SEGMENTS;
    while (!hasChainAtLeastSegments(BigInteger.valueOf(1), assertShippedSegmentsMinimum) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }
    app.getWorkManager().stopChainFabrication(BigInteger.valueOf(1));
    app.stop();

    // assertions
    verify(amazonProvider, atLeast(assertShippedSegmentsMinimum)).putS3Object(eq("/tmp/chain-1-segment-12345.ogg"), eq("xj-segment-test"), any());
    Collection<Segment> result = injector.getInstance(SegmentDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(1)));
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
  private boolean hasRemainingWork(WorkType type) throws Exception {
    int total = 0;
    for (Work work : app.getWorkManager().readAllWork()) {
      if (type == work.getType()) total++;
    }
    return 0 < total;
  }

  /**
   Does a specified Chain have at least N segments?

   @param chainId   to test
   @param threshold minimum # of segments to qualify
   @return true if has at least N segments
   @throws Exception on failure
   */
  private boolean hasChainAtLeastSegments(BigInteger chainId, int threshold) throws Exception {
    Collection<Segment> result = injector.getInstance(SegmentDAO.class).readAllInState(Access.internal(), chainId, SegmentState.Dubbed);
    return result.size() >= threshold;
  }


}
