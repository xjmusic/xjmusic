// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.macro;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.SegmentDAO;
import io.xj.service.hub.dao.SegmentMemeDAO;
import io.xj.service.hub.testing.IntegrationTestModule;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.hub.model.SegmentType;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.ChainType;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentState;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.IntegrationTestProvider;
import io.xj.service.nexus.craft.CraftFactory;
import io.xj.service.nexus.craft.CraftModule;
import io.xj.service.nexus.fabricator.FabricatorModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;

import static io.xj.service.hub.testing.Assert.assertExactMemes;
import static org.junit.Assert.assertEquals;

public class CraftSegmentPatternMemeIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  private IntegrationTestingFixtures fake;
  private Injector injector;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableSet.of(new HubModule(), new CraftModule(), new FabricatorModule(), new IntegrationTestModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    craftFactory = injector.getInstance(CraftFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);

    test.reset();

    fake.insertFixtureB1();
    fake.insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fake.chain1, fake.library2));

    // Chain "Test Print #1" has this segment that was just crafted
    fake.segment1 = test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setOffset(1L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create(fake.segment1, ProgramType.Macro, fake.program4_binding1, 3));
    test.insert(SegmentChoice.create(fake.segment1, ProgramType.Main, fake.program5_binding1, 5));
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  /**
   [#165803886] Segment memes expected to be taken directly of sequence_pattern binding
   */
  @Test
  public void craftSegmentMemesDirectlyFromSequenceBindingBinding() throws Exception {
    fake.segment2 = craftSegment(fake.chain1, 2, Instant.parse("2017-02-14T12:02:36.000001Z"));
    fake.segment3 = craftSegment(fake.chain1, 3, fake.segment2.getEndAt());
    fake.segment4 = craftSegment(fake.chain1, 4, fake.segment3.getEndAt());
    fake.segment5 = craftSegment(fake.chain1, 5, fake.segment4.getEndAt());

    assertEquals(SegmentType.NextMacro, fake.segment2.getType());
    assertExactMemes(Lists.newArrayList("Regret", "Wild", "Hindsight", "Tropical"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(fake.segment2.getId())));

    assertEquals(SegmentType.Continue, fake.segment3.getType());
    assertExactMemes(Lists.newArrayList("Wild", "Hindsight", "Pride", "Shame", "Tropical"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(fake.segment3.getId())));

    assertEquals(SegmentType.Continue, fake.segment3.getType());
    assertExactMemes(Lists.newArrayList("Wild", "Cozy", "Optimism", "Outlook", "Tropical"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(fake.segment4.getId())));

    assertEquals(SegmentType.Continue, fake.segment3.getType());
    assertExactMemes(Lists.newArrayList("Wild", "Cozy", "Pessimism", "Outlook", "Tropical"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(fake.segment5.getId())));
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
    Segment segment = test.insert(Segment.create(chain, offset, SegmentState.Planned, from, null, "C", 8, 0.8, 120, "chain-1-waveform-12345.wav"));
    injector.getInstance(SegmentDAO.class).updateState(Access.internal(), segment.getId(), SegmentState.Crafting);
    Segment craftingSegment = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), segment.getId());
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), craftingSegment);
    craftFactory.macroMain(fabricator).doWork();
    injector.getInstance(SegmentDAO.class).updateState(Access.internal(), segment.getId(), SegmentState.Crafted);
    return injector.getInstance(SegmentDAO.class).readOne(Access.internal(), segment.getId());
  }

}
