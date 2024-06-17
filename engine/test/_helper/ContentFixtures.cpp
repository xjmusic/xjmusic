// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "ContentFixtures.h"
#include "TestHelpers.h"
#include "LoremIpsum.h"

#include <utility>

using namespace XJ;

const std::string ContentFixtures::TEST_TEMPLATE_CONFIG = "outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n";

std::vector<float> ContentFixtures::listOfRandomValues(int N) {
  std::vector<float> result(N);
  for (int i = 0; i < N; i++) {
    result[i] = random(RANDOM_VALUE_FROM, RANDOM_VALUE_TO);
  }
  return result;
}

std::vector<std::string> ContentFixtures::listOfUniqueRandom(long N, const std::vector<std::string> &sourceItems) {
  int count = 0;
  std::vector<std::string> items;
  while (count < N) {
    std::string p = random(sourceItems);
    if (std::find(items.begin(), items.end(), p) == items.end()) {
      items.push_back(p);
      count++;
    }
  }
  return items;
}

float ContentFixtures::random(float A, float B) {
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_real_distribution<> dis(A, B);
  return static_cast<float>(dis(gen));
}

std::string ContentFixtures::random(std::vector<std::string> array) {
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_int_distribution<> dis(0, static_cast<int>(array.size()) - 1);
  return array[dis(gen)];
}

int ContentFixtures::random(std::vector<int> array) {
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_int_distribution<> dis(0, static_cast<int>(array.size()) - 1);
  return array[dis(gen)];
}

std::vector<std::variant<Instrument, InstrumentAudio>>
ContentFixtures::buildInstrumentWithAudios(
    Instrument instrument,
    const std::string &notes
) {
  std::vector<std::variant<Instrument, InstrumentAudio>> result;
  result.emplace_back(instrument);
  std::vector<std::string> splitNotes = StringUtils::split(notes, ',');
  for (const std::string &note: splitNotes) {
    std::string name = Instrument::toString(instrument.type) + "-" + note;
    auto audio = buildAudio(instrument, name, note);
    result.emplace_back(audio);
  }
  return result;
}

InstrumentAudio ContentFixtures::buildAudio(
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
  instrumentAudio.id = Entity::computeUniqueId();
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

InstrumentAudio ContentFixtures::buildAudio(
    const Instrument &instrument,
    std::string name,
    std::string note
) {
  InstrumentAudio instrumentAudio;
  instrumentAudio.id = Entity::computeUniqueId();
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

Project ContentFixtures::buildProject() {
  Project project;
  project.id = Entity::computeUniqueId();
  return project;
}

Program ContentFixtures::buildProgram(
    const Library &library,
    Program::Type type,
    Program::State state,
    std::string name,
    std::string key,
    float tempo
) {
  Program program;
  program.id = Entity::computeUniqueId();
  program.libraryId = library.id;
  program.type = type;
  program.state = state;
  program.name = std::move(name);
  program.key = std::move(key);
  program.tempo = tempo;
  return program;
}

Program ContentFixtures::buildProgram(
    Program::Type type,
    std::string key,
    float tempo
) {
  Program program;
  program.id = Entity::computeUniqueId();
  program.libraryId = Entity::computeUniqueId();
  program.type = type;
  program.state = Program::State::Published;
  program.name = "Test " + Program::toString(type) + "-Program";
  program.key = std::move(key);
  program.tempo = tempo;
  return program;
}

Program ContentFixtures::buildDetailProgram(
    std::string key,
    bool doPatternRestartOnChord,
    std::string name
) {
  Program program;
  program.id = Entity::computeUniqueId();
  program.libraryId = Entity::computeUniqueId();
  program.type = Program::Type::Detail;
  program.state = Program::State::Published;
  program.name = std::move(name);
  program.key = std::move(key);
  program.config = "doPatternRestartOnChord=" + std::string(doPatternRestartOnChord ? "true" : "false");
  return program;
}

ProgramMeme ContentFixtures::buildMeme(
    const Program &program,
    std::string name
) {
  ProgramMeme meme;
  meme.id = Entity::computeUniqueId();
  meme.programId = program.id;
  meme.name = std::move(name);
  return meme;
}

ProgramSequence ContentFixtures::buildSequence(
    const Program &program,
    int total,
    std::string name,
    float intensity,
    std::string key
) {
  ProgramSequence sequence;
  sequence.id = Entity::computeUniqueId();
  sequence.programId = program.id;
  sequence.total = (short) total;
  sequence.name = std::move(name);
  sequence.key = std::move(key);
  sequence.intensity = intensity;
  return sequence;
}

ProgramSequence ContentFixtures::buildSequence(
    const Program &program,
    int total
) {
  ProgramSequence sequence;
  sequence.id = Entity::computeUniqueId();
  sequence.programId = program.id;
  sequence.total = (short) total;
  sequence.name = "Test " + std::to_string(total) + "-beat Sequence";
  return sequence;
}

ProgramSequenceBinding ContentFixtures::buildBinding(
    const ProgramSequence &programSequence,
    int offset
) {
  ProgramSequenceBinding binding;
  binding.id = Entity::computeUniqueId();
  binding.programId = programSequence.programId;
  binding.programSequenceId = programSequence.id;
  binding.offset = offset;
  return binding;
}

ProgramSequenceBindingMeme ContentFixtures::buildMeme(
    const ProgramSequenceBinding &programSequenceBinding,
    std::string name
) {
  ProgramSequenceBindingMeme meme;
  meme.id = Entity::computeUniqueId();
  meme.programId = programSequenceBinding.programId;
  meme.programSequenceBindingId = programSequenceBinding.id;
  meme.name = std::move(name);
  return meme;
}

ProgramSequenceChord ContentFixtures::buildChord(
    const ProgramSequence &programSequence,
    float position,
    std::string name
) {
  ProgramSequenceChord chord;
  chord.id = Entity::computeUniqueId();
  chord.programSequenceId = programSequence.id;
  chord.programId = programSequence.programId;
  chord.position = position;
  chord.name = std::move(name);
  return chord;
}

ProgramSequenceChordVoicing ContentFixtures::buildVoicing(
    const ProgramSequenceChord &programSequenceChord,
    const ProgramVoice &voice,
    std::string notes
) {
  ProgramSequenceChordVoicing voicing;
  voicing.id = Entity::computeUniqueId();
  voicing.programId = programSequenceChord.programId;
  voicing.programSequenceChordId = programSequenceChord.id;
  voicing.programVoiceId = voice.id;
  voicing.notes = std::move(notes);
  return voicing;
}

ProgramVoice ContentFixtures::buildVoice(
    const Program &program,
    Instrument::Type type,
    std::string name
) {
  ProgramVoice voice;
  voice.id = Entity::computeUniqueId();
  voice.programId = program.id;
  voice.type = type;
  voice.name = std::move(name);
  return voice;
}

ProgramVoice ContentFixtures::buildVoice(
    const Program &program,
    Instrument::Type type
) {
  return buildVoice(program, type, Instrument::toString(type));
}

ProgramVoiceTrack ContentFixtures::buildTrack(
    const ProgramVoice &programVoice,
    std::string name
) {
  ProgramVoiceTrack track;
  track.id = Entity::computeUniqueId();
  track.programId = programVoice.programId;
  track.programVoiceId = programVoice.id;
  track.name = std::move(name);
  return track;
}

ProgramVoiceTrack ContentFixtures::buildTrack(
    const ProgramVoice &programVoice
) {
  return buildTrack(programVoice, Instrument::toString(programVoice.type));
}

ProgramSequencePattern ContentFixtures::buildPattern(
    const ProgramSequence &programSequence,
    const ProgramVoice &programVoice,
    int total,
    std::string name
) {
  ProgramSequencePattern pattern;
  pattern.id = Entity::computeUniqueId();
  pattern.programId = programSequence.programId;
  pattern.programSequenceId = programSequence.id;
  pattern.programVoiceId = programVoice.id;
  pattern.total = (short) total;
  pattern.name = std::move(name);
  return pattern;
}

ProgramSequencePattern ContentFixtures::buildPattern(
    const ProgramSequence &sequence,
    const ProgramVoice &voice,
    int total
) {
  return buildPattern(sequence, voice, total, sequence.name + " pattern");
}

ProgramSequencePatternEvent ContentFixtures::buildEvent(
    const ProgramSequencePattern &pattern,
    const ProgramVoiceTrack &track,
    float position,
    float duration,
    std::string note,
    float velocity
) {
  ProgramSequencePatternEvent event;
  event.id = Entity::computeUniqueId();
  event.programId = pattern.programId;
  event.programSequencePatternId = pattern.id;
  event.programVoiceTrackId = track.id;
  event.position = position;
  event.duration = duration;
  event.tones = std::move(note);
  event.velocity = velocity;
  return event;
}

ProgramSequencePatternEvent ContentFixtures::buildEvent(
    const ProgramSequencePattern &pattern,
    const ProgramVoiceTrack &track,
    float position,
    float duration,
    std::string note
) {
  return buildEvent(pattern, track, position, duration, std::move(note), 1.0f);
}

Instrument ContentFixtures::buildInstrument(
    Instrument::Type type,
    Instrument::Mode mode,
    bool isTonal,
    bool isMultiphonic
) {
  Instrument instrument;
  instrument.id = Entity::computeUniqueId();
  instrument.libraryId = Entity::computeUniqueId();
  instrument.type = type;
  instrument.mode = mode;
  instrument.state = Instrument::State::Published;
  instrument.config = "isTonal=" + std::string(isTonal ? "true" : "false") +
                      "\nisMultiphonic=" + std::string(isMultiphonic ? "true" : "false");
  instrument.name = "Test " + Instrument::toString(type) + "-Instrument";
  return instrument;
}

InstrumentMeme ContentFixtures::buildMeme(
    const Instrument &instrument,
    std::string name
) {
  InstrumentMeme instrumentMeme;
  instrumentMeme.id = Entity::computeUniqueId();
  instrumentMeme.instrumentId = instrument.id;
  instrumentMeme.name = std::move(name);
  return instrumentMeme;
}

InstrumentAudio ContentFixtures::buildInstrumentAudio(
    const Instrument &instrument,
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
  instrumentAudio.id = Entity::computeUniqueId();
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

Library ContentFixtures::buildLibrary(
    const Project &project,
    std::string name
) {
  Library library;
  library.id = Entity::computeUniqueId();
  library.projectId = project.id;
  library.name = std::move(name);
  return library;
}

Project ContentFixtures::buildProject(
    std::string name
) {
  Project project;
  project.id = Entity::computeUniqueId();
  project.name = std::move(name);
  return project;
}

Template ContentFixtures::buildTemplate(
    const Project &project1,
    std::string name,
    std::string shipKey
) {
  Template tmpl;
  tmpl.id = Entity::computeUniqueId();
  tmpl.shipKey = std::move(shipKey);
  tmpl.config = ContentFixtures::TEST_TEMPLATE_CONFIG;
  tmpl.projectId = project1.id;
  tmpl.name = std::move(name);
  return tmpl;
}

Template ContentFixtures::buildTemplate(
    const Project &project1,
    std::string name,
    std::string shipKey,
    std::string config
) {
  Template tmpl = buildTemplate(project1, std::move(name), std::move(shipKey));
  tmpl.config = std::move(config);
  return tmpl;
}

Template ContentFixtures::buildTemplate(
    const Project &project1,
    const std::string &name
) {
  return buildTemplate(project1, name, name + "123");
}

TemplateBinding ContentFixtures::buildTemplateBinding(
    const Template &tmpl,
    const Library &library
) {
  TemplateBinding templateBinding;
  templateBinding.id = Entity::computeUniqueId();
  templateBinding.type = TemplateBinding::Type::Library;
  templateBinding.targetId = library.id;
  templateBinding.templateId = tmpl.id;
  return templateBinding;
}

TemplateBinding ContentFixtures::buildTemplateBinding(
    const Template &tmpl,
    const Program &program
) {
  TemplateBinding templateBinding;
  templateBinding.id = Entity::computeUniqueId();
  templateBinding.type = TemplateBinding::Type::Program;
  templateBinding.targetId = program.id;
  templateBinding.templateId = tmpl.id;
  return templateBinding;
}

TemplateBinding ContentFixtures::buildTemplateBinding(
    const Template &tmpl,
    const Instrument &instrument
) {
  TemplateBinding templateBinding;
  templateBinding.id = Entity::computeUniqueId();
  templateBinding.type = TemplateBinding::Type::Instrument;
  templateBinding.targetId = instrument.id;
  templateBinding.templateId = tmpl.id;
  return templateBinding;
}

ProgramMeme ContentFixtures::buildProgramMeme(
    const Program &program,
    std::string name
) {
  ProgramMeme programMeme;
  programMeme.id = Entity::computeUniqueId();
  programMeme.programId = program.id;
  programMeme.name = std::move(name);
  return programMeme;
}

ProgramSequence ContentFixtures::buildProgramSequence(
    const Program &program,
    int total,
    std::string name,
    float intensity,
    std::string key
) {
  ProgramSequence programSequence;
  programSequence.id = Entity::computeUniqueId();
  programSequence.programId = program.id;
  programSequence.total = (short) total;
  programSequence.name = std::move(name);
  programSequence.key = std::move(key);
  programSequence.intensity = intensity;
  return programSequence;
}

ProgramSequenceBinding ContentFixtures::buildProgramSequenceBinding(
    const ProgramSequence &programSequence,
    int offset
) {
  ProgramSequenceBinding programSequenceBinding;
  programSequenceBinding.id = Entity::computeUniqueId();
  programSequenceBinding.programId = programSequence.programId;
  programSequenceBinding.programSequenceId = programSequence.id;
  programSequenceBinding.offset = offset;
  return programSequenceBinding;
}

ProgramSequenceBindingMeme ContentFixtures::buildProgramSequenceBindingMeme(
    const ProgramSequenceBinding &programSequenceBinding,
    std::string name
) {
  ProgramSequenceBindingMeme programSequenceBindingMeme;
  programSequenceBindingMeme.id = Entity::computeUniqueId();
  programSequenceBindingMeme.programId = programSequenceBinding.programId;
  programSequenceBindingMeme.programSequenceBindingId = programSequenceBinding.id;
  programSequenceBindingMeme.name = std::move(name);
  return programSequenceBindingMeme;
}

ProgramSequenceChord ContentFixtures::buildProgramSequenceChord(
    const ProgramSequence &programSequence,
    float position,
    std::string name
) {
  ProgramSequenceChord programSequenceChord;
  programSequenceChord.id = Entity::computeUniqueId();
  programSequenceChord.programSequenceId = programSequence.id;
  programSequenceChord.programId = programSequence.programId;
  programSequenceChord.position = position;
  programSequenceChord.name = std::move(name);
  return programSequenceChord;
}

ProgramSequenceChordVoicing ContentFixtures::buildProgramSequenceChordVoicing(
    const ProgramSequenceChord &programSequenceChord,
    const ProgramVoice &voice,
    std::string notes
) {
  ProgramSequenceChordVoicing programSequenceChordVoicing;
  programSequenceChordVoicing.id = Entity::computeUniqueId();
  programSequenceChordVoicing.programId = programSequenceChord.programId;
  programSequenceChordVoicing.programSequenceChordId = programSequenceChord.id;
  programSequenceChordVoicing.programVoiceId = voice.id;
  programSequenceChordVoicing.notes = std::move(notes);
  return programSequenceChordVoicing;
}

ProgramVoice ContentFixtures::buildProgramVoice(
    const Program &program,
    Instrument::Type type,
    std::string name
) {
  ProgramVoice programVoice;
  programVoice.id = Entity::computeUniqueId();
  programVoice.programId = program.id;
  programVoice.type = type;
  programVoice.name = std::move(name);
  return programVoice;
}

ProgramVoiceTrack ContentFixtures::buildProgramVoiceTrack(
    const ProgramVoice &programVoice,
    std::string name
) {
  ProgramVoiceTrack programVoiceTrack;
  programVoiceTrack.id = Entity::computeUniqueId();
  programVoiceTrack.programId = programVoice.programId;
  programVoiceTrack.programVoiceId = programVoice.id;
  programVoiceTrack.name = std::move(name);
  return programVoiceTrack;
}

ProgramSequencePattern ContentFixtures::buildProgramSequencePattern(
    const ProgramSequence &programSequence,
    const ProgramVoice &programVoice,
    int total,
    std::string name
) {
  ProgramSequencePattern programSequencePattern;
  programSequencePattern.id = Entity::computeUniqueId();
  programSequencePattern.programId = programSequence.programId;
  programSequencePattern.programSequenceId = programSequence.id;
  programSequencePattern.programVoiceId = programVoice.id;
  programSequencePattern.total = (short) total;
  programSequencePattern.name = std::move(name);
  return programSequencePattern;
}

ProgramSequencePatternEvent ContentFixtures::buildProgramSequencePatternEvent(
    const ProgramSequencePattern &programSequencePattern,
    const ProgramVoiceTrack &programVoiceTrack,
    float position,
    float duration,
    std::string tones,
    float velocity
) {
  ProgramSequencePatternEvent programSequencePatternEvent;
  programSequencePatternEvent.id = Entity::computeUniqueId();
  programSequencePatternEvent.programId = programSequencePattern.programId;
  programSequencePatternEvent.programSequencePatternId = programSequencePattern.id;
  programSequencePatternEvent.programVoiceTrackId = programVoiceTrack.id;
  programSequencePatternEvent.position = position;
  programSequencePatternEvent.duration = duration;
  programSequencePatternEvent.tones = std::move(tones);
  programSequencePatternEvent.velocity = velocity;
  return programSequencePatternEvent;
}

Instrument ContentFixtures::buildInstrument(
    const Library &library,
    Instrument::Type type,
    Instrument::Mode mode,
    Instrument::State state,
    std::string name
) {
  Instrument instrument;
  instrument.config = (new TemplateConfig())->toString();
  instrument.id = Entity::computeUniqueId();
  instrument.libraryId = library.id;
  instrument.type = type;
  instrument.mode = mode;
  instrument.state = state;
  instrument.name = std::move(name);
  return instrument;
}

InstrumentMeme ContentFixtures::buildInstrumentMeme(
    const Instrument &instrument,
    std::string name
) {
  InstrumentMeme instrumentMeme;
  instrumentMeme.id = Entity::computeUniqueId();
  instrumentMeme.instrumentId = instrument.id;
  instrumentMeme.name = std::move(name);
  return instrumentMeme;
}

void ContentFixtures::setupFixtureB1(ContentEntityStore *store) {

  // Project "bananas"
  project1 = ContentFixtures::buildProject("bananas");

  // Library "house"
  library2 = ContentFixtures::buildLibrary(project1, "house");

  // Template Binding to library 2
  template1 = ContentFixtures::buildTemplate(project1, "Test Template 1", "test1");
  templateBinding1 = ContentFixtures::buildTemplateBinding(template1, library2);

  // "Tropical, Wild to Cozy" macro-program in house library
  program4 = ContentFixtures::buildProgram(library2, Program::Type::Macro, Program::State::Published,
                                           "Tropical, Wild to Cozy", "C", 120.0f);
  program4_meme0 = ContentFixtures::buildMeme(program4, "Tropical");
  //
  program4_sequence0 = ContentFixtures::buildSequence(program4, 0, "Start Wild", 0.6f, "C");
  program4_sequence0_binding0 = ContentFixtures::buildBinding(program4_sequence0, 0);
  program4_sequence0_binding0_meme0 = ContentFixtures::buildMeme(program4_sequence0_binding0, "Wild");
  //
  program4_sequence1 = ContentFixtures::buildSequence(program4, 0, "Intermediate", 0.4f, "Bb minor");
  program4_sequence1_binding0 = ContentFixtures::buildBinding(program4_sequence1, 1);
  program4_sequence1_binding0_meme0 = ContentFixtures::buildMeme(program4_sequence1_binding0, "Cozy");
  program4_sequence1_binding0_meme1 = ContentFixtures::buildMeme(program4_sequence1_binding0, "Wild");
  //
  program4_sequence2 = ContentFixtures::buildSequence(program4, 0, "Finish Cozy", 0.4f, "Ab minor");
  program4_sequence2_binding0 = ContentFixtures::buildBinding(program4_sequence2, 2);
  program4_sequence2_binding0_meme0 = ContentFixtures::buildMeme(program4_sequence2_binding0, "Cozy");

  // Main program
  program5 = ContentFixtures::buildProgram(library2, Program::Type::Main, Program::State::Published,
                                           "Main Jam", "C minor", 140);
  program5_voiceBass = ContentFixtures::buildVoice(program5, Instrument::Type::Bass, "Bass");
  program5_voiceSticky = ContentFixtures::buildVoice(program5, Instrument::Type::Sticky, "Sticky");
  program5_voiceStripe = ContentFixtures::buildVoice(program5, Instrument::Type::Stripe, "Stripe");
  program5_voicePad = ContentFixtures::buildVoice(program5, Instrument::Type::Pad, "Pad");
  program5_meme0 = ContentFixtures::buildMeme(program5, "Outlook");
  //
  program5_sequence0 = ContentFixtures::buildSequence(program5, 16, "Intro", 0.5f, "G major");
  program5_sequence0_chord0 = ContentFixtures::buildChord(program5_sequence0, 0.0f, "G major");
  program5_sequence0_chord0_voicing = ContentFixtures::buildVoicing(program5_sequence0_chord0,
                                                                    program5_voiceBass, "G3, B3, D4");
  program5_sequence0_chord1 = ContentFixtures::buildChord(program5_sequence0, 8.0f, "Ab minor");
  program5_sequence0_chord1_voicing = ContentFixtures::buildVoicing(program5_sequence0_chord1,
                                                                    program5_voiceBass,
                                                                    "Ab3, Db3, F4");
  program5_sequence0_chord2 = ContentFixtures::buildChord(program5_sequence0, 75.0,
                                                          "G-9");// this ChordEntity should be ignored, because it's past the end of the main-pattern total
  program5_sequence0_chord2_voicing = ContentFixtures::buildVoicing(program5_sequence0_chord2,
                                                                    program5_voiceBass,
                                                                    "G3, Bb3, D4, A4");
  program5_sequence0_binding0 = ContentFixtures::buildBinding(program5_sequence0, 0);
  program5_sequence0_binding0_meme0 = ContentFixtures::buildMeme(program5_sequence0_binding0,
                                                                 "Optimism");
  //
  program5_sequence1 = ContentFixtures::buildSequence(program5, 32, "Drop", 0.5f, "G minor");
  program5_sequence1_chord0 = ContentFixtures::buildChord(program5_sequence1, 0.0f, "C major");
  //
  program5_sequence1_chord0_voicing = ContentFixtures::buildVoicing(program5_sequence1_chord0,
                                                                    program5_voiceBass,
                                                                    "Ab3, Db3, F4");
  program5_sequence1_chord1 = ContentFixtures::buildChord(program5_sequence1, 8.0f, "Bb minor");
  //
  program5_sequence1_chord1_voicing = ContentFixtures::buildVoicing(program5_sequence1_chord1,
                                                                    program5_voiceBass,
                                                                    "Ab3, Db3, F4");
  program5_sequence1_binding0 = ContentFixtures::buildBinding(program5_sequence1, 1);
  program5_sequence1_binding0_meme0 = ContentFixtures::buildMeme(program5_sequence1_binding0,
                                                                 "Pessimism");
  program5_sequence1_binding1 = ContentFixtures::buildBinding(program5_sequence1, 1);
  program5_sequence1_binding1_meme0 = ContentFixtures::buildMeme(program5_sequence1_binding0,
                                                                 "Pessimism");

  // A basic beat
  program35 = ContentFixtures::buildProgram(library2, Program::Type::Beat, Program::State::Published,
                                            "Basic Beat", "C", 121);
  program35_meme0 = ContentFixtures::buildMeme(program35, "Basic");
  program35_voice0 = ContentFixtures::buildVoice(program35, Instrument::Type::Drum, "Drums");
  program35_voice0_track0 = ContentFixtures::buildTrack(program35_voice0, "KICK");
  program35_voice0_track1 = ContentFixtures::buildTrack(program35_voice0, "SNARE");
  program35_voice0_track2 = ContentFixtures::buildTrack(program35_voice0, "KICK");
  program35_voice0_track3 = ContentFixtures::buildTrack(program35_voice0, "SNARE");
  //
  program35_sequence0 = ContentFixtures::buildSequence(program35, 16, "Base", 0.5f, "C");
  program35_sequence0_pattern0 = ContentFixtures::buildPattern(program35_sequence0, program35_voice0,
                                                               4, "Drop");
  program35_sequence0_pattern0_event0 = ContentFixtures::buildEvent(program35_sequence0_pattern0,
                                                                    program35_voice0_track0, 0.0f,
                                                                    1.0f, "C2", 1.0f);
  program35_sequence0_pattern0_event1 = ContentFixtures::buildEvent(program35_sequence0_pattern0,
                                                                    program35_voice0_track1, 1.0f,
                                                                    1.0f, "G5", 0.8f);
  program35_sequence0_pattern0_event2 = ContentFixtures::buildEvent(program35_sequence0_pattern0,
                                                                    program35_voice0_track2, 2.5f,
                                                                    1.0f, "C2", 0.6f);
  program35_sequence0_pattern0_event3 = ContentFixtures::buildEvent(program35_sequence0_pattern0,
                                                                    program35_voice0_track3, 3.0f,
                                                                    1.0f, "G5", 0.9f);
  //
  program35_sequence0_pattern1 = ContentFixtures::buildPattern(program35_sequence0, program35_voice0,
                                                               4, "Drop Alt");
  program35_sequence0_pattern1_event0 = ContentFixtures::buildEvent(program35_sequence0_pattern1,
                                                                    program35_voice0_track0, 0.0f,
                                                                    1.0f, "B5", 0.9f);
  program35_sequence0_pattern1_event1 = ContentFixtures::buildEvent(program35_sequence0_pattern1,
                                                                    program35_voice0_track1, 1.0f,
                                                                    1.0f, "D2", 1.0f);
  program35_sequence0_pattern1_event2 = ContentFixtures::buildEvent(program35_sequence0_pattern1,
                                                                    program35_voice0_track2, 2.5f,
                                                                    1.0f, "E4", 0.7f);
  program35_sequence0_pattern1_event3 = ContentFixtures::buildEvent(program35_sequence0_pattern1,
                                                                    program35_voice0_track3, 3.0f,
                                                                    1.0f, "c3", 0.5f);

  // Put all entities into the given content store
  store->put(project1);
  store->put(library2);
  store->put(program35);
  store->put(program35_voice0);
  store->put(program35_voice0_track0);
  store->put(program35_voice0_track1);
  store->put(program35_voice0_track2);
  store->put(program35_voice0_track3);
  store->put(program35_meme0);
  store->put(program35_sequence0);
  store->put(program35_sequence0_pattern0);
  store->put(program35_sequence0_pattern0_event0);
  store->put(program35_sequence0_pattern0_event1);
  store->put(program35_sequence0_pattern0_event2);
  store->put(program35_sequence0_pattern0_event3);
  store->put(program35_sequence0_pattern1);
  store->put(program35_sequence0_pattern1_event0);
  store->put(program35_sequence0_pattern1_event1);
  store->put(program35_sequence0_pattern1_event2);
  store->put(program35_sequence0_pattern1_event3);
  store->put(program4);
  store->put(program4_meme0);
  store->put(program4_sequence0);
  store->put(program4_sequence0_binding0);
  store->put(program4_sequence0_binding0_meme0);
  store->put(program4_sequence1);
  store->put(program4_sequence1_binding0);
  store->put(program4_sequence1_binding0_meme0);
  store->put(program4_sequence1_binding0_meme1);
  store->put(program4_sequence2);
  store->put(program4_sequence2_binding0);
  store->put(program4_sequence2_binding0_meme0);
  store->put(program5);
  store->put(program5_voiceBass);
  store->put(program5_voiceSticky);
  store->put(program5_voiceStripe);
  store->put(program5_voicePad);
  store->put(program5_meme0);
  store->put(program5_sequence0);
  store->put(program5_sequence0_binding0);
  store->put(program5_sequence0_binding0_meme0);
  store->put(program5_sequence0_chord0);
  store->put(program5_sequence0_chord0_voicing);
  store->put(program5_sequence0_chord1);
  store->put(program5_sequence0_chord1_voicing);
  store->put(program5_sequence0_chord2);
  store->put(program5_sequence0_chord2_voicing);
  store->put(program5_sequence1);
  store->put(program5_sequence1_binding0);
  store->put(program5_sequence1_binding0_meme0);
  store->put(program5_sequence1_chord0);
  store->put(program5_sequence1_chord0_voicing);
  store->put(program5_sequence1_chord1);
  store->put(program5_sequence1_chord1_voicing);
  store->put(template1);
  store->put(templateBinding1);
}

void ContentFixtures::setupFixtureB2(ContentEntityStore *store) {
  // "Tangy, Chunky to Smooth" macro-program in house library
  program3 = ContentFixtures::buildProgram(library2, Program::Type::Macro, Program::State::Published,
                                           "Tangy, Chunky to Smooth", "G minor", 120.0f);
  program3_meme0 = ContentFixtures::buildMeme(program3, "Tangy");
  //
  program3_sequence0 = ContentFixtures::buildSequence(program3, 0, "Start Chunky", 0.4f, "G minor");
  program3_sequence0_binding0 = ContentFixtures::buildBinding(program3_sequence0, 0);
  program3_sequence0_binding0_meme0 = ContentFixtures::buildMeme(program3_sequence0_binding0,
                                                                 "Chunky");
  //
  program3_sequence1 = ContentFixtures::buildSequence(program3, 0, "Finish Smooth", 0.6f, "C");
  program3_sequence1_binding0 = ContentFixtures::buildBinding(program3_sequence1, 1);
  program3_sequence1_binding0_meme0 = ContentFixtures::buildMeme(program3_sequence1_binding0,
                                                                 "Smooth");

  // Main program
  program15 = ContentFixtures::buildProgram(library2, Program::Type::Main, Program::State::Published,
                                            "Next Jam", "Db minor", 140);
  program15_voiceBass = ContentFixtures::buildVoice(program5, Instrument::Type::Bass, "Bass");
  program15_meme0 = ContentFixtures::buildMeme(program15, "Hindsight");
  //
  program15_sequence0 = ContentFixtures::buildSequence(program15, 16, "Intro", 0.5f, "G minor");
  program15_sequence0_chord0 = ContentFixtures::buildChord(program15_sequence0, 0.0f, "G minor");
  program15_sequence0_chord0_voicing = ContentFixtures::buildVoicing(program15_sequence0_chord0,
                                                                     program15_voiceBass,
                                                                     "G3, Bb3, D4");
  program15_sequence0_chord1 = ContentFixtures::buildChord(program15_sequence0, 8.0f, "Ab minor");
  program15_sequence0_chord1_voicing = ContentFixtures::buildVoicing(program15_sequence0_chord1,
                                                                     program15_voiceBass,
                                                                     "Ab3, C3, Eb4");
  program15_sequence0_binding0 = ContentFixtures::buildBinding(program15_sequence0, 0);
  program15_sequence0_binding0_meme0 = ContentFixtures::buildMeme(program15_sequence0_binding0,
                                                                  "Regret");
  //
  program15_sequence1 = ContentFixtures::buildSequence(program15, 32, "Outro", 0.5f, "A major");
  program15_sequence1_chord0 = ContentFixtures::buildChord(program15_sequence1, 0.0f, "C major");
  program15_sequence1_chord0_voicing = ContentFixtures::buildVoicing(program15_sequence0_chord0,
                                                                     program15_voiceBass,
                                                                     "E3, G3, C4");
  program15_sequence1_chord1 = ContentFixtures::buildChord(program15_sequence1, 8.0f, "Bb major");
  program15_sequence1_chord1_voicing = ContentFixtures::buildVoicing(program15_sequence0_chord1,
                                                                     program15_voiceBass,
                                                                     "F3, Bb3, D4");
  program15_sequence1_binding0 = ContentFixtures::buildBinding(program15_sequence1, 1);
  program15_sequence1_binding0_meme0 = ContentFixtures::buildMeme(program15_sequence1_binding0,
                                                                  "Pride");
  program15_sequence1_binding0_meme1 = ContentFixtures::buildMeme(program15_sequence1_binding0,
                                                                  "Shame");

  // put the entities in the store
  store->put(program3);
  store->put(program3_meme0);
  store->put(program3_sequence0);
  store->put(program3_sequence0_binding0);
  store->put(program3_sequence0_binding0_meme0);
  store->put(program3_sequence1);
  store->put(program3_sequence1_binding0);
  store->put(program3_sequence1_binding0_meme0);
  store->put(program15);
  store->put(program15_voiceBass);
  store->put(program15_meme0);
  store->put(program15_sequence0);
  store->put(program15_sequence0_chord0);
  store->put(program15_sequence0_chord0_voicing);
  store->put(program15_sequence0_chord1);
  store->put(program15_sequence0_chord1_voicing);
  store->put(program15_sequence0_binding0);
  store->put(program15_sequence0_binding0_meme0);
  store->put(program15_sequence1);
  store->put(program15_sequence1_chord0);
  store->put(program15_sequence1_chord0_voicing);
  store->put(program15_sequence1_chord1);
  store->put(program15_sequence1_chord1_voicing);
  store->put(program15_sequence1_binding0);
  store->put(program15_sequence1_binding0_meme0);
  store->put(program15_sequence1_binding0_meme1);
}

void ContentFixtures::setupFixtureB3(ContentEntityStore *store) {
  // A basic beat
  program9 = ContentFixtures::buildProgram(library2, Program::Type::Beat, Program::State::Published,
                                           "Basic Beat", "C", 121);
  program9_meme0 = ContentFixtures::buildMeme(program9, "Basic");
  //
  program9_voice0 = ContentFixtures::buildVoice(program9, Instrument::Type::Drum, "Drums");
  program9_voice0_track0 = ContentFixtures::buildTrack(program9_voice0, "BLEEP");
  program9_voice0_track1 = ContentFixtures::buildTrack(program9_voice0, "BLEEP");
  program9_voice0_track2 = ContentFixtures::buildTrack(program9_voice0, "BLEEP");
  program9_voice0_track3 = ContentFixtures::buildTrack(program9_voice0, "BLEEP");
  program9_voice0_track4 = ContentFixtures::buildTrack(program9_voice0, "KICK");
  program9_voice0_track5 = ContentFixtures::buildTrack(program9_voice0, "SNARE");
  program9_voice0_track6 = ContentFixtures::buildTrack(program9_voice0, "KICK");
  program9_voice0_track7 = ContentFixtures::buildTrack(program9_voice0, "SNARE");
  program9_voice0_track8 = ContentFixtures::buildTrack(program9_voice0, "KICK");
  program9_voice0_track9 = ContentFixtures::buildTrack(program9_voice0, "SNARE");
  program9_voice0_track10 = ContentFixtures::buildTrack(program9_voice0, "KICK");
  program9_voice0_track11 = ContentFixtures::buildTrack(program9_voice0, "SNARE");
  program9_voice0_track12 = ContentFixtures::buildTrack(program9_voice0, "TOOT");
  program9_voice0_track13 = ContentFixtures::buildTrack(program9_voice0, "TOOT");
  program9_voice0_track14 = ContentFixtures::buildTrack(program9_voice0, "TOOT");
  program9_voice0_track15 = ContentFixtures::buildTrack(program9_voice0, "TOOT");
  //
  program9_sequence0 = ContentFixtures::buildSequence(program9, 16, "Base", 0.5f, "C");
  //
  program9_sequence0_pattern0 = ContentFixtures::buildPattern(program9_sequence0, program9_voice0, 4,
                                                              "Intro");
  program9_sequence0_pattern0_event0 = ContentFixtures::buildEvent(program9_sequence0_pattern0,
                                                                   program9_voice0_track0, 0, 1, "C2",
                                                                   1.0f);
  program9_sequence0_pattern0_event1 = ContentFixtures::buildEvent(program9_sequence0_pattern0,
                                                                   program9_voice0_track1, 1, 1, "G5",
                                                                   0.8f);
  program9_sequence0_pattern0_event2 = ContentFixtures::buildEvent(program9_sequence0_pattern0,
                                                                   program9_voice0_track2, 2.5f, 1,
                                                                   "C2", 0.6f);
  program9_sequence0_pattern0_event3 = ContentFixtures::buildEvent(program9_sequence0_pattern0,
                                                                   program9_voice0_track3, 3, 1, "G5",
                                                                   0.9f);
  //
  program9_sequence0_pattern1 = ContentFixtures::buildPattern(program9_sequence0, program9_voice0, 4,
                                                              "Loop A");
  program9_sequence0_pattern1_event0 = ContentFixtures::buildEvent(program9_sequence0_pattern1,
                                                                   program9_voice0_track4, 0, 1, "C2",
                                                                   1.0f);
  program9_sequence0_pattern1_event1 = ContentFixtures::buildEvent(program9_sequence0_pattern1,
                                                                   program9_voice0_track5, 1, 1, "G5",
                                                                   0.8f);
  program9_sequence0_pattern1_event2 = ContentFixtures::buildEvent(program9_sequence0_pattern1,
                                                                   program9_voice0_track6, 2.5f, 1,
                                                                   "C2", 0.6f);
  program9_sequence0_pattern1_event3 = ContentFixtures::buildEvent(program9_sequence0_pattern1,
                                                                   program9_voice0_track7, 3, 1, "G5",
                                                                   0.9f);
  //
  program9_sequence0_pattern2 = ContentFixtures::buildPattern(program9_sequence0, program9_voice0, 4,
                                                              "Loop B");
  program9_sequence0_pattern2_event0 = ContentFixtures::buildEvent(program9_sequence0_pattern2,
                                                                   program9_voice0_track8, 0, 1, "B5",
                                                                   0.9f);
  program9_sequence0_pattern2_event1 = ContentFixtures::buildEvent(program9_sequence0_pattern2,
                                                                   program9_voice0_track9, 1, 1, "D2",
                                                                   1.0f);
  program9_sequence0_pattern2_event2 = ContentFixtures::buildEvent(program9_sequence0_pattern2,
                                                                   program9_voice0_track10, 2.5f, 1,
                                                                   "E4", 0.7f);
  program9_sequence0_pattern2_event3 = ContentFixtures::buildEvent(program9_sequence0_pattern2,
                                                                   program9_voice0_track11, 3, 1,
                                                                   "C3",
                                                                   0.5f);
  //
  program9_sequence0_pattern3 = ContentFixtures::buildPattern(program9_sequence0, program9_voice0, 4,
                                                              "Outro");
  program9_sequence0_pattern3_event0 = ContentFixtures::buildEvent(program9_sequence0_pattern3,
                                                                   program9_voice0_track12, 0, 1,
                                                                   "C2",
                                                                   1.0f);
  program9_sequence0_pattern3_event1 = ContentFixtures::buildEvent(program9_sequence0_pattern3,
                                                                   program9_voice0_track13, 1, 1,
                                                                   "G5",
                                                                   0.8f);
  program9_sequence0_pattern3_event2 = ContentFixtures::buildEvent(program9_sequence0_pattern3,
                                                                   program9_voice0_track14, 2.5f, 1,
                                                                   "C2", 0.6f);
  program9_sequence0_pattern3_event3 = ContentFixtures::buildEvent(program9_sequence0_pattern3,
                                                                   program9_voice0_track15, 3, 1,
                                                                   "G5",
                                                                   0.9f);

  // Instrument "808"
  instrument8 = ContentFixtures::buildInstrument(library2, Instrument::Type::Drum,
                                                 Instrument::Mode::Event, Instrument::State::Published,
                                                 "808 Drums");
  instrument8.volume = 0.76f;// For testing: Instrument has overall volume parameter https://github.com/xjmusic/xjmusic/issues/300
  instrument8_meme0 = ContentFixtures::buildMeme(instrument8, "heavy");
  instrument8_audio8kick = ContentFixtures::buildAudio(instrument8, "Kick",
                                                       "19801735098q47895897895782138975898.wav",
                                                       0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb",
                                                       1.0f);
  instrument8_audio8snare = ContentFixtures::buildAudio(instrument8, "Snare",
                                                        "975898198017350afghjkjhaskjdfjhk.wav", 0.01f,
                                                        1.5f, 120.0f, 0.62f, "SNARE", "Ab", 0.8f);
  instrument8_audio8bleep = ContentFixtures::buildAudio(instrument8, "Bleep",
                                                        "17350afghjkjhaskjdfjhk9758981980.wav", 0.01f,
                                                        1.5f, 120.0f, 0.62f, "BLEEP", "Ab", 0.8f);
  instrument8_audio8toot = ContentFixtures::buildAudio(instrument8, "Toot",
                                                       "askjdfjhk975898198017350afghjkjh.wav", 0.01f,
                                                       1.5f, 120.0f, 0.62f, "TOOT", "Ab", 0.8f);

  // put the entities in the store
  store->put(program9);
  store->put(program9_meme0);
  store->put(program9_voice0);
  store->put(program9_voice0_track0);
  store->put(program9_voice0_track1);
  store->put(program9_voice0_track2);
  store->put(program9_voice0_track3);
  store->put(program9_voice0_track4);
  store->put(program9_voice0_track5);
  store->put(program9_voice0_track6);
  store->put(program9_voice0_track7);
  store->put(program9_voice0_track8);
  store->put(program9_voice0_track9);
  store->put(program9_voice0_track10);
  store->put(program9_voice0_track11);
  store->put(program9_voice0_track12);
  store->put(program9_voice0_track13);
  store->put(program9_voice0_track14);
  store->put(program9_voice0_track15);
  store->put(program9_sequence0);
  store->put(program9_sequence0_pattern0);
  store->put(program9_sequence0_pattern0_event0);
  store->put(program9_sequence0_pattern0_event1);
  store->put(program9_sequence0_pattern0_event2);
  store->put(program9_sequence0_pattern0_event3);
  store->put(program9_sequence0_pattern1);
  store->put(program9_sequence0_pattern1_event0);
  store->put(program9_sequence0_pattern1_event1);
  store->put(program9_sequence0_pattern1_event2);
  store->put(program9_sequence0_pattern1_event3);
  store->put(program9_sequence0_pattern2);
  store->put(program9_sequence0_pattern2_event0);
  store->put(program9_sequence0_pattern2_event1);
  store->put(program9_sequence0_pattern2_event2);
  store->put(program9_sequence0_pattern2_event3);
  store->put(program9_sequence0_pattern3);
  store->put(program9_sequence0_pattern3_event0);
  store->put(program9_sequence0_pattern3_event1);
  store->put(program9_sequence0_pattern3_event2);
  store->put(program9_sequence0_pattern3_event3);
  store->put(instrument8);
  store->put(instrument8_meme0);
  store->put(instrument8_audio8kick);
  store->put(instrument8_audio8snare);
  store->put(instrument8_audio8bleep);
  store->put(instrument8_audio8toot);
}

void ContentFixtures::setupFixtureB4_DetailBass(ContentEntityStore *store) {
  // A basic bass pattern
  program10 = ContentFixtures::buildProgram(library2, Program::Type::Detail,
                                            Program::State::Published,
                                            "Earth Bass Detail Pattern", "C", 121);
  program10_meme0 = ContentFixtures::buildMeme(program10, "EARTH");
  //
  program10_voice0 = ContentFixtures::buildVoice(program10, Instrument::Type::Bass, "Dirty Bass");
  program10_voice0_track0 = ContentFixtures::buildTrack(program10_voice0, "BUM");
  //
  program10_sequence0 = ContentFixtures::buildSequence(program10, 16, "Simple Walk", 0.5f, "C");
  //
  program10_sequence0_pattern0 = ContentFixtures::buildPattern(program10_sequence0, program10_voice0,
                                                               4, "Intro");
  program10_sequence0_pattern0_event0 = ContentFixtures::buildEvent(program10_sequence0_pattern0,
                                                                    program10_voice0_track0, 0, 1,
                                                                    "C2", 1.0f);
  program10_sequence0_pattern0_event1 = ContentFixtures::buildEvent(program10_sequence0_pattern0,
                                                                    program10_voice0_track0, 1, 1,
                                                                    "G5", 0.8f);
  program10_sequence0_pattern0_event2 = ContentFixtures::buildEvent(program10_sequence0_pattern0,
                                                                    program10_voice0_track0, 2, 1,
                                                                    "C2", 0.6f);
  program10_sequence0_pattern0_event3 = ContentFixtures::buildEvent(program10_sequence0_pattern0,
                                                                    program10_voice0_track0, 3, 1,
                                                                    "G5", 0.9f);
  //
  program10_sequence0_pattern1 = ContentFixtures::buildPattern(program10_sequence0, program10_voice0,
                                                               4, "Loop A");
  program10_sequence0_pattern1_event0 = ContentFixtures::buildEvent(program10_sequence0_pattern1,
                                                                    program10_voice0_track0, 0, 1,
                                                                    "C2", 1.0f);
  program10_sequence0_pattern1_event1 = ContentFixtures::buildEvent(program10_sequence0_pattern1,
                                                                    program10_voice0_track0, 1, 1,
                                                                    "G5", 0.8f);
  program10_sequence0_pattern1_event2 = ContentFixtures::buildEvent(program10_sequence0_pattern1,
                                                                    program10_voice0_track0, 2, 1,
                                                                    "C2", 0.6f);
  program10_sequence0_pattern1_event3 = ContentFixtures::buildEvent(program10_sequence0_pattern1,
                                                                    program10_voice0_track0, 3, 1,
                                                                    "G5", 0.9f);
  //
  program10_sequence0_pattern2 = ContentFixtures::buildPattern(program10_sequence0, program10_voice0,
                                                               4, "Loop B");
  program10_sequence0_pattern2_event0 = ContentFixtures::buildEvent(program10_sequence0_pattern2,
                                                                    program10_voice0_track0, 0, 1,
                                                                    "B5", 0.9f);
  program10_sequence0_pattern2_event1 = ContentFixtures::buildEvent(program10_sequence0_pattern2,
                                                                    program10_voice0_track0, 1, 1,
                                                                    "D2", 1.0f);
  program10_sequence0_pattern2_event2 = ContentFixtures::buildEvent(program10_sequence0_pattern2,
                                                                    program10_voice0_track0, 2, 1,
                                                                    "E4", 0.7f);
  program10_sequence0_pattern2_event3 = ContentFixtures::buildEvent(program10_sequence0_pattern2,
                                                                    program10_voice0_track0, 3, 1,
                                                                    "C3", 0.5f);
  //
  program10_sequence0_pattern3 = ContentFixtures::buildPattern(program10_sequence0, program10_voice0,
                                                               4, "Outro");
  program10_sequence0_pattern3_event0 = ContentFixtures::buildEvent(program10_sequence0_pattern3,
                                                                    program10_voice0_track0, 0, 1,
                                                                    "C2", 1.0f);
  program10_sequence0_pattern3_event1 = ContentFixtures::buildEvent(program10_sequence0_pattern3,
                                                                    program10_voice0_track0, 1, 1,
                                                                    "G5", 0.8f);
  program10_sequence0_pattern3_event2 = ContentFixtures::buildEvent(program10_sequence0_pattern3,
                                                                    program10_voice0_track0, 2, 1,
                                                                    "C2", 0.6f);
  program10_sequence0_pattern3_event3 = ContentFixtures::buildEvent(program10_sequence0_pattern3,
                                                                    program10_voice0_track0, 3, 1,
                                                                    "G5", 0.9f);

  // Instrument "Bass"
  instrument9 = ContentFixtures::buildInstrument(library2, Instrument::Type::Bass,
                                                 Instrument::Mode::Event, Instrument::State::Published,
                                                 "Bass");
  instrument9_meme0 = ContentFixtures::buildMeme(instrument9, "heavy");
  instrument9_audio8 = ContentFixtures::buildAudio(instrument9, "bass",
                                                   "19801735098q47895897895782138975898.wav", 0.01f,
                                                   2.123f, 120.0f, 0.62f, "BLOOP", "Eb", 1.0f);

  // put them all in the store
  store->put(program10);
  store->put(program10_meme0);
  store->put(program10_voice0);
  store->put(program10_voice0_track0);
  store->put(program10_sequence0);
  store->put(program10_sequence0_pattern0);
  store->put(program10_sequence0_pattern0_event0);
  store->put(program10_sequence0_pattern0_event1);
  store->put(program10_sequence0_pattern0_event2);
  store->put(program10_sequence0_pattern0_event3);
  store->put(program10_sequence0_pattern1);
  store->put(program10_sequence0_pattern1_event0);
  store->put(program10_sequence0_pattern1_event1);
  store->put(program10_sequence0_pattern1_event2);
  store->put(program10_sequence0_pattern1_event3);
  store->put(program10_sequence0_pattern2);
  store->put(program10_sequence0_pattern2_event0);
  store->put(program10_sequence0_pattern2_event1);
  store->put(program10_sequence0_pattern2_event2);
  store->put(program10_sequence0_pattern2_event3);
  store->put(program10_sequence0_pattern3);
  store->put(program10_sequence0_pattern3_event0);
  store->put(program10_sequence0_pattern3_event1);
  store->put(program10_sequence0_pattern3_event2);
  store->put(program10_sequence0_pattern3_event3);
  store->put(instrument9);
  store->put(instrument9_meme0);
  store->put(instrument9_audio8);
}

void ContentFixtures::generatedFixture(ContentEntityStore *store, int N) {

  project1 = ContentFixtures::buildProject("Generated");
  store->put(project1);
  library1 = ContentFixtures::buildLibrary(project1, "generated");
  store->put(library1);

  template1 = ContentFixtures::buildTemplate(project1, "Complex Library Test", "complex");
  store->put(template1);
  store->put(ContentFixtures::buildTemplateBinding(template1, library1));

  // Create a N-magnitude set of unique major memes
  std::vector<std::string>
      majorMemeNames = listOfUniqueRandom(N, LoremIpsum::COLORS);
  std::vector<std::string>
      minorMemeNames = listOfUniqueRandom((long) (double) (N >> 1), LoremIpsum::VARIANTS);
  std::vector<std::string>
      percussiveNames = listOfUniqueRandom(N, LoremIpsum::PERCUSSIVE_NAMES);

  // Generate a Drum Instrument for each meme
  for (int i = 0; i < N; i++) {
    std::string majorMemeName = majorMemeNames[i];
    std::string minorMemeName = random(minorMemeNames);
    //
    Instrument instrument = ContentFixtures::buildInstrument(library1, Instrument::Type::Drum,
                                                             Instrument::Mode::Event,
                                                             Instrument::State::Published,
                                                             majorMemeName + " Drums");
    store->put(instrument);
    store->put(ContentFixtures::buildInstrumentMeme(instrument, majorMemeName));
    store->put(ContentFixtures::buildInstrumentMeme(instrument, minorMemeName));
    // audios of instrument
    for (int k = 0; k < N; k++)
      store->put(
          ContentFixtures::buildAudio(
              instrument, StringUtils::toProper(percussiveNames[k]),
              StringUtils::toLowerSlug(percussiveNames[k]) + ".wav",
              random(0, 0.05f),
              random(0.25f, 2),
              random(80, 120), 0.62f,
              percussiveNames[k],
              "X",
              random(0.8f, 1)));
    //
    std::cout << "Generated Drum-type Instrument id=" << instrument.id << ", minorMeme=" << minorMemeName
              << ", majorMeme=" << majorMemeName << std::endl;
  }

  // Generate Perc Loop Instruments
  for (int i = 0; i < N; i++) {
    Instrument instrument = ContentFixtures::buildInstrument(
        library1,
        Instrument::Type::Percussion,
        Instrument::Mode::Loop,
        Instrument::State::Published,
        "Perc Loop");
    store->put(instrument);
    std::cout << "Generated PercLoop-type Instrument id=" << instrument.id << std::endl;
  }

  // Generate N*2 total Macro-type programs, each transitioning of one MemeEntity to another
  for (int i = 0; i < N << 1; i++) {
    std::vector<std::string>
        twoMemeNames = listOfUniqueRandom(2, majorMemeNames);
    std::string majorMemeFromName = twoMemeNames[0];
    std::string majorMemeToName = twoMemeNames[1];
    std::string minorMemeName = random(minorMemeNames);
    std::vector<std::string>
        twoKeys = listOfUniqueRandom(2, LoremIpsum::MUSICAL_KEYS);
    std::string keyFrom = twoKeys[0];
    std::string keyTo = twoKeys[1];
    float intensityFrom = random(0.3f, 0.9f);
    float tempoFrom = random(80, 120);
    //
    std::string programName = minorMemeName;
    programName += ", create ";
    programName += majorMemeFromName;
    programName += " to ";
    programName += majorMemeToName;
    Program program = ContentFixtures::buildProgram(
        library1,
        Program::Type::Macro,
        Program::State::Published,
        programName,
        keyFrom,
        tempoFrom);
    store->put(program);
    store->put(ContentFixtures::buildProgramMeme(program, minorMemeName));
    // of offset 0
    ProgramSequence sequence0 = ContentFixtures::buildSequence(
        program,
        0,
        "Start " + majorMemeFromName,
        intensityFrom,
        keyFrom);
    store->put(sequence0);
    ProgramSequenceBinding binding0 = ContentFixtures::buildProgramSequenceBinding(sequence0, 0);
    store->put(binding0);
    store->put(
        ContentFixtures::buildProgramSequenceBindingMeme(
            binding0,
            majorMemeFromName));
    // to offset 1
    float intensityTo = random(0.3f, 0.9f);
    ProgramSequence sequence1 = ContentFixtures::buildSequence(program, 0,
                                                               "Finish " + majorMemeToName,
                                                               intensityTo, keyTo);
    store->put(sequence1);
    ProgramSequenceBinding binding1 = ContentFixtures::buildProgramSequenceBinding(sequence1, 1);
    store->put(binding1);
    store->put(
        ContentFixtures::buildProgramSequenceBindingMeme(binding1, majorMemeToName));
    //
    std::cout << "Generated Macro-type Program id=" << program.id << ", minorMeme=" << minorMemeName
              << ", majorMemeFrom=" << majorMemeFromName << ", majorMemeTo=" << majorMemeToName << std::endl;
  }

  // Generate N*4 total Main-type Programs, each having N patterns comprised of ~N*2 chords, bound to N*4 sequence patterns
  std::vector<ProgramSequence> sequences(N);
  for (int i = 0; i < N << 2; i++) {
    std::string majorMemeName = random(majorMemeNames);
    std::vector<std::string>
        sequenceNames = listOfUniqueRandom(N, LoremIpsum::ELEMENTS);
    std::vector<std::string>
        subKeys = listOfUniqueRandom(N, LoremIpsum::MUSICAL_KEYS);
    std::vector<float> subDensities = listOfRandomValues(N);
    float tempo = random(80, 120);
    //
    Program program = ContentFixtures::buildProgram(library1, Program::Type::Main,
                                                    Program::State::Published,
                                                    majorMemeName + ": " + StringUtils::join(sequenceNames, ", "),
                                                    subKeys[0], tempo);
    store->put(program);
    store->put(ContentFixtures::buildProgramMeme(program, majorMemeName));
    // sequences of program
    for (int iP = 0; iP < N; iP++) {
      int total = random(LoremIpsum::SEQUENCE_TOTALS);
      sequences[iP] = ContentFixtures::buildSequence(program, total,
                                                     majorMemeName + " in " + sequenceNames[iP],
                                                     subDensities[iP], subKeys[iP]);
      store->put(sequences[iP]);
      for (int iPC = 0; iPC < N << 2; iPC++) {
        // always use first chord, then use more chords with more intensity
        if (0 == iPC || random(0, 1) < subDensities[iP]) {
          store->put(
              ContentFixtures::buildChord(sequences[iP],
                                          std::floor((float) iPC * (float) total * 4 / (float) N),
                                          random(LoremIpsum::MUSICAL_CHORDS)));
        }
      }
    }
    // sequence sequence binding
    for (int offset = 0; offset < (N << 2); offset++) {
      int num = static_cast<int>(std::floor(random(0, (float) N)));
      ProgramSequenceBinding binding = ContentFixtures::buildProgramSequenceBinding(sequences[num], offset);
      store->put(binding);
      store->put(ContentFixtures::buildMeme(binding, random(minorMemeNames)));
    }
    std::cout << "Generated Main-type Program id=" << program.id << ", majorMeme=" << majorMemeName << " with " << N
              << " sequences bound " << (N << 2) << " times" << std::endl;
  }

  // Generate N total Beat-type Sequences, each having N voices, and N*2 patterns comprised of N*8 events
  std::vector<ProgramVoice> voices = std::vector<ProgramVoice>(N);
  std::unordered_map<std::string, ProgramVoiceTrack> trackMap;
  for (int i = 0; i < N; i++) {
    std::string majorMemeName = majorMemeNames[i];
    float tempo = random(80, 120);
    std::string key = random(LoremIpsum::MUSICAL_KEYS);
    float intensity = random(0.4f, 0.9f);
    //
    Program program = ContentFixtures::buildProgram(library1, Program::Type::Beat,
                                                    Program::State::Published,
                                                    majorMemeName + " Beat",
                                                    key,
                                                    tempo);
    store->put(program);
    trackMap.clear();
    store->put(ContentFixtures::buildProgramMeme(program, majorMemeName));
    // voices of program
    for (int iV = 0; iV < N; iV++) {
      voices[iV] = ContentFixtures::buildVoice(program, Instrument::Type::Drum,
                                               majorMemeName + " " + percussiveNames[iV]);
      store->put(voices[iV]);
    }
    ProgramSequence sequenceBase = ContentFixtures::buildSequence(program,
                                                                  random(LoremIpsum::SEQUENCE_TOTALS),
                                                                  "Base", intensity, key);
    store->put(sequenceBase);
    // patterns of program
    for (int iP = 0; iP < N << 1; iP++) {
      int total = random(LoremIpsum::PATTERN_TOTALS);
      int num = static_cast<int>(std::floor(random(0, (float) N)));

      // first pattern is always a Loop (because that's required) then the rest at random
      std::string patternName = majorMemeName;
      patternName += " ";
      patternName += majorMemeName;
      patternName += " pattern ";
      patternName += random(LoremIpsum::ELEMENTS);
      ProgramSequencePattern pattern = ContentFixtures::buildPattern(
          sequenceBase,
          voices[num],
          total,
          patternName
      );
      store->put(pattern);
      for (int iPE = 0; iPE < N << 2; iPE++) {
        // always use first chord, then use more chords with more intensity
        if (0 == iPE || random(0, 1) < intensity) {
          std::string name = percussiveNames[num];
          if (trackMap.find(name) == trackMap.end())
            trackMap[name] = ContentFixtures::buildTrack(voices[num], name);
          store->put(trackMap[name]);
          store->put(ContentFixtures::buildEvent(
              pattern,
              trackMap[name],
              static_cast<float>(std::floor(static_cast<float>(iPE) * (float) total * 4 / (float) N)),
              random(0.25f, 1.0f),
              "X",
              random(0.4f, 0.9f)
          ));
        }
      }
    }
    std::cout << "Generated Beat-type Program id=" << program.id << ", majorMeme=" << majorMemeName << " with " << N
              << " patterns" << std::endl;
  }
}
