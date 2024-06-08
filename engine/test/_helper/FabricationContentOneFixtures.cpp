// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "FabricationContentOneFixtures.h"

#include <utility>

using namespace XJ;

const std::string FabricationContentOneFixtures::TEST_TEMPLATE_CONFIG = "outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n";

std::vector<std::variant<Instrument, InstrumentAudio>>
FabricationContentOneFixtures::buildInstrumentWithAudios(
    Instrument instrument,
    const std::string &notes
) {
  std::vector<std::variant<Instrument, InstrumentAudio>> result;
  result.emplace_back(instrument);
  std::vector<std::string> splitNotes = StringUtils::split(notes, ',');
  for (const std::string& note: splitNotes) {
    std::string name = Instrument::toString(instrument.type) + "-" + note;
    auto audio = buildAudio(instrument, name, note);
    result.emplace_back(audio);
  }
  return result;
}

InstrumentAudio FabricationContentOneFixtures::buildAudio(
    const Instrument &instrument,
    std::string name,
    std::string waveformKey,
    float start,
    float length,
    float tempo,
    float intensity,
    std::string event,
    std::string note,
    float volume
) {
  InstrumentAudio instrumentAudio;
  instrumentAudio.id = ContentTestHelper::randomUUID();
  instrumentAudio.instrumentId = instrument.id;
  instrumentAudio.name = std::move(name);
  instrumentAudio.waveformKey = std::move(waveformKey);
  instrumentAudio.transientSeconds = start;
  instrumentAudio.loopBeats = length;
  instrumentAudio.tempo = tempo;
  instrumentAudio.intensity = intensity;
  instrumentAudio.volume = volume;
  instrumentAudio.tones = std::move(note);
  instrumentAudio.event = std::move(event);
  return instrumentAudio;
}

InstrumentAudio FabricationContentOneFixtures::buildAudio(
    const Instrument &instrument,
    std::string name,
    std::string note
) {
  InstrumentAudio instrumentAudio;
  instrumentAudio.id = ContentTestHelper::randomUUID();
  instrumentAudio.instrumentId = instrument.id;
  instrumentAudio.name = std::move(name);
  instrumentAudio.waveformKey = "test123";
  instrumentAudio.transientSeconds = 0.0f;
  instrumentAudio.loopBeats = 1.0f;
  instrumentAudio.tempo = 120.0f;
  instrumentAudio.intensity = 1.0f;
  instrumentAudio.volume = 1.0f;
  instrumentAudio.event = "X";
  instrumentAudio.tones = std::move(note);
  return instrumentAudio;
}

Project FabricationContentOneFixtures::buildProject() {
  Project project;
  project.id = ContentTestHelper::randomUUID();
  return project;
}

Program FabricationContentOneFixtures::buildProgram(
    const Library &library,
    Program::Type type,
    Program::State state,
    std::string name,
    std::string key,
    float tempo
) {
  Program program;
  program.id = ContentTestHelper::randomUUID();
  program.libraryId = library.id;
  program.type = type;
  program.state = state;
  program.name = std::move(name);
  program.key = std::move(key);
  program.tempo = tempo;
  return program;
}

Program FabricationContentOneFixtures::buildProgram(
    Program::Type type,
    std::string key,
    float tempo
) {
  Program program;
  program.id = ContentTestHelper::randomUUID();
  program.libraryId = ContentTestHelper::randomUUID();
  program.type = type;
  program.state = Program::State::Published;
  program.name = "Test " + Program::toString(type) + "-Program";
  program.key = std::move(key);
  program.tempo = tempo;
  return program;
}

Program FabricationContentOneFixtures::buildDetailProgram(
    std::string key,
    bool doPatternRestartOnChord,
    std::string name
) {
  Program program;
  program.id = ContentTestHelper::randomUUID();
  program.libraryId = ContentTestHelper::randomUUID();
  program.type = Program::Type::Detail;
  program.state = Program::State::Published;
  program.name = std::move(name);
  program.key = std::move(key);
  program.config = "doPatternRestartOnChord=" + std::string(doPatternRestartOnChord ? "true" : "false");
  return program;
}

ProgramMeme FabricationContentOneFixtures::buildMeme(
    const Program& program,
    std::string name
) {
  ProgramMeme meme;
  meme.id = ContentTestHelper::randomUUID();
  meme.programId = program.id;
  meme.name = std::move(name);
  return meme;
}

ProgramSequence FabricationContentOneFixtures::buildSequence(
    const Program& program,
    int total,
    std::string name,
    float intensity,
    std::string key
) {
  ProgramSequence sequence;
  sequence.id = ContentTestHelper::randomUUID();
  sequence.programId = program.id;
  sequence.total = (short) total;
  sequence.name = std::move(name);
  sequence.key = std::move(key);
  sequence.intensity = intensity;
  return sequence;
}

ProgramSequence FabricationContentOneFixtures::buildSequence(
    const Program& program,
    int total
) {
  ProgramSequence sequence;
  sequence.id = ContentTestHelper::randomUUID();
  sequence.programId = program.id;
  sequence.total = (short) total;
  sequence.name = "Test " + std::to_string(total) + "-beat Sequence";
  return sequence;
}

ProgramSequenceBinding FabricationContentOneFixtures::buildBinding(
    const ProgramSequence& programSequence,
    int offset
) {
  ProgramSequenceBinding binding;
  binding.id = ContentTestHelper::randomUUID();
  binding.programId = programSequence.programId;
  binding.programSequenceId = programSequence.id;
  binding.offset = offset;
  return binding;
}

ProgramSequenceBindingMeme FabricationContentOneFixtures::buildMeme(
    const ProgramSequenceBinding& programSequenceBinding,
    std::string name
) {
  ProgramSequenceBindingMeme meme;
  meme.id = ContentTestHelper::randomUUID();
  meme.programId = programSequenceBinding.programId;
  meme.programSequenceBindingId = programSequenceBinding.id;
  meme.name = std::move(name);
  return meme;
}

ProgramSequenceChord FabricationContentOneFixtures::buildChord(
    const ProgramSequence& programSequence,
    float position,
    std::string name
) {
  ProgramSequenceChord chord;
  chord.id = ContentTestHelper::randomUUID();
  chord.programSequenceId = programSequence.id;
  chord.programId = programSequence.programId;
  chord.position = position;
  chord.name = std::move(name);
  return chord;
}

ProgramSequenceChordVoicing FabricationContentOneFixtures::buildVoicing(
    const ProgramSequenceChord& programSequenceChord,
    const ProgramVoice& voice,
    std::string notes
) {
  ProgramSequenceChordVoicing voicing;
  voicing.id = ContentTestHelper::randomUUID();
  voicing.programId = programSequenceChord.programId;
  voicing.programSequenceChordId = programSequenceChord.id;
  voicing.programVoiceId = voice.id;
  voicing.notes = std::move(notes);
  return voicing;
}

ProgramVoice FabricationContentOneFixtures::buildVoice(
    const Program& program,
    Instrument::Type type,
    std::string name
) {
  ProgramVoice voice;
  voice.id = ContentTestHelper::randomUUID();
  voice.programId = program.id;
  voice.type = type;
  voice.name = std::move(name);
  return voice;
}

ProgramVoice FabricationContentOneFixtures::buildVoice(
    const Program& program,
    Instrument::Type type
) {
  return buildVoice(program, type, Instrument::toString(type));
}

ProgramVoiceTrack FabricationContentOneFixtures::buildTrack(
    const ProgramVoice& programVoice,
    std::string name
) {
  ProgramVoiceTrack track;
  track.id = ContentTestHelper::randomUUID();
  track.programId = programVoice.programId;
  track.programVoiceId = programVoice.id;
  track.name = std::move(name);
  return track;
}

ProgramVoiceTrack FabricationContentOneFixtures::buildTrack(
    const ProgramVoice& programVoice
) {
  return buildTrack(programVoice, Instrument::toString(programVoice.type));
}

ProgramSequencePattern FabricationContentOneFixtures::buildPattern(
    const ProgramSequence& programSequence,
    const ProgramVoice& programVoice,
    int total,
    std::string name
) {
  ProgramSequencePattern pattern;
  pattern.id = ContentTestHelper::randomUUID();
  pattern.programId = programSequence.programId;
  pattern.programSequenceId = programSequence.id;
  pattern.programVoiceId = programVoice.id;
  pattern.total = (short) total;
  pattern.name = std::move(name);
  return pattern;
}

ProgramSequencePattern FabricationContentOneFixtures::buildPattern(
    const ProgramSequence& sequence,
    const ProgramVoice& voice,
    int total
) {
  return buildPattern(sequence, voice, total, sequence.name + " pattern");
}

ProgramSequencePatternEvent FabricationContentOneFixtures::buildEvent(
    const ProgramSequencePattern& pattern,
    const ProgramVoiceTrack& track,
    float position,
    float duration,
    std::string note,
    float velocity
) {
  ProgramSequencePatternEvent event;
  event.id = ContentTestHelper::randomUUID();
  event.programId = pattern.programId;
  event.programSequencePatternId = pattern.id;
  event.programVoiceTrackId = track.id;
  event.position = position;
  event.duration = duration;
  event.tones = std::move(note);
  event.velocity = velocity;
  return event;
}

ProgramSequencePatternEvent FabricationContentOneFixtures::buildEvent(
    ProgramSequencePattern pattern,
    ProgramVoiceTrack track,
    float position,
    float duration,
    std::string note
) {
  return buildEvent(std::move(pattern), std::move(track), position, duration, std::move(note), 1.0f);
}

Instrument FabricationContentOneFixtures::buildInstrument(
    Instrument::Type type,
    Instrument::Mode mode,
    bool isTonal,
    bool isMultiphonic
) {
  Instrument instrument;
  instrument.id = ContentTestHelper::randomUUID();
  instrument.libraryId = ContentTestHelper::randomUUID();
  instrument.type = type;
  instrument.mode = mode;
  instrument.state = Instrument::State::Published;
  instrument.config = "isTonal=" + std::string(isTonal ? "true" : "false") +
                      "\nisMultiphonic=" + std::string(isMultiphonic ? "true" : "false");
  instrument.name = "Test " + Instrument::toString(type) + "-Instrument";
  return instrument;
}

InstrumentMeme FabricationContentOneFixtures::buildMeme(
    const Instrument& instrument,
    std::string name
) {
  InstrumentMeme instrumentMeme;
  instrumentMeme.id = ContentTestHelper::randomUUID();
  instrumentMeme.instrumentId = instrument.id;
  instrumentMeme.name = std::move(name);
  return instrumentMeme;
}

InstrumentAudio FabricationContentOneFixtures::buildInstrumentAudio(
    const Instrument& instrument,
    std::string name,
    std::string waveformKey,
    float start,
    float length,
    float tempo,
    float intensity,
    std::string event,
    std::string tones,
    float volume
) {
  InstrumentAudio instrumentAudio;
  instrumentAudio.id = ContentTestHelper::randomUUID();
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

Library FabricationContentOneFixtures::buildLibrary(
    const Project& project,
    std::string name
) {
  Library library;
  library.id = ContentTestHelper::randomUUID();
  library.projectId = project.id;
  library.name = std::move(name);
  return library;
}

Project FabricationContentOneFixtures::buildProject(
    std::string name
) {
  Project project;
  project.id = ContentTestHelper::randomUUID();
  project.name = std::move(name);
  return project;
}

Template FabricationContentOneFixtures::buildTemplate(
    const Project& project1,
    std::string name,
    std::string shipKey
) {
  Template tmpl;
  tmpl.id = ContentTestHelper::randomUUID();
  tmpl.shipKey = std::move(shipKey);
  tmpl.config = FabricationContentOneFixtures::TEST_TEMPLATE_CONFIG;
  tmpl.projectId = project1.id;
  tmpl.name = std::move(name);
  return tmpl;
}

Template FabricationContentOneFixtures::buildTemplate(
    Project project1,
    std::string name,
    std::string shipKey,
    std::string config
) {
  Template tmpl = buildTemplate(std::move(project1), std::move(name), std::move(shipKey));
  tmpl.config = std::move(config);
  return tmpl;
}

Template FabricationContentOneFixtures::buildTemplate(
    Project project1,
    const std::string& name
) {
  return buildTemplate(std::move(project1), name, name + "123");
}

TemplateBinding FabricationContentOneFixtures::buildTemplateBinding(
    const Template& tmpl,
    const Library& library
) {
  TemplateBinding templateBinding;
  templateBinding.id = ContentTestHelper::randomUUID();
  templateBinding.type = TemplateBinding::Type::Library;
  templateBinding.targetId = library.id;
  templateBinding.templateId = tmpl.id;
  return templateBinding;
}

ProgramMeme FabricationContentOneFixtures::buildProgramMeme(
    const Program& program,
    std::string name
) {
  ProgramMeme programMeme;
  programMeme.id = ContentTestHelper::randomUUID();
  programMeme.programId = program.id;
  programMeme.name = std::move(name);
  return programMeme;
}

ProgramSequence FabricationContentOneFixtures::buildProgramSequence(
    const Program& program,
    int total,
    std::string name,
    float intensity,
    std::string key
) {
  ProgramSequence programSequence;
  programSequence.id = ContentTestHelper::randomUUID();
  programSequence.programId = program.id;
  programSequence.total = (short) total;
  programSequence.name = std::move(name);
  programSequence.key = std::move(key);
  programSequence.intensity = intensity;
  return programSequence;
}

ProgramSequenceBinding FabricationContentOneFixtures::buildProgramSequenceBinding(
    const ProgramSequence& programSequence,
    int offset
) {
  ProgramSequenceBinding programSequenceBinding;
  programSequenceBinding.id = ContentTestHelper::randomUUID();
  programSequenceBinding.programId = programSequence.programId;
  programSequenceBinding.programSequenceId = programSequence.id;
  programSequenceBinding.offset = offset;
  return programSequenceBinding;
}

ProgramSequenceBindingMeme FabricationContentOneFixtures::buildProgramSequenceBindingMeme(
    const ProgramSequenceBinding& programSequenceBinding,
    std::string name
) {
  ProgramSequenceBindingMeme programSequenceBindingMeme;
  programSequenceBindingMeme.id = ContentTestHelper::randomUUID();
  programSequenceBindingMeme.programId = programSequenceBinding.programId;
  programSequenceBindingMeme.programSequenceBindingId = programSequenceBinding.id;
  programSequenceBindingMeme.name = std::move(name);
  return programSequenceBindingMeme;
}

ProgramSequenceChord FabricationContentOneFixtures::buildProgramSequenceChord(
    const ProgramSequence& programSequence,
    float position,
    std::string name
) {
  ProgramSequenceChord programSequenceChord;
  programSequenceChord.id = ContentTestHelper::randomUUID();
  programSequenceChord.programSequenceId = programSequence.id;
  programSequenceChord.programId = programSequence.programId;
  programSequenceChord.position = position;
  programSequenceChord.name = std::move(name);
  return programSequenceChord;
}

ProgramSequenceChordVoicing FabricationContentOneFixtures::buildProgramSequenceChordVoicing(
    const ProgramSequenceChord& programSequenceChord,
    const ProgramVoice& voice,
    std::string notes
) {
  ProgramSequenceChordVoicing programSequenceChordVoicing;
  programSequenceChordVoicing.id = ContentTestHelper::randomUUID();
  programSequenceChordVoicing.programId = programSequenceChord.programId;
  programSequenceChordVoicing.programSequenceChordId = programSequenceChord.id;
  programSequenceChordVoicing.programVoiceId = voice.id;
  programSequenceChordVoicing.notes = std::move(notes);
  return programSequenceChordVoicing;
}

ProgramVoice FabricationContentOneFixtures::buildProgramVoice(
    const Program& program,
    Instrument::Type type,
    std::string name
) {
  ProgramVoice programVoice;
  programVoice.id = ContentTestHelper::randomUUID();
  programVoice.programId = program.id;
  programVoice.type = type;
  programVoice.name = std::move(name);
  return programVoice;
}

ProgramVoiceTrack FabricationContentOneFixtures::buildProgramVoiceTrack(
    const ProgramVoice& programVoice,
    std::string name
) {
  ProgramVoiceTrack programVoiceTrack;
  programVoiceTrack.id = ContentTestHelper::randomUUID();
  programVoiceTrack.programId = programVoice.programId;
  programVoiceTrack.programVoiceId = programVoice.id;
  programVoiceTrack.name = std::move(name);
  return programVoiceTrack;
}

ProgramSequencePattern FabricationContentOneFixtures::buildProgramSequencePattern(
    const ProgramSequence& programSequence,
    const ProgramVoice& programVoice,
    int total,
    std::string name
) {
  ProgramSequencePattern programSequencePattern;
  programSequencePattern.id = ContentTestHelper::randomUUID();
  programSequencePattern.programId = programSequence.programId;
  programSequencePattern.programSequenceId = programSequence.id;
  programSequencePattern.programVoiceId = programVoice.id;
  programSequencePattern.total = (short) total;
  programSequencePattern.name = std::move(name);
  return programSequencePattern;
}

ProgramSequencePatternEvent FabricationContentOneFixtures::buildProgramSequencePatternEvent(
    const ProgramSequencePattern& programSequencePattern,
    const ProgramVoiceTrack& programVoiceTrack,
    float position,
    float duration,
    std::string tones,
    float velocity
) {
  ProgramSequencePatternEvent programSequencePatternEvent;
  programSequencePatternEvent.id = ContentTestHelper::randomUUID();
  programSequencePatternEvent.programId = programSequencePattern.programId;
  programSequencePatternEvent.programSequencePatternId = programSequencePattern.id;
  programSequencePatternEvent.programVoiceTrackId = programVoiceTrack.id;
  programSequencePatternEvent.position = position;
  programSequencePatternEvent.duration = duration;
  programSequencePatternEvent.tones = std::move(tones);
  programSequencePatternEvent.velocity = velocity;
  return programSequencePatternEvent;
}

Instrument FabricationContentOneFixtures::buildInstrument(
    const Library& library,
    Instrument::Type type,
    Instrument::Mode mode,
    Instrument::State state,
    std::string name
) {
  Instrument instrument;
  instrument.config = (new TemplateConfig())->toString();
  instrument.id = ContentTestHelper::randomUUID();
  instrument.libraryId = library.id;
  instrument.type = type;
  instrument.mode = mode;
  instrument.state = state;
  instrument.name = std::move(name);
  return instrument;
}

InstrumentMeme FabricationContentOneFixtures::buildInstrumentMeme(
    const Instrument& instrument,
    std::string name
) {
  InstrumentMeme instrumentMeme;
  instrumentMeme.id = ContentTestHelper::randomUUID();
  instrumentMeme.instrumentId = instrument.id;
  instrumentMeme.name = std::move(name);
  return instrumentMeme;
}
