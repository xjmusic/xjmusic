// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.dub.master;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.xj.core.CoreModule;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.instrument.InstrumentType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.sequence.SequenceType;
import io.xj.craft.CraftModule;
import io.xj.dub.BaseIT;
import io.xj.dub.DubFactory;
import io.xj.dub.DubModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;

public class DubMasterInitialIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule(), new DubModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private DubFactory dubFactory;
  private FabricatorFactory fabricatorFactory;
  private SegmentFactory segmentFactory;

  // Testing entities for reference
  private Segment segment6;

  @Before
  public void setUp() throws Exception {
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    dubFactory = injector.getInstance(DubFactory.class);

    // Fixtures
    IntegrationTestEntity.reset();
    insertLibraryA();

    // Chain "Print #2" has 1 initial segment in dubbing state - Master is complete
    IntegrationTestEntity.insertChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Timestamp.valueOf("2014-08-12 12:17:02.527142"), null, null);

    segment6 = segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(2))
      .setOffset(BigInteger.valueOf(0))
      .setStateEnum(SegmentState.Dubbing)
      .setBeginAt("2017-02-14 12:01:00.000001")
      .setEndAt("2017-02-14 12:01:07.384616")
      .setKey("C minor")
      .setTotal(16)
      .setDensity(0.55)
      .setTempo(130.0)
      .setWaveformKey("chain-1-segment-9f7s89d8a7892.wav");
    segment6.add(new Choice()
      .setSegmentId(BigInteger.valueOf(6))
      .setSequencePatternId(BigInteger.valueOf(340))
      .setTypeEnum(SequenceType.Macro)
      .setTranspose(0));
    segment6.add(new Choice()
      .setSegmentId(BigInteger.valueOf(6))
      .setSequencePatternId(BigInteger.valueOf(1550))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(-6));
    Choice choice1 = segment6.add(new Choice()
      .setSegmentId(BigInteger.valueOf(6))
      .setSequenceId(BigInteger.valueOf(35))
      .setTypeEnum(SequenceType.Rhythm)
      .setTranspose(0));
    ImmutableList.of("Special", "Wild", "Pessimism", "Outlook").forEach(memeName -> {
      try {
        segment6.add(new SegmentMeme()
          .setSegmentId(BigInteger.valueOf(4))
          .setName(memeName));
      } catch (CoreException ignored) {
      }
    });
    segment6.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(4))
      .setPosition(0.0)
      .setName("A minor"));
    segment6.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(4))
      .setPosition(8.0)
      .setName("D major"));
    segment6.add(new Arrangement()
      .setChoiceUuid(choice1.getUuid())
      .setVoiceId(BigInteger.valueOf(1))
      .setInstrumentId(BigInteger.valueOf(1)));
    IntegrationTestEntity.insert(segment6);

    // Instrument, InstrumentMeme
    IntegrationTestEntity.insertInstrument(1, 2, 2, "808 Drums", InstrumentType.Percussive, 0.9);
    IntegrationTestEntity.insertInstrumentMeme(1, "heavy");

    // Audio, AudioEvent
    IntegrationTestEntity.insertAudio(1, 1, "Published", "Kick", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 440);
    IntegrationTestEntity.insertAudioEvent(1, 2.5, 1, "KICK", "Eb", 0.8, 1.0);

    // Audio, AudioEvent
    IntegrationTestEntity.insertAudio(2, 1, "Published", "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01, 1.5, 120.0, 1200);
    IntegrationTestEntity.insertAudioEvent(2, 3, 1, "SNARE", "Ab", 0.1, 0.8);

    // future: insert arrangement of choice1
    // future: insert 8 picks of audio 1
    // future: insert 8 picks of audio 2

    // bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(2, 2);
  }

  @Test
  public void dubMasterInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(segment6);

    dubFactory.master(fabricator).doWork();

    // future test: success of dub master continue test
  }

/*
future:

  @Test
  public void dubMasterInitial_failsIfSegmentHasNoWaveformKey() throws Exception {
    IntegrationTestService.getDb().update(SEGMENT)
      .set(SEGMENT.WAVEFORM_KEY, DSL.value((String) null))
      .where(SEGMENT.ID.eq(BigInteger.valueOf(6)))
      .execute();

    failure.expectMessage("Segment has no waveform key!");
    failure.expect(CoreException.class);

    Fabricator basis = fabricatorFactory.fabricate(segment6);

    dubFactory.master(basis).doWork();
  }
*/

}
