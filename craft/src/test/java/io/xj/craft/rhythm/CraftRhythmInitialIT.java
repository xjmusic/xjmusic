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

public class CraftRhythmInitialIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private BasisFactory basisFactory;

  // Testing entities for reference
  private Segment segment6;

  @Before
  public void setUp() throws Exception {
    IntegrationTestEntity.reset();

    // Account "jams"
    IntegrationTestEntity.insertAccount(1, "jams");

    // Greg has "user" and "admin" roles, belongs to account "jams", has "google" auth
    IntegrationTestEntity.insertUser(2, "greg", "greg@email.com", "http://pictures.com/greg.gif");
    IntegrationTestEntity.insertUserRole(1, 2, UserRoleType.Admin);

    // Tonya has a "user" role and belongs to account "jams"
    IntegrationTestEntity.insertUser(3, "tonya", "tonya@email.com", "http://pictures.com/tonya.gif");
    IntegrationTestEntity.insertUserRole(2, 3, UserRoleType.User);
    IntegrationTestEntity.insertAccountUser(3, 1, 3);

    // Library "house"
    IntegrationTestEntity.insertLibrary(2, 1, "house");

    // "Special, Wild to Cozy" macro-sequence in house library
    IntegrationTestEntity.insertSequence(4, 3, 2, SequenceType.Macro, SequenceState.Published, "Special, Wild to Cozy", 0.5, "C", 120);
    IntegrationTestEntity.insertSequenceMeme(2, 4, "Special");
    IntegrationTestEntity.insertPatternAndSequencePattern(3, 4, PatternType.Macro, PatternState.Published, 0, 64, "Start Wild", 0.6, "C", 125);
    IntegrationTestEntity.insertSequencePatternMeme(3, 4, 3, "Wild");
    IntegrationTestEntity.insertPatternChord(3, 3, 0, "C");
    IntegrationTestEntity.insertPatternAndSequencePattern(4, 4, PatternType.Macro, PatternState.Published, 1, 64, "Finish Finish Cozy", 0.4, "Bb minor", 115);
    IntegrationTestEntity.insertSequencePatternMeme(4, 4, 4, "Cozy");
    IntegrationTestEntity.insertPatternChord(4, 4, 0, "Bb minor");

    // Main sequence
    IntegrationTestEntity.insertSequence(5, 3, 2, SequenceType.Main, SequenceState.Published, "Main Jam", 0.2, "F# minor", 140);
    IntegrationTestEntity.insertSequenceMeme(3, 5, "Outlook");
    IntegrationTestEntity.insertPatternAndSequencePattern(15, 5, PatternType.Main, PatternState.Published, 0, 16, "Intro", 0.5, "F# minor", 135.0);
    IntegrationTestEntity.insertSequencePatternMeme(6, 5, 15, "Pessimism");
    IntegrationTestEntity.insertPatternChord(12, 15, 0, "F# minor");
    IntegrationTestEntity.insertPatternChord(14, 15, 8, "G minor");
    IntegrationTestEntity.insertPatternAndSequencePattern(16, 5, PatternType.Main, PatternState.Published, 1, 16, "Intro", 0.5, "G major", 135.0);
    IntegrationTestEntity.insertSequencePatternMeme(7, 5, 16, "Optimism");
    IntegrationTestEntity.insertPatternChord(16, 16, 0, "D minor");
    IntegrationTestEntity.insertPatternChord(18, 16, 8, "G major");

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

    // Detail Sequence
    IntegrationTestEntity.insertSequence(7, 3, 2, SequenceType.Detail, SequenceState.Published, "Detail Jam", 0.3, "Cb minor", 170);

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    IntegrationTestEntity.insertChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    segment6 = IntegrationTestEntity.insertSegment(6, 2, 0, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:07.384616"), "C minor", 16, 0.55, 130, "chain-1-segment-97898asdf7892.wav", new JSONObject());
    IntegrationTestEntity.insertSegmentMeme(101, 6, "Special");
    IntegrationTestEntity.insertSegmentMeme(102, 6, "Wild");
    IntegrationTestEntity.insertSegmentMeme(103, 6, "Pessimism");
    IntegrationTestEntity.insertSegmentMeme(104, 6, "Outlook");
    IntegrationTestEntity.insertChoice(101, 6, 4, SequenceType.Macro, 0, 0);
    IntegrationTestEntity.insertChoice(102, 6, 5, SequenceType.Main, 10, -6);
    IntegrationTestEntity.insertSegmentChord(101, 6, 0, "C minor");
    IntegrationTestEntity.insertSegmentChord(102, 6, 8, "Db minor");

    // bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2, 2);

    // Instantiate the test subject
    basisFactory = injector.getInstance(BasisFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void craftRhythmInitial() throws Exception {
    Basis basis = basisFactory.createBasis(segment6);

    craftFactory.rhythm(basis).doWork();

    // choice of rhythm-type sequence
    assertNotNull(injector.getInstance(ChoiceDAO.class).readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), BigInteger.valueOf(6), SequenceType.Rhythm));
  }
}
