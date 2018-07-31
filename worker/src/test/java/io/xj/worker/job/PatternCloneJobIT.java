// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.app.App;
import io.xj.core.dao.PatternChordDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.dao.PatternEventDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_chord.PatternChord;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.work.WorkManager;
import io.xj.craft.CraftModule;
import io.xj.dub.DubModule;
import io.xj.worker.WorkerModule;

import com.google.common.collect.ImmutableList;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import net.greghaines.jesque.worker.JobFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public class PatternCloneJobIT {
  private static final int TEST_DURATION_SECONDS = 2;
  private static final int MILLIS_PER_SECOND = 1000;
  @Spy final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  @Rule public ExpectedException failure = ExpectedException.none();
  @Mock AmazonProvider amazonProvider;
  private Injector injector;
  private App app;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // inject mocks
    createInjector();

    // configs
    System.setProperty("pattern.file.bucket", "xj-pattern-test");

    // Account "pilots"
    IntegrationTestEntity.insertAccount(1, "pilots");

    // Ted has "user" and "admin" roles, belongs to account "pilots", has "google" auth
    IntegrationTestEntity.insertUser(2, "ted", "ted@email.com", "http://pictures.com/ted.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Sally has a "user" role and belongs to account "pilots"
    IntegrationTestEntity.insertUser(3, "sally", "sally@email.com", "http://pictures.com/sally.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house" and "pajamas"
    IntegrationTestEntity.insertLibrary(2, 1, "house");
    IntegrationTestEntity.insertLibrary(42, 1, "pajamas");

    // Sequence "808" and "2020"
    IntegrationTestEntity.insertSequence(1, 2, 2, SequenceType.Rhythm, SequenceState.Published, "808 Drums", 0.9, "G", 120);
    IntegrationTestEntity.insertSequenceMeme(1, 1, "heavy");
    IntegrationTestEntity.insertVoice(1, 1, InstrumentType.Percussive, "Kick Drum");
    IntegrationTestEntity.insertVoice(2, 1, InstrumentType.Percussive, "Snare Drum");
    IntegrationTestEntity.insertSequence(12, 2, 42, SequenceType.Rhythm, SequenceState.Published, "2020 Drums", 0.9, "G", 120);
    IntegrationTestEntity.insertVoice(3, 12, InstrumentType.Percussive, "Kack Dram");
    IntegrationTestEntity.insertVoice(4, 12, InstrumentType.Percussive, "Snarr Dram");

    // Pattern "Verse"
    IntegrationTestEntity.insertPattern(1, 1, PatternType.Loop, PatternState.Published, 0, 16, "Verse 1", 0.5, "G", 120);
    IntegrationTestEntity.insertPatternMeme(1, 1, "GREEN");
    IntegrationTestEntity.insertPatternChord(1, 1, 0, "Db7");
    IntegrationTestEntity.insertPatternEvent(101, 1, 1, 0.0, 1.0, "KICK", "C5", 1.0, 1.0);
    IntegrationTestEntity.insertPatternEvent(102, 1, 2, 1.0, 1.0, "SNARE", "C5", 1.0, 1.0);

    // Pattern "Verse"
    IntegrationTestEntity.insertPattern(2, 1, PatternType.Loop, PatternState.Published, 0, 16, "Verse 2", 0.5, "G", 120);
    IntegrationTestEntity.insertPatternMeme(2, 2, "YELLOW");
    IntegrationTestEntity.insertPatternChord(2, 2, 0, "Gm9");
    IntegrationTestEntity.insertPatternEvent(103, 2, 1, 0.0, 1.0, "KICK", "C5", 1.0, 1.0);
    IntegrationTestEntity.insertPatternEvent(104, 2, 2, 1.0, 1.0, "SNARE", "C5", 1.0, 1.0);

    // Newly cloned patterns -- awaiting PatternClone job to run, and create their child entities
    IntegrationTestEntity.insertPattern(3, 1, PatternType.Loop, PatternState.Published, 0, 16, "Verse 34", 0.5, "G", 120);
    IntegrationTestEntity.insertPattern(4, 12, PatternType.Loop, PatternState.Published, 0, 16, "Verse 79", 0.5, "G", 120);

    // Don't sleep between processing work
    System.setProperty("app.port", "9043");

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
          bind(WorkManager.class).toInstance(workManager);
        }
      }));
  }

  @After
  public void tearDown() throws Exception {
    app = null;
    injector = null;

    System.clearProperty("segment.file.bucket");
    System.clearProperty("pattern.file.bucket");
  }

  /**
   [#294] Cloneworker finds Segments and Pattern in deleted state and actually deletes the records, child entities and S3 objects
   */
  @Test
  public void runWorker() throws Exception {
    app.start();
    app.getWorkManager().doPatternClone(BigInteger.valueOf(1), BigInteger.valueOf(3));
    app.getWorkManager().doPatternClone(BigInteger.valueOf(2), BigInteger.valueOf(4));

    Thread.sleep(TEST_DURATION_SECONDS * MILLIS_PER_SECOND);
    app.stop();

    // Verify existence of cloned patterns
    Pattern resultOne = injector.getInstance(PatternDAO.class).readOne(Access.internal(), BigInteger.valueOf(3));
    Pattern resultTwo = injector.getInstance(PatternDAO.class).readOne(Access.internal(), BigInteger.valueOf(4));
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
