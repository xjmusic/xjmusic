// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
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
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;

import static org.junit.Assert.assertNotNull;

public class CraftRhythmInitialIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(new CoreModule(), new CraftModule());
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    reset();
    insertFixtureB1();
    insertFixtureB2();
    insertFixtureB_Instruments();

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = insert(Chain.create(account1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    insert(ChainBinding.create(chain2, library2));

    // segment crafting
    segment6 = insert(Segment.create()
      .setChainId(chain2.getId())
      .setOffset(0L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:07.384616Z")
      .setKey("C minor")
      .setTotal(16)
      .setDensity(0.55)
      .setTempo(130.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create().setSegmentId(segment6.getId())
      .setProgramId(program4.getId())
      .setProgramSequenceBindingId(program4_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(0));
    insert(SegmentChoice.create().setSegmentId(segment6.getId())
      .setProgramId(program5.getId())
      .setProgramSequenceBindingId(program5_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-6));
    for (String memeName : ImmutableList.of("Special", "Wild", "Pessimism", "Outlook")) {
      insert(SegmentMeme.create(segment6, memeName));
    }
    insert(SegmentChord.create(segment6, 0.0, "C minor"));
    insert(SegmentChord.create(segment6, 8.0, "Db minor"));
  }

  @Test
  public void craftRhythmInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segment6);

    craftFactory.rhythm(fabricator).doWork();

    // assert choice of rhythm-type sequence
    Collection<SegmentChoice> segmentChoices = injector.getInstance(SegmentChoiceDAO.class)
      .readMany(Access.internal(), ImmutableList.of(segment6.getId()));
    assertNotNull(SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Rhythm));
  }
}
