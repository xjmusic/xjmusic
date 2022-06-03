// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.craft;

import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.ChainType;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.hub.TemplateConfig;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.meme.MemeTaxonomy;
import io.xj.nexus.NexusException;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.MemeIsometry;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrument;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrumentAudio;
import static io.xj.hub.IntegrationTestingFixtures.buildInstrumentMeme;
import static io.xj.hub.IntegrationTestingFixtures.buildLibrary;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.persistence.Segments.DELTA_UNLIMITED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CraftImplTest {
  @Mock
  public Fabricator fabricator;
  @Mock
  public HubContent sourceMaterial;
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
    when(fabricator.sourceMaterial()).thenReturn(sourceMaterial);
    subject = new CraftImpl(fabricator);
  }

  @Test
  public void precomputeDeltas() throws NexusException {
    CraftImpl.ChoiceIndexProvider choiceIndexProvider = SegmentChoice::getInstrumentType;
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> ProgramType.Detail.toString().equals(choice.getProgramType());
    subject.precomputeDeltas(choiceFilter, choiceIndexProvider, fabricator.getTemplateConfig().getDetailLayerOrder().stream().map(InstrumentType::toString).collect(Collectors.toList()), List.of(), 1);
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

  /**
   PercLoops are not adhering to "__BPM" memes
   https://www.pivotaltracker.com/story/show/181975131
   */
  @Test
  public void chooseFreshInstrumentAudio() {
    Account account1 = buildAccount("testing");
    Library library1 = buildLibrary(account1, "leaves");
    Instrument instrument1 = buildInstrument(library1, InstrumentType.Percussion, InstrumentMode.Event, InstrumentState.Published, "Loop 75 beats per minute");
    InstrumentMeme instrument1meme = buildInstrumentMeme(instrument1, "70BPM");
    InstrumentAudio instrument1audio = buildInstrumentAudio(instrument1, "slow loop", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.62f, "PRIMARY", "X", 1.0f);
    Instrument instrument2 = buildInstrument(library1, InstrumentType.Percussion, InstrumentMode.Event, InstrumentState.Published, "Loop 85 beats per minute");
    InstrumentMeme instrument2meme = buildInstrumentMeme(instrument2, "90BPM");
    InstrumentAudio instrument2audio = buildInstrumentAudio(instrument2, "fast loop", "90bpm.wav", 0.01f, 2.123f, 120.0f, 0.62f, "SECONDARY", "X", 1.0f);
    //
    when(sourceMaterial.getInstrument(eq(instrument1.getId()))).thenReturn(Optional.of(instrument1));
    when(sourceMaterial.getInstrument(eq(instrument2.getId()))).thenReturn(Optional.of(instrument2));
    when(sourceMaterial.getInstrumentAudios(eq(List.of(InstrumentType.Percussion)), eq(List.of(InstrumentMode.Loop)))).thenReturn(List.of(instrument1audio, instrument2audio));
    when(fabricator.getMemeIsometryOfSegment()).thenReturn(MemeIsometry.of(MemeTaxonomy.empty(), List.of("70BPM")));
    when(sourceMaterial.getMemesForInstrumentId(eq(instrument1.getId()))).thenReturn(List.of(instrument1meme));
    when(sourceMaterial.getMemesForInstrumentId(eq(instrument2.getId()))).thenReturn(List.of(instrument2meme));
    when(sourceMaterial.getInstrumentAudio(eq(instrument1audio.getId()))).thenReturn(Optional.of(instrument1audio));

    var result = subject.chooseFreshInstrumentAudio(List.of(InstrumentType.Percussion), List.of(InstrumentMode.Loop), List.of(instrument1audio.getInstrumentId()), List.of("PRIMARY"));

    verify(sourceMaterial, times(1)).getMemesForInstrumentId(eq(instrument1.getId()));
    assertTrue(result.isPresent());
  }
}
