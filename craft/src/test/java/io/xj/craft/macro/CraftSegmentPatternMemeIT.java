//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
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

import static io.xj.core.Assert.assertExactMemes;

public class CraftSegmentPatternMemeIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  @Before
  public void setUp() throws Exception {
    craftFactory = injector.getInstance(CraftFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    SegmentFactory segmentFactory = injector.getInstance(SegmentFactory.class);

    IntegrationTestEntity.reset();
    insertLibraryB1();
    insertLibraryB2();

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);

    // Chain "Test Print #1" has this segment that was just crafted
    Segment seg3 = segmentFactory.newSegment(BigInteger.valueOf(1))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(1))
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chain-1-segment-9f7s89d8a7892.wav");
    seg3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(1))
      .setSequencePatternId(BigInteger.valueOf(441))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(3));
    seg3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(1))
      .setSequencePatternId(BigInteger.valueOf(1651))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(5));
    IntegrationTestEntity.insert(seg3);

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2);
  }

  /**
   [#165803886] Segment memes expected to be taken directly from sequence_pattern binding
   */
  @Test
  public void craftSegmentMemesDirectlyFromSequencePatternBinding() throws Exception {
    Segment seg2 = craftSegment(2, 1, 2, Instant.parse("2017-02-14T12:02:36.000001Z"));
    Segment seg3 = craftSegment(3, 1, 3, seg2.getEndAt());
    Segment seg4 = craftSegment(4, 1, 4, seg3.getEndAt());
    Segment seg5 = craftSegment(5, 1, 5, seg4.getEndAt());

    assertExactMemes(Lists.newArrayList("Regret", "Wild", "Hindsight", "Tropical"), seg2.getMemes());
    assertExactMemes(Lists.newArrayList("Wild", "Hindsight", "Pride", "Shame", "Tropical"), seg3.getMemes());
    assertExactMemes(Lists.newArrayList("Wild", "Cozy", "Optimism", "Outlook", "Tropical"), seg4.getMemes());
    assertExactMemes(Lists.newArrayList("Wild", "Cozy", "Pessimism", "Outlook", "Tropical"), seg5.getMemes());
  }

  /**
   Craft a segment in the test

   @param offset to craft at
   @param from   time
   @return crafted segment
   @throws Exception on failure
   */
  private Segment craftSegment(int id, int chainId, int offset, Instant from) throws Exception {
    IntegrationTestEntity.insertSegment_Planned(id, chainId, offset, from);
    injector.getInstance(SegmentDAO.class).updateState(Access.internal(), BigInteger.valueOf(id), SegmentState.Crafting);
    Segment craftingSegment = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), BigInteger.valueOf(id));
    Fabricator fabricator = fabricatorFactory.fabricate(craftingSegment);
    craftFactory.macroMain(fabricator).doWork();
    injector.getInstance(SegmentDAO.class).updateState(Access.internal(), BigInteger.valueOf(id), SegmentState.Crafted);
    return injector.getInstance(SegmentDAO.class).readOne(Access.internal(), BigInteger.valueOf(id));
  }

}
