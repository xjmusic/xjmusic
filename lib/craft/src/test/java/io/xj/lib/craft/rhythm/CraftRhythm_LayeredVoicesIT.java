// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.typesafe.config.Config;
import io.xj.lib.core.CoreModule;
import io.xj.lib.core.IntegrationTestingFixtures;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppConfiguration;
import io.xj.lib.core.dao.DAO;
import io.xj.lib.core.dao.ProgramDAO;
import io.xj.lib.core.dao.SegmentDAO;
import io.xj.lib.core.fabricator.Fabricator;
import io.xj.lib.core.fabricator.FabricatorFactory;
import io.xj.lib.core.model.Chain;
import io.xj.lib.core.model.ChainBinding;
import io.xj.lib.core.model.ChainState;
import io.xj.lib.core.model.ChainType;
import io.xj.lib.core.model.Instrument;
import io.xj.lib.core.model.InstrumentAudio;
import io.xj.lib.core.model.InstrumentAudioEvent;
import io.xj.lib.core.model.InstrumentMeme;
import io.xj.lib.core.model.InstrumentState;
import io.xj.lib.core.model.InstrumentType;
import io.xj.lib.core.model.Program;
import io.xj.lib.core.model.ProgramMeme;
import io.xj.lib.core.model.ProgramSequencePatternType;
import io.xj.lib.core.model.ProgramSequence;
import io.xj.lib.core.model.ProgramSequencePattern;
import io.xj.lib.core.model.ProgramSequencePatternEvent;
import io.xj.lib.core.model.ProgramState;
import io.xj.lib.core.model.ProgramType;
import io.xj.lib.core.model.ProgramVoice;
import io.xj.lib.core.model.ProgramVoiceTrack;
import io.xj.lib.core.model.Segment;
import io.xj.lib.core.model.SegmentChoice;
import io.xj.lib.core.model.SegmentChoiceArrangementPick;
import io.xj.lib.core.model.SegmentChord;
import io.xj.lib.core.model.SegmentMeme;
import io.xj.lib.core.model.SegmentState;
import io.xj.lib.core.testing.AppTestConfiguration;
import io.xj.lib.core.testing.IntegrationTestProvider;
import io.xj.lib.craft.CraftFactory;
import io.xj.lib.craft.CraftModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;

import static io.xj.lib.core.Tables.SEGMENT_CHOICE_ARRANGEMENT;
import static io.xj.lib.core.Tables.SEGMENT_CHOICE_ARRANGEMENT_PICK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 [#166481918] Rhythm fabrication composited of layered Patterns
 */
public class CraftRhythm_LayeredVoicesIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  private IntegrationTestingFixtures fixture;
  private Injector injector;
  private IntegrationTestProvider test;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    injector = AppConfiguration.inject(config, ImmutableList.of(new CoreModule(), new CraftModule()));
    test = injector.getInstance(IntegrationTestProvider.class);
    fixture = new IntegrationTestingFixtures(test);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    test.reset();
    fixture.insertFixtureB1();
    fixture.insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    fixture.chain1 = test.insert(Chain.create(fixture.account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    test.insert(ChainBinding.create(fixture.chain1, fixture.library2));
    fixture.segment1 = test.insert(Segment.create(fixture.chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    fixture.segment2 = test.insert(Segment.create(fixture.chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

    // Instrument "808"
    fixture.instrument1 = test.insert(Instrument.create(fixture.user3, fixture.library2, InstrumentType.Percussive, InstrumentState.Published, "808 Drums"));
    test.insert(InstrumentMeme.create(fixture.instrument1, "heavy"));
    //
    fixture.audioKick = test.insert(InstrumentAudio.create(fixture.instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, 0.6));
    test.insert(InstrumentAudioEvent.create(fixture.audioKick, 0, 1, "KICK", "Eb", 1.0));
    //
    fixture.audioSnare = test.insert(InstrumentAudio.create(fixture.instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01, 1.5, 120.0, 1200, 0.6));
    test.insert(InstrumentAudioEvent.create(fixture.audioSnare, 0, 1, "SNARE", "Ab", 1.0));
    //
    fixture.audioHihat = test.insert(InstrumentAudio.create(fixture.instrument1, "Hihat", "iop0803k1k2l3h5a3s2d3f4g.wav", 0.01, 1.5, 120.0, 1200, 0.6));
    test.insert(InstrumentAudioEvent.create(fixture.audioHihat, 0, 1, "HIHAT", "Ab", 1.0));
    // Remove fixture rhythm program and build a new one that includes layered voices
    fixture.destroyInnerEntities(fixture.program35);
    injector.getInstance(ProgramDAO.class).destroy(Access.internal(), fixture.program35.getId());
    // A basic beat
    fixture.program35 = test.insert(Program.create(fixture.user3, fixture.library2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, 0.6));
    test.insert(ProgramMeme.create(fixture.program35, "Basic"));
    ProgramVoice locomotion = test.insert(ProgramVoice.create(fixture.program35, InstrumentType.Percussive, "Locomotion"));
    ProgramVoice kickSnare = test.insert(ProgramVoice.create(fixture.program35, InstrumentType.Percussive, "BoomBap"));
    ProgramSequence sequence35a = test.insert(ProgramSequence.create(fixture.program35, 16, "Base", 0.5, "C", 110.3));
    //
    ProgramSequencePattern pattern35a1 = test.insert(ProgramSequencePattern.create(sequence35a, locomotion, ProgramSequencePatternType.Loop, 1, "Hi-hat"));
    ProgramVoiceTrack trackHihat = test.insert(ProgramVoiceTrack.create(locomotion, "HIHAT"));
    test.insert(ProgramSequencePatternEvent.create(pattern35a1, trackHihat, 0.0, 1.0, "C2", 1.0));
    test.insert(ProgramSequencePatternEvent.create(pattern35a1, trackHihat, 0.25, 1.0, "G5", 0.4));
    test.insert(ProgramSequencePatternEvent.create(pattern35a1, trackHihat, 0.5, 1.0, "C2", 0.6));
    test.insert(ProgramSequencePatternEvent.create(pattern35a1, trackHihat, 0.75, 1.0, "C2", 0.3));
    //
    ProgramSequencePattern pattern35a2 = test.insert(ProgramSequencePattern.create(sequence35a, kickSnare, ProgramSequencePatternType.Loop, 4, "Kick/Snare"));
    ProgramVoiceTrack trackKick = test.insert(ProgramVoiceTrack.create(kickSnare, "KICK"));
    ProgramVoiceTrack trackSnare = test.insert(ProgramVoiceTrack.create(kickSnare, "SNARE"));
    test.insert(ProgramSequencePatternEvent.create(pattern35a2, trackKick, 0.0, 1.0, "B5", 0.9));
    test.insert(ProgramSequencePatternEvent.create(pattern35a2, trackSnare, 1.0, 1.0, "D2", 1.0));
    test.insert(ProgramSequencePatternEvent.create(pattern35a2, trackKick, 2.5, 1.0, "E4", 0.7));
    test.insert(ProgramSequencePatternEvent.create(pattern35a2, trackSnare, 3.0, 1.0, "c3", 0.5));

    // segment just crafted
    // Testing entities for reference
    fixture.segment3 = test.insert(Segment.create()
      .setChainId(fixture.chain1.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create(fixture.segment3, ProgramType.Macro, fixture.program4_binding0, 3));
    test.insert(SegmentChoice.create(fixture.segment3, ProgramType.Main, fixture.program5_binding0, 5));
    test.insert(SegmentChoice.create(fixture.segment3, ProgramType.Rhythm, fixture.program35, 5));

    // segment crafting
    fixture.segment4 = test.insert(Segment.create()
      .setChainId(fixture.chain1.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("D Major")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    test.insert(SegmentChoice.create(fixture.segment4, ProgramType.Macro, fixture.program4_binding0, 3));
    test.insert(SegmentChoice.create(fixture.segment4, ProgramType.Main, fixture.program5_binding1, -5));

    for (String memeName : ImmutableList.of("Cozy", "Classic", "Outlook", "Rosy"))
      test.insert(SegmentMeme.create(fixture.segment4, memeName));

    test.insert(SegmentChord.create(fixture.segment4, 0.0, "A minor"));
    test.insert(SegmentChord.create(fixture.segment4, 8.0, "D Major"));
  }

  @After
  public void tearDown() {
    test.shutdown();
  }

  @Test
  public void craftRhythmVoiceContinue() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), fixture.segment4);

    craftFactory.rhythm(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), fixture.segment4.getId());
    assertTrue(0 < test.getDSL()
      .selectCount().from(SEGMENT_CHOICE_ARRANGEMENT)
      .where(SEGMENT_CHOICE_ARRANGEMENT.SEGMENT_ID.eq(result.getId()))
      .fetchOne(0, int.class));
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedHihat = 0;
    Collection<SegmentChoiceArrangementPick> picks = DAO.modelsFrom(
      SegmentChoiceArrangementPick.class,
      test.getDSL()
        .selectFrom(SEGMENT_CHOICE_ARRANGEMENT_PICK)
        .where(SEGMENT_CHOICE_ARRANGEMENT_PICK.SEGMENT_ID.eq(result.getId()))
        .fetch());
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(fixture.audioKick.getId()))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(fixture.audioSnare.getId()))
        pickedSnare++;
      if (pick.getInstrumentAudioId().equals(fixture.audioHihat.getId()))
        pickedHihat++;
    }
    assertEquals(8, pickedKick);
    assertEquals(8, pickedSnare);
    assertEquals(64, pickedHihat);
  }


}
