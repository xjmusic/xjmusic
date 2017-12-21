// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.work.clone;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.app.App;
import io.xj.core.dao.PhaseChordDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase_chord.PhaseChord;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.work.WorkManager;
import io.xj.craft.CraftModule;
import io.xj.dub.DubModule;
import io.xj.worker.WorkerModule;

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
public class PhaseCloneIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private App app;
  private static final int TEST_DURATION_SECONDS = 1;
  private static final int MILLIS_PER_SECOND = 1000;
  @Mock AmazonProvider amazonProvider;
  @Spy final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // inject mocks
    createInjector();

    // configs
    System.setProperty("phase.file.bucket", "xj-phase-test");

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

    // Pattern "808" and "2020"
    IntegrationTestEntity.insertPattern(1, 2, 2, PatternType.Rhythm, "808 Drums", 0.9, "G", 120);
    IntegrationTestEntity.insertPatternMeme(1, 1, "heavy");
    IntegrationTestEntity.insertPattern(12, 2, 42, PatternType.Rhythm, "2020 Drums", 0.9, "G", 120);

    // Phase "Verse"
    IntegrationTestEntity.insertPhase(1, 1, 0, 16, "Verse 1", 0.5, "G", 120);
    IntegrationTestEntity.insertPhaseMeme(1, 1, "GREEN");
    IntegrationTestEntity.insertPhaseChord(1, 1, 0, "Db7");

    // Phase "Verse"
    IntegrationTestEntity.insertPhase(2, 1, 0, 16, "Verse 2", 0.5, "G", 120);
    IntegrationTestEntity.insertPhaseMeme(2, 2, "YELLOW");
    IntegrationTestEntity.insertPhaseChord(2, 2, 0, "Gm9");

    // Newly cloned phases -- awaiting PhaseClone job to run, and create their child entities
    IntegrationTestEntity.insertPhase(3, 1, 0, 16, "Verse 34", 0.5, "G", 120);
    IntegrationTestEntity.insertPhase(4, 12, 0, 16, "Verse 79", 0.5, "G", 120);

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

    System.clearProperty("link.file.bucket");
    System.clearProperty("phase.file.bucket");
  }

  /**
   [#294] Cloneworker finds Links and Phase in deleted state and actually deletes the records, child entities and S3 objects
   */
  @Test
  public void runWorker() throws Exception {
    app.start();
    app.getWorkManager().schedulePhaseClone(0, BigInteger.valueOf(1), BigInteger.valueOf(3));
    app.getWorkManager().schedulePhaseClone(0, BigInteger.valueOf(2), BigInteger.valueOf(4));

    Thread.sleep(TEST_DURATION_SECONDS * MILLIS_PER_SECOND);
    app.stop();

    // Verify existence of cloned phases
    Phase resultOne = injector.getInstance(PhaseDAO.class).readOne(Access.internal(), BigInteger.valueOf(3));
    Phase resultTwo = injector.getInstance(PhaseDAO.class).readOne(Access.internal(), BigInteger.valueOf(4));
    assertNotNull(resultOne);
    assertNotNull(resultTwo);

    // Verify existence of children of cloned phase #1
    Collection<PhaseMeme> memesOne = injector.getInstance(PhaseMemeDAO.class).readAll(Access.internal(), resultOne.getId());
    Collection<PhaseChord> chordsOne = injector.getInstance(PhaseChordDAO.class).readAll(Access.internal(), resultOne.getId());
    assertEquals(1, memesOne.size());
    assertEquals(1, chordsOne.size());
    PhaseMeme memeOne = memesOne.iterator().next();
    assertEquals("Green", memeOne.getName());
    PhaseChord chordOne = chordsOne.iterator().next();
    assertEquals(0, chordOne.getPosition(), 0.01);
    assertEquals("Db7", chordOne.getName());

    // Verify existence of children of cloned phase #2
    Collection<PhaseMeme> memesTwo = injector.getInstance(PhaseMemeDAO.class).readAll(Access.internal(), resultTwo.getId());
    Collection<PhaseChord> chordsTwo = injector.getInstance(PhaseChordDAO.class).readAll(Access.internal(), resultTwo.getId());
    assertEquals(1, memesTwo.size());
    assertEquals(1, chordsTwo.size());
    PhaseMeme memeTwo = memesTwo.iterator().next();
    assertEquals("Yellow", memeTwo.getName());
    PhaseChord chordTwo = chordsTwo.iterator().next();
    assertEquals(0, chordTwo.getPosition(), 0.01);
    assertEquals("Gm9", chordTwo.getName());
  }

}
