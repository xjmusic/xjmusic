// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValueFactory;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.App;
import io.xj.core.app.AppConfiguration;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.dao.SegmentChoiceDAO;
import io.xj.core.dao.SegmentChordDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SegmentMemeDAO;
import io.xj.core.dao.SegmentMessageDAO;
import io.xj.core.entity.MessageType;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentMessage;
import io.xj.core.model.SegmentState;
import io.xj.core.model.Work;
import io.xj.core.model.WorkState;
import io.xj.core.model.WorkType;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.craft.CraftModule;
import io.xj.dub.DubModule;
import io.xj.worker.WorkerModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ChainFabricateJobIT {
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MAXIMUM_TEST_WAIT_MILLIS = 30 * MILLIS_PER_SECOND;
  private static final int ARBITRARY_SMALL_PAUSE_SECONDS = 3;
  long startTime = System.currentTimeMillis();
  @Mock
  AmazonProvider amazonProvider;
  private App app;

  private IntegrationTestingFixtures fix;
  private Injector injector;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault()
      .withValue("work.bufferSeconds", ConfigValueFactory.fromAnyRef(1000))
      .withValue("work.bufferFabricateDelaySeconds", ConfigValueFactory.fromAnyRef(3))
      .withValue("work.chainEraseRecurSeconds", ConfigValueFactory.fromAnyRef(1))
      .withValue("work.chainDelayRecurSeconds", ConfigValueFactory.fromAnyRef(1))
      .withValue("app.port", ConfigValueFactory.fromAnyRef(9043))
      .withValue("work.concurrency", ConfigValueFactory.fromAnyRef(1));

    injector = AppConfiguration.inject(config, ImmutableList.of(Modules.override(new CoreModule(), new WorkerModule(), new CraftModule(), new DubModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      })));
    test = injector.getInstance(IntegrationTestProvider.class);
    fix = new IntegrationTestingFixtures(test);


    // reset to shared fixtures
    test.reset();
    fix.insertFixtureB1();
    fix.insertFixtureB_Instruments();

    // Chain "Test Print #1" is ready to begin
    fix.chain1 = test.insert(Chain.create(fix.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.now().minusSeconds(1000), null, null));
    test.insert(ChainBinding.create(fix.chain1, fix.library2));

    // Server App
    app = new App(ImmutableList.of("io.xj.worker"), injector);
  }

  @After
  public void tearDown() {
  }

  @Test
  public void fabricatesSegments() throws Exception {
    when(amazonProvider.generateKey(String.format("chains-%s-segments", fix.chain1.getId()), "ogg"))
      .thenReturn("chains-1-segments-12345.ogg");
    app.getWorkManager().startChainFabrication(fix.chain1.getId());
    assertTrue(hasRemainingWork(WorkType.ChainFabricate));

    // Start app, wait for work, stop app
    app.start();
    int assertShippedSegmentsMinimum = 3;
    while (!hasChainAtLeastSegments(fix.chain1.getId(), assertShippedSegmentsMinimum) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }
    app.getWorkManager().stopChainFabrication(fix.chain1.getId());
    app.stop();

    // assertions
    verify(amazonProvider, atLeast(assertShippedSegmentsMinimum)).generateKey(eq(String.format("chains-%s-segments", fix.chain1.getId())), eq("ogg"));
    verify(amazonProvider, atLeast(assertShippedSegmentsMinimum)).putS3Object(eq("/tmp/chains-1-segments-12345.ogg"), eq("xj-segment-test"), any());
    Collection<Segment> result = injector.getInstance(SegmentDAO.class).readMany(Access.internal(), ImmutableList.of(fix.chain1.getId()));
    assertTrue(assertShippedSegmentsMinimum <= result.size());
  }

  /**
   [#150279582] Engineer expects Worker to tolerate and report malformed sequences or instruments
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance
   */
  @Test
  public void fabricatesSegments_revertsAndRequeuesOnFailure() throws Exception {
    when(amazonProvider.generateKey(String.format("chains-%s-segments", fix.chain1.getId()), "ogg"))
      .thenReturn("chains-1-segments-12345.ogg");

    // destroy contents of program to invoke failure
    injector.getInstance(ProgramDAO.class).destroyChildEntities(Access.internal(), ImmutableList.of(fix.program4.getId()));

    // this segment is already in planned state-- it will end up reverted a.k.a. back in planned state
    fix.segment1 = test.insert(Segment.create(fix.chain1, 0, Instant.now().minusSeconds(1000)));

    // This ensures that the re-queued work does not get executed before the end of the test
    System.setProperty("segment.requeue.seconds", "666");

    // Send individual chain segment fabrication message to queue
    app.getWorkManager().scheduleSegmentFabricate(1, fix.segment1.getId());
    assertTrue(hasRemainingWork(WorkType.SegmentFabricate));

    // Start app, wait arbitrary # of seconds (it should fail immediately, which is what we are testing for), stop app
    app.start();
    Thread.sleep(ARBITRARY_SMALL_PAUSE_SECONDS * MILLIS_PER_SECOND);
    app.getWorkManager().stopChainFabrication(fix.chain1.getId());
    app.stop();

    // verify that the chain is still in fabricate state
    Chain resultChain = injector.getInstance(ChainDAO.class).readOne(Access.internal(), fix.chain1.getId());
    assertNotNull(resultChain);
    assertEquals(ChainState.Fabricate, resultChain.getState());

    // verify that the segment is in planned state
    Segment resultSegment = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), fix.segment1.getId());
    assertNotNull(resultSegment);
    assertEquals(SegmentState.Planned, resultSegment.getState());

    // verify that a follow-up segment fabricate job has been queued
    assertTrue(app.getWorkManager().isExistingWork(WorkState.Queued, WorkType.SegmentFabricate, fix.segment1.getId()));

    // verify that an error message has been created and attached to this segment, informing engineers of the problem
    Collection<SegmentMessage> resultSegmentMessages = injector.getInstance(SegmentMessageDAO.class).readMany(Access.internal(), ImmutableList.of(resultSegment.getId()));
    assertTrue(1 <= resultSegmentMessages.size());
    assertEquals(MessageType.Error, resultSegmentMessages.iterator().next().getType());
    String resultErrorBody = resultSegmentMessages.iterator().next().getBody();
    assertTrue(resultErrorBody.contains("Failed while doing Craft work"));

    // verify that the segment has no other child entities (besides messages)
    assertTrue(injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(fix.segment1.getId())).isEmpty());
    assertTrue(injector.getInstance(SegmentChordDAO.class).readMany(Access.internal(), ImmutableList.of(fix.segment1.getId())).isEmpty());
    assertTrue(injector.getInstance(SegmentChoiceDAO.class).readMany(Access.internal(), ImmutableList.of(fix.segment1.getId())).isEmpty());

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
  private boolean hasChainAtLeastSegments(UUID chainId, int threshold) throws Exception {
    Collection<Segment> result = injector.getInstance(SegmentDAO.class).readAllInState(Access.internal(), chainId, SegmentState.Dubbed);
    return result.size() >= threshold;
  }


}
