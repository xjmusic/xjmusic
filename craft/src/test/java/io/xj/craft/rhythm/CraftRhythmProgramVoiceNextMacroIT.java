// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.rhythm;

import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.SegmentChoiceArrangementDAO;
import io.xj.core.dao.SegmentChoiceDAO;
import io.xj.core.dao.SegmentChoiceArrangementPickDAO;
import io.xj.core.exception.CoreException;
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
import io.xj.core.model.ProgramType;
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

public class CraftRhythmProgramVoiceNextMacroIT extends FixtureIT {
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
    insert(InstrumentAudioEvent.create(audioSnare, 1, 1, "SNARE", "Ab", 1.0));
  }

  @Test
  public void craftRhythmVoiceNextMacro() throws Exception {
    insertSegments3and4(true);
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segment4);

    craftFactory.rhythm(fabricator).doWork();

    // assert rhythm choice
    SegmentChoice rhythmChoice = SegmentChoice.findFirstOfType(injector.getInstance(SegmentChoiceDAO.class)
      .readMany(Access.internal(), ImmutableList.of(segment4.getId())), ProgramType.Rhythm);
    assertTrue(injector.getInstance(SegmentChoiceArrangementDAO.class).readMany(Access.internal(), ImmutableList.of(segment4.getId()))
      .stream().anyMatch(a -> a.getSegmentChoiceId().equals(rhythmChoice.getId())));
    // test vector for [#154014731] persist Audio pick in memory
    int pickedKick = 0;
    int pickedSnare = 0;
    Collection<SegmentChoiceArrangementPick> picks = injector.getInstance(SegmentChoiceArrangementPickDAO.class).readMany(Access.internal(), ImmutableList.of(segment4.getId()));
    for (SegmentChoiceArrangementPick pick : picks) {
      if (pick.getInstrumentAudioId().equals(audioKick.getId()))
        pickedKick++;
      if (pick.getInstrumentAudioId().equals(audioSnare.getId()))
        pickedSnare++;
    }
    assertEquals(8, pickedKick);
    assertEquals(8, pickedSnare);
  }

  @Test
  public void craftRhythmVoiceNextMacro_okIfNoRhythmChoice() throws Exception {
    insertSegments3and4(false);
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segment4);

    craftFactory.rhythm(fabricator).doWork();
  }

  /**
   Insert fixture segments 3 and 4, including the rhythm choice for segment 3 only if specified

   @param excludeRhythmChoiceForSegment3 if desired for the purpose of this test
   */
  private void insertSegments3and4(boolean excludeRhythmChoiceForSegment3) throws CoreException {
    // Chain "Test Print #1" has this segment that was just crafted
    segment3 = insert(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(2L)
      .setStateEnum(SegmentState.Crafted)
      .setBeginAt("2017-02-14T12:02:04.000001Z")
      .setEndAt("2017-02-14T12:02:36.000001Z")
      .setKey("Ab minor")
      .setTotal(64)
      .setDensity(0.30)
      .setTempo(120.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create().setSegmentId(segment3.getId())
      .setProgramId(program4.getId())
      .setProgramSequenceBindingId(program4_binding2.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(3));
    insert(SegmentChoice.create().setSegmentId(segment3.getId())
      .setProgramId(program5.getId())
      .setProgramSequenceBindingId(program5_binding1.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(1));
    if (!excludeRhythmChoiceForSegment3)
      insert(SegmentChoice.create().setSegmentId(segment3.getId())
        .setProgramId(program35.getId())
        .setTypeEnum(ProgramType.Rhythm)
        .setTranspose(-4));

    // Chain "Test Print #1" has a segment in crafting state - Foundation is complete
    segment4 = insert(Segment.create()
      .setChainId(chain1.getId())
      .setOffset(3L)
      .setStateEnum(SegmentState.Crafting)
      .setBeginAt("2017-02-14T12:03:08.000001Z")
      .setEndAt("2017-02-14T12:03:15.836735Z")
      .setKey("F minor")
      .setTotal(16)
      .setDensity(0.45)
      .setTempo(125.0)
      .setWaveformKey("chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create().setSegmentId(segment4.getId())
      .setProgramId(program3.getId())
      .setProgramSequenceBindingId(program3_binding0.getId())
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(4));
    insert(SegmentChoice.create().setSegmentId(segment4.getId())
      .setProgramId(program15.getId())
      .setProgramSequenceBindingId(program15_binding0.getId())
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-2));
    for (String memeName : ImmutableList.of("Hindsight", "Chunky", "Regret", "Tangy")) {
      insert(SegmentMeme.create(segment4, memeName));
    }
    insert(SegmentChord.create(segment4, 0.0, "F minor"));
    insert(SegmentChord.create(segment4, 8.0, "Gb minor"));
  }

}
