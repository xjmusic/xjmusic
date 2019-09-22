// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ProgramDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.PatternType;
import io.xj.core.model.program.ProgramState;
import io.xj.core.model.program.ProgramType;
import io.xj.core.model.program.sub.Pattern;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.Track;
import io.xj.core.model.program.sub.Voice;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.Pick;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Instant;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 [#166481918] Rhythm fabrication composited from layered Patterns
 */
public class CraftRhythm_LayeredVoicesIT extends FixtureIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  @Before
  public void setUp() throws Exception {
    injector = Guice.createInjector(new CoreModule(), new CraftModule());
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    reset();
    insertFixtureB1();
    insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    insert(newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now(), newChainBinding(library2)));
    insert(newSegment(1, 1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    insert(newSegment(2, 1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

    // Instrument "808"
    Instrument instrument1 = newInstrument(1, 3, 2, InstrumentType.Percussive, InstrumentState.Published, "808 Drums", now());
    instrument1.add(newInstrumentMeme("heavy"));
    //
    audioKick = instrument1.add(newAudio("Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440, 0.6));
    instrument1.add(newAudioEvent(audioKick, 0, 1, "KICK", "Eb", 1.0));
    //
    audioSnare = instrument1.add(newAudio("Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01, 1.5, 120.0, 1200, 0.6));
    instrument1.add(newAudioEvent(audioSnare, 0, 1, "SNARE", "Ab", 1.0));
    //
    audioHihat = instrument1.add(newAudio("Hihat", "iop0803k1k2l3h5a3s2d3f4g.wav", 0.01, 1.5, 120.0, 1200, 0.6));
    instrument1.add(newAudioEvent(audioHihat, 0, 1, "HIHAT", "Ab", 1.0));
    //
    insert(instrument1);

    // Remove fixture rhythm program and build a new one that includes layered voices
    injector.getInstance(ProgramDAO.class).destroy(internal, BigInteger.valueOf(35));
    // A basic beat
    program35 = newProgram(35, 3, 2, ProgramType.Rhythm, ProgramState.Published, "Basic Beat", "C", 121, now());
    program35.add(newProgramMeme("Basic"));
    Voice locomotion = program35.add(newVoice(InstrumentType.Percussive, "Locomotion"));
    Voice kickSnare = program35.add(newVoice(InstrumentType.Percussive, "BoomBap"));
    Sequence sequence35a = program35.add(newSequence(16, "Base", 0.5, "C", 110.3));
    //
    Pattern pattern35a1 = program35.add(newPattern(sequence35a, locomotion, PatternType.Loop, 1, "Hi-hat"));
    Track trackHihat = program35.add(newTrack(locomotion, "HIHAT"));
    program35.add(newEvent(pattern35a1, trackHihat, 0.0, 1.0, "C2", 1.0));
    program35.add(newEvent(pattern35a1, trackHihat, 0.25, 1.0, "G5", 0.4));
    program35.add(newEvent(pattern35a1, trackHihat, 0.5, 1.0, "C2", 0.6));
    program35.add(newEvent(pattern35a1, trackHihat, 0.75, 1.0, "C2", 0.3));
    //
    Pattern pattern35a2 = program35.add(newPattern(sequence35a, kickSnare, PatternType.Loop, 4, "Kick/Snare"));
    Track trackKick = program35.add(newTrack(kickSnare, "KICK"));
    Track trackSnare = program35.add(newTrack(kickSnare, "SNARE"));
    program35.add(newEvent(pattern35a2, trackKick, 0.0, 1.0, "B5", 0.9));
    program35.add(newEvent(pattern35a2, trackSnare, 1.0, 1.0, "D2", 1.0));
    program35.add(newEvent(pattern35a2, trackKick, 2.5, 1.0, "E4", 0.7));
    program35.add(newEvent(pattern35a2, trackSnare, 3.0, 1.0, "c3", 0.5));
    //
    insert(program35);

    // segment just crafted
    // Testing entities for reference
    segment3 = segmentFactory.newSegment(BigInteger.valueOf(3))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav");
    segment3.add(new Choice()
      .setProgramId(BigInteger.valueOf(4))
      .setSequenceBindingId(program4_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(3));
    segment3.add(new Choice()
      .setProgramId(BigInteger.valueOf(5))
      .setSequenceBindingId(program5_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(5));
    segment3.add(new Choice()
      .setProgramId(BigInteger.valueOf(35))
      .setTypeEnum(ProgramType.Rhythm)
      .setTranspose(5));
    insert(segment3);

    // segment crafting
    segment4 = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("D Major")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav");
    segment4.add(new Choice()
      .setProgramId(BigInteger.valueOf(4))
      .setSequenceBindingId(program4_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(3));
    segment4.add(new Choice()
      .setProgramId(BigInteger.valueOf(5))
      .setSequenceBindingId(program5_binding1.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-5));
    ImmutableList.of("Cozy", "Classic", "Outlook", "Rosy").forEach(memeName -> segment4.add(newSegmentMeme(memeName)));
    segment4.add(newSegmentChord(0.0, "A minor"));
    segment4.add(newSegmentChord(8.0, "D Major"));
    insert(segment4);
  }

  @Test
  public void craftRhythmVoiceContinue() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(segment4);

    craftFactory.rhythm(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOne(Access.internal(), BigInteger.valueOf(4));
    assertNotNull(result.getArrangementsForChoice(result.getChoiceOfType(ProgramType.Rhythm)));
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    int pickedHihat = 0;
    Collection<Pick> picks = result.getPicks();
    for (Pick pick : picks) {
      if (pick.getAudioId().equals(audioKick.getId()))
        pickedKick++;
      if (pick.getAudioId().equals(audioSnare.getId()))
        pickedSnare++;
      if (pick.getAudioId().equals(audioHihat.getId()))
        pickedHihat++;
    }
    assertEquals(8, pickedKick);
    assertEquals(8, pickedSnare);
    assertEquals(64, pickedHihat);
  }


}
