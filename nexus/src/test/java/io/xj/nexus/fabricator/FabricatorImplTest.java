// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.fabricator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.hub.HubTopology;
import io.xj.hub.client.HubClient;
import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubClientModule;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Note;
import io.xj.lib.music.StickyBun;
import io.xj.lib.music.Tuning;
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
import io.xj.nexus.model.SegmentMeta;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.Chains;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusPersistenceModule;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.Segments;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildEvent;
import static io.xj.hub.IntegrationTestingFixtures.buildPattern;
import static io.xj.hub.IntegrationTestingFixtures.buildProgram;
import static io.xj.hub.IntegrationTestingFixtures.buildSequence;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.hub.IntegrationTestingFixtures.buildTrack;
import static io.xj.hub.IntegrationTestingFixtures.buildVoice;
import static io.xj.hub.IntegrationTestingFixtures.buildVoicing;
import static io.xj.lib.music.NoteTest.assertNote;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoice;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoiceArrangement;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChoiceArrangementPick;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentChord;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegmentMeta;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 FUTURE: https://www.pivotaltracker.com/story/show/170035559 Split the FabricatorImplTest into separate tests of the FabricatorImpl, SegmentWorkbenchImpl, SegmentRetrospectiveImpl, and IngestImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class FabricatorImplTest {
  static int SEQUENCE_TOTAL_BEATS = 64;
  @Mock
  public Environment env;
  @Mock
  public FabricatorFactory mockFabricatorFactory;
  @Mock
  public SegmentWorkbench mockSegmentWorkbench;
  @Mock
  public SegmentRetrospective mockRetrospective;
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
  public JsonProvider jsonProvider;
  @Captor
  ArgumentCaptor<Object> entityCaptor;
  private FabricatorImpl subject;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;
  private Segment segment;

  @Before
  public void setUp() throws Exception {
    var injector = Guice.createInjector(Modules.override(new FileStoreModule(), new NexusPersistenceModule(), new HubClientModule(), new NexusPersistenceModule(), new MixerModule(), new JsonapiModule(), new NexusWorkModule()).with(new AbstractModule() {
      @Override
      public void configure() {
        bind(Environment.class).toInstance(env);
        bind(Tuning.class).toInstance(mockTuning);
        bind(HubClient.class).toInstance(mockHubClient);
        bind(FabricatorFactory.class).toInstance(mockFabricatorFactory);
        bind(SegmentWorkbench.class).toInstance(mockSegmentWorkbench);
        bind(SegmentRetrospective.class).toInstance(mockRetrospective);
      }
    }));
    jsonProvider = injector.getInstance(JsonProvider.class);
    var entityFactory = injector.getInstance(EntityFactory.class);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    sourceMaterial = new HubContent(Streams.concat(fake.setupFixtureB1().stream(), fake.setupFixtureB2().stream(), fake.setupFixtureB3().stream(), Stream.of(fake.template1, fake.templateBinding1)).collect(Collectors.toList()));

    // Here's a basic setup that can be replaced for complex tests
    var chain = store.put(buildChain(fake.account1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2017-12-12T01:00:08.000000Z")));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, Instant.parse("2017-12-12T01:00:16.000000Z"), Instant.parse("2017-12-12T01:00:22.000000Z"), "G major", 8, 0.6, 240.0, "seg123", "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);
  }

  /**
   Instrument has overall volume parameter https://www.pivotaltracker.com/story/show/179215413
   */
  @Test
  public void computeAudioVolume() throws Exception {
    buildTemplateBinding(fake.template1, fake.library2);
    var chain = store.put(buildChain(fake.account1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2017-12-12T01:00:08.000000Z")));
    store.put(buildSegment(chain, 1, SegmentState.CRAFTED, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120.0, "seg123", "ogg"));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, Instant.parse("2017-12-12T01:00:16.000000Z"), Instant.parse("2017-12-12T01:00:22.000000Z"), "G major", 8, 0.6, 240.0, "seg123", "ogg"));
    var choice = store.put(buildSegmentChoice(segment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program9, fake.program9_voice0, fake.instrument8));
    var arrangement = store.put(buildSegmentChoiceArrangement(choice));
    var pick = store.put(buildSegmentChoiceArrangementPick(arrangement, fake.program9_sequence0_pattern0_event0, fake.instrument8_audio8kick, "KICK"));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);

    double result = subject.getAudioVolume(pick); // instantiates a time computer; see expectation above

    assertEquals(0.76, result, 0.01);
  }

  @Test
  public void pick_returned_by_picks() throws Exception {
    buildTemplateBinding(fake.template1, fake.library2);
    var chain = store.put(buildChain(fake.account1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2017-12-12T01:00:08.000000Z")));
    store.put(buildSegment(chain, 1, SegmentState.CRAFTED, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120.0, "seg123", "ogg"));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, Instant.parse("2017-12-12T01:00:16.000000Z"), Instant.parse("2017-12-12T01:00:22.000000Z"), "G major", 8, 0.6, 240.0, "seg123", "ogg"));
    store.put(buildSegmentChord(segment, 0.0, "A"));
    store.put(buildSegmentChoice(segment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program5));
    SegmentChoice beatChoice = store.put(buildSegmentChoice(segment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program35, fake.program35_voice0, fake.instrument8));
    SegmentChoiceArrangement beatArrangement = store.put(buildSegmentChoiceArrangement(beatChoice));
    SegmentChoiceArrangementPick beatPick = store.put(new SegmentChoiceArrangementPick().id(UUID.randomUUID()).segmentId(beatArrangement.getSegmentId()).segmentChoiceArrangementId(beatArrangement.getId()).programSequencePatternEventId(fake.program35_sequence0_pattern0_event0.getId()).instrumentAudioId(fake.instrument8_audio8kick.getId()).event("CLANG").start(0.273).length(1.571).amplitude(0.8).tones("A4"));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockSegmentWorkbench.getSegmentChoiceArrangementPicks()).thenReturn(ImmutableList.of(beatPick));
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);

    Collection<SegmentChoiceArrangementPick> result = subject.getPicks();

    SegmentChoiceArrangementPick resultPick = result.iterator().next();
    assertEquals(beatArrangement.getId(), resultPick.getSegmentChoiceArrangementId());
    assertEquals(fake.instrument8_audio8kick.getId(), resultPick.getInstrumentAudioId());
    assertEquals(0.273, resultPick.getStart(), 0.001);
    assertEquals(1.571, resultPick.getLength(), 0.001);
    assertEquals(0.8, resultPick.getAmplitude(), 0.1);
    assertEquals("A4", resultPick.getTones());
  }


  @Test
  public void getDistinctChordVoicingTypes() throws Exception {
    sourceMaterial = new HubContent(Streams.concat(fake.setupFixtureB1().stream(), fake.setupFixtureB2().stream(), fake.setupFixtureB3().stream(), Stream.of(buildVoicing(fake.program5_sequence0_chord0, fake.program5_voiceSticky, "G4, B4, D4"), buildVoicing(fake.program5_sequence0_chord0, fake.program5_voiceStripe, "F5"), buildVoicing(fake.program5_sequence0_chord0, fake.program5_voicePad, "(None)") // No voicing notes- doesn't count!
    )).collect(Collectors.toList()));
    store.put(buildTemplateBinding(fake.template1, fake.library2));
    var chain = store.put(buildChain(fake.account1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2017-12-12T01:00:08.000000Z")));
    segment = store.put(buildSegment(chain, 0, SegmentState.CRAFTING, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120.0, "seg123", "ogg"));
    SegmentChoice mainChoice = store.put(buildSegmentChoice(segment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program5));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockSegmentWorkbench.getChoiceOfType(ProgramType.Main)).thenReturn(Optional.of(mainChoice));
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);

    Set<InstrumentType> result = subject.getDistinctChordVoicingTypes();

    assertEquals(Set.of(InstrumentType.Bass, InstrumentType.Sticky, InstrumentType.Stripe), result);
  }


  /**
   https://www.pivotaltracker.com/story/show/176728582 Choose next Macro program based on the memes of the last sequence from the previous Macro program
   */
  @Test
  public void getType() throws NexusException, ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException, FabricationFatalException {
    var chain = store.put(buildChain(fake.account1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2017-12-12T01:00:08.000000Z")));
    Segment previousSegment = store.put(buildSegment(chain, 1, SegmentState.CRAFTED, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120.0, "seg123", "ogg"));
    var previousMacroChoice = // second-to-last sequence of macro program
      store.put(buildSegmentChoice(previousSegment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program4, fake.program4_sequence1_binding0));
    var previousMainChoice = // last sequence of main program
      store.put(buildSegmentChoice(previousSegment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program5, fake.program5_sequence1_binding0));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, Instant.parse("2017-12-12T01:00:16.000000Z"), Instant.parse("2017-12-12T01:00:22.000000Z"), "G major", 8, 0.6, 240.0, "seg123", "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockRetrospective.getPreviousChoiceOfType(ProgramType.Main)).thenReturn(Optional.of(previousMainChoice));
    when(mockRetrospective.getPreviousChoiceOfType(ProgramType.Macro)).thenReturn(Optional.of(previousMacroChoice));
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);

    var result = subject.getType();

    assertEquals(SegmentType.NEXTMACRO, result);
  }

  // FUTURE: test getChoicesOfPreviousSegments

  @Test
  public void getMemeIsometryOfNextSequenceInPreviousMacro() throws NexusException, ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException, FabricationFatalException {
    var chain = store.put(buildChain(fake.account1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2017-12-12T01:00:08.000000Z")));
    Segment previousSegment = store.put(buildSegment(chain, 1, SegmentState.CRAFTED, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120.0, "seg123", "ogg"));
    var previousMacroChoice = // second-to-last sequence of macro program
      store.put(buildSegmentChoice(previousSegment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program4, fake.program4_sequence1_binding0));
    store.put(buildSegmentChoice(previousSegment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program5, fake.program5_sequence1_binding0));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, Instant.parse("2017-12-12T01:00:16.000000Z"), Instant.parse("2017-12-12T01:00:22.000000Z"), "G major", 8, 0.6, 240.0, "seg123", "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockRetrospective.getPreviousChoiceOfType(ProgramType.Macro)).thenReturn(Optional.of(previousMacroChoice));
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);

    var result = subject.getMemeIsometryOfNextSequenceInPreviousMacro();

    assertArrayEquals(new String[]{"COZY", "TROPICAL"}, result.getSources().stream().sorted().toArray());
  }

  @Test
  public void getChordAt() throws NexusException, ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException, FabricationFatalException {
    var chain = store.put(buildChain(fake.account1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2017-12-12T01:00:08.000000Z")));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, Instant.parse("2017-12-12T01:00:16.000000Z"), Instant.parse("2017-12-12T01:00:22.000000Z"), "G major", 8, 0.6, 240.0, "seg123", "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegmentChords()).thenReturn(ImmutableList.of(buildSegmentChord(segment, 0.0, "C"), buildSegmentChord(segment, 2.0, "F"), buildSegmentChord(segment, 5.5, "Gm")));
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);

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
  public void computeProgramRange() throws NexusException, ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException, HubClientException, FabricationFatalException {
    var chain = store.put(buildChain(fake.account1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2017-12-12T01:00:08.000000Z")));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, Instant.parse("2017-12-12T01:00:16.000000Z"), Instant.parse("2017-12-12T01:00:22.000000Z"), "G major", 8, 0.6, 240.0, "seg123", "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    var program = buildProgram(ProgramType.Detail, "C", 120.0f, 1.0f);
    var voice = buildVoice(program, InstrumentType.Bass);
    var track = buildTrack(voice);
    var sequence = buildSequence(program, 4);
    var pattern = buildPattern(sequence, voice, 4);
    sourceMaterial = new HubContent(ImmutableList.of(program, voice, track, sequence, pattern, fake.template1, fake.templateBinding1, buildEvent(pattern, track, 0.0f, 1.0f, "C1"), buildEvent(pattern, track, 1.0f, 1.0f, "D2")));
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);

    var result = subject.getProgramRange(program.getId(), InstrumentType.Bass);

    assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
    assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
  }

  @Test
  public void computeProgramRange_ignoresAtonalNotes() throws NexusException, ManagerPrivilegeException, ManagerFatalException, ManagerExistenceException, HubClientException, FabricationFatalException {
    var chain = store.put(buildChain(fake.account1, fake.template1, "test", ChainType.PRODUCTION, ChainState.FABRICATE, Instant.parse("2017-12-12T01:00:08.000000Z")));
    segment = store.put(buildSegment(chain, 2, SegmentState.CRAFTING, Instant.parse("2017-12-12T01:00:16.000000Z"), Instant.parse("2017-12-12T01:00:22.000000Z"), "G major", 8, 0.6, 240.0, "seg123", "ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    var program = buildProgram(ProgramType.Detail, "C", 120.0f, 1.0f);
    var voice = buildVoice(program, InstrumentType.Bass);
    var track = buildTrack(voice);
    var sequence = buildSequence(program, 4);
    var pattern = buildPattern(sequence, voice, 4);
    sourceMaterial = new HubContent(ImmutableList.of(program, voice, track, sequence, pattern, buildEvent(pattern, track, 0.0f, 1.0f, "C1"), buildEvent(pattern, track, 1.0f, 1.0f, "X"), buildEvent(pattern, track, 2.0f, 1.0f, "D2"), fake.template1, fake.templateBinding1));
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);

    var result = subject.getProgramRange(program.getId(), InstrumentType.Bass);

    assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
    assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
  }

  @Test
  public void getProgramSequence_fromSequence() throws NexusException, ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, HubClientException, FabricationFatalException {
    var account1 = buildAccount("fish");
    Template template1 = buildTemplate(account1, "Test Template 1", "test1");
    var chain = store.put(NexusIntegrationTestingFixtures.buildChain(template1));
    segment = store.put(buildSegment(chain, SegmentType.CONTINUE, 17, 4, SegmentState.DUBBED, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", SEQUENCE_TOTAL_BEATS, 0.73, 120.0, String.format("chains-%s-segments-%s", Chains.getIdentifier(chain), 17), "wav"));
    SegmentChoice choice = store.put(buildSegmentChoice(segment, ProgramType.Main, fake.program5_sequence0));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    sourceMaterial = new HubContent(ImmutableList.of(fake.program5_sequence0, fake.template1, fake.templateBinding1));
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);

    var result = subject.getProgramSequence(choice);

    assertEquals(fake.program5_sequence0.getId(), result.orElseThrow().getId());
  }

  @Test
  public void getProgramSequence_fromSequenceBinding() throws NexusException, ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, HubClientException, FabricationFatalException {
    var account1 = buildAccount("fish");
    Template template1 = buildTemplate(account1, "Test Template 1", "test1");
    var chain = store.put(NexusIntegrationTestingFixtures.buildChain(template1));
    segment = store.put(buildSegment(chain, SegmentType.CONTINUE, 17, 4, SegmentState.DUBBED, Instant.parse("2017-02-14T12:01:00.000001Z"), Instant.parse("2017-02-14T12:01:32.000001Z"), "D major", SEQUENCE_TOTAL_BEATS, 0.73, 120.0, String.format("chains-%s-segments-%s", Chains.getIdentifier(chain), 17), "wav"));
    SegmentChoice choice = store.put(buildSegmentChoice(segment, ProgramType.Main, fake.program5_sequence0_binding0));
    when(mockFabricatorFactory.loadRetrospective(any(), any())).thenReturn(mockRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any())).thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment()).thenReturn(segment);
    when(mockChainManager.readOne(eq(segment.getChainId()))).thenReturn(chain);
    sourceMaterial = new HubContent(ImmutableList.of(fake.program5_sequence0, fake.program5_sequence0_binding0, fake.template1, fake.templateBinding1));
    subject = new FabricatorImpl(sourceMaterial, segment, env, mockChainManager, mockFabricatorFactory, mockSegmentManager, mockJsonapiPayloadFactory, jsonProvider);

    var result = subject.getProgramSequence(choice);

    assertEquals(fake.program5_sequence0.getId(), result.orElseThrow().getId());
  }

  /**
   Sticky buns v2 https://www.pivotaltracker.com/story/show/179153822 use slash root when available
   */
  @Test
  public void getRootNote() {
    assertNote("C4", subject.getRootNoteMidRange("C3,E3,G3,A#3,C4,E4,G4", Chord.of("Cm")).orElseThrow());
  }

  /**
   Should add meme from ALL program and instrument types! https://www.pivotaltracker.com/story/show/181336704
   */
  @Test
  public void put_addsMemesForChoice() throws NexusException {
    subject.put(buildSegmentChoice(segment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program9, fake.program9_voice0, fake.instrument8));
    subject.put(buildSegmentChoice(segment, Segments.DELTA_UNLIMITED, Segments.DELTA_UNLIMITED, fake.program4, fake.program4_sequence1_binding0));

    verify(mockSegmentWorkbench, times(7)).put(entityCaptor.capture());
    List<Object> results = entityCaptor.getAllValues();
    assertEquals("HEAVY", ((SegmentMeme) results.get(0)).getName());
    assertEquals("BASIC", ((SegmentMeme) results.get(1)).getName());
    assertEquals(fake.instrument8.getId(), ((SegmentChoice) results.get(2)).getInstrumentId());
    assertEquals("WILD", ((SegmentMeme) results.get(3)).getName());
    assertEquals("COZY", ((SegmentMeme) results.get(4)).getName());
    assertEquals("TROPICAL", ((SegmentMeme) results.get(5)).getName());
    assertEquals(fake.program4.getId(), ((SegmentChoice) results.get(6)).getProgramId());
    assertEquals(fake.program4_sequence1_binding0.getId(), ((SegmentChoice) results.get(6)).getProgramSequenceBindingId());
  }

  /**
   Unit test behavior of choosing an event for a note in a detail program
   <p>
   Sticky bun note choices should persist into following segments https://www.pivotaltracker.com/story/show/182132467
   */
  @Test
  public void getStickyBun_readMetaFromCurrentSegment() throws JsonProcessingException {
    var bun = new StickyBun(fake.program9_sequence0_pattern0_event0.getId(), 3);
    var bunJson = jsonProvider.getMapper().writeValueAsString(bun);
    var bunKey = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event0.getId());
    var bunMeta = buildSegmentMeta(segment, bunKey, bunJson);
    when(mockSegmentWorkbench.getSegmentMeta(eq(bunKey))).thenReturn(Optional.of(bunMeta));

    var result = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.getId()).orElseThrow();

    assertEquals(fake.program9_sequence0_pattern0_event0.getId(), result.getEventId());
    assertArrayEquals(bun.getValues().toArray(), result.getValues().toArray());
  }

  /**
   Unit test behavior of choosing an event for a note in a detail program
   <p>
   Sticky bun note choices should persist into following segments https://www.pivotaltracker.com/story/show/182132467
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
   Sticky bun note choices should persist into following segments https://www.pivotaltracker.com/story/show/182132467
   */
  @Test
  public void getStickyBun_createForEvent() throws JsonProcessingException, NexusException {
    var result = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.getId()).orElseThrow();

    var resultJson = jsonProvider.getMapper().writeValueAsString(result);
    assertEquals(fake.program9_sequence0_pattern0_event0.getId(), result.getEventId());
    verify(mockSegmentWorkbench, times(1)).put(entityCaptor.capture());
    List<Object> results = entityCaptor.getAllValues();
    SegmentMeta resultMeta = (SegmentMeta) results.get(0);
    assertEquals(StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event0.getId()), resultMeta.getKey());
    assertEquals(resultJson, resultMeta.getValue());
  }

  /**
   Unit test behavior of choosing an event for a note in a detail program
   <p>
   Sticky bun note choices should persist into following segments https://www.pivotaltracker.com/story/show/182132467
   */
  @Test
  public void getStickyBun_multipleEventsPickedSeparately() throws JsonProcessingException {
    var bun0 = new StickyBun(fake.program9_sequence0_pattern0_event0.getId(), 3);
    var bunJson0 = jsonProvider.getMapper().writeValueAsString(bun0);
    var bunKey0 = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event0.getId());
    var bunMeta0 = buildSegmentMeta(segment, bunKey0, bunJson0);
    when(mockSegmentWorkbench.getSegmentMeta(eq(bunKey0))).thenReturn(Optional.of(bunMeta0));

    var bun1 = new StickyBun(fake.program9_sequence0_pattern0_event1.getId(), 3);
    var bunJson1 = jsonProvider.getMapper().writeValueAsString(bun1);
    var bunKey1 = StickyBun.computeMetaKey(fake.program9_sequence0_pattern0_event1.getId());
    var bunMeta1 = buildSegmentMeta(segment, bunKey1, bunJson1);
    when(mockSegmentWorkbench.getSegmentMeta(eq(bunKey1))).thenReturn(Optional.of(bunMeta1));

    var result0 = subject.getStickyBun(fake.program9_sequence0_pattern0_event0.getId()).orElseThrow();
    var result1 = subject.getStickyBun(fake.program9_sequence0_pattern0_event1.getId()).orElseThrow();

    assertEquals(fake.program9_sequence0_pattern0_event0.getId(), result0.getEventId());
    assertArrayEquals(bun0.getValues().toArray(), result0.getValues().toArray());
    assertEquals(fake.program9_sequence0_pattern0_event1.getId(), result1.getEventId());
    assertArrayEquals(bun1.getValues().toArray(), result1.getValues().toArray());
  }


}
