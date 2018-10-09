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
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.util.TimestampUTC;
import io.xj.craft.CraftModule;
import io.xj.dub.DubModule;
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
public class MultiJobStressIT {
  private static final int MILLIS_PER_SECOND = 1000;
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  AmazonProvider amazonProvider;
  private Injector injector;
  private App app;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // inject with mocks
    injector = Guice.createInjector(Modules.override(new CoreModule(), new WorkerModule(), new CraftModule(), new DubModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));

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
    IntegrationTestEntity.insertPattern(315, 35, PatternType.Loop, PatternState.Published, 16, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(346, 315, "Heavy");

    // For cloning-related test: Sequence "808" and "2020"
    IntegrationTestEntity.insertSequence(1001, 2, 2, SequenceType.Rhythm, SequenceState.Published, "808 Drums", 0.9, "G", 120);
    IntegrationTestEntity.insertSequenceMeme(1001, 1001, "heavy");
    IntegrationTestEntity.insertVoice(1001, 1001, InstrumentType.Percussive, "Kick Drum");
    IntegrationTestEntity.insertVoice(1002, 1001, InstrumentType.Percussive, "Snare Drum");
    IntegrationTestEntity.insertSequence(10012, 2, 2, SequenceType.Rhythm, SequenceState.Published, "2020 Drums", 0.9, "G", 120);
    IntegrationTestEntity.insertVoice(1003, 10012, InstrumentType.Percussive, "Kack Dram");
    IntegrationTestEntity.insertVoice(1004, 10012, InstrumentType.Percussive, "Snarr Dram");

    // For cloning-related test: Pattern "Verse"
    IntegrationTestEntity.insertPattern(1001, 1001, PatternType.Loop, PatternState.Published, 16, "Verse 1", 0.5, "G", 120);
    IntegrationTestEntity.insertPatternMeme(1001, 1001, "GREEN");
    IntegrationTestEntity.insertPatternChord(1001, 1001, 0, "Db7");
    IntegrationTestEntity.insertPatternEvent(100101, 1001, 1001, 0.0, 1.0, "KICK", "C5", 1.0, 1.0);
    IntegrationTestEntity.insertPatternEvent(100102, 1001, 1002, 1.0, 1.0, "SNARE", "C5", 1.0, 1.0);

    // For cloning-related test: Pattern "Verse"
    IntegrationTestEntity.insertPattern(1002, 1001, PatternType.Loop, PatternState.Published, 16, "Verse 2", 0.5, "G", 120);
    IntegrationTestEntity.insertPatternMeme(1002, 1002, "YELLOW");
    IntegrationTestEntity.insertPatternChord(1002, 1002, 0, "Gm9");
    IntegrationTestEntity.insertPatternEvent(100103, 1002, 1001, 0.0, 1.0, "KICK", "C5", 1.0, 1.0);
    IntegrationTestEntity.insertPatternEvent(100104, 1002, 1002, 1.0, 1.0, "SNARE", "C5", 1.0, 1.0);

    // Newly cloned patterns -- awaiting PatternClone job to run, and create their child entities
    IntegrationTestEntity.insertPattern(1003, 1001, PatternType.Loop, PatternState.Published, 16, "Verse 34", 0.5, "G", 120);
    IntegrationTestEntity.insertPattern(1004, 10012, PatternType.Loop, PatternState.Published, 16, "Verse 79", 0.5, "G", 120);

    // Chain "Test Print #1" is ready to begin
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, TimestampUTC.nowMinusSeconds(1000), null, null);

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 1, 2);

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

    // Start app, send chain fabrication message to queue
    app.start();
    app.getWorkManager().startChainFabrication(BigInteger.valueOf(1));

    // wait until the middle of chain fabrication, then begin another job
    Thread.sleep(5 * MILLIS_PER_SECOND);
    app.getWorkManager().doPatternClone(BigInteger.valueOf(1001), BigInteger.valueOf(1003));
    app.getWorkManager().doPatternClone(BigInteger.valueOf(1002), BigInteger.valueOf(1004));

    // wait for work, stop chain fabrication, stop app
    Thread.sleep(5 * MILLIS_PER_SECOND);
    app.getWorkManager().stopChainFabrication(BigInteger.valueOf(1));
    app.stop();

    // Verify chain shipped segments
    int assertShippedSegmentsMinimum = 2;
    verify(amazonProvider, atLeast(assertShippedSegmentsMinimum)).putS3Object(eq("/tmp/chain-1-segment-12345.ogg"), eq("xj-segment-test"), any());
    Collection<Segment> result = injector.getInstance(SegmentDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(1)));
    assertTrue(assertShippedSegmentsMinimum < result.size());

    // Verify existence of cloned patterns
    Pattern resultOne = injector.getInstance(PatternDAO.class).readOne(Access.internal(), BigInteger.valueOf(1003));
    Pattern resultTwo = injector.getInstance(PatternDAO.class).readOne(Access.internal(), BigInteger.valueOf(1004));
    assertNotNull(resultOne);
    assertNotNull(resultTwo);

    // Verify existence of children of cloned pattern #1
    Collection<PatternMeme> memesOne = injector.getInstance(PatternMemeDAO.class).readAll(Access.internal(), ImmutableList.of(resultOne.getId()));
    Collection<PatternChord> chordsOne = injector.getInstance(PatternChordDAO.class).readAll(Access.internal(), ImmutableList.of(resultOne.getId()));
    Collection<PatternEvent> eventsOne = injector.getInstance(PatternEventDAO.class).readAll(Access.internal(), ImmutableList.of(resultOne.getId()));
    assertEquals(1, memesOne.size());
    assertEquals(1, chordsOne.size());
    assertEquals(2, eventsOne.size());
    PatternMeme memeOne = memesOne.iterator().next();
    assertEquals("Green", memeOne.getName());
    PatternChord chordOne = chordsOne.iterator().next();
    assertEquals(0, chordOne.getPosition(), 0.01);
    assertEquals("Db7", chordOne.getName());

    // Verify existence of children of cloned pattern #2
    Collection<PatternMeme> memesTwo = injector.getInstance(PatternMemeDAO.class).readAll(Access.internal(), ImmutableList.of(resultTwo.getId()));
    Collection<PatternChord> chordsTwo = injector.getInstance(PatternChordDAO.class).readAll(Access.internal(), ImmutableList.of(resultTwo.getId()));
    Collection<PatternEvent> eventsTwo = injector.getInstance(PatternEventDAO.class).readAll(Access.internal(), ImmutableList.of(resultTwo.getId()));
    assertEquals(1, memesTwo.size());
    assertEquals(1, chordsTwo.size());
    assertEquals(2, eventsTwo.size());
    PatternMeme memeTwo = memesTwo.iterator().next();
    assertEquals("Yellow", memeTwo.getName());
    PatternChord chordTwo = chordsTwo.iterator().next();
    assertEquals(0, chordTwo.getPosition(), 0.01);
    assertEquals("Gm9", chordTwo.getName());

  }

}
