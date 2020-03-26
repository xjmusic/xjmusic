// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.rest_api.RestApiException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.IntegrationTestingFixtures;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.ChainType;
import io.xj.service.hub.model.Instrument;
import io.xj.service.hub.model.InstrumentAudio;
import io.xj.service.hub.model.InstrumentAudioEvent;
import io.xj.service.hub.model.InstrumentMeme;
import io.xj.service.hub.model.InstrumentState;
import io.xj.service.hub.model.InstrumentType;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.model.SegmentMeme;
import io.xj.service.hub.model.SegmentState;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.IntegrationTestModule;
import io.xj.service.hub.testing.IntegrationTestProvider;
import io.xj.service.nexus.craft.CraftFactory;
import io.xj.service.nexus.craft.CraftModule;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import io.xj.service.nexus.fabricator.FabricatorModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;

import static io.xj.service.hub.Tables.SEGMENT_CHOICE_ARRANGEMENT_PICK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CraftRhythmProgramVoiceNextMainIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  private IntegrationTestingFixtures fake;
  private IntegrationTestProvider test;
  private ProgramDAO programDAO;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(new HubModule(), new CraftModule(), new FabricatorModule(), new IntegrationTestModule()));
    programDAO = injector.getInstance(ProgramDAO.class);
    test = injector.getInstance(IntegrationTestProvider.class);
    fake = new IntegrationTestingFixtures(test);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    test.reset();
    fake.insertFixtureB1();
    fake.insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    fake.chain1 = test.insert(Chain.create(fake.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fake.chain1, fake.library2));
    fake.segment1 = test.insert(Segment.create(fake.chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    fake.segment2 = test.insert(Segment.create(fake.chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

    // Instrument "808"
    fake.instrument1 = test.insert(Instrument.create(fake.user3, fake.library2, InstrumentType.Percussive, InstrumentState.Published, "808 Drums"));
    test.insert(InstrumentMeme.create(fake.instrument1, "heavy"));
    //
    fake.audioKick = test.insert(InstrumentAudio.create(fake.instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, 0.6));
    test.insert(InstrumentAudioEvent.create(fake.audioKick, 0, 1, "KICK", "Eb", 1.0));
    //
    fake.audioSnare = test.insert(InstrumentAudio.create(fake.instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01, 1.5, 120.0, 1200, 0.6));
    test.insert(InstrumentAudioEvent.create(fake.audioSnare, 1, 1, "SNARE", "Ab", 1.0));
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void craftRhythmVoiceNextMain() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fake.segment4);

    craftFactory.rhythm(fabricator).doWork();

    assertNotNull(fabricator.getArrangements(fabricator.getCurrentRhythmChoice()));

    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    Collection<SegmentChoiceArrangementPick> picks = programDAO.modelsFrom(
      SegmentChoiceArrangementPick.class,
      test.getDSL()
        .selectFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_ID.eq(fake.segment4.getId()))
        .fetch());
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fake.audioKick.getId()))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(fake.audioSnare.getId()))
        pickedSnare++;
    }
    assertEquals(8, pickedKick);
    assertEquals(8, pickedSnare);
  }

  @Test
  public void craftRhythmVoiceNextMain_okIfNoRhythmChoice() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fake.segment4);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segments 3 and 4, including the rhythm choice for segment 3 only if specified

   @param excludeRhythmChoiceForSegment3 if desired for the purpose of this test
   */
  private void insertSegments3and4(boolean excludeRhythmChoiceForSegment3) throws HubException, RestApiException {
    // segment just crafted
    // Testing entities for reference
    fake.segment3 = test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create().setSegmentId(fake.segment3.getId())
      .setProgramId(fake.program4.getId())
      .setProgramSequenceBindingId(fake.program4_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(3));
    test.insert(SegmentChoice.create().setSegmentId(fake.segment3.getId())
      .setProgramId(fake.program15.getId())
      .setProgramSequenceBindingId(fake.program15_binding1.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-4));
    if (!excludeRhythmChoiceForSegment3)
      test.insert(SegmentChoice.create().setSegmentId(fake.segment3.getId())
        .setProgramId(fake.program35.getId())
        .setTypeEnum(ProgramType.Rhythm)
        .setTranspose(-5));

    // segment crafting
    fake.segment4 = test.insert(Segment.create()
      .setChainId(fake.chain1.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("G minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create().setSegmentId(fake.segment4.getId())
      .setProgramId(fake.program4.getId())
      .setProgramSequenceBindingId(fake.program4_binding1.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(3));
    test.insert(SegmentChoice.create().setSegmentId(fake.segment4.getId())
      .setProgramId(fake.program15.getId())
      .setProgramSequenceBindingId(fake.program15_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(0));
    for (String memeName : ImmutableList.of("Regret", "Sky", "Hindsight", "Tropical")) {
      test.insert(SegmentMeme.create(fake.segment4, memeName));
    }
    test.insert(SegmentChord.create(fake.segment4, 0.0, "G minor"));
    test.insert(SegmentChord.create(fake.segment4, 8.0, "Ab minor"));
  }

}
