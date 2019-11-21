// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.app.App;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentState;
import io.xj.core.model.Work;
import io.xj.core.model.WorkType;
import io.xj.craft.CraftModule;
import io.xj.dub.DubModule;
import io.xj.worker.WorkerModule;
import net.greghaines.jesque.worker.JobFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChainEraseJobIT extends FixtureIT {
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MAXIMUM_TEST_WAIT_MILLIS = 30 * MILLIS_PER_SECOND;
  long startTime = System.currentTimeMillis();
  @Mock
  private AmazonProvider amazonProvider;
  private App app;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new WorkerModule(), new CraftModule(), new DubModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));
    // reset to shared fixtures
    reset();
    insertFixtureA();

    // Chain "Test Print #1" has 5 total segments
    chain1 = insert(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Erase, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    insert(ChainBinding.create(chain1, library10000001));
    segment1 = insert(Segment.create(chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892-ONE.wav"));
    segment2 = insert(Segment.create(chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-2807f2d5g2h32-TWO.wav"));

    // Chain "Test Print #1" has this segment that was just dubbed
    segment3 = Segment.create(chain1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "F Major", 64, 0.30, 120.0, "chains-1-segments-198745hj78dfs-THREE.wav");
    insert(segment3);

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    segment4 = Segment.create(chain1, 3, SegmentState.Dubbing, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "G minor", 16, 0.45, 120.0, "chains-1-segments-897h4d4f1h2j4-FOUR.wav");
    insert(segment4);

    // Chain "Test Print #1" is ready to begin
    chain2 = insert(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Erase, Instant.now().minusSeconds(300), Instant.now(), null));
    insert(ChainBinding.create(chain2, library10000001));

    // Don't sleep between processing work
    System.setProperty("app.port", "9043");

    // Recur frequently, as a hack before implementing [#395] ExpectationOfWork client executes a `ChainDeleteJob` and enqueues `SegmentDeleteJob` for each Segment in the Chain
    System.setProperty("work.chain.delete.recur.seconds", "1");

    // bucket config
    System.setProperty("segment.file.bucket", "xj-segment-test");

    // Server App
    app = injector.getInstance(App.class);
    app.configureServer("io.xj.worker");

    // Attach Job Factory to App
    JobFactory jobFactory = injector.getInstance(JobFactory.class);
    app.setJobFactory(jobFactory);
  }

  @After
  public void tearDown() {
    System.clearProperty("segment.file.bucket");
    System.clearProperty("work.chain.delete.recur.seconds");
  }

  /**
   [#294] Eraseworker finds Segments and Audio in deleted state and actually deletes the records, child entities and S3 objects
   */
  @Test
  public void runWorker() throws Exception {
    app.getWorkManager().startChainErase(chain1.getId());
    app.getWorkManager().startChainErase(chain2.getId());
    assertTrue(hasRemainingWork(WorkType.ChainErase));

    // Start app, wait for work, stop app
    app.start();
    while ((hasRemainingWork(WorkType.ChainErase)) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }
    app.stop();

    assertEquals(0, injector.getInstance(ChainDAO.class).readMany(Access.internal(), ImmutableList.of(chain1.getId())).size());
    assertEquals(0, injector.getInstance(SegmentDAO.class).readMany(Access.internal(), ImmutableList.of(chain1.getId())).size());
    assertEquals(0, injector.getInstance(SegmentDAO.class).readMany(Access.internal(), ImmutableList.of(chain2.getId())).size());

    verify(amazonProvider).deleteS3Object("xj-segment-test", "chains-1-segments-9f7s89d8a7892-ONE.wav");
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chains-1-segments-2807f2d5g2h32-TWO.wav");
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chains-1-segments-198745hj78dfs-THREE.wav");
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chains-1-segments-897h4d4f1h2j4-FOUR.wav");
  }

  /**
   Whether this test is within the time limit

   @return true if within time limit
   */
  private boolean isWithinTimeLimit() {
    return MAXIMUM_TEST_WAIT_MILLIS > System.currentTimeMillis() - startTime;
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


}
