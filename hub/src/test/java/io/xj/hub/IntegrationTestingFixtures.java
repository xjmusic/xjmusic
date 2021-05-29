// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import io.xj.Account;
import io.xj.AccountUser;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioChord;
import io.xj.InstrumentAudioEvent;
import io.xj.InstrumentMeme;
import io.xj.Library;
import io.xj.Program;
import io.xj.ProgramMeme;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequenceBindingMeme;
import io.xj.ProgramSequenceChord;
import io.xj.ProgramSequenceChordVoicing;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.ProgramVoice;
import io.xj.ProgramVoiceTrack;
import io.xj.User;
import io.xj.UserRole;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.hub.testing.HubIntegrationTestProvider;
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
  public InstrumentAudioChord audioChord1;
  public InstrumentAudioEvent audioEvent1;
  public InstrumentAudioEvent audioEvent2;
  public InstrumentAudioEvent audioEvent401a;
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
  //
  public HubContentFixtures content;


  /**
   Library of Content A (shared test fixture)
   */
  public void insertFixtureA() throws HubException, JsonApiException {
    // account
    account1 = test.insert(buildAccount("testing"));
    user101 = test.insert(buildUser("john", "john@email.com", "http://pictures.com/john.gif"));
    test.insert(buildUserRole(user101, UserRole.Type.Admin));

    // Library content all created at this known time
    Instant at = Instant.parse("2014-08-12T12:17:02.527142Z");
    library10000001 = test.insert(buildLibrary(account1, "leaves"));

    // Instrument 201
    instrument201 = test.insert(buildInstrument(library10000001, Instrument.Type.Percussive, Instrument.State.Published, "808 Drums"));
    test.insert(buildInstrumentMeme(instrument201, "Ants"));
    test.insert(buildInstrumentMeme(instrument201, "Mold"));
    //
    instrument201_audio402 = test.insert(buildInstrumentAudio(instrument201, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01, 2.123, 120.0, 0.62));
    test.insert(buildInstrumentAudioChord(instrument201_audio402, 0.0, "E minor"));
    test.insert(buildInstrumentAudioChord(instrument201_audio402, 4.0, "A major"));
    test.insert(buildInstrumentAudioChord(instrument201_audio402, 8.0, "B minor"));
    test.insert(buildInstrumentAudioChord(instrument201_audio402, 12.0, "F# major"));
    test.insert(buildInstrumentAudioChord(instrument201_audio402, 16.0, "Ab7"));
    test.insert(buildInstrumentAudioChord(instrument201_audio402, 20.0, "Bb7"));
    //
    var audio401 = test.insert(buildInstrumentAudio(instrument201, "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 0.62));
    test.insert(buildInstrumentAudioEvent(audio401, 0.0, 1.0, "KICK", "Eb", 1.0));
    test.insert(buildInstrumentAudioEvent(audio401, 1.0, 1.0, "SNARE", "Ab", 0.8));
    test.insert(buildInstrumentAudioEvent(audio401, 2.5, 1.0, "KICK", "C", 1.0));
    test.insert(buildInstrumentAudioEvent(audio401, 3.0, 1.0, "SNARE", "B", 0.8));

    // Instrument 202
    instrument202 = test.insert(buildInstrument(library10000001, Instrument.Type.Percussive, Instrument.State.Published, "909 Drums"));
    test.insert(buildInstrumentMeme(instrument202, "Peel"));

    // Program 701, main-type, has sequence with chords, bound to many offsets
    program701 = test.insert(buildProgram(library10000001, Program.Type.Main, Program.State.Published, "leaves", "C#", 120.4, 0.6));
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
    program702 = test.insert(buildProgram(library10000001, Program.Type.Rhythm, Program.State.Published, "coconuts", "F#", 110.3, 0.6));
    test.insert(buildProgramMeme(program702, "Ants"));
    program2_voice1 = test.insert(buildProgramVoice(program702, Instrument.Type.Percussive, "Drums"));
    var sequence702a = test.insert(buildProgramSequence(program702, 16, "Base", 0.5, "C", 110.3));
    var pattern901 = test.insert(buildProgramSequencePattern(sequence702a, program2_voice1, ProgramSequencePattern.Type.Loop, 16, "growth"));
    var trackBoom = test.insert(buildProgramVoiceTrack(program2_voice1, "BOOM"));
    var trackSmack = test.insert(buildProgramVoiceTrack(program2_voice1, "BOOM"));
    program702_pattern901_boomEvent = test.insert(buildProgramSequencePatternEvent(pattern901, trackBoom, 0.0, 1.0, "C", 1.0));
    test.insert(buildProgramSequencePatternEvent(pattern901, trackSmack, 1.0, 1.0, "G", 0.8));
    test.insert(buildProgramSequencePatternEvent(pattern901, trackBoom, 2.5, 1.0, "C", 0.6));
    test.insert(buildProgramSequencePatternEvent(pattern901, trackSmack, 3.0, 1.0, "G", 0.9));

    // Program 703
    program703 = test.insert(buildProgram(library10000001, Program.Type.Main, Program.State.Published, "bananas", "Gb", 100.6, 0.6));
    test.insert(buildProgramMeme(program703, "Peel"));

    // DELIBERATELY UNUSED stuff that should not get used because it's in a different library
    library10000002 = test.insert(buildLibrary(account1, "Garbage Library"));
    //
    instrument251 = test.insert(buildInstrument(library10000002, Instrument.Type.Percussive, Instrument.State.Published, "Garbage Instrument"));
    test.insert(buildInstrumentMeme(instrument251, "Garbage MemeObject"));
    //
    program751 = test.insert(buildProgram(library10000002, Program.Type.Rhythm, Program.State.Published, "coconuts", "F#", 110.3, 0.6));
    test.insert(buildProgramMeme(program751, "Ants"));
    var voiceGarbage = test.insert(buildProgramVoice(program751, Instrument.Type.Percussive, "Garbage"));
    var sequence751a = test.insert(buildProgramSequence(program751, 16, "Base", 0.5, "C", 110.3));
    var pattern951 = test.insert(buildProgramSequencePattern(sequence751a, voiceGarbage, ProgramSequencePattern.Type.Loop, 16, "Garbage"));
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
  public void insertFixtureB_Instruments() throws HubException, JsonApiException {
    instrument201 = test.insert(buildInstrument(library2, Instrument.Type.Percussive, Instrument.State.Published, "808 Drums"));
    test.insert(buildInstrumentMeme(instrument201, "Ants"));
    test.insert(buildInstrumentMeme(instrument201, "Mold"));
    //
    audio401 = test.insert(buildInstrumentAudio(instrument201, "Beat", "19801735098q47895897895782138975898.wav", 0.01, 2.123, 120.0, 0.62));
    audioEvent401a = test.insert(buildInstrumentAudioEvent(audio401, 0.0, 1.0, "KICK", "Eb", 1.0));
    test.insert(buildInstrumentAudioEvent(audio401, 1.0, 1.0, "SNARE", "Ab", 0.8));
    test.insert(buildInstrumentAudioEvent(audio401, 2.5, 1.0, "KICK", "C", 1.0));
    test.insert(buildInstrumentAudioEvent(audio401, 3.0, 1.0, "SNARE", "B", 0.8));
    //
    var audio402 = test.insert(buildInstrumentAudio(instrument201, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01, 2.123, 120.0, 0.62));
    test.insert(buildInstrumentAudioChord(audio402, 0.0, "E minor"));
    test.insert(buildInstrumentAudioChord(audio402, 4.0, "A major"));
    test.insert(buildInstrumentAudioChord(audio402, 8.0, "B minor"));
    test.insert(buildInstrumentAudioChord(audio402, 12.0, "F# major"));
    test.insert(buildInstrumentAudioChord(audio402, 16.0, "Ab7"));
    test.insert(buildInstrumentAudioChord(audio402, 20.0, "Bb7"));
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

  public static InstrumentAudioEvent buildInstrumentAudioEvent(InstrumentAudio instrumentAudio, double position, double duration, String name, String note, double velocity) {
    return InstrumentAudioEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrumentAudio.getInstrumentId())
      .setInstrumentAudioId(instrumentAudio.getId())
      .setName(name)
      .setPosition(position)
      .setDuration(duration)
      .setNote(note)
      .setVelocity(velocity)
      .build();
  }

  public static InstrumentAudioChord buildInstrumentAudioChord(InstrumentAudio instrumentAudio, double position, String name) {
    return InstrumentAudioChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrumentAudio.getInstrumentId())
      .setInstrumentAudioId(instrumentAudio.getId())
      .setName(name)
      .setPosition(position)
      .build();
  }

  public static InstrumentAudio buildInstrumentAudio(Instrument instrument, String name, String waveformKey, double start, double length, double tempo, double density) {
    return InstrumentAudio.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrument.getId())
      .setName(name)
      .setWaveformKey(waveformKey)
      .setStart(start)
      .setLength(length)
      .setTempo(tempo)
      .setDensity(density)
      .build();
  }

  public static User buildUser(String name, String email, String avatarUrl) {
    return User.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setEmail(email)
      .setAvatarUrl(avatarUrl)
      .setName(name)
      .build();
  }

  public static Library buildLibrary(Account account, String name) {
    return Library.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account.getId())
      .setName(name)
      .build();
  }

  public static Account buildAccount(String name) {
    return Account.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setName(name)
      .build();
  }

  public static UserRole buildUserRole(User user, UserRole.Type type) {
    return UserRole.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setUserId(user.getId())
      .setType(type)
      .build();
  }

  public static AccountUser buildAccountUser(Account account, User user) {
    return AccountUser.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setAccountId(account.getId())
      .setUserId(user.getId())
      .build();
  }

  public static Program buildProgram(Library library, Program.Type type, Program.State state, String name, String key, double tempo, double density) {
    return Program.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(library.getId())
      .setType(type)
      .setState(state)
      .setName(name)
      .setKey(key)
      .setTempo(tempo)
      .setDensity(density)
      .build();
  }

  public static ProgramMeme buildProgramMeme(Program program, String name) {
    return ProgramMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .setName(name)
      .build();
  }

  public static ProgramSequence buildProgramSequence(Program program, int total, String name, double density, String key, double tempo) {
    return ProgramSequence.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .setTotal(total)
      .setName(name)
      .setKey(key)
      .setTempo(tempo)
      .setDensity(density)
      .build();
  }

  public static ProgramSequenceBinding buildProgramSequenceBinding(ProgramSequence programSequence, int offset) {
    return ProgramSequenceBinding.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequence.getProgramId())
      .setProgramSequenceId(programSequence.getId())
      .setOffset(offset)
      .build();
  }

  public static ProgramSequenceBindingMeme buildProgramSequenceBindingMeme(ProgramSequenceBinding programSequenceBinding, String name) {
    return ProgramSequenceBindingMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequenceBinding.getProgramId())
      .setProgramSequenceBindingId(programSequenceBinding.getId())
      .setName(name)
      .build();
  }

  public static ProgramSequenceChord buildProgramSequenceChord(ProgramSequence programSequence, double position, String name) {
    return ProgramSequenceChord.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramSequenceId(programSequence.getId())
      .setProgramId(programSequence.getProgramId())
      .setPosition(position)
      .setName(name)
      .build();
  }

  public static ProgramSequenceChordVoicing buildProgramSequenceChordVoicing(Instrument.Type type, ProgramSequenceChord programSequenceChord, String notes) {
    return ProgramSequenceChordVoicing.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequenceChord.getProgramId())
      .setProgramSequenceChordId(programSequenceChord.getId())
      .setType(type)
      .setNotes(notes)
      .build();
  }

  public static ProgramVoice buildProgramVoice(Program program, Instrument.Type type, String name) {
    return ProgramVoice.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(program.getId())
      .setType(type)
      .setName(name)
      .build();
  }

  public static ProgramVoiceTrack buildProgramVoiceTrack(ProgramVoice programVoice, String name) {
    return ProgramVoiceTrack.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programVoice.getProgramId())
      .setProgramVoiceId(programVoice.getId())
      .setName(name)
      .build();
  }

  public static ProgramSequencePattern buildProgramSequencePattern(ProgramSequence programSequence, ProgramVoice programVoice, ProgramSequencePattern.Type type, int total, String name) {
    return ProgramSequencePattern.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequence.getProgramId())
      .setProgramSequenceId(programSequence.getId())
      .setProgramVoiceId(programVoice.getId())
      .setType(type)
      .setTotal(total)
      .setName(name)
      .build();
  }

  public static ProgramSequencePatternEvent buildProgramSequencePatternEvent(ProgramSequencePattern programSequencePattern, ProgramVoiceTrack programVoiceTrack, double position, double duration, String note, double velocity) {
    return ProgramSequencePatternEvent.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setProgramId(programSequencePattern.getProgramId())
      .setProgramSequencePatternId(programSequencePattern.getId())
      .setProgramVoiceTrackId(programVoiceTrack.getId())
      .setPosition(position)
      .setDuration(duration)
      .setNote(note)
      .setVelocity(velocity)
      .build();
  }

  public static Instrument buildInstrument(Library library, Instrument.Type type, Instrument.State state, String name) {
    return Instrument.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setLibraryId(library.getId())
      .setType(type)
      .setState(state)
      .setName(name)
      .build();
  }

  public static InstrumentMeme buildInstrumentMeme(Instrument instrument, String name) {
    return InstrumentMeme.newBuilder()
      .setId(UUID.randomUUID().toString())
      .setInstrumentId(instrument.getId())
      .setName(name)
      .build();
  }
}
