// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.sequence.SequenceType;
import io.xj.craft.BaseIT;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CraftRhythmVoiceInitialIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private SegmentFactory segmentFactory;

  // Testing entities for reference
  private Segment segment6;

  @Before
  public void setUp() throws Exception {
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    IntegrationTestEntity.reset();
    insertLibraryB1();
    insertLibraryB3();

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    IntegrationTestEntity.insertChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);

    // bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(2, 2);
  }

  @Test
  public void craftRhythmVoiceInitial() throws Exception {
    insertSegments6(true);
    Fabricator fabricator = fabricatorFactory.fabricate(segment6);

    craftFactory.rhythm(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), BigInteger.valueOf(6));
    assertNotNull(result.getArrangementsForChoice(result.getChoiceOfType(SequenceType.Rhythm)));
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedBleep = 0;
    int pickedToot = 0;
    for (Pick pick : result.getPicks()) {
      if (pick.getAudioId().equals(BigInteger.valueOf(1)))
        pickedKick++;
      if (pick.getAudioId().equals(BigInteger.valueOf(2)))
        pickedSnare++;
      if (pick.getAudioId().equals(BigInteger.valueOf(3)))
        pickedBleep++;
      if (pick.getAudioId().equals(BigInteger.valueOf(4)))
        pickedToot++;
    }
    assertEquals(12, pickedKick);
    assertEquals(12, pickedSnare);
    assertEquals(4, pickedBleep);
    assertEquals(4, pickedToot);
  }

  @Test
  public void craftRhythmVoiceInitial_okWhenNoRhythmChoice() throws Exception {
    insertSegments6(false);
    Fabricator fabricator = fabricatorFactory.fabricate(segment6);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the rhythm choice only if specified

   @param includeRhythmChoice if desired for the purpose of this test
   */
  private void insertSegments6(boolean includeRhythmChoice) throws CoreException {
    // segment crafting
    segment6 = segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(2))
      .setOffset(BigInteger.valueOf(3))
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:07.384616Z")
      .setKey("D Major")
      .setTotal(32)
      .setDensity(0.55)
      .setTempo(130.0)
      .setWaveformKey("chain-1-segment-9f7s89d8a7892.wav");
    segment6.add(new Choice()
      .setSegmentId(BigInteger.valueOf(6))
      .setSequencePatternId(BigInteger.valueOf(340))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(0));
    segment6.add(new Choice()
      .setSegmentId(BigInteger.valueOf(6))
      .setSequencePatternId(BigInteger.valueOf(1550))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(-6));
    if (includeRhythmChoice)
      segment6.add(new Choice()
        .setSegmentId(BigInteger.valueOf(6))
        .setSequenceId(BigInteger.valueOf(99035))
        .setTypeEnum(SequenceType.Rhythm)
        .setTranspose(0));
    ImmutableList.of("Special", "Wild", "Pessimism", "Outlook").forEach(memeName -> {
      try {
        segment6.add(new SegmentMeme()
          .setSegmentId(BigInteger.valueOf(6))
          .setName(memeName));
      } catch (CoreException ignored) {
      }
    });
    segment6.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(6))
      .setPosition(0.0)
      .setName("C minor"));
    segment6.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(6))
      .setPosition(8.0)
      .setName("Db minor"));
    IntegrationTestEntity.insert(segment6);
  }

}
