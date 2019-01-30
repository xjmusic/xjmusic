// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;

import static org.junit.Assert.assertNotNull;

public class CraftRhythmNextMainIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

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

    // "Tropical, Sky to Ocean" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Tropical, Sky to Ocean", 0.5, "C", 120);
    IntegrationTestEntity.insertSequenceMeme(2, 4, "Tropical");
    // " pattern offset 0
    IntegrationTestEntity.insertPatternAndSequencePattern(3, 4, PatternType.Macro, PatternState.Published, 0, 0, "Start Sky", 0.6, "C", 125);
    IntegrationTestEntity.insertSequencePatternMeme(3, 4, 3, "Sky");
    IntegrationTestEntity.insertPatternChord(3, 3, 0, "C");
    // " pattern offset 1
    IntegrationTestEntity.insertPatternAndSequencePattern(4, 4, PatternType.Macro, PatternState.Published, 1, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertSequencePatternMeme(4, 4, 4, "Ocean");
    IntegrationTestEntity.insertSequencePatternMeme(49, 4, 4, "Sky");
    IntegrationTestEntity.insertPatternChord(4, 4, 0, "Bb minor");
    // " pattern offset 2
    IntegrationTestEntity.insertPatternAndSequencePattern(5, 4, PatternType.Macro, PatternState.Published, 2, 0, "Finish Ocean", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertSequencePatternMeme(5, 4, 4, "Ocean");
    IntegrationTestEntity.insertPatternChord(5, 5, 0, "Ab minor");

    // "Tangy, Chunky to Smooth" macro-sequence in house library
    IntegrationTestEntity.insertSequence(3, 3, 2, SequenceType.Macro, SequenceState.Published, "Tangy, Chunky to Smooth", 0.5, "G minor", 120);
    IntegrationTestEntity.insertSequenceMeme(1, 3, "Tangy");
    // # pattern offset 0
    IntegrationTestEntity.insertPatternAndSequencePattern(1, 3, PatternType.Macro, PatternState.Published, 0, 0, "Start Chunky", 0.4, "G minor", 115);
    IntegrationTestEntity.insertSequencePatternMeme(1, 3, 1, "Chunky");
    IntegrationTestEntity.insertPatternChord(1, 1, 0, "G minor");
    // # pattern offset 1
    IntegrationTestEntity.insertPatternAndSequencePattern(2, 3, PatternType.Macro, PatternState.Published, 1, 0, "Finish Smooth", 0.6, "C", 125);
    IntegrationTestEntity.insertSequencePatternMeme(2, 3, 2, "Smooth");
    IntegrationTestEntity.insertPatternChord(2, 2, 0, "C");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertSequenceMeme(3, 5, "Outlook");
    // # pattern offset 0
    IntegrationTestEntity.insertPatternAndSequencePattern(15, 5, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertSequencePatternMeme(6, 5, 15, "Optimism");
    IntegrationTestEntity.insertPatternChord(12, 15, 0, "G major");
    IntegrationTestEntity.insertPatternChord(14, 15, 8, "Ab minor");
    // # pattern offset 1
    IntegrationTestEntity.insertPatternAndSequencePattern(16, 5, PatternType.Main, PatternState.Published, 1, 16, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertSequencePatternMeme(7, 5, 16, "Pessimism");
    IntegrationTestEntity.insertPatternChord(16, 16, 0, "C major");
    IntegrationTestEntity.insertPatternChord(18, 16, 8, "Bb minor");

    // Another Main sequence to go to
    IntegrationTestEntity.insertSequence(15, 3, 2, SequenceType.Main, SequenceState.Published, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertSequenceMeme(43, 15, "Hindsight");
    IntegrationTestEntity.insertPatternAndSequencePattern(415, 15, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertSequencePatternMeme(46, 15, 415, "Regret");
    IntegrationTestEntity.insertPatternChord(412, 415, 0, "G minor");
    IntegrationTestEntity.insertPatternChord(414, 415, 8, "Ab minor");
    IntegrationTestEntity.insertPatternAndSequencePattern(416, 15, PatternType.Main, PatternState.Published, 1, 16, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertSequencePatternMeme(47, 15, 416, "Pride");
    IntegrationTestEntity.insertSequencePatternMeme(149, 15, 416, "Shame");
    IntegrationTestEntity.insertPatternChord(416, 416, 0, "C major");
    IntegrationTestEntity.insertPatternChord(418, 416, 8, "Bb major");

    /*
    Note that in any real use case, after
    [#163158036] memes bound to sequence-patter
    because sequence-pattern binding is not considered for rhythm sequences,
    rhythm sequence patterns do not have memes.
     */

    // A basic beat
    IntegrationTestEntity.insertSequence(35, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Basic Beat", 0.2, "C", 121);
    IntegrationTestEntity.insertSequenceMeme(343, 35, "Basic");
    IntegrationTestEntity.insertPatternAndSequencePattern(315, 35, PatternType.Intro, PatternState.Published, 0, 16, "Drop", 0.5, "C", 125.0);
    IntegrationTestEntity.insertSequencePatternMeme(346, 35, 315, "Heavy");
    IntegrationTestEntity.insertPatternAndSequencePattern(316, 35, PatternType.Loop, PatternState.Published, 1, 16, "Continue", 0.5, "C", 125.0);
    IntegrationTestEntity.insertSequencePatternMeme(347, 35, 316, "Heavy");

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-segment-97898asdf7892.wav", new JSONObject());
    IntegrationTestEntity.insertSegment(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-segment-97898asdf7892.wav", new JSONObject());

    // Chain "Test Print #1" has this segment that was just crafted
    IntegrationTestEntity.insertSegment(3, 1, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-segment-97898asdf7892.wav", new JSONObject());
    IntegrationTestEntity.insertChoice(25, 3, 4, SequenceType.Macro, 0, 3);
    IntegrationTestEntity.insertChoice(26, 3, 5, SequenceType.Main, 1, -4);
    IntegrationTestEntity.insertChoice(27, 3, 35, SequenceType.Rhythm, 0, 5);

    // Chain "Test Print #1" has a segment in crafting state - Foundation is complete
    segment4 = IntegrationTestEntity.insertSegment(4, 1, 3, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "G minor", 16, 0.45, 125, "chain-1-segment-97898asdf7892.wav", new JSONObject());
    IntegrationTestEntity.insertSegmentMeme(101, 4, "Regret");
    IntegrationTestEntity.insertSegmentMeme(102, 4, "Sky");
    IntegrationTestEntity.insertSegmentMeme(103, 4, "Hindsight");
    IntegrationTestEntity.insertSegmentMeme(104, 4, "Tropical");
    IntegrationTestEntity.insertChoice(101, 4, 4, SequenceType.Macro, 1, 3);
    IntegrationTestEntity.insertChoice(102, 4, 15, SequenceType.Main, 0, 0);
    IntegrationTestEntity.insertSegmentChord(101, 4, 0, "G minor");
    IntegrationTestEntity.insertSegmentChord(102, 4, 8, "Ab minor");

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 1, 2);

    // Instantiate the test subject
    craftFactory = injector.getInstance(CraftFactory.class);
    basisFactory = injector.getInstance(BasisFactory.class);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void craftRhythmNextMain() throws Exception {
    Basis basis = basisFactory.createBasis(segment4);

    craftFactory.rhythm(basis).doWork();

    // choice of rhythm-type sequence
    assertNotNull(injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), BigInteger.valueOf(4), SequenceType.Rhythm));
  }

  @Test
  public void craftRhythmNextMain_okEvenWithoutPreviousSegmentRhythmChoice() throws Exception {
    Basis basis = basisFactory.createBasis(segment4);
    injector.getInstance(ChoiceDAO.class).destroy(Access.internal(), BigInteger.valueOf(27));

    craftFactory.rhythm(basis).doWork();

    // choice of rhythm-type sequence
    assertNotNull(injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), BigInteger.valueOf(4), SequenceType.Rhythm));
  }

}
