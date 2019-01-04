// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
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
import io.xj.core.model.sequence.SequenceState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.pattern.PatternState;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.testing.Testing;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONObject;
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

public class CraftFoundationContinueIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

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

    // "Tropical, Wild to Cozy" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Tropical, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertSequenceMeme(2, 4, "Tropical");
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
    IntegrationTestEntity.insertPatternMeme(6, 15, "Pessimism");
    IntegrationTestEntity.insertPatternChord(12, 15, 0, "Gb minor");
    IntegrationTestEntity.insertPatternChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPatternSequencePattern(16, 5, PatternType.Main, PatternState.Published, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPatternMeme(7, 16, "Optimism");
    IntegrationTestEntity.insertPatternChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPatternChord(18, 16, 8, "G major");

    // [#154090557] this Chord should be ignored, because it's past the end of the main-pattern total
    IntegrationTestEntity.insertPatternChord(42, 16, 75, "G-9");

    // Extra sequences
    IntegrationTestEntity.insertSequence(6, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Beat Jam", 0.6, "D#", 150);
    IntegrationTestEntity.insertSequence(7, 3, 2, SequenceType.Detail, SequenceState.Published, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertSegment(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-segment-97898asdf7892.wav", new JSONObject());
    IntegrationTestEntity.insertSegment(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-segment-97898asdf7892.wav", new JSONObject());

    // Chain "Test Print #1" has this segment that was just crafted
    IntegrationTestEntity.insertSegment(3, 1, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "F major", 64, 0.30, 120, "chain-1-segment-97898asdf7892.wav", new JSONObject());
    IntegrationTestEntity.insertChoice(25, 3, 4, SequenceType.Macro, 1, 3);
    IntegrationTestEntity.insertChoice(26, 3, 5, SequenceType.Main, 0, 5);

    // Chain "Test Print #1" has a planned segment
    segment4 = IntegrationTestEntity.insertSegment_Planned(4, 1, 3, Timestamp.valueOf("2017-02-14 12:03:08.000001"), new JSONObject());

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 1, 2);

    // Instantiate the test subject
    craftFactory = injector.getInstance(CraftFactory.class);
    basisFactory = injector.getInstance(BasisFactory.class);
  }

  @After
  public void tearDown() throws Exception {
  }

  /**
   [#162361525] persist Segment basis as JSON, then read basis JSON during fabrication of any segment that continues a main sequence
   */
  @Test
  public void craftFoundationContinue() throws Exception {
    Basis basis = basisFactory.createBasis(segment4);

    craftFactory.macroMain(basis).doWork();

    Segment resultSegment = injector.getInstance(SegmentDAO.class).readOneAtChainOffset(Access.internal(), BigInteger.valueOf(1), BigInteger.valueOf(3));
    assertEquals(Timestamp.valueOf("2017-02-14 12:03:15.8425"), resultSegment.getEndAt());
    assertEquals(Integer.valueOf(16), resultSegment.getTotal());
    assertEquals(Double.valueOf(0.45), resultSegment.getDensity());
    assertEquals("D major", resultSegment.getKey());
    assertEquals(Double.valueOf(125), resultSegment.getTempo());

    JSONObject resultBasis = resultSegment.getBasis();
    assertEquals("Continue", resultBasis.get("type"));

    Collection<SegmentMeme> resultSegmentMemes = injector.getInstance(SegmentMemeDAO.class).readAll(Access.internal(), ImmutableList.of(resultSegment.getId()));

    assertEquals(4, resultSegmentMemes.size());
    resultSegmentMemes.forEach(segmentMemeRecord -> Testing.assertIn(new String[]{"Cozy", "Tropical", "Outlook", "Optimism"}, segmentMemeRecord.getName()));

    Collection<SegmentChord> resultSegmentChords = injector.getInstance(SegmentChordDAO.class).readAll(Access.internal(), ImmutableList.of(resultSegment.getId()));
    assertEquals(2, resultSegmentChords.size());
    Iterator<SegmentChord> it = resultSegmentChords.iterator();

    SegmentChord chordOne = it.next();
    assertEquals(Double.valueOf(0), chordOne.getPosition());
    assertEquals("A minor", chordOne.getName());

    SegmentChord chordTwo = it.next();
    assertEquals(Double.valueOf(8), chordTwo.getPosition());
    assertEquals("D major", chordTwo.getName());

    // choice of macro-type sequence
    Choice resultMacroChoice = injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), BigInteger.valueOf(4), SequenceType.Macro);
    assertNotNull(resultMacroChoice);
    assertEquals(BigInteger.valueOf(4), resultMacroChoice.getSequenceId());
    assertEquals(Integer.valueOf(3), resultMacroChoice.getTranspose());
    assertEquals(BigInteger.valueOf(1), resultMacroChoice.getSequencePatternOffset());

    // choice of main-type sequence
    Choice resultMainChoice = injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), BigInteger.valueOf(4), SequenceType.Main);
    assertNotNull(resultMainChoice);
    assertEquals(BigInteger.valueOf(5), resultMainChoice.getSequenceId());
    assertEquals(Integer.valueOf(-5), resultMainChoice.getTranspose());
    assertEquals(BigInteger.valueOf(1), resultMainChoice.getSequencePatternOffset());

  }

}
