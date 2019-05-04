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
import io.xj.core.dao.PatternChordDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SequencePatternMemeDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkState;
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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChainFabricateJobIT extends BaseIT {
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MAXIMUM_TEST_WAIT_MILLIS = 30 * MILLIS_PER_SECOND;
  private static final int ARBITRARY_SMALL_PAUSE_SECONDS = 3;
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
    System.setProperty("audio.file.bucket", "xj-audio-test");
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
    System.clearProperty("audio.file.bucket");
    System.clearProperty("segment.file.bucket");

    System.clearProperty("work.concurrency");

    System.clearProperty("work.buffer.seconds");
    System.clearProperty("work.buffer.craft.delay.seconds");
    System.clearProperty("work.buffer.dub.delay.seconds");
    System.clearProperty("work.chain.recur.seconds");
    System.clearProperty("work.chain.delete.recur.seconds");
    System.clearProperty("work.chain.delay.seconds");
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
  public void fabricatesSegments() throws Exception {
    when(amazonProvider.generateKey("chain-1-segment", "ogg"))
      .thenReturn("chain-1-segment-12345.ogg");
    app.getWorkManager().startChainFabrication(BigInteger.valueOf(1));
    assertTrue(hasRemainingWork(WorkType.ChainFabricate));

    // Start app, wait for work, stop app
    app.start();
    int assertShippedSegmentsMinimum = 3;
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
   [#150279582] Engineer expects Worker to tolerate and report malformed sequences or instruments
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance
   */
  @Test
  public void fabricatesSegments_revertsAndRequeuesOnFailure() throws Exception {
    injector.getInstance(PatternDAO.class).destroy(Access.internal(), BigInteger.valueOf(15));
    injector.getInstance(SequencePatternMemeDAO.class).destroy(Access.internal(), BigInteger.valueOf(6));
    injector.getInstance(PatternChordDAO.class).destroy(Access.internal(), BigInteger.valueOf(12));
    injector.getInstance(PatternChordDAO.class).destroy(Access.internal(), BigInteger.valueOf(14));
    injector.getInstance(PatternDAO.class).destroy(Access.internal(), BigInteger.valueOf(16));
    injector.getInstance(SequencePatternMemeDAO.class).destroy(Access.internal(), BigInteger.valueOf(7));
    injector.getInstance(PatternChordDAO.class).destroy(Access.internal(), BigInteger.valueOf(16));
    injector.getInstance(PatternChordDAO.class).destroy(Access.internal(), BigInteger.valueOf(18));
    injector.getInstance(SequencePatternMemeDAO.class).destroy(Access.internal(), BigInteger.valueOf(46));
    injector.getInstance(SequencePatternMemeDAO.class).destroy(Access.internal(), BigInteger.valueOf(47));
    injector.getInstance(SequencePatternMemeDAO.class).destroy(Access.internal(), BigInteger.valueOf(149));
    injector.getInstance(PatternChordDAO.class).destroy(Access.internal(), BigInteger.valueOf(412));
    injector.getInstance(PatternChordDAO.class).destroy(Access.internal(), BigInteger.valueOf(414));
    injector.getInstance(PatternChordDAO.class).destroy(Access.internal(), BigInteger.valueOf(416));
    injector.getInstance(PatternChordDAO.class).destroy(Access.internal(), BigInteger.valueOf(418));
    injector.getInstance(PatternDAO.class).destroy(Access.internal(), BigInteger.valueOf(415));
    injector.getInstance(PatternDAO.class).destroy(Access.internal(), BigInteger.valueOf(416));

    // this segment is already in planned state-- it will end up reverted a.k.a. back in planned state
    IntegrationTestEntity.insertSegment_Planned(101, 1, 0, TimestampUTC.nowMinusSeconds(1000));

    // This ensures that the re-queued work does not get executed before the end of the test
    System.setProperty("segment.requeue.seconds", "666");

    // Send individual chain segment fabrication message to queue
    app.getWorkManager().scheduleSegmentFabricate(1, BigInteger.valueOf(101));
    assertTrue(hasRemainingWork(WorkType.SegmentFabricate));

    // Start app, wait arbitrary # of seconds (it should fail immediately, which is what we are testing for), stop app
    app.start();
    Thread.sleep(ARBITRARY_SMALL_PAUSE_SECONDS * MILLIS_PER_SECOND);
    app.getWorkManager().stopChainFabrication(BigInteger.valueOf(1));
    app.stop();

    // verify that the chain is still in fabricate state
    Chain resultChain = injector.getInstance(ChainDAO.class).readOne(Access.internal(), BigInteger.valueOf(1));
    assertNotNull(resultChain);
    assertEquals(ChainState.Fabricate, resultChain.getState());

    // verify that the segment is in planned state
    Segment resultSegment = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), BigInteger.valueOf(101));
    assertNotNull(resultSegment);
    assertEquals(SegmentState.Planned, resultSegment.getState());

    // verify that a follow-up segment fabricate job has been queued
    assertTrue(app.getWorkManager().isExistingWork(WorkState.Queued, WorkType.SegmentFabricate, BigInteger.valueOf(101)));

    // verify that an error message has been created and attached to this segment, informing engineers of the problem
    Collection<SegmentMessage> resultSegmentMessages = resultSegment.getMessages();
    assertEquals(1, resultSegmentMessages.size());
    assertEquals(MessageType.Error, resultSegmentMessages.iterator().next().getType());
    String resultErrorBody = resultSegmentMessages.iterator().next().getBody();
    assertTrue(resultErrorBody.contains("Failed while doing Craft work"));

    // verify that the segment has no other child entities (besides messages)
    assertTrue(resultSegment.getMemes().isEmpty());
    assertTrue(resultSegment.getChords().isEmpty());
    assertTrue(resultSegment.getChoices().isEmpty());

    // Cleanup
    System.clearProperty("segment.requeue.seconds");
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
    Collection<Segment> result = injector.getInstance(SegmentDAO.class).readAllInState(Access.internal(), chainId, SegmentState.Dubbed);
    return result.size() >= threshold;
  }


}
