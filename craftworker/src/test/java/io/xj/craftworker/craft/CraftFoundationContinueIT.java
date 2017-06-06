// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.craftworker.craft;

import io.xj.core.CoreModule;
import io.xj.core.basis.Basis;
import io.xj.core.basis.BasisFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.idea.Idea;
import io.xj.core.model.link.Link;
import io.xj.core.model.role.Role;
import io.xj.core.tables.records.LinkMemeRecord;
import io.xj.core.tables.records.LinkRecord;
import io.xj.core.util.testing.Testing;
import io.xj.craftworker.CraftworkerModule;

import org.jooq.Result;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Timestamp;

import static io.xj.core.Tables.CHOICE;
import static io.xj.core.Tables.LINK;
import static io.xj.core.Tables.LINK_CHORD;
import static io.xj.core.tables.LinkMeme.LINK_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CraftFoundationContinueIT {
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
    IntegrationTestEntity.insertPhase(3, 4, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPhase(4, 4, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");

    // Main idea
    IntegrationTestEntity.insertIdea(5, 3, 2, Idea.MAIN, "Main Jam", 0.2, "Gb minor", 140);
    IntegrationTestEntity.insertIdeaMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPhase(15, 5, 0, 16, "Intro", 0.5, "Gb minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "Gb minor");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPhase(16, 5, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Optimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "G major");

    // Extra ideas
    IntegrationTestEntity.insertIdea(6, 3, 2, Idea.RHYTHM, "Beat Jam", 0.6, "D#", 150);
    IntegrationTestEntity.insertIdea(7, 3, 2, Idea.SUPPORT, "Support Jam", 0.3, "Cb minor", 170);

    // Chain "Test Print #1" has 5 total links
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);
    IntegrationTestEntity.insertLink(1, 1, 0, Link.DUBBED, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertLink(2, 1, 1, Link.DUBBING, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-link-97898asdf7892.wav");

    // Chain "Test Print #1" has this link that was just crafted
    IntegrationTestEntity.insertLink(3, 1, 2, Link.CRAFTED, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-link-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(25, 3, 4, Choice.MACRO, 1, 3);
    IntegrationTestEntity.insertChoice(26, 3, 5, Choice.MAIN, 0, 5);

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

    LinkRecord resultLink = IntegrationTestService.getDb().selectFrom(LINK)
      .where(LINK.CHAIN_ID.eq(ULong.valueOf(1)))
      .and(LINK.OFFSET.eq(ULong.valueOf(3)))
      .fetchOne();
    assertEquals(Timestamp.valueOf("2017-02-14 12:03:15.836735"), resultLink.getEndAt());
    assertEquals(UInteger.valueOf(16), resultLink.getTotal());
    assertEquals(Double.valueOf(0.45), resultLink.getDensity());
    assertEquals("D major", resultLink.getKey());
    assertEquals(Double.valueOf(125), resultLink.getTempo());

    Result<LinkMemeRecord> resultLinkMemes = IntegrationTestService.getDb().selectFrom(LINK_MEME)
      .where(LINK_MEME.LINK_ID.eq(ULong.valueOf(4)))
      .fetch();
    assertEquals(4, resultLinkMemes.size());
    resultLinkMemes.forEach(linkMemeRecord -> Testing.assertIn(new String[]{"Cozy", "Tropical", "Outlook", "Optimism"}, linkMemeRecord.getName()));

    // chord @ 0
    assertNotNull(IntegrationTestService.getDb().selectFrom(LINK_CHORD)
      .where(LINK_CHORD.LINK_ID.eq(ULong.valueOf(4)))
      .and(LINK_CHORD.POSITION.eq(Double.valueOf(0)))
      .and(LINK_CHORD.NAME.eq("A minor"))
      .fetchOne());

    // chord @ 8
    assertNotNull(IntegrationTestService.getDb().selectFrom(LINK_CHORD)
      .where(LINK_CHORD.LINK_ID.eq(ULong.valueOf(4)))
      .and(LINK_CHORD.POSITION.eq(Double.valueOf(8)))
      .and(LINK_CHORD.NAME.eq("D major"))
      .fetchOne());

    // choice of macro-type idea
    assertNotNull(IntegrationTestService.getDb().selectFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(ULong.valueOf(4)))
      .and(CHOICE.IDEA_ID.eq(ULong.valueOf(4)))
      .and(CHOICE.TYPE.eq(Choice.MACRO))
      .and(CHOICE.TRANSPOSE.eq(3))
      .and(CHOICE.PHASE_OFFSET.eq(ULong.valueOf(1)))
      .fetchOne());

    // choice of main-type idea
    assertNotNull(IntegrationTestService.getDb().selectFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(ULong.valueOf(4)))
      .and(CHOICE.IDEA_ID.eq(ULong.valueOf(5)))
      .and(CHOICE.TYPE.eq(Choice.MAIN))
      .and(CHOICE.TRANSPOSE.eq(-5))
      .and(CHOICE.PHASE_OFFSET.eq(ULong.valueOf(1)))
      .fetchOne());

  }

}
