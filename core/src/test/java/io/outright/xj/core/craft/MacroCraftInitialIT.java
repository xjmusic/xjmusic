// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.craft;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.integration.IntegrationTestEntity;
import io.outright.xj.core.integration.IntegrationTestService;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.LinkMemeRecord;
import io.outright.xj.core.tables.records.LinkRecord;
import io.outright.xj.core.util.testing.Testing;

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

import static io.outright.xj.core.Tables.CHOICE;
import static io.outright.xj.core.Tables.LINK;
import static io.outright.xj.core.Tables.LINK_CHORD;
import static io.outright.xj.core.tables.LinkMeme.LINK_MEME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MacroCraftInitialIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Injector injector = Guice.createInjector(new CoreModule());
  private CraftFactory craftFactory;

  // Testing entities for reference
  private Link link6;

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

    // Extra ideas
    IntegrationTestEntity.insertIdea(6, 3, 2, Idea.RHYTHM, "Beat Jam", 0.6, "D#", 150);
    IntegrationTestEntity.insertIdea(7, 3, 2, Idea.SUPPORT, "Support Jam", 0.3, "Cb minor", 170);

    // Chain "Print #2" has 1 initial planned link
    IntegrationTestEntity.insertChain(2, 1, "Print #2", Chain.PRODUCTION, Chain.FABRICATING, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null);
    link6 = IntegrationTestEntity.insertLink_Planned(6, 2, 0, Timestamp.valueOf("2017-02-14 12:01:00.000001"));
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
  public void macroCraftInitial() throws Exception {
    craftFactory.createMacroCraft(link6).craft();

    LinkRecord resultLink = IntegrationTestService.getDb().selectFrom(LINK)
      .where(LINK.CHAIN_ID.eq(ULong.valueOf(2)))
      .and(LINK.OFFSET.eq(ULong.valueOf(0)))
      .fetchOne();
    assertEquals(Timestamp.valueOf("2017-02-14 12:01:07.384616"), resultLink.getEndAt());
    assertEquals(UInteger.valueOf(16), resultLink.getTotal());
    assertEquals(Double.valueOf(0.55), resultLink.getDensity());
    assertEquals("C minor", resultLink.getKey());
    assertEquals(Double.valueOf(130), resultLink.getTempo());

    Result<LinkMemeRecord> resultLinkMemes = IntegrationTestService.getDb().selectFrom(LINK_MEME)
      .where(LINK_MEME.LINK_ID.eq(ULong.valueOf(6)))
      .fetch();
    assertEquals(4, resultLinkMemes.size());
    resultLinkMemes.forEach(linkMemeRecord -> Testing.assertIn(new String[]{"Tropical", "Wild", "Pessimism", "Outlook"}, linkMemeRecord.getName()));

    // chord @ 0
    assertNotNull(IntegrationTestService.getDb().selectFrom(LINK_CHORD)
      .where(LINK_CHORD.LINK_ID.eq(ULong.valueOf(6)))
      .and(LINK_CHORD.POSITION.eq(Double.valueOf(0)))
      .and(LINK_CHORD.NAME.eq("C minor"))
      .fetchOne());

    // chord @ 8
    assertNotNull(IntegrationTestService.getDb().selectFrom(LINK_CHORD)
      .where(LINK_CHORD.LINK_ID.eq(ULong.valueOf(6)))
      .and(LINK_CHORD.POSITION.eq(Double.valueOf(8)))
      .and(LINK_CHORD.NAME.eq("Db minor"))
      .fetchOne());

    // choice of macro-type idea
    assertNotNull(IntegrationTestService.getDb().selectFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(ULong.valueOf(6)))
      .and(CHOICE.IDEA_ID.eq(ULong.valueOf(4)))
      .and(CHOICE.TYPE.eq(Choice.MACRO))
      .and(CHOICE.TRANSPOSE.eq(0))
      .and(CHOICE.PHASE_OFFSET.eq(ULong.valueOf(0)))
      .fetchOne());

    // choice of main-type idea
    assertNotNull(IntegrationTestService.getDb().selectFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(ULong.valueOf(6)))
      .and(CHOICE.IDEA_ID.eq(ULong.valueOf(5)))
      .and(CHOICE.TYPE.eq(Choice.MAIN))
      .and(CHOICE.TRANSPOSE.eq(-6))
      .and(CHOICE.PHASE_OFFSET.eq(ULong.valueOf(0)))
      .fetchOne());

  }
}
