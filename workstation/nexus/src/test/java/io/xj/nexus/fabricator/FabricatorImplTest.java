// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.fabricator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.hub.HubContent;
import io.xj.hub.HubTopology;
import io.xj.hub.entity.EntityFactoryImpl;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.json.JsonProvider;
import io.xj.hub.json.JsonProviderImpl;
import io.xj.hub.jsonapi.JsonapiPayloadFactory;
import io.xj.hub.music.Chord;
import io.xj.hub.music.Note;
import io.xj.hub.music.PitchClass;
import io.xj.hub.music.StickyBun;
import io.xj.hub.pojos.Template;
import io.xj.hub.util.ValueException;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildEvent;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildPattern;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildProgram;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildProject;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildSequence;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildTrack;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildVoice;
import static io.xj.nexus.NexusHubIntegrationTestingFixtures.buildVoicing;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoiceArrangement;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentMeta;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FabricatorImplTest {
  static int SEQUENCE_TOTAL_BEATS = 64;
  @Mock
  public FabricatorFactory mockFabricatorFactory;
  @Mock
  public SegmentRetrospective mockRetrospective;
  @Mock
  public JsonapiPayloadFactory mockJsonapiPayloadFactory;
  public JsonProvider jsonProvider;
  FabricatorImpl subject;
  HubContent sourceMaterial;
  FabricationEntityStore store;
  NexusIntegrationTestingFixtures fake;
  Segment segment;

  @BeforeEach
  public void setUp() throws Exception {
    jsonProvider = new JsonProviderImpl();
    var entityFactory = new EntityFactoryImpl(jsonProvider);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    store = new FabricationEntityStoreImpl(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store.clear();

    // Mock request via HubClientFactory returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Stream.concat(Stream.concat(Stream.concat(fake.setupFixtureB1().stream(), fake.setupFixtureB2().stream()), fake.setupFixtureB3().stream()), Stream.of(fake.template1, fake.templateBinding1)).collect(Collectors.toList()));

    // Here's a basic setup that can be replaced for complex tests
    var chain = store.put(buildChain(fake.project1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, "G major", 8, 0.6f, 240.0f, "seg123"));
    when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
    subject = new FabricatorImpl(mockFabricatorFactory, store, sourceMaterial, segment.getId(), mockJsonapiPayloadFactory, jsonProvider, 48000.0f, 2, null);
  }

  @Test
  public void pick_returned_by_picks() throws Exception {
    buildTemplateBinding(fake.template1, fake.library2);
    var chain = store.put(buildChain(fake.project1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE));
    store.put(buildSegment(chain, 1, SegmentState.CRAFTED, "F major", 8, 0.6f, 120.0f, "seg123"));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, "G major", 8, 0.6f, 240.0f, "seg123"));
    store.put(buildSegmentChord(segment, 0.0f, "A"));
    store.put(buildSegmentChoice(segment, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program5));
    SegmentChoice beatChoice = store.put(buildSegmentChoice(segment, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program35, fake.program35_voice0, fake.instrument8));
    SegmentChoiceArrangement beatArrangement = store.put(buildSegmentChoiceArrangement(beatChoice));
    store.put(new SegmentChoiceArrangementPick().id(UUID.randomUUID()).segmentId(beatArrangement.getSegmentId()).segmentChoiceArrangementId(beatArrangement.getId()).programSequencePatternEventId(fake.program35_sequence0_pattern0_event0.getId()).instrumentAudioId(fake.instrument8_audio8kick.getId()).event("CLANG").startAtSegmentMicros((long) (0.273 * MICROS_PER_SECOND)).lengthMicros((long) (1.571 * MICROS_PER_SECOND)).amplitude(0.8f).tones("A4"));
    when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
    subject = new FabricatorImpl(mockFabricatorFactory, store, sourceMaterial, segment.getId(), mockJsonapiPayloadFactory, jsonProvider, 48000.0f, 2, null);

    Collection<SegmentChoiceArrangementPick> result = subject.getPicks();

    SegmentChoiceArrangementPick resultPick = result.iterator().next();
    assertEquals(beatArrangement.getId(), resultPick.getSegmentChoiceArrangementId());
    assertEquals(fake.instrument8_audio8kick.getId(), resultPick.getInstrumentAudioId());
    assertEquals(0.273 * MICROS_PER_SECOND, resultPick.getStartAtSegmentMicros(), 0.001);
    assertEquals(1.571 * MICROS_PER_SECOND, resultPick.getLengthMicros(), 0.001);
    assertEquals(0.8f, resultPick.getAmplitude(), 0.1);
    assertEquals("A4", resultPick.getTones());
  }


  @Test
  public void getDistinctChordVoicingTypes() throws Exception {
    sourceMaterial = new HubContent(Stream.concat(Stream.concat(Stream.concat(fake.setupFixtureB1().stream(), fake.setupFixtureB2().stream()), fake.setupFixtureB3().stream()), Stream.of(buildVoicing(fake.program5_sequence0_chord0, fake.program5_voiceSticky, "G4, B4, D4"), buildVoicing(fake.program5_sequence0_chord0, fake.program5_voiceStripe, "F5"), buildVoicing(fake.program5_sequence0_chord0, fake.program5_voicePad, "(None)") // No voicing notes- doesn't count!
    )).collect(Collectors.toList()));
    var chain = store.put(buildChain(fake.project1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE));
    segment = store.put(buildSegment(chain, 0, SegmentState.CRAFTING, "F major", 8, 0.6f, 120.0f, "seg123"));
    store.put(buildSegmentChoice(segment, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program5));
    when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
    subject = new FabricatorImpl(mockFabricatorFactory, store, sourceMaterial, segment.getId(), mockJsonapiPayloadFactory, jsonProvider, 48000.0f, 2, null);

    Set<InstrumentType> result = subject.getDistinctChordVoicingTypes();

    assertEquals(Set.of(InstrumentType.Bass, InstrumentType.Sticky, InstrumentType.Stripe), result);
  }


  /**
   Choose next Macro program based on the memes of the last sequence from the previous Macro program https://github.com/xjmusic/workstation/issues/299
   */
  @Test
  public void getType() throws NexusException, FabricationFatalException {
    var chain = store.put(buildChain(fake.project1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE));
    Segment previousSegment = store.put(buildSegment(chain, 1, SegmentState.CRAFTED, "F major", 8, 0.6f, 120.0f, "seg123"));
    var previousMacroChoice = // second-to-last sequence of macro program
      store.put(buildSegmentChoice(previousSegment, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program4, fake.program4_sequence1_binding0));
    var previousMainChoice = // last sequence of main program
      store.put(buildSegmentChoice(previousSegment, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program5, fake.program5_sequence1_binding0));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, "G major", 8, 0.6f, 240.0f, "seg123"));
    when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
    when(mockRetrospective.getPreviousChoiceOfType(ProgramType.Main)).thenReturn(Optional.of(previousMainChoice));
    when(mockRetrospective.getPreviousChoiceOfType(ProgramType.Macro)).thenReturn(Optional.of(previousMacroChoice));
    subject = new FabricatorImpl(mockFabricatorFactory, store, sourceMaterial, segment.getId(), mockJsonapiPayloadFactory, jsonProvider, 48000.0f, 2, null);

    var result = subject.getType();

    assertEquals(SegmentType.NEXT_MACRO, result);
  }

  // FUTURE: test getChoicesOfPreviousSegments

  @Test
  public void getMemeIsometryOfNextSequenceInPreviousMacro() throws NexusException, FabricationFatalException {
    var chain = store.put(buildChain(fake.project1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE));
    Segment previousSegment = store.put(buildSegment(chain, 1, SegmentState.CRAFTED, "F major", 8, 0.6f, 120.0f, "seg123"));
    var previousMacroChoice = // second-to-last sequence of macro program
      store.put(buildSegmentChoice(previousSegment, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program4, fake.program4_sequence1_binding0));
    store.put(buildSegmentChoice(previousSegment, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program5, fake.program5_sequence1_binding0));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, "G major", 8, 0.6f, 240.0f, "seg123"));
    when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
    when(mockRetrospective.getPreviousChoiceOfType(ProgramType.Macro)).thenReturn(Optional.of(previousMacroChoice));
    subject = new FabricatorImpl(mockFabricatorFactory, store, sourceMaterial, segment.getId(), mockJsonapiPayloadFactory, jsonProvider, 48000.0f, 2, null);

    var result = subject.getMemeIsometryOfNextSequenceInPreviousMacro();

    assertArrayEquals(new String[]{"COZY", "TROPICAL"}, result.getSources().stream().sorted().toArray());
  }

  @Test
  public void getChordAt() throws NexusException, FabricationFatalException, ValueException {
    var chain = store.put(buildChain(fake.project1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, "G major", 8, 0.6f, 240.0f, "seg123"));
    when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
    subject = new FabricatorImpl(mockFabricatorFactory, store, sourceMaterial, segment.getId(), mockJsonapiPayloadFactory, jsonProvider, 48000.0f, 2, null);
    subject.put(buildSegmentChord(segment, 0.0f, "C"), false);
    subject.put(buildSegmentChord(segment, 2.0f, "F"), false);
    subject.put(buildSegmentChord(segment, 5.5f, "Gm"), false);

    assertEquals("C", subject.getChordAt(0.0).orElseThrow().getName());
    assertEquals("C", subject.getChordAt(1.0).orElseThrow().getName());
    assertEquals("F", subject.getChordAt(2.0).orElseThrow().getName());
    assertEquals("F", subject.getChordAt(3.0).orElseThrow().getName());
    assertEquals("F", subject.getChordAt(5.0).orElseThrow().getName());
    assertEquals("Gm", subject.getChordAt(5.5).orElseThrow().getName());
    assertEquals("Gm", subject.getChordAt(6.0).orElseThrow().getName());
    assertEquals("Gm", subject.getChordAt(7.5).orElseThrow().getName());
  }

  @Test
  public void computeProgramRange() throws NexusException, FabricationFatalException, ValueException {
    var chain = store.put(buildChain(fake.project1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, "G major", 8, 0.6f, 240.0f, "seg123"));
    when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
    var program = buildProgram(ProgramType.Detail, "C", 120.0f);
    var voice = buildVoice(program, InstrumentType.Bass);
    var track = buildTrack(voice);
    var sequence = buildSequence(program, 4);
    var pattern = buildPattern(sequence, voice, 4);
    sourceMaterial = new HubContent(List.of(program, voice, track, sequence, pattern, fake.template1, fake.templateBinding1, buildEvent(pattern, track, 0.0f, 1.0f, "C1"), buildEvent(pattern, track, 1.0f, 1.0f, "D2")));
    subject = new FabricatorImpl(mockFabricatorFactory, store, sourceMaterial, segment.getId(), mockJsonapiPayloadFactory, jsonProvider, 48000.0f, 2, null);

    var result = subject.getProgramRange(program.getId(), InstrumentType.Bass);

    assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
    assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
  }

  @Test
  public void computeProgramRange_ignoresAtonalNotes() throws NexusException, FabricationFatalException, ValueException {
    var chain = store.put(buildChain(fake.project1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, "G major", 8, 0.6f, 240.0f, "seg123"));
    when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
    var program = buildProgram(ProgramType.Detail, "C", 120.0f);
    var voice = buildVoice(program, InstrumentType.Bass);
    var track = buildTrack(voice);
    var sequence = buildSequence(program, 4);
    var pattern = buildPattern(sequence, voice, 4);
    sourceMaterial = new HubContent(List.of(program, voice, track, sequence, pattern, buildEvent(pattern, track, 0.0f, 1.0f, "C1"), buildEvent(pattern, track, 1.0f, 1.0f, "X"), buildEvent(pattern, track, 2.0f, 1.0f, "D2"), fake.template1, fake.templateBinding1));
    subject = new FabricatorImpl(mockFabricatorFactory, store, sourceMaterial, segment.getId(), mockJsonapiPayloadFactory, jsonProvider, 48000.0f, 2, null);

    var result = subject.getProgramRange(program.getId(), InstrumentType.Bass);

    assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
    assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
  }

  @Test
  public void getProgramSequence_fromSequence() throws NexusException, FabricationFatalException, ValueException {
    var project1 = buildProject("fish");
    Template template1 = buildTemplate(project1, "Test Template 1", "test1");
    var chain = store.put(NexusIntegrationTestingFixtures.buildChain(template1));
    segment = store.put(buildSegment(chain, SegmentType.CONTINUE, 17, 4, SegmentState.CRAFTED, "D major", SEQUENCE_TOTAL_BEATS, 0.73f, 120.0f, String.format("chains-%s-segments-%s", ChainUtils.getIdentifier(chain), 17), true));
    SegmentChoice choice = store.put(buildSegmentChoice(segment, ProgramType.Main, fake.program5_sequence0));
    when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
    sourceMaterial = new HubContent(List.of(fake.program5_sequence0, fake.template1, fake.templateBinding1));
    subject = new FabricatorImpl(mockFabricatorFactory, store, sourceMaterial, segment.getId(), mockJsonapiPayloadFactory, jsonProvider, 48000.0f, 2, null);

    var result = subject.getProgramSequence(choice);

    assertEquals(fake.program5_sequence0.getId(), result.orElseThrow().getId());
  }

  @Test
  public void getProgramSequence_fromSequenceBinding() throws NexusException, FabricationFatalException, ValueException {
    var project1 = buildProject("fish");
    Template template1 = buildTemplate(project1, "Test Template 1", "test1");
    var chain = store.put(NexusIntegrationTestingFixtures.buildChain(template1));
    segment = store.put(buildSegment(chain, SegmentType.CONTINUE, 17, 4, SegmentState.CRAFTED, "D major", SEQUENCE_TOTAL_BEATS, 0.73f, 120.0f, String.format("chains-%s-segments-%s", ChainUtils.getIdentifier(chain), 17), true));
    SegmentChoice choice = store.put(buildSegmentChoice(segment, ProgramType.Main, fake.program5_sequence0_binding0));
    when(mockFabricatorFactory.loadRetrospective(any())).thenReturn(mockRetrospective);
    sourceMaterial = new HubContent(List.of(fake.program5_sequence0, fake.program5_sequence0_binding0, fake.template1, fake.templateBinding1));
    subject = new FabricatorImpl(mockFabricatorFactory, store, sourceMaterial, segment.getId(), mockJsonapiPayloadFactory, jsonProvider, 48000.0f, 2, null);

    var result = subject.getProgramSequence(choice);

    assertEquals(fake.program5_sequence0.getId(), result.orElseThrow().getId());
  }

  /**
   Sticky buns v2 use slash root when available https://github.com/xjmusic/workstation/issues/231
   */
  @Test
  public void getRootNote() {
    var result = subject.getRootNoteMidRange("C3,E3,G3,A#3,C4,E4,G4", Chord.of("Cm")).orElseThrow();
    assertEquals(PitchClass.C, result.getPitchClass());
    assertEquals(4, result.getOctave().intValue());
  }

  /**
   Should add meme from ALL program and instrument types! https://github.com/xjmusic/workstation/issues/210
   */
  @Test
  public void put_addsMemesForChoice() throws NexusException {
    subject.put(buildSegmentChoice(segment, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program9, fake.program9_voice0, fake.instrument8), false);
    subject.put(buildSegmentChoice(segment, Segment.DELTA_UNLIMITED, Segment.DELTA_UNLIMITED, fake.program4, fake.program4_sequence1_binding0), false);

    var resultMemes = store.readAll(segment.getId(), SegmentMeme.class).stream().sorted(Comparator.comparing(SegmentMeme::getName)).toList();
    assertEquals("BASIC", (resultMemes.get(0)).getName());
    assertEquals("COZY", (resultMemes.get(1)).getName());
    assertEquals("HEAVY", (resultMemes.get(2)).getName());
    assertEquals("TROPICAL", (resultMemes.get(3)).getName());
    assertEquals("WILD", (resultMemes.get(4)).getName());
    var resultChoices = store.readAll(segment.getId(), SegmentChoice.class).stream().sorted(Comparator.comparing(SegmentChoice::getProgramType)).toList();
    assertEquals(fake.program4.getId(), (resultChoices.get(0)).getProgramId());
    assertEquals(fake.program4_sequence1_binding0.getId(), (resultChoices.get(0)).getProgramSequenceBindingId());
    assertEquals(fake.instrument8.getId(), (resultChoices.get(1)).getInstrumentId());
  }

  /**
   Unit test behavior of choosing an event for a note in a detail program
   <p>
   Sticky bun note choices should persist into following segments https://github.com/xjmusic/workstation/issues/281
   */
  @Test
  public void getStickyBun_readMetaFromCurrentSegment() throws JsonProcessingException, NexusException {
    var bun = new StickyBun(fake.program9_sequence0_pattern0_event0.getId(), 3);
    var bunJson = jsonProvider.getMapper().writeValueAsString(bun);
    var bunKey = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event0.getId());
    store.put(buildSegmentMeta(segment, bunKey, bunJson));

    var result = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.getId()).orElseThrow();

    assertEquals(fake.program9_sequence0_pattern0_event0.getId(), result.getEventId());
    assertArrayEquals(bun.getValues().toArray(), result.getValues().toArray());
  }

  /**
   Unit test behavior of choosing an event for a note in a detail program
   <p>
   Sticky bun note choices should persist into following segments https://github.com/xjmusic/workstation/issues/281
   */
  @Test
  public void getStickyBun_readMetaFromPreviousSegment() throws JsonProcessingException {
    var bun = new StickyBun(fake.program9_sequence0_pattern0_event0.getId(), 3);
    var bunJson = jsonProvider.getMapper().writeValueAsString(bun);
    var bunKey = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event0.getId());
    var bunMeta = buildSegmentMeta(segment, bunKey, bunJson);
    when(mockRetrospective.getPreviousMeta(eq(bunKey))).thenReturn(Optional.of(bunMeta));

    var result = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.getId()).orElseThrow();

    assertEquals(fake.program9_sequence0_pattern0_event0.getId(), result.getEventId());
    assertArrayEquals(bun.getValues().toArray(), result.getValues().toArray());
  }

  /**
   Unit test behavior of choosing a different events for a series of X notes in a detail program
   <p>
   Sticky bun note choices should persist into following segments https://github.com/xjmusic/workstation/issues/281
   */
  @Test
  public void getStickyBun_createForEvent() {
    var result = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.getId()).orElseThrow();

    assertEquals(fake.program9_sequence0_pattern0_event0.getId(), result.getEventId());
  }

  /**
   Unit test behavior of choosing an event for a note in a detail program
   <p>
   Sticky bun note choices should persist into following segments https://github.com/xjmusic/workstation/issues/281
   */
  @Test
  public void getStickyBun_multipleEventsPickedSeparately() throws JsonProcessingException, NexusException {
    var bun0 = new StickyBun(fake.program9_sequence0_pattern0_event0.getId(), 3);
    var bunJson0 = jsonProvider.getMapper().writeValueAsString(bun0);
    var bunKey0 = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event0.getId());
    store.put(buildSegmentMeta(segment, bunKey0, bunJson0));
    var bun1 = new StickyBun(fake.program9_sequence0_pattern0_event1.getId(), 3);
    var bunJson1 = jsonProvider.getMapper().writeValueAsString(bun1);
    var bunKey1 = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event1.getId());
    store.put(buildSegmentMeta(segment, bunKey1, bunJson1));

    var result0 = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.getId()).orElseThrow();
    var result1 = subject.getStickyBun(fake.program9_sequence0_pattern0_event1.getId()).orElseThrow();

    assertEquals(fake.program9_sequence0_pattern0_event0.getId(), result0.getEventId());
    assertArrayEquals(bun0.getValues().toArray(), result0.getValues().toArray());
    assertEquals(fake.program9_sequence0_pattern0_event1.getId(), result1.getEventId());
    assertArrayEquals(bun1.getValues().toArray(), result1.getValues().toArray());
  }

  @Test
  public void getMemeTaxonomy() {
    var result = subject.getMemeTaxonomy();

    assertEquals(2, result.getCategories().size());
    assertEquals("COLOR", result.getCategories().get(0).getName());
    assertEquals("SEASON", result.getCategories().get(1).getName());
  }
}
