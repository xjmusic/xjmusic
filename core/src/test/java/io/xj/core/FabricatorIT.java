//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_pattern.SequencePattern;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.Collection;

import static io.xj.core.Assert.assertExactMemes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class FabricatorIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private FabricatorFactory fabricatorFactory;
  private SegmentDAO segmentDAO;
  private SegmentFactory segmentFactory;

  @Before
  public void setUp() throws CoreException {
    IntegrationTestEntity.reset();
    insertLibraryB1();
    insertLibraryB2();

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertSegment_NoContent(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-segment-9f7s89d8a7892.wav");
    IntegrationTestEntity.insertSegment_NoContent(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-segment-9f7s89d8a7892.wav");

    IntegrationTestEntity.insertSegment_NoContent(3, 1, 2, SegmentState.Crafted, Timestamp.valueOf("2017-02-14 12:02:04.000001"), Timestamp.valueOf("2017-02-14 12:02:36.000001"), "Ab minor", 64, 0.30, 120, "chain-1-segment-9f7s89d8a7892.wav");

    IntegrationTestEntity.insertSegment_NoContent(4, 1, 3, SegmentState.Crafting, Timestamp.valueOf("2017-02-14 12:03:08.000001"), Timestamp.valueOf("2017-02-14 12:03:15.836735"), "F minor", 16, 0.45, 125, "chain-1-segment-9f7s89d8a7892.wav");

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2);

    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    segmentDAO = injector.getInstance(SegmentDAO.class);
  }

  /**
   [#165954619] Choice is by sequence, for rhythm- and detail-type sequences
   */
  @Test
  public void getSequenceOfChoice_bySequence() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    Sequence result = fabricator.getSequenceOfChoice(new Choice()
      .setTypeEnum(SequenceType.Rhythm)
      .setSequenceId(BigInteger.valueOf(35)));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(35), result.getId());
  }

  /**
   [#165954619] Choice is by sequence-pattern, for macro- or main-type sequences
   */
  @Test
  public void getSequenceOfChoice_bySequencePattern() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    Sequence result = fabricator.getSequenceOfChoice(new Choice()
      .setTypeEnum(SequenceType.Main)
      .setSequencePatternId(BigInteger.valueOf(415150)));

    assertNotNull(result);
    assertEquals(BigInteger.valueOf(15), result.getId());
  }

  @Test
  public void getMemesOfChoice() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertExactMemes(Lists.newArrayList("Wild", "Cozy", "Tropical"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequencePatternId(BigInteger.valueOf(441))
      .setTypeEnum(SequenceType.Macro)));
    assertExactMemes(Lists.newArrayList("Pessimism", "Outlook"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequencePatternId(BigInteger.valueOf(1651))
      .setTypeEnum(SequenceType.Main)));
    assertExactMemes(Lists.newArrayList("Basic"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequenceId(BigInteger.valueOf(35))
      .setTypeEnum(SequenceType.Rhythm)));
    assertExactMemes(Lists.newArrayList("Chunky", "Tangy"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(130))
      .setTypeEnum(SequenceType.Main)));
    assertExactMemes(Lists.newArrayList("Regret", "Hindsight"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(415150))
      .setTypeEnum(SequenceType.Main)));
    assertExactMemes(Lists.newArrayList("Pride", "Shame", "Hindsight"), fabricator.getMemesOfChoice(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(416151))
      .setTypeEnum(SequenceType.Main)));
  }

  @Test
  public void getCachedRandomSequencePatternAtOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    SequencePattern result0 = fabricator.getRandomSequencePatternAtOffset(BigInteger.valueOf(5), BigInteger.valueOf(0));
    assertNotNull(result0);
    assertEquals(BigInteger.valueOf(15), result0.getPatternId());
    SequencePattern result1 = fabricator.getRandomSequencePatternAtOffset(BigInteger.valueOf(5), BigInteger.valueOf(1));
    assertNotNull(result1);
    assertEquals(BigInteger.valueOf(16), result1.getPatternId());
  }

  @Test
  public void getPreviousSegmentsWithSameMainSequence() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    // segment-1 at offset-0 of chain-1
    Segment seg1 = segmentFactory.newSegment(BigInteger.valueOf(5))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(4))
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14 12:03:08.000001")
      .setEndAt("2017-02-14 12:03:15.836735")
      .setKey("F minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(125.0)
      .setWaveformKey("chain-1-segment-9f7s89d8a7892.wav");
    seg1.add(new Choice()
      .setSegmentId(BigInteger.valueOf(5))
      .setSequencePatternId(BigInteger.valueOf(231))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(4));
    seg1.add(new Choice()
      .setSegmentId(BigInteger.valueOf(5))
      .setSequencePatternId(BigInteger.valueOf(416151))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(-2));
    IntegrationTestEntity.insert(seg1);
    //
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(5)));

    Collection<Segment> result = fabricator.getPreviousSegmentsWithSameMainSequence();

    assertEquals(1, result.size());
  }

  @Test
  public void getCachedRandomSequencePatternAtOffset_exceptionOnNoCandidate() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("No candidate SequencePattern");
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    fabricator.getRandomSequencePatternAtOffset(BigInteger.valueOf(3), BigInteger.valueOf(7732));
  }

  @Test
  public void getCachedRandomPatternByType() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    Pattern result0 = fabricator.getRandomPatternByType(BigInteger.valueOf(35), PatternType.Intro);
    assertNotNull(result0);
    assertEquals(BigInteger.valueOf(315), result0.getId());
    Pattern result1 = fabricator.getRandomPatternByType(BigInteger.valueOf(35), PatternType.Loop);
    assertNotNull(result1);
    assertEquals(BigInteger.valueOf(317), result1.getId());
  }

  @Test
  public void getCachedRandomPatternByType_exceptionOnNoCandidate() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("No candidate Pattern");
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    fabricator.getRandomPatternByType(BigInteger.valueOf(35), PatternType.Outro);
  }

  @Test
  public void getMaxAvailableSequencePatternOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertSequencePattern(1652, 5, 16, 2);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertEquals(BigInteger.valueOf(2L), fabricator.getMaxAvailableSequencePatternOffset(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(1550))));
  }

  @Test
  public void getNextSequencePatternOffset() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertSequencePattern(1652, 5, 16, 2);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertEquals(BigInteger.valueOf(1L), fabricator.getNextSequencePatternOffset(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(1550))));
  }

  @Test
  public void getNextSequencePatternOffset_endLoopsBackToZero() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertEquals(BigInteger.valueOf(0L), fabricator.getNextSequencePatternOffset(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(1651))));
  }

  @Test
  public void hasOneMoreSequencePatternOffset_true() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertSequencePattern(1652, 5, 16, 2);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertTrue(fabricator.hasOneMoreSequencePatternOffset(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(1550))));
  }

  @Test
  public void hasOneMoreSequencePatternOffset_false() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertFalse(fabricator.hasOneMoreSequencePatternOffset(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(1651))));
  }

  @Test
  public void hasTwoMoreSequencePatternOffsets_true() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertSequencePattern(1652, 5, 16, 2);
    IntegrationTestEntity.insertSequencePattern(1653, 5, 16, 3);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertTrue(fabricator.hasTwoMoreSequencePatternOffsets(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(1651))));
  }


  /**
   Same conditions as that return true for hasOneMore offset, can be false for hasTwoMore
   */
  @Test
  public void hasTwoMoreSequencePatternOffsets_false() throws Exception {
    Access access = new Access(ImmutableMap.of(
      "roles", "Artist",
      "accounts", "1"
    ));
    IntegrationTestEntity.insertSequencePattern(1652, 5, 16, 2);
    Fabricator fabricator = fabricatorFactory.fabricate(segmentDAO.readOne(access, BigInteger.valueOf(4)));

    assertFalse(fabricator.hasTwoMoreSequencePatternOffsets(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(1651))));
  }

  // TODO assert that chain Configs are stored in segment content after fabrication occurs

}
