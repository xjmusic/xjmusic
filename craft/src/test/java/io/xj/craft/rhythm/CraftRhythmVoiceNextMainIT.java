// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.Choice;
import io.xj.core.model.segment.sub.Pick;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentState;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.program.ProgramType;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Instant;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CraftRhythmVoiceNextMainIT extends FixtureIT {
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
    instrument1.add(newAudioEvent(audioSnare, 1, 1, "SNARE", "Ab", 1.0));
    //
    insert(instrument1);
  }

  @Test
  public void craftRhythmVoiceNextMain() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(segment4);

    craftFactory.rhythm(fabricator).doWork();

    assertNotNull(segment4.getArrangementsForChoice(segment4.getChoiceOfType(ProgramType.Rhythm)));

    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    for (Pick pick : segment4.getPicks()) {
      if (pick.getAudioId().equals(audioKick.getId()))
        pickedKick++;
      if (pick.getAudioId().equals(audioSnare.getId()))
        pickedSnare++;
    }
    assertEquals(8, pickedKick);
    assertEquals(8, pickedSnare);
  }

  @Test
  public void craftRhythmVoiceNextMain_okIfNoRhythmChoice() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(segment4);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segments 3 and 4, including the rhythm choice for segment 3 only if specified

   @param excludeRhythmChoiceForSegment3 if desired for the purpose of this test
   */
  private void insertSegments3and4(boolean excludeRhythmChoiceForSegment3) throws CoreException {
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
      .setProgramId(BigInteger.valueOf(15))
      .setSequenceBindingId(program15_binding1.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-4));
    if (!excludeRhythmChoiceForSegment3)
      segment3.add(new Choice()
        .setProgramId(BigInteger.valueOf(35))
        .setTypeEnum(ProgramType.Rhythm)
        .setTranspose(-5));
    insert(segment3);

    // segment crafting
    segment4 = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("G minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav");
    segment4.add(new Choice()
      .setProgramId(BigInteger.valueOf(4))
      .setSequenceBindingId(program4_binding1.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(3));
    segment4.add(new Choice()
      .setProgramId(BigInteger.valueOf(15))
      .setSequenceBindingId(program15_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(0));
    ImmutableList.of("Regret", "Sky", "Hindsight", "Tropical").forEach(memeName -> {
      segment4.add(newSegmentMeme(memeName));
    });
    segment4.add(newSegmentChord(0.0, "G minor"));
    segment4.add(newSegmentChord(8.0, "Ab minor"));
    insert(segment4);
  }

}
