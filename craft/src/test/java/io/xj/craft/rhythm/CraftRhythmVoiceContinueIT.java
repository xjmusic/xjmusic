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

public class CraftRhythmVoiceContinueIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Link link4;

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

    // "Classic, Wild to Cozy" macro-pattern in house library
    IntegrationTestEntity.insertPattern(4, 3, 2, PatternType.Macro, PatternState.Published, "Classic, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertPatternMeme(2, 4, "Classic");
    IntegrationTestEntity.insertPhase(3, 4, PhaseType.Macro, PhaseState.Published, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPhase(4, 4, PhaseType.Macro, PhaseState.Published, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");

    // Main pattern
    IntegrationTestEntity.insertPattern(5, 3, 2, PatternType.Main, PatternState.Published, "Main Jam", 0.2, "Gb minor", 140);
    IntegrationTestEntity.insertPatternMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPhase(15, 5, PhaseType.Main, PhaseState.Published, 0, 16, "Intro", 0.5, "Gb minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Cloudy");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "Gb minor");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPhase(16, 5, PhaseType.Main, PhaseState.Published, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Rosy");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "G major");

    // A basic beat
    IntegrationTestEntity.insertPattern(35, 3, 2, PatternType.Rhythm, PatternState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertPatternMeme(343, 35, "Basic");
    IntegrationTestEntity.insertVoice(1, 35, InstrumentType.Percussive, "drums");

    // basic beat first phase
    IntegrationTestEntity.insertPhase(316, 35, PhaseType.Loop, PhaseState.Published, 0, 16, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(347, 316, "Heavy");

    /*
    Voice "Drums" are onomatopoeic to "KICK" and "SNARE" 2x each
    There are two types of phases: Intro and Loop [#153976073] Artist wants Phase to have type *Macro* or *Main* (for Macro- or Main-type patterns), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Pattern) in order to create a composition that is dynamic when chosen to fill a Link.
     */
    IntegrationTestEntity.insertPhase(315, 35, PhaseType.Intro, PhaseState.Published, 1, 4, "Drop", 0.5, "C", 125.0);
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
    IntegrationTestEntity.insertPhase(317, 35, PhaseType.Loop, PhaseState.Published, 1, 4, "Drop Alt", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(349, 317, "Heavy");
    IntegrationTestEntity.insertPhaseEvent(11, 317, 1, 0, 1, "CLACK", "B5", 0.1, 0.9);
    IntegrationTestEntity.insertPhaseEvent(12, 317, 1, 1, 1, "SNARL", "D2", 0.5, 1.0);
    IntegrationTestEntity.insertPhaseEvent(14, 317, 1, 2.5, 1, "CLICK", "E4", 0.1, 0.7);
    IntegrationTestEntity.insertPhaseEvent(15, 317, 1, 3, 1, "SNAP", "C3", 0.5, 0.5);

    // These events should not be used, they are here to resolve issues from [#153976336] PhaseEvent belongs to Phase
    IntegrationTestEntity.insertPhaseEvent(5, 316, 1, 0, 1, "JAM", "C2", 0.8, 1.0);
    IntegrationTestEntity.insertPhaseEvent(6, 316, 1, 1, 1, "HAM", "G5", 0.1, 0.8);
    IntegrationTestEntity.insertPhaseEvent(7, 316, 1, 2.5, 1, "MARMALADE", "C2", 0.8, 0.6);
    IntegrationTestEntity.insertPhaseEvent(8, 316, 1, 3, 1, "TOAST", "G5", 0.1, 0.9);

    // harmonicDetail pattern
    IntegrationTestEntity.insertPattern(7, 3, 2, PatternType.Detail, PatternState.Published, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Test Print #1" has 5 total links
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, LinkState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");

    // Chain "Test Print #1" has this link that was just crafted
    IntegrationTestEntity.insertLink(3, 1, 2, LinkState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(25, 3, 4, PatternType.Macro, 1, 3);
    IntegrationTestEntity.insertChoice(26, 3, 5, PatternType.Main, 0, 5);
    IntegrationTestEntity.insertChoice(27, 3, 35, PatternType.Rhythm, 0, 5);

    // Chain "Test Print #1" is crafting - Structure is complete
    link4 = IntegrationTestEntity.insertLink(4, 1, 3, LinkState.Crafting, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "D major", 16, 0.45, 120, "chain-1-link-97898asdf7892.wav");
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
  public void craftRhythmVoiceContinue_okIfNoRhythmChoice() throws Exception {
    Basis basis = basisFactory.createBasis(link4);
    injector.getInstance(ChoiceDAO.class).destroy(Access.internal(), BigInteger.valueOf(103));

    craftFactory.rhythm(basis).doWork();
  }

}
