// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.foundation;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.model.phase.PhaseType;
import io.xj.craft.CraftFactory;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.LinkChordDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.link_chord.LinkChord;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.testing.Testing;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisFactory;
import io.xj.craft.CraftModule;



import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CraftFoundationNextMacroIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Link link4;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "kingfruits"
    IntegrationTestEntity.insertAccount(1, "kingfruits");

    // Candy has "user" and "admin" roles, belongs to account "kingfruits", has "google" auth
    IntegrationTestEntity.insertUser(2, "candy", "candy@email.com", "http://pictures.com/candy.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Claude has a "user" role and belongs to account "kingfruits"
    IntegrationTestEntity.insertUser(3, "claude", "claude@email.com", "http://pictures.com/claude.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Metal, Wild to Basement" macro-pattern in house library
    IntegrationTestEntity.insertPattern(4, 3, 2, PatternType.Macro, "Wild to Basement", 0.5, "C", 120);
    // " phase offset 0
    IntegrationTestEntity.insertPhase(3, 4, PhaseType.Macro, 0, 0, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    // " phase offset 1
    IntegrationTestEntity.insertPhase(4, 4, PhaseType.Macro, 1, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Basement");
    IntegrationTestEntity.insertPhaseMeme(49, 4, "Wild");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");
    // " phase offset 2
    IntegrationTestEntity.insertPhase(5, 4, PhaseType.Macro, 2, 0, "Finish Basement", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertPhaseMeme(5, 4, "Basement");
    IntegrationTestEntity.insertPhaseChord(5, 5, 0, "Ab minor");

    // "Chunky to Smooth" macro-pattern in house library
    IntegrationTestEntity.insertPattern(3, 3, 2, PatternType.Macro, "Chunky to Smooth", 0.5, "G minor", 120);
    // # phase offset 0
    IntegrationTestEntity.insertPhase(1, 3, PhaseType.Macro, 0, 0, "Start Chunky", 0.4, "G minor", 115);
    IntegrationTestEntity.insertPhaseMeme(1, 1, "Chunky");
    IntegrationTestEntity.insertPhaseChord(1, 1, 0, "G minor");
    // # phase offset 1
    IntegrationTestEntity.insertPhase(2, 3, PhaseType.Macro, 1, 0, "Finish Smooth", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(2, 2, "Smooth");
    IntegrationTestEntity.insertPhaseChord(2, 2, 0, "C");

    // Main pattern
    IntegrationTestEntity.insertPattern(5, 3, 2, PatternType.Main, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertPatternMeme(3, 5, "Outlook");
    // # phase offset 0
    IntegrationTestEntity.insertPhase(15, 5, PhaseType.Main, 0, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Optimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "G major");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "Ab minor");
    // # phase offset 1
    IntegrationTestEntity.insertPhase(16, 5, PhaseType.Main, 1, 16, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "C major");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "Bb minor");

    // Another Main pattern to go to
    IntegrationTestEntity.insertPattern(15, 3, 2, PatternType.Main, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertPatternMeme(43, 15, "Hindsight");
    IntegrationTestEntity.insertPhase(415, 15, PhaseType.Main, 0, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(46, 415, "Regret");
    IntegrationTestEntity.insertPhaseChord(412, 415, 0, "G minor");
    IntegrationTestEntity.insertPhaseChord(414, 415, 8, "Ab minor");
    IntegrationTestEntity.insertPhase(416, 15, PhaseType.Main, 1, 16, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(47, 416, "Pride");
    IntegrationTestEntity.insertPhaseMeme(149, 416, "Shame");
    IntegrationTestEntity.insertPhaseChord(416, 416, 0, "C major");
    IntegrationTestEntity.insertPhaseChord(418, 416, 8, "Bb major");

    // [#154090557] this Chord should be ignored, because it's past the end of the main-phase total
    IntegrationTestEntity.insertPhaseChord(42, 415, 75, "G-9");

    // Chain "Test Print #1" has 5 total links
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, LinkState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");

    // Chain "Test Print #1" has this link that was just crafted
    IntegrationTestEntity.insertLink(3, 1, 2, LinkState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "Ab minor", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav"); // final key is based on phase of main pattern
    IntegrationTestEntity.insertChoice(25, 3, 4, PatternType.Macro, 1, 3); // macro-pattern current phase is transposed to be Db minor
    IntegrationTestEntity.insertChoice(26, 3, 5, PatternType.Main, 1, 1); // main-key of previous link is transposed to match, Db minor

    // Chain "Test Print #1" has a planned link
    link4 = IntegrationTestEntity.insertLink_Planned(4, 1, 3, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

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
  public void craftFoundationNextMacro() throws Exception {
    Basis basis = basisFactory.createBasis(link4);

    craftFactory.foundation(basis).doWork();

    Link resultLink = injector.getInstance(LinkDAO.class).readOneAtChainOffset(Access.internal(), BigInteger.valueOf(1), BigInteger.valueOf(3));

    assertEquals(Timestamp.valueOf("2017-02-14 12:03:15.8425"), resultLink.getEndAt());
    assertEquals(Integer.valueOf(16), resultLink.getTotal());
    assertEquals(Double.valueOf(0.45), resultLink.getDensity());
    assertEquals("F minor", resultLink.getKey());
    assertEquals(Double.valueOf(125), resultLink.getTempo());

    Collection<LinkMeme> resultLinkMemes = injector.getInstance(LinkMemeDAO.class).readAll(Access.internal(), resultLink.getId());
    assertEquals(3, resultLinkMemes.size());
    resultLinkMemes.forEach(linkMemeRecord -> Testing.assertIn(new String[]{"Hindsight", "Chunky", "Regret"}, linkMemeRecord.getName()));

    Collection<LinkChord> resultLinkChords = injector.getInstance(LinkChordDAO.class).readAll(Access.internal(), resultLink.getId());
    assertEquals(2, resultLinkChords.size());
    Iterator<LinkChord> it = resultLinkChords.iterator();

    LinkChord chordOne = it.next();
    assertEquals(Integer.valueOf(0), chordOne.getPosition());
    assertEquals("F minor", chordOne.getName());

    LinkChord chordTwo = it.next();
    assertEquals(Integer.valueOf(8), chordTwo.getPosition());
    assertEquals("Gb minor", chordTwo.getName());

    // choice of macro-type pattern
    Choice resultMacroChoice = injector.getInstance(ChoiceDAO.class).readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), BigInteger.valueOf(4), PatternType.Macro);
    assertNotNull(resultMacroChoice);
    assertEquals(BigInteger.valueOf(3), resultMacroChoice.getPatternId());
    assertEquals(Integer.valueOf(4), resultMacroChoice.getTranspose());
    assertEquals(BigInteger.valueOf(0), resultMacroChoice.getPhaseOffset());

    // choice of main-type pattern
    Choice resultMainChoice = injector.getInstance(ChoiceDAO.class).readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), BigInteger.valueOf(4), PatternType.Main);
    assertNotNull(resultMainChoice);
    assertEquals(BigInteger.valueOf(15), resultMainChoice.getPatternId());
    assertEquals(Integer.valueOf(-2), resultMainChoice.getTranspose());
    assertEquals(BigInteger.valueOf(0), resultMainChoice.getPhaseOffset());

  }

}
