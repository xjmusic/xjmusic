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

public class CraftFoundationInitialIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Link link6;

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
    IntegrationTestEntity.insertPhase(4, 4, PhaseType.Macro, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");

    // Main pattern
    IntegrationTestEntity.insertPattern(5, 3, 2, PatternType.Main, "Main Jam", 0.2, "F# minor", 140);
    IntegrationTestEntity.insertPatternMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPhase(15, 5, PhaseType.Main, 0, 16, "Intro", 0.5, "F# minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "F# minor");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPhase(16, 5, PhaseType.Main, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Optimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "G major");

    // [#154090557] this Chord should be ignored, because it's past the end of the main-phase total
    IntegrationTestEntity.insertPhaseChord(42, 15, 75, "G-9");

    // Extra patterns
    IntegrationTestEntity.insertPattern(6, 3, 2, PatternType.Rhythm, "Beat Jam", 0.6, "D#", 150);
    IntegrationTestEntity.insertPattern(7, 3, 2, PatternType.Detail, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Print #2" has 1 initial planned link
    IntegrationTestEntity.insertChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    link6 = IntegrationTestEntity.insertLink_Planned(6, 2, 0, Timestamp.valueOf("2017-02-14 12:01:00.000001"));
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
  public void craftFoundationInitial() throws Exception {
    Basis basis = basisFactory.createBasis(link6);

    craftFactory.foundation(basis).doWork();

    Link resultLink = injector.getInstance(LinkDAO.class).readOneAtChainOffset(Access.internal(), BigInteger.valueOf(2), BigInteger.valueOf(0));
    assertEquals(Timestamp.valueOf("2017-02-14 12:01:07.384616"), resultLink.getEndAt());
    assertEquals(Integer.valueOf(16), resultLink.getTotal());
    assertEquals(0.55, resultLink.getDensity(), 0.01);
    assertEquals("C minor", resultLink.getKey());
    assertEquals(130.0, resultLink.getTempo(), 0.01);

    Collection<LinkMeme> resultLinkMemes = injector.getInstance(LinkMemeDAO.class).readAll(Access.internal(), resultLink.getId());

    assertEquals(4, resultLinkMemes.size());
    resultLinkMemes.forEach(linkMemeRecord -> Testing.assertIn(new String[]{"Tropical", "Wild", "Pessimism", "Outlook"}, linkMemeRecord.getName()));

    Collection<LinkChord> resultLinkChords = injector.getInstance(LinkChordDAO.class).readAll(Access.internal(), resultLink.getId());
    assertEquals(2, resultLinkChords.size());
    Iterator<LinkChord> it = resultLinkChords.iterator();

    LinkChord chordOne = it.next();
    assertEquals(Integer.valueOf(0), chordOne.getPosition());
    assertEquals("C minor", chordOne.getName());

    LinkChord chordTwo = it.next();
    assertEquals(Integer.valueOf(8), chordTwo.getPosition());
    assertEquals("Db minor", chordTwo.getName());


    // choice of macro-type pattern
    Choice resultMacroChoice = injector.getInstance(ChoiceDAO.class).readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), resultLink.getId(), PatternType.Macro);
    assertNotNull(resultMacroChoice);
    assertEquals(BigInteger.valueOf(4), resultMacroChoice.getPatternId());
    assertEquals(Integer.valueOf(0), resultMacroChoice.getTranspose());
    assertEquals(BigInteger.valueOf(0), resultMacroChoice.getPhaseOffset());

    // choice of main-type pattern
    Choice resultMainChoice = injector.getInstance(ChoiceDAO.class).readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), resultLink.getId(), PatternType.Main);
    assertNotNull(resultMainChoice);
    assertEquals(BigInteger.valueOf(5), resultMainChoice.getPatternId());
    assertEquals(Integer.valueOf(-6), resultMainChoice.getTranspose());
    assertEquals(BigInteger.valueOf(0), resultMainChoice.getPhaseOffset());

  }
}
