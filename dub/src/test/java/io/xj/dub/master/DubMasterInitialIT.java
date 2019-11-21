// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentState;
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
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    dubFactory = injector.getInstance(DubFactory.class);

    // Fixtures
    reset();
    insertFixtureB1();
    insertFixtureB_Instruments();

    // Chain "Print #2" has 1 initial segment in dubbing state - Master is complete
    chain2 = insert(Chain.create(account1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    insert(ChainBinding.create(chain2, library2));

    segment6 = insert(Segment.create(chain2, 0, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:07.384616Z"), "C minor", 16, 0.55, 130, "chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create(segment6, ProgramType.Macro, program4_binding0, 0));
    insert(SegmentChoice.create(segment6, ProgramType.Main, program5_binding0, 0));
    SegmentChoice choice1 = insert(SegmentChoice.create(segment6, ProgramType.Rhythm, program35, 0));
    insert(SegmentMeme.create(segment6, "Special"));
    insert(SegmentMeme.create(segment6, "Wild"));
    insert(SegmentMeme.create(segment6, "Pessimism"));
    insert(SegmentMeme.create(segment6, "Outlook"));
    insert(SegmentChord.create(segment6, 0.0, "A minor"));
    insert(SegmentChord.create(segment6, 8.0, "D major"));
    insert(SegmentChoiceArrangement.create(choice1, voiceDrums, instrument201));

    // future: insert arrangement of choice1
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2
  }

  @Test
  public void dubMasterInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segment6);

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
