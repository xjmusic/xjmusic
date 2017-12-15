// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.work.dub;

import io.xj.core.CoreModule;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisFactory;
import io.xj.mixer.util.InternalResource;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import io.xj.worker.WorkerModule;
import io.xj.core.dub.DubFactory;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DubMasterContinueIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector;
  private DubFactory dubFactory;
  private BasisFactory basisFactory;
  private static final String testResourceFilePath = "test_audio" + File.separator + "F32LSB_48kHz_Stereo.wav";

  // Testing entities for reference
  private Link link4;
  @Mock AmazonProvider amazonProvider;

  @Before
  public void setUp() throws Exception {
    createInjector();

    IntegrationTestEntity.deleteAll();

    // Account "elephants"
    IntegrationTestEntity.insertAccount(1, "elephants");

    // Jen has "user" and "admin" roles, belongs to account "elephants", has "google" auth
    IntegrationTestEntity.insertUser(2, "jen", "jen@email.com", "http://pictures.com/jen.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Fred has a "user" role and belongs to account "elephants"
    IntegrationTestEntity.insertUser(3, "fred", "fred@email.com", "http://pictures.com/fred.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Classic, Wild to Cozy" macro-pattern in house library
    IntegrationTestEntity.insertPattern(4, 3, 2, PatternType.Macro, "Classic, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertPatternMeme(2, 4, "Classic");
    IntegrationTestEntity.insertPhase(3, 4, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPhase(4, 4, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");

    // Main pattern
    IntegrationTestEntity.insertPattern(5, 3, 2, PatternType.Main, "Main Jam", 0.2, "Gb minor", 140);
    IntegrationTestEntity.insertPatternMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPhase(15, 5, 0, 16, "Intro", 0.5, "Gb minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Cloudy");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "Gb minor");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPhase(16, 5, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Rosy");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "G major");

    // A basic beat
    IntegrationTestEntity.insertPattern(35, 3, 2, PatternType.Rhythm, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertPatternMeme(343, 35, "Basic");

    // basic beat first phase
    IntegrationTestEntity.insertPhase(316, 35, 0, 16, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(347, 316, "Heavy");

    // setup voice second phase
    IntegrationTestEntity.insertPhase(315, 35, 1, 4, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(346, 315, "Heavy");
    IntegrationTestEntity.insertVoice(1, 315, InstrumentType.Percussive, "drums");

    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    IntegrationTestEntity.insertVoiceEvent(1, 1, 0, 1, "BOOM", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertVoiceEvent(2, 1, 1, 1, "SMACK", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertVoiceEvent(3, 1, 2.5, 1, "BOOM", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertVoiceEvent(4, 1, 3, 1, "SMACK", "G5", 0.1, 0.9);

    // detail pattern
    IntegrationTestEntity.insertPattern(7, 3, 2, PatternType.Detail, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Test Print #1" has 5 total links
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892");
    IntegrationTestEntity.insertLink(2, 1, 1, LinkState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892");

    // Chain "Test Print #1" has this link that was just dubbed
    IntegrationTestEntity.insertLink(3, 1, 2, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-link-97898asdf7892");
    IntegrationTestEntity.insertChoice(25, 3, 4, PatternType.Macro, 1, 3);
    IntegrationTestEntity.insertChoice(26, 3, 5, PatternType.Main, 0, 5);
    IntegrationTestEntity.insertChoice(27, 3, 35, PatternType.Rhythm, 0, 5);

    // Chain "Test Print #1" is dubbing - Structure is complete
    link4 = IntegrationTestEntity.insertLink(4, 1, 3, LinkState.Dubbing, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "D major", 16, 0.45, 120, "chain-1-link-97898asdf7892");
    IntegrationTestEntity.insertLinkMeme(101, 4, "Cozy");
    IntegrationTestEntity.insertLinkMeme(102, 4, "Classic");
    IntegrationTestEntity.insertLinkMeme(103, 4, "Outlook");
    IntegrationTestEntity.insertLinkMeme(104, 4, "Rosy");
    IntegrationTestEntity.insertChoice(101, 4, 4, PatternType.Macro, 1, 3);
    IntegrationTestEntity.insertChoice(102, 4, 5, PatternType.Main, 1, -5);
    IntegrationTestEntity.insertLinkChord(101, 4, 0, "A minor");
    IntegrationTestEntity.insertLinkChord(102, 4, 8, "D major");

    // choice of rhythm-type pattern
    IntegrationTestEntity.insertChoice(103, 4, 35, PatternType.Rhythm, 1, 2);

    // Instrument "808"
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, 1, "heavy");

    // Audio "Kick"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "19801735098q47895897895782138975898", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "198017350afghjkjhaskjdfjhk975898", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 2, 3, 1, "SNARE", "Ab", 0.1, 0.8);

    // insert arrangement of choice 103
    IntegrationTestEntity.insertArrangement(1, 103, 1, 1);

    // future: insert 8 picks of audio 1
    IntegrationTestEntity.insertPick(1, 1, 1, 0, 1, 1, 440);
    IntegrationTestEntity.insertPick(2, 1, 1, 1, 1, 1, 440);
    IntegrationTestEntity.insertPick(3, 1, 1, 2, 1, 1, 440);
    IntegrationTestEntity.insertPick(4, 1, 1, 3, 1, 1, 440);
    IntegrationTestEntity.insertPick(5, 1, 1, 4, 1, 1, 440);
    IntegrationTestEntity.insertPick(6, 1, 1, 5, 1, 1, 440);
    IntegrationTestEntity.insertPick(7, 1, 1, 6, 1, 1, 440);
    IntegrationTestEntity.insertPick(8, 1, 1, 7, 1, 1, 440);

    // future: insert 4 picks of audio 2
    IntegrationTestEntity.insertPick(9, 1, 2, 1, 1, 1, 600);
    IntegrationTestEntity.insertPick(10, 1, 2, 3, 1, 1, 600);
    IntegrationTestEntity.insertPick(11, 1, 2, 5, 1, 1, 600);
    IntegrationTestEntity.insertPick(12, 1, 2, 7, 1, 1, 600);

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 1, 2);

    // System properties
    System.setProperty("audio.file.bucket", "my-test-bucket");

    // Instantiate the test subject
    dubFactory = injector.getInstance(DubFactory.class);
    basisFactory = injector.getInstance(BasisFactory.class);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new WorkerModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));
  }


  @After
  public void tearDown() throws Exception {
    dubFactory = null;
    basisFactory = null;
    System.clearProperty("audio.file.bucket");
  }

  @Test
  public void dubMasterContinue() throws Exception {
    InternalResource testAudioResource = new InternalResource(testResourceFilePath);
    // it's necessary to have two separate streams for this mock of two separate file reads
    InputStream audioStreamOne = FileUtils.openInputStream(testAudioResource.getFile());;
    InputStream audioStreamTwo = FileUtils.openInputStream(testAudioResource.getFile());;
    when(amazonProvider.streamS3Object("my-test-bucket",
      "19801735098q47895897895782138975898")).thenReturn(audioStreamOne);
    when(amazonProvider.streamS3Object("my-test-bucket",
      "198017350afghjkjhaskjdfjhk975898")).thenReturn(audioStreamTwo);

    Basis basis = basisFactory.createBasis(link4);
    dubFactory.master(basis).doWork();

    // future test: success of dub master continue test
  }

}

