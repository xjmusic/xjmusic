//  Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SegmentMemeDAO;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentState;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
    reset();
    insertFixtureB1();
    insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    chain1 = insert(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    insert(ChainBinding.create(chain1, library2));

    // Chain "Test Print #1" has this segment that was just crafted
    segment1 = insert(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create(segment1, ProgramType.Macro, program4_binding1, 3));
    insert(SegmentChoice.create(segment1, ProgramType.Main, program5_binding1, 5));
  }

  /**
   [#165803886] Segment memes expected to be taken directly of sequence_pattern binding
   */
  @Test
  public void craftSegmentMemesDirectlyFromSequenceBindingBinding() throws Exception {
    segment2 = craftSegment(chain1, 2, Instant.parse("2017-02-14T12:02:36.000001Z"));
    segment3 = craftSegment(chain1, 3, segment2.getEndAt());
    segment4 = craftSegment(chain1, 4, segment3.getEndAt());
    segment5 = craftSegment(chain1, 5, segment4.getEndAt());

    assertEquals(FabricatorType.NextMacro, segment2.getType());
    assertExactMemes(Lists.newArrayList("Regret", "Wild", "Hindsight", "Tropical"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(segment2.getId())));

    assertEquals(FabricatorType.Continue, segment3.getType());
    assertExactMemes(Lists.newArrayList("Wild", "Hindsight", "Pride", "Shame", "Tropical"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(segment3.getId())));

    assertEquals(FabricatorType.Continue, segment3.getType());
    assertExactMemes(Lists.newArrayList("Wild", "Cozy", "Optimism", "Outlook", "Tropical"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(segment4.getId())));

    assertEquals(FabricatorType.Continue, segment3.getType());
    assertExactMemes(Lists.newArrayList("Wild", "Cozy", "Pessimism", "Outlook", "Tropical"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(segment5.getId())));
  }

  /**
   Craft a segment in the test

   @param chain  to craft segment in
   @param offset to craft at
   @param from   time
   @return crafted segment
   @throws Exception on failure
   */
  private Segment craftSegment(Chain chain, int offset, Instant from) throws Exception {
    Segment segment = insert(Segment.create(chain, offset, SegmentState.Planned, from, null, "C", 8, 0.8, 120, "chain-1-waveform-12345.wav"));
    injector.getInstance(SegmentDAO.class).updateState(Access.internal(), segment.getId(), SegmentState.Crafting);
    Segment craftingSegment = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), segment.getId());
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), craftingSegment);
    craftFactory.macroMain(fabricator).doWork();
    injector.getInstance(SegmentDAO.class).updateState(Access.internal(), segment.getId(), SegmentState.Crafted);
    return injector.getInstance(SegmentDAO.class).readOne(Access.internal(), segment.getId());
  }

}
