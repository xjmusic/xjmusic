// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

import com.google.common.collect.Lists;
import io.xj.core.CoreModule;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.craft.BaseIT;
import io.xj.craft.CraftFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.craft.CraftModule;


import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;

import static io.xj.core.Assert.assertExactChords;
import static io.xj.core.Assert.assertExactMemes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CraftFoundationNextMacroIT extends BaseIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private SegmentFactory segmentFactory;

  // Testing entities for reference
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    IntegrationTestEntity.reset();
    insertLibraryB1();
    insertLibraryB2();

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);
    IntegrationTestEntity.insertSegment_NoContent(1, 1, 0, SegmentState.Dubbed, Timestamp.valueOf("2017-02-14 12:01:00.000001"), Timestamp.valueOf("2017-02-14 12:01:32.000001"), "D major", 64, 0.73, 120, "chain-1-segment-9f7s89d8a7892.wav");
    IntegrationTestEntity.insertSegment_NoContent(2, 1, 1, SegmentState.Dubbing, Timestamp.valueOf("2017-02-14 12:01:32.000001"), Timestamp.valueOf("2017-02-14 12:02:04.000001"), "Db minor", 64, 0.85, 120, "chain-1-segment-9f7s89d8a7892.wav");

    // Chain "Test Print #1" has this segment that was just crafted
    Segment seg3 = segmentFactory.newSegment(BigInteger.valueOf(3))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(2))
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14 12:02:04.000001")
      .setEndAt("2017-02-14 12:02:36.000001")
      .setKey("Ab minor")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chain-1-segment-9f7s89d8a7892.wav");
    seg3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequencePatternId(BigInteger.valueOf(441))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(3));
    seg3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequencePatternId(BigInteger.valueOf(1651))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(1));
    IntegrationTestEntity.insert(seg3);

    // Chain "Test Print #1" has a planned segment
    segment4 = IntegrationTestEntity.insertSegment_Planned(4, 1, 3, Timestamp.valueOf("2017-02-14 12:03:08.000001"));

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2);
  }

  @Test
  public void craftFoundationNextMacro() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(segment4);

    craftFactory.macroMain(fabricator).doWork();

    assertEquals(Timestamp.valueOf("2017-02-14 12:03:15.697116384"), segment4.getEndAt());
    assertEquals(Integer.valueOf(16), segment4.getTotal());
    assertEquals(Double.valueOf(0.55), segment4.getDensity());
    assertEquals("F minor", segment4.getKey());
    assertEquals(Double.valueOf(130), segment4.getTempo());
    assertExactMemes(Lists.newArrayList("Regret", "Wild", "Hindsight", "Tropical"), segment4.getMemes());
    assertExactChords(Lists.newArrayList("F minor", "Gb minor"), segment4.getChords());
    assertEquals(BigInteger.valueOf(4), fabricator.getSequenceOfChoice(segment4.getChoiceOfType(SequenceType.Macro)).getId());
    assertEquals(Integer.valueOf(-1), segment4.getChoiceOfType(SequenceType.Macro).getTranspose());
    assertEquals(BigInteger.valueOf(0), fabricator.getSequencePatternOffsetForChoice(segment4.getChoiceOfType(SequenceType.Macro)));
    assertEquals(BigInteger.valueOf(15), fabricator.getSequenceOfChoice(segment4.getChoiceOfType(SequenceType.Main)).getId());
    assertEquals(Integer.valueOf(-2), segment4.getChoiceOfType(SequenceType.Main).getTranspose());
    assertEquals(BigInteger.valueOf(0), fabricator.getSequencePatternOffsetForChoice(segment4.getChoiceOfType(SequenceType.Main)));
  }

}
