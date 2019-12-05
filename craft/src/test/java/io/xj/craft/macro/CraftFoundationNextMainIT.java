// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.dao.SegmentChoiceDAO;
import io.xj.core.dao.SegmentChordDAO;
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
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import io.xj.craft.exception.CraftException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;

import static io.xj.core.testing.Assert.assertExactChords;
import static io.xj.core.testing.Assert.assertExactMemes;
import static org.junit.Assert.assertEquals;

public class CraftFoundationNextMainIT {
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
    injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule(), new CraftModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    test.reset();
    fake.insertFixtureB1();
    fake.insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fake.chain1, fake.library2));
    fake.segment1 = test.insert(Segment.create(fake.chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    fake.segment2 = test.insert(Segment.create(fake.chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

    // Chain "Test Print #1" has this segment that was just crafted
    fake.segment3 = test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create(fake.segment3, ProgramType.Macro, fake.program4_binding0, 3));
    test.insert(SegmentChoice.create(fake.segment3, ProgramType.Main, fake.program5_binding1, -4));

    // Chain "Test Print #1" has a planned segment
    fake.segment4 = test.insert(Segment.create(fake.chain1, 3L, SegmentState.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"), null, "C", 8, 0.8, 120, "chain-1-waveform-12345.wav"));
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void craftFoundationNextMain() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fake.segment4);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOneAtChainOffset(Access.internal(), fake.chain1.getId(), 3L);
    assertEquals(FabricatorType.NextMain, result.getType());
    assertEquals("2017-02-14T12:03:15.840157Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(16), result.getTotal());
    assertEquals(Double.valueOf(0.45), result.getDensity());
    assertEquals("G minor", result.getKey());
    assertEquals(Double.valueOf(125), result.getTempo());
    // assert memes
    assertExactMemes(Lists.newArrayList("Hindsight", "Tropical", "Cozy", "Wild", "Regret"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(result.getId())));
    // assert chords
    assertExactChords(Lists.newArrayList("G minor", "Ab minor"),
      injector.getInstance(SegmentChordDAO.class).readMany(Access.internal(), ImmutableList.of(result.getId())));
    // assert choices
    Collection<SegmentChoice> segmentChoices = injector.getInstance(SegmentChoiceDAO.class)
      .readMany(Access.internal(), ImmutableList.of(result.getId()));
    // assert macro choice
    SegmentChoice macroChoice = SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Macro);
    assertEquals(fake.program4_binding1.getId(), macroChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(3), macroChoice.getTranspose());
    assertEquals(Long.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    // assert main choice
    SegmentChoice mainChoice = SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Main);
    assertEquals(fake.program15_binding0.getId(), mainChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), mainChoice.getTranspose());
    assertEquals(Long.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }

  /**
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance
   */
  @Test
  public void craftFoundationNextMain_revertsAndRequeuesOnFailure() throws Exception {
    // Chain "Test Print #1" has a dangling (preceded by another planned segment) planned segment
    fake.segment5 = test.insert(Segment.create(fake.chain1, 4L, SegmentState.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"), null, "C", 8, 0.8, 120, "chain-1-waveform-12345.wav"));
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fake.segment5);

    failure.expect(CraftException.class);

    craftFactory.macroMain(fabricator).doWork();
  }

}
