// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CraftRhythmVoiceContinueIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
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
    IntegrationTestEntity.insertPatternSequencePattern(3, 4, PatternType.Macro, PatternState.Published, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPatternMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPatternChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPatternSequencePattern(4, 4, PatternType.Macro, PatternState.Published, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPatternMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPatternChord(4, 4, 0, "Bb minor");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "Gb minor", 140);
    IntegrationTestEntity.insertSequenceMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPatternSequencePattern(15, 5, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "Gb minor", 135.0);
    IntegrationTestEntity.insertPatternMeme(6, 15, "Cloudy");
    IntegrationTestEntity.insertPatternChord(12, 15, 0, "Gb minor");
    IntegrationTestEntity.insertPatternChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPatternSequencePattern(16, 5, PatternType.Main, PatternState.Published, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPatternMeme(7, 16, "Rosy");
    IntegrationTestEntity.insertPatternChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPatternChord(18, 16, 8, "G major");

    // A basic beat
    IntegrationTestEntity.insertSequence(35, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertSequenceMeme(343, 35, "Basic");
    IntegrationTestEntity.insertVoice(1, 35, InstrumentType.Percussive, "drums");

    /*
    Voice "Drums" are onomatopoeic to "KICK" and "SNARE" 2x each
    There are two types of patterns: Intro and Loop [#153976073] Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Sequence) in order to create a composition that is dynamic when chosen to fill a Segment.
     */
    IntegrationTestEntity.insertPattern(315, 35, PatternType.Intro, PatternState.Published, 4, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(346, 315, "Heavy");
    IntegrationTestEntity.insertPatternEvent(1, 315, 1, 0, 1, "CLOCK", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(2, 315, 1, 1, 1, "SNORT", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(3, 315, 1, 2.5, 1, "KICK", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(4, 315, 1, 3, 1, "SNARL", "G5", 0.1, 0.9);

    /*
    this is an alternate pattern at the same offset
    [#150279647] Artist wants to create multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.
    they are also onomatopoeic to "KICK" and "SNARE" 2x each
     */
    IntegrationTestEntity.insertPattern(317, 35, PatternType.Loop, PatternState.Published, 4, "Drop Alt", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(349, 317, "Heavy");
    IntegrationTestEntity.insertPatternEvent(11, 317, 1, 0, 1, "CLACK", "B5", 0.1, 0.9);
    IntegrationTestEntity.insertPatternEvent(12, 317, 1, 1, 1, "SNARL", "D2", 0.5, 1.0);
    IntegrationTestEntity.insertPatternEvent(14, 317, 1, 2.5, 1, "CLICK", "E4", 0.1, 0.7);
    IntegrationTestEntity.insertPatternEvent(15, 317, 1, 3, 1, "SNAP", "C3", 0.5, 0.5);

    // harmonicDetail sequence
    IntegrationTestEntity.insertSequence(7, 3, 2, SequenceType.Detail, SequenceState.Published, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-segment-97898asdf7892.wav");

    // Chain "Test Print #1" has this segment that was just crafted
    IntegrationTestEntity.insertSegment(3, 1, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(25, 3, 4, SequenceType.Macro, 1, 3);
    IntegrationTestEntity.insertChoice(26, 3, 5, SequenceType.Main, 0, 5);
    IntegrationTestEntity.insertChoice(27, 3, 35, SequenceType.Rhythm, 0, 5);

    // Chain "Test Print #1" is crafting - Structure is complete
    segment4 = IntegrationTestEntity.insertSegment(4, 1, 3, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "D major", 16, 0.45, 120, "chain-1-segment-97898asdf7892.wav");
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
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "https://static.xj.io/19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "https://static.xj.io/198017350afghjkjhaskjdfjhk975898.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 2, 3, 1, "SNARE", "Ab", 0.1, 0.8);

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 1, 2);

    // Instantiate the test subject
    craftFactory = injector.getInstance(CraftFactory.class);
    basisFactory = injector.getInstance(BasisFactory.class);
  }

  @After
  public void tearDown() throws Exception {
    craftFactory = null;
    basisFactory = null;
  }

  @Test
  public void craftRhythmVoiceContinue() throws Exception {
    Basis basis = basisFactory.createBasis(segment4);

    craftFactory.rhythm(basis).doWork();

    assertFalse(injector.getInstance(ArrangementDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(103))).isEmpty());

    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    for (Pick pick : basis.picks()) {
      if (pick.getAudioId().equals(BigInteger.valueOf(1)))
        pickedKick++;
      if (pick.getAudioId().equals(BigInteger.valueOf(2)))
        pickedSnare++;
    }
    assertEquals(8, pickedKick);
    assertEquals(8, pickedSnare);
  }

  @Test
  public void craftRhythmVoiceContinue_okIfNoRhythmChoice() throws Exception {
    Basis basis = basisFactory.createBasis(segment4);
    injector.getInstance(ChoiceDAO.class).destroy(Access.internal(), BigInteger.valueOf(103));

    craftFactory.rhythm(basis).doWork();
  }

}
