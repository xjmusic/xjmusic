// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.app.App;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkType;
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
import java.sql.Timestamp;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChainEraseJobIT extends BaseIT {
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MAXIMUM_TEST_WAIT_MILLIS = 30 * MILLIS_PER_SECOND;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  long startTime = System.currentTimeMillis();
  @Mock
  private AmazonProvider amazonProvider;
  private Injector injector;
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
    SegmentFactory segmentFactory = injector.getInstance(SegmentFactory.class);

    // reset to shared fixtures
    IntegrationTestEntity.reset();
    insertLibraryA();

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertSegment_NoContent(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-segment-9f7s89d8a7892-ONE.wav");
    IntegrationTestEntity.insertSegment_NoContent(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-segment-2807f2d5g2h32-TWO.wav");

    // Chain "Test Print #1" has this segment that was just dubbed
    Segment segment3 = segmentFactory.newSegment(BigInteger.valueOf(3))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(2))
      .setStateEnum(SegmentState.Dubbed)
      .setBeginAt("2017-02-14 12:02:04.000001")
      .setEndAt("2017-02-14 12:02:36.000001")
      .setKey("Ab minor")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chain-1-segment-198745hj78dfs-THREE.wav");
    segment3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequencePatternId(BigInteger.valueOf(340))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(3));
    segment3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequencePatternId(BigInteger.valueOf(1651))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(1));
    segment3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequenceId(BigInteger.valueOf(35))
      .setTypeEnum(SequenceType.Rhythm)
      .setTranspose(-4));
    IntegrationTestEntity.insert(segment3);

    // Chain "Test Print #1" has a segment in dubbing state - Structure is complete
    Segment segment4 = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(3))
      .setStateEnum(SegmentState.Dubbing)
      .setBeginAt("2017-02-14 12:03:08.000001")
      .setEndAt("2017-02-14 12:03:15.836735")
      .setKey("F minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(125.0)
      .setWaveformKey("chain-1-segment-897h4d4f1h2j4-FOUR.wav");
    segment4.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(130))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(4));
    segment4.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(415150))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(-2));
    segment4.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequenceId(BigInteger.valueOf(35))
      .setTypeEnum(SequenceType.Rhythm)
      .setTranspose(5));
    ImmutableList.of("Hindsight", "Chunky", "Regret", "Tangy").forEach(memeName -> {
      try {
        segment4.add(new SegmentMeme()
          .setSegmentId(BigInteger.valueOf(4))
          .setName(memeName));
      } catch (CoreException ignored) {
      }
    });
    segment4.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(4))
      .setPosition(0.0)
      .setName("G minor"));
    segment4.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(4))
      .setPosition(8.0)
      .setName("Ab minor"));
    IntegrationTestEntity.insert(segment4);

    // Chain "Test Print #1" is ready to begin
    IntegrationTestEntity.insertChain(2, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Timestamp.from(new Date().toInstant().minusSeconds(300)), Timestamp.from(new Date().toInstant()), null);

    // Bind the library to the chains
    IntegrationTestEntity.insertChainLibrary(1, 2);
    IntegrationTestEntity.insertChainLibrary(2, 2);

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
    app.getWorkManager().startChainErase(BigInteger.valueOf(1));
    app.getWorkManager().startChainErase(BigInteger.valueOf(2));
    assertTrue(hasRemainingWork(WorkType.ChainErase));

    // Start app, wait for work, stop app
    app.start();
    while ((hasRemainingWork(WorkType.ChainErase)) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }
    app.stop();

    assertEquals(0, injector.getInstance(ChainDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(1))).size());
    assertEquals(0, injector.getInstance(SegmentDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(1))).size());
    assertEquals(0, injector.getInstance(SegmentDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(2))).size());

    verify(amazonProvider).deleteS3Object("xj-segment-test", "chain-1-segment-9f7s89d8a7892-ONE.wav");
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chain-1-segment-2807f2d5g2h32-TWO.wav");
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chain-1-segment-198745hj78dfs-THREE.wav");
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chain-1-segment-897h4d4f1h2j4-FOUR.wav");
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
