// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.craft.macro;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.core.CoreModule;
import io.xj.lib.core.IntegrationTestingFixtures;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppConfiguration;
import io.xj.lib.core.dao.SegmentChoiceDAO;
import io.xj.lib.core.dao.SegmentChordDAO;
import io.xj.lib.core.dao.SegmentDAO;
import io.xj.lib.core.dao.SegmentMemeDAO;
import io.xj.lib.core.fabricator.Fabricator;
import io.xj.lib.core.fabricator.FabricatorFactory;
import io.xj.lib.core.fabricator.FabricatorType;
import io.xj.lib.core.model.Chain;
import io.xj.lib.core.model.ChainBinding;
import io.xj.lib.core.model.ChainState;
import io.xj.lib.core.model.ChainType;
import io.xj.lib.core.model.ProgramType;
import io.xj.lib.core.model.Segment;
import io.xj.lib.core.model.SegmentChoice;
import io.xj.lib.core.model.SegmentState;
import io.xj.lib.core.testing.AppTestConfiguration;
import io.xj.lib.core.testing.IntegrationTestProvider;
import io.xj.lib.craft.CraftFactory;
import io.xj.lib.craft.CraftModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;

import static io.xj.lib.core.testing.Assert.assertExactChords;
import static io.xj.lib.core.testing.Assert.assertExactMemes;
import static org.junit.Assert.assertEquals;

public class CraftFoundationInitialIT {
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

    // Chain "Print #2" has 1 initial planned segment
    fake.chain2 = test.insert(Chain.create(fake.account1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fake.chain2, fake.library2));
    fake.segment6 = test.insert(Segment.create(fake.chain2, 0L, SegmentState.Planned, Instant.parse("2017-02-14T12:01:00.000001Z"), null, "C", 8, 0.8, 120, "chain-1-waveform-12345.wav"));
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void craftFoundationInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fake.segment6);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOneAtChainOffset(Access.internal(), fake.chain2.getId(), 0L);
    assertEquals(fake.segment6.getId(), result.getId());
    assertEquals(FabricatorType.Initial, result.getType());
    assertEquals("2017-02-14T12:01:07.384616Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(16), result.getTotal());
    assertEquals(0.55, result.getDensity(), 0.01);
    assertEquals("G major", result.getKey());
    assertEquals(130.0, result.getTempo(), 0.01);
    // assert memes
    assertExactMemes(Lists.newArrayList("Tropical", "Wild", "Outlook", "Optimism"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(fake.segment6.getId())));
    // assert chords
    assertExactChords(Lists.newArrayList("G major", "Ab minor"),
      injector.getInstance(SegmentChordDAO.class).readMany(Access.internal(), ImmutableList.of(fake.segment6.getId())));
    // assert choices
    Collection<SegmentChoice> segmentChoices = injector.getInstance(SegmentChoiceDAO.class)
      .readMany(Access.internal(), ImmutableList.of(fake.segment6.getId()));
    SegmentChoice macroChoice = SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Macro);
    assertEquals(fake.program4_binding0.getId(), macroChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), macroChoice.getTranspose());
    assertEquals(Long.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    SegmentChoice mainChoice = SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Main);
    assertEquals(fake.program5_binding0.getId(), mainChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(0), mainChoice.getTranspose());
    assertEquals(Long.valueOf(0), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }
}
