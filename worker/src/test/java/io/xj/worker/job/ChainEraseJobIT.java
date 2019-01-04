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
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkType;
import io.xj.craft.CraftModule;
import io.xj.dub.DubModule;
import io.xj.worker.WorkerModule;
import net.greghaines.jesque.worker.JobFactory;
import org.json.JSONObject;
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
import java.util.Objects;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ChainEraseJobIT {
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
    IntegrationTestEntity.reset();

    // inject mocks
    createInjector();

    // configs
    System.setProperty("segment.file.bucket", "xj-segment-test");

    // Account "pilots"
    IntegrationTestEntity.insertAccount(1, "pilots");

    // Ted has "user" and "admin" roles, belongs to account "pilots", has "google" auth
    IntegrationTestEntity.insertUser(2, "ted", "ted@email.com", "http://pictures.com/ted.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Sally has a "user" role and belongs to account "pilots"
    IntegrationTestEntity.insertUser(3, "sally", "sally@email.com", "http://pictures.com/sally.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Heavy, Deep to Metal" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Heavy, Deep to Metal", 0.5, "C", 120);
    IntegrationTestEntity.insertSequenceMeme(2, 4, "Heavy");
    // " pattern offset 0
    IntegrationTestEntity.insertPatternSequencePattern(3, 4, PatternType.Macro, PatternState.Published, 0, 0, "Start Deep", 0.6, "C", 125);
    IntegrationTestEntity.insertPatternMeme(3, 3, "Deep");
    IntegrationTestEntity.insertPatternChord(3, 3, 0, "C");
    // " pattern offset 1
    IntegrationTestEntity.insertPatternSequencePattern(4, 4, PatternType.Macro, PatternState.Published, 1, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPatternMeme(4, 4, "Metal");
    IntegrationTestEntity.insertPatternMeme(49, 4, "Deep");
    IntegrationTestEntity.insertPatternChord(4, 4, 0, "Bb minor");
    // " pattern offset 2
    IntegrationTestEntity.insertPatternSequencePattern(5, 4, PatternType.Macro, PatternState.Published, 2, 0, "Finish Metal", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertPatternMeme(5, 4, "Metal");
    IntegrationTestEntity.insertPatternChord(5, 5, 0, "Ab minor");

    // "Tech, Steampunk to Modern" macro-sequence in house library
    IntegrationTestEntity.insertSequence(3, 3, 2, SequenceType.Macro, SequenceState.Published, "Tech, Steampunk to Modern", 0.5, "G minor", 120);
    IntegrationTestEntity.insertSequenceMeme(1, 3, "Tech");
    // # pattern offset 0
    IntegrationTestEntity.insertPatternSequencePattern(1, 3, PatternType.Macro, PatternState.Published, 0, 0, "Start Steampunk", 0.4, "G minor", 115);
    IntegrationTestEntity.insertPatternMeme(1, 1, "Steampunk");
    IntegrationTestEntity.insertPatternChord(1, 1, 0, "G minor");
    // # pattern offset 1
    IntegrationTestEntity.insertPatternSequencePattern(2, 3, PatternType.Macro, PatternState.Published, 1, 0, "Finish Modern", 0.6, "C", 125);
    IntegrationTestEntity.insertPatternMeme(2, 2, "Modern");
    IntegrationTestEntity.insertPatternChord(2, 2, 0, "C");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertSequenceMeme(3, 5, "Attitude");
    // # pattern offset 0
    IntegrationTestEntity.insertPatternSequencePattern(15, 5, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPatternMeme(6, 15, "Gritty");
    IntegrationTestEntity.insertPatternChord(12, 15, 0, "G major");
    IntegrationTestEntity.insertPatternChord(14, 15, 8, "Ab minor");
    // # pattern offset 1
    IntegrationTestEntity.insertPatternSequencePattern(16, 5, PatternType.Main, PatternState.Published, 1, 16, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPatternMeme(7, 16, "Gentle");
    IntegrationTestEntity.insertPatternChord(16, 16, 0, "C major");
    IntegrationTestEntity.insertPatternChord(18, 16, 8, "Bb minor");

    // Another Main sequence to go to
    IntegrationTestEntity.insertSequence(15, 3, 2, SequenceType.Main, SequenceState.Published, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertSequenceMeme(43, 15, "Temptation");
    IntegrationTestEntity.insertPatternSequencePattern(415, 15, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPatternMeme(46, 415, "Food");
    IntegrationTestEntity.insertPatternChord(412, 415, 0, "G minor");
    IntegrationTestEntity.insertPatternChord(414, 415, 8, "Ab minor");
    IntegrationTestEntity.insertPatternSequencePattern(416, 15, PatternType.Main, PatternState.Published, 1, 16, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertPatternMeme(47, 416, "Drink");
    IntegrationTestEntity.insertPatternMeme(149, 416, "Shame");
    IntegrationTestEntity.insertPatternChord(416, 416, 0, "C major");
    IntegrationTestEntity.insertPatternChord(418, 416, 8, "Bb major");

    // A basic beat
    IntegrationTestEntity.insertSequence(35, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertSequenceMeme(343, 35, "Basic");
    IntegrationTestEntity.insertPattern(315, 35, PatternType.Loop, PatternState.Published, 4, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(346, 315, "Heavy");

    // setup voice pattern events
    IntegrationTestEntity.insertVoice(1, 35, InstrumentType.Percussive, "drums");

    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    IntegrationTestEntity.insertPatternEvent(1, 315, 1, 0, 1, "BOOM", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(2, 315, 1, 1, 1, "SMACK", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(3, 315, 1, 2.5, 1, "BOOM", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(4, 315, 1, 3, 1, "SMACK", "G5", 0.1, 0.9);

    // basic beat second pattern
    IntegrationTestEntity.insertPattern(316, 35, PatternType.Loop, PatternState.Published, 4, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(347, 316, "Heavy");

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-segment-97898asdf7892.wav", new JSONObject());
    IntegrationTestEntity.insertSegment(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-segment-2807fdghj3272.wav", new JSONObject());

    // Chain "Test Print #1" has this segment that was just dubbed
    IntegrationTestEntity.insertSegment(3, 1, 2, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "Ab minor", 64, 0.30, 120, "chain-1-segment-198745hj78dfs.wav", new JSONObject()); // final key is based on pattern of main sequence
    IntegrationTestEntity.insertChoice(25, 3, 4, SequenceType.Macro, 1, 3); // macro-sequence current pattern is transposed to be Db minor
    IntegrationTestEntity.insertChoice(26, 3, 5, SequenceType.Main, 1, 1); // main-key of previous segment is transposed to match, Db minor
    IntegrationTestEntity.insertChoice(27, 3, 35, SequenceType.Rhythm, 0, -4);

    // Chain "Test Print #1" has a segment in dubbing state - Structure is complete
    IntegrationTestEntity.insertSegment(4, 1, 3, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "F minor", 16, 0.45, 125, "chain-1-segment-897hdfhjd7884.wav", new JSONObject());
    IntegrationTestEntity.insertSegmentMeme(101, 4, "Hindsight");
    IntegrationTestEntity.insertSegmentMeme(102, 4, "Chunky");
    IntegrationTestEntity.insertSegmentMeme(103, 4, "Regret");
    IntegrationTestEntity.insertSegmentMeme(104, 4, "Tangy");
    IntegrationTestEntity.insertChoice(101, 4, 3, SequenceType.Macro, 0, 4);
    IntegrationTestEntity.insertChoice(102, 4, 15, SequenceType.Main, 0, -2);
    IntegrationTestEntity.insertSegmentChord(101, 4, 0, "F minor");
    IntegrationTestEntity.insertSegmentChord(102, 4, 8, "Gb minor");

    // choice of rhythm-type sequence
    IntegrationTestEntity.insertChoice(103, 4, 35, SequenceType.Rhythm, 0, 5);

    // Chain "Test Print #1" is ready to begin
    IntegrationTestEntity.insertChain(2, 1, "Test Print #1", ChainType.Production, ChainState.Erase, Timestamp.from(new Date().toInstant().minusSeconds(300)), Timestamp.from(new Date().toInstant()), null);

    // Bind the library to the chains
    IntegrationTestEntity.insertChainLibrary(1, 1, 2);
    IntegrationTestEntity.insertChainLibrary(2, 2, 2);

    // Don't sleep between processing work
    System.setProperty("app.port", "9043");

    // Recur frequently, as a hack before implementing [#395] ExpectationOfWork client executes a `ChainDeleteJob` and enqueues `SegmentDeleteJob` for each Segment in the Chain
    System.setProperty("work.chain.delete.recur.seconds", "1");

    // Server App
    app = injector.getInstance(App.class);
    app.configureServer("io.xj.worker");

    // Attach Job Factory to App
    JobFactory jobFactory = injector.getInstance(JobFactory.class);
    app.setJobFactory(jobFactory);
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
    while (hasRemainingWork(WorkType.ChainErase) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }
    app.stop();

    assertEquals(0, injector.getInstance(ChainDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(1))).size());
    assertEquals(0, injector.getInstance(SegmentDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(1))).size());
    assertEquals(0, injector.getInstance(SegmentDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(2))).size());

    verify(amazonProvider).deleteS3Object("xj-segment-test", "chain-1-segment-97898asdf7892.wav");
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chain-1-segment-2807fdghj3272.wav");
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chain-1-segment-198745hj78dfs.wav");
    verify(amazonProvider).deleteS3Object("xj-segment-test", "chain-1-segment-897hdfhjd7884.wav");
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
      if (Objects.equals(type, work.getType())) total++;
    }
    return 0 < total;
  }


}
