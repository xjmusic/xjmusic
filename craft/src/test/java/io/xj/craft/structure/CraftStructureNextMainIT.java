// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.structure;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.model.phase.PhaseType;
import io.xj.craft.CraftFactory;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.user_role.UserRoleType;
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

import static org.junit.Assert.assertNotNull;

public class CraftStructureNextMainIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Link link4;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "strawberries"
    IntegrationTestEntity.insertAccount(1, "strawberries");

    // John has "user" and "admin" roles, belongs to account "strawberries", has "google" auth
    IntegrationTestEntity.insertUser(2, "john", "john@email.com", "http://pictures.com/john.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Jenny has a "user" role and belongs to account "strawberries"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Tropical, Sky to Ocean" macro-pattern in house library
    IntegrationTestEntity.insertPattern(4, 3, 2, PatternType.Macro, "Tropical, Sky to Ocean", 0.5, "C", 120);
    IntegrationTestEntity.insertPatternMeme(2, 4, "Tropical");
    // " phase offset 0
    IntegrationTestEntity.insertPhase(3, 4, PhaseType.Macro, 0, 0, "Start Sky", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Sky");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    // " phase offset 1
    IntegrationTestEntity.insertPhase(4, 4, PhaseType.Macro, 1, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Ocean");
    IntegrationTestEntity.insertPhaseMeme(49, 4, "Sky");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");
    // " phase offset 2
    IntegrationTestEntity.insertPhase(5, 4, PhaseType.Macro, 2, 0, "Finish Ocean", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertPhaseMeme(5, 4, "Ocean");
    IntegrationTestEntity.insertPhaseChord(5, 5, 0, "Ab minor");

    // "Tangy, Chunky to Smooth" macro-pattern in house library
    IntegrationTestEntity.insertPattern(3, 3, 2, PatternType.Macro, "Tangy, Chunky to Smooth", 0.5, "G minor", 120);
    IntegrationTestEntity.insertPatternMeme(1, 3, "Tangy");
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

    // A basic beat
    IntegrationTestEntity.insertPattern(35, 3, 2, PatternType.Rhythm, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertPatternMeme(343, 35, "Basic");
    IntegrationTestEntity.insertPhase(315, 35, PhaseType.Intro, 0, 16, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(346, 315, "Heavy");
    IntegrationTestEntity.insertPhase(316, 35, PhaseType.Loop, 0, 16, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(347, 316, "Heavy");

    // Chain "Test Print #1" has 5 total links
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertLink(1, 1, 0, LinkState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, LinkState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");

    // Chain "Test Print #1" has this link that was just crafted
    IntegrationTestEntity.insertLink(3, 1, 2, LinkState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(25, 3, 4, PatternType.Macro, 0, 3);
    IntegrationTestEntity.insertChoice(26, 3, 5, PatternType.Main, 1, -4);
    IntegrationTestEntity.insertChoice(27, 3, 35, PatternType.Rhythm, 0, 5);

    // Chain "Test Print #1" has a link in crafting state - Foundation is complete
    link4 = IntegrationTestEntity.insertLink(4, 1, 3, LinkState.Crafting, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "G minor", 16, 0.45, 125, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLinkMeme(101,4,"Regret");
    IntegrationTestEntity.insertLinkMeme(102,4,"Sky");
    IntegrationTestEntity.insertLinkMeme(103,4,"Hindsight");
    IntegrationTestEntity.insertLinkMeme(104,4,"Tropical");
    IntegrationTestEntity.insertChoice(101,4, 4, PatternType.Macro,1,3);
    IntegrationTestEntity.insertChoice(102,4, 15, PatternType.Main,0,0);
    IntegrationTestEntity.insertLinkChord(101,4,0,"G minor");
    IntegrationTestEntity.insertLinkChord(102,4,8,"Ab minor");

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
  public void craftStructureNextMain() throws Exception {
    Basis basis = basisFactory.createBasis(link4);

    craftFactory.structure(basis).doWork();

    // choice of rhythm-type pattern
    assertNotNull(injector.getInstance(ChoiceDAO.class).readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), BigInteger.valueOf(4), PatternType.Rhythm));
  }

}
