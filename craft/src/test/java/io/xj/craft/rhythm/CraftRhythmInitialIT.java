// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.dao.SegmentChoiceDAO;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentState;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;

public class CraftRhythmInitialIT {
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
    fake.insertFixtureB_Instruments();

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    fake.chain2 = test.insert(Chain.create(fake.account1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fake.chain2, fake.library2));

    // segment crafting
    fake.segment6 = test.insert(Segment.create()
      .setChainId(fake.chain2.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:07.384616Z")
      .setKey("C minor")
      .setTotal(16)
      .setDensity(0.55)
      .setTempo(130.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create().setSegmentId(fake.segment6.getId())
      .setProgramId(fake.program4.getId())
      .setProgramSequenceBindingId(fake.program4_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(0));
    test.insert(SegmentChoice.create().setSegmentId(fake.segment6.getId())
      .setProgramId(fake.program5.getId())
      .setProgramSequenceBindingId(fake.program5_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-6));
    for (String memeName : ImmutableList.of("Special", "Wild", "Pessimism", "Outlook")) {
      test.insert(SegmentMeme.create(fake.segment6, memeName));
    }
    test.insert(SegmentChord.create(fake.segment6, 0.0, "C minor"));
    test.insert(SegmentChord.create(fake.segment6, 8.0, "Db minor"));
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void craftRhythmInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fake.segment6);

    craftFactory.rhythm(fabricator).doWork();

    // assert choice of rhythm-type sequence
    Collection<SegmentChoice> segmentChoices = injector.getInstance(SegmentChoiceDAO.class)
      .readMany(Access.internal(), ImmutableList.of(fake.segment6.getId()));
    assertNotNull(SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Rhythm));
  }
}
