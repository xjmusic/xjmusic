// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craftworker.craft;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.basis.Basis;
import io.outright.xj.core.basis.BasisFactory;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.craftworker.CraftworkerModule;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Timestamp;

import static io.outright.xj.core.Tables.CHOICE;
import static org.junit.Assert.assertNotNull;

public class CraftStructureNextMacroIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private Injector injector = Guice.createInjector(new CoreModule(), new CraftworkerModule());
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
    IntegrationTestEntity.insertUserRole(1, 2, Role.ADMIN);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(2, 3, Role.USER);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Tropical, Wild to Cozy" macro-idea in house library
    IntegrationTestEntity.insertIdea(4, 3, 2, Idea.MACRO, "Tropical, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertIdeaMeme(2, 4, "Tropical");
    // " phase offset 0
    IntegrationTestEntity.insertPhase(3, 4, 0, 0, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    // " phase offset 1
    IntegrationTestEntity.insertPhase(4, 4, 1, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseMeme(49, 4, "Wild");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");
    // " phase offset 2
    IntegrationTestEntity.insertPhase(5, 4, 2, 0, "Finish Cozy", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertPhaseMeme(5, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(5, 5, 0, "Ab minor");

    // "Tangy, Chunky to Smooth" macro-idea in house library
    IntegrationTestEntity.insertIdea(3, 3, 2, Idea.MACRO, "Tangy, Chunky to Smooth", 0.5, "G minor", 120);
    IntegrationTestEntity.insertIdeaMeme(1, 3, "Tangy");
    // # phase offset 0
    IntegrationTestEntity.insertPhase(1, 3, 0, 0, "Start Chunky", 0.4, "G minor", 115);
    IntegrationTestEntity.insertPhaseMeme(1, 1, "Chunky");
    IntegrationTestEntity.insertPhaseChord(1, 1, 0, "G minor");
    // # phase offset 1
    IntegrationTestEntity.insertPhase(2, 3, 1, 0, "Finish Smooth", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(2, 2, "Smooth");
    IntegrationTestEntity.insertPhaseChord(2, 2, 0, "C");

    // Main idea
    IntegrationTestEntity.insertIdea(5, 3, 2, Idea.MAIN, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertIdeaMeme(3, 5, "Outlook");
    // # phase offset 0
    IntegrationTestEntity.insertPhase(15, 5, 0, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Optimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "G major");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "Ab minor");
    // # phase offset 1
    IntegrationTestEntity.insertPhase(16, 5, 1, 16, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "C major");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "Bb minor");

    // Another Main idea to go to
    IntegrationTestEntity.insertIdea(15, 3, 2, Idea.MAIN, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertIdeaMeme(43, 15, "Hindsight");
    IntegrationTestEntity.insertPhase(415, 15, 0, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(46, 415, "Regret");
    IntegrationTestEntity.insertPhaseChord(412, 415, 0, "G minor");
    IntegrationTestEntity.insertPhaseChord(414, 415, 8, "Ab minor");
    IntegrationTestEntity.insertPhase(416, 15, 1, 16, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(47, 416, "Pride");
    IntegrationTestEntity.insertPhaseMeme(149, 416, "Shame");
    IntegrationTestEntity.insertPhaseChord(416, 416, 0, "C major");
    IntegrationTestEntity.insertPhaseChord(418, 416, 8, "Bb major");

    // A basic beat
    IntegrationTestEntity.insertIdea(35, 3, 2, Idea.RHYTHM, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertIdeaMeme(343, 35, "Basic");
    IntegrationTestEntity.insertPhase(315, 35, 0, 16, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(346, 315, "Heavy");
    IntegrationTestEntity.insertPhase(316, 35, 1, 16, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(347, 316, "Heavy");

    // Chain "Test Print #1" has 5 total links
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, Link.DUBBING, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");

    // Chain "Test Print #1" has this link that was just crafted
    IntegrationTestEntity.insertLink(3, 1, 2, Link.CRAFTED, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "Ab minor", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav"); // final key is based on phase of main idea
    IntegrationTestEntity.insertChoice(25, 3, 4, Choice.MACRO, 1, 3); // macro-idea current phase is transposed to be Db minor
    IntegrationTestEntity.insertChoice(26, 3, 5, Choice.MAIN, 1, 1); // main-key of previous link is transposed to match, Db minor
    IntegrationTestEntity.insertChoice(27, 3, 35, Choice.RHYTHM, 0, -4);

    // Chain "Test Print #1" has a link in crafting state - Foundation is complete
    link4 = IntegrationTestEntity.insertLink(4, 1, 3, Link.CRAFTING, Timestamp.valueOf("2017-02-14 12:03:08.000001"),Timestamp.valueOf("2017-02-14 12:03:15.836735"),"F minor", 16, 0.45, 125, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLinkMeme(101,4,"Hindsight");
    IntegrationTestEntity.insertLinkMeme(102,4,"Chunky");
    IntegrationTestEntity.insertLinkMeme(103,4,"Regret");
    IntegrationTestEntity.insertLinkMeme(104,4,"Tangy");
    IntegrationTestEntity.insertChoice(101,4, 3, Choice.MACRO,0,4);
    IntegrationTestEntity.insertChoice(102,4, 15, Choice.MAIN,0,-2);
    IntegrationTestEntity.insertLinkChord(101,4,0,"F minor");
    IntegrationTestEntity.insertLinkChord(102,4,8,"Gb minor");

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
  public void craftStructureNextMacro() throws Exception {
    Basis basis = basisFactory.createBasis(link4);

    craftFactory.structure(basis).doWork();

    // choice of rhythm-type idea
    assertNotNull(IntegrationTestService.getDb().selectFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(ULong.valueOf(4)))
      .and(CHOICE.IDEA_ID.eq(ULong.valueOf(35)))
      .and(CHOICE.TYPE.eq(Choice.RHYTHM))
      .and(CHOICE.TRANSPOSE.eq(5))
      .and(CHOICE.PHASE_OFFSET.eq(ULong.valueOf(0)))
      .fetchOne());

  }

}
