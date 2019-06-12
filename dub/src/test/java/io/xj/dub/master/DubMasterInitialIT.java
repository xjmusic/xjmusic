// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

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
import io.xj.craft.CraftModule;
import io.xj.dub.DubFactory;
import io.xj.dub.DubModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;

public class DubMasterInitialIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private DubFactory dubFactory;
  private FabricatorFactory fabricatorFactory;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(new CoreModule(), new CraftModule(), new DubModule());
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    dubFactory = injector.getInstance(DubFactory.class);

    // Fixtures
    reset();
    insertFixtureB1();
    insertFixtureB_Instruments();

    // Chain "Print #2" has 1 initial segment in dubbing state - Master is complete
    insert(newChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now(), newChainBinding(library2)));

    segment6 = newSegment(6, 2, 0, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:07.384616Z"), "C minor", 16, 0.55, 130, "chains-1-segments-9f7s89d8a7892.wav");
    segment6.add(newChoice(ProgramType.Macro, 4, program4_binding0.getId(), 0));
    segment6.add(newChoice(ProgramType.Main, 5, program5_binding0.getId(), 0));
    Choice choice1 = segment6.add(newChoice(ProgramType.Rhythm, 35, null, 0));
    segment6.add(newSegmentMeme("Special"));
    segment6.add(newSegmentMeme("Wild"));
    segment6.add(newSegmentMeme("Pessimism"));
    segment6.add(newSegmentMeme("Outlook"));
    segment6.add(newSegmentChord(0.0, "A minor"));
    segment6.add(newSegmentChord(8.0, "D major"));
    segment6.add(newArrangement(choice1, voiceDrums, instrument201));
    insert(segment6);

    // future: insert arrangement of choice1
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2
  }

  @Test
  public void dubMasterInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(segment6);

    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

/*
future:

  @Test
  public void dubMasterInitial_failsIfSegmentHasNoWaveformKey() throws Exception {
    IntegrationTestProvider.getDb().update(SEGMENT)
      .set(SEGMENT.WAVEFORM_KEY, DSL.value((String) null))
      .where(SEGMENT.ID.eq(BigInteger.valueOf(6)))
      .execute();

    failure.expectMessage("Segment has no waveform key!");
    failure.expect(CoreException.class);

    Fabricator basis = fabricatorFactory.fabricate(segment6);

    dubFactory.master(basis).doWork();
  }
*/

}
