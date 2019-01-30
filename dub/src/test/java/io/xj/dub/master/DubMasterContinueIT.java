// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import io.xj.core.CoreModule;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.craft.CraftModule;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import io.xj.dub.DubFactory;
import io.xj.dub.DubModule;
import io.xj.mixer.util.InternalResource;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
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
  private static final String testResourceFilePath = "test_audio" + File.separator + "F32LSB_48kHz_Stereo.wav";
  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  AmazonProvider amazonProvider;
  private Injector injector;
  private DubFactory dubFactory;
  private BasisFactory basisFactory;
  // Testing entities for reference
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    createInjector();

    IntegrationTestEntity.reset();

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

    // "Classic, Wild to Cozy" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Classic, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertSequenceMeme(2, 4, "Classic");
    IntegrationTestEntity.insertPatternAndSequencePattern(3, 4, PatternType.Macro, PatternState.Published, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertSequencePatternMeme(3, 4, 3, "Wild");
    IntegrationTestEntity.insertPatternChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPatternAndSequencePattern(4, 4, PatternType.Macro, PatternState.Published, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertSequencePatternMeme(4, 4, 4, "Cozy");
    IntegrationTestEntity.insertPatternChord(4, 4, 0, "Bb minor");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "Gb minor", 140);
    IntegrationTestEntity.insertSequenceMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPatternAndSequencePattern(15, 5, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "Gb minor", 135.0);
    IntegrationTestEntity.insertSequencePatternMeme(6, 5, 15, "Cloudy");
    IntegrationTestEntity.insertPatternChord(12, 15, 0, "Gb minor");
    IntegrationTestEntity.insertPatternChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPatternAndSequencePattern(16, 5, PatternType.Main, PatternState.Published, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertSequencePatternMeme(7, 5, 16, "Rosy");
    IntegrationTestEntity.insertPatternChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPatternChord(18, 16, 8, "G major");

    /*
    Note that in any real use case, after
    [#163158036] memes bound to sequence-patter
    because sequence-pattern binding is not considered for rhythm sequences,
    rhythm sequence patterns do not have memes.
     */

    // A basic beat
    IntegrationTestEntity.insertSequence(35, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertSequenceMeme(343, 35, "Basic");

    // basic beat first pattern
    IntegrationTestEntity.insertPatternAndSequencePattern(316, 35, PatternType.Loop, PatternState.Published, 0, 16, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertSequencePatternMeme(347, 35, 316, "Heavy");

    // setup voice second pattern
    IntegrationTestEntity.insertPatternAndSequencePattern(315, 35, PatternType.Loop, PatternState.Published, 1, 4, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertSequencePatternMeme(346, 35, 315, "Heavy");
    IntegrationTestEntity.insertVoice(1, 35, InstrumentType.Percussive, "drums");

    // Voice "Drums" has events "BOOM" and "SMACK" 2x each
    IntegrationTestEntity.insertPatternEvent(1, 315, 1, 0, 1, "BOOM", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(2, 315, 1, 1, 1, "SMACK", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(3, 315, 1, 2.5, 1, "BOOM", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(4, 315, 1, 3, 1, "SMACK", "G5", 0.1, 0.9);

    // detail sequence
    IntegrationTestEntity.insertSequence(7, 3, 2, SequenceType.Detail, SequenceState.Published, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-segment-97898asdf7892", new JSONObject());
    IntegrationTestEntity.insertSegment(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-segment-97898asdf7892", new JSONObject());

    // Chain "Test Print #1" has this segment that was just dubbed
    IntegrationTestEntity.insertSegment(3, 1, 2, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-segment-97898asdf7892", new JSONObject());
    IntegrationTestEntity.insertChoice(25, 3, 4, SequenceType.Macro, 1, 3);
    IntegrationTestEntity.insertChoice(26, 3, 5, SequenceType.Main, 0, 5);
    IntegrationTestEntity.insertChoice(27, 3, 35, SequenceType.Rhythm, 0, 5);

    // Chain "Test Print #1" is dubbing - Structure is complete
    segment4 = IntegrationTestEntity.insertSegment(4, 1, 3, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "D major", 16, 0.45, 120, "chain-1-segment-97898asdf7892", new JSONObject());
    IntegrationTestEntity.insertSegmentMeme(101, 4, "Cozy");
    IntegrationTestEntity.insertSegmentMeme(102, 4, "Classic");
    IntegrationTestEntity.insertSegmentMeme(103, 4, "Outlook");
    IntegrationTestEntity.insertSegmentMeme(104, 4, "Rosy");
    IntegrationTestEntity.insertChoice(101, 4, 4, SequenceType.Macro, 1, 3);
    IntegrationTestEntity.insertChoice(102, 4, 5, SequenceType.Main, 1, -5);
    IntegrationTestEntity.insertSegmentChord(101, 4, 0, "A minor");
    IntegrationTestEntity.insertSegmentChord(102, 4, 8, "D major");

    // choice of rhythm-type sequence
    IntegrationTestEntity.insertChoice(103, 4, 35, SequenceType.Rhythm, 1, 2);

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

    // FUTURE: determine new test vector for [#154014731] persist Audio pick in memory

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 1, 2);

    // System properties
    System.setProperty("audio.file.bucket", "my-test-bucket");

    // Instantiate the test subject
    dubFactory = injector.getInstance(DubFactory.class);
    basisFactory = injector.getInstance(BasisFactory.class);
  }

  private void createInjector() {
    injector = Guice.createInjector(Modules.override(new CoreModule(), new CraftModule(), new DubModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(AmazonProvider.class).toInstance(amazonProvider);
        }
      }));
  }


  @After
  public void tearDown() {
    System.clearProperty("audio.file.bucket");
  }

  @Test
  public void dubMasterContinue() throws Exception {
    InternalResource testAudioResource = new InternalResource(testResourceFilePath);
    // it's necessary to have two separate streams for this mock of two separate file reads
    InputStream audioStreamOne = FileUtils.openInputStream(testAudioResource.getFile());
    InputStream audioStreamTwo = FileUtils.openInputStream(testAudioResource.getFile());
    when(amazonProvider.streamS3Object("my-test-bucket",
      "19801735098q47895897895782138975898")).thenReturn(audioStreamOne);
    when(amazonProvider.streamS3Object("my-test-bucket",
      "198017350afghjkjhaskjdfjhk975898")).thenReturn(audioStreamTwo);

    Basis basis = basisFactory.createBasis(segment4);
    dubFactory.master(basis).doWork();

    // future test: success of dub master continue test
  }

}

