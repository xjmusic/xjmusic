//  Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import io.xj.api.Account;
import io.xj.api.AccountUser;
import io.xj.api.ContentBindingType;
import io.xj.api.Instrument;
import io.xj.api.InstrumentAudio;
import io.xj.api.InstrumentMeme;
import io.xj.api.InstrumentState;
import io.xj.api.InstrumentType;
import io.xj.api.Library;
import io.xj.api.Program;
import io.xj.api.ProgramMeme;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequenceBinding;
import io.xj.api.ProgramSequenceBindingMeme;
import io.xj.api.ProgramSequenceChord;
import io.xj.api.ProgramSequenceChordVoicing;
import io.xj.api.ProgramSequencePattern;
import io.xj.api.ProgramSequencePatternEvent;
import io.xj.api.ProgramSequencePatternType;
import io.xj.api.ProgramState;
import io.xj.api.ProgramType;
import io.xj.api.ProgramVoice;
import io.xj.api.ProgramVoiceTrack;
import io.xj.api.Template;
import io.xj.api.TemplateBinding;
import io.xj.api.TemplatePlayback;
import io.xj.api.TemplateType;
import io.xj.api.User;
import io.xj.api.UserRole;
import io.xj.api.UserRoleType;
import io.xj.lib.jsonapi.JsonapiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/**
 [#165954673] Integration tests use shared scenario fixtures as much as possible
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
public class IntegrationTestingFixtures {
  private static final Logger log = LoggerFactory.getLogger(IntegrationTestingFixtures.class);
  private final HubIntegrationTestProvider test;

  // These are fully exposed (no getters/setters) for ease of use in testing
  public Account account1;
  public Account account2;
  public Instrument instrument201;
  public Instrument instrument202;
  public Instrument instrument251;
  public Instrument instrument8;
  public Instrument instrument9;
  public InstrumentAudio audio1;
  public InstrumentAudio audio2;
  public InstrumentAudio audio401;
  public InstrumentAudio audio8bleep;
  public InstrumentAudio audio8kick;
  public InstrumentAudio audio8snare;
  public InstrumentAudio audio8toot;
  public InstrumentAudio instrument201_audio402;
  public Library library10000001;
  public Library library10000002;
  public Library library1;
  public Library library1a;
  public Library library1b;
  public Library library2;
  public Library library2a;
  public Library library2b;
  public Program program15;
  public Program program1;
  public Program program2;
  public Program program35;
  public Program program3;
  public Program program4;
  public Program program701;
  public Program program702;
  public Program program703;
  public Program program751;
  public Program program9;
  public ProgramMeme program701_meme0;
  public ProgramMeme programMeme1;
  public ProgramMeme programMeme35;
  public ProgramMeme programMeme3;
  public ProgramSequence program1_sequence1;
  public ProgramSequence program3_sequence1;
  public ProgramSequence programSequence35;
  public ProgramSequenceBinding program15_binding0;
  public ProgramSequenceBinding program15_binding1;
  public ProgramSequenceBinding program3_binding0;
  public ProgramSequenceBinding program3_binding1;
  public ProgramSequenceChord program3_chord1;
  public ProgramSequencePattern program2_sequence1_pattern1;
  public ProgramSequencePatternEvent program2_sequence1_pattern1_event0;
  public ProgramSequencePatternEvent program2_sequence1_pattern1_event1;
  public ProgramSequencePatternEvent program702_pattern901_boomEvent;
  public ProgramVoice program2_voice1;
  public ProgramVoice program2_voice2;
  public ProgramVoiceTrack program2_voice1_track0;
  public ProgramVoiceTrack program2_voice1_track1;
  public User user101;
  public User user1;
  public User user2;
  public User user3;
  public User user4;
  public User user53;
  public User user5;
  public Template template1;
  public TemplateBinding templateBinding1;
  //
  public HubContentFixtures content;


  /**
   Library of Content A (shared test fixture)
   */
  public void insertFixtureA() throws HubException, JsonapiException {
    // account
    account1 = test.insert(buildAccount("testing"));
    user101 = test.insert(buildUser("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(buildUserRole(user101, UserRoleType.ADMIN));

    // Library content all created at this known time
    Instant at = Instant.parse("2014-08-12T12:17:02.527142Z");
    library10000001 = test.insert(buildLibrary(account1, "leaves"));

    // Templates: enhanced preview chain creation for artists in Lab UI #178457569
    template1 = test.insert(buildTemplate(account1, "test", UUID.randomUUID().toString()));
    templateBinding1 = test.insert(buildTemplateBinding(template1, library10000001));

    // Instrument 201
    instrument201 = test.insert(buildInstrument(library10000001, InstrumentType.PERCUSSIVE, InstrumentState.PUBLISHED, "808 Drums"));
    test.insert(buildInstrumentMeme(instrument201, "Ants"));
    test.insert(buildInstrumentMeme(instrument201, "Mold"));
    //
    instrument201_audio402 = test.insert(buildInstrumentAudio(instrument201, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01, 2.123, 120.0, 0.62, "KICK", "Eb", 1.0));
    //
    var audio401 = test.insert(buildInstrumentAudio(instrument201, "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 0.62, "KICK", "Eb", 1.0));

    // Instrument 202
    instrument202 = test.insert(buildInstrument(library10000001, InstrumentType.PERCUSSIVE, InstrumentState.PUBLISHED, "909 Drums"));
    test.insert(buildInstrumentMeme(instrument202, "Peel"));

    // Program 701, main-type, has sequence with chords, bound to many offsets
    program701 = test.insert(buildProgram(library10000001, ProgramType.MAIN, ProgramState.PUBLISHED, "leaves", "C#", 120.4, 0.6));
    program701_meme0 = test.insert(buildProgramMeme(program701, "Ants"));
    var sequence902 = test.insert(buildProgramSequence(program701, 16, "decay", 0.25, "F#", 110.3));
    test.insert(buildProgramSequenceChord(sequence902, 0.0, "G minor"));
    test.insert(buildProgramSequenceChord(sequence902, 4.0, "C major"));
    test.insert(buildProgramSequenceChord(sequence902, 8.0, "F7"));
    test.insert(buildProgramSequenceChord(sequence902, 12.0, "G7"));
    test.insert(buildProgramSequenceChord(sequence902, 16.0, "F minor"));
    test.insert(buildProgramSequenceChord(sequence902, 20.0, "Bb major"));
    var binding902_0 = test.insert(buildProgramSequenceBinding(sequence902, 0));
    var binding902_1 = test.insert(buildProgramSequenceBinding(sequence902, 1));
    var binding902_2 = test.insert(buildProgramSequenceBinding(sequence902, 2));
    var binding902_3 = test.insert(buildProgramSequenceBinding(sequence902, 3));
    var binding902_4 = test.insert(buildProgramSequenceBinding(sequence902, 4));
    test.insert(buildProgramSequenceBinding(sequence902, 5));
    test.insert(buildProgramSequenceBindingMeme(binding902_0, "Gravel"));
    test.insert(buildProgramSequenceBindingMeme(binding902_1, "Gravel"));
    test.insert(buildProgramSequenceBindingMeme(binding902_2, "Gravel"));
    test.insert(buildProgramSequenceBindingMeme(binding902_3, "Rocks"));
    test.insert(buildProgramSequenceBindingMeme(binding902_1, "Fuzz"));
    test.insert(buildProgramSequenceBindingMeme(binding902_2, "Fuzz"));
    test.insert(buildProgramSequenceBindingMeme(binding902_3, "Fuzz"));
    test.insert(buildProgramSequenceBindingMeme(binding902_4, "Noise"));

    // Program 702, rhythm-type, has unbound sequence with pattern with events
    program702 = test.insert(buildProgram(library10000001, ProgramType.RHYTHM, ProgramState.PUBLISHED, "coconuts", "F#", 110.3, 0.6));
    test.insert(buildProgramMeme(program702, "Ants"));
    program2_voice1 = test.insert(buildProgramVoice(program702, InstrumentType.PERCUSSIVE, "Drums"));
    var sequence702a = test.insert(buildProgramSequence(program702, 16, "Base", 0.5, "C", 110.3));
    var pattern901 = test.insert(buildProgramSequencePattern(sequence702a, program2_voice1, ProgramSequencePatternType.LOOP, 16, "growth"));
    var trackBoom = test.insert(buildProgramVoiceTrack(program2_voice1, "BOOM"));
    var trackSmack = test.insert(buildProgramVoiceTrack(program2_voice1, "BOOM"));
    program702_pattern901_boomEvent = test.insert(buildProgramSequencePatternEvent(pattern901, trackBoom, 0.0, 1.0, "C", 1.0));
    test.insert(buildProgramSequencePatternEvent(pattern901, trackSmack, 1.0, 1.0, "G", 0.8));
    test.insert(buildProgramSequencePatternEvent(pattern901, trackBoom, 2.5, 1.0, "C", 0.6));
    test.insert(buildProgramSequencePatternEvent(pattern901, trackSmack, 3.0, 1.0, "G", 0.9));

    // Program 703
    program703 = test.insert(buildProgram(library10000001, ProgramType.MAIN, ProgramState.PUBLISHED, "bananas", "Gb", 100.6, 0.6));
    test.insert(buildProgramMeme(program703, "Peel"));

    // DELIBERATELY UNUSED stuff that should not get used because it's in a different library
    library10000002 = test.insert(buildLibrary(account1, "Garbage Library"));
    //
    instrument251 = test.insert(buildInstrument(library10000002, InstrumentType.PERCUSSIVE, InstrumentState.PUBLISHED, "Garbage Instrument"));
    test.insert(buildInstrumentMeme(instrument251, "Garbage MemeObject"));
    //
    program751 = test.insert(buildProgram(library10000002, ProgramType.RHYTHM, ProgramState.PUBLISHED, "coconuts", "F#", 110.3, 0.6));
    test.insert(buildProgramMeme(program751, "Ants"));
    var voiceGarbage = test.insert(buildProgramVoice(program751, InstrumentType.PERCUSSIVE, "Garbage"));
    var sequence751a = test.insert(buildProgramSequence(program751, 16, "Base", 0.5, "C", 110.3));
    var pattern951 = test.insert(buildProgramSequencePattern(sequence751a, voiceGarbage, ProgramSequencePatternType.LOOP, 16, "Garbage"));
    var trackGr = test.insert(buildProgramVoiceTrack(voiceGarbage, "GR"));
    var trackBag = test.insert(buildProgramVoiceTrack(voiceGarbage, "BAG"));
    test.insert(buildProgramSequencePatternEvent(pattern951, trackGr, 0.0, 1.0, "C", 1.0));
    test.insert(buildProgramSequencePatternEvent(pattern951, trackBag, 1.0, 1.0, "G", 0.8));
    test.insert(buildProgramSequencePatternEvent(pattern951, trackGr, 2.5, 1.0, "C", 0.6));
    test.insert(buildProgramSequencePatternEvent(pattern951, trackBag, 3.0, 1.0, "G", 0.9));
  }

  /**
   Library of Content B-1 (shared test fixture)
   */
  public void insertFixtureB1() throws HubException {
    Collection<Object> entities = content.setupFixtureB1(true);
    for (Object Object : entities) {
      test.insert(Object);
    }
  }

  /**
   Library of Content B-1 (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  public void insertFixtureB2() throws HubException {
    Collection<Object> entities = content.setupFixtureB2();
    for (Object Object : entities) {
      test.insert(Object);
    }
  }

  /**
   Library of Content B-3 (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   <p>
   [#163158036] memes bound to sequence-pattern because sequence-binding is not considered for rhythm sequences, rhythm sequence patterns do not have memes.
   <p>
   [#165954619] Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (rhythm- and detail-type sequences)
   <p>
   [#153976073] Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment.
   + For this test, there's an Intro Pattern with all BLEEPS, multiple Loop Patterns with KICK and SNARE (2x each), and an Outro Pattern with all TOOTS.
   <p>
   [#150279647] Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset.
   */
  public void insertFixtureB3() throws HubException {
    Collection<Object> entities = content.setupFixtureB3();
    for (Object Object : entities) {
      test.insert(Object);
    }
  }

  /**
   Library of Content B: Instruments (shared test fixture)
   <p>
   [#165954673] Integration tests use shared scenario fixtures as much as possible
   */
  public void insertFixtureB_Instruments() throws HubException, JsonapiException {
    instrument201 = test.insert(buildInstrument(library2, InstrumentType.PERCUSSIVE, InstrumentState.PUBLISHED, "808 Drums"));
    test.insert(buildInstrumentMeme(instrument201, "Ants"));
    test.insert(buildInstrumentMeme(instrument201, "Mold"));
    //
    audio401 = test.insert(buildInstrumentAudio(instrument201, "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 0.62, "KICK", "Eb", 1.0));
    //
    var audio402 = test.insert(buildInstrumentAudio(instrument201, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01, 2.123, 120.0, 0.62, "KICK", "Eb", 1.0));
  }

  /**
   Create a new Integration Testing Fixtures instance by providing the integration test provider
   */
  public IntegrationTestingFixtures(HubIntegrationTestProvider hubIntegrationTestProvider) {
    test = hubIntegrationTestProvider;
    content = new HubContentFixtures();
  }

  /**
   Create a new Integration Testing Fixtures instance by providing the integration test provider and content fixtures
   */
  public IntegrationTestingFixtures(HubIntegrationTestProvider hubIntegrationTestProvider, HubContentFixtures content) {
    test = hubIntegrationTestProvider;
    this.content = content;
  }

  public static InstrumentAudio buildInstrumentAudio(Instrument instrument, String name, String waveformKey, double start, double length, double tempo, double density, String event, String note, double volume) {
    return new InstrumentAudio()
      .id(UUID.randomUUID())
      .instrumentId(instrument.getId())
      .name(name)
      .waveformKey(waveformKey)
      .start(start)
      .length(length)
      .tempo(tempo)
      .density(density)
      .volume(volume)
      .note(note)
      .event(event);
  }

  public static User buildUser(String name, String email, String avatarUrl) {
    return new User()
      .id(UUID.randomUUID())
      .email(email)
      .avatarUrl(avatarUrl)
      .name(name);
  }

  public static Library buildLibrary(Account account, String name) {
    return new Library()
      .id(UUID.randomUUID())
      .accountId(account.getId())
      .name(name);
  }

  public static Account buildAccount(String name) {
    return new Account()
      .id(UUID.randomUUID())
      .name(name);
  }

  /**
   NOTE: it's crucial tht a test template configuration disable certain aleatory features,
   e.g. `choiceDeltaEnabled = false` to disable choice delta randomness,
   otherwise tests may sporadically fail.
   */
  public static Template buildTemplate(Account account1, TemplateType type, String name, String embedKey) {
    return new Template()
      .id(UUID.randomUUID())
      .embedKey(embedKey)
      .type(type)
      .config("outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\nchoiceDeltaEnabled = false\n")
      .accountId(account1.getId())
      .name(name);
  }

  public static Template buildTemplate(Account account1, String name, String embedKey) {
    return buildTemplate(account1, TemplateType.PREVIEW, name, embedKey);
  }

  public static TemplateBinding buildTemplateBinding(Template template, Library library) {
    return new TemplateBinding()
      .id(UUID.randomUUID())
      .type(ContentBindingType.LIBRARY)
      .targetId(library.getId())
      .templateId(template.getId());
  }

  public static TemplateBinding buildTemplateBinding(Template template, Instrument instrument) {
    return new TemplateBinding()
      .id(UUID.randomUUID())
      .type(ContentBindingType.INSTRUMENT)
      .targetId(instrument.getId())
      .templateId(template.getId());
  }

  public static TemplateBinding buildTemplateBinding(Template template, Program program) {
    return new TemplateBinding()
      .id(UUID.randomUUID())
      .type(ContentBindingType.PROGRAM)
      .targetId(program.getId())
      .templateId(template.getId());
  }

  public static TemplatePlayback buildTemplatePlayback(Template template, User user) {
    return new TemplatePlayback()
      .id(UUID.randomUUID())
      .userId(user.getId())
      .templateId(template.getId());
  }

  public static UserRole buildUserRole(User user, UserRoleType type) {
    return new UserRole()
      .id(UUID.randomUUID())
      .userId(user.getId())
      .type(type);
  }

  public static AccountUser buildAccountUser(Account account, User user) {
    return new AccountUser()
      .id(UUID.randomUUID())
      .accountId(account.getId())
      .userId(user.getId());
  }

  public static Program buildProgram(Library library, ProgramType type, ProgramState state, String name, String key, double tempo, double density) {
    return new Program()
      .id(UUID.randomUUID())
      .libraryId(library.getId())
      .type(type)
      .state(state)
      .name(name)
      .key(key)
      .tempo(tempo)
      .density(density);
  }

  public static ProgramMeme buildProgramMeme(Program program, String name) {
    return new ProgramMeme()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .name(name);
  }

  public static ProgramSequence buildProgramSequence(Program program, int total, String name, double density, String key, double tempo) {
    return new ProgramSequence()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .total(total)
      .name(name)
      .key(key)
      .tempo(tempo)
      .density(density);
  }

  public static ProgramSequenceBinding buildProgramSequenceBinding(ProgramSequence programSequence, int offset) {
    return new ProgramSequenceBinding()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .offset(offset);
  }

  public static ProgramSequenceBindingMeme buildProgramSequenceBindingMeme(ProgramSequenceBinding programSequenceBinding, String name) {
    return new ProgramSequenceBindingMeme()
      .id(UUID.randomUUID())
      .programId(programSequenceBinding.getProgramId())
      .programSequenceBindingId(programSequenceBinding.getId())
      .name(name);
  }

  public static ProgramSequenceChord buildProgramSequenceChord(ProgramSequence programSequence, double position, String name) {
    return new ProgramSequenceChord()
      .id(UUID.randomUUID())
      .programSequenceId(programSequence.getId())
      .programId(programSequence.getProgramId())
      .position(position)
      .name(name);
  }

  public static ProgramSequenceChordVoicing buildProgramSequenceChordVoicing(InstrumentType type, ProgramSequenceChord programSequenceChord, String notes) {
    return new ProgramSequenceChordVoicing()
      .id(UUID.randomUUID())
      .programId(programSequenceChord.getProgramId())
      .programSequenceChordId(programSequenceChord.getId())
      .type(type)
      .notes(notes);
  }

  public static ProgramVoice buildProgramVoice(Program program, InstrumentType type, String name) {
    return new ProgramVoice()
      .id(UUID.randomUUID())
      .programId(program.getId())
      .type(type)
      .name(name);
  }

  public static ProgramVoiceTrack buildProgramVoiceTrack(ProgramVoice programVoice, String name) {
    return new ProgramVoiceTrack()
      .id(UUID.randomUUID())
      .programId(programVoice.getProgramId())
      .programVoiceId(programVoice.getId())
      .name(name);
  }

  public static ProgramSequencePattern buildProgramSequencePattern(ProgramSequence programSequence, ProgramVoice programVoice, ProgramSequencePatternType type, int total, String name) {
    return new ProgramSequencePattern()
      .id(UUID.randomUUID())
      .programId(programSequence.getProgramId())
      .programSequenceId(programSequence.getId())
      .programVoiceId(programVoice.getId())
      .type(type)
      .total(total)
      .name(name);
  }

  public static ProgramSequencePatternEvent buildProgramSequencePatternEvent(ProgramSequencePattern programSequencePattern, ProgramVoiceTrack programVoiceTrack, double position, double duration, String note, double velocity) {
    return new ProgramSequencePatternEvent()
      .id(UUID.randomUUID())
      .programId(programSequencePattern.getProgramId())
      .programSequencePatternId(programSequencePattern.getId())
      .programVoiceTrackId(programVoiceTrack.getId())
      .position(position)
      .duration(duration)
      .note(note)
      .velocity(velocity);
  }

  public static Instrument buildInstrument(Library library, InstrumentType type, InstrumentState state, String name) {
    return new Instrument()
      .id(UUID.randomUUID())
      .libraryId(library.getId())
      .type(type)
      .state(state)
      .density(0.6)
      .name(name);
  }

  public static InstrumentMeme buildInstrumentMeme(Instrument instrument, String name) {
    return new InstrumentMeme()
      .id(UUID.randomUUID())
      .instrumentId(instrument.getId())
      .name(name)
      ;
  }
}
