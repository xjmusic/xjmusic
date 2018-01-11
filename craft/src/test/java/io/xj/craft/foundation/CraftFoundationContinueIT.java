// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.foundation;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
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
import io.xj.core.model.phase.PhaseType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.testing.Testing;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisFactory;
import io.xj.craft.CraftFactory;
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

public class CraftFoundationContinueIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Link link4;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "bananas"
    IntegrationTestEntity.insertAccount(1, "bananas");

    // John has "user" and "admin" roles, belongs to account "bananas", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Tropical, Wild to Cozy" macro-pattern in house library
    IntegrationTestEntity.insertPattern(4, 3, 2, PatternType.Macro, "Tropical, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertPatternMeme(2, 4, "Tropical");
    IntegrationTestEntity.insertPhase(3, 4, PhaseType.Macro, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPhase(4, 4, PhaseType.Macro, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");

    // Main pattern
    IntegrationTestEntity.insertPattern(5, 3, 2, PatternType.Main, "Main Jam", 0.2, "Gb minor", 140);
    IntegrationTestEntity.insertPatternMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPhase(15, 5, PhaseType.Main, 0, 16, "Intro", 0.5, "Gb minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "Gb minor");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPhase(16, 5, PhaseType.Main, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Optimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "G major");

    // [#154090557] this Chord should be ignored, because it's past the end of the main-phase total
    IntegrationTestEntity.insertPhaseChord(42, 16, 75, "G-9");

    // Extra patterns
    IntegrationTestEntity.insertPattern(6, 3, 2, PatternType.Rhythm, "Beat Jam", 0.6, "D#", 150);
    IntegrationTestEntity.insertPattern(7, 3, 2, PatternType.Detail, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Test Print #1" has 5 total links
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, LinkState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");

    // Chain "Test Print #1" has this link that was just crafted
    IntegrationTestEntity.insertLink(3, 1, 2, LinkState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(25, 3, 4, PatternType.Macro, 1, 3);
    IntegrationTestEntity.insertChoice(26, 3, 5, PatternType.Main, 0, 5);

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
  public void craftFoundationContinue() throws Exception {
    Basis basis = basisFactory.createBasis(link4);

    craftFactory.foundation(basis).doWork();

    Link resultLink = injector.getInstance(LinkDAO.class).readOneAtChainOffset(Access.internal(), BigInteger.valueOf(1), BigInteger.valueOf(3));
    assertEquals(Timestamp.valueOf("2017-02-14 12:03:15.8425"), resultLink.getEndAt());
    assertEquals(Integer.valueOf(16), resultLink.getTotal());
    assertEquals(Double.valueOf(0.45), resultLink.getDensity());
    assertEquals("D major", resultLink.getKey());
    assertEquals(Double.valueOf(125), resultLink.getTempo());

    Collection<LinkMeme> resultLinkMemes = injector.getInstance(LinkMemeDAO.class).readAll(Access.internal(), resultLink.getId());

    assertEquals(4, resultLinkMemes.size());
    resultLinkMemes.forEach(linkMemeRecord -> Testing.assertIn(new String[]{"Cozy", "Tropical", "Outlook", "Optimism"}, linkMemeRecord.getName()));

    Collection<LinkChord> resultLinkChords = injector.getInstance(LinkChordDAO.class).readAll(Access.internal(), resultLink.getId());
    assertEquals(2, resultLinkChords.size());
    Iterator<LinkChord> it = resultLinkChords.iterator();

    LinkChord chordOne = it.next();
    assertEquals(Integer.valueOf(0), chordOne.getPosition());
    assertEquals("A minor", chordOne.getName());

    LinkChord chordTwo = it.next();
    assertEquals(Integer.valueOf(8), chordTwo.getPosition());
    assertEquals("D major", chordTwo.getName());

    // choice of macro-type pattern
    Choice resultMacroChoice = injector.getInstance(ChoiceDAO.class).readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), BigInteger.valueOf(4), PatternType.Macro);
    assertNotNull(resultMacroChoice);
    assertEquals(BigInteger.valueOf(4), resultMacroChoice.getPatternId());
    assertEquals(Integer.valueOf(3), resultMacroChoice.getTranspose());
    assertEquals(BigInteger.valueOf(1), resultMacroChoice.getPhaseOffset());

    // choice of main-type pattern
    Choice resultMainChoice = injector.getInstance(ChoiceDAO.class).readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), BigInteger.valueOf(4), PatternType.Main);
    assertNotNull(resultMainChoice);
    assertEquals(BigInteger.valueOf(5), resultMainChoice.getPatternId());
    assertEquals(Integer.valueOf(-5), resultMainChoice.getTranspose());
    assertEquals(BigInteger.valueOf(1), resultMainChoice.getPhaseOffset());

  }

}
