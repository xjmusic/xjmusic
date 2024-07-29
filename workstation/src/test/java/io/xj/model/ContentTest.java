package io.xj.model;

import io.xj.model.enums.ContentBindingType;
import io.xj.model.enums.InstrumentMode;
import io.xj.model.enums.InstrumentState;
import io.xj.model.enums.InstrumentType;
import io.xj.model.enums.ProgramState;
import io.xj.model.enums.ProgramType;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.pojos.InstrumentMeme;
import io.xj.model.pojos.Library;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramMeme;
import io.xj.model.pojos.ProgramSequence;
import io.xj.model.pojos.ProgramSequenceBinding;
import io.xj.model.pojos.ProgramSequenceBindingMeme;
import io.xj.model.pojos.ProgramSequenceChord;
import io.xj.model.pojos.ProgramSequenceChordVoicing;
import io.xj.model.pojos.ProgramSequencePattern;
import io.xj.model.pojos.ProgramSequencePatternEvent;
import io.xj.model.pojos.ProgramVoice;
import io.xj.model.pojos.ProgramVoiceTrack;
import io.xj.model.pojos.Project;
import io.xj.model.pojos.ProjectUser;
import io.xj.model.pojos.Template;
import io.xj.model.pojos.TemplateBinding;
import io.xj.model.pojos.User;

import java.util.List;
import java.util.UUID;

abstract public class ContentTest {
  protected Instrument instrument1;
  protected Instrument instrument2;
  protected InstrumentAudio instrument1_audio;
  protected InstrumentAudio instrument2_audio;
  protected InstrumentMeme instrument1_meme;
  protected InstrumentMeme instrument2_meme;
  protected Library library1;
  protected Program program1;
  protected Program program2;
  protected ProgramMeme program1_meme;
  protected ProgramMeme program2_meme;
  protected ProgramSequence program2_sequence;
  protected ProgramSequence program1_sequence;
  protected ProgramSequenceBinding program1_sequence_binding1;
  protected ProgramSequenceBinding program1_sequence_binding2;
  protected ProgramSequenceBindingMeme program1_sequence_binding1_meme1;
  protected ProgramSequenceBindingMeme program1_sequence_binding1_meme2;
  protected ProgramSequenceChord program1_sequence_chord0;
  protected ProgramSequenceChord program1_sequence_chord1;
  protected ProgramSequenceChordVoicing program1_sequence_chord0_voicing0;
  protected ProgramSequenceChordVoicing program1_sequence_chord1_voicing1;
  protected ProgramSequencePattern program2_sequence_pattern1;
  protected ProgramSequencePattern program2_sequence_pattern2;
  protected ProgramSequencePatternEvent program2_sequence_pattern1_event1;
  protected ProgramSequencePatternEvent program2_sequence_pattern1_event2;
  protected ProgramVoice program1_voice;
  protected ProgramVoice program2_voice;
  protected ProgramVoiceTrack program2_voice_track1;
  protected ProgramVoiceTrack program2_voice_track2;
  protected Project project1;
  protected ProjectUser project1_user;
  protected Template template1;
  protected Template template2;
  protected TemplateBinding template1_binding;
  protected User user1;

  protected User buildUser() {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setName("Test Person");
    user.setIsAdmin(false);
    user.setAvatarUrl("https://static.xj.io/images/default-avatar.jpg");
    return user;
  }

  protected Project buildProject() {
    var project = new Project();
    project.setId(UUID.randomUUID());
    project.setName("testing");
    project.setUpdatedAt(System.currentTimeMillis());
    return project;
  }

  protected ProjectUser buildProjectUser(Project project, User user) {
    var projectUser = new ProjectUser();
    projectUser.setId(UUID.randomUUID());
    projectUser.setProjectId(project.getId());
    projectUser.setUserId(user.getId());
    return projectUser;
  }

  protected Library buildLibrary(Project project) {
    var library = new Library();
    library.setId(UUID.randomUUID());
    library.setProjectId(project.getId());
    library.setName("leaves");
    return library;
  }

  protected Template buildTemplate(Project project1, String name, String shipKey) {
    var template = new Template();
    template.setId(UUID.randomUUID());
    template.setShipKey(shipKey);
    template.setConfig("deltaArcEnabled = false\n");
    template.setProjectId(project1.getId());
    template.setName(name);
    template.setUpdatedAt(System.currentTimeMillis());
    return template;
  }

  protected TemplateBinding buildTemplateBinding(Template template, Library library) {
    var templateBinding = new TemplateBinding();
    templateBinding.setId(UUID.randomUUID());
    templateBinding.setType(ContentBindingType.Library);
    templateBinding.setTargetId(library.getId());
    templateBinding.setTemplateId(template.getId());
    return templateBinding;
  }

  protected TemplateBinding buildTemplateBinding(Template template, Program program) {
    var templateBinding = new TemplateBinding();
    templateBinding.setId(UUID.randomUUID());
    templateBinding.setType(ContentBindingType.Program);
    templateBinding.setTargetId(program.getId());
    templateBinding.setTemplateId(template.getId());
    return templateBinding;
  }

  protected TemplateBinding buildTemplateBinding(Template template, Instrument instrument) {
    var templateBinding = new TemplateBinding();
    templateBinding.setId(UUID.randomUUID());
    templateBinding.setType(ContentBindingType.Instrument);
    templateBinding.setTargetId(instrument.getId());
    templateBinding.setTemplateId(template.getId());
    return templateBinding;
  }

  protected Instrument buildInstrument(Library library, InstrumentType type, InstrumentMode mode, String name) {
    var instrument = new Instrument();
    instrument.setId(UUID.randomUUID());
    instrument.setLibraryId(library.getId());
    instrument.setType(type);
    instrument.setMode(mode);
    instrument.setState(InstrumentState.Published);
    instrument.setName(name);
    instrument.setUpdatedAt(System.currentTimeMillis());
    return instrument;
  }

  protected InstrumentMeme buildInstrumentMeme(Instrument instrument, String name) {
    var instrumentMeme = new InstrumentMeme();
    instrumentMeme.setId(UUID.randomUUID());
    instrumentMeme.setInstrumentId(instrument.getId());
    instrumentMeme.setName(name);
    return instrumentMeme;
  }

  protected InstrumentAudio buildInstrumentAudio(Instrument instrument, String name, String waveformKey, float start, float length, float tempo, float intensity, String event, String tones, float volume) {
    var instrumentAudio = new InstrumentAudio();
    instrumentAudio.setId(UUID.randomUUID());
    instrumentAudio.setInstrumentId(instrument.getId());
    instrumentAudio.setName(name);
    instrumentAudio.setWaveformKey(waveformKey);
    instrumentAudio.setTransientSeconds(start);
    instrumentAudio.setLengthSeconds(length * 60 / tempo);
    instrumentAudio.setLoopBeats(length);
    instrumentAudio.setTempo(tempo);
    instrumentAudio.setIntensity(intensity);
    instrumentAudio.setVolume(volume);
    instrumentAudio.setTones(tones);
    instrumentAudio.setEvent(event);
    return instrumentAudio;
  }

  protected Program buildProgram(Library library, ProgramType type, String name, String key, Float tempo) {
    var program = new Program();
    program.setId(UUID.randomUUID());
    program.setLibraryId(library.getId());
    program.setType(type);
    program.setState(ProgramState.Published);
    program.setName(name);
    program.setKey(key);
    program.setTempo(tempo);
    program.setUpdatedAt(System.currentTimeMillis());
    return program;
  }

  protected ProgramMeme buildProgramMeme(Program program, String name) {
    var programMeme = new ProgramMeme();
    programMeme.setId(UUID.randomUUID());
    programMeme.setProgramId(program.getId());
    programMeme.setName(name);
    return programMeme;
  }

  protected ProgramSequence buildProgramSequence(Program program, int total, String name, float intensity, String key) {
    var programSequence = new ProgramSequence();
    programSequence.setId(UUID.randomUUID());
    programSequence.setProgramId(program.getId());
    programSequence.setTotal((short) total);
    programSequence.setName(name);
    programSequence.setKey(key);
    programSequence.setIntensity(intensity);
    return programSequence;
  }

  protected ProgramSequenceBinding buildProgramSequenceBinding(ProgramSequence programSequence, int offset) {
    var programSequenceBinding = new ProgramSequenceBinding();
    programSequenceBinding.setId(UUID.randomUUID());
    programSequenceBinding.setProgramId(programSequence.getProgramId());
    programSequenceBinding.setProgramSequenceId(programSequence.getId());
    programSequenceBinding.setOffset(offset);
    return programSequenceBinding;
  }

  protected ProgramSequenceBindingMeme buildProgramSequenceBindingMeme(ProgramSequenceBinding programSequenceBinding, String name) {
    var programSequenceBindingMeme = new ProgramSequenceBindingMeme();
    programSequenceBindingMeme.setId(UUID.randomUUID());
    programSequenceBindingMeme.setProgramId(programSequenceBinding.getProgramId());
    programSequenceBindingMeme.setProgramSequenceBindingId(programSequenceBinding.getId());
    programSequenceBindingMeme.setName(name);
    return programSequenceBindingMeme;
  }

  protected ProgramSequenceChord buildProgramSequenceChord(ProgramSequence programSequence, double position, String name) {
    var programSequenceChord = new ProgramSequenceChord();
    programSequenceChord.setId(UUID.randomUUID());
    programSequenceChord.setProgramSequenceId(programSequence.getId());
    programSequenceChord.setProgramId(programSequence.getProgramId());
    programSequenceChord.setPosition(position);
    programSequenceChord.setName(name);
    return programSequenceChord;
  }

  protected ProgramSequenceChordVoicing buildProgramSequenceChordVoicing(ProgramSequenceChord programSequenceChord, ProgramVoice voice, String notes) {
    var programSequenceChordVoicing = new ProgramSequenceChordVoicing();
    programSequenceChordVoicing.setId(UUID.randomUUID());
    programSequenceChordVoicing.setProgramId(programSequenceChord.getProgramId());
    programSequenceChordVoicing.setProgramSequenceChordId(programSequenceChord.getId());
    programSequenceChordVoicing.setProgramVoiceId(voice.getId());
    programSequenceChordVoicing.setNotes(notes);
    return programSequenceChordVoicing;
  }

  protected ProgramVoice buildProgramVoice(Program program, InstrumentType type, String name) {
    var programVoice = new ProgramVoice();
    programVoice.setId(UUID.randomUUID());
    programVoice.setProgramId(program.getId());
    programVoice.setType(type);
    programVoice.setName(name);
    return programVoice;
  }

  protected ProgramVoiceTrack buildProgramVoiceTrack(ProgramVoice programVoice, String name) {
    var programVoiceTrack = new ProgramVoiceTrack();
    programVoiceTrack.setId(UUID.randomUUID());
    programVoiceTrack.setProgramId(programVoice.getProgramId());
    programVoiceTrack.setProgramVoiceId(programVoice.getId());
    programVoiceTrack.setName(name);
    return programVoiceTrack;
  }

  protected ProgramSequencePattern buildProgramSequencePattern(ProgramSequence programSequence, ProgramVoice programVoice, int total, String name) {
    var programSequencePattern = new ProgramSequencePattern();
    programSequencePattern.setId(UUID.randomUUID());
    programSequencePattern.setProgramId(programSequence.getProgramId());
    programSequencePattern.setProgramSequenceId(programSequence.getId());
    programSequencePattern.setProgramVoiceId(programVoice.getId());
    programSequencePattern.setTotal((short) total);
    programSequencePattern.setName(name);
    return programSequencePattern;
  }

  protected ProgramSequencePatternEvent buildProgramSequencePatternEvent(ProgramSequencePattern programSequencePattern, ProgramVoiceTrack programVoiceTrack, float position, float duration, String note, float velocity) {
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

  protected HubContent buildHubContent() {
    // user
    user1 = buildUser();

    // project
    project1 = buildProject();
    project1_user = buildProjectUser(project1, user1);

    // Library content all created at this known time
    library1 = buildLibrary(project1);

    // Templates: enhanced preview chain creation for artists in Lab UI https://github.com/xjmusic/xjmusic/issues/205
    template1 = buildTemplate(project1, "test1", UUID.randomUUID().toString());
    template1_binding = buildTemplateBinding(template1, library1);
    template2 = buildTemplate(project1, "test2", UUID.randomUUID().toString());

    // Instrument 1
    instrument1 = buildInstrument(library1, InstrumentType.Drum, InstrumentMode.Event, "808 Drums");
    instrument1_meme = buildInstrumentMeme(instrument1, "Ants");
    instrument1_audio = buildInstrumentAudio(instrument1, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);

    // Instrument 2
    instrument2 = buildInstrument(library1, InstrumentType.Pad, InstrumentMode.Chord, "Pad");
    instrument2_meme = buildInstrumentMeme(instrument2, "Peanuts");
    instrument2_audio = buildInstrumentAudio(instrument2, "Chord Fm", "a0b9fg73k107s74kf9b4h8d9e009f7-g0e73982.wav", 0.02f, 1.123f, 140.0f, 0.52f, "BING", "F,A,C", 0.9f);

    // Program 1, main-type, has sequence with chords, bound to many offsets
    program1 = buildProgram(library1, ProgramType.Main, "leaves", "C#", 120.4f);
    program1_meme = buildProgramMeme(program1, "Ants");
    program1_voice = buildProgramVoice(program1, InstrumentType.Stripe, "Birds");
    program1_sequence = buildProgramSequence(program1, (short) 8, "decay", 0.25f, "F#");
    program1_sequence_chord0 = buildProgramSequenceChord(program1_sequence, 0.0, "G minor");
    program1_sequence_chord1 = buildProgramSequenceChord(program1_sequence, 2.0, "A minor");
    program1_sequence_chord0_voicing0 = buildProgramSequenceChordVoicing(program1_sequence_chord0, program1_voice, "G");
    program1_sequence_chord1_voicing1 = buildProgramSequenceChordVoicing(program1_sequence_chord1, program1_voice, "Bb");
    program1_sequence_binding1 = buildProgramSequenceBinding(program1_sequence, 0);
    program1_sequence_binding2 = buildProgramSequenceBinding(program1_sequence, 5);
    program1_sequence_binding1_meme1 = buildProgramSequenceBindingMeme(program1_sequence_binding1, "Gravel");
    program1_sequence_binding1_meme2 = buildProgramSequenceBindingMeme(program1_sequence_binding1, "Road");

    // Program 2, beat-type, has unbound sequence with pattern with events
    program2 = buildProgram(library1, ProgramType.Beat, "coconuts", "F#", 110.3f);
    program2_meme = buildProgramMeme(program2, "Bells");
    program2_voice = buildProgramVoice(program2, InstrumentType.Drum, "Drums");
    program2_sequence = buildProgramSequence(program2, (short) 16, "Base", 0.5f, "C");
    program2_sequence_pattern1 = buildProgramSequencePattern(program2_sequence, program2_voice, (short) 16, "growth");
    program2_sequence_pattern2 = buildProgramSequencePattern(program2_sequence, program2_voice, (short) 12, "decay");
    program2_voice_track1 = buildProgramVoiceTrack(program2_voice, "BOOM");
    program2_voice_track2 = buildProgramVoiceTrack(program2_voice, "SMACK");
    program2_sequence_pattern1_event1 = buildProgramSequencePatternEvent(program2_sequence_pattern1, program2_voice_track1, 0.0f, 1.0f, "C", 1.0f);
    program2_sequence_pattern1_event2 = buildProgramSequencePatternEvent(program2_sequence_pattern1, program2_voice_track2, 0.5f, 1.1f, "D", 0.9f);

    // ingest all content
    HubContent content = new HubContent(List.of(
      program1_sequence_binding1,
      instrument1,
      instrument1_audio,
      instrument1_meme,
      instrument2,
      instrument2_audio,
      instrument2_meme,
      library1,
      program2_sequence_pattern1,
      program2_sequence_pattern2,
      program1,
      program1_meme,
      program1_voice,
      program2,
      program2_meme,
      program2_sequence_pattern1_event1,
      program2_sequence_pattern1_event2,
      program2_voice,
      project1,
      project1_user,
      program2_sequence,
      program1_sequence,
      program1_sequence_binding2,
      program1_sequence_binding1_meme1,
      program1_sequence_binding1_meme2,
      program1_sequence_chord0,
      program1_sequence_chord0_voicing0,
      program1_sequence_chord1_voicing1,
      program1_sequence_chord1,
      template1,
      template2,
      template1_binding,
      program2_voice_track1,
      program2_voice_track2,
      user1
    ), false);

    // test error
    content.addError(new Error("test"));

    return content;
  }
}
