// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.app.App;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseType;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PatternCloneJobIT {
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
    IntegrationTestEntity.insertPattern(1, 2, 2, PatternType.Rhythm, "808 Drums", 0.9, "C", 120);
    IntegrationTestEntity.insertPatternMeme(1, 1, "heavy");
    IntegrationTestEntity.insertPattern(12, 2, 42, PatternType.Rhythm, "2020 Drums", 0.9, "G", 120);

    // Phase "Kick"
    IntegrationTestEntity.insertPhase(1, 1, PhaseType.Loop, 0, 16, "Verse", 0.5, "C", 120.0);
    IntegrationTestEntity.insertVoice(1, 1, InstrumentType.Percussive, "KICK");
    IntegrationTestEntity.insertPhaseChord(1, 1, 0, "Db7");

    // Phase "Snare"
    IntegrationTestEntity.insertPhase(2, 1, PhaseType.Loop, 1, 16, "Verse", 0.5, "C", 120.0);
    IntegrationTestEntity.insertVoice(2, 1, InstrumentType.Percussive, "SNARE");
    IntegrationTestEntity.insertPhaseChord(2, 2, 0, "Gm9");

    // Newly cloned pattern -- awaiting PatternClone job to run, and create its child entities
    IntegrationTestEntity.insertPattern(14, 2, 42, PatternType.Rhythm, "808 Drums Clone Y'all", 0.9, "D", 120);

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
    when(amazonProvider.generateKey(any(), any())).thenReturn("superAwesomeKey123");

    app.start();
    app.getWorkManager().schedulePatternClone(0, BigInteger.valueOf(1), BigInteger.valueOf(14));

    Thread.sleep(TEST_DURATION_SECONDS * MILLIS_PER_SECOND);
    app.stop();

    // Verify existence of cloned pattern
    Pattern result = injector.getInstance(PatternDAO.class).readOne(Access.internal(), BigInteger.valueOf(14));
    assertNotNull(result);

    // Verify existence of cloned memes
    Collection<PatternMeme> memes = injector.getInstance(PatternMemeDAO.class).readAll(Access.internal(), result.getId());
    assertEquals(1, memes.size());

    // Verify existence of cloned phases
    Collection<Phase> phases = injector.getInstance(PhaseDAO.class).readAll(Access.internal(), result.getId());
    assertEquals(2, phases.size());

    // Verify enqueued phase clone jobs
    verify(workManager).schedulePhaseClone(eq(0), eq(BigInteger.valueOf(1)), any());
    verify(workManager).schedulePhaseClone(eq(0), eq(BigInteger.valueOf(2)), any());
  }

}
