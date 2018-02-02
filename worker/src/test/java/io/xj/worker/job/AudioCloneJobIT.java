// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.app.App;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.user_role.UserRoleType;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class AudioCloneJobIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private App app;
  private static final int TEST_DURATION_SECONDS = 1;
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

    // Library "house" and "pajamas"
    IntegrationTestEntity.insertLibrary(2, 1, "house");
    IntegrationTestEntity.insertLibrary(42, 1, "pajamas");

    // Instrument "808" and "2020"
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, 1, "heavy");
    IntegrationTestEntity.insertInstrument(12, 42, 2, "2020 Drums", InstrumentType.Percussive, 0.9);

    // Audio "Kick"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "instrument-1-audio-asdg709a709835789agw73yh87.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);
    IntegrationTestEntity.insertAudioChord(1, 1, 0, "Db7");

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "instrument-1-audio-978as789dgih35hi897gjhyi8f.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 2, 3, 1, "SNARE", "Ab", 0.1, 0.8);
    IntegrationTestEntity.insertAudioChord(2, 2, 0, "Gm9");

    // Newly cloned audios -- awaiting AudioClone job to run, and create their child entities
    IntegrationTestEntity.insertAudio(3, 1, "Published", "Kick", "instrument-1-audio-superAwesomeKey1.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudio(4, 12, "Published", "Kick", "instrument-1-audio-superAwesomeKey2.wav", 0.01, 2.123, 120.0, 440);

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
   [#294] Cloneworker finds Links and Audio in deleted state and actually deletes the records, child entities and S3 objects
   */
  @Test
  public void runWorker() throws Exception {
    app.start();
    app.getWorkManager().doAudioClone(BigInteger.valueOf(1), BigInteger.valueOf(3));
    app.getWorkManager().doAudioClone(BigInteger.valueOf(2), BigInteger.valueOf(4));

    Thread.sleep(TEST_DURATION_SECONDS * MILLIS_PER_SECOND);
    app.stop();

    // Verify existence of cloned audios
    Audio resultOne = injector.getInstance(AudioDAO.class).readOne(Access.internal(), BigInteger.valueOf(3));
    Audio resultTwo = injector.getInstance(AudioDAO.class).readOne(Access.internal(), BigInteger.valueOf(4));
    assertNotNull(resultOne);
    assertNotNull(resultTwo);

    // Verify existence of children of cloned audio #1
    Collection<AudioEvent> eventsOne = injector.getInstance(AudioEventDAO.class).readAll(Access.internal(), ImmutableList.of(resultOne.getId()));
    Collection<AudioChord> chordsOne = injector.getInstance(AudioChordDAO.class).readAll(Access.internal(), ImmutableList.of(resultOne.getId()));
    assertEquals(1, eventsOne.size());
    assertEquals(1, chordsOne.size());
    AudioEvent eventOne = eventsOne.iterator().next();
    assertEquals(2.5, eventOne.getPosition(),0.01);
    assertEquals(1, eventOne.getDuration(),0.01);
    assertEquals("KICK", eventOne.getInflection());
    assertEquals("Eb", eventOne.getNote());
    AudioChord chordOne = chordsOne.iterator().next();
    assertEquals(0, chordOne.getPosition(),0.01);
    assertEquals("Db7", chordOne.getName());

    // Verify existence of children of cloned audio #2
    Collection<AudioEvent> eventsTwo = injector.getInstance(AudioEventDAO.class).readAll(Access.internal(), ImmutableList.of(resultTwo.getId()));
    Collection<AudioChord> chordsTwo = injector.getInstance(AudioChordDAO.class).readAll(Access.internal(), ImmutableList.of(resultTwo.getId()));
    assertEquals(1, eventsTwo.size());
    assertEquals(1, chordsTwo.size());
    AudioEvent eventTwo = eventsTwo.iterator().next();
    assertEquals(3, eventTwo.getPosition(),0.01);
    assertEquals(1, eventTwo.getDuration(),0.01);
    assertEquals("SNARE", eventTwo.getInflection());
    assertEquals("Ab", eventTwo.getNote());
    AudioChord chordTwo = chordsTwo.iterator().next();
    assertEquals(0, chordTwo.getPosition(),0.01);
    assertEquals("Gm9", chordTwo.getName());

    // Verify objects copied within S3
    verify(amazonProvider, times(1)).copyS3Object("xj-audio-test",
      "instrument-1-audio-asdg709a709835789agw73yh87.wav", "xj-audio-test", "instrument-1-audio-superAwesomeKey1.wav");
    verify(amazonProvider, times(1)).copyS3Object("xj-audio-test",
      "instrument-1-audio-978as789dgih35hi897gjhyi8f.wav", "xj-audio-test", "instrument-1-audio-superAwesomeKey2.wav");
  }


}
