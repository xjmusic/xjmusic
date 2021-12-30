// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.craft;

import io.xj.api.*;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.hub_client.client.HubContent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.nexus.NexusIntegrationTestingFixtures.*;
import static io.xj.nexus.craft.detail.DetailCraftImpl.DETAIL_INSTRUMENT_TYPES;
import static io.xj.nexus.persistence.Segments.DELTA_UNLIMITED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftImplTest {
  @Mock
  public Fabricator fabricator;
  @Mock
  public HubContent hubContent;
  private CraftImpl subject;
  private Segment segment0;
  private Program program1;

  @Before
  public void setUp() throws Exception {
    Account account1 = buildAccount("fish");
    Library library1 = buildLibrary(account1, "sea");
    program1 = buildProgram(library1, ProgramType.Detail, ProgramState.Published, "swimming", "C", 120.0f, 0.6f);
    Template template1 = buildTemplate(account1, "Test Template 1", "test1");
    // Chain "Test Print #1" is fabricating segments
    Chain chain1 = buildChain(account1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, template1, Instant.parse("2014-08-12T12:17:02.527142Z"), null, null);

    segment0 = buildSegment(
      chain1,
      SegmentType.INITIAL,
      2,
      128,
      SegmentState.DUBBED,
      Instant.parse("2017-02-14T12:01:00.000001Z"),
      Instant.parse("2017-02-14T12:01:32.000001Z"),
      "D major",
      64,
      0.73,
      120.0,
      "chains-1-segments-9f7s89d8a7892",
      "wav");

    TemplateConfig templateConfig = new TemplateConfig(template1);
    when(fabricator.getTemplateConfig()).thenReturn(templateConfig);
    when(fabricator.getSegment()).thenReturn(segment0);
    subject = new CraftImpl(fabricator);
  }

  @Test
  public void precomputeDeltas() throws NexusException {
    CraftImpl.ChoiceIndexProvider choiceIndexProvider = SegmentChoice::getInstrumentType;
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> ProgramType.Detail.toString().equals(choice.getProgramType());
    subject.precomputeDeltas(choiceFilter, choiceIndexProvider, DETAIL_INSTRUMENT_TYPES, List.of(), 1);
  }

  @Test
  public void isIntroSegment() {
    assertTrue(subject.isIntroSegment(buildSegmentChoice(segment0, 132, 200, program1)));
    assertFalse(subject.isIntroSegment(buildSegmentChoice(segment0, 110, 200, program1)));
    assertFalse(subject.isIntroSegment(buildSegmentChoice(segment0, 200, 250, program1)));
  }

  @Test
  public void inBounds() {
    assertFalse(CraftImpl.inBounds(DELTA_UNLIMITED, 17, 19));
    assertFalse(CraftImpl.inBounds(4, DELTA_UNLIMITED, 2));
    assertFalse(CraftImpl.inBounds(4, 17, 19));
    assertFalse(CraftImpl.inBounds(4, 17, 2));
    assertTrue(CraftImpl.inBounds(DELTA_UNLIMITED, DELTA_UNLIMITED, 799));
    assertTrue(CraftImpl.inBounds(DELTA_UNLIMITED, 17, 2));
    assertTrue(CraftImpl.inBounds(4, DELTA_UNLIMITED, 19));
    assertTrue(CraftImpl.inBounds(4, 17, 12));
    assertTrue(CraftImpl.inBounds(4, 17, 17));
    assertTrue(CraftImpl.inBounds(4, 17, 4));
  }

  @Test
  public void isOutroSegment() {
    assertTrue(subject.isOutroSegment(buildSegmentChoice(segment0, 20, 130, program1)));
    assertFalse(subject.isOutroSegment(buildSegmentChoice(segment0, 20, 100, program1)));
    assertFalse(subject.isOutroSegment(buildSegmentChoice(segment0, 20, 250, program1)));
  }

  @Test
  public void isSilentEntireSegment() {
    assertTrue(subject.isSilentEntireSegment(buildSegmentChoice(segment0, 12, 25, program1)));
    assertTrue(subject.isSilentEntireSegment(buildSegmentChoice(segment0, 200, 225, program1)));
    assertFalse(subject.isSilentEntireSegment(buildSegmentChoice(segment0, 50, 136, program1)));
    assertFalse(subject.isSilentEntireSegment(buildSegmentChoice(segment0, 150, 200, program1)));
    assertFalse(subject.isSilentEntireSegment(buildSegmentChoice(segment0, 130, 150, program1)));
  }

  @Test
  public void isActiveEntireSegment() {
    assertFalse(subject.isActiveEntireSegment(buildSegmentChoice(segment0, 12, 25, program1)));
    assertFalse(subject.isActiveEntireSegment(buildSegmentChoice(segment0, 200, 225, program1)));
    assertFalse(subject.isActiveEntireSegment(buildSegmentChoice(segment0, 50, 136, program1)));
    assertFalse(subject.isActiveEntireSegment(buildSegmentChoice(segment0, 150, 200, program1)));
    assertFalse(subject.isActiveEntireSegment(buildSegmentChoice(segment0, 130, 150, program1)));
    assertTrue(subject.isActiveEntireSegment(buildSegmentChoice(segment0, 126, 195, program1)));
  }

  @Test
  public void isUnlimitedIn() {
    assertTrue(CraftImpl.isUnlimitedIn(buildSegmentChoice(segment0, DELTA_UNLIMITED, 25, program1)));
    assertFalse(CraftImpl.isUnlimitedIn(buildSegmentChoice(segment0, 25, DELTA_UNLIMITED, program1)));
  }

  @Test
  public void isUnlimitedOut() {
    assertFalse(CraftImpl.isUnlimitedOut(buildSegmentChoice(segment0, DELTA_UNLIMITED, 25, program1)));
    assertTrue(CraftImpl.isUnlimitedOut(buildSegmentChoice(segment0, 25, DELTA_UNLIMITED, program1)));
  }
}
