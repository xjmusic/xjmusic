// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.macro;

import com.google.common.collect.Lists;
import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.integration.IntegrationTestEntity;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentFactory;
import io.xj.core.model.sequence.SequenceType;
import io.xj.craft.BaseIT;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.craft.CraftFactory;
import io.xj.craft.CraftModule;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.time.Instant;

import static io.xj.core.Assert.assertExactChords;
import static io.xj.core.Assert.assertExactMemes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class CraftFoundationInitialIT extends BaseIT {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private final Injector injector = Guice.createInjector(new CoreModule(), new CraftModule());
  private CraftFactory craftFactory;
  private FabricatorFactory fabricatorFactory;

  // Testing entities for reference
  private Segment segment6;

  @Before
  public void setUp() throws Exception {
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
    craftFactory = injector.getInstance(CraftFactory.class);

    // Fixtures
    IntegrationTestEntity.reset();
    insertLibraryB1();

    // Chain "Print #2" has 1 initial planned segment
    IntegrationTestEntity.insertChain(2, 1, "Print #2", ChainType.Production, ChainState.Fabricate, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);
    segment6 = IntegrationTestEntity.insertSegment_Planned(6, 2, 0, Instant.parse("2017-02-14T12:01:00.000001Z"));
    // bind the library to the chain
    IntegrationTestEntity.insertChainLibrary(2, 2);
  }

  @Test
  public void craftFoundationInitial() throws Exception {
    Fabricator fabricator = fabricatorFactory.fabricate(segment6);

    craftFactory.macroMain(fabricator).doWork();

    Segment result = injector.getInstance(SegmentDAO.class).readOneAtChainOffset(Access.internal(), BigInteger.valueOf(2), BigInteger.valueOf(0));
    assertEquals("2017-02-14T12:01:07.384616Z", result.getEndAt().toString());
    assertEquals(Integer.valueOf(16), result.getTotal());
    assertEquals(0.55, result.getDensity(), 0.01);
    assertEquals("G major", result.getKey());
    assertEquals(130.0, result.getTempo(), 0.01);
    assertExactMemes(Lists.newArrayList("Tropical", "Wild", "Outlook", "Optimism"), result.getMemes());
    assertExactChords(Lists.newArrayList("G major", "Ab minor"), result.getChords());
    assertEquals(BigInteger.valueOf(4), fabricator.getSequenceOfChoice(result.getChoiceOfType(SequenceType.Macro)).getId());
    assertEquals(Integer.valueOf(0), result.getChoiceOfType(SequenceType.Macro).getTranspose());
    assertEquals(BigInteger.valueOf(0), fabricator.getSequencePatternOffsetForChoice(result.getChoiceOfType(SequenceType.Macro)));
    assertEquals(BigInteger.valueOf(5), fabricator.getSequenceOfChoice(result.getChoiceOfType(SequenceType.Main)).getId());
    assertEquals(Integer.valueOf(0), result.getChoiceOfType(SequenceType.Main).getTranspose());
    assertEquals(BigInteger.valueOf(0), fabricator.getSequencePatternOffsetForChoice(result.getChoiceOfType(SequenceType.Main)));
  }
}
