// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.util.Modules;
import com.typesafe.config.Config;
import io.xj.lib.app.AppConfiguration;
import io.xj.lib.music.Tuning;
import io.xj.service.hub.HubModule;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.ChainBindingDAO;
import io.xj.service.hub.dao.ChainConfigDAO;
import io.xj.service.hub.dao.ChainDAO;
import io.xj.service.hub.dao.InstrumentAudioChordDAO;
import io.xj.service.hub.dao.InstrumentAudioDAO;
import io.xj.service.hub.dao.InstrumentAudioEventDAO;
import io.xj.service.hub.dao.InstrumentDAO;
import io.xj.service.hub.dao.InstrumentMemeDAO;
import io.xj.service.hub.dao.LibraryDAO;
import io.xj.service.hub.dao.ProgramDAO;
import io.xj.service.hub.dao.ProgramMemeDAO;
import io.xj.service.hub.dao.ProgramSequenceBindingDAO;
import io.xj.service.hub.dao.ProgramSequenceBindingMemeDAO;
import io.xj.service.hub.dao.ProgramSequenceChordDAO;
import io.xj.service.hub.dao.ProgramSequenceDAO;
import io.xj.service.hub.dao.ProgramSequencePatternDAO;
import io.xj.service.hub.dao.ProgramSequencePatternEventDAO;
import io.xj.service.hub.dao.ProgramVoiceDAO;
import io.xj.service.hub.dao.ProgramVoiceTrackDAO;
import io.xj.service.hub.dao.SegmentChoiceDAO;
import io.xj.service.hub.dao.SegmentDAO;
import io.xj.service.hub.model.Account;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.ChainConfig;
import io.xj.service.hub.model.ChainConfigType;
import io.xj.service.hub.model.ChainState;
import io.xj.service.hub.model.ChainType;
import io.xj.service.hub.model.Instrument;
import io.xj.service.hub.model.InstrumentAudio;
import io.xj.service.hub.model.Library;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceBinding;
import io.xj.service.hub.model.ProgramSequencePatternEvent;
import io.xj.service.hub.model.ProgramState;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.ProgramVoice;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentChoiceArrangement;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.model.SegmentState;
import io.xj.service.hub.model.User;
import io.xj.service.hub.testing.AppTestConfiguration;
import io.xj.service.hub.testing.IntegrationTestModule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyDouble;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 FUTURE: [#170035559] Split the FabricatorImplTest into separate tests of the FabricatorImpl, SegmentWorkbenchImpl, SegmentRetrospectiveImpl, and IngestImpl
 */
@RunWith(MockitoJUnitRunner.class)
public class FabricatorImplTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();
  @Mock
  public TimeComputerFactory timeComputerFactory;
  @Mock
  public TimeComputer timeComputer;
  @Mock
  public LibraryDAO libraryDAO;
  @Mock
  public InstrumentDAO instrumentDAO;
  @Mock
  public InstrumentAudioChordDAO instrumentAudioChordDAO;
  @Mock
  public InstrumentAudioDAO instrumentAudioDAO;
  @Mock
  public InstrumentAudioEventDAO instrumentAudioEventDAO;
  @Mock
  public InstrumentMemeDAO instrumentMemeDAO;
  @Mock
  public ProgramDAO programDAO;
  @Mock
  public ProgramSequencePatternEventDAO programEventDAO;
  @Mock
  public ProgramMemeDAO programMemeDAO;
  @Mock
  public ProgramSequencePatternDAO programPatternDAO;
  @Mock
  public ProgramSequenceBindingDAO programSequenceBindingDAO;
  @Mock
  public ProgramSequenceBindingMemeDAO programSequenceBindingMemeDAO;
  @Mock
  public ProgramSequenceChordDAO programSequenceChordDAO;
  @Mock
  public ProgramSequenceDAO programSequenceDAO;
  @Mock
  public ProgramVoiceTrackDAO programTrackDAO;
  @Mock
  public ProgramVoiceDAO programVoiceDAO;
  @Mock
  public ChainDAO chainDAO;
  @Mock
  public ChainConfigDAO chainConfigDAO;
  @Mock
  public ChainBindingDAO chainBindingDAO;
  @Mock
  public SegmentDAO segmentDAO;
  @Mock
  public SegmentChoiceDAO segmentChoiceDAO;
  @Mock
  public Tuning tuning;
  //
  private Fabricator subject;
  private FabricatorFactory fabricatorFactory;

  @Before
  public void setUp() throws Exception {
    Config config = AppTestConfiguration.getDefault();
    Injector injector = AppConfiguration.inject(config, ImmutableSet.of(Modules.override(new HubModule(), new FabricatorModule(), new IntegrationTestModule()).with(
      new AbstractModule() {
        @Override
        public void configure() {
          bind(LibraryDAO.class).toInstance(libraryDAO);
          bind(InstrumentDAO.class).toInstance(instrumentDAO);
          bind(InstrumentAudioChordDAO.class).toInstance(instrumentAudioChordDAO);
          bind(InstrumentAudioDAO.class).toInstance(instrumentAudioDAO);
          bind(InstrumentAudioEventDAO.class).toInstance(instrumentAudioEventDAO);
          bind(InstrumentMemeDAO.class).toInstance(instrumentMemeDAO);
          bind(ProgramDAO.class).toInstance(programDAO);
          bind(ProgramSequencePatternEventDAO.class).toInstance(programEventDAO);
          bind(ProgramMemeDAO.class).toInstance(programMemeDAO);
          bind(ProgramSequencePatternDAO.class).toInstance(programPatternDAO);
          bind(ProgramSequenceBindingDAO.class).toInstance(programSequenceBindingDAO);
          bind(ProgramSequenceBindingMemeDAO.class).toInstance(programSequenceBindingMemeDAO);
          bind(ProgramSequenceChordDAO.class).toInstance(programSequenceChordDAO);
          bind(ProgramSequenceDAO.class).toInstance(programSequenceDAO);
          bind(ProgramVoiceTrackDAO.class).toInstance(programTrackDAO);
          bind(ProgramVoiceDAO.class).toInstance(programVoiceDAO);
          bind(SegmentDAO.class).toInstance(segmentDAO);
          bind(SegmentChoiceDAO.class).toInstance(segmentChoiceDAO);
          bind(ChainDAO.class).toInstance(chainDAO);
          bind(ChainConfigDAO.class).toInstance(chainConfigDAO);
          bind(ChainBindingDAO.class).toInstance(chainBindingDAO);
          bind(Tuning.class).toInstance(tuning);
          bind(TimeComputerFactory.class).toInstance(timeComputerFactory);
        }
      })));
    fabricatorFactory = injector.getInstance(FabricatorFactory.class);
  }

  @Test
  public void usesTimeComputer() throws Exception {
    // interconnected set of fake entities
    Library library = Library.create();
    User user = User.create();
    Program mainProgram = Program.create(user, library, ProgramType.Main, ProgramState.Published, "song", "C", 120, 0.6);
    ProgramSequence mainProgramSequence = ProgramSequence.create(mainProgram, 8, "part one", 0.5, "C", 120);
    ProgramSequenceBinding mainProgramSequenceBinding = ProgramSequenceBinding.create(mainProgramSequence, 5);
    Chain chain = Chain.create(Account.create(), "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null);
    Segment previousSegment = Segment.create(chain, 1, SegmentState.Crafted, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120, "seg123.ogg");
    Segment segment = Segment.create(chain, 2, SegmentState.Crafting, Instant.parse("2017-12-12T01:00:16.000000Z"), Instant.parse("2017-12-12T01:00:22.000000Z"), "G major", 8, 0.6, 240, "seg123.ogg");
    ChainBinding chainBinding = ChainBinding.create(chain, library);
    ChainConfig chainConfig = ChainConfig.create(chain, ChainConfigType.OutputEncoding, "PCM_SIGNED");
    SegmentChoice mainChoice = SegmentChoice.create(segment, ProgramType.Main, mainProgramSequenceBinding, 4);
    // mocks use fake entities
    when(timeComputerFactory.create(anyDouble(), anyDouble(), anyDouble()))
      .thenReturn(timeComputer);
    when(timeComputer.getSecondsAtPosition(anyDouble()))
      .thenReturn(Double.valueOf(0));
    when(chainDAO.readOne(any(), eq(chain.getId())))
      .thenReturn(chain);
    when(chainConfigDAO.readMany(any(), eq(ImmutableList.of(chain.getId()))))
      .thenReturn(ImmutableList.of(chainConfig));
    when(chainBindingDAO.readMany(any(), eq(ImmutableList.of(chain.getId()))))
      .thenReturn(ImmutableList.of(chainBinding));
    when(programDAO.readMany(any(), eq(ImmutableList.of(library.getId()))))
      .thenReturn(ImmutableList.of(mainProgram));
    when(programDAO.readIdsInLibraries(any(), eq(ImmutableList.of(library.getId()))))
      .thenReturn(ImmutableList.of(mainProgram.getId()));
    when(programDAO.readManyWithChildEntities(any(), eq(ImmutableList.of(mainProgram.getId()))))
      .thenReturn(ImmutableList.of(mainProgram, mainProgramSequence, mainProgramSequenceBinding));
    when(instrumentDAO.readIdsInLibraries(any(), eq(ImmutableList.of(library.getId()))))
      .thenReturn(ImmutableList.of());
    when(programSequenceDAO.readMany(any(), eq(ImmutableList.of(mainProgram.getId()))))
      .thenReturn(ImmutableList.of(mainProgramSequence));
    when(programSequenceBindingDAO.readMany(any(), eq(ImmutableList.of(mainProgram.getId()))))
      .thenReturn(ImmutableList.of(mainProgramSequenceBinding));
    when(segmentDAO.readAllSubEntities(any(), any(), eq(true)))
      .thenReturn(ImmutableList.of());
    when(segmentDAO.readOneAtChainOffset(any(), eq(chain.getId()), eq(Long.valueOf(1))))
      .thenReturn(previousSegment);
    when(segmentChoiceDAO.readOneOfTypeForSegment(any(), eq(previousSegment), eq(ProgramType.Main)))
      .thenReturn(mainChoice);
    // fabricator
    subject = fabricatorFactory.fabricate(Access.internal(), segment);

    Double result = subject.computeSecondsAtPosition(0); // instantiates a time computer; see expectation above

    assertEquals(Double.valueOf(0), result);
    verify(timeComputerFactory).create(8.0, 120, 240.0);
  }


  @Test
  public void pick_returned_by_picks() throws Exception {
    // interconnected set of fake entities
    Library library = Library.create();
    User user = User.create();
    Program rhythmProgram = Program.create(user, library, ProgramType.Rhythm, ProgramState.Published, "song", "C", 120, 0.6);
    ProgramSequence rhythmProgramSequence = ProgramSequence.create(rhythmProgram, 8, "part one", 0.5, "C", 120);
    ProgramVoice rhythmProgramVoice = ProgramVoice.create(rhythmProgram);
    ProgramSequencePatternEvent rhythmProgramEvent = ProgramSequencePatternEvent.create(rhythmProgram);
    Program mainProgram = Program.create(user, library, ProgramType.Main, ProgramState.Published, "song", "C", 120, 0.6);
    ProgramSequence mainProgramSequence = ProgramSequence.create(mainProgram, 8, "part one", 0.5, "C", 120);
    ProgramSequenceBinding mainProgramSequenceBinding = ProgramSequenceBinding.create(mainProgramSequence, 5);
    Instrument instrument = Instrument.create(library);
    InstrumentAudio audio = InstrumentAudio.create();
    Chain chain = Chain.create(Account.create(), "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null);
    Segment previousSegment = Segment.create(chain, 1, SegmentState.Crafted, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120, "seg123.ogg");
    Segment segment = Segment.create(chain, 2, SegmentState.Crafting, Instant.parse("2017-12-12T01:00:16.000000Z"), Instant.parse("2017-12-12T01:00:22.000000Z"), "G major", 8, 0.6, 240, "seg123.ogg");
    ChainBinding chainBinding = ChainBinding.create(chain, library);
    ChainConfig chainConfig = ChainConfig.create(chain, ChainConfigType.OutputEncoding, "PCM_SIGNED");
    SegmentChoice mainChoice = SegmentChoice.create(segment, ProgramType.Main, mainProgramSequenceBinding, 4);
    SegmentChoice rhythmChoice = SegmentChoice.create(segment, ProgramType.Rhythm, rhythmProgram, 0);
    SegmentChoiceArrangement rhythmArrangement = SegmentChoiceArrangement.create(segment, rhythmChoice, rhythmProgramVoice, instrument);
    SegmentChoiceArrangementPick rhythmPick = SegmentChoiceArrangementPick.create(rhythmArrangement)
      .setProgramSequencePatternEventId(rhythmProgramEvent.getId())
      .setInstrumentAudioId(audio.getId())
      .setName("CLANG")
      .setStart(0.273)
      .setLength(1.571)
      .setAmplitude(0.8)
      .setPitch(432.0);
    // mocks use fake entities
    when(timeComputerFactory.create(anyDouble(), anyDouble(), anyDouble()))
      .thenReturn(timeComputer);
    when(timeComputer.getSecondsAtPosition(anyDouble()))
      .thenReturn(Double.valueOf(0));
    when(chainDAO.readOne(any(), eq(chain.getId())))
      .thenReturn(chain);
    when(chainConfigDAO.readMany(any(), eq(ImmutableList.of(chain.getId()))))
      .thenReturn(ImmutableList.of(chainConfig));
    when(chainBindingDAO.readMany(any(), eq(ImmutableList.of(chain.getId()))))
      .thenReturn(ImmutableList.of(chainBinding));
    when(programDAO.readMany(any(), eq(ImmutableList.of(library.getId()))))
      .thenReturn(ImmutableList.of(mainProgram));
    when(programDAO.readIdsInLibraries(any(), eq(ImmutableList.of(library.getId()))))
      .thenReturn(ImmutableList.of(mainProgram.getId()));
    when(programDAO.readManyWithChildEntities(any(), eq(ImmutableList.of(mainProgram.getId()))))
      .thenReturn(ImmutableList.of(mainProgram, mainProgramSequence, mainProgramSequenceBinding, rhythmProgram, rhythmProgramSequence, rhythmProgramVoice, rhythmProgramEvent));
    when(instrumentDAO.readIdsInLibraries(any(), eq(ImmutableList.of(library.getId()))))
      .thenReturn(ImmutableList.of());
    when(programSequenceDAO.readMany(any(), eq(ImmutableList.of(mainProgram.getId()))))
      .thenReturn(ImmutableList.of(mainProgramSequence));
    when(programSequenceBindingDAO.readMany(any(), eq(ImmutableList.of(mainProgram.getId()))))
      .thenReturn(ImmutableList.of(mainProgramSequenceBinding));
    when(segmentDAO.readAllSubEntities(any(), any(), eq(true)))
      .thenReturn(ImmutableList.of(mainChoice, rhythmChoice, rhythmArrangement, rhythmPick));
    when(segmentDAO.readOneAtChainOffset(any(), eq(chain.getId()), eq(Long.valueOf(1))))
      .thenReturn(previousSegment);
    when(segmentChoiceDAO.readOneOfTypeForSegment(any(), eq(previousSegment), eq(ProgramType.Main)))
      .thenReturn(mainChoice);
    // fabricator
    subject = fabricatorFactory.fabricate(Access.internal(), segment);

    Collection<SegmentChoiceArrangementPick> result = subject.getSegmentPicks();

    SegmentChoiceArrangementPick resultPick = result.iterator().next();
    assertEquals(rhythmArrangement.getId(), resultPick.getSegmentChoiceArrangementId());
    assertEquals(audio.getId(), resultPick.getInstrumentAudioId());
    assertEquals(0.273, resultPick.getStart(), 0.001);
    assertEquals(1.571, resultPick.getLength(), 0.001);
    assertEquals(0.8, resultPick.getAmplitude(), 0.1);
    assertEquals(432.0, resultPick.getPitch(), 0.1);
  }

  /*
  FUTURE implement additional tests, after [#170035559] Split the FabricatorImplTest into separate tests of the FabricatorImpl, SegmentWorkbenchImpl, SegmentRetrospectiveImpl, and IngestImpl

  @Test
  public void timeComputer_variedSegmentTotals() throws Exception {
    Chain chain = Chain.create();
    when(chainDAO.readOne(any(), eq(chain.getId())))
      .thenReturn(Chain.create(Account.create(), "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null));
    when(segmentDAO.readOneAtChainOffset(any(), eq(chain.getId()), eq(Long.valueOf(1))))
      .thenReturn(Segment.create(Chain.create(), 1, SegmentState.Crafted, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 32, 0.6, 120, "seg123.ogg"));
    when(segmentDAO.readAllSubEntities(any(), any(), any())).thenReturn(ImmutableList.of());
    when(timeComputerFactory.create(anyDouble(), anyDouble(), anyDouble())).thenReturn(timeComputer);
    when(timeComputer.getSecondsAtPosition(anyDouble())).thenReturn(Double.valueOf(0));
    subject = fabricatorFactory.fabricate(Access.internal(), Segment.create()
      .setOffset(2L)
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(240.0)
      .setChainId(chain.getId())
      .setTotal(16)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z"));

    assertEquals(Double.valueOf(0), subject.computeSecondsAtPosition(0)); // instantiates a time computer; see expectation above

    verify(timeComputerFactory).create(16, 120, 240.0);
  }

  @Test
  public void timeComputer_weirdSegmentTotalsAndTempos() throws Exception {
    Chain chain = Chain.create();
    when(chainDAO.readOne(any(), eq(chain.getId())))
      .thenReturn(Chain.create(Account.create(), "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null));
    when(segmentDAO.readOneAtChainOffset(any(), eq(chain.getId()), eq(Long.valueOf(1))))
      .thenReturn(Segment.create(Chain.create(), 1, SegmentState.Crafted, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 23, 0.6, 67, "seg123.ogg"));
    when(segmentDAO.readAllSubEntities(any(), any(), any())).thenReturn(ImmutableList.of());
    when(timeComputerFactory.create(anyDouble(), anyDouble(), anyDouble())).thenReturn(timeComputer);
    when(timeComputer.getSecondsAtPosition(anyDouble())).thenReturn(Double.valueOf(0));
    subject = fabricatorFactory.fabricate(Access.internal(), Segment.create()
      .setOffset(2L)
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(121.0)
      .setChainId(chain.getId())
      .setTotal(79)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z"));

    assertEquals(Double.valueOf(0), subject.computeSecondsAtPosition(0)); // instantiates a time computer; see expectation above

    verify(timeComputerFactory).create(79, 67.0, 121.0);
  }

  @Test
  public void getOutputAudioEncoding() throws Exception {
    Chain chain = Chain.create();
    Library library = Library.create();
    Program program = Program.create();
    Segment segment = Segment.create(Chain.create(), 1, SegmentState.Crafted, Instant.parse("2017-12-12T01:00:08.000000Z"), Instant.parse("2017-12-12T01:00:16.000000Z"), "F major", 8, 0.6, 120, "seg123.ogg");
    when(programDAO.readIdsInLibraries(any(), eq(ImmutableList.of(library.getId()))))
      .thenReturn(ImmutableList.of());
    when(instrumentDAO.readIdsInLibraries(any(), eq(ImmutableList.of(library.getId()))))
      .thenReturn(ImmutableList.of());
    when(chainConfigDAO.readMany(any(), eq(ImmutableList.of(chain.getId()))))
      .thenReturn(ImmutableList.of(ChainConfig.create(chain, ChainConfigType.OutputEncoding, "PCM_SIGNED")));
    when(chainBindingDAO.readMany(any(), eq(ImmutableList.of(chain.getId()))))
      .thenReturn(ImmutableList.of(ChainBinding.create(chain, library)));
    when(chainDAO.readOne(any(), eq(chain.getId())))
      .thenReturn(Chain.create(Account.create(), "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null));
    when(chainDAO.readOne(any(), eq(chain.getId())))
      .thenReturn(Chain.create(Account.create(), "test", ChainType.Production, ChainState.Fabricate, Instant.parse("2017-12-12T01:00:08.000000Z"), null, null));
    when(segmentDAO.readOneAtChainOffset(any(), eq(chain.getId()), eq(Long.valueOf(1))))
      .thenReturn(segment);
    when(segmentChoiceDAO.readOneOfTypeForSegment(any(), any(), eq(ProgramType.Main))).thenReturn(SegmentChoice.create(segment, ProgramType.Main, program, 4));

    subject = fabricatorFactory.fabricate(Access.internal(), Segment.create()
      .setOffset(2L)
      .setDensity(0.6)
      .setKey("G major")
      .setState("Crafting")
      .setTempo(240.0)
      .setChainId(chain.getId())
      .setTotal(8)
      .setBeginAt("2017-12-12T01:00:16.000000Z")
      .setEndAt("2017-12-12T01:00:22.000000Z"));

    assertEquals("PCM_SIGNED", subject.getOutputAudioFormat().getEncoding().toString()); // instantiates a time computer; see expectation above
  }



  @Test
  public void choice_getChoiceOfType() throws Exception {
    // segment
    segment
      .setChainId(UUID.randomUUID())
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted");
    // memes
    segment.add(of("Hindsight"));
    segment.add(of("Chunky"));
    segment.add(of("Regret"));
    segment.add(of("Tangy"));
    // choices
    UUID id1 = UUID.randomUUID();
    segment.add(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceBindingId(id1)
      .setTypeEnum(ProgramType.Macro)
      .setTranspose(4));
    UUID id2 = UUID.randomUUID();
    segment.add(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceBindingId(id2)
      .setTypeEnum(ProgramType.Main)
      .setTranspose(-2));
    segment.add(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setTypeEnum(ProgramType.Rhythm)
      .setTranspose(-4));
    // chords
    segment.add(of(0.0, "F minor"));
    segment.add(of(8.0, "Gb minor"));

    assertEquals(id1, segment.getChoiceOfType(ProgramType.Macro).getProgramSequenceBindingId());
    assertEquals(id2, segment.getChoiceOfType(ProgramType.Main).getProgramSequenceBindingId());
    assertEquals(BigInteger.valueOf(35), segment.getChoiceOfType(ProgramType.Rhythm).getProgramId());
  }

  @Test
  public void entity_getAllEntities() throws Exception {
    // segment
    segment
      .setChainId(UUID.randomUUID())
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted");
    // memes
    segment.add(of("Hindsight"));
    segment.add(of("Chunky"));
    segment.add(of("Regret"));
    segment.add(of("Tangy"));
    // chords
    segment.add(of(0.0, "F minor"));
    segment.add(of(8.0, "Gb minor"));
    // choices
    SegmentChoice choice1 = segment.add(of(ProgramType.Macro, 5, UUID.randomUUID(), 4));
    SegmentChoice choice2 = segment.add(of(ProgramType.Main, 5, UUID.randomUUID(), -2));
    SegmentChoice choice3 = segment.add(of(ProgramType.Rhythm, 5, UUID.randomUUID(), -4));
    // arrangements
    SegmentChoiceArrangement arrangement1 = segment.add(of(choice1));
    SegmentChoiceArrangement arrangement2 = segment.add(of(choice2));
    SegmentChoiceArrangement arrangement3 = segment.add(of(choice3));
    // picks (currently NOT included in getAllSubEntities)
    segment.add(of(arrangement1));
    segment.add(of(arrangement2));
    segment.add(of(arrangement3));

    assertEquals(12, segment.getAllSubEntities().size());
    segment.validate();
  }

  @Test
  public void arrangement_addAssignsUniqueIds() throws CoreException {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    segment.add(of(choice));
    segment.add(of(choice));
    segment.add(of(choice));

    assertEquals(3, segment.getArrangements().size());
    segment.validateContent();
  }

  @Test
  public void arrangement_addExceptionOnDuplicateId() throws CoreException {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    SegmentChoiceArrangement arrangement = new SegmentChoiceArrangement()
      .setId(UUID.randomUUID())
      .setSegmentChoiceId(choice.getId())
      .setProgramVoiceId(UUID.randomUUID())
      .setInstrumentId(UUID.randomUUID());
    segment.add(arrangement);

    segment.add(arrangement);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void arrangement_addExceptionOnBadRelationId() throws CoreException {
    segment.add(of(new SegmentChoice().setId(UUID.randomUUID())));

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("nonexistent choice", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void arrangement_afterLoadingNewEntityHasUniqueId() throws CoreException {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    segment.setArrangements(ImmutableList.of(
      of(choice),
      of(choice)
    ));
    segment.add(of(choice));

    segment.validateContent();
  }

  @Test
  public void arrangement_getAllForChoice() throws CoreException {
    SegmentChoice choiceB = segment.add(of(UUID.randomUUID()));
    SegmentChoice choiceA = segment.add(of(UUID.randomUUID()));
    segment.add(of(choiceA));
    segment.add(of(choiceB));
    segment.add(of(choiceB));

    assertEquals(2, segment.getArrangements(choiceB).size());
    segment.validateContent();
  }

  @Test
  public void arrangement_setAssignsUniqueIds() throws CoreException {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    segment.setArrangements(ImmutableList.of(
      of(choice),
      of(choice),
      of(choice)));

    assertEquals(3, segment.getArrangements().size());
    segment.validateContent();
  }

  @Test
  public void arrangement_setWithImmutableList_thenAddAnother() throws CoreException {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    segment.setArrangements(ImmutableList.of(
      of(choice),
      of(choice),
      of(choice)));
    segment.add(of(choice));

    assertEquals(4, segment.getArrangements().size());
    segment.validateContent();
  }

  @Test
  public void choice_addAssignsUniqueIds() throws CoreException {
    segment.add(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setTypeEnum(ProgramType.Macro)
      .setProgramSequenceBindingId(UUID.randomUUID()));
    segment.add(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setTypeEnum(ProgramType.Main)
      .setProgramSequenceBindingId(UUID.randomUUID()));
    segment.add(new SegmentChoice()
      .setTypeEnum(ProgramType.Rhythm)
      .setProgramId(UUID.randomUUID()));

    segment.validateContent();
  }

  @Test
  public void choice_addExceptionOnDuplicateId() throws CoreException {
    SegmentChoice choice = new SegmentChoice()
      .setId(UUID.randomUUID())
      .setProgramId(UUID.randomUUID())
      .setTypeEnum(ProgramType.Macro)
      .setProgramSequenceBindingId(UUID.randomUUID());
    segment.add(choice);

    segment.add(choice);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void choice_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setChoices(ImmutableList.of(
      of(ProgramType.Macro, 5, UUID.randomUUID(), 0),
      of(ProgramType.Main, 5, UUID.randomUUID(), 0)));
    segment.add(new SegmentChoice()
      .setTypeEnum(ProgramType.Rhythm)
      .setProgramId(UUID.randomUUID()));

    segment.validateContent();
  }

  @Test
  public void choice_setAssignsUniqueIds() throws CoreException {
    segment.setChoices(ImmutableList.of(new SegmentChoice()
        .setProgramId(UUID.randomUUID())
        .setTypeEnum(ProgramType.Macro)
        .setProgramSequenceBindingId(UUID.randomUUID()),
      new SegmentChoice()
        .setProgramId(UUID.randomUUID())
        .setTypeEnum(ProgramType.Main)
        .setProgramSequenceBindingId(UUID.randomUUID()),
      new SegmentChoice()
        .setProgramId(UUID.randomUUID())
        .setTypeEnum(ProgramType.Rhythm)
        .setProgramId(UUID.randomUUID())));

    assertEquals(3, segment.getChoices().size());
    segment.validateContent();
  }

  @Test
  public void choice_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setChoices(ImmutableList.of(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setTypeEnum(ProgramType.Macro)
      .setProgramSequenceBindingId(UUID.randomUUID())));
    segment.add(new SegmentChoice()
      .setProgramId(UUID.randomUUID())
      .setTypeEnum(ProgramType.Main)
      .setProgramSequenceBindingId(UUID.randomUUID()));

    assertEquals(2, segment.getChoices().size());
    segment.validateContent();
  }

  @Test
  public void chord_addAssignsUniqueIds() throws CoreException {
    segment.add(of(0, "C# Major"));
    segment.add(of(1, "D7"));
    segment.add(of(2, "G minor"));

    assertExactChords(ImmutableList.of("C# Major", "D7", "G minor"), segment.getChords());
    segment.validateContent();
  }

  @Test
  public void chord_addExceptionOnDuplicateId() throws CoreException {
    SegmentChord chord1 = of(0.0, "C");
    chord1.setId(UUID.randomUUID());
    segment.add(chord1);

    segment.add(chord1);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void chord_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setChords(ImmutableList.of(
      of(0.0, "C# Major"),
      of(0.0, "D7")));
    segment.add(new SegmentChord()
      .setPosition(0.0)
      .setName("G minor"));

    segment.validateContent();
  }

  @Test
  public void chord_setAssignsUniqueIds() throws CoreException {
    segment.setChords(ImmutableList.of(
      of(0, "C# Major"),
      of(1, "D7"),
      of(2, "G minor")
    ));

    assertExactChords(ImmutableList.of("C# Major", "D7", "G minor"), segment.getChords());
    segment.validateContent();
  }

  @Test
  public void chord_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setChords(ImmutableList.of(of(0, "C# Major")));
    segment.add(of(0, "D7"));

    assertEquals(2, segment.getChords().size());
    segment.validateContent();
  }

  @Test
  public void meme_addAssignsUniqueIds() throws CoreException {
    segment.add(of("Red"));
    segment.add(of("Yellow"));
    segment.add(of("Blue"));

    assertExactMemes(ImmutableList.of("Red", "Yellow", "Blue"), segment.getMemes());
    segment.validateContent();
  }

  @Test
  public void meme_addExceptionOnDuplicateId() throws CoreException {
    SegmentMeme meme1 = of("Test");
    meme1.setId(UUID.randomUUID());
    segment.add(meme1);

    segment.add(meme1);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void meme_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setMemes(ImmutableList.of(
      of("Red"),
      of("Yellow")));
    segment.add(of("Blue"));

    segment.validateContent();
  }

  @Test
  public void meme_setAssignsUniqueIds() throws CoreException {
    segment.setMemes(ImmutableList.of(
      of("Red"),
      of("Yellow"),
      of("Blue"))
    );

    assertExactMemes(ImmutableList.of("Red", "Yellow", "Blue"), segment.getMemes());
    segment.validateContent();
  }

  @Test
  public void meme_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setMemes(ImmutableList.of(
      of("Red")));
    segment.add(of("Yellow"));

    assertEquals(2, segment.getMemes().size());
    segment.validateContent();
  }

  @Test
  public void message_addAssignsUniqueIds() throws CoreException {
    segment.add(of(MessageType.Info, "All is Well!"));
    segment.add(of(MessageType.Warning, "This is your Final Warning."));
    segment.add(of(MessageType.Error, "Danger, Will Robinson!"));

    assertEquals(3, segment.getMessages().size());
    segment.validateContent();
  }

  @Test
  public void message_addExceptionOnDuplicateId() throws CoreException {
    SegmentMessage message1 = of(MessageType.Info, "Test");
    message1.setId(UUID.randomUUID());
    segment.add(message1);

    segment.add(message1);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void message_afterLoadingNewEntityHasUniqueId() throws CoreException {
    segment.setMessages(ImmutableList.of(
      of(MessageType.Info, "All is Well!"),
      of(MessageType.Warning, "This is your Final Warning.")));
    segment.add(of(MessageType.Error, "Danger, Will Robinson!"));

    segment.validateContent();
  }

  @Test
  public void message_setAssignsUniqueIds() throws CoreException {
    segment.setMessages(ImmutableList.of(
      of(MessageType.Info, "All is Well!"),
      of(MessageType.Warning, "This is your Final Warning."),
      of(MessageType.Error, "Danger, Will Robinson!")
    ));

    assertEquals(3, segment.getMessages().size());
    segment.validateContent();
  }

  @Test
  public void message_setWithImmutableList_thenAddAnother() throws CoreException {
    segment.setMessages(ImmutableList.of(
      of(MessageType.Info, "All is Well!")));
    segment.add(of(MessageType.Warning, "This is your Final Warning."));

    assertEquals(2, segment.getMessages().size());
    segment.validateContent();
  }

  @Test
  public void pick_addAssignsUniqueIds() throws CoreException {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    SegmentChoiceArrangement arrangement = segment.add(of(choice));
    segment.add(of(arrangement));
    segment.add(of(arrangement));
    segment.add(of(arrangement));

    assertEquals(3, segment.getPicks().size());
    segment.validateContent();
  }

  @Test
  public void pick_addExceptionOnDuplicateId() throws CoreException {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    SegmentChoiceArrangement arrangement = segment.add(of(choice));
    SegmentChoiceArrangementPick pk = of(arrangement);
    pk.setId(UUID.randomUUID());
    segment.add(pk);

    segment.add(pk);

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("already exists", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void pick_addExceptionOnBadRelationId() throws CoreException {
    segment.add(of(new SegmentChoiceArrangement().setId(UUID.randomUUID())));

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("nonexistent arrangement", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void pick_afterLoadingNewEntityHasUniqueId() throws CoreException {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    SegmentChoiceArrangement arrangement = segment.add(of(choice));
    SegmentChoiceArrangementPick pick1 = of(arrangement);
    pick1.setId(UUID.randomUUID());
    SegmentChoiceArrangementPick pick2 = of(arrangement);
    pick2.setId(UUID.randomUUID());
    segment.setPicks(ImmutableList.of(pick1, pick2));
    segment.add(of(arrangement));

    segment.validateContent();
  }

  @Test
  public void pick_setAssignsUniqueIds() throws CoreException {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    SegmentChoiceArrangement arrangement = segment.add(of(choice));
    segment.setPicks(ImmutableList.of(
      of(arrangement),
      of(arrangement),
      of(arrangement)));

    assertEquals(3, segment.getPicks().size());
    segment.validateContent();
  }

  @Test
  public void pick_setWithImmutableList_thenAddAnother() throws CoreException {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    SegmentChoiceArrangement arrangement = segment.add(of(choice));
    segment.setPicks(ImmutableList.of(
      of(arrangement),
      of(arrangement),
      of(arrangement)));
    segment.add(of(arrangement));

    assertEquals(4, segment.getPicks().size());
    segment.validateContent();
  }

  @Test
  public void type_getSet() throws CoreException {
    segment.setType("Initial");

    assertEquals(SegmentType.Initial, segment.getType());
    segment.validateContent();
  }

  @Test
  public void typeEnum_getSet() throws CoreException {
    segment.setTypeEnum(SegmentType.Initial);

    assertEquals(SegmentType.Initial, segment.getType());
    segment.validateContent();
  }

  @Test
  public void add_failsOnInvalidChoice() {
    segment.add(new SegmentChoice());

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("Program ID is required", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidArrangement() {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));

    segment.add(new SegmentChoiceArrangement().setSegmentChoiceId(choice.getId()));

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("Voice ID is required", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidArrangementRelation() {

    segment.add(new SegmentChoiceArrangement());

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("has null choiceId", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidPick() {
    SegmentChoice choice = segment.add(of(UUID.randomUUID()));
    segment.add(of(choice));

    segment.add(new SegmentChoiceArrangementPick().setSegmentChoiceArrangementId(UUID.randomUUID()));

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("has nonexistent arrangementId", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidPickRelation() {
    segment.add(new SegmentChoiceArrangementPick());

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("has null arrangementId", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidMeme() {
    segment.add(new SegmentMeme());

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("Name is required", segment.getErrors().iterator().next().getMessage());
  }

  @Test
  public void add_failsOnInvalidChord() {
    segment.add(new SegmentChord());

    assertEquals(CoreException.class, segment.getErrors().iterator().next().getClass());
    assertContains("Name is required", segment.getErrors().iterator().next().getMessage());
  }

   */

}
