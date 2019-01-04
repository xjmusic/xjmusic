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
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CraftRhythmVoiceInitialIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Segment segment6;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "pigs"
    IntegrationTestEntity.insertAccount(1, "pigs");

    // Greg has "user" and "admin" roles, belongs to account "pigs", has "google" auth
    IntegrationTestEntity.insertUser(2, "greg", "greg@email.com", "http://pictures.com/greg.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Tonya has a "user" role and belongs to account "pigs"
    IntegrationTestEntity.insertUser(3, "tonya", "tonya@email.com", "http://pictures.com/tonya.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Special, Wild to Cozy" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Special, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertSequenceMeme(2, 4, "Special");
    IntegrationTestEntity.insertPatternSequencePattern(3, 4, PatternType.Macro, PatternState.Published, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPatternMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPatternChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPatternSequencePattern(4, 4, PatternType.Macro, PatternState.Published, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPatternMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPatternChord(4, 4, 0, "Bb minor");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "F# minor", 140);
    IntegrationTestEntity.insertSequenceMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPatternSequencePattern(15, 5, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "F# minor", 135.0);
    IntegrationTestEntity.insertPatternMeme(6, 15, "Pessimism");
    IntegrationTestEntity.insertPatternChord(12, 15, 0, "F# minor");
    IntegrationTestEntity.insertPatternChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPatternSequencePattern(16, 5, PatternType.Main, PatternState.Published, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPatternMeme(7, 16, "Optimism");
    IntegrationTestEntity.insertPatternChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPatternChord(18, 16, 8, "G major");

    // A basic beat, first pattern has voice and events
    IntegrationTestEntity.insertSequence(35, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertSequenceMeme(343, 35, "Basic");
    IntegrationTestEntity.insertVoice(1, 35, InstrumentType.Percussive, "drums");

    /*
    There are two types of patterns: Intro and Loop [#153976073] Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Sequence) in order to create a composition that is dynamic when chosen to fill a Segment.
    [#150279647] Artist wants to create multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.
    ---
    For this test, there's an Intro Pattern with all BLEEPS,
    multiple Loop Patterns with KICK and SNARE (2x each),
    and an Outro Pattern with all TOOTS
     */
    // Intro Pattern
    IntegrationTestEntity.insertPattern(315, 35, PatternType.Intro, PatternState.Published, 4, "Intro", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(345, 315, "Heavy");
    IntegrationTestEntity.insertPatternEvent(1, 315, 1, 0, 1, "BLEEP", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(2, 315, 1, 1, 1, "BLEIP", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(3, 315, 1, 2.5, 1, "BLEAP", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(4, 315, 1, 3, 1, "BLEEEP", "G5", 0.1, 0.9);
    // Loop Pattern A
    IntegrationTestEntity.insertPattern(316, 35, PatternType.Loop, PatternState.Published, 4, "Loop A", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(346, 316, "Heavy");
    IntegrationTestEntity.insertPatternEvent(5, 316, 1, 0, 1, "CLOCK", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(6, 316, 1, 1, 1, "SNORT", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(7, 316, 1, 2.5, 1, "KICK", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(8, 316, 1, 3, 1, "SNARL", "G5", 0.1, 0.9);
    // Loop Pattern A
    IntegrationTestEntity.insertPattern(317, 35, PatternType.Loop, PatternState.Published, 4, "Loop B", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(347, 317, "Heavy");
    IntegrationTestEntity.insertPatternEvent(11, 317, 1, 0, 1, "KIICK", "B5", 0.1, 0.9);
    IntegrationTestEntity.insertPatternEvent(12, 317, 1, 1, 1, "SNARR", "D2", 0.5, 1.0);
    IntegrationTestEntity.insertPatternEvent(14, 317, 1, 2.5, 1, "KEICK", "E4", 0.1, 0.7);
    IntegrationTestEntity.insertPatternEvent(15, 317, 1, 3, 1, "SNAER", "C3", 0.5, 0.5);
    // Outro Pattern
    IntegrationTestEntity.insertPattern(318, 35, PatternType.Outro, PatternState.Published, 4, "Outro", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(348, 318, "Heavy");
    IntegrationTestEntity.insertPatternEvent(16, 318, 1, 0, 1, "TOOT", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPatternEvent(17, 318, 1, 1, 1, "TOOOT", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPatternEvent(18, 318, 1, 2.5, 1, "TOOTE", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPatternEvent(19, 318, 1, 3, 1, "TOUT", "G5", 0.1, 0.9);

    // Detail Sequence
    IntegrationTestEntity.insertSequence(7, 3, 2, SequenceType.Detail, SequenceState.Published, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    IntegrationTestEntity.insertChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    segment6 = IntegrationTestEntity.insertSegment(6, 2, 0, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:07.384616"), "C minor", 32, 0.55, 130, "chain-1-segment-97898asdf7892.wav", new JSONObject());
    IntegrationTestEntity.insertSegmentMeme(101, 6, "Special");
    IntegrationTestEntity.insertSegmentMeme(102, 6, "Wild");
    IntegrationTestEntity.insertSegmentMeme(103, 6, "Pessimism");
    IntegrationTestEntity.insertSegmentMeme(104, 6, "Outlook");
    IntegrationTestEntity.insertChoice(101, 6, 4, SequenceType.Macro, 0, 0);
    IntegrationTestEntity.insertChoice(102, 6, 5, SequenceType.Main, 0, -6);
    IntegrationTestEntity.insertSegmentChord(101, 6, 0, "C minor");
    IntegrationTestEntity.insertSegmentChord(102, 6, 8, "Db minor");

    // choice of rhythm-type sequence
    IntegrationTestEntity.insertChoice(103, 6, 35, SequenceType.Rhythm, 0, 0);

    // Instrument "808"
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, 1, "heavy");

    // Audio "Kick"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "https://static.xj.io/19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 1, 0, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "https://static.xj.io/975898198017350afghjkjhaskjdfjhk.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 2, 0, 1, "SNARE", "Ab", 0.1, 0.8);

    // Audio "Bleep"
    IntegrationTestEntity.insertAudio(3, 1, "Published", "Bleep", "https://static.xj.io/17350afghjkjhaskjdfjhk9758981980.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(3, 3, 0, 1, "BLEEP", "Ab", 0.8, 0.8);

    // Audio "Toot"
    IntegrationTestEntity.insertAudio(4, 1, "Published", "Toot", "https://static.xj.io/askjdfjhk975898198017350afghjkjh.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(4, 4, 0, 1, "TOOT", "Ab", 0.1, 0.8);

    // bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2, 2);

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
  public void craftRhythmVoiceInitial() throws Exception {
    Basis basis = basisFactory.createBasis(segment6);

    craftFactory.rhythm(basis).doWork();

    assertFalse(injector.getInstance(ArrangementDAO.class).readAll(Access.internal(), ImmutableList.of(BigInteger.valueOf(103))).isEmpty());

    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedBleep = 0;
    int pickedToot = 0;
    for (Pick pick : basis.picks()) {
      if (pick.getAudioId().equals(BigInteger.valueOf(1)))
        pickedKick++;
      if (pick.getAudioId().equals(BigInteger.valueOf(2)))
        pickedSnare++;
      if (pick.getAudioId().equals(BigInteger.valueOf(3)))
        pickedBleep++;
      if (pick.getAudioId().equals(BigInteger.valueOf(4)))
        pickedToot++;
    }
    assertEquals(12, pickedKick);
    assertEquals(12, pickedSnare);
    assertEquals(4, pickedBleep);
    assertEquals(4, pickedToot);
  }

  @Test
  public void craftRhythmVoiceInitial_okWhenNoRhythmChoice() throws Exception {
    Basis basis = basisFactory.createBasis(segment6);
    injector.getInstance(ChoiceDAO.class).destroy(Access.internal(), BigInteger.valueOf(103));

    craftFactory.rhythm(basis).doWork();
  }

}
