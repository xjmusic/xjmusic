// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.worker;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.App;
import io.xj.core.app.AppConfiguration;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.external.AmazonProvider;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.model.SegmentState;
import io.xj.core.model.Work;
import io.xj.core.model.WorkType;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.craft.CraftModule;
import io.xj.dub.DubModule;
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
public class ChainEraseJobIT {
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MAXIMUM_TEST_WAIT_MILLIS = 30 * MILLIS_PER_SECOND;
  long startTime = System.currentTimeMillis();
  private App app;
  @Mock
  private AmazonProvider amazonProvider;

  private IntegrationTestingFixtures fake;
  private Injector injector;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule(), new WorkerModule(), new CraftModule(), new DubModule()).with(
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
    fake.insertFixtureA();

    // Chain "Test Print #1" has 5 total segments
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Erase, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fake.chain1, fake.library10000001));
    fake.segment1 = test.insert(Segment.create(fake.chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892-ONE.wav"));
    SegmentChoice choice = test.insert(SegmentChoice.create(fake.segment1, ProgramType.Rhythm, fake.program702, 0));
    SegmentChoiceArrangement arrangement = test.insert(SegmentChoiceArrangement.create(choice, fake.programVoice3, fake.instrument201));
    test.insert(SegmentChoiceArrangementPick.create(arrangement, fake.program702_pattern901_boomEvent, fake.instrument201_audio402,0.0,1.0,1.0,400.0,"boom"));

    fake.segment2 = test.insert(Segment.create(fake.chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-2807f2d5g2h32-TWO.wav"));

    // Chain "Test Print #1" has this segment that was just dubbed
    fake.segment3 = Segment.create(fake.chain1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "F Major", 64, 0.30, 120.0, "chains-1-segments-198745hj78dfs-THREE.wav");
    test.insert(fake.segment3);

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    fake.segment4 = Segment.create(fake.chain1, 3, SegmentState.Dubbing, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "G minor", 16, 0.45, 120.0, "chains-1-segments-897h4d4f1h2j4-FOUR.wav");
    test.insert(fake.segment4);

    // Chain "Test Print #1" is ready to begin
    fake.chain2 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Erase, Instant.now().minusSeconds(300), Instant.now(), null));
    test.insert(ChainBinding.create(fake.chain2, fake.library10000001));

    // Don't sleep between processing work
    System.setProperty("app.port", "9043");

    // Recur frequently, as a hack before implementing [#395] ExpectationOfWork client executes a `ChainDeleteJob` and enqueues `SegmentDeleteJob` for each Segment in the Chain
    System.setProperty("work.chain.delete.recur.seconds", "1");

    // bucket config
    System.setProperty("segment.file.bucket", "xj-segment-test");

    // Server App
    app = new App(ImmutableList.of("io.xj.worker"), injector);
  }

  @After
  public void tearDown() {
    app.stop();
    test.shutdown();
  }

  /**
   [#294] Eraseworker finds Segments and Audio in deleted state and actually deletes the records, child entities and S3 objects
   */
  @Test
  public void runWorker() throws Exception {
    app.getWorkManager().startChainErase(fake.chain1.getId());
    app.getWorkManager().startChainErase(fake.chain2.getId());
    assertTrue(hasRemainingWork(WorkType.ChainErase));

    // Start app, wait for work, stop app
    app.start();
    while ((hasRemainingWork(WorkType.ChainErase)) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }

    assertEquals(0, injector.getInstance(ChainDAO.class).readMany(Access.internal(), ImmutableList.of(fake.chain1.getId())).size());
    assertEquals(0, injector.getInstance(SegmentDAO.class).readMany(Access.internal(), ImmutableList.of(fake.chain1.getId())).size());
    assertEquals(0, injector.getInstance(SegmentDAO.class).readMany(Access.internal(), ImmutableList.of(fake.chain2.getId())).size());

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
