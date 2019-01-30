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
import io.xj.core.dao.PatternDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.model.work.Work;
import io.xj.core.model.work.WorkType;
import io.xj.core.work.WorkManager;
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
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.util.Objects;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 [#153976888] PatternErase job erase a Pattern in the background, in order to keep the UI functioning at a reasonable speed.
 */
@RunWith(MockitoJUnitRunner.class)
public class SequenceEraseJobIT {
  private static final int MILLIS_PER_SECOND = 1000;
  private static final int MAXIMUM_TEST_WAIT_MILLIS = 30 * MILLIS_PER_SECOND;
  @Spy
  private final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);
  @Rule
  public ExpectedException failure = ExpectedException.none();
  long startTime = System.currentTimeMillis();
  private Injector injector;
  private App app;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // inject mocks
    createInjector();

    // Account "pilots"
    IntegrationTestEntity.insertAccount(1, "pilots");

    // Ted has "user" and "admin" roles, belongs to account "pilots", has "google" auth
    IntegrationTestEntity.insertUser(2, "ted", "ted@email.com", "http://pictures.com/ted.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Sally has a "user" role and belongs to account "pilots"
    IntegrationTestEntity.insertUser(3, "sally", "sally@email.com", "http://pictures.com/sally.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house" and "fleeces"
    IntegrationTestEntity.insertLibrary(2, 1, "house");
    IntegrationTestEntity.insertLibrary(42, 1, "fleeces");

    // Sequence "808" and "2020"
    IntegrationTestEntity.insertSequence(1, 2, 2, SequenceType.Rhythm, SequenceState.Published, "808 Drums", 0.9, "G", 120);
    IntegrationTestEntity.insertSequenceMeme(1, 1, "heavy");
    IntegrationTestEntity.insertVoice(1, 1, InstrumentType.Percussive, "Kick Drum");
    IntegrationTestEntity.insertVoice(2, 1, InstrumentType.Percussive, "Snare Drum");
    IntegrationTestEntity.insertSequence(12, 2, 42, SequenceType.Rhythm, SequenceState.Published, "2020 Drums", 0.9, "G", 120);
    IntegrationTestEntity.insertVoice(3, 12, InstrumentType.Percussive, "Kack Dram");
    IntegrationTestEntity.insertVoice(4, 12, InstrumentType.Percussive, "Snarr Dram");

    // Pattern "Verse"
    IntegrationTestEntity.insertPatternAndSequencePattern(1, 1, PatternType.Loop, PatternState.Published, 0, 16, "Verse 1", 0.5, "G", 120);
    IntegrationTestEntity.insertSequencePatternMeme(1, 1, 1, "GREEN");
    IntegrationTestEntity.insertPatternChord(1, 1, 0, "Db7");
    IntegrationTestEntity.insertPatternEvent(101, 1, 1, 0.0, 1.0, "KICK", "C5", 1.0, 1.0);
    IntegrationTestEntity.insertPatternEvent(102, 1, 2, 1.0, 1.0, "SNARE", "C5", 1.0, 1.0);

    // Pattern "Verse"
    IntegrationTestEntity.insertPatternAndSequencePattern(2, 1, PatternType.Loop, PatternState.Published, 1, 16, "Verse 2", 0.5, "G", 120);
    IntegrationTestEntity.insertSequencePatternMeme(2, 1, 2, "YELLOW");
    IntegrationTestEntity.insertPatternChord(2, 2, 0, "Gm9");
    IntegrationTestEntity.insertPatternEvent(103, 2, 1, 0.0, 1.0, "KICK", "C5", 1.0, 1.0);
    IntegrationTestEntity.insertPatternEvent(104, 2, 2, 1.0, 1.0, "SNARE", "C5", 1.0, 1.0);

    // Newly cloned patterns -- awaiting PatternClone job to run, and create their child entities
    IntegrationTestEntity.insertPattern(3, 1, PatternType.Loop, PatternState.Published, 16, "Verse 34", 0.5, "G", 120);
    IntegrationTestEntity.insertPattern(4, 12, PatternType.Loop, PatternState.Published, 16, "Verse 79", 0.5, "G", 120);

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
          bind(WorkManager.class).toInstance(workManager);
        }
      }));
  }

  @After
  public void tearDown() {
  }

  @Test
  public void runWorker() throws Exception {
    app.getWorkManager().doSequenceErase(BigInteger.valueOf(1));
    assertTrue(hasRemainingWork(WorkType.SequenceErase));

    // Start app, wait for work, stop app
    app.start();
    while ((hasRemainingWork(WorkType.SequenceErase) || hasAnyChildPatterns(BigInteger.valueOf(1))) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }
    app.stop();

    assertNull(injector.getInstance(PatternDAO.class).readOne(Access.internal(), BigInteger.valueOf(1)));
    assertNull(injector.getInstance(PatternDAO.class).readOne(Access.internal(), BigInteger.valueOf(2)));
    assertNull(injector.getInstance(PatternDAO.class).readOne(Access.internal(), BigInteger.valueOf(3)));
  }

  /**
   Whether the specified sequence has any child patterns

   @param sequenceId to test
   @return true if has any child patterns
   */
  private boolean hasAnyChildPatterns(BigInteger sequenceId) throws Exception {
    return !injector.getInstance(PatternDAO.class).readAll(Access.internal(), ImmutableList.of(sequenceId)).isEmpty();
  }


  /**
   [#155682779] Engineer expects SequenceErase job to be cancelled if the Sequence has already been deleted.
   */
  @Test
  public void cancelsJobIfEntityDoesNotExist() throws Exception {
    app.start();

    app.getWorkManager().doSequenceErase(BigInteger.valueOf(712));

    while (hasRemainingWork(WorkType.SequenceErase) && isWithinTimeLimit()) {
      Thread.sleep(MILLIS_PER_SECOND);
    }
    app.stop();
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
