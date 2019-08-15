// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
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

import static org.junit.Assert.assertNotNull;

public class CraftRhythmInitialIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(new CoreModule(), new CraftModule());
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    reset();
    insertFixtureB1();
    insertFixtureB2();
    insertFixtureB_Instruments();

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    insert(newChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now(), newChainBinding(library2)));

    // segment crafting
    segment6 = segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(2))
      .setOffset(0L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:07.384616Z")
      .setKey("C minor")
      .setTotal(16)
      .setDensity(0.55)
      .setTempo(130.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav");
    segment6.add(new Choice()
      .setProgramId(BigInteger.valueOf(4))
      .setSequenceBindingId(program4_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(0));
    segment6.add(new Choice()
      .setProgramId(BigInteger.valueOf(5))
      .setSequenceBindingId(program5_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-6));
    ImmutableList.of("Special", "Wild", "Pessimism", "Outlook").forEach(memeName -> segment6.add(newSegmentMeme(memeName)));
    segment6.add(newSegmentChord(0.0,"C minor"));
    segment6.add(newSegmentChord(8.0,"Db minor"));
    insert(segment6);
  }

  @Test
  public void craftRhythmInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(segment6);

    craftFactory.rhythm(fabricator).doWork();

    // choice of rhythm-type sequence
    assertNotNull(segment6.getChoiceOfType(ProgramType.Rhythm));
  }
}
