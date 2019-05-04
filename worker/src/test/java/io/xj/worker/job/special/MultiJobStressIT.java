// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.special;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.app.App;
import io.xj.core.dao.PatternChordDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternEventDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.segment.Segment;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MultiJobStressIT extends BaseIT {
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MAXIMUM_TEST_WAIT_MILLIS = 30 * MILLIS_PER_SECOND;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  long startTime = System.currentTimeMillis();
  @Mock
  AmazonProvider amazonProvider;
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

    // reset to shared fixtures
    IntegrationTestEntity.reset();
    insertLibraryA();

    // Chain "Test Print #1" is ready to begin
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, TimestampUTC.nowMinusSeconds(1000), null, null);

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2);

    // ExpectationOfWork recurs frequently to speed up test
    System.setProperty("work.buffer.seconds", "1000");
    System.setProperty("work.buffer.craft.delay.seconds", "1");
    System.setProperty("work.buffer.dub.delay.seconds", "3");
    System.setProperty("work.chain.recur.seconds", "1");
    System.setProperty("work.chain.delete.recur.seconds", "1");
    System.setProperty("work.chain.delay.seconds", "1");

    // Don't sleep between processing work
    System.setProperty("app.port", "9043");

    // segment file config
    System.setProperty("segment.file.bucket", "xj-segment-test");

    // work concurrency config
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
    System.clearProperty("work.concurrency");
    System.clearProperty("work.buffer.seconds");
    System.clearProperty("segment.file.bucket");
    System.clearProperty("work.buffer.craft.delay.seconds");
    System.clearProperty("work.buffer.dub.delay.seconds");
    System.clearProperty("work.chain.recur.seconds");
    System.clearProperty("work.chain.delete.recur.seconds");
    System.clearProperty("work.chain.delay.seconds");
  }

  @Test
  public void clonesPatternWhileChainFabrication() throws Exception {
    when(amazonProvider.generateKey("chain-1-segment", "ogg"))
      .thenReturn("chain-1-segment-12345.ogg");
    app.getWorkManager().startChainFabrication(BigInteger.valueOf(1));

    // Start app, wait until at least one segment has been fabricated, then begin another job
    app.start();
    while (!hasChainAtLeastSegments(BigInteger.valueOf(1), 2) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }
    app.getWorkManager().doPatternClone(BigInteger.valueOf(1001), BigInteger.valueOf(1003));
    app.getWorkManager().doPatternClone(BigInteger.valueOf(1002), BigInteger.valueOf(1004));

    // Wait until pattern clone jobs are complete, then stop chain fabrication, stop app
    while (hasRemainingWork(WorkType.PatternClone) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }
    app.getWorkManager().stopChainFabrication(BigInteger.valueOf(1));
    app.stop();

    // Verify existence of cloned patterns
    Pattern resultOne = injector.getInstance(PatternDAO.class).readOne(Access.internal(), BigInteger.valueOf(1003));
    Pattern resultTwo = injector.getInstance(PatternDAO.class).readOne(Access.internal(), BigInteger.valueOf(1004));
    assertNotNull(resultOne);
    assertNotNull(resultTwo);

    // Verify existence of children of cloned pattern #1
    Collection<PatternChord> chordsOne = injector.getInstance(PatternChordDAO.class).readAll(Access.internal(), ImmutableList.of(resultOne.getId()));
    Collection<PatternEvent> eventsOne = injector.getInstance(PatternEventDAO.class).readAll(Access.internal(), ImmutableList.of(resultOne.getId()));
    assertEquals(1, chordsOne.size());
    assertEquals(2, eventsOne.size());
    PatternChord chordOne = chordsOne.iterator().next();
    assertEquals(0, chordOne.getPosition(), 0.01);
    assertEquals("Db7", chordOne.getName());

    // Verify existence of children of cloned pattern #2
    Collection<PatternChord> chordsTwo = injector.getInstance(PatternChordDAO.class).readAll(Access.internal(), ImmutableList.of(resultTwo.getId()));
    Collection<PatternEvent> eventsTwo = injector.getInstance(PatternEventDAO.class).readAll(Access.internal(), ImmutableList.of(resultTwo.getId()));
    assertEquals(1, chordsTwo.size());
    assertEquals(2, eventsTwo.size());
    PatternChord chordTwo = chordsTwo.iterator().next();
    assertEquals(0, chordTwo.getPosition(), 0.01);
    assertEquals("Gm9", chordTwo.getName());
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


  /**
   Does a specified Chain have at least N segments?

   @param chainId   to test
   @param threshold minimum # of segments to qualify
   @return true if has at least N segments
   @throws Exception on failure
   */
  private boolean hasChainAtLeastSegments(BigInteger chainId, int threshold) throws Exception {
    Collection<Segment> result = injector.getInstance(SegmentDAO.class).readAll(Access.internal(), ImmutableList.of(chainId));
    return result.size() >= threshold;
  }

}
