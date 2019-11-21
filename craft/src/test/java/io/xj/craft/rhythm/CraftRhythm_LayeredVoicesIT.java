// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.dao.SegmentChoiceArrangementDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SegmentChoiceArrangementPickDAO;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.Instrument;
import io.xj.core.model.InstrumentAudio;
import io.xj.core.model.InstrumentAudioEvent;
import io.xj.core.model.InstrumentMeme;
import io.xj.core.model.InstrumentState;
import io.xj.core.model.InstrumentType;
import io.xj.core.model.Program;
import io.xj.core.model.ProgramSequencePatternEvent;
import io.xj.core.model.ProgramMeme;
import io.xj.core.model.ProgramSequencePattern;
import io.xj.core.model.ProgramPatternType;
import io.xj.core.model.ProgramSequence;
import io.xj.core.model.ProgramState;
import io.xj.core.model.ProgramVoiceTrack;
import io.xj.core.model.ProgramType;
import io.xj.core.model.ProgramVoice;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentChoiceArrangementPick;
import io.xj.core.model.SegmentChord;
import io.xj.core.model.SegmentMeme;
import io.xj.core.model.SegmentState;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 [#166481918] Rhythm fabrication composited of layered Patterns
 */
public class CraftRhythm_LayeredVoicesIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(new CoreModule(), new CraftModule());
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    reset();
    insertFixtureB1();
    insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    chain1 = insert(Chain.create(account1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null));
    insert(ChainBinding.create(chain1, library2));
    segment1 = insert(Segment.create(chain1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    segment2 = insert(Segment.create(chain1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

    // Instrument "808"
    Instrument instrument1 = insert(Instrument.create(user3, library2, InstrumentType.Percussive, InstrumentState.Published, "808 Drums"));
    insert(InstrumentMeme.create(instrument1, "heavy"));
    //
    audioKick = insert(InstrumentAudio.create(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, 0.6));
    insert(InstrumentAudioEvent.create(audioKick, 0, 1, "KICK", "Eb", 1.0));
    //
    audioSnare = insert(InstrumentAudio.create(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01, 1.5, 120.0, 1200, 0.6));
    insert(InstrumentAudioEvent.create(audioSnare, 0, 1, "SNARE", "Ab", 1.0));
    //
    audioHihat = insert(InstrumentAudio.create(instrument1, "Hihat", "iop0803k1k2l3h5a3s2d3f4g.wav", 0.01, 1.5, 120.0, 1200, 0.6));
    insert(InstrumentAudioEvent.create(audioHihat, 0, 1, "HIHAT", "Ab", 1.0));
    // Remove fixture rhythm program and build a new one that includes layered voices
    injector.getInstance(ProgramDAO.class).destroyChildEntities(internal, ImmutableList.of(program35.getId()));
    injector.getInstance(ProgramDAO.class).destroy(internal, program35.getId());
    // A basic beat
    program35 = insert(Program.create(user3, library2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, 0.6));
    insert(ProgramMeme.create(program35, "Basic"));
    ProgramVoice locomotion = insert(ProgramVoice.create(program35, InstrumentType.Percussive, "Locomotion"));
    ProgramVoice kickSnare = insert(ProgramVoice.create(program35, InstrumentType.Percussive, "BoomBap"));
    ProgramSequence sequence35a = insert(ProgramSequence.create(program35, 16, "Base", 0.5, "C", 110.3));
    //
    ProgramSequencePattern pattern35a1 = insert(ProgramSequencePattern.create(sequence35a, locomotion, ProgramPatternType.Loop, 1, "Hi-hat"));
    ProgramVoiceTrack trackHihat = insert(ProgramVoiceTrack.create(locomotion, "HIHAT"));
    insert(ProgramSequencePatternEvent.create(pattern35a1, trackHihat, 0.0, 1.0, "C2", 1.0));
    insert(ProgramSequencePatternEvent.create(pattern35a1, trackHihat, 0.25, 1.0, "G5", 0.4));
    insert(ProgramSequencePatternEvent.create(pattern35a1, trackHihat, 0.5, 1.0, "C2", 0.6));
    insert(ProgramSequencePatternEvent.create(pattern35a1, trackHihat, 0.75, 1.0, "C2", 0.3));
    //
    ProgramSequencePattern pattern35a2 = insert(ProgramSequencePattern.create(sequence35a, kickSnare, ProgramPatternType.Loop, 4, "Kick/Snare"));
    ProgramVoiceTrack trackKick = insert(ProgramVoiceTrack.create(kickSnare, "KICK"));
    ProgramVoiceTrack trackSnare = insert(ProgramVoiceTrack.create(kickSnare, "SNARE"));
    insert(ProgramSequencePatternEvent.create(pattern35a2, trackKick, 0.0, 1.0, "B5", 0.9));
    insert(ProgramSequencePatternEvent.create(pattern35a2, trackSnare, 1.0, 1.0, "D2", 1.0));
    insert(ProgramSequencePatternEvent.create(pattern35a2, trackKick, 2.5, 1.0, "E4", 0.7));
    insert(ProgramSequencePatternEvent.create(pattern35a2, trackSnare, 3.0, 1.0, "c3", 0.5));

    // segment just crafted
    // Testing entities for reference
    segment3 = insert(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create(segment3, ProgramType.Macro, program4_binding0, 3));
    insert(SegmentChoice.create(segment3, ProgramType.Main, program5_binding0, 5));
    insert(SegmentChoice.create(segment3, ProgramType.Rhythm, program35, 5));

    // segment crafting
    segment4 = insert(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("D Major")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create(segment4, ProgramType.Macro, program4_binding0, 3));
    insert(SegmentChoice.create(segment4, ProgramType.Main, program5_binding1, -5));

    for (String memeName : ImmutableList.of("Cozy", "Classic", "Outlook", "Rosy"))
      insert(SegmentMeme.create(segment4, memeName));

    insert(SegmentChord.create(segment4, 0.0, "A minor"));
    insert(SegmentChord.create(segment4, 8.0, "D Major"));
  }

  @Test
  public void craftRhythmVoiceContinue() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segment4);

    craftFactory.rhythm(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), segment4.getId());
    assertTrue(0 < injector.getInstance(SegmentChoiceArrangementDAO.class).readMany(Access.internal(), ImmutableList.of(result.getId())).size());

    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedHihat = 0;
    Collection<SegmentChoiceArrangementPick> picks = injector.getInstance(SegmentChoiceArrangementPickDAO.class).readMany(Access.internal(), ImmutableList.of(result.getId()));
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(audioKick.getId()))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(audioSnare.getId()))
        pickedSnare++;
      if (pick.getInstrumentAudioId().equals(audioHihat.getId()))
        pickedHihat++;
    }
    assertEquals(8, pickedKick);
    assertEquals(8, pickedSnare);
    assertEquals(64, pickedHihat);
  }


}
