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

public class CraftFoundationInitialIT {
  @Rule public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Segment segment6;

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
    IntegrationTestEntity.insertPatternSequencePattern(4, 4, PatternType.Macro, PatternState.Published, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertPatternMeme(4, 4, "Cozy");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "F# minor", 140);
    IntegrationTestEntity.insertSequenceMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPatternSequencePattern(15, 5, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "F# minor", 135.0);
    IntegrationTestEntity.insertPatternMeme(6, 15, "Pessimism");
    IntegrationTestEntity.insertPatternChord(12, 15, 0, "F# minor");
    IntegrationTestEntity.insertPatternChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPatternSequencePattern(16, 5, PatternType.Main, PatternState.Published, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertPatternMeme(7, 16, "Optimism");
    IntegrationTestEntity.insertPatternChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPatternChord(18, 16, 8, "G major");

    // [#154090557] this Chord should be ignored, because it's past the end of the main-pattern total
    IntegrationTestEntity.insertPatternChord(42, 15, 75, "G-9");

    // Extra sequences
    IntegrationTestEntity.insertSequence(6, 3, 2, SequenceType.Rhythm, SequenceState.Published, "Beat Jam", 0.6, "D#", 150);
    IntegrationTestEntity.insertSequence(7, 3, 2, SequenceType.Detail, SequenceState.Published, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Print #2" has 1 initial planned segment
    IntegrationTestEntity.insertChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    segment6 = IntegrationTestEntity.insertSegment_Planned(6, 2, 0, Timestamp.valueOf("2017-02-14 12:01:00.000001"));
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
    Basis basis = basisFactory.createBasis(segment6);

    craftFactory.macroMain(basis).doWork();

    Segment resultSegment = injector.getInstance(SegmentDAO.class).readOneAtChainOffset(Access.internal(), BigInteger.valueOf(2), BigInteger.valueOf(0));
    assertEquals(Timestamp.valueOf("2017-02-14 12:01:07.384616"), resultSegment.getEndAt());
    assertEquals(Integer.valueOf(16), resultSegment.getTotal());
    assertEquals(0.55, resultSegment.getDensity(), 0.01);
    assertEquals("C minor", resultSegment.getKey());
    assertEquals(130.0, resultSegment.getTempo(), 0.01);

    Collection<SegmentMeme> resultSegmentMemes = injector.getInstance(SegmentMemeDAO.class).readAll(Access.internal(), ImmutableList.of(resultSegment.getId()));

    assertEquals(4, resultSegmentMemes.size());
    resultSegmentMemes.forEach(segmentMemeRecord -> Testing.assertIn(new String[]{"Tropical", "Wild", "Pessimism", "Outlook"}, segmentMemeRecord.getName()));

    Collection<SegmentChord> resultSegmentChords = injector.getInstance(SegmentChordDAO.class).readAll(Access.internal(), ImmutableList.of(resultSegment.getId()));
    assertEquals(2, resultSegmentChords.size());
    Iterator<SegmentChord> it = resultSegmentChords.iterator();

    SegmentChord chordOne = it.next();
    assertEquals(Double.valueOf(0), chordOne.getPosition());
    assertEquals("C minor", chordOne.getName());

    SegmentChord chordTwo = it.next();
    assertEquals(Double.valueOf(8), chordTwo.getPosition());
    assertEquals("Db minor", chordTwo.getName());


    // choice of macro-type sequence
    Choice resultMacroChoice = injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), resultSegment.getId(), SequenceType.Macro);
    assertNotNull(resultMacroChoice);
    assertEquals(BigInteger.valueOf(4), resultMacroChoice.getSequenceId());
    assertEquals(Integer.valueOf(0), resultMacroChoice.getTranspose());
    assertEquals(BigInteger.valueOf(0), resultMacroChoice.getSequencePatternOffset());

    // choice of main-type sequence
    Choice resultMainChoice = injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), resultSegment.getId(), SequenceType.Main);
    assertNotNull(resultMainChoice);
    assertEquals(BigInteger.valueOf(5), resultMainChoice.getSequenceId());
    assertEquals(Integer.valueOf(-6), resultMainChoice.getTranspose());
    assertEquals(BigInteger.valueOf(0), resultMainChoice.getSequencePatternOffset());

  }
}
