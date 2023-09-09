// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus;

import io.xj.hub.enums.*;
import io.xj.hub.tables.pojos.*;
import io.xj.hub.util.CsvUtils;
import jakarta.annotation.Nullable;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 Integration tests use shared scenario fixtures as much as possible https://www.pivotaltracker.com/story/show/165954673
 <p>
 Testing the hypothesis that, while unit tests are all independent,
 integration tests ought to be as much about testing all features around a consensus model of the platform
 as they are about testing all resources.
 */
public class HubIntegrationTestingFixtures {
  public static final String TEST_TEMPLATE_CONFIG = "outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n";

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
  public Library library3;
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
  public ProgramVoice program1_voiceBass;
  public ProgramVoice program1_voicePad;
  public ProgramVoice program1_voiceSticky;
  public ProgramVoice program1_voiceStripe;
  public ProgramVoice program2_voice2;
  public ProgramVoice program702_voice1;
  public ProgramVoiceTrack program2_voice1_track0;
  public ProgramVoiceTrack program2_voice1_track1;
  public Template template1;
  public Template template2;
  public TemplateBinding templateBinding1;
  public User user101;
  public User user1;
  public User user2;
  public User user3;
  public User user4;
  public User user53;
  public User user5;
  //
  public HubContentFixtures content;

  public static Collection<Object> buildInstrumentWithAudios(Instrument instrument, String notes) {
    List<Object> result = new ArrayList<>(List.of(instrument));
    for (String note : CsvUtils.split(notes)) {
      var audio = buildAudio(instrument, String.format("%s-%s", instrument.getType().name(), note), note);
      result.add(audio);
    }
    return result;
  }

  public static InstrumentAudio buildAudio(Instrument instrument, String name, String waveformKey, float start, float length, float tempo, float density, String event, String note, float volume) {
    var instrumentAudio = new InstrumentAudio();
    instrumentAudio.setId(UUID.randomUUID());
    instrumentAudio.setInstrumentId(instrument.getId());
    instrumentAudio.setName(name);
    instrumentAudio.setWaveformKey(waveformKey);
    instrumentAudio.setTransientSeconds(start);
    instrumentAudio.setTotalBeats(length);
    instrumentAudio.setTempo(tempo);
    instrumentAudio.setDensity(density);
    instrumentAudio.setVolume(volume);
    instrumentAudio.setTones(note);
    instrumentAudio.setEvent(event);
    return instrumentAudio;
  }

  public static InstrumentAudio buildAudio(Instrument instrument, String name, String note) {
    var instrumentAudio = new InstrumentAudio();
    instrumentAudio.setId(UUID.randomUUID());
    instrumentAudio.setInstrumentId(instrument.getId());
    instrumentAudio.setName(name);
    instrumentAudio.setWaveformKey("test123");
    instrumentAudio.setTransientSeconds(0.0f);
    instrumentAudio.setTotalBeats(1.0f);
    instrumentAudio.setTempo(120.0f);
    instrumentAudio.setDensity(1.0f);
    instrumentAudio.setVolume(1.0f);
    instrumentAudio.setEvent("X");
    instrumentAudio.setTones(note);
    return instrumentAudio;
  }

  public static User buildUser(String name, String email, String avatarUrl, String roles) {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setEmail(email);
    user.setAvatarUrl(avatarUrl);
    user.setName(name);
    user.setRoles(roles);
    return user;
  }

  public static Account buildAccount() {
    var account = new Account();
    account.setId(UUID.randomUUID());
    return account;
  }

  public static AccountUser buildAccountUser(Account account, User user) {
    var accountUser = new AccountUser();
    accountUser.setId(UUID.randomUUID());
    accountUser.setAccountId(account.getId());
    accountUser.setUserId(user.getId());
    return accountUser;
  }

  public static Program buildProgram(Library library, ProgramType type, ProgramState state, String name, String key, float tempo, float density) {
    var program = new Program();
    program.setId(UUID.randomUUID());
    program.setLibraryId(library.getId());
    program.setType(type);
    program.setState(state);
    program.setName(name);
    program.setKey(key);
    program.setTempo(tempo);
    program.setDensity(density);
    return program;
  }

  public static Program buildProgram(ProgramType type, String key, float tempo, float density) {
    var program = new Program();
    program.setId(UUID.randomUUID());
    program.setLibraryId(UUID.randomUUID());
    program.setType(type);
    program.setState(ProgramState.Published);
    program.setName(String.format("Test %s-Program", type.toString()));
    program.setKey(key);
    program.setTempo(tempo);
    program.setDensity(density);
    return program;
  }

  public static Program buildDetailProgram(String key, Boolean doPatternRestartOnChord, String name) {
    var program = new Program();
    program.setId(UUID.randomUUID());
    program.setLibraryId(UUID.randomUUID());
    program.setType(ProgramType.Detail);
    program.setState(ProgramState.Published);
    program.setName(name);
    program.setKey(key);
    program.setConfig(String.format("doPatternRestartOnChord=%b", doPatternRestartOnChord));
    return program;
  }

  public static ProgramMeme buildMeme(Program program, String name) {
    var meme = new ProgramMeme();
    meme.setId(UUID.randomUUID());
    meme.setProgramId(program.getId());
    meme.setName(name);
    return meme;
  }

  public static ProgramSequence buildSequence(Program program, int total, String name, float density, String key) {
    var sequence = new ProgramSequence();
    sequence.setId(UUID.randomUUID());
    sequence.setProgramId(program.getId());
    sequence.setTotal((short) total);
    sequence.setName(name);
    sequence.setKey(key);
    sequence.setDensity(density);
    return sequence;
  }

  public static ProgramSequence buildSequence(Program program, int total) {
    var sequence = new ProgramSequence();
    sequence.setId(UUID.randomUUID());
    sequence.setProgramId(program.getId());
    sequence.setTotal((short) total);
    sequence.setName(String.format("Test %d-beat Sequence", total));
    return sequence;
  }

  public static ProgramSequenceBinding buildBinding(ProgramSequence programSequence, int offset) {
    var binding = new ProgramSequenceBinding();
    binding.setId(UUID.randomUUID());
    binding.setProgramId(programSequence.getProgramId());
    binding.setProgramSequenceId(programSequence.getId());
    binding.setOffset(offset);
    return binding;
  }

  public static ProgramSequenceBindingMeme buildMeme(ProgramSequenceBinding programSequenceBinding, String name) {
    var meme = new ProgramSequenceBindingMeme();
    meme.setId(UUID.randomUUID());
    meme.setProgramId(programSequenceBinding.getProgramId());
    meme.setProgramSequenceBindingId(programSequenceBinding.getOffset());
    meme.setName(name);
    return meme;
  }

  public static ProgramSequenceChord buildChord(ProgramSequence programSequence, double position, String name) {
    var chord = new ProgramSequenceChord();
    chord.setId(UUID.randomUUID());
    chord.setProgramSequenceId(programSequence.getId());
    chord.setProgramId(programSequence.getProgramId());
    chord.setPosition(position);
    chord.setName(name);
    return chord;
  }

  public static ProgramSequenceChordVoicing buildVoicing(ProgramSequenceChord programSequenceChord, ProgramVoice voice, String notes) {
    var voicing = new ProgramSequenceChordVoicing();
    voicing.setId(UUID.randomUUID());
    voicing.setProgramId(programSequenceChord.getProgramId());
    voicing.setProgramSequenceChordId(programSequenceChord.getId());
    voicing.setProgramVoiceId(voice.getId());
    voicing.setNotes(notes);
    return voicing;
  }

  public static ProgramVoice buildVoice(Program program, InstrumentType type, String name) {
    var voice = new ProgramVoice();
    voice.setId(UUID.randomUUID());
    voice.setProgramId(program.getId());
    voice.setType(type);
    voice.setName(name);
    return voice;
  }

  public static ProgramVoice buildVoice(Program program, InstrumentType type) {
    return buildVoice(program, type, type.toString());
  }

  public static ProgramVoiceTrack buildTrack(ProgramVoice programVoice, String name) {
    var track = new ProgramVoiceTrack();
    track.setId(UUID.randomUUID());
    track.setProgramId(programVoice.getProgramId());
    track.setProgramVoiceId(programVoice.getId());
    track.setName(name);
    return track;
  }

  public static ProgramVoiceTrack buildTrack(ProgramVoice programVoice) {
    return buildTrack(programVoice, programVoice.getType().toString());
  }

  public static ProgramSequencePattern buildPattern(ProgramSequence programSequence, ProgramVoice programVoice, int total, String name) {
    var pattern = new ProgramSequencePattern();
    pattern.setId(UUID.randomUUID());
    pattern.setProgramId(programSequence.getProgramId());
    pattern.setProgramSequenceId(programSequence.getId());
    pattern.setProgramVoiceId(programVoice.getId());
    pattern.setTotal((short) total);
    pattern.setName(name);
    return pattern;
  }

  public static ProgramSequencePattern buildPattern(ProgramSequence sequence, ProgramVoice voice, int total) {
    return buildPattern(sequence, voice, total, sequence.getName() + " pattern");
  }

  public static ProgramSequencePatternEvent buildEvent(ProgramSequencePattern pattern, ProgramVoiceTrack track, float position, float duration, String note, float velocity) {
    var event = new ProgramSequencePatternEvent();
    event.setId(UUID.randomUUID());
    event.setProgramId(pattern.getProgramId());
    event.setProgramSequencePatternId(pattern.getId());
    event.setProgramVoiceTrackId(track.getId());
    event.setPosition(position);
    event.setDuration(duration);
    event.setTones(note);
    event.setVelocity(velocity);
    return event;
  }

  public static ProgramSequencePatternEvent buildEvent(ProgramSequencePattern pattern, ProgramVoiceTrack track, float position, float duration, String note) {
    return buildEvent(pattern, track, position, duration, note, 1.0f);
  }

  public static Instrument buildInstrument(InstrumentType type, InstrumentMode mode, Boolean isTonal, Boolean isMultiphonic) {
    var instrument = new Instrument();
    instrument.setId(UUID.randomUUID());
    instrument.setLibraryId(UUID.randomUUID());
    instrument.setType(type);
    instrument.setMode(mode);
    instrument.setState(InstrumentState.Published);
    instrument.setConfig(String.format("isTonal=%b\nisMultiphonic=%b", isTonal, isMultiphonic));
    instrument.setName(String.format("Test %s-Instrument", type.toString()));
    return instrument;
  }

  public static InstrumentMeme buildMeme(Instrument instrument, String name) {
    var instrumentMeme = new InstrumentMeme();
    instrumentMeme.setId(UUID.randomUUID());
    instrumentMeme.setInstrumentId(instrument.getId());
    instrumentMeme.setName(name);
    return instrumentMeme;
  }

  public static TemplateBinding buildBinding(Template template, Program program) {
    var binding = new TemplateBinding();
    binding.setId(UUID.randomUUID());
    binding.setTemplateId(template.getId());
    binding.setTargetId(program.getId());
    binding.setType(ContentBindingType.Program);
    return binding;
  }

  public static TemplateBinding buildBinding(Template template, Instrument instrument) {
    var binding = new TemplateBinding();
    binding.setId(UUID.randomUUID());
    binding.setTemplateId(template.getId());
    binding.setTargetId(instrument.getId());
    binding.setType(ContentBindingType.Instrument);
    return binding;
  }

  public static TemplateBinding buildBinding(Template template, Library library) {
    var binding = new TemplateBinding();
    binding.setId(UUID.randomUUID());
    binding.setTemplateId(template.getId());
    binding.setTargetId(library.getId());
    binding.setType(ContentBindingType.Library);
    return binding;
  }

  public static InstrumentAudio buildInstrumentAudio(Instrument instrument, String name, String waveformKey, float start, float length, float tempo, float density, String event, String tones, float volume) {
    var instrumentAudio = new InstrumentAudio();
    instrumentAudio.setId(UUID.randomUUID());
    instrumentAudio.setInstrumentId(instrument.getId());
    instrumentAudio.setName(name);
    instrumentAudio.setWaveformKey(waveformKey);
    instrumentAudio.setTransientSeconds(start);
    instrumentAudio.setTotalBeats(length);
    instrumentAudio.setTempo(tempo);
    instrumentAudio.setDensity(density);
    instrumentAudio.setVolume(volume);
    instrumentAudio.setTones(tones);
    instrumentAudio.setEvent(event);
    return instrumentAudio;
  }

  public static InstrumentAudio buildInstrumentAudio(Instrument instrument, String name, float start, float length, float tempo) {
    return buildInstrumentAudio(instrument, name, "key123", start, length, tempo);
  }

  public static InstrumentAudio buildInstrumentAudio(Instrument instrument, String name, @Nullable String waveformKey, float start, float length, float tempo) {
    return buildInstrumentAudio(instrument, name, waveformKey, start, length, tempo, 0.6f, "X", "X", 1.0f);
  }

  public static UserAuth buildUserAuth(User user, UserAuthType type, String externalAccessToken, String externalRefreshToken, String externalAccount) {
    var auth = new UserAuth();
    auth.setId(UUID.randomUUID());
    auth.setUserId(user.getId());
    auth.setType(type);
    auth.setExternalAccessToken(externalAccessToken);
    auth.setExternalRefreshToken(externalRefreshToken);
    auth.setExternalAccount(externalAccount);
    return auth;
  }

  public static UserAuthToken buildUserAuthToken(UserAuth userAuth, String value) {
    var token = new UserAuthToken();
    token.setId(UUID.randomUUID());
    token.setUserId(userAuth.getUserId());
    token.setUserAuthId(userAuth.getId());
    token.setAccessToken(value);
    return token;
  }

  public static Library buildLibrary(Account account, String name) {
    var library = new Library();
    library.setId(UUID.randomUUID());
    library.setAccountId(account.getId());
    library.setName(name);
    return library;
  }

  public static Account buildAccount(String name) {
    var account = new Account();
    account.setId(UUID.randomUUID());
    account.setName(name);
    return account;
  }

  /**
   NOTE: it's crucial tht a test template configuration disable certain aleatory features,
   e.g. `deltaArcEnabled = false` to disable choice delta randomness,
   otherwise tests may sporadically fail.
   */
  public static Template buildTemplate(Account account1, TemplateType type, String name, String shipKey) {
    var template = new Template();
    template.setId(UUID.randomUUID());
    template.setShipKey(shipKey);
    template.setType(type);
    template.setConfig(TEST_TEMPLATE_CONFIG);
    template.setAccountId(account1.getId());
    template.setName(name);
    return template;
  }

  public static Template buildTemplate(Account account1, String name, String shipKey) {
    return buildTemplate(account1, TemplateType.Preview, name, shipKey);
  }

  public static Template buildTemplate(Account account1, String name, String shipKey, String config) {
    var template = buildTemplate(account1, TemplateType.Preview, name, shipKey);
    template.setConfig(config);
    return template;
  }

  public static Template buildTemplate(Account account1, String name) {
    return buildTemplate(account1, TemplateType.Preview, name, String.format("%s123", name));
  }

  public static TemplateBinding buildTemplateBinding(Template template, Library library) {
    var templateBinding = new TemplateBinding();
    templateBinding.setId(UUID.randomUUID());
    templateBinding.setType(ContentBindingType.Library);
    templateBinding.setTargetId(library.getId());
    templateBinding.setTemplateId(template.getId());
    return templateBinding;
  }

  public static TemplateBinding buildTemplateBinding(Template template, Instrument instrument) {
    var templateBinding = new TemplateBinding();
    templateBinding.setId(UUID.randomUUID());
    templateBinding.setType(ContentBindingType.Instrument);
    templateBinding.setTargetId(instrument.getId());
    templateBinding.setTemplateId(template.getId());
    return templateBinding;
  }

  public static TemplateBinding buildTemplateBinding(Template template, Program program) {
    var templateBinding = new TemplateBinding();
    templateBinding.setId(UUID.randomUUID());
    templateBinding.setType(ContentBindingType.Program);
    templateBinding.setTargetId(program.getId());
    templateBinding.setTemplateId(template.getId());
    return templateBinding;
  }

  public static TemplatePlayback buildTemplatePlayback(Template template, User user) {
    var templatePlayback = new TemplatePlayback();
    templatePlayback.setId(UUID.randomUUID());
    templatePlayback.setUserId(user.getId());
    templatePlayback.setTemplateId(template.getId());
    templatePlayback.setCreatedAt(Timestamp.from(Instant.now()).toLocalDateTime());
    return templatePlayback;
  }

  public static TemplatePublication buildTemplatePublication(Template template, User user) {
    var templatePublication = new TemplatePublication();
    templatePublication.setId(UUID.randomUUID());
    templatePublication.setUserId(user.getId());
    templatePublication.setTemplateId(template.getId());
    templatePublication.setCreatedAt(Timestamp.from(Instant.now()).toLocalDateTime());
    return templatePublication;
  }

  public static Program buildProgram(Library library, ProgramType type, String name) {
    return buildProgram(library, type, ProgramState.Published, name, "C", 120.0f, 0.62f);
  }


  public static Program buildProgram(Library library, ProgramType type, ProgramState state, String name, String key, Float tempo, Float density) {
    var program = new Program();
    program.setId(UUID.randomUUID());
    program.setLibraryId(library.getId());
    program.setType(type);
    program.setState(state);
    program.setName(name);
    program.setKey(key);
    program.setTempo(tempo);
    program.setDensity(density);
    return program;
  }

  public static ProgramMeme buildProgramMeme(Program program, String name) {
    var programMeme = new ProgramMeme();
    programMeme.setId(UUID.randomUUID());
    programMeme.setProgramId(program.getId());
    programMeme.setName(name);
    return programMeme;
  }

  public static ProgramSequence buildProgramSequence(Program program, int total, String name, float density, String key) {
    var programSequence = new ProgramSequence();
    programSequence.setId(UUID.randomUUID());
    programSequence.setProgramId(program.getId());
    programSequence.setTotal((short) total);
    programSequence.setName(name);
    programSequence.setKey(key);
    programSequence.setDensity(density);
    return programSequence;
  }

  public static ProgramSequenceBinding buildProgramSequenceBinding(ProgramSequence programSequence, int offset) {
    var programSequenceBinding = new ProgramSequenceBinding();
    programSequenceBinding.setOffset(UUID.randomUUID());
    programSequenceBinding.setProgramId(programSequence.getProgramId());
    programSequenceBinding.setProgramSequenceId(programSequence.getId());
    programSequenceBinding.setOffset(offset);
    return programSequenceBinding;
  }

  public static ProgramSequenceBindingMeme buildProgramSequenceBindingMeme(ProgramSequenceBinding programSequenceBinding, String name) {
    var programSequenceBindingMeme = new ProgramSequenceBindingMeme();
    programSequenceBindingMeme.setId(UUID.randomUUID());
    programSequenceBindingMeme.setProgramId(programSequenceBinding.getProgramId());
    programSequenceBindingMeme.setProgramSequenceBindingId(programSequenceBinding.getOffset());
    programSequenceBindingMeme.setName(name);
    return programSequenceBindingMeme;
  }

  public static ProgramSequenceChord buildProgramSequenceChord(ProgramSequence programSequence, double position, String name) {
    var programSequenceChord = new ProgramSequenceChord();
    programSequenceChord.setId(UUID.randomUUID());
    programSequenceChord.setProgramSequenceId(programSequence.getId());
    programSequenceChord.setProgramId(programSequence.getProgramId());
    programSequenceChord.setPosition(position);
    programSequenceChord.setName(name);
    return programSequenceChord;
  }

  public static ProgramSequenceChordVoicing buildProgramSequenceChordVoicing(ProgramSequenceChord programSequenceChord, ProgramVoice voice, String notes) {
    var programSequenceChordVoicing = new ProgramSequenceChordVoicing();
    programSequenceChordVoicing.setId(UUID.randomUUID());
    programSequenceChordVoicing.setProgramId(programSequenceChord.getProgramId());
    programSequenceChordVoicing.setProgramSequenceChordId(programSequenceChord.getId());
    programSequenceChordVoicing.setProgramVoiceId(voice.getId());
    programSequenceChordVoicing.setNotes(notes);
    return programSequenceChordVoicing;
  }

  public static ProgramVoice buildProgramVoice(Program program, InstrumentType type, String name) {
    var programVoice = new ProgramVoice();
    programVoice.setId(UUID.randomUUID());
    programVoice.setProgramId(program.getId());
    programVoice.setType(type);
    programVoice.setName(name);
    return programVoice;
  }

  public static ProgramVoiceTrack buildProgramVoiceTrack(ProgramVoice programVoice, String name) {
    var programVoiceTrack = new ProgramVoiceTrack();
    programVoiceTrack.setId(UUID.randomUUID());
    programVoiceTrack.setProgramId(programVoice.getProgramId());
    programVoiceTrack.setProgramVoiceId(programVoice.getId());
    programVoiceTrack.setName(name);
    return programVoiceTrack;
  }

  public static ProgramSequencePattern buildProgramSequencePattern(ProgramSequence programSequence, ProgramVoice programVoice, int total, String name) {
    var programSequencePattern = new ProgramSequencePattern();
    programSequencePattern.setId(UUID.randomUUID());
    programSequencePattern.setProgramId(programSequence.getProgramId());
    programSequencePattern.setProgramSequenceId(programSequence.getId());
    programSequencePattern.setProgramVoiceId(programVoice.getId());
    programSequencePattern.setTotal((short) total);
    programSequencePattern.setName(name);
    return programSequencePattern;
  }

  public static ProgramSequencePatternEvent buildProgramSequencePatternEvent(ProgramSequencePattern programSequencePattern, ProgramVoiceTrack programVoiceTrack, float position, float duration, String note, float velocity) {
    var programSequencePatternEvent = new ProgramSequencePatternEvent();
    programSequencePatternEvent.setId(UUID.randomUUID());
    programSequencePatternEvent.setProgramId(programSequencePattern.getProgramId());
    programSequencePatternEvent.setProgramSequencePatternId(programSequencePattern.getId());
    programSequencePatternEvent.setProgramVoiceTrackId(programVoiceTrack.getId());
    programSequencePatternEvent.setPosition(position);
    programSequencePatternEvent.setDuration(duration);
    programSequencePatternEvent.setTones(note);
    programSequencePatternEvent.setVelocity(velocity);
    return programSequencePatternEvent;
  }

  public static Instrument buildInstrument(Library library, InstrumentType type, InstrumentMode mode, InstrumentState state, String name) {
    var instrument = new Instrument();
    instrument.setId(UUID.randomUUID());
    instrument.setLibraryId(library.getId());
    instrument.setType(type);
    instrument.setMode(mode);
    instrument.setState(state);
    instrument.setDensity(0.6f);
    instrument.setName(name);
    return instrument;
  }

  public static InstrumentMeme buildInstrumentMeme(Instrument instrument, String name) {
    var instrumentMeme = new InstrumentMeme();
    instrumentMeme.setId(UUID.randomUUID());
    instrumentMeme.setInstrumentId(instrument.getId());
    instrumentMeme.setName(name);
    return instrumentMeme;
  }
}
