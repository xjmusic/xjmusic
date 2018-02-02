// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.app.App;
import io.xj.core.dao.AudioDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.instrument.InstrumentType;
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

import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AudioEraseJobIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private App app;
  private static final int TEST_DURATION_SECONDS = 3;
  private static final int MILLIS_PER_SECOND = 1000;
  @Mock AmazonProvider amazonProvider;
  @Spy final WorkManager workManager = Guice.createInjector(new CoreModule()).getInstance(WorkManager.class);

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // inject mocks
    createInjector();

    // configs
    System.setProperty("audio.file.bucket", "xj-audio-test");

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

    // Instrument "808"
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, 1, "heavy");

    // Audio "Kick"
    IntegrationTestEntity.insertAudio(1, 1, "Erase", "Kick", "instrument-1-audio-asdg709a709835789agw73yh87.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Erase", "Snare", "instrument-1-audio-978as789dgih35hi897gjhyi8f.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 2, 3, 1, "SNARE", "Ab", 0.1, 0.8);

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
    System.clearProperty("audio.file.bucket");
  }

  /**
   [#294] Eraseworker finds Links and Audio in deleted state and actually deletes the records, child entities and S3 objects
   */
  @Test
  public void runWorker() throws Exception {
    app.start();

    app.getWorkManager().doAudioErase(BigInteger.valueOf(1));
    app.getWorkManager().doAudioErase(BigInteger.valueOf(2));

    Thread.sleep(TEST_DURATION_SECONDS * MILLIS_PER_SECOND);
    app.stop();

    assertNull( injector.getInstance(AudioDAO.class).readOne(Access.internal(), BigInteger.valueOf(1)));
    assertNull( injector.getInstance(AudioDAO.class).readOne(Access.internal(), BigInteger.valueOf(2)));

    verify(amazonProvider).deleteS3Object("xj-audio-test",
      "instrument-1-audio-asdg709a709835789agw73yh87.wav");
    verify(amazonProvider).deleteS3Object("xj-audio-test",
      "instrument-1-audio-978as789dgih35hi897gjhyi8f.wav");
  }

}
