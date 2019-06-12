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

public class DubMasterNextMainIT extends FixtureIT {
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
    insertFixtureB2();
    insertFixtureB_Instruments();

    // Chain "Test Print #1" has 5 total segments
    insert(newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now(), newChainBinding(library2)));
    insert(newSegment(1, 1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    insert(newSegment(2, 1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

    // Chain "Test Print #1" has this segment that was just dubbed
    segment3 = newSegment(3, 1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "F Major", 64, 0.30, 120.0, "chains-1-segments-9f7s89d8a7892.wav");
    segment3.add(newChoice(ProgramType.Macro, 4, program4_binding0.getId(), 3));
    segment3.add(newChoice(ProgramType.Main, 5, program5_binding1.getId(), -4));
    insert(segment3);

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    segment4 = newSegment(4, 1, 3, SegmentState.Dubbing, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "G minor", 16, 0.45, 120.0, "chains-1-segments-9f7s89d8a7892.wav");
    segment4.add(newChoice(ProgramType.Macro, 4, program4_binding1.getId(), 3));
    segment4.add(newChoice(ProgramType.Main, 5, program15_binding0.getId(), -5));
    Choice choice1 = segment4.add(newChoice(ProgramType.Rhythm, 35, null, -5));
    segment4.add(newSegmentMeme("Regret"));
    segment4.add(newSegmentMeme("Sky"));
    segment4.add(newSegmentMeme("Hindsight"));
    segment4.add(newSegmentMeme("Tropical"));
    segment4.add(newSegmentChord(0.0, "G minor"));
    segment4.add(newSegmentChord(8.0, "Ab minor"));
    segment4.add(newArrangement(choice1, voiceDrums, instrument201));
    insert(segment4);

    // future: insert arrangement of choice
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2
  }

  @Test
  public void dubMasterNextMain() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(segment4);

    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

}
