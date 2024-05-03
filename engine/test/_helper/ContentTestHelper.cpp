// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "ContentTestHelper.h"

#include <utility>

using namespace Content;

ContentTestHelper::ContentTestHelper() : counter(0) {
  std::random_device rd;
  session = rd();

  // project
  project1 = buildProject();

  // Library content all created at this known time
  library1 = buildLibrary(project1);

  // Templates: enhanced preview chain creation for artists in Lab UI https://www.pivotaltracker.com/story/show/178457569
  template1 = buildTemplate(project1, "test1", randomUUID());
  template1_binding = buildTemplateBinding(template1, library1);
  template2 = buildTemplate(project1, "test2", randomUUID());

  // Instrument 1
  instrument1 = buildInstrument(library1, Instrument::Type::Drum, Instrument::Mode::Event, "808 Drums");
  instrument1_meme = buildInstrumentMeme(instrument1, "Ants");
  instrument1_audio = buildInstrumentAudio(instrument1, "Chords Cm to D", "a0b9f74kf9b4h8d9e0g73k107s09f7-g0e73982.wav", 0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb", 1.0f);

  // Instrument 2
  instrument2 = buildInstrument(library1, Instrument::Type::Pad, Instrument::Mode::Chord, "Pad");
  instrument2_meme = buildInstrumentMeme(instrument2, "Peanuts");
  instrument2_audio = buildInstrumentAudio(instrument2, "Chord Fm", "a0b9fg73k107s74kf9b4h8d9e009f7-g0e73982.wav", 0.02f, 1.123f, 140.0f, 0.52f, "BING", "F,A,C", 0.9f);

  // Program 1, main-type, has sequence with chords, bound to many offsets
  program1 = buildProgram(library1, Program::Type::Main, "leaves", "C#", 120.4f);
  program1_meme = buildProgramMeme(program1, "Ants");
  program1_voice = buildProgramVoice(program1, Instrument::Type::Stripe, "Birds");
  program1_sequence = buildProgramSequence(program1, 8, "decay", 0.25f, "F#");
  program1_sequence_chord0 = buildProgramSequenceChord(program1_sequence, 0.0, "G minor");
  program1_sequence_chord1 = buildProgramSequenceChord(program1_sequence, 2.0, "A minor");
  program1_sequence_chord0_voicing0 = buildProgramSequenceChordVoicing(program1_sequence_chord0, program1_voice, "G");
  program1_sequence_chord1_voicing1 = buildProgramSequenceChordVoicing(program1_sequence_chord1, program1_voice, "Bb");
  program1_sequence_binding1 = buildProgramSequenceBinding(program1_sequence, 0);
  program1_sequence_binding2 = buildProgramSequenceBinding(program1_sequence, 5);
  program1_sequence_binding1_meme1 = buildProgramSequenceBindingMeme(program1_sequence_binding1, "Gravel");
  program1_sequence_binding1_meme2 = buildProgramSequenceBindingMeme(program1_sequence_binding1, "Road");

  // Program 2, beat-type, has unbound sequence with pattern with events
  program2 = buildProgram(library1, Program::Type::Beat, "coconuts", "F#", 110.3f);
  program2_meme = buildProgramMeme(program2, "Bells");
  program2_voice = buildProgramVoice(program2, Instrument::Type::Drum, "Drums");
  program2_sequence = buildProgramSequence(program2, 16, "Base", 0.5f, "C");
  program2_sequence_pattern1 = buildProgramSequencePattern(program2_sequence, program2_voice, 16, "growth");
  program2_sequence_pattern2 = buildProgramSequencePattern(program2_sequence, program2_voice, 12, "decay");
  program2_voice_track1 = buildProgramVoiceTrack(program2_voice, "BOOM");
  program2_voice_track2 = buildProgramVoiceTrack(program2_voice, "SMACK");
  program2_sequence_pattern1_event1 = buildProgramSequencePatternEvent(program2_sequence_pattern1, program2_voice_track1, 0.0f, 1.0f, "C", 1.0f);
  program2_sequence_pattern1_event2 = buildProgramSequencePatternEvent(program2_sequence_pattern1, program2_voice_track2, 0.5f, 1.1f, "D", 0.9f);

  // Set all content in store
  store.setProjects({project1});
  store.setLibraries({library1});
  store.setTemplates({template1, template2});
  store.setTemplateBindings({template1_binding});
  store.setInstruments({instrument1, instrument2});
  store.setInstrumentMemes({instrument1_meme, instrument2_meme});
  store.setInstrumentAudios({instrument1_audio, instrument2_audio});
  store.setPrograms({program1, program2});
  store.setProgramMemes({program1_meme, program2_meme});
  store.setProgramVoices({program1_voice, program2_voice});
  store.setProgramSequences({program1_sequence, program2_sequence});
  store.setProgramSequenceChords({program1_sequence_chord0, program1_sequence_chord1});
  store.setProgramSequenceChordVoicings({program1_sequence_chord0_voicing0, program1_sequence_chord1_voicing1});
  store.setProgramSequenceBindings({program1_sequence_binding1, program1_sequence_binding2});
  store.setProgramSequenceBindingMemes({program1_sequence_binding1_meme1, program1_sequence_binding1_meme2});
  store.setProgramSequencePatterns({program2_sequence_pattern1, program2_sequence_pattern2});
  store.setProgramVoiceTracks({program2_voice_track1, program2_voice_track2});
  store.setProgramSequencePatternEvents({program2_sequence_pattern1_event1, program2_sequence_pattern1_event2});
}

long long ContentTestHelper::currentTimeMillis() {
  auto now = std::chrono::system_clock::now();
  auto duration = now.time_since_epoch();
  return std::chrono::duration_cast<std::chrono::milliseconds>(duration).count();
}

std::string ContentTestHelper::randomUUID() {
  std::stringstream ss;
  ss << std::hex << std::setw(16) << std::setfill('0') << currentTimeMillis();
  ss << "-";
  ss << std::hex << std::setw(16) << std::setfill('0') << session;
  ss << "-";
  ss << std::hex << std::setw(16) << std::setfill('0') << counter++;
  return ss.str();
}

Project ContentTestHelper::buildProject() {
  Project project;
  project.id = randomUUID();
  project.name = "testing";
  project.updatedAt = currentTimeMillis();
  return project;
}

Library ContentTestHelper::buildLibrary(const Project &project) {
  Library library;
  library.id = randomUUID();
  library.projectId = project.id;
  library.name = "leaves";
  return library;
}

Template ContentTestHelper::buildTemplate(const Project& project, std::string name, std::string shipKey) {
  Template tmpl;
  tmpl.id = randomUUID();
  tmpl.shipKey = std::move(shipKey);
  tmpl.config = "deltaArcEnabled = false\n";
  tmpl.projectId = project.id;
  tmpl.name = std::move(name);
  tmpl.updatedAt = currentTimeMillis();
  return tmpl;
}

TemplateBinding ContentTestHelper::buildTemplateBinding(const Template& tmpl, const Library &library) {
  TemplateBinding templateBinding;
  templateBinding.id = randomUUID();
  templateBinding.type = TemplateBinding::Type::Library;
  templateBinding.targetId = library.id;
  templateBinding.templateId = tmpl.id;
  return templateBinding;
}

TemplateBinding ContentTestHelper::buildTemplateBinding(const Template& tmpl, const Program &program) {
  TemplateBinding templateBinding;
  templateBinding.id = randomUUID();
  templateBinding.type = TemplateBinding::Type::Program;
  templateBinding.targetId = program.id;
  templateBinding.templateId = tmpl.id;
  return templateBinding;
}

TemplateBinding ContentTestHelper::buildTemplateBinding(const Template& tmpl, const Instrument &instrument) {
  TemplateBinding templateBinding;
  templateBinding.id = randomUUID();
  templateBinding.type = TemplateBinding::Type::Instrument;
  templateBinding.targetId = instrument.id;
  templateBinding.templateId = tmpl.id;
  return templateBinding;
}

Instrument ContentTestHelper::buildInstrument(const Library& library, Instrument::Type type, Instrument::Mode mode, std::string name) {
  Instrument instrument;
  instrument.id = randomUUID();
  instrument.libraryId = library.id;
  instrument.type = type;
  instrument.mode = mode;
  instrument.state = Instrument::State::Published;
  instrument.name = std::move(name);
  instrument.updatedAt = currentTimeMillis();
  return instrument;
}

InstrumentMeme ContentTestHelper::buildInstrumentMeme(const Instrument& instrument, std::string name) {
  InstrumentMeme instrumentMeme;
  instrumentMeme.id = randomUUID();
  instrumentMeme.instrumentId = instrument.id;
  instrumentMeme.name = std::move(name);
  return instrumentMeme;
}

InstrumentAudio ContentTestHelper::buildInstrumentAudio(const Instrument& instrument, std::string name, std::string waveformKey, float start, float length, float tempo, float intensity, std::string event, std::string tones, float volume) {
  InstrumentAudio instrumentAudio;
  instrumentAudio.id = randomUUID();
  instrumentAudio.instrumentId = instrument.id;
  instrumentAudio.name = std::move(name);
  instrumentAudio.waveformKey = std::move(waveformKey);
  instrumentAudio.transientSeconds = start;
  instrumentAudio.loopBeats = length;
  instrumentAudio.tempo = tempo;
  instrumentAudio.intensity = intensity;
  instrumentAudio.volume = volume;
  instrumentAudio.tones = std::move(tones);
  instrumentAudio.event = std::move(event);
  return instrumentAudio;
}

Program ContentTestHelper::buildProgram(const Library& library, Program::Type type, std::string name, std::string key, float tempo) {
  Program program;
  program.id = randomUUID();
  program.libraryId = library.id;
  program.type = type;
  program.state = Program::State::Published;
  program.name = std::move(name);
  program.key = std::move(key);
  program.tempo = tempo;
  program.updatedAt = currentTimeMillis();
  return program;
}

ProgramMeme ContentTestHelper::buildProgramMeme(const Program& program, std::string name) {
  ProgramMeme programMeme;
  programMeme.id = randomUUID();
  programMeme.programId = program.id;
  programMeme.name = std::move(name);
  return programMeme;
}

ProgramSequence ContentTestHelper::buildProgramSequence(const Program& program, int total, std::string name, float intensity, std::string key) {
  ProgramSequence programSequence;
  programSequence.id = randomUUID();
  programSequence.programId = program.id;
  programSequence.total = total;
  programSequence.name = std::move(name);
  programSequence.key = std::move(key);
  programSequence.intensity  =intensity;
  return programSequence;
}

ProgramSequenceBinding ContentTestHelper::buildProgramSequenceBinding(const ProgramSequence& programSequence, int offset) {
  ProgramSequenceBinding programSequenceBinding;
  programSequenceBinding.id = randomUUID();
  programSequenceBinding.programId = programSequence.programId;
  programSequenceBinding.programSequenceId = programSequence.id;
  programSequenceBinding.offset = offset;
  return programSequenceBinding;
}

ProgramSequenceBindingMeme ContentTestHelper::buildProgramSequenceBindingMeme(const ProgramSequenceBinding& programSequenceBinding, std::string name) {
  ProgramSequenceBindingMeme programSequenceBindingMeme;
  programSequenceBindingMeme.id = randomUUID();
  programSequenceBindingMeme.programId = programSequenceBinding.programId;
  programSequenceBindingMeme.programSequenceBindingId = programSequenceBinding.id;
  programSequenceBindingMeme.name = std::move(name);
  return programSequenceBindingMeme;
}

ProgramSequenceChord ContentTestHelper::buildProgramSequenceChord(const ProgramSequence& programSequence, float position, std::string name) {
  ProgramSequenceChord programSequenceChord;
  programSequenceChord.id = randomUUID();
  programSequenceChord.programSequenceId = programSequence.id;
  programSequenceChord.programId = programSequence.programId;
  programSequenceChord.position = position;
  programSequenceChord.name = std::move(name);
  return programSequenceChord;
}

ProgramSequenceChordVoicing ContentTestHelper::buildProgramSequenceChordVoicing(const ProgramSequenceChord& programSequenceChord, const ProgramVoice &voice, std::string notes) {
  ProgramSequenceChordVoicing programSequenceChordVoicing;
  programSequenceChordVoicing.id = randomUUID();
  programSequenceChordVoicing.programId = programSequenceChord.programId;
  programSequenceChordVoicing.programSequenceChordId = programSequenceChord.id;
  programSequenceChordVoicing.programVoiceId = voice.id;
  programSequenceChordVoicing.notes = std::move(notes);
  return programSequenceChordVoicing;
}

ProgramVoice ContentTestHelper::buildProgramVoice(const Program& program, Instrument::Type type, std::string name) {
  ProgramVoice programVoice;
  programVoice.id = randomUUID();
  programVoice.programId = program.id;
  programVoice.type = type;
  programVoice.name = std::move(name);
  return programVoice;
}

ProgramVoiceTrack ContentTestHelper::buildProgramVoiceTrack(const ProgramVoice& programVoice, std::string name) {
  ProgramVoiceTrack programVoiceTrack;
  programVoiceTrack.id = randomUUID();
  programVoiceTrack.programId = programVoice.programId;
  programVoiceTrack.programVoiceId = programVoice.id;
  programVoiceTrack.name = std::move(name);
  return programVoiceTrack;
}

ProgramSequencePattern ContentTestHelper::buildProgramSequencePattern(const ProgramSequence& programSequence, const ProgramVoice& programVoice, int total, std::string name) {
  ProgramSequencePattern programSequencePattern;
  programSequencePattern.id = randomUUID();
  programSequencePattern.programId = programSequence.programId;
  programSequencePattern.programSequenceId = programSequence.id;
  programSequencePattern.programVoiceId = programVoice.id;
  programSequencePattern.total = total;
  programSequencePattern.name = std::move(name);
  return programSequencePattern;
}

ProgramSequencePatternEvent ContentTestHelper::buildProgramSequencePatternEvent(const ProgramSequencePattern& programSequencePattern, const ProgramVoiceTrack& programVoiceTrack, float position, float duration, std::string tones, float velocity) {
  ProgramSequencePatternEvent programSequencePatternEvent;
  programSequencePatternEvent.id = randomUUID();
  programSequencePatternEvent.programId = programSequencePattern.programId;
  programSequencePatternEvent.programSequencePatternId = programSequencePattern.id;
  programSequencePatternEvent.programVoiceTrackId = programVoiceTrack.id;
  programSequencePatternEvent.position = position;
  programSequencePatternEvent.duration = duration;
  programSequencePatternEvent.tones = std::move(tones);
  programSequencePatternEvent.velocity = velocity;
  return programSequencePatternEvent;
}
