//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.program.ProgramType;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Instant;

import static io.xj.core.testing.Assert.assertExactMemes;
import static org.junit.Assert.assertEquals;

public class CraftSegmentPatternMemeIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(new CoreModule(), new CraftModule());
    craftFactory = injector.getInstance(CraftFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    segmentFactory = injector.getInstance(SegmentFactory.class);

    reset();
    insertFixtureB1();
    insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    insert(newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now(), newChainBinding(library2)));

    // Chain "Test Print #1" has this segment that was just crafted
    segment1 = segmentFactory.newSegment(BigInteger.valueOf(1))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(1L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav");
    segment1.add(new Choice()
      .setProgramId(BigInteger.valueOf(4))
      .setSequenceBindingId(program4_binding1.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(3));
    segment1.add(new Choice()
      .setProgramId(BigInteger.valueOf(5))
      .setSequenceBindingId(program5_binding1.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(5));
    insert(segment1);
  }

  /**
   [#165803886] Segment memes expected to be taken directly from sequence_pattern binding
   */
  @Test
  public void craftSegmentMemesDirectlyFromSequenceBindingBinding() throws Exception {
    segment2 = craftSegment(2, 1, 2, Instant.parse("2017-02-14T12:02:36.000001Z"));
    segment3 = craftSegment(3, 1, 3, segment2.getEndAt());
    segment4 = craftSegment(4, 1, 4, segment3.getEndAt());
    segment5 = craftSegment(5, 1, 5, segment4.getEndAt());

    assertEquals(FabricatorType.NextMacro, segment2.getType());
    assertExactMemes(Lists.newArrayList("Regret", "Wild", "Hindsight", "Tropical"), segment2.getMemes());

    assertEquals(FabricatorType.Continue, segment3.getType());
    assertExactMemes(Lists.newArrayList("Wild", "Hindsight", "Pride", "Shame", "Tropical"), segment3.getMemes());

    assertEquals(FabricatorType.Continue, segment3.getType());
    assertExactMemes(Lists.newArrayList("Wild", "Cozy", "Optimism", "Outlook", "Tropical"), segment4.getMemes());

    assertEquals(FabricatorType.Continue, segment3.getType());
    assertExactMemes(Lists.newArrayList("Wild", "Cozy", "Pessimism", "Outlook", "Tropical"), segment5.getMemes());
  }

  /**
   Craft a segment in the test

   @param offset to craft at
   @param from   time
   @return crafted segment
   @throws Exception on failure
   */
  private Segment craftSegment(int id, int chainId, int offset, Instant from) throws Exception {
    insert(newSegment(id, chainId, offset, from));
    injector.getInstance(SegmentDAO.class).updateState(Access.internal(), BigInteger.valueOf(id), SegmentState.Crafting);
    Segment craftingSegment = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), BigInteger.valueOf(id));
    Fabricator fabricator = fabricatorFactory.fabricate(craftingSegment);
    craftFactory.macroMain(fabricator).doWork();
    injector.getInstance(SegmentDAO.class).updateState(Access.internal(), BigInteger.valueOf(id), SegmentState.Crafted);
    return injector.getInstance(SegmentDAO.class).readOne(Access.internal(), BigInteger.valueOf(id));
  }

}
