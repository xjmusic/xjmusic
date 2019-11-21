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

public class DubMasterNextMacroIT extends FixtureIT {
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
    insertFixtureB2();
    insertFixtureB_Instruments();

    // Chain "Test Print #1" has 5 total segments
    chain1 = insert(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    insert(ChainBinding.create(chain1, library2));
    segment1 = insert(Segment.create(chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    segment2 = insert(Segment.create(chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

    // Chain "Test Print #1" has this segment that was just dubbed
    segment3 = insert(Segment.create(chain1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "Ab minor", 64, 0.30, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create(segment3, ProgramType.Macro, program4_binding1, 3));
    insert(SegmentChoice.create(segment3, ProgramType.Main, program5_binding1, -4));

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    segment4 = insert(Segment.create(chain1, 3, SegmentState.Dubbing, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "F minor", 16, 0.45, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create(segment4, ProgramType.Macro, program3_binding0, 4));
    insert(SegmentChoice.create(segment4, ProgramType.Main, program15_binding0, -2));
    SegmentChoice choice1 = insert(SegmentChoice.create(segment4, ProgramType.Rhythm, program35, 5));
    insert(SegmentMeme.create(segment4, "Hindsight"));
    insert(SegmentMeme.create(segment4, "Chunky"));
    insert(SegmentMeme.create(segment4, "Regret"));
    insert(SegmentMeme.create(segment4, "Tangy"));
    insert(SegmentChord.create(segment4, 0.0, "F minor"));
    insert(SegmentChord.create(segment4, 8.0, "Gb minor"));
    insert(SegmentChoiceArrangement.create(choice1, voiceDrums, instrument201));

    // future: insert arrangement of choice
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2
  }

  @Test
  public void dubMasterNextMacro() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segment4);

    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

}
