// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.craft.CraftFactory;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.SegmentChordDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SegmentMemeDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.testing.Testing;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import io.xj.craft.CraftModule;


import com.google.common.collect.ImmutableList;
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

public class CraftFoundationNextMacroIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "kingfruits"
    IntegrationTestEntity.insertAccount(1, "kingfruits");

    // Candy has "user" and "admin" roles, belongs to account "kingfruits", has "google" auth
    IntegrationTestEntity.insertUser(2, "candy", "candy@email.com", "http://pictures.com/candy.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Claude has a "user" role and belongs to account "kingfruits"
    IntegrationTestEntity.insertUser(3, "claude", "claude@email.com", "http://pictures.com/claude.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Metal, Wild to Basement" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Wild to Basement", 0.5, "C", 120);
    // " pattern offset 0
    IntegrationTestEntity.insertPatternSequencePattern(3, 4, PatternType.Macro, PatternState.Published, 0, 0, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertPatternMeme(3, 3, "Wild");
    IntegrationTestEntity.insertPatternChord(3, 3, 0, "C");
    // " pattern offset 1
    IntegrationTestEntity.insertPatternSequencePattern(4, 4, PatternType.Macro, PatternState.Published, 1, 0, "Intermediate", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPatternMeme(4, 4, "Basement");
    IntegrationTestEntity.insertPatternMeme(49, 4, "Wild");
    IntegrationTestEntity.insertPatternChord(4, 4, 0, "Bb minor");
    // " pattern offset 2
    IntegrationTestEntity.insertPatternSequencePattern(5, 4, PatternType.Macro, PatternState.Published, 2, 0, "Finish Basement", 0.4, "Ab minor", 125);
    IntegrationTestEntity.insertPatternMeme(5, 4, "Basement");
    IntegrationTestEntity.insertPatternChord(5, 5, 0, "Ab minor");

    // "Chunky to Smooth" macro-sequence in house library
    IntegrationTestEntity.insertSequence(3, 3, 2, SequenceType.Macro, SequenceState.Published, "Chunky to Smooth", 0.5, "G minor", 120);
    // # pattern offset 0
    IntegrationTestEntity.insertPatternSequencePattern(1, 3, PatternType.Macro, PatternState.Published, 0, 0, "Start Chunky", 0.4, "G minor", 115);
    IntegrationTestEntity.insertPatternMeme(1, 1, "Chunky");
    IntegrationTestEntity.insertPatternChord(1, 1, 0, "G minor");
    // # pattern offset 1
    IntegrationTestEntity.insertPatternSequencePattern(2, 3, PatternType.Macro, PatternState.Published, 1, 0, "Finish Smooth", 0.6, "C", 125);
    IntegrationTestEntity.insertPatternMeme(2, 2, "Smooth");
    IntegrationTestEntity.insertPatternChord(2, 2, 0, "C");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "C minor", 140);
    IntegrationTestEntity.insertSequenceMeme(3, 5, "Outlook");
    // # pattern offset 0
    IntegrationTestEntity.insertPatternSequencePattern(15, 5, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPatternMeme(6, 15, "Optimism");
    IntegrationTestEntity.insertPatternChord(12, 15, 0, "G major");
    IntegrationTestEntity.insertPatternChord(14, 15, 8, "Ab minor");
    // # pattern offset 1
    IntegrationTestEntity.insertPatternSequencePattern(16, 5, PatternType.Main, PatternState.Published, 1, 16, "Drop", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPatternMeme(7, 16, "Pessimism");
    IntegrationTestEntity.insertPatternChord(16, 16, 0, "C major");
    IntegrationTestEntity.insertPatternChord(18, 16, 8, "Bb minor");

    // Another Main sequence to go to
    IntegrationTestEntity.insertSequence(15, 3, 2, SequenceType.Main, SequenceState.Published, "Next Jam", 0.2, "Db minor", 140);
    IntegrationTestEntity.insertSequenceMeme(43, 15, "Hindsight");
    IntegrationTestEntity.insertPatternSequencePattern(415, 15, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "G minor", 135.0);
    IntegrationTestEntity.insertPatternMeme(46, 415, "Regret");
    IntegrationTestEntity.insertPatternChord(412, 415, 0, "G minor");
    IntegrationTestEntity.insertPatternChord(414, 415, 8, "Ab minor");
    IntegrationTestEntity.insertPatternSequencePattern(416, 15, PatternType.Main, PatternState.Published, 1, 16, "Outro", 0.5, "A major", 135.0);
    IntegrationTestEntity.insertPatternMeme(47, 416, "Pride");
    IntegrationTestEntity.insertPatternMeme(149, 416, "Shame");
    IntegrationTestEntity.insertPatternChord(416, 416, 0, "C major");
    IntegrationTestEntity.insertPatternChord(418, 416, 8, "Bb major");

    // [#154090557] this Chord should be ignored, because it's past the end of the main-pattern total
    IntegrationTestEntity.insertPatternChord(42, 415, 75, "G-9");

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-segment-97898asdf7892.wav");
    IntegrationTestEntity.insertSegment(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-segment-97898asdf7892.wav");

    // Chain "Test Print #1" has this segment that was just crafted
    IntegrationTestEntity.insertSegment(3, 1, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "Ab minor", 64, 0.30, 120, "chain-1-segment-97898asdf7892.wav"); // final key is based on pattern of main sequence
    IntegrationTestEntity.insertChoice(25, 3, 4, SequenceType.Macro, 1, 3); // macro-sequence current pattern is transposed to be Db minor
    IntegrationTestEntity.insertChoice(26, 3, 5, SequenceType.Main, 1, 1); // main-key of previous segment is transposed to match, Db minor

    // Chain "Test Print #1" has a planned segment
    segment4 = IntegrationTestEntity.insertSegment_Planned(4, 1, 3, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

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
  public void craftFoundationNextMacro() throws Exception {
    Basis basis = basisFactory.createBasis(segment4);

    craftFactory.macroMain(basis).doWork();

    Segment resultSegment = injector.getInstance(SegmentDAO.class).readOneAtChainOffset(Access.internal(), BigInteger.valueOf(1), BigInteger.valueOf(3));

    assertEquals(Timestamp.valueOf("2017-02-14 12:03:15.8425"), resultSegment.getEndAt());
    assertEquals(Integer.valueOf(16), resultSegment.getTotal());
    assertEquals(Double.valueOf(0.45), resultSegment.getDensity());
    assertEquals("F minor", resultSegment.getKey());
    assertEquals(Double.valueOf(125), resultSegment.getTempo());

    Collection<SegmentMeme> resultSegmentMemes = injector.getInstance(SegmentMemeDAO.class).readAll(Access.internal(), ImmutableList.of(resultSegment.getId()));

    assertEquals(3, resultSegmentMemes.size());
    resultSegmentMemes.forEach(segmentMemeRecord -> Testing.assertIn(new String[]{"Hindsight", "Chunky", "Regret"}, segmentMemeRecord.getName()));

    Collection<SegmentChord> resultSegmentChords = injector.getInstance(SegmentChordDAO.class).readAll(Access.internal(), ImmutableList.of(resultSegment.getId()));
    assertEquals(2, resultSegmentChords.size());
    Iterator<SegmentChord> it = resultSegmentChords.iterator();

    SegmentChord chordOne = it.next();
    assertEquals(Double.valueOf(0), chordOne.getPosition());
    assertEquals("F minor", chordOne.getName());

    SegmentChord chordTwo = it.next();
    assertEquals(Double.valueOf(8), chordTwo.getPosition());
    assertEquals("Gb minor", chordTwo.getName());

    // choice of macro-type sequence
    Choice resultMacroChoice = injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), BigInteger.valueOf(4), SequenceType.Macro);
    assertNotNull(resultMacroChoice);
    assertEquals(BigInteger.valueOf(3), resultMacroChoice.getSequenceId());
    assertEquals(Integer.valueOf(4), resultMacroChoice.getTranspose());
    assertEquals(BigInteger.valueOf(0), resultMacroChoice.getSequencePatternOffset());

    // choice of main-type sequence
    Choice resultMainChoice = injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), BigInteger.valueOf(4), SequenceType.Main);
    assertNotNull(resultMainChoice);
    assertEquals(BigInteger.valueOf(15), resultMainChoice.getSequenceId());
    assertEquals(Integer.valueOf(-2), resultMainChoice.getTranspose());
    assertEquals(BigInteger.valueOf(0), resultMainChoice.getSequencePatternOffset());

  }

}
