// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Streams;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.*;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.common.Topology;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.music.Note;
import io.xj.lib.music.Tuning;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusIntegrationTestingFixtures;
import io.xj.nexus.dao.ChainBindingDAO;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.dao.NexusDAOModule;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.hub_client.client.*;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreModule;
import io.xj.nexus.testing.NexusTestConfiguration;
import io.xj.nexus.work.NexusWorkModule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static io.xj.nexus.NexusIntegrationTestingFixtures.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 FUTURE: [#170035559] Split the FabricatorImplTest into separate tests of the FabricatorImpl, SegmentWorkbenchImpl, SegmentRetrospectiveImpl, and IngestImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class FabricatorImplTest {
  private FabricatorImpl subject;
  private NexusEntityStore store;
  private NexusIntegrationTestingFixtures fake;
  private Config config;

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
  public ChainBindingDAO mockChainBindingDAO;

  @Mock
  public SegmentDAO mockSegmentDAO;

  @Mock
  public PayloadFactory mockPayloadFactory;

  @Mock
  public FileStoreProvider mockFileStoreProvider;

  @Before
  public void setUp() throws Exception {
    config = NexusTestConfiguration.getDefault();
    var injector = AppConfiguration.inject(config, ImmutableSet.of(Modules.override(new FileStoreModule(), new NexusDAOModule(), new HubClientModule(), new NexusEntityStoreModule(), new MixerModule(), new JsonApiModule(), new NexusWorkModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(Tuning.class).toInstance(mockTuning);
          bind(HubClient.class).toInstance(mockHubClient);
          bind(FabricatorFactory.class).toInstance(mockFabricatorFactory);
          bind(TimeComputer.class).toInstance(mockTimeComputer);
          bind(SegmentWorkbench.class).toInstance(mockSegmentWorkbench);
          bind(SegmentRetrospective.class).toInstance(mockSegmentRetrospective);
        }
      })));
    var entityFactory = injector.getInstance(EntityFactory.class);
    Topology.buildHubApiTopology(entityFactory);
    Topology.buildNexusApiTopology(entityFactory);

    // Manipulate the underlying entity store; reset before each test
    store = injector.getInstance(NexusEntityStore.class);
    store.deleteAll();

    // Mock request via HubClient returns fake generated library of hub content
    fake = new NexusIntegrationTestingFixtures();
    when(mockHubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(Streams.concat(
        fake.setupFixtureB1().stream(),
        fake.setupFixtureB2().stream(),
        fake.setupFixtureB3().stream()
      ).collect(Collectors.toList())));
  }

  @Test
  public void usesTimeComputer() throws Exception {
    Library library = Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .build();
    var chain = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(UUID.randomUUID().toString())
      .setName("test")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2017-12-12T01:00:08.000000Z")
      .setConfig("outputEncoding=\"PCM_SIGNED\"")
      .build());
    Segment previousSegment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(1)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-12-12T01:00:08.000000Z")
      .setEndAt("2017-12-12T01:00:16.000000Z")
      .setKey("F major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(120)
      .setStorageKey("seg123.ogg")
      .build());
    Segment segment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(2)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z")
      .setKey("G major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(240)
      .setStorageKey("seg123.ogg")
      .build());
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setType(ChainBinding.Type.Library)
      .setTargetId(library.getId())
      .build());
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
    subject = new FabricatorImpl(access, segment, config, env, mockHubClient, mockChainDAO, mockChainBindingDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockPayloadFactory);

    Double result = subject.computeSecondsAtPosition(0); // instantiates a time computer; see expectation above

    assertEquals(Double.valueOf(0), result);
    verify(mockFabricatorFactory).createTimeComputer(8.0, 120, 240.0);
  }


  @Test
  public void pick_returned_by_picks() throws Exception {
    var chain = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(UUID.randomUUID().toString())
      .setName("test")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2017-12-12T01:00:08.000000Z")
      .setConfig("outputEncoding=\"PCM_SIGNED\"")
      .build());
    Segment previousSegment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(1)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-12-12T01:00:08.000000Z")
      .setEndAt("2017-12-12T01:00:16.000000Z")
      .setKey("F major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(120)
      .setStorageKey("seg123.ogg")
      .build());
    Segment segment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(2)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z")
      .setKey("G major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(240)
      .setStorageKey("seg123.ogg")
      .build());
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    store.put(SegmentChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment.getId())
      .setName("A")
      .setPosition(0)
      .build());
    store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment.getId())
      .setProgramType(Program.Type.Main)
      .setProgramId(fake.program5.getId())
      .build());
    SegmentChoice rhythmChoice = store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment.getId())
      .setProgramType(Program.Type.Rhythm)
      .setProgramId(fake.program35.getId())
      .setProgramVoiceId(fake.program35_voice0.getId())
      .setInstrumentId(fake.instrument8.getId())
      .build());
    SegmentChoiceArrangement rhythmArrangement = store.put(SegmentChoiceArrangement.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment.getId())
      .setSegmentChoiceId(rhythmChoice.getId())
      .build());
    SegmentChoiceArrangementPick rhythmPick = store.put(
      SegmentChoiceArrangementPick.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(rhythmArrangement.getSegmentId())
        .setSegmentChoiceArrangementId(rhythmArrangement.getId())
        .setProgramSequencePatternEventId(fake.program35_sequence0_pattern0_event0.getId())
        .setInstrumentAudioId(fake.instrument8_audio8kick.getId())
        .setName("CLANG")
        .setStart(0.273)
        .setLength(1.571)
        .setAmplitude(0.8)
        .setNote("A4")
        .build());
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
    when(mockSegmentWorkbench.getSegmentChoiceArrangementPicks())
      .thenReturn(ImmutableList.of(rhythmPick));
    when(mockSegmentRetrospective.getPreviousSegment())
      .thenReturn(java.util.Optional.ofNullable(previousSegment));
    var access = HubClientAccess.internal();
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, segment, config, env, mockHubClient, mockChainDAO, mockChainBindingDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockPayloadFactory);

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
    var chain = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(UUID.randomUUID().toString())
      .setName("test")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2017-12-12T01:00:08.000000Z")
      .setConfig("outputEncoding=\"PCM_SIGNED\"")
      .build());
    Segment previousSegment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(1)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-12-12T01:00:08.000000Z")
      .setEndAt("2017-12-12T01:00:16.000000Z")
      .setKey("F major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(120)
      .setStorageKey("seg123.ogg")
      .build());
    Segment segment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(2)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z")
      .setKey("G major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(240)
      .setStorageKey("seg123.ogg")
      .build());
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    SegmentChoice mainChoice = store.put(SegmentChoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setSegmentId(segment.getId())
      .setProgramType(Program.Type.Main)
      .setProgramId(fake.program5.getId())
      .build());
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
    when(mockSegmentWorkbench.getChoiceOfType(Program.Type.Main))
      .thenReturn(Optional.of(mainChoice));
    when(mockSegmentRetrospective.getPreviousSegment())
      .thenReturn(java.util.Optional.ofNullable(previousSegment));
    var access = HubClientAccess.internal();
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, segment, config, env, mockHubClient, mockChainDAO, mockChainBindingDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockPayloadFactory);

    List<Instrument.Type> result = subject.getDistinctChordVoicingTypes();

    assertEquals(ImmutableList.of(
      Instrument.Type.Bass
    ), result);
  }

  /**
   [#176728582] Choose next Macro program based on the memes of the last sequence from the previous Macro program
   */
  @Test
  public void determineType() throws NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    var chain = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(UUID.randomUUID().toString())
      .setName("test")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2017-12-12T01:00:08.000000Z")
      .setConfig("outputEncoding=\"PCM_SIGNED\"")
      .build());
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    Segment previousSegment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(1)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-12-12T01:00:08.000000Z")
      .setEndAt("2017-12-12T01:00:16.000000Z")
      .setKey("F major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(120)
      .setStorageKey("seg123.ogg")
      .build());
    var previousMacroChoice = // second-to-last sequence of macro program
      store.put(SegmentChoice.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(previousSegment.getId())
        .setProgramType(Program.Type.Macro)
        .setProgramId(fake.program4.getId())
        .setProgramSequenceBindingId(fake.program4_sequence1_binding0.getId())
        .build());
    var previousMainChoice = // last sequence of main program
      store.put(SegmentChoice.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(previousSegment.getId())
        .setProgramType(Program.Type.Main)
        .setProgramId(fake.program5.getId())
        .setProgramSequenceBindingId(fake.program5_sequence1_binding0.getId())
        .build());
    Segment segment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(2)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z")
      .setKey("G major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(240)
      .setStorageKey("seg123.ogg")
      .build());
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentRetrospective.getPreviousSegment())
      .thenReturn(Optional.of(previousSegment));
    when(mockSegmentRetrospective.getPreviousChoiceOfType(Program.Type.Main))
      .thenReturn(Optional.of(previousMainChoice));
    when(mockSegmentRetrospective.getPreviousChoiceOfType(Program.Type.Macro))
      .thenReturn(Optional.of(previousMacroChoice));
    var access = HubClientAccess.internal();
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, segment, config, env, mockHubClient, mockChainDAO, mockChainBindingDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockPayloadFactory);

    var result = subject.determineType();

    assertEquals(Segment.Type.NextMacro, result);
  }

  // FUTURE: test getChoicesOfPreviousSegments

  @Test
  public void getMemeIsometryOfNextSequenceInPreviousMacro() throws NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    var chain = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(UUID.randomUUID().toString())
      .setName("test")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2017-12-12T01:00:08.000000Z")
      .setConfig("outputEncoding=\"PCM_SIGNED\"")
      .build());
    store.put(ChainBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setTargetId(fake.library2.getId())
      .setType(ChainBinding.Type.Library)
      .build());
    Segment previousSegment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(1)
      .setState(Segment.State.Crafted)
      .setBeginAt("2017-12-12T01:00:08.000000Z")
      .setEndAt("2017-12-12T01:00:16.000000Z")
      .setKey("F major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(120)
      .setStorageKey("seg123.ogg")
      .build());
    var previousMacroChoice = // second-to-last sequence of macro program
      store.put(SegmentChoice.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(previousSegment.getId())
        .setProgramType(Program.Type.Macro)
        .setProgramId(fake.program4.getId())
        .setProgramSequenceBindingId(fake.program4_sequence1_binding0.getId())
        .build());
    var previousMainChoice = // last sequence of main program
      store.put(SegmentChoice.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(previousSegment.getId())
        .setProgramType(Program.Type.Main)
        .setProgramId(fake.program5.getId())
        .setProgramSequenceBindingId(fake.program5_sequence1_binding0.getId())
        .build());
    Segment segment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(2)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z")
      .setKey("G major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(240)
      .setStorageKey("seg123.ogg")
      .build());
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockSegmentRetrospective.getPreviousSegment())
      .thenReturn(Optional.of(previousSegment));
    when(mockSegmentRetrospective.getPreviousChoiceOfType(Program.Type.Main))
      .thenReturn(Optional.of(previousMainChoice));
    when(mockSegmentRetrospective.getPreviousChoiceOfType(Program.Type.Macro))
      .thenReturn(Optional.of(previousMacroChoice));
    var access = HubClientAccess.internal();
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, segment, config, env, mockHubClient, mockChainDAO, mockChainBindingDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockPayloadFactory);

    var result = subject.getMemeIsometryOfNextSequenceInPreviousMacro();

    assertEquals(ImmutableList.of("TROPICAL", "COZY"), result.getSources());
  }

  @Test
  public void getChordAt() throws NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    var chain = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(UUID.randomUUID().toString())
      .setName("test")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2017-12-12T01:00:08.000000Z")
      .setConfig("outputEncoding=\"PCM_SIGNED\"")
      .build());
    Segment segment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(2)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z")
      .setKey("G major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(240)
      .setStorageKey("seg123.ogg")
      .build());
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegmentChords())
      .thenReturn(ImmutableList.of(
        SegmentChord.newBuilder()
          .setId(UUID.randomUUID().toString())
          .setSegmentId(segment.getId())
          .setName("C")
          .setPosition(0)
          .build(),
        SegmentChord.newBuilder()
          .setId(UUID.randomUUID().toString())
          .setSegmentId(segment.getId())
          .setName("F")
          .setPosition(2)
          .build(),
        SegmentChord.newBuilder()
          .setId(UUID.randomUUID().toString())
          .setSegmentId(segment.getId())
          .setName("Gm")
          .setPosition(5.5)
          .build()
      ));
    var access = HubClientAccess.internal();
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    subject = new FabricatorImpl(access, segment, config, env, mockHubClient, mockChainDAO, mockChainBindingDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockPayloadFactory);

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
    var chain = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(UUID.randomUUID().toString())
      .setName("test")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2017-12-12T01:00:08.000000Z")
      .setConfig("outputEncoding=\"PCM_SIGNED\"")
      .build());
    Segment segment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(2)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z")
      .setKey("G major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(240)
      .setStorageKey("seg123.ogg")
      .build());
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegmentChords())
      .thenReturn(ImmutableList.of());
    var access = HubClientAccess.internal();
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    var program = makeProgram(Program.Type.Detail, "C", 120.0, 1.0);
    var voice = makeVoice(program, Instrument.Type.Bass);
    var track = makeTrack(voice);
    var sequence = makeSequence(program, 4);
    var pattern = makePattern(sequence, voice, ProgramSequencePattern.Type.Loop, 4);
    when(mockHubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(ImmutableList.of(
        program,
        voice,
        track,
        sequence,
        pattern,
        makeEvent(pattern, track, 0.0, 1.0, "C1"),
        makeEvent(pattern, track, 1.0, 1.0, "D2")
      )));
    subject = new FabricatorImpl(access, segment, config, env, mockHubClient, mockChainDAO, mockChainBindingDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockPayloadFactory);

    var result = subject.computeProgramRange(program.getId(), Instrument.Type.Bass);

    assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
    assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
  }

  @Test
  public void computeProgramRange_ignoresAtonalNotes() throws NexusException, DAOPrivilegeException, DAOFatalException, DAOExistenceException, HubClientException {
    var chain = store.put(Chain.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(UUID.randomUUID().toString())
      .setName("test")
      .setType(Chain.Type.Production)
      .setState(Chain.State.Fabricate)
      .setStartAt("2017-12-12T01:00:08.000000Z")
      .setConfig("outputEncoding=\"PCM_SIGNED\"")
      .build());
    Segment segment = store.put(Segment.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setChainId(chain.getId())
      .setOffset(2)
      .setState(Segment.State.Crafting)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z")
      .setKey("G major")
      .setTotal(8)
      .setDensity(0.6)
      .setTempo(240)
      .setStorageKey("seg123.ogg")
      .build());
    when(mockFabricatorFactory.loadRetrospective(any(), any(), any()))
      .thenReturn(mockSegmentRetrospective);
    when(mockFabricatorFactory.setupWorkbench(any(), any(), any()))
      .thenReturn(mockSegmentWorkbench);
    when(mockSegmentWorkbench.getSegmentChords())
      .thenReturn(ImmutableList.of());
    var access = HubClientAccess.internal();
    when(mockSegmentWorkbench.getSegment())
      .thenReturn(segment);
    when(mockChainDAO.readOne(eq(access), eq(segment.getChainId()))).thenReturn(chain);
    var program = makeProgram(Program.Type.Detail, "C", 120.0, 1.0);
    var voice = makeVoice(program, Instrument.Type.Bass);
    var track = makeTrack(voice);
    var sequence = makeSequence(program, 4);
    var pattern = makePattern(sequence, voice, ProgramSequencePattern.Type.Loop, 4);
    when(mockHubClient.ingest(any(), any(), any(), any()))
      .thenReturn(new HubContent(ImmutableList.of(
        program,
        voice,
        track,
        sequence,
        pattern,
        makeEvent(pattern, track, 0.0, 1.0, "C1"),
        makeEvent(pattern, track, 1.0, 1.0, "X"),
        makeEvent(pattern, track, 2.0, 1.0, "D2")
      )));
    subject = new FabricatorImpl(access, segment, config, env, mockHubClient, mockChainDAO, mockChainBindingDAO, mockFileStoreProvider, mockFabricatorFactory, mockSegmentDAO, mockPayloadFactory);

    var result = subject.computeProgramRange(program.getId(), Instrument.Type.Bass);

    assertTrue(Note.of("C1").sameAs(result.getLow().orElseThrow()));
    assertTrue(Note.of("D2").sameAs(result.getHigh().orElseThrow()));
  }

}
