// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentState;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramState;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.enums.TemplateType;
import io.xj.hub.enums.UserAuthType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.AccountUser;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.InstrumentMeme;
import io.xj.hub.tables.pojos.Library;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.Template;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.tables.pojos.TemplatePlayback;
import io.xj.hub.tables.pojos.TemplatePublication;
import io.xj.hub.tables.pojos.User;
import io.xj.hub.tables.pojos.UserAuth;
import io.xj.hub.tables.pojos.UserAuthToken;
import io.xj.lib.util.CSV;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Integration tests use shared scenario fixtures as much as possible https://www.pivotaltracker.com/story/show/165954673
 * <p>
 * Testing the hypothesis that, while unit tests are all independent,
 * integration tests ought to be as much about testing all features around a consensus model of the platform
 * as they are about testing all resources.
 */
public class IntegrationTestingFixtures {
  public static final String TEST_TEMPLATE_CONFIG = "outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n";
  final HubIntegrationTest test;

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

  /**
   * Create a new Integration Testing Fixtures instance by providing the integration test provider
   */
  public IntegrationTestingFixtures(HubIntegrationTest hubIntegrationTest) {
    test = hubIntegrationTest;
    content = new HubContentFixtures();
  }

  /**
   * Create a new Integration Testing Fixtures instance by providing the integration test provider and content fixtures
   */
  public IntegrationTestingFixtures(HubIntegrationTest hubIntegrationTest, HubContentFixtures content) {
    test = hubIntegrationTest;
    this.content = content;
  }

  public static Collection<Object> buildInstrumentWithAudios(Instrument instrument, String notes) {
    List<Object> result = new ArrayList<>(List.of(instrument));
    for (String note : CSV.split(notes)) {
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
    meme.setProgramSequenceBindingId(programSequenceBinding.getId());
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
   * NOTE: it's crucial tht a test template configuration disable certain aleatory features,
   * e.g. `deltaArcEnabled = false` to disable choice delta randomness,
   * otherwise tests may sporadically fail.
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
    programSequenceBinding.setId(UUID.randomUUID());
    programSequenceBinding.setProgramId(programSequence.getProgramId());
    programSequenceBinding.setProgramSequenceId(programSequence.getId());
    programSequenceBinding.setOffset(offset);
    return programSequenceBinding;
  }

  public static ProgramSequenceBindingMeme buildProgramSequenceBindingMeme(ProgramSequenceBinding programSequenceBinding, String name) {
    var programSequenceBindingMeme = new ProgramSequenceBindingMeme();
    programSequenceBindingMeme.setId(UUID.randomUUID());
    programSequenceBindingMeme.setProgramId(programSequenceBinding.getProgramId());
    programSequenceBindingMeme.setProgramSequenceBindingId(programSequenceBinding.getId());
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

  /**
   * Library of Content A (shared test fixture)
   */
  public void insertFixtureA() throws HubException {
    // account
    account1 = test.insert(buildAccount("testing"));
    user101 = test.insert(buildUser("john", "john@email.com", "https://pictures.com/john.gif", "Admin"));

    // Library content all created at this known time
    Instant at = Instant.parse("2014-08-12T12:17:02.527142Z");
    library10000001 = test.insert(buildLibrary(account1, "leaves"));

    // Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
    template1 = test.insert(buildTemplate(account1, "test", UUID.randomUUID().toString()));
    templateBinding1 = test.insert(buildTemplateBinding(template1, library10000001));

    // Instrument 201
    instrument201 = test.insert(buildInstrument(library10000001, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "808 Drums"));
    test.insert(buildInstrumentMeme(instrument201, "Ants"));
    test.insert(buildInstrumentMeme(instrument201, "Mold"));
    //
    instrument201_audio402 = test.insert(buildInstrumentAudio(instrument201, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f));
    //
    var audio401 = test.insert(buildInstrumentAudio(instrument201, "Beat", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f));

    // Instrument 202
    instrument202 = test.insert(buildInstrument(library10000001, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "909 Drums"));
    test.insert(buildInstrumentMeme(instrument202, "Peel"));

    // Program 701, main-type, has sequence with chords, bound to many offsets
    program701 = test.insert(buildProgram(library10000001, ProgramType.Main, ProgramState.Published, "leaves", "C#", 120.4f, 0.6f));
    program701_meme0 = test.insert(buildProgramMeme(program701, "Ants"));
    var sequence902 = test.insert(buildProgramSequence(program701, (short) 16, "decay", 0.25f, "F#"));
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

    // Program 702, beat-type, has unbound sequence with pattern with events
    program702 = test.insert(buildProgram(library10000001, ProgramType.Beat, ProgramState.Published, "coconuts", "F#", 110.3f, 0.6f));
    test.insert(buildProgramMeme(program702, "Ants"));
    program702_voice1 = test.insert(buildProgramVoice(program702, InstrumentType.Drum, "Drums"));
    var sequence702a = test.insert(buildProgramSequence(program702, (short) 16, "Base", 0.5f, "C"));
    var pattern901 = test.insert(buildProgramSequencePattern(sequence702a, program702_voice1, (short) 16, "growth"));
    var trackBoom = test.insert(buildProgramVoiceTrack(program702_voice1, "BOOM"));
    var trackSmack = test.insert(buildProgramVoiceTrack(program702_voice1, "BOOM"));
    program702_pattern901_boomEvent = test.insert(buildProgramSequencePatternEvent(pattern901, trackBoom, 0.0f, 1.0f, "C", 1.0f));
    test.insert(buildProgramSequencePatternEvent(pattern901, trackSmack, 1.0f, 1.0f, "G", 0.8f));
    test.insert(buildProgramSequencePatternEvent(pattern901, trackBoom, 2.5f, 1.0f, "C", 0.6f));
    test.insert(buildProgramSequencePatternEvent(pattern901, trackSmack, 3.0f, 1.0f, "G", 0.9f));

    // Program 703
    program703 = test.insert(buildProgram(library10000001, ProgramType.Main, ProgramState.Published, "bananas", "Gb", 100.6f, 0.6f));
    test.insert(buildProgramMeme(program703, "Peel"));

    // DELIBERATELY UNUSED stuff that should not get used because it's in a different library
    library10000002 = test.insert(buildLibrary(account1, "Garbage Library"));
    //
    instrument251 = test.insert(buildInstrument(library10000002, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "Garbage Instrument"));
    test.insert(buildInstrumentMeme(instrument251, "Garbage MemeObject"));
    //
    program751 = test.insert(buildProgram(library10000002, ProgramType.Beat, ProgramState.Published, "coconuts", "F#", 110.3f, 0.6f));
    test.insert(buildProgramMeme(program751, "Ants"));
    var voiceGarbage = test.insert(buildProgramVoice(program751, InstrumentType.Drum, "Garbage"));
    var sequence751a = test.insert(buildProgramSequence(program751, (short) 16, "Base", 0.5f, "C"));
    var pattern951 = test.insert(buildProgramSequencePattern(sequence751a, voiceGarbage, (short) 16, "Garbage"));
    var trackGr = test.insert(buildProgramVoiceTrack(voiceGarbage, "GR"));
    var trackBag = test.insert(buildProgramVoiceTrack(voiceGarbage, "BAG"));
    test.insert(buildProgramSequencePatternEvent(pattern951, trackGr, 0.0f, 1.0f, "C", 1.0f));
    test.insert(buildProgramSequencePatternEvent(pattern951, trackBag, 1.0f, 1.0f, "G", 0.8f));
    test.insert(buildProgramSequencePatternEvent(pattern951, trackGr, 2.5f, 1.0f, "C", 0.6f));
    test.insert(buildProgramSequencePatternEvent(pattern951, trackBag, 3.0f, 1.0f, "G", 0.9f));
  }

  /**
   * Library of Content B-1 (shared test fixture)
   */
  public void insertFixtureB1() throws HubException {
    Collection<Object> entities = content.setupFixtureB1(true);
    for (Object Object : entities) {
      test.insert(Object);
    }
  }

  /**
   * Library of Content B-1 (shared test fixture)
   * <p>
   * Integration tests use shared scenario fixtures as much as possible https://www.pivotaltracker.com/story/show/165954673
   */
  public void insertFixtureB2() throws HubException {
    Collection<Object> entities = content.setupFixtureB2();
    for (Object Object : entities) {
      test.insert(Object);
    }
  }

  /**
   * Library of Content B-3 (shared test fixture)
   * <p>
   * Integration tests use shared scenario fixtures as much as possible https://www.pivotaltracker.com/story/show/165954673
   * <p>
   * memes bound to sequence-pattern because sequence-binding is not considered for beat sequences, beat sequence patterns do not have memes. https://www.pivotaltracker.com/story/show/163158036
   * <p>
   * Choice is either by sequence-pattern (macro- or main-type sequences) or by sequence (beat- and detail-type sequences) https://www.pivotaltracker.com/story/show/165954619
   * <p>
   * Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Beat or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment. https://www.pivotaltracker.com/story/show/153976073
   * + For this test, there's an Intro Pattern with all BLEEPS, multiple Loop Patterns with KICK and SNARE (2x each), and an Outro Pattern with all TOOTS.
   * <p>
   * Artist wants to of multiple Patterns with the same offset in the same Sequence, in order that XJ randomly select one of the patterns at that offset. https://www.pivotaltracker.com/story/show/150279647
   */
  public void insertFixtureB3() throws HubException {
    Collection<Object> entities = content.setupFixtureB3();
    for (Object Object : entities) {
      test.insert(Object);
    }
  }

  /**
   * Library of Content B: Instruments (shared test fixture)
   * <p>
   * Integration tests use shared scenario fixtures as much as possible https://www.pivotaltracker.com/story/show/165954673
   */
  public void insertFixtureB_Instruments() throws HubException {
    instrument201 = test.insert(buildInstrument(library2, InstrumentType.Drum, InstrumentMode.Event, InstrumentState.Published, "808 Drums"));
    test.insert(buildInstrumentMeme(instrument201, "Ants"));
    test.insert(buildInstrumentMeme(instrument201, "Mold"));
    //
    audio401 = test.insert(buildInstrumentAudio(instrument201, "Beat", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f));
    //
    var audio402 = test.insert(buildInstrumentAudio(instrument201, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f));
  }

}
