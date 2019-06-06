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

import static org.junit.Assert.assertNotNull;

public class CraftRhythmInitialIT extends BaseIT {
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;
  private SegmentFactory segmentFactory;

  // Test subject
  private Segment segment6;

  @Before
  public void setUp() throws Exception {
    segmentFactory = injector.getInstance(SegmentFactory.class);
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    IntegrationTestEntity.reset();
    insertLibraryB1();
    insertLibraryB2();
    insertLibraryB_Instruments();

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    IntegrationTestEntity.insertChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);

    // segment crafting
    segment6 = segmentFactory.newSegment(BigInteger.valueOf(6))
      .setChainId(BigInteger.valueOf(2))
      .setOffset(BigInteger.valueOf(0))
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:01:00.000001Z")
      .setEndAt("2017-02-14T12:01:07.384616Z")
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
      .setSequencePatternId(BigInteger.valueOf(1651))
      .setTypeEnum(SequenceType.Main)
      .setTranspose(-6));
    ImmutableList.of("Special", "Wild", "Pessimism", "Outlook").forEach(memeName -> {
      try {
        segment6.add(new SegmentMeme()
          .setSegmentId(BigInteger.valueOf(4))
          .setName(memeName));
      } catch (CoreException ignored) {
      }
    });
    segment6.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(6))
      .setPosition(0.0)
      .setName("C minor"));
    segment6.add(new SegmentChord()
      .setSegmentId(BigInteger.valueOf(6))
      .setPosition(8.0)
      .setName("Db minor"));
    IntegrationTestEntity.insert(segment6);

    // bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(2, 2);
  }

  @Test
  public void craftRhythmInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(segment6);

    craftFactory.rhythm(fabricator).doWork();

    // choice of rhythm-type sequence
    assertNotNull(segment6.getChoiceOfType(SequenceType.Rhythm));
  }
}
