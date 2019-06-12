// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import io.xj.core.CoreModule;
import io.xj.core.FixtureIT;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.program.ProgramType;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Instant;

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
    segmentFactory = injector.getInstance(SegmentFactory.class);

    // Fixtures
    reset();
    insertFixtureB1();
    insertFixtureB2();

    // Chain "Test Print #1" has 5 total segments
    insert(newChain(1, 1, "Test Print #1", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null, now(), newChainBinding(library2)));
    insert(newSegment(1, 1, 0, SegmentState.Dubbed, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", 64, 0.73, 120, "chains-1-segments-9f7s89d8a7892.wav"));
    insert(newSegment(2, 1, 1, SegmentState.Dubbing, Instant.parse("2017-02-14T12:01:32.000001Z"), Instant.parse("2017-02-14T12:02:04.000001Z"), "Db minor", 64, 0.85, 120, "chains-1-segments-9f7s89d8a7892.wav"));

    // Chain "Test Print #1" has this segment that was just crafted
    segment3 = newSegment(3, 1,2, SegmentState.Crafted, Instant.parse("2017-02-14T12:02:04.000001Z"), Instant.parse("2017-02-14T12:02:36.000001Z"), "F Major", 64, 0.30, 120.0, "chains-1-segments-9f7s89d8a7892.wav");
    segment3.add(newChoice(ProgramType.Macro,4,program4_binding1.getId(),3));
    segment3.add(newChoice(ProgramType.Main,5,program5_binding0.getId(),5));
    insert(segment3);

    // Chain "Test Print #1" has a planned segment
    segment4 = insert(newSegment(4, 1, 3, Instant.parse("2017-02-14T12:03:08.000001Z")));
  }

  /**
   [#162361525] persist Segment basis as JSON, then read basis JSON during fabrication of any segment that continues a main sequence
   */
  @Test
  public void craftFoundationContinue() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(segment4);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOneAtChainOffset(Access.internal(), BigInteger.valueOf(1), 3L);
    assertEquals(FabricatorType.Continue, result.getType());
    assertEquals("2017-02-14T12:03:23.680157Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(32), result.getTotal());
    assertEquals(Double.valueOf(0.45), result.getDensity());
    assertEquals("Ab minor", result.getKey());
    assertEquals(Double.valueOf(125), result.getTempo());
    assertEquals(FabricatorType.Continue, result.getType());
    assertExactMemes(Lists.newArrayList("Outlook", "Tropical", "Cozy", "Wild", "Pessimism"), result.getMemes());
    assertExactChords(Lists.newArrayList("B minor", "C# major"), result.getChords());
    assertEquals(program4_binding1.getId(), result.getChoiceOfType(ProgramType.Macro).getSequenceBindingId());
    assertEquals(Integer.valueOf(3), result.getChoiceOfType(ProgramType.Macro).getTranspose());
    assertEquals(Long.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(result.getChoiceOfType(ProgramType.Macro)));
    assertEquals(program5_binding1.getId(), result.getChoiceOfType(ProgramType.Main).getSequenceBindingId());
    assertEquals(Integer.valueOf(1), result.getChoiceOfType(ProgramType.Main).getTranspose());
    assertEquals(Long.valueOf(1), fabricator.getSequenceBindingOffsetForChoice(result.getChoiceOfType(ProgramType.Main)));
  }

}
