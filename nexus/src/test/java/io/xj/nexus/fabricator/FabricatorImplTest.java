// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.ContentBindingType;
import io.xj.api.InstrumentType;
import io.xj.api.Library;
import io.xj.api.ProgramSequencePatternType;
import io.xj.api.ProgramType;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.api.Template;
import io.xj.api.TemplateBinding;
import io.xj.api.TemplateType;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.music.Note;
import io.xj.lib.music.Tuning;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.NexusTestConfiguration;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.dao.NexusDAOModule;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.dao.Segments;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.hub_client.client.HubClient;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubClientException;
import io.xj.nexus.hub_client.client.HubClientModule;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreModule;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplateBinding;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildAccount;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildEvent;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildLibrary;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildPattern;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildProgram;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSequence;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildTrack;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildVoice;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.eq;
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
  public ChainDAO mockChainDAO;
  @Mock
  public SegmentDAO mockSegmentDAO;
  @Mock
  public JsonapiPayloadFactory mockJsonapiPayloadFactory;
  @Mock
  public FileStoreProvider mockFileStoreProvider;
  private Config config;
  private FabricatorImpl subject;
  private HubContent sourceMaterial;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;

  @Before
  public void setUp() throws Exception {
    config = NexusTestConfiguration.getDefault();
    var injector = Guice.createInjector(Modules.override(new FileStoreModule(), new NexusDAOModule(), new HubClientModule(), new NexusEntityStoreModule(), new MixerModule(), new JsonapiModule(), new NexusWorkModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Config.class).toInstance(config);
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
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

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
    var account1 = buildAccount("fish");
    Library library1 = buildLibrary(account1, "test");
    var template1 = buildTemplate(account1, "Test Template 1", "test1");
    buildTemplateBinding(template1, library1);
    var chain = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(UUID.randomUUID())
      .name("test")
      .templateId(template1.getId())
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2017-12-12T01:00:08.000000Z"));
    Segment previousSegment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(1L)
      .state(SegmentState.CRAFTED)
      .beginAt("2017-12-12T01:00:08.000000Z")
      .endAt("2017-12-12T01:00:16.000000Z")
      .key("F major")
      .total(8)
      .density(0.6)
      .tempo(120.0)
      .storageKey("seg123.ogg"));
    Segment segment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(2L)
      .state(SegmentState.CRAFTING)
      .beginAt("2017-12-12T01:00:16.000000Z")
      .endAt("2017-12-12T01:00:22.000000Z")
      .key("G major")
      .total(8)
      .density(0.6)
      .tempo(240.0)
      .storageKey("seg123.ogg"));
    when(mockFabricatorFactory.createTimeComputer(anyDouble(), anyDouble(), anyDouble()))
      .thenReturn(mockTimeComputer);
    when(mockTimeComputer.getSecondsAtPosition(anyDouble()))
      .thenReturn(Double.valueOf(0));
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentRetrospective.getPreviousSegment())
      .thenReturn(java.util.Optional.ofNullable(previousSegment));
    var access = HubClientAccess.internal();
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, sourceMaterial, segment, config, env, mockChainDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockJsonapiPayloadFactory);

    Double result = subject.getSecondsAtPosition(0); // instantiates a time computer; see expectation above

    assertEquals(Double.valueOf(0), result);
    verify(mockFabricatorFactory).createTimeComputer(8.0, 120, 240.0);
  }


  @Test
  public void pick_returned_by_picks() throws Exception {
    var template = new Template()
      .id(UUID.randomUUID())
      .accountId(UUID.randomUUID())
      .name("test")
      .config("outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"");
    var chain = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(UUID.randomUUID())
      .templateId(template.getId())
      .name("test")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2017-12-12T01:00:08.000000Z"));
    store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(1L)
      .state(SegmentState.CRAFTED)
      .beginAt("2017-12-12T01:00:08.000000Z")
      .endAt("2017-12-12T01:00:16.000000Z")
      .key("F major")
      .total(8)
      .density(0.6)
      .tempo(120.0)
      .storageKey("seg123.ogg"));
    Segment segment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(2L)
      .state(SegmentState.CRAFTING)
      .beginAt("2017-12-12T01:00:16.000000Z")
      .endAt("2017-12-12T01:00:22.000000Z")
      .key("G major")
      .total(8)
      .density(0.6)
      .tempo(240.0)
      .storageKey("seg123.ogg"));
    store.put(new TemplateBinding()
      .id(UUID.randomUUID())
      .templateId(template.getId())
      .targetId(fake.library2.getId())
      .type(ContentBindingType.LIBRARY));
    store.put(new SegmentChord()
      .id(UUID.randomUUID())
      .segmentId(segment.getId())
      .name("A")
      .position(0.0));
    store.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment.getId())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programType(ProgramType.MAIN)
      .programId(fake.program5.getId()));
    SegmentChoice rhythmChoice = store.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment.getId())
      .programType(ProgramType.RHYTHM)
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programId(fake.program35.getId())
      .programVoiceId(fake.program35_voice0.getId())
      .instrumentId(fake.instrument8.getId()));
    SegmentChoiceArrangement rhythmArrangement = store.put(new SegmentChoiceArrangement()
      .id(UUID.randomUUID())
      .segmentId(segment.getId())
      .segmentChoiceId(rhythmChoice.getId()));
    SegmentChoiceArrangementPick rhythmPick = store.put(
      new SegmentChoiceArrangementPick()
        .id(UUID.randomUUID())
        .segmentId(rhythmArrangement.getSegmentId())
        .segmentChoiceArrangementId(rhythmArrangement.getId())
        .programSequencePatternEventId(fake.program35_sequence0_pattern0_event0.getId())
        .instrumentAudioId(fake.instrument8_audio8kick.getId())
        .name("CLANG")
        .start(0.273)
        .length(1.571)
        .amplitude(0.8)
        .note("A4"));
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentWorkbench.getSegmentChoiceArrangementPicks())
      .thenReturn(ImmutableList.of(rhythmPick));
    var access = HubClientAccess.internal();
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, sourceMaterial, segment, config, env, mockChainDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockJsonapiPayloadFactory);

    Collection<SegmentChoiceArrangementPick> result = subject.getPicks();

    SegmentChoiceArrangementPick resultPick = result.iterator().next();
    assertEquals(rhythmArrangement.getId(), resultPick.getSegmentChoiceArrangementId());
    assertEquals(fake.instrument8_audio8kick.getId(), resultPick.getInstrumentAudioId());
    assertEquals(0.273, resultPick.getStart(), 0.001);
    assertEquals(1.571, resultPick.getLength(), 0.001);
    assertEquals(0.8, resultPick.getAmplitude(), 0.1);
    assertEquals("A4", resultPick.getNote());
  }


  @Test
  public void getDistinctChordVoicingTypes() throws Exception {
    var chain = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(UUID.randomUUID())
      .name("test")
      .templateId(fake.template1.getId())
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2017-12-12T01:00:08.000000Z"));
    store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(1L)
      .state(SegmentState.CRAFTED)
      .beginAt("2017-12-12T01:00:08.000000Z")
      .endAt("2017-12-12T01:00:16.000000Z")
      .key("F major")
      .total(8)
      .density(0.6)
      .tempo(120.0)
      .storageKey("seg123.ogg"));
    Segment segment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(2L)
      .state(SegmentState.CRAFTING)
      .beginAt("2017-12-12T01:00:16.000000Z")
      .endAt("2017-12-12T01:00:22.000000Z")
      .key("G major")
      .total(8)
      .density(0.6)
      .tempo(240.0)
      .storageKey("seg123.ogg"));
    SegmentChoice mainChoice = store.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(segment.getId())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programType(ProgramType.MAIN)
      .programId(fake.program5.getId()));
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentWorkbench.getChoiceOfType(ProgramType.MAIN))
      .thenReturn(Optional.of(mainChoice));
    var access = HubClientAccess.internal();
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, sourceMaterial, segment, config, env, mockChainDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockJsonapiPayloadFactory);

    List<InstrumentType> result = subject.getDistinctChordVoicingTypes();

    assertEquals(ImmutableList.of(
      InstrumentType.BASS
    ), result);
  }


  /**
   [#176728582] Choose next Macro program based on the memes of the last sequence from the previous Macro program
   */
  @Test
  public void getType() throws NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    var chain = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(UUID.randomUUID())
      .templateId(fake.template1.getId())
      .name("test")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2017-12-12T01:00:08.000000Z"));
    Segment previousSegment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(1L)
      .state(SegmentState.CRAFTED)
      .beginAt("2017-12-12T01:00:08.000000Z")
      .endAt("2017-12-12T01:00:16.000000Z")
      .key("F major")
      .total(8)
      .density(0.6)
      .tempo(120.0)
      .storageKey("seg123.ogg"));
    var previousMacroChoice = // second-to-last sequence of macro program
      store.put(new SegmentChoice()
        .id(UUID.randomUUID())
        .deltaIn(Segments.DELTA_UNLIMITED)
        .deltaOut(Segments.DELTA_UNLIMITED)
        .segmentId(previousSegment.getId())
        .programType(ProgramType.MACRO)
        .programId(fake.program4.getId())
        .programSequenceBindingId(fake.program4_sequence1_binding0.getId()));
    var previousMainChoice = // last sequence of main program
      store.put(new SegmentChoice()
        .id(UUID.randomUUID())
        .segmentId(previousSegment.getId())
        .deltaIn(Segments.DELTA_UNLIMITED)
        .deltaOut(Segments.DELTA_UNLIMITED)
        .programType(ProgramType.MAIN)
        .programId(fake.program5.getId())
        .programSequenceBindingId(fake.program5_sequence1_binding0.getId()));
    Segment segment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(2L)
      .state(SegmentState.CRAFTING)
      .beginAt("2017-12-12T01:00:16.000000Z")
      .endAt("2017-12-12T01:00:22.000000Z")
      .key("G major")
      .total(8)
      .density(0.6)
      .tempo(240.0)
      .storageKey("seg123.ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentRetrospective.getPreviousChoiceOfType(ProgramType.MAIN))
      .thenReturn(Optional.of(previousMainChoice));
    when(mockSegmentRetrospective.getPreviousChoiceOfType(ProgramType.MACRO))
      .thenReturn(Optional.of(previousMacroChoice));
    var access = HubClientAccess.internal();
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, sourceMaterial, segment, config, env, mockChainDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockJsonapiPayloadFactory);

    var result = subject.getType();

    assertEquals(SegmentType.NEXTMACRO, result);
  }

  // FUTURE: test getChoicesOfPreviousSegments

  @Test
  public void getMemeIsometryOfNextSequenceInPreviousMacro() throws NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    var chain = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(UUID.randomUUID())
      .templateId(fake.template1.getId())
      .name("test")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2017-12-12T01:00:08.000000Z"));
    Segment previousSegment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(1L)
      .state(SegmentState.CRAFTED)
      .beginAt("2017-12-12T01:00:08.000000Z")
      .endAt("2017-12-12T01:00:16.000000Z")
      .key("F major")
      .total(8)
      .density(0.6)
      .tempo(120.0)
      .storageKey("seg123.ogg"));
    var previousMacroChoice = // second-to-last sequence of macro program
      store.put(new SegmentChoice()
        .id(UUID.randomUUID())
        .deltaIn(Segments.DELTA_UNLIMITED)
        .deltaOut(Segments.DELTA_UNLIMITED)
        .segmentId(previousSegment.getId())
        .programType(ProgramType.MACRO)
        .programId(fake.program4.getId())
        .programSequenceBindingId(fake.program4_sequence1_binding0.getId()));
    store.put(new SegmentChoice()
      .id(UUID.randomUUID())
      .segmentId(previousSegment.getId())
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programType(ProgramType.MAIN)
      .programId(fake.program5.getId())
      .programSequenceBindingId(fake.program5_sequence1_binding0.getId()));
    Segment segment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(2L)
      .state(SegmentState.CRAFTING)
      .beginAt("2017-12-12T01:00:16.000000Z")
      .endAt("2017-12-12T01:00:22.000000Z")
      .key("G major")
      .total(8)
      .density(0.6)
      .tempo(240.0)
      .storageKey("seg123.ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentRetrospective.getPreviousChoiceOfType(ProgramType.MACRO))
      .thenReturn(Optional.of(previousMacroChoice));
    var access = HubClientAccess.internal();
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, sourceMaterial, segment, config, env, mockChainDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockJsonapiPayloadFactory);

    var result = subject.getMemeIsometryOfNextSequenceInPreviousMacro();

    assertArrayEquals(new String[]{"COZY", "TROPICAL"}, result.getSources().stream().sorted().toArray());
  }

  @Test
  public void getChordAt() throws NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    var chain = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(UUID.randomUUID())
      .templateId(fake.template1.getId())
      .name("test")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2017-12-12T01:00:08.000000Z"));
    Segment segment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(2L)
      .state(SegmentState.CRAFTING)
      .beginAt("2017-12-12T01:00:16.000000Z")
      .endAt("2017-12-12T01:00:22.000000Z")
      .key("G major")
      .total(8)
      .density(0.6)
      .tempo(240.0)
      .storageKey("seg123.ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegmentChords())
      .thenReturn(ImmutableList.of(
        new SegmentChord()
          .id(UUID.randomUUID())
          .segmentId(segment.getId())
          .name("C")
          .position(0.0)
        ,
        new SegmentChord()
          .id(UUID.randomUUID())
          .segmentId(segment.getId())
          .name("F")
          .position(2.0)
        ,
        new SegmentChord()
          .id(UUID.randomUUID())
          .segmentId(segment.getId())
          .name("Gm")
          .position(5.5)

      ));
    var access = HubClientAccess.internal();
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, sourceMaterial, segment, config, env, mockChainDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockJsonapiPayloadFactory);

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
  public void computeProgramRange() throws NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException, HubClientException {
    var chain = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(UUID.randomUUID())
      .templateId(fake.template1.getId())
      .name("test")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2017-12-12T01:00:08.000000Z"));
    Segment segment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(2L)
      .state(SegmentState.CRAFTING)
      .beginAt("2017-12-12T01:00:16.000000Z")
      .endAt("2017-12-12T01:00:22.000000Z")
      .key("G major")
      .total(8)
      .density(0.6)
      .tempo(240.0)
      .storageKey("seg123.ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    var access = HubClientAccess.internal();
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    var program = buildProgram(ProgramType.DETAIL, "C", 120.0, 1.0);
    var voice = buildVoice(program, InstrumentType.BASS);
    var track = buildTrack(voice);
    var sequence = buildSequence(program, 4);
    var pattern = buildPattern(sequence, voice, ProgramSequencePatternType.LOOP, 4);
    sourceMaterial = new HubContent(ImmutableList.of(
      program,
      voice,
      track,
      sequence,
      pattern,
      fake.template1,
      fake.templateBinding1,
      buildEvent(pattern, track, 0.0, 1.0, "C1"),
      buildEvent(pattern, track, 1.0, 1.0, "D2")
    ));
    subject = new FabricatorImpl(access, sourceMaterial, segment, config, env, mockChainDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockJsonapiPayloadFactory);

    var result = subject.getProgramRange(program.getId(), InstrumentType.BASS);

    assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
    assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
  }

  @Test
  public void computeProgramRange_ignoresAtonalNotes() throws NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException, HubClientException {
    var chain = store.put(new Chain()
      .id(UUID.randomUUID())
      .accountId(UUID.randomUUID())
      .templateId(fake.template1.getId())
      .name("test")
      .type(TemplateType.PRODUCTION)
      .state(ChainState.FABRICATE)
      .startAt("2017-12-12T01:00:08.000000Z"));
    Segment segment = store.put(new Segment()
      .id(UUID.randomUUID())
      .chainId(chain.getId())
      .offset(2L)
      .state(SegmentState.CRAFTING)
      .beginAt("2017-12-12T01:00:16.000000Z")
      .endAt("2017-12-12T01:00:22.000000Z")
      .key("G major")
      .total(8)
      .density(0.6)
      .tempo(240.0)
      .storageKey("seg123.ogg"));
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    var access = HubClientAccess.internal();
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    var program = buildProgram(ProgramType.DETAIL, "C", 120.0, 1.0);
    var voice = buildVoice(program, InstrumentType.BASS);
    var track = buildTrack(voice);
    var sequence = buildSequence(program, 4);
    var pattern = buildPattern(sequence, voice, ProgramSequencePatternType.LOOP, 4);
    sourceMaterial = new HubContent(ImmutableList.of(
      program,
      voice,
      track,
      sequence,
      pattern,
      buildEvent(pattern, track, 0.0, 1.0, "C1"),
      buildEvent(pattern, track, 1.0, 1.0, "X"),
      buildEvent(pattern, track, 2.0, 1.0, "D2"),
      fake.template1,
      fake.templateBinding1
    ));
    subject = new FabricatorImpl(access, sourceMaterial, segment, config, env, mockChainDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockJsonapiPayloadFactory);

    var result = subject.getProgramRange(program.getId(), InstrumentType.BASS);

    assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
    assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
  }
}
