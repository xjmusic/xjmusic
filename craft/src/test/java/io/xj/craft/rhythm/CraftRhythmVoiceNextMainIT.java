// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.sequence.SequenceType;
import io.xj.craft.BaseIT;
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

public class CraftRhythmVoiceNextMainIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private SegmentFactory segmentFactory;

  // Testing entities for reference
  private Segment segment4;

  @Before
  public void setUp() throws Exception {
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    IntegrationTestEntity.reset();
    insertLibraryB1();
    insertLibraryB2();

    // Chain "Test Print #1" has 5 total segments
    IntegrationTestEntity.insertChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);
    IntegrationTestEntity.insertSegment_NoContent(1, 1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chain-1-segment-9f7s89d8a7892.wav");
    IntegrationTestEntity.insertSegment_NoContent(2, 1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chain-1-segment-9f7s89d8a7892.wav");

    // Instrument "808"
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, "heavy");

    // Audio "Kick"
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio "Snare"
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 3, 1, "SNARE", "Ab", 0.1, 0.8);

    // Bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(1, 2);
  }

  @Test
  public void craftRhythmVoiceNextMain() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(segment4);

    craftFactory.rhythm(fabricator).doWork();

    assertNotNull(segment4.getArrangementsForChoice(segment4.getChoiceOfType(SequenceType.Rhythm)));

    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    for (Pick pick : segment4.getPicks()) {
      if (pick.getAudioId().equals(BigInteger.valueOf(1)))
        pickedKick++;
      if (pick.getAudioId().equals(BigInteger.valueOf(2)))
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
    Segment segment3 = segmentFactory.newSegment(BigInteger.valueOf(3))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(2))
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("F Major")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chain-1-segment-9f7s89d8a7892.wav");
    segment3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequencePatternId(BigInteger.valueOf(340))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(3));
    segment3.add(new Choice()
      .setSegmentId(BigInteger.valueOf(3))
      .setSequencePatternId(BigInteger.valueOf(1651))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(-4));
    if (!excludeRhythmChoiceForSegment3)
      segment3.add(new Choice()
        .setSegmentId(BigInteger.valueOf(3))
        .setSequenceId(BigInteger.valueOf(35))
        .setTypeEnum(SequenceType.Rhythm)
        .setTranspose(-5));
    IntegrationTestEntity.insert(segment3);

    // segment crafting
    segment4 = segmentFactory.newSegment(BigInteger.valueOf(4))
      .setChainId(BigInteger.valueOf(1))
      .setOffset(BigInteger.valueOf(3))
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("G minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(120.0)
      .setWaveformKey("chain-1-segment-9f7s89d8a7892.wav");
    segment4.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(441))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(3));
    segment4.add(new Choice()
      .setSegmentId(BigInteger.valueOf(4))
      .setSequencePatternId(BigInteger.valueOf(415150))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(0));
    ImmutableList.of("Regret", "Sky", "Hindsight", "Tropical").forEach(memeName -> {
      try {
        segment4.add(new SegmentMeme()
          .setSegmentId(BigInteger.valueOf(4))
          .setName(memeName));
      } catch (CoreException ignored) {
      }
    });
    segment4.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(4))
      .setPosition(0.0)
      .setName("G minor"));
    segment4.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(4))
      .setPosition(8.0)
      .setName("Ab minor"));
    IntegrationTestEntity.insert(segment4);
  }

}
