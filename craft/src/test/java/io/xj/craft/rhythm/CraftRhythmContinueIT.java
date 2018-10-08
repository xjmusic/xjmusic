// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import java.math.BigInteger;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;


import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Timestamp;

import static org.junit.Assert.assertNotNull;

public class CraftRhythmContinueIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "manatees"
    IntegrationTestEntity.insertAccount(1, "manatees");

    // Jen has "user" and "admin" roles, belongs to account "manatees", has "google" auth
    IntegrationTestEntity.insertUser(2, "jen", "jen@email.com", "http://pictures.com/jen.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Fred has a "user" role and belongs to account "manatees"
    IntegrationTestEntity.insertUser(3, "fred", "fred@email.com", "http://pictures.com/fred.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Classic, Wild to Cozy" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Classic, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertSequenceMeme(2, 4, "Classic");
    IntegrationTestEntity.insertPatternSequencePattern(3, 4, PatternType.Macro, PatternState.Published, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPatternMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPatternChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPatternSequencePattern(4, 4, PatternType.Macro, PatternState.Published, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPatternMeme(4, 4, "Cozy");
    IntegrationTestEntity.insertPatternChord(4, 4, 0, "Bb minor");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "Gb minor", 140);
    IntegrationTestEntity.insertSequenceMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPatternSequencePattern(15, 5, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "Gb minor", 135.0);
    IntegrationTestEntity.insertPatternMeme(6, 15, "Cloudy");
    IntegrationTestEntity.insertPatternChord(12, 15, 0, "Gb minor");
    IntegrationTestEntity.insertPatternChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPatternSequencePattern(16, 5, PatternType.Main, PatternState.Published, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPatternMeme(7, 16, "Rosy");
    IntegrationTestEntity.insertPatternChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPatternChord(18, 16, 8, "G major");

    // A basic beat
    IntegrationTestEntity.insertSequence(35, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertSequenceMeme(343, 35, "Basic");
    IntegrationTestEntity.insertPatternSequencePattern(315, 35, PatternType.Intro, PatternState.Published, 0, 16, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(346, 315, "Heavy");
    IntegrationTestEntity.insertPatternSequencePattern(316, 35, PatternType.Loop, PatternState.Published, 0, 16, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertPatternMeme(347, 316, "Heavy");

    // harmonicDetail sequence
    IntegrationTestEntity.insertSequence(7, 3, 2, SequenceType.Detail, SequenceState.Published, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-segment-97898asdf7892.wav");

    // Chain "Test Print #1" has this segment that was just crafted
    IntegrationTestEntity.insertSegment(3, 1, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertChoice(25, 3, 4, SequenceType.Macro, 1, 3);
    IntegrationTestEntity.insertChoice(26, 3, 5, SequenceType.Main, 0, 5);
    IntegrationTestEntity.insertChoice(27, 3, 35, SequenceType.Rhythm, 0, 5);

    // Chain "Test Print #1" is crafting - Foundation is complete
    segment4 = IntegrationTestEntity.insertSegment(4, 1, 3, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "D major", 16, 0.45, 120, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegmentMeme(101,4,"Cozy");
    IntegrationTestEntity.insertSegmentMeme(102,4,"Classic");
    IntegrationTestEntity.insertSegmentMeme(103,4,"Outlook");
    IntegrationTestEntity.insertSegmentMeme(104,4,"Rosy");
    IntegrationTestEntity.insertChoice(101,4, 4, SequenceType.Macro,1,3);
    IntegrationTestEntity.insertChoice(102,4, 5, SequenceType.Main,1,-5);
    IntegrationTestEntity.insertSegmentChord(101,4,0,"A minor");
    IntegrationTestEntity.insertSegmentChord(102,4,8,"D major");

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
  public void craftRhythmContinue() throws Exception {
    Basis basis = basisFactory.createBasis(segment4);

    craftFactory.rhythm(basis).doWork();

    // choice of rhythm-type sequence
    assertNotNull(injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), BigInteger.valueOf(4), SequenceType.Rhythm));
  }

  @Test
  public void craftRhythmContinue_okEvenWithoutPreviousSegmentRhythmChoice() throws Exception {
    Basis basis = basisFactory.createBasis(segment4);
    injector.getInstance(ChoiceDAO.class).destroy(Access.internal(), BigInteger.valueOf(27));

    craftFactory.rhythm(basis).doWork();

    // choice of rhythm-type sequence
    assertNotNull(injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), BigInteger.valueOf(4), SequenceType.Rhythm));
  }

}
