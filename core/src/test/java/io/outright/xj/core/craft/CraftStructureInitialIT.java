// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.craft;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.role.Role;

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

public class CraftStructureInitialIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Injector injector = Guice.createInjector(new CoreModule());
  private CraftFactory craftFactory;

  // Testing entities for reference
  private Link link6;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.deleteAll();

    // Account "jams"
    IntegrationTestEntity.insertAccount(1, "jams");

    // Greg has "user" and "admin" roles, belongs to account "jams", has "google" auth
    IntegrationTestEntity.insertUser(2, "greg", "greg@email.com", "http://pictures.com/greg.gif");
    IntegrationTestEntity.insertUserRole(1, 2, Role.ADMIN);

    // Tonya has a "user" role and belongs to account "jams"
    IntegrationTestEntity.insertUser(3, "tonya", "tonya@email.com", "http://pictures.com/tonya.gif");
    IntegrationTestEntity.insertUserRole(2, 3, Role.USER);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Special, Wild to Cozy" macro-idea in house library
    IntegrationTestEntity.insertIdea(4, 3, 2, Idea.MACRO, "Special, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertIdeaMeme(2, 4, "Special");
    IntegrationTestEntity.insertPhase(3, 4, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPhase(4, 4, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");

    // Main idea
    IntegrationTestEntity.insertIdea(5, 3, 2, Idea.MAIN, "Main Jam", 0.2, "F# minor", 140);
    IntegrationTestEntity.insertIdeaMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPhase(15, 5, 0, 16, "Intro", 0.5, "F# minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "F# minor");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPhase(16, 5, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Optimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "G major");

    // A basic beat
    IntegrationTestEntity.insertIdea(35, 3, 2, Idea.RHYTHM, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertIdeaMeme(343, 35, "Basic");
    IntegrationTestEntity.insertPhase(315, 35, 0, 16, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(346, 315, "Heavy");
    IntegrationTestEntity.insertPhase(316, 35, 1, 16, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPhaseMeme(347, 316, "Heavy");

    // Support Idea
    IntegrationTestEntity.insertIdea(7, 3, 2, Idea.SUPPORT, "Support Jam", 0.3, "Cb minor", 170);

    // Chain "Print #2" has 1 initial link in crafting state - Foundation is complete
    IntegrationTestEntity.insertChain(2, 1, "Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);
    link6 = IntegrationTestEntity.insertLink(6, 2, 0, Link.CRAFTING, Timestamp.valueOf("2017-02-14 12:01:00.000001"),Timestamp.valueOf("2017-02-14 12:01:07.384616"),"C minor",16, 0.55, 130);
    IntegrationTestEntity.insertLinkMeme(101,6,"Special");
    IntegrationTestEntity.insertLinkMeme(102,6,"Wild");
    IntegrationTestEntity.insertLinkMeme(103,6,"Pessimism");
    IntegrationTestEntity.insertLinkMeme(104,6,"Outlook");
    IntegrationTestEntity.insertChoice(101,6, 4, Choice.MACRO,0,0);
    IntegrationTestEntity.insertChoice(102,6, 5, Choice.MAIN,10,-6);
    IntegrationTestEntity.insertLinkChord(101,6,0,"C minor");
    IntegrationTestEntity.insertLinkChord(102,6,8,"Db minor");

    // bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2, 2);

    // Instantiate the test subject
    craftFactory = injector.getInstance(CraftFactory.class);


  }

  @After
  public void tearDown() throws Exception {
    craftFactory = null;
  }

  @Test
  public void craftStructureInitial() throws Exception {
    Basis basis = craftFactory.createBasis(link6);

    craftFactory.structure(basis).craft();

    // choice of rhythm-type idea
    assertNotNull(IntegrationTestService.getDb().selectFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(ULong.valueOf(6)))
      .and(CHOICE.IDEA_ID.eq(ULong.valueOf(35)))
      .and(CHOICE.TYPE.eq(Choice.RHYTHM))
      .and(CHOICE.TRANSPOSE.eq(0))
      .and(CHOICE.PHASE_OFFSET.eq(ULong.valueOf(0)))
      .fetchOne());
  }
}
