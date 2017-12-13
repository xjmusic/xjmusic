// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.work.craft.foundation;

import org.jooq.Result;
import org.jooq.types.UInteger;
import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.xj.core.CoreModule;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.integration.IntegrationTestService;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.link.Link;
import io.xj.core.model.role.Role;
import io.xj.core.tables.records.LinkMemeRecord;
import io.xj.core.tables.records.LinkRecord;
import io.xj.core.testing.Testing;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisFactory;
import io.xj.core.craft.CraftFactory;
import io.xj.worker.WorkerModule;
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

public class CraftFoundationInitialIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new WorkerModule());
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
    IntegrationTestEntity.insertUserRole(1, 2, Role.ADMIN);

    // Jenny has a "user" role and belongs to account "bananas"
    IntegrationTestEntity.insertUser(3, "jenny", "jenny@email.com", "http://pictures.com/jenny.gif");
    IntegrationTestEntity.insertUserRole(2, 3, Role.USER);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Tropical, Wild to Cozy" macro-pattern in house library
    IntegrationTestEntity.insertPattern(4, 3, 2, PatternType.Macro, "Tropical, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertPatternMeme(2, 4, "Tropical");
    IntegrationTestEntity.insertPhase(3, 4, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPhaseMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPhaseChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPhase(4, 4, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPhaseMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPhaseChord(4, 4, 0, "Bb minor");

    // Main pattern
    IntegrationTestEntity.insertPattern(5, 3, 2, PatternType.Main, "Main Jam", 0.2, "F# minor", 140);
    IntegrationTestEntity.insertPatternMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPhase(15, 5, 0, 16, "Intro", 0.5, "F# minor", 135.0);
    IntegrationTestEntity.insertPhaseMeme(6, 15, "Pessimism");
    IntegrationTestEntity.insertPhaseChord(12, 15, 0, "F# minor");
    IntegrationTestEntity.insertPhaseChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPhase(16, 5, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPhaseMeme(7, 16, "Optimism");
    IntegrationTestEntity.insertPhaseChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPhaseChord(18, 16, 8, "G major");

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

    // choice of macro-type pattern
    assertNotNull(IntegrationTestService.getDb().selectFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(ULong.valueOf(6)))
      .and(CHOICE.PATTERN_ID.eq(ULong.valueOf(4)))
      .and(CHOICE.TYPE.eq(PatternType.Macro.toString()))
      .and(CHOICE.TRANSPOSE.eq(0))
      .and(CHOICE.PHASE_OFFSET.eq(ULong.valueOf(0)))
      .fetchOne());

    // choice of main-type pattern
    assertNotNull(IntegrationTestService.getDb().selectFrom(CHOICE)
      .where(CHOICE.LINK_ID.eq(ULong.valueOf(6)))
      .and(CHOICE.PATTERN_ID.eq(ULong.valueOf(5)))
      .and(CHOICE.TYPE.eq(PatternType.Main.toString()))
      .and(CHOICE.TRANSPOSE.eq(-6))
      .and(CHOICE.PHASE_OFFSET.eq(ULong.valueOf(0)))
      .fetchOne());

  }
}
