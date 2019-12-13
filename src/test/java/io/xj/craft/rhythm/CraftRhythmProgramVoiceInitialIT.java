// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.core.CoreModule;
import io.xj.core.IntegrationTestingFixtures;
import io.xj.core.access.Access;
import io.xj.core.app.AppConfiguration;
import io.xj.core.dao.DAO;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.dao.SegmentChoiceArrangementDAO;
import io.xj.core.dao.SegmentChoiceArrangementPickDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangementPick;
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

import static io.xj.core.Tables.SEGMENT_CHOICE_ARRANGEMENT;
import static io.xj.core.Tables.SEGMENT_CHOICE_ARRANGEMENT_PICK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CraftRhythmProgramVoiceInitialIT {
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
    fake.insertFixtureB3();

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    fake.chain2 = test.insert(Chain.create(fake.account1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fake.chain2, fake.library2));
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void craftRhythmVoiceInitial() throws Exception {
    insertSegments6();
    // force known rhythm selection by destroying program 35
    injector.getInstance(ProgramDAO.class).destroyChildEntities(Access.internal(), ImmutableList.of(fake.program35.getId()));
    injector.getInstance(ProgramDAO.class).destroy(Access.internal(), fake.program35.getId());
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fake.segment6);

    craftFactory.rhythm(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), fake.segment6.getId());
    assertTrue(0 < test.getDSL()
      .selectCount().from(SEGMENT_CHOICE_ARRANGEMENT)
      .where(SEGMENT_CHOICE_ARRANGEMENT.SEGMENT_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedBleep = 0;
    int pickedToot = 0;
    Collection<SegmentChoiceArrangementPick> picks = DAO.modelsFrom(
      SegmentChoiceArrangementPick.class,
      test.getDSL()
        .selectFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_ID.eq(result.getId()))
        .fetch());
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fake.audio8kick.getId()))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(fake.audio8snare.getId()))
        pickedSnare++;
      if (pick.getInstrumentAudioId().equals(fake.audio8bleep.getId()))
        pickedBleep++;
      if (pick.getInstrumentAudioId().equals(fake.audio8toot.getId()))
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
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fake.segment6);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segment 6, including the rhythm choice only if specified
   */
  private void insertSegments6() throws CoreException {
    // segment crafting
    fake.segment6 = test.insert(Segment.create()
      .setChainId(fake.chain2.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:07.384616Z")
      .setKey("D Major")
      .setTotal(32)
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

}
