// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.api.*;
import io.xj.hub.HubTopology;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.music.Note;
import io.xj.lib.music.Tuning;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientException;
import io.xj.nexus.hub_client.client.HubClientModule;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.*;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.IntegrationTestingFixtures.*;
import static io.xj.nexus.NexusIntegrationTestingFixtures.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 FUTURE: [#170035559] Split the FabricatorImplTest into separate tests of the FabricatorImpl, SegmentWorkbenchImpl, SegmentRetrospectiveImpl, and IngestImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class FabricatorImplTest {
  @Mock
  public Environment env;
  @Mock
  public FabricatorFactory mockFabricatorFactory;
  @Mock
  public TimeComputer mockTimeComputer;
  @Mock
  public SegmentWorkbench mockSegmentWorkbench;
  @Mock
  public SegmentRetrospective mockSegmentRetrospective;
  @Mock
  public Tuning mockTuning;
  @Mock
  public HubClient mockHubClient;
  @Mock
  public ChainManager mockChainManager;
  @Mock
  public SegmentManager mockSegmentManager;
  @Mock
  public JsonapiPayloadFactory mockJsonapiPayloadFactory;
  private FabricatorImpl subject;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    var injector = Guice.createInjector(Modules.override(new FileStoreModule(), new NexusPersistenceModule(), new HubClientModule(), new NexusPersistenceModule(), new MixerModule(), new JsonapiModule(), new NexusWorkModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Environment.class).toInstance(env);
          bind(Tuning.class).toInstance(mockTuning);
          bind(HubClient.class).toInstance(mockHubClient);
          bind(FabricatorFactory.class).toInstance(mockFabricatorFactory);
          bind(TimeComputer.class).toInstance(mockTimeComputer);
          bind(SegmentWorkbench.class).toInstance(mockSegmentWorkbench);
          bind(SegmentRetrospective.class).toInstance(mockSegmentRetrospective);
        }
      }));
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream(),
      fake.setupFixtureB3().stream()
    ).collect(Collectors.toList()));
  }

  @Test
  public void usesTimeComputer() throws Exception {
    buildTemplateBinding(fake.template1, fake.library2);
    var chain = store.put(buildChain(
      fake.account1,
      fake.template1,
      "test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2017-12-12T01:00:08.000000Z")));
    Segment previousSegment = store.put(buildSegment(
      chain,
      1,
      SegmentState.CRAFTED,
      Instant.parse("2017-12-12T01:00:08.000000Z"),
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      "F major",
      8,
      0.6,
      120.0,
      "seg123",
      "ogg"));
    Segment segment = store.put(buildSegment(
      chain,
      2,
      SegmentState.CRAFTING,
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      Instant.parse("2017-12-12T01:00:22.000000Z"),
      "G major",
      8,
      0.6,
      240.0,
      "seg123",
      "ogg"));
    when(mockFabricatorFactory.createTimeComputer(anyDouble(), anyDouble(), anyDouble()))
      .thenReturn(mockTimeComputer);
    when(mockTimeComputer.getSecondsAtPosition(anyDouble()))
      .thenReturn(Double.valueOf(0));
    when(mockFabricatorFactory.loadRetrospective(any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentRetrospective.getPreviousSegment())
      .thenReturn(java.util.Optional.ofNullable(previousSegment));
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory);

    Double result = subject.getSecondsAtPosition(0); // instantiates a time computer; see expectation above

    assertEquals(Double.valueOf(0), result);
    verify(mockFabricatorFactory).createTimeComputer(8.0, 120, 120.0);
  }

  /**
   Instrument has overall volume parameter #179215413
   */
  @Test
  public void computeAudioVolume() throws Exception {
    buildTemplateBinding(fake.template1, fake.library2);
    var chain = store.put(buildChain(
      fake.account1,
      fake.template1,
      "test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2017-12-12T01:00:08.000000Z")));
    Segment previousSegment = store.put(buildSegment(
      chain,
      1,
      SegmentState.CRAFTED,
      Instant.parse("2017-12-12T01:00:08.000000Z"),
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      "F major",
      8,
      0.6,
      120.0,
      "seg123",
      "ogg"));
    Segment segment = store.put(buildSegment(
      chain,
      2,
      SegmentState.CRAFTING,
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      Instant.parse("2017-12-12T01:00:22.000000Z"),
      "G major",
      8,
      0.6,
      240.0,
      "seg123",
      "ogg"));
    var choice = store.put(buildSegmentChoice(segment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program9, fake.program9_voice0, fake.instrument8));
    var arrangement = store.put(buildSegmentChoiceArrangement(choice));
    var pick = store.put(buildSegmentChoiceArrangementPick(arrangement, fake.program9_sequence0_pattern0_event0, fake.instrument8_audio8kick, "KICK"));
    when(mockFabricatorFactory.loadRetrospective(any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory);

    double result = subject.computeAudioVolume(pick); // instantiates a time computer; see expectation above

    assertEquals(0.76, result, 0.01);
  }

  @Test
  public void pick_returned_by_picks() throws Exception {
    buildTemplateBinding(fake.template1, fake.library2);
    var chain = store.put(buildChain(
      fake.account1,
      fake.template1,
      "test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2017-12-12T01:00:08.000000Z")));
    store.put(buildSegment(
      chain,
      1,
      SegmentState.CRAFTED,
      Instant.parse("2017-12-12T01:00:08.000000Z"),
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      "F major",
      8,
      0.6,
      120.0,
      "seg123",
      "ogg"));
    Segment segment = store.put(buildSegment(
      chain,
      2,
      SegmentState.CRAFTING,
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      Instant.parse("2017-12-12T01:00:22.000000Z"),
      "G major",
      8,
      0.6,
      240.0,
      "seg123",
      "ogg"));
    store.put(buildSegmentChord(segment, 0.0, "A"));
    store.put(buildSegmentChoice(
      segment,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program5));
    SegmentChoice beatChoice = store.put(buildSegmentChoice(
      segment,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program35,
      fake.program35_voice0,
      fake.instrument8));
    SegmentChoiceArrangement beatArrangement = store.put(buildSegmentChoiceArrangement(beatChoice));
    SegmentChoiceArrangementPick beatPick = store.put(
      new SegmentChoiceArrangementPick()
        .id(UUID.randomUUID())
        .segmentId(beatArrangement.getSegmentId())
        .segmentChoiceArrangementId(beatArrangement.getId())
        .programSequencePatternEventId(fake.program35_sequence0_pattern0_event0.getId())
        .instrumentAudioId(fake.instrument8_audio8kick.getId())
        .event("CLANG")
        .start(0.273)
        .length(1.571)
        .amplitude(0.8)
        .note("A4"));
    when(mockFabricatorFactory.loadRetrospective(any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentWorkbench.getSegmentChoiceArrangementPicks())
      .thenReturn(ImmutableList.of(beatPick));
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory);

    Collection<SegmentChoiceArrangementPick> result = subject.getPicks();

    SegmentChoiceArrangementPick resultPick = result.iterator().next();
    assertEquals(beatArrangement.getId(), resultPick.getSegmentChoiceArrangementId());
    assertEquals(fake.instrument8_audio8kick.getId(), resultPick.getInstrumentAudioId());
    assertEquals(0.273, resultPick.getStart(), 0.001);
    assertEquals(1.571, resultPick.getLength(), 0.001);
    assertEquals(0.8, resultPick.getAmplitude(), 0.1);
    assertEquals("A4", resultPick.getNote());
  }


  @Test
  public void getDistinctChordVoicingTypes() throws Exception {
    sourceMaterial = new HubContent(Streams.concat(
      fake.setupFixtureB1().stream(),
      fake.setupFixtureB2().stream(),
      fake.setupFixtureB3().stream(),
      Stream.of(
        buildVoicing(InstrumentType.Sticky, fake.program5_sequence0_chord0, "G4, B4, D4"),
        buildVoicing(InstrumentType.Stripe, fake.program5_sequence0_chord0, "F5"),
        buildVoicing(InstrumentType.Pad, fake.program5_sequence0_chord0, "(None)") // No voicing notes- doesn't count!
      )
    ).collect(Collectors.toList()));
    store.put(buildTemplateBinding(fake.template1, fake.library2));
    var chain = store.put(buildChain(
      fake.account1,
      fake.template1,
      "test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2017-12-12T01:00:08.000000Z")));
    var segment = store.put(buildSegment(
      chain,
      0,
      SegmentState.CRAFTING,
      Instant.parse("2017-12-12T01:00:08.000000Z"),
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      "F major",
      8,
      0.6,
      120.0,
      "seg123",
      "ogg"));
    SegmentChoice mainChoice = store.put(buildSegmentChoice(
      segment,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program5));
    when(mockFabricatorFactory.loadRetrospective(any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentWorkbench.getChoiceOfType(ProgramType.Main))
      .thenReturn(Optional.of(mainChoice));
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory);

    Set<InstrumentType> result = subject.getDistinctChordVoicingTypes();

    assertEquals(Set.of(
      InstrumentType.Bass,
      InstrumentType.Sticky,
      InstrumentType.Stripe
    ), result);
  }


  /**
   [#176728582] Choose next Macro program based on the memes of the last sequence from the previous Macro program
   */
  @Test
  public void getType() throws NexusException, ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException {
    var chain = store.put(buildChain(
      fake.account1,
      fake.template1,
      "test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2017-12-12T01:00:08.000000Z")));
    Segment previousSegment = store.put(buildSegment(
      chain,
      1,
      SegmentState.CRAFTED,
      Instant.parse("2017-12-12T01:00:08.000000Z"),
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      "F major",
      8,
      0.6,
      120.0,
      "seg123",
      "ogg"));
    var previousMacroChoice = // second-to-last sequence of macro program
      store.put(buildSegmentChoice(
        previousSegment,
        Segments.DELTA_UNLIMITED,
        Segments.DELTA_UNLIMITED,
        fake.program4,
        fake.program4_sequence1_binding0));
    var previousMainChoice = // last sequence of main program
      store.put(buildSegmentChoice(
        previousSegment,
        Segments.DELTA_UNLIMITED,
        Segments.DELTA_UNLIMITED,
        fake.program5,
        fake.program5_sequence1_binding0));
    Segment segment = store.put(buildSegment(
      chain,
      2,
      SegmentState.CRAFTING,
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      Instant.parse("2017-12-12T01:00:22.000000Z"),
      "G major",
      8,
      0.6,
      240.0,
      "seg123",
      "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentRetrospective.getPreviousChoiceOfType(ProgramType.Main))
      .thenReturn(Optional.of(previousMainChoice));
    when(mockSegmentRetrospective.getPreviousChoiceOfType(ProgramType.Macro))
      .thenReturn(Optional.of(previousMacroChoice));
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory);

    var result = subject.getType();

    assertEquals(SegmentType.NEXTMACRO, result);
  }

  // FUTURE: test getChoicesOfPreviousSegments

  @Test
  public void getMemeIsometryOfNextSequenceInPreviousMacro() throws NexusException, ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException {
    var chain = store.put(buildChain(
      fake.account1,
      fake.template1,
      "test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2017-12-12T01:00:08.000000Z")));
    Segment previousSegment = store.put(buildSegment(
      chain,
      1,
      SegmentState.CRAFTED,
      Instant.parse("2017-12-12T01:00:08.000000Z"),
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      "F major",
      8,
      0.6,
      120.0,
      "seg123",
      "ogg"));
    var previousMacroChoice = // second-to-last sequence of macro program
      store.put(buildSegmentChoice(
        previousSegment,
        Segments.DELTA_UNLIMITED,
        Segments.DELTA_UNLIMITED,
        fake.program4,
        fake.program4_sequence1_binding0));
    store.put(buildSegmentChoice(
      previousSegment,
      Segments.DELTA_UNLIMITED,
      Segments.DELTA_UNLIMITED,
      fake.program5,
      fake.program5_sequence1_binding0));
    Segment segment = store.put(buildSegment(
      chain,
      2,
      SegmentState.CRAFTING,
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      Instant.parse("2017-12-12T01:00:22.000000Z"),
      "G major",
      8,
      0.6,
      240.0,
      "seg123",
      "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentRetrospective.getPreviousChoiceOfType(ProgramType.Macro))
      .thenReturn(Optional.of(previousMacroChoice));
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory);

    var result = subject.getMemeIsometryOfNextSequenceInPreviousMacro();

    assertArrayEquals(new String[]{"COZY", "TROPICAL"}, result.getSources().stream().sorted().toArray());
  }

  @Test
  public void getChordAt() throws NexusException, ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException {
    var chain = store.put(buildChain(
      fake.account1,
      fake.template1,
      "test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2017-12-12T01:00:08.000000Z")));
    Segment segment = store.put(buildSegment(
      chain,
      2,
      SegmentState.CRAFTING,
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      Instant.parse("2017-12-12T01:00:22.000000Z"),
      "G major",
      8,
      0.6,
      240.0,
      "seg123",
      "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegmentChords())
      .thenReturn(ImmutableList.of(
        buildSegmentChord(segment, 0.0, "C"),
        buildSegmentChord(
          segment, 2.0, "F"),
        buildSegmentChord(
          segment, 5.5, "Gm")));
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory);

    assertEquals("C", subject.getChordAt(0).orElseThrow().getName());
    assertEquals("C", subject.getChordAt(1).orElseThrow().getName());
    assertEquals("F", subject.getChordAt(2).orElseThrow().getName());
    assertEquals("F", subject.getChordAt(3).orElseThrow().getName());
    assertEquals("F", subject.getChordAt(5).orElseThrow().getName());
    assertEquals("Gm", subject.getChordAt(5.5).orElseThrow().getName());
    assertEquals("Gm", subject.getChordAt(6).orElseThrow().getName());
    assertEquals("Gm", subject.getChordAt(7.5).orElseThrow().getName());
  }

  @Test
  public void computeProgramRange() throws NexusException, ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException, HubClientException {
    var chain = store.put(buildChain(
      fake.account1,
      fake.template1,
      "test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2017-12-12T01:00:08.000000Z")));
    Segment segment = store.put(buildSegment(
      chain,
      2,
      SegmentState.CRAFTING,
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      Instant.parse("2017-12-12T01:00:22.000000Z"),
      "G major",
      8,
      0.6,
      240.0,
      "seg123",
      "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    var program = buildProgram(ProgramType.Detail, "C", 120.0f, 1.0f);
    var voice = buildVoice(program, InstrumentType.Bass);
    var track = buildTrack(voice);
    var sequence = buildSequence(program, 4);
    var pattern = buildPattern(sequence, voice, 4);
    sourceMaterial = new HubContent(ImmutableList.of(
      program,
      voice,
      track,
      sequence,
      pattern,
      fake.template1,
      fake.templateBinding1,
      buildEvent(pattern, track, 0.0f, 1.0f, "C1"),
      buildEvent(pattern, track, 1.0f, 1.0f, "D2")
    ));
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory);

    var result = subject.getProgramRange(program.getId(), InstrumentType.Bass);

    assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
    assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
  }

  @Test
  public void computeProgramRange_ignoresAtonalNotes() throws NexusException, ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException, HubClientException {
    var chain = store.put(buildChain(
      fake.account1,
      fake.template1,
      "test",
      ChainType.PRODUCTION,
      ChainState.FABRICATE,
      Instant.parse("2017-12-12T01:00:08.000000Z")));
    Segment segment = store.put(buildSegment(
      chain,
      2,
      SegmentState.CRAFTING,
      Instant.parse("2017-12-12T01:00:16.000000Z"),
      Instant.parse("2017-12-12T01:00:22.000000Z"),
      "G major",
      8,
      0.6,
      240.0,
      "seg123",
      "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    var program = buildProgram(ProgramType.Detail, "C", 120.0f, 1.0f);
    var voice = buildVoice(program, InstrumentType.Bass);
    var track = buildTrack(voice);
    var sequence = buildSequence(program, 4);
    var pattern = buildPattern(sequence, voice, 4);
    sourceMaterial = new HubContent(ImmutableList.of(
      program,
      voice,
      track,
      sequence,
      pattern,
      buildEvent(pattern, track, 0.0f, 1.0f, "C1"),
      buildEvent(pattern, track, 1.0f, 1.0f, "X"),
      buildEvent(pattern, track, 2.0f, 1.0f, "D2"),
      fake.template1,
      fake.templateBinding1
    ));
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory);

    var result = subject.getProgramRange(program.getId(), InstrumentType.Bass);

    assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
    assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
  }
}
