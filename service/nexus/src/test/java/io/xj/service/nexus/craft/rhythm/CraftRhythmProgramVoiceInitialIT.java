// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.rest_api.RestApiException;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.dao.SegmentDAO;
import io.xj.service.hub.HubException;
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
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.model.SegmentMeme;
import io.xj.service.hub.model.SegmentState;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.IntegrationTestProvider;
import io.xj.service.nexus.craft.CraftFactory;
import io.xj.service.nexus.craft.CraftModule;
import io.xj.service.nexus.fabricator.FabricatorModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;

import static io.xj.service.hub.Tables.SEGMENT_CHOICE_ARRANGEMENT;
import static io.xj.service.hub.Tables.SEGMENT_CHOICE_ARRANGEMENT_PICK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CraftRhythmProgramVoiceInitialIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  private IntegrationTestingFixtures fixture;
  private Injector injector;
  private IntegrationTestProvider test;
  private ProgramDAO programDAO;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableSet.of(new HubModule(), new CraftModule(), new FabricatorModule(), new IntegrationTestModule()));
    programDAO = injector.getInstance(ProgramDAO.class);
    test = injector.getInstance(IntegrationTestProvider.class);
    fixture = new IntegrationTestingFixtures(test);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    test.reset();
    fixture.insertFixtureB1();
    fixture.insertFixtureB3();

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    fixture.chain2 = test.insert(Chain.create(fixture.account1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fixture.chain2, fixture.library2));
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void craftRhythmVoiceInitial() throws Exception {
    insertSegments6();
    // force known rhythm selection by destroying program 35
    fixture.destroyInnerEntities(fixture.program35);
    injector.getInstance(ProgramDAO.class).destroy(Access.internal(), fixture.program35.getId());
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fixture.segment6);

    craftFactory.rhythm(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), fixture.segment6.getId());
    assertTrue(0 < test.getDSL()
      .selectCount().from(SEGMENT_CHOICE_ARRANGEMENT)
      .where(SEGMENT_CHOICE_ARRANGEMENT.SEGMENT_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedBleep = 0;
    int pickedToot = 0;
    Collection<SegmentChoiceArrangementPick> picks = programDAO.modelsFrom(
      SegmentChoiceArrangementPick.class,
      test.getDSL()
        .selectFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_ID.eq(result.getId()))
        .fetch());
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fixture.audio8kick.getId()))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(fixture.audio8snare.getId()))
        pickedSnare++;
      if (pick.getInstrumentAudioId().equals(fixture.audio8bleep.getId()))
        pickedBleep++;
      if (pick.getInstrumentAudioId().equals(fixture.audio8toot.getId()))
        pickedToot++;
    }
    assertEquals(12, pickedKick);
    assertEquals(12, pickedSnare);
    assertEquals(4, pickedBleep);
    assertEquals(4, pickedToot);
  }

  @Test
  public void craftRhythmVoiceInitial_okWhenNoRhythmChoice() throws Exception {
    insertSegments6();
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fixture.segment6);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the rhythm choice only if specified
   */
  private void insertSegments6() throws HubException, RestApiException {
    // segment crafting
    fixture.segment6 = test.insert(Segment.create()
      .setChainId(fixture.chain2.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:07.384616Z")
      .setKey("D Major")
      .setTotal(32)
      .setDensity(0.55)
      .setTempo(130.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create().setSegmentId(fixture.segment6.getId())
      .setProgramId(fixture.program4.getId())
      .setProgramSequenceBindingId(fixture.program4_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(0));
    test.insert(SegmentChoice.create().setSegmentId(fixture.segment6.getId())
      .setProgramId(fixture.program5.getId())
      .setProgramSequenceBindingId(fixture.program5_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-6));
    for (String memeName : ImmutableList.of("Special", "Wild", "Pessimism", "Outlook")) {
      test.insert(SegmentMeme.create(fixture.segment6, memeName));
    }
    test.insert(SegmentChord.create(fixture.segment6, 0.0, "C minor"));
    test.insert(SegmentChord.create(fixture.segment6, 8.0, "Db minor"));
  }

}
