// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO convert this test to C++

package io.xj.engine.craft;

import io.xj.engine.ContentFixtures;
import io.xj.engine.SegmentFixtures;
import io.xj.engine.FabricationException;
import io.xj.engine.fabricator.Fabricator;
import io.xj.engine.fabricator.MemeIsometry;
import io.xj.engine.fabricator.SegmentRetrospective;
import io.xj.model.pojos.Chain;
import io.xj.model.enums.ChainState;
import io.xj.model.enums.ChainType;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.enums.SegmentState;
import io.xj.model.enums.SegmentType;
import io.xj.model.HubContent;
import io.xj.model.InstrumentConfig;
import io.xj.model.TemplateConfig;
import io.xj.model.enums.InstrumentMode;
import io.xj.model.enums.InstrumentState;
import io.xj.model.enums.InstrumentType;
import io.xj.model.enums.ProgramState;
import io.xj.model.enums.ProgramType;
import io.xj.model.meme.MemeTaxonomy;
import io.xj.model.music.Chord;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.pojos.InstrumentMeme;
import io.xj.model.pojos.Library;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.Project;
import io.xj.model.pojos.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static io.xj.model.pojos.Segment.DELTA_UNLIMITED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CraftImplTest {
  static final int TEST_REPEAT_TIMES = 20;
  @Mock
  public Fabricator fabricator;
  public HubContent sourceMaterial;
  @Mock
  public SegmentRetrospective segmentRetrospective;
  CraftImpl subject;
  Segment segment0;
  Program program1;

  @BeforeEach
  public void setUp() throws Exception {
    Project project1 = ContentFixtures.buildProject("fish");
    Library library1 = ContentFixtures.buildLibrary(project1, "sea");
    program1 = ContentFixtures.buildProgram(library1, ProgramType.Detail, ProgramState.Published, "swimming", "C", 120.0f);
    Template template1 = ContentFixtures.buildTemplate(project1, "Test Template 1", "test1");
    // Chain "Test Print #1" is fabricating segments
    Chain chain1 = SegmentFixtures.buildChain(project1, "Test Print #1", ChainType.PRODUCTION, ChainState.FABRICATE, template1, null);

    segment0 = SegmentFixtures.buildSegment(chain1, SegmentType.INITIAL, 2, 128, SegmentState.CRAFTED, "D major", 64, 0.73f, 120.0f, "chains-1-segments-9f7s89d8a7892", true);

    var templateConfig = new TemplateConfig(template1);
    when(fabricator.getTemplateConfig()).thenReturn(templateConfig);

    sourceMaterial = new HubContent();

    subject = new CraftImpl(fabricator);
  }

  @Test
  public void precomputeDeltas() throws FabricationException {
    CraftImpl.ChoiceIndexProvider choiceIndexProvider = choice -> choice.getInstrumentType().toString();
    Predicate<SegmentChoice> choiceFilter = (SegmentChoice choice) -> ProgramType.Detail.equals(choice.getProgramType());
    subject.precomputeDeltas(choiceFilter, choiceIndexProvider, fabricator.getTemplateConfig().getDetailLayerOrder().stream().map(InstrumentType::toString).collect(Collectors.toList()), List.of(), 1);
  }

  @Test
  public void isIntroSegment() {
    when(fabricator.getSegment()).thenReturn(segment0);

    assertTrue(subject.isIntroSegment(SegmentFixtures.buildSegmentChoice(segment0, 132, 200, program1)));
    assertFalse(subject.isIntroSegment(SegmentFixtures.buildSegmentChoice(segment0, 110, 200, program1)));
    assertFalse(subject.isIntroSegment(SegmentFixtures.buildSegmentChoice(segment0, 200, 250, program1)));
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
    when(fabricator.getSegment()).thenReturn(segment0);

    assertTrue(subject.isOutroSegment(SegmentFixtures.buildSegmentChoice(segment0, 20, 130, program1)));
    assertFalse(subject.isOutroSegment(SegmentFixtures.buildSegmentChoice(segment0, 20, 100, program1)));
    assertFalse(subject.isOutroSegment(SegmentFixtures.buildSegmentChoice(segment0, 20, 250, program1)));
  }

  @Test
  public void isSilentEntireSegment() {
    when(fabricator.getSegment()).thenReturn(segment0);

    assertTrue(subject.isSilentEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 12, 25, program1)));
    assertTrue(subject.isSilentEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 200, 225, program1)));
    assertFalse(subject.isSilentEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 50, 136, program1)));
    assertFalse(subject.isSilentEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 150, 200, program1)));
    assertFalse(subject.isSilentEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 130, 150, program1)));
  }

  @Test
  public void isActiveEntireSegment() {
    when(fabricator.getSegment()).thenReturn(segment0);

    assertFalse(subject.isActiveEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 12, 25, program1)));
    assertFalse(subject.isActiveEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 200, 225, program1)));
    assertFalse(subject.isActiveEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 50, 136, program1)));
    assertFalse(subject.isActiveEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 150, 200, program1)));
    assertFalse(subject.isActiveEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 130, 150, program1)));
    assertTrue(subject.isActiveEntireSegment(SegmentFixtures.buildSegmentChoice(segment0, 126, 195, program1)));
  }

  @Test
  public void isUnlimitedIn() {
    assertTrue(CraftImpl.isUnlimitedIn(SegmentFixtures.buildSegmentChoice(segment0, DELTA_UNLIMITED, 25, program1)));
    assertFalse(CraftImpl.isUnlimitedIn(SegmentFixtures.buildSegmentChoice(segment0, 25, DELTA_UNLIMITED, program1)));
  }

  @Test
  public void isUnlimitedOut() {
    assertFalse(CraftImpl.isUnlimitedOut(SegmentFixtures.buildSegmentChoice(segment0, DELTA_UNLIMITED, 25, program1)));
    assertTrue(CraftImpl.isUnlimitedOut(SegmentFixtures.buildSegmentChoice(segment0, 25, DELTA_UNLIMITED, program1)));
  }

  /**
   PercLoops are not adhering to "__BPM" memes
   https://github.com/xjmusic/xjmusic/issues/296
   */
  @Test
  public void chooseFreshInstrumentAudio() throws Exception {
    Project project1 = ContentFixtures.buildProject("testing");
    Library library1 = ContentFixtures.buildLibrary(project1, "leaves");
    Instrument instrument1 = ContentFixtures.buildInstrument(library1, InstrumentType.Percussion, InstrumentMode.Event, InstrumentState.Published, "Loop 75 beats per minute");
    InstrumentMeme instrument1meme = ContentFixtures.buildInstrumentMeme(instrument1, "70BPM");
    InstrumentAudio instrument1audio = ContentFixtures.buildInstrumentAudio(instrument1, "slow loop", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.62f, "PRIMARY", "X", 1.0f);
    Instrument instrument2 = ContentFixtures.buildInstrument(library1, InstrumentType.Percussion, InstrumentMode.Event, InstrumentState.Published, "Loop 85 beats per minute");
    InstrumentMeme instrument2meme = ContentFixtures.buildInstrumentMeme(instrument2, "90BPM");
    InstrumentAudio instrument2audio = ContentFixtures.buildInstrumentAudio(instrument2, "fast loop", "90bpm.wav", 0.01f, 2.123f, 120.0f, 0.62f, "SECONDARY", "X", 1.0f);
    //
    sourceMaterial.putAll(Set.of(project1, library1, instrument1, instrument2, instrument1audio, instrument2audio, instrument1meme, instrument2meme));
    when(fabricator.getMemeIsometryOfSegment()).thenReturn(MemeIsometry.of(MemeTaxonomy.empty(), List.of("70BPM")));
    when(fabricator.sourceMaterial()).thenReturn(sourceMaterial);

    var result = subject.chooseFreshInstrumentAudio(List.of(InstrumentType.Percussion), List.of(InstrumentMode.Event), List.of(instrument1audio.getInstrumentId()), List.of("PRIMARY"));

    assertTrue(result.isPresent());
  }

  /**
   XJ Should choose the correct chord audio per Main Program chord https://github.com/xjmusic/xjmusic/issues/237
   */
  @Test
  public void selectNewChordPartInstrumentAudio_stripSpaces() throws Exception {
    when(fabricator.sourceMaterial()).thenReturn(sourceMaterial);

    selectNewChordPartInstrumentAudio(" G   major  ", "G-7", " G    major    ");
  }

  /**
   Chord-mode Instrument: Slash Chord Fluency
   https://github.com/xjmusic/xjmusic/issues/227
   When the exact match is not present for an entire slash chord name, choose a chord matching the pre-slash name
   */
  @Test
  public void selectNewChordPartInstrumentAudio_slashChordFluency() throws Exception {
    when(fabricator.sourceMaterial()).thenReturn(sourceMaterial);

    selectNewChordPartInstrumentAudio("Ab/C", "Eb/G", "Ab");
    selectNewChordPartInstrumentAudio("Ab", "Eb/G", "Ab/C");
  }

  /**
   Enhanced Synonymous Chord recognition https://github.com/xjmusic/xjmusic/issues/236
   */
  @Test
  public void selectNewChordPartInstrumentAudio_chordSynonyms() throws Exception {
    when(fabricator.sourceMaterial()).thenReturn(sourceMaterial);

    selectNewChordPartInstrumentAudio("CMadd9", "Cm6", "C add9");
  }

  @Test
  public void selectGeneralAudioIntensityLayers_threeLayers() throws Exception {
    Project project1 = ContentFixtures.buildProject("testing");
    Library library1 = ContentFixtures.buildLibrary(project1, "leaves");
    Instrument instrument1 = ContentFixtures.buildInstrument(library1, InstrumentType.Percussion, InstrumentMode.Loop, InstrumentState.Published, "Test loop audio");
    instrument1.setConfig("isAudioSelectionPersistent=true");
    InstrumentConfig instrumentConfig = new InstrumentConfig(instrument1);
    InstrumentAudio instrument1audio1a = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.2f, "PERC", "X", 1.0f);
    InstrumentAudio instrument1audio1b = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.2f, "PERC", "X", 1.0f);
    InstrumentAudio instrument1audio2a = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.5f, "PERC", "X", 1.0f);
    InstrumentAudio instrument1audio2b = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.5f, "PERC", "X", 1.0f);
    InstrumentAudio instrument1audio3a = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.8f, "PERC", "X", 1.0f);
    InstrumentAudio instrument1audio3b = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.8f, "PERC", "X", 1.0f);
    sourceMaterial.putAll(Set.of(instrument1, instrument1audio1a, instrument1audio1b, instrument1audio2a, instrument1audio2b, instrument1audio3a, instrument1audio3b));
    when(fabricator.sourceMaterial()).thenReturn(sourceMaterial);
    when(fabricator.retrospective()).thenReturn(segmentRetrospective);
    when(fabricator.getInstrumentConfig(same(instrument1))).thenReturn(instrumentConfig);

    var result = subject.selectGeneralAudioIntensityLayers(instrument1).stream()
      .sorted(Comparator.comparing(InstrumentAudio::getIntensity))
      .toList();

    assertEquals(3, result.size());
    assertTrue(Set.of(instrument1audio1a.getId(), instrument1audio1b.getId()).contains(result.get(0).getId()));
    assertTrue(Set.of(instrument1audio2a.getId(), instrument1audio2b.getId()).contains(result.get(1).getId()));
    assertTrue(Set.of(instrument1audio3a.getId(), instrument1audio3b.getId()).contains(result.get(2).getId()));
  }

  @Test
  public void selectGeneralAudioIntensityLayers_continueSegment() throws Exception {
    Project project1 = ContentFixtures.buildProject("testing");
    Library library1 = ContentFixtures.buildLibrary(project1, "leaves");
    Instrument instrument1 = ContentFixtures.buildInstrument(library1, InstrumentType.Percussion, InstrumentMode.Loop, InstrumentState.Published, "Test loop audio");
    instrument1.setConfig("isAudioSelectionPersistent=true");
    InstrumentConfig instrumentConfig = new InstrumentConfig(instrument1);
    InstrumentAudio instrument1audio1a = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.2f, "PERC", "X", 1.0f);
    InstrumentAudio instrument1audio1b = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.2f, "PERC", "X", 1.0f);
    InstrumentAudio instrument1audio2a = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.5f, "PERC", "X", 1.0f);
    InstrumentAudio instrument1audio2b = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.5f, "PERC", "X", 1.0f);
    InstrumentAudio instrument1audio3a = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.8f, "PERC", "X", 1.0f);
    InstrumentAudio instrument1audio3b = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.8f, "PERC", "X", 1.0f);
    sourceMaterial.putAll(Set.of(instrument1, instrument1audio1a, instrument1audio1b, instrument1audio2a, instrument1audio2b, instrument1audio3a, instrument1audio3b));
    SegmentChoice choice = SegmentFixtures.buildSegmentChoice(segment0, instrument1);
    SegmentChoiceArrangement arrangement = SegmentFixtures.buildSegmentChoiceArrangement(choice);
    SegmentChoiceArrangementPick pick1 = SegmentFixtures.buildSegmentChoiceArrangementPick(segment0, arrangement, instrument1audio1a, instrument1audio1a.getEvent());
    SegmentChoiceArrangementPick pick2 = SegmentFixtures.buildSegmentChoiceArrangementPick(segment0, arrangement, instrument1audio2a, instrument1audio2a.getEvent());
    SegmentChoiceArrangementPick pick3 = SegmentFixtures.buildSegmentChoiceArrangementPick(segment0, arrangement, instrument1audio3a, instrument1audio3a.getEvent());
    when(fabricator.sourceMaterial()).thenReturn(sourceMaterial);
    when(fabricator.retrospective()).thenReturn(segmentRetrospective);
    when(segmentRetrospective.getPreviousPicksForInstrument(same(instrument1.getId()))).thenReturn(Set.of(pick1, pick2, pick3));
    when(fabricator.getInstrumentConfig(same(instrument1))).thenReturn(instrumentConfig);

    var result = subject.selectGeneralAudioIntensityLayers(instrument1).stream()
      .sorted(Comparator.comparing(InstrumentAudio::getIntensity))
      .toList();

    assertEquals(3, result.size());
    assertEquals(instrument1audio1a.getId(), result.get(0).getId());
    assertEquals(instrument1audio2a.getId(), result.get(1).getId());
    assertEquals(instrument1audio3a.getId(), result.get(2).getId());
  }


  /**
   Do the subroutine of testing the new chord part instrument audio selection

   @param expectThis chord name
   @param notThat    chord name
   @param match      chord name
   */
  void selectNewChordPartInstrumentAudio(String expectThis, String notThat, String match) throws Exception {
    Project project1 = ContentFixtures.buildProject("testing");
    Library library1 = ContentFixtures.buildLibrary(project1, "leaves");
    Instrument instrument1 = ContentFixtures.buildInstrument(library1, InstrumentType.Percussion, InstrumentMode.Chord, InstrumentState.Published, "Test chord audio");
    InstrumentAudio instrument1audio1 = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.62f, "PRIMARY", expectThis, 1.0f);
    InstrumentAudio instrument1audio2 = ContentFixtures.buildInstrumentAudio(instrument1, "ping", "70bpm.wav", 0.01f, 2.123f, 120.0f, 0.62f, "PRIMARY", notThat, 1.0f);
    //
    sourceMaterial.putAll(Set.of(instrument1, instrument1audio1, instrument1audio2));

    for (var i = 0; i < TEST_REPEAT_TIMES; i++) {
      var result = subject.selectNewChordPartInstrumentAudio(instrument1, Chord.of(match));

      assertTrue(result.isPresent());
      assertEquals(instrument1audio1.getId(), result.get().getId());
    }
  }

}
