// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.Access;
import io.xj.core.dao.SegmentChoiceDAO;
import io.xj.core.dao.SegmentChordDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SegmentMemeDAO;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainBinding;
import io.xj.core.model.ChainState;
import io.xj.core.model.ChainType;
import io.xj.core.model.ProgramType;
import io.xj.core.model.Segment;
import io.xj.core.model.SegmentChoice;
import io.xj.core.model.SegmentState;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Instant;
import java.util.Collection;

import static io.xj.core.testing.Assert.assertExactChords;
import static io.xj.core.testing.Assert.assertExactMemes;
import static org.junit.Assert.assertEquals;

public class CraftFoundationContinueIT extends FixtureIT {
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
    segment1 = insert(Segment.create(chain1, 0L, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    segment2 = insert(Segment.create(chain1, 1L, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

    // Chain "Test Print #1" has this segment that was just crafted
    segment3 = insert(Segment.create(chain1, 2L, SegmentState.Crafted, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "F Major", 64, 0.30, 120.0, "chains-1-segments-9f7s89d8a7892.wav"));
    insert(SegmentChoice.create(segment3, ProgramType.Macro, program4_binding1, 3));
    insert(SegmentChoice.create(segment3, ProgramType.Main, program5_binding0, 5));

    // Chain "Test Print #1" has a planned segment
    segment4 = insert(Segment.create(chain1, 3L, SegmentState.Planned, Instant.parse("2017-02-14T12:03:08.000001Z"), null, "C", 4, 1.0, 120, "chains-1-segments-9f7s89d8a7892.wav"));
  }

  /**
   [#162361525] persist Segment basis as JSON, then read basis JSON during fabrication of any segment that continues a main sequence
   */
  @Test
  public void craftFoundationContinue() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(Access.internal(), segment4);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOneAtChainOffset(Access.internal(), chain1.getId(), 3L);
    assertEquals(FabricatorType.Continue, result.getType());
    assertEquals("2017-02-14T12:03:23.680157Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(32), result.getTotal());
    assertEquals(Double.valueOf(0.45), result.getDensity());
    assertEquals("Ab minor", result.getKey());
    assertEquals(Double.valueOf(125), result.getTempo());
    assertEquals(FabricatorType.Continue, result.getType());
    // assert memes
    assertExactMemes(Lists.newArrayList("Outlook", "Tropical", "Cozy", "Wild", "Pessimism"),
      injector.getInstance(SegmentMemeDAO.class).readMany(Access.internal(), ImmutableList.of(result.getId())));
    // assert chords
    assertExactChords(Lists.newArrayList("B minor", "C# major"),
      injector.getInstance(SegmentChordDAO.class).readMany(Access.internal(), ImmutableList.of(result.getId())));
    // assert choices
    Collection<SegmentChoice> segmentChoices = injector.getInstance(SegmentChoiceDAO.class)
      .readMany(Access.internal(), ImmutableList.of(result.getId()));
    // assert macro choice
    SegmentChoice macroChoice = SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Macro);
    assertEquals(program4_binding1.getId(), macroChoice.getProgramSequenceBindingId());
    assertEquals(Integer.valueOf(3), macroChoice.getTranspose());
    assertEquals(Long.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(macroChoice));
    // assert main choice
    SegmentChoice mainChoice = SegmentChoice.findFirstOfType(segmentChoices, ProgramType.Main);
    assertEquals(program5_binding1.getId(), mainChoice.getProgramSequenceBindingId()); // next main sequence binding in same program as previous sequence
    assertEquals(Integer.valueOf(1), mainChoice.getTranspose());
    assertEquals(Long.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(mainChoice));
  }

}
