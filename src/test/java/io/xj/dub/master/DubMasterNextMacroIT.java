// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangement;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentState;
import io.xj.core.testing.AppTestConfiguration;
import io.xj.core.testing.IntegrationTestProvider;
import io.xj.craft.CraftModule;
import io.xj.dub.DubFactory;
import io.xj.dub.DubModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;

public class DubMasterNextMacroIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private DubFactory dubFactory;
  private FabricatorFactory fabricatorFactory;

  private IntegrationTestingFixtures fake;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule(), new CraftModule(), new DubModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    dubFactory = injector.getInstance(DubFactory.class);

    // Fixtures
    test.reset();
    fake.insertFixtureB1();
    fake.insertFixtureB2();
    fake.insertFixtureB_Instruments();

    // Chain "Test Print #1" has 5 total segments
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fake.chain1, fake.library2));
    fake.segment1 = test.insert(Segment.create(fake.chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    fake.segment2 = test.insert(Segment.create(fake.chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

    // Chain "Test Print #1" has this segment that was just dubbed
    fake.segment3 = test.insert(Segment.create(fake.chain1, 2, SegmentState.Dubbed, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "Ab minor", 64, 0.30, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create(fake.segment3, ProgramType.Macro, fake.program4_binding1, 3));
    test.insert(SegmentChoice.create(fake.segment3, ProgramType.Main, fake.program5_binding1, -4));

    // Chain "Test Print #1" has this segment dubbing - Structure is complete
    fake.segment4 = test.insert(Segment.create(fake.chain1, 3, SegmentState.Dubbing, Instant.parse("2017-02-14T12:03:08.000001Z"), Instant.parse("2017-02-14T12:03:15.836735Z"), "F minor", 16, 0.45, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create(fake.segment4, ProgramType.Macro, fake.program3_binding0, 4));
    test.insert(SegmentChoice.create(fake.segment4, ProgramType.Main, fake.program15_binding0, -2));
    SegmentChoice choice1 = test.insert(SegmentChoice.create(fake.segment4, ProgramType.Rhythm, fake.program35, 5));
    test.insert(SegmentMeme.create(fake.segment4, "Hindsight"));
    test.insert(SegmentMeme.create(fake.segment4, "Chunky"));
    test.insert(SegmentMeme.create(fake.segment4, "Regret"));
    test.insert(SegmentMeme.create(fake.segment4, "Tangy"));
    test.insert(SegmentChord.create(fake.segment4, 0.0, "F minor"));
    test.insert(SegmentChord.create(fake.segment4, 8.0, "Gb minor"));
    test.insert(SegmentChoiceArrangement.create(choice1, fake.programVoice3, fake.instrument201));

    // future: insert arrangement of choice
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2
  }

  @After
  public void tearDown() {
    test.shutdown();
  }


  @Test
  public void dubMasterNextMacro() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fake.segment4);

    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

}
