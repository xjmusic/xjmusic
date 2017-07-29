// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.eraseworker;

import io.xj.core.CoreModule;
import io.xj.core.app.App;
import io.xj.core.app.config.Config;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.idea.Idea;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.link.Link;
import io.xj.core.model.role.Role;
import io.xj.core.model.voice.Voice;
import io.xj.eraseworker.erase.EraseWorkerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Timestamp;
import java.util.Date;

import static io.xj.core.Tables.LINK;
import static io.xj.core.tables.Audio.AUDIO;
import static io.xj.core.tables.Chain.CHAIN;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class EraseworkerIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private App app;
  @Mock private AmazonProvider amazonProvider;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // inject mocks
    createInjector();

    // configs
    System.setProperty("link.file.bucket", "xj-link-test");
    System.setProperty("audio.file.bucket", "xj-audio-test");

    // Account "pilots"
    IntegrationTestEntity.insertAccount(1, "pilots");

    // Ted has "user" and "admin" roles, belongs to account "pilots", has "google" auth
    IntegrationTestEntity.insertUser(2, "ted", "ted@email.com", "http://pictures.com/ted.gif");
    IntegrationTestEntity.insertUserRole(1, 2, Role.ADMIN);

    // Sally has a "user" role and belongs to account "pilots"
    IntegrationTestEntity.insertUser(3, "sally", "sally@email.com", "http://pictures.com/sally.gif");
    IntegrationTestEntity.insertUserRole(2, 3, Role.USER);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Heavy, Deep to Metal" macro-idea in house library
    IntegrationTestEntity.insertIdea(4, 3, 2, Idea.MACRO, "Heavy, Deep to Metal", 0.5, "C", 120);
    IntegrationTestEntity.insertIdeaMeme(2, 4, "Heavy");
    // " phase offset 0
    IntegrationTestEntity.insertPhase(3, 4, 0, 0, "Start Deep", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Deep");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    // " phase offset 1
    IntegrationTestEntity.insertPhase(4, 4, 1, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Metal");
    IntegrationTestEntity.insertPhaseMeme(49, 4, "Deep");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");
    // " phase offset 2
    IntegrationTestEntity.insertPhase(5, 4, 2, 0, "Finish Metal", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertPhaseMeme(5, 4, "Metal");
    IntegrationTestEntity.insertPhaseChord(5, 5, 0, "Ab minor");

    // "Tech, Steampunk to Modern" macro-idea in house library
    IntegrationTestEntity.insertIdea(3, 3, 2, Idea.MACRO, "Tech, Steampunk to Modern", 0.5, "G minor", 120);
    IntegrationTestEntity.insertIdeaMeme(1, 3, "Tech");
    // # phase offset 0
    IntegrationTestEntity.insertPhase(1, 3, 0, 0, "Start Steampunk", 0.4, "G minor", 115);
    IntegrationTestEntity.insertPhaseMeme(1, 1, "Steampunk");
    IntegrationTestEntity.insertPhaseChord(1, 1, 0, "G minor");
    // # phase offset 1
    IntegrationTestEntity.insertPhase(2, 3, 1, 0, "Finish Modern", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(2, 2, "Modern");
    IntegrationTestEntity.insertPhaseChord(2, 2, 0, "C");

    // Main idea
    IntegrationTestEntity.insertIdea(5, 3, 2, Idea.MAIN, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertIdeaMeme(3, 5, "Attitude");
    // # phase offset 0
    IntegrationTestEntity.insertPhase(15, 5, 0, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Gritty");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "G major");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "Ab minor");
    // # phase offset 1
    IntegrationTestEntity.insertPhase(16, 5, 1, 16, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Gentle");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "C major");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "Bb minor");

    // Another Main idea to go to
    IntegrationTestEntity.insertIdea(15, 3, 2, Idea.MAIN, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertIdeaMeme(43, 15, "Temptation");
    IntegrationTestEntity.insertPhase(415, 15, 0, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(46, 415, "Food");
    IntegrationTestEntity.insertPhaseChord(412, 415, 0, "G minor");
    IntegrationTestEntity.insertPhaseChord(414, 415, 8, "Ab minor");
    IntegrationTestEntity.insertPhase(416, 15, 1, 16, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(47, 416, "Drink");
    IntegrationTestEntity.insertPhaseMeme(149, 416, "Shame");
    IntegrationTestEntity.insertPhaseChord(416, 416, 0, "C major");
    IntegrationTestEntity.insertPhaseChord(418, 416, 8, "Bb major");

    // A basic beat
    IntegrationTestEntity.insertIdea(35, 3, 2, Idea.RHYTHM, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertIdeaMeme(343, 35, "Basic");
    IntegrationTestEntity.insertPhase(315, 35, 0, 4, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(346, 315, "Heavy");

    // setup voice phase events
    IntegrationTestEntity.insertVoice(1, 315, Voice.PERCUSSIVE, "drums");

    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    IntegrationTestEntity.insertVoiceEvent(1, 1, 0, 1, "BOOM", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertVoiceEvent(2, 1, 1, 1, "SMACK", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertVoiceEvent(3, 1, 2.5, 1, "BOOM", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertVoiceEvent(4, 1, 3, 1, "SMACK", "G5", 0.1, 0.9);

    // basic beat second phase
    IntegrationTestEntity.insertPhase(316, 35, 1, 4, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(347, 316, "Heavy");

    // Chain "Test Print #1" has 5 total links
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.ERASE, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, Link.DUBBING, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-2807fdghj3272.wav");

    // Chain "Test Print #1" has this link that was just dubbed
    IntegrationTestEntity.insertLink(3, 1, 2, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "Ab minor", 64, 0.30, 120, "chain-1-link-198745hj78dfs.wav"); // final key is based on phase of main idea
    IntegrationTestEntity.insertChoice(25, 3, 4, Choice.MACRO, 1, 3); // macro-idea current phase is transposed to be Db minor
    IntegrationTestEntity.insertChoice(26, 3, 5, Choice.MAIN, 1, 1); // main-key of previous link is transposed to match, Db minor
    IntegrationTestEntity.insertChoice(27, 3, 35, Choice.RHYTHM, 0, -4);

    // Chain "Test Print #1" has a link in dubbing state - Structure is complete
    IntegrationTestEntity.insertLink(4, 1, 3, Link.DUBBING, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "F minor", 16, 0.45, 125, "chain-1-link-897hdfhjd7884.wav");
    IntegrationTestEntity.insertLinkMeme(101, 4, "Hindsight");
    IntegrationTestEntity.insertLinkMeme(102, 4, "Chunky");
    IntegrationTestEntity.insertLinkMeme(103, 4, "Regret");
    IntegrationTestEntity.insertLinkMeme(104, 4, "Tangy");
    IntegrationTestEntity.insertChoice(101, 4, 3, Choice.MACRO, 0, 4);
    IntegrationTestEntity.insertChoice(102, 4, 15, Choice.MAIN, 0, -2);
    IntegrationTestEntity.insertLinkChord(101, 4, 0, "F minor");
    IntegrationTestEntity.insertLinkChord(102, 4, 8, "Gb minor");

    // choice of rhythm-type idea
    IntegrationTestEntity.insertChoice(103, 4, 35, Choice.RHYTHM, 0, 5);

    // Instrument "808"
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", Instrument.PERCUSSIVE, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, 1, "heavy");

    // Audio "Kick"
    IntegrationTestEntity.insertAudio(1, 1, "Erase", "Kick", "instrument-1-audio-asdg709a709835789agw73yh87.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Erase", "Snare", "instrument-1-audio-978as789dgih35hi897gjhyi8f.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 2, 3, 1, "SNARE", "Ab", 0.1, 0.8);

    // Chain "Test Print #1" is ready to begin
    IntegrationTestEntity.insertChain(2, 1, "Test Print #1", Chain.PRODUCTION, Chain.ERASE, Timestamp.from(new Date().toInstant().minusSeconds(300)), Timestamp.from(new Date().toInstant()));

    // Bind the library to the chains
    IntegrationTestEntity.insertChainLibrary(1, 1, 2);
    IntegrationTestEntity.insertChainLibrary(2, 2, 2);

    // Don't sleep between processing work
    System.setProperty("app.port", "9043");

    // Server App
    app = injector.getInstance(App.class);
    app.configureServer("io.xj.eraseworker");

    // Worker (Guice) factory
    EraseWorkerFactory eraseWorkerFactory = injector.getInstance(EraseWorkerFactory.class);

    // Chain-Erase Workload
    app.registerSimpleWorkload(
      "Erase Chains",
      eraseWorkerFactory.chainEraseWorker(Config.workBatchSize()));

    // Audio-Erase Workload
    app.registerSimpleWorkload(
      "Erase Audio",
      eraseWorkerFactory.audioEraseWorker(Config.workBatchSize()));

  }

  private void createInjector() {
    injector = Guice.createInjector(new EraseworkerModule(), Modules.override(new CoreModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
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
  public void eraseworkerRun() throws Exception {
    int testDurationSeconds = 5;

    app.start();
    Thread.sleep(testDurationSeconds * 1000);
    app.stop();

    assertEquals(0, IntegrationTestService.getDb()
      .selectFrom(CHAIN)
      .fetch().size());

    assertEquals(0, IntegrationTestService.getDb()
      .selectFrom(LINK)
      .fetch().size());

    assertEquals(0, IntegrationTestService.getDb()
      .selectFrom(AUDIO)
      .fetch().size());

    verify(amazonProvider).deleteS3Object("xj-audio-test",
      "instrument-1-audio-asdg709a709835789agw73yh87.wav");
    verify(amazonProvider).deleteS3Object("xj-audio-test",
      "instrument-1-audio-978as789dgih35hi897gjhyi8f.wav");
    verify(amazonProvider).deleteS3Object("xj-link-test", "chain-1-link-97898asdf7892.wav");
    verify(amazonProvider).deleteS3Object("xj-link-test", "chain-1-link-2807fdghj3272.wav");
    verify(amazonProvider).deleteS3Object("xj-link-test", "chain-1-link-198745hj78dfs.wav");
    verify(amazonProvider).deleteS3Object("xj-link-test", "chain-1-link-897hdfhjd7884.wav");
  }

}
