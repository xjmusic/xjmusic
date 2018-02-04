// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.PhaseState;
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CraftRhythmVoiceNextMacroIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Link link4;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "crows"
    IntegrationTestEntity.insertAccount(1, "crows");

    // John has "user" and "admin" roles, belongs to account "crows", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "crows"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Tropical, Wild to Cozy" macro-pattern in house library
    IntegrationTestEntity.insertPattern(4, 3, 2, PatternType.Macro, PatternState.Published, "Tropical, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertPatternMeme(2, 4, "Tropical");
    // " phase offset 0
    IntegrationTestEntity.insertPhase(3, 4, PhaseType.Macro, PhaseState.Published, 0, 0, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    // " phase offset 1
    IntegrationTestEntity.insertPhase(4, 4, PhaseType.Macro, PhaseState.Published, 1, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseMeme(49, 4, "Wild");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");
    // " phase offset 2
    IntegrationTestEntity.insertPhase(5, 4, PhaseType.Macro, PhaseState.Published, 2, 0, "Finish Cozy", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertPhaseMeme(5, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(5, 5, 0, "Ab minor");

    // "Tangy, Chunky to Smooth" macro-pattern in house library
    IntegrationTestEntity.insertPattern(3, 3, 2, PatternType.Macro, PatternState.Published, "Tangy, Chunky to Smooth", 0.5, "G minor", 120);
    IntegrationTestEntity.insertPatternMeme(1, 3, "Tangy");
    // # phase offset 0
    IntegrationTestEntity.insertPhase(1, 3, PhaseType.Macro, PhaseState.Published, 0, 0, "Start Chunky", 0.4, "G minor", 115);
    IntegrationTestEntity.insertPhaseMeme(1, 1, "Chunky");
    IntegrationTestEntity.insertPhaseChord(1, 1, 0, "G minor");
    // # phase offset 1
    IntegrationTestEntity.insertPhase(2, 3, PhaseType.Macro, PhaseState.Published, 1, 0, "Finish Smooth", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(2, 2, "Smooth");
    IntegrationTestEntity.insertPhaseChord(2, 2, 0, "C");

    // Main pattern
    IntegrationTestEntity.insertPattern(5, 3, 2, PatternType.Main, PatternState.Published, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertPatternMeme(3, 5, "Outlook");
    // # phase offset 0
    IntegrationTestEntity.insertPhase(15, 5, PhaseType.Main, PhaseState.Published, 0, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Optimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "G major");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "Ab minor");
    // # phase offset 1
    IntegrationTestEntity.insertPhase(16, 5, PhaseType.Main, PhaseState.Published, 1, 16, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "C major");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "Bb minor");

    // Another Main pattern to go to
    IntegrationTestEntity.insertPattern(15, 3, 2, PatternType.Main, PatternState.Published, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertPatternMeme(43, 15, "Hindsight");
    IntegrationTestEntity.insertPhase(415, 15, PhaseType.Main, PhaseState.Published, 0, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(46, 415, "Regret");
    IntegrationTestEntity.insertPhaseChord(412, 415, 0, "G minor");
    IntegrationTestEntity.insertPhaseChord(414, 415, 8, "Ab minor");
    IntegrationTestEntity.insertPhase(416, 15, PhaseType.Main, PhaseState.Published, 1, 16, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(47, 416, "Pride");
    IntegrationTestEntity.insertPhaseMeme(149, 416, "Shame");
    IntegrationTestEntity.insertPhaseChord(416, 416, 0, "C major");
    IntegrationTestEntity.insertPhaseChord(418, 416, 8, "Bb major");

    // A basic beat
    IntegrationTestEntity.insertPattern(35, 3, 2, PatternType.Rhythm, PatternState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertPatternMeme(343, 35, "Basic");
    IntegrationTestEntity.insertVoice(1, 35, InstrumentType.Percussive, "drums");

    /*
    Voice "Drums" are onomatopoeic to "KICK" and "SNARE" 2x each
    There are two types of phases: Intro and Loop [#153976073] Artist wants Phase to have type *Macro* or *Main* (for Macro- or Main-type patterns), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Pattern) in order to create a composition that is dynamic when chosen to fill a Link.
     */
    IntegrationTestEntity.insertPhase(315, 35, PhaseType.Intro, PhaseState.Published, 0, 4, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(346, 315, "Heavy");
    IntegrationTestEntity.insertPhaseEvent(1, 315, 1, 0, 1, "CLOCK", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPhaseEvent(2, 315, 1, 1, 1, "SNORT", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPhaseEvent(3, 315, 1, 2.5, 1, "KICK", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPhaseEvent(4, 315, 1, 3, 1, "SNARL", "G5", 0.1, 0.9);

    /*
    this is an alternate phase at the same offset
    [#150279647] Artist wants to create multiple Phases with the same offset in the same Pattern, in order that XJ randomly select one of the phases at that offset.
    they are also onomatopoeic to "KICK" and "SNARE" 2x each
     */
    IntegrationTestEntity.insertPhase(317, 35, PhaseType.Loop, PhaseState.Published, 0, 4, "Drop Alt", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(349, 317, "Heavy");
    IntegrationTestEntity.insertPhaseEvent(11, 317, 1, 0, 1, "CLACK", "B5", 0.1, 0.9);
    IntegrationTestEntity.insertPhaseEvent(12, 317, 1, 1, 1, "SNARL", "D2", 0.5, 1.0);
    IntegrationTestEntity.insertPhaseEvent(14, 317, 1, 2.5, 1, "CLICK", "E4", 0.1, 0.7);
    IntegrationTestEntity.insertPhaseEvent(15, 317, 1, 3, 1, "SNAP", "C3", 0.5, 0.5);

    // basic beat second phase
    IntegrationTestEntity.insertPhase(316, 35, PhaseType.Loop, PhaseState.Published, 1, 4, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(347, 316, "Heavy");

    // Chain "Test Print #1" has 5 total links
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, LinkState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");

    // Chain "Test Print #1" has this link that was just crafted
    IntegrationTestEntity.insertLink(3, 1, 2, LinkState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "Ab minor", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav"); // final key is based on phase of main pattern
    IntegrationTestEntity.insertChoice(25, 3, 4, PatternType.Macro, 1, 3); // macro-pattern current phase is transposed to be Db minor
    IntegrationTestEntity.insertChoice(26, 3, 5, PatternType.Main, 1, 1); // main-key of previous link is transposed to match, Db minor
    IntegrationTestEntity.insertChoice(27, 3, 35, PatternType.Rhythm, 0, -4);

    // Chain "Test Print #1" has a link in crafting state - Structure is complete
    link4 = IntegrationTestEntity.insertLink(4, 1, 3, LinkState.Crafting, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "F minor", 16, 0.45, 125, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLinkMeme(101, 4, "Hindsight");
    IntegrationTestEntity.insertLinkMeme(102, 4, "Chunky");
    IntegrationTestEntity.insertLinkMeme(103, 4, "Regret");
    IntegrationTestEntity.insertLinkMeme(104, 4, "Tangy");
    IntegrationTestEntity.insertChoice(101, 4, 3, PatternType.Macro, 0, 4);
    IntegrationTestEntity.insertChoice(102, 4, 15, PatternType.Main, 0, -2);
    IntegrationTestEntity.insertLinkChord(101, 4, 0, "F minor");
    IntegrationTestEntity.insertLinkChord(102, 4, 8, "Gb minor");

    // choice of rhythm-type pattern
    IntegrationTestEntity.insertChoice(103, 4, 35, PatternType.Rhythm, 0, 5);

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
  public void craftRhythmVoiceNextMacro() throws Exception {
    Basis basis = basisFactory.createBasis(link4);

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
  public void craftRhythmVoiceNextMacro_okIfNoRhythmChoice() throws Exception {
    Basis basis = basisFactory.createBasis(link4);
    injector.getInstance(ChoiceDAO.class).destroy(Access.internal(), BigInteger.valueOf(103));

    craftFactory.rhythm(basis).doWork();
  }

}
