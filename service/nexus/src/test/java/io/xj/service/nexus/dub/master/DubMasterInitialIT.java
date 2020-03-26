// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.dub.master;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.testing.IntegrationTestModule;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.ChainType;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentChoiceArrangement;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.model.SegmentMeme;
import io.xj.service.hub.model.SegmentState;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.IntegrationTestProvider;
import io.xj.service.nexus.craft.CraftModule;
import io.xj.service.nexus.dub.DubFactory;
import io.xj.service.nexus.dub.DubModule;
import io.xj.service.nexus.fabricator.FabricatorModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;

public class DubMasterInitialIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private DubFactory dubFactory;
  private FabricatorFactory fabricatorFactory;
  private IntegrationTestingFixtures fake;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubModule(), new CraftModule(), new DubModule(), new FabricatorModule(), new IntegrationTestModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);

    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    dubFactory = injector.getInstance(DubFactory.class);

    // Fixtures
    test.reset();
    fake.insertFixtureB1();
    fake.insertFixtureB_Instruments();

    // Chain "Print #2" has 1 initial segment in dubbing state - Master is complete
    fake.chain2 = test.insert(Chain.create(fake.account1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fake.chain2, fake.library2));

    fake.segment6 = test.insert(Segment.create(fake.chain2, 0, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:07.384616Z"), "C minor", 16, 0.55, 130, "chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create(fake.segment6, ProgramType.Macro, fake.program4_binding0, 0));
    test.insert(SegmentChoice.create(fake.segment6, ProgramType.Main, fake.program5_binding0, 0));
    SegmentChoice choice1 = test.insert(SegmentChoice.create(fake.segment6, ProgramType.Rhythm, fake.program35, 0));
    test.insert(SegmentMeme.create(fake.segment6, "Special"));
    test.insert(SegmentMeme.create(fake.segment6, "Wild"));
    test.insert(SegmentMeme.create(fake.segment6, "Pessimism"));
    test.insert(SegmentMeme.create(fake.segment6, "Outlook"));
    test.insert(SegmentChord.create(fake.segment6, 0.0, "A minor"));
    test.insert(SegmentChord.create(fake.segment6, 8.0, "D major"));
    test.insert(SegmentChoiceArrangement.create(choice1, fake.programVoice3, fake.instrument201));

    // future: insert arrangement of choice1
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void dubMasterInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fake.segment6);

    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

/*
future:

  @Test
  public void dubMasterInitial_failsIfSegmentHasNoWaveformKey() throws Exception {
    IntegrationTestProvider.getDSL().update(SEGMENT)
      .set(SEGMENT.WAVEFORM_KEY, DSL.value((String) null))
      .where(SEGMENT.ID.eq(BigInteger.valueOf(6)))
      .execute();

    failure.expectMessage("Segment has no waveform key!");
    failure.expect(CoreException.class);

    Fabricator basis = fabricatorFactory.fabricate(fake.segment6);

    dubFactory.master(basis).doWork();
  }
*/

}
