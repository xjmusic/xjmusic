// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "ContentFixtures.h"
#include "TestHelpers.h"
#include "LoremIpsum.h"

#include <utility>

using namespace XJ;

const std::string ContentFixtures::TEST_TEMPLATE_CONFIG = "outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n";

std::vector<float> ContentFixtures::listOfRandomValues(const int N) {
  std::vector<float> result(N);
  for (int i = 0; i < N; i++) {
    result[i] = random(RANDOM_VALUE_FROM, RANDOM_VALUE_TO);
  }
  return result;
}

std::vector<std::string> ContentFixtures::listOfUniqueRandom(const long N, const std::vector<std::string> &sourceItems) {
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

float ContentFixtures::random(const float A, const float B) {
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
    const Instrument *instrument,
    const std::string &notes) {
  std::vector<std::variant<Instrument, InstrumentAudio>> result;
  result.emplace_back(*instrument);
  std::vector<std::string> splitNotes = StringUtils::split(notes, ',');
  for (const std::string &note: splitNotes) {
    const std::string name = Instrument::toString(instrument->type) + "-" + note;
    auto audio = buildAudio(instrument, name, note);
    result.emplace_back(audio);
  }
  return result;
}

InstrumentAudio ContentFixtures::buildAudio(
    const Instrument *instrument,
    std::string name,
    std::string waveformKey,
    const float start,
    const float length,
    const float tempo,
    const float intensity,
    std::string event,
    std::string note,
    const float volume) {
  InstrumentAudio instrumentAudio;
  instrumentAudio.id = EntityUtils::computeUniqueId();
  instrumentAudio.instrumentId = instrument->id;
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
    const Instrument *instrument,
    std::string name,
    std::string note) {
  InstrumentAudio instrumentAudio;
  instrumentAudio.id = EntityUtils::computeUniqueId();
  instrumentAudio.instrumentId = instrument->id;
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
  project.id = EntityUtils::computeUniqueId();
  return project;
}

Program ContentFixtures::buildProgram(
    const Library *library,
    const Program::Type type,
    const Program::State state,
    std::string name,
    std::string key,
    const float tempo) {
  Program program;
  program.id = EntityUtils::computeUniqueId();
  program.libraryId = library->id;
  program.type = type;
  program.state = state;
  program.name = std::move(name);
  program.key = std::move(key);
  program.tempo = tempo;
  return program;
}

Program ContentFixtures::buildProgram(
    const Program::Type type,
    std::string key,
    const float tempo
) {
  Program program;
  program.id = EntityUtils::computeUniqueId();
  program.libraryId = EntityUtils::computeUniqueId();
  program.type = type;
  program.state = Program::State::Published;
  program.name = "Test " + Program::toString(type) + "-Program";
  program.key = std::move(key);
  program.tempo = tempo;
  return program;
}

Program ContentFixtures::buildDetailProgram(
    std::string key,
    const bool doPatternRestartOnChord,
    std::string name
) {
  Program program;
  program.id = EntityUtils::computeUniqueId();
  program.libraryId = EntityUtils::computeUniqueId();
  program.type = Program::Type::Detail;
  program.state = Program::State::Published;
  program.name = std::move(name);
  program.key = std::move(key);
  program.config = "doPatternRestartOnChord=" + std::string(doPatternRestartOnChord ? "true" : "false");
  return program;
}

ProgramMeme ContentFixtures::buildMeme(
    const Program *program,
    std::string name) {
  ProgramMeme meme;
  meme.id = EntityUtils::computeUniqueId();
  meme.programId = program->id;
  meme.name = std::move(name);
  return meme;
}

ProgramSequence ContentFixtures::buildSequence(
    const Program *program,
    const int total,
    std::string name,
    const float intensity,
    std::string key) {
  ProgramSequence sequence;
  sequence.id = EntityUtils::computeUniqueId();
  sequence.programId = program->id;
  sequence.total = static_cast<short>(total);
  sequence.name = std::move(name);
  sequence.key = std::move(key);
  sequence.intensity = intensity;
  return sequence;
}

ProgramSequence ContentFixtures::buildSequence(
    const Program *program,
    const int total) {
  ProgramSequence sequence;
  sequence.id = EntityUtils::computeUniqueId();
  sequence.programId = program->id;
  sequence.total = static_cast<short>(total);
  sequence.name = "Test " + std::to_string(total) + "-beat Sequence";
  return sequence;
}

ProgramSequenceBinding ContentFixtures::buildBinding(
    const ProgramSequence *programSequence,
    const int offset) {
  ProgramSequenceBinding binding;
  binding.id = EntityUtils::computeUniqueId();
  binding.programId = programSequence->programId;
  binding.programSequenceId = programSequence->id;
  binding.offset = offset;
  return binding;
}

ProgramSequenceBindingMeme ContentFixtures::buildMeme(
    const ProgramSequenceBinding *programSequenceBinding,
    std::string name) {
  ProgramSequenceBindingMeme meme;
  meme.id = EntityUtils::computeUniqueId();
  meme.programId = programSequenceBinding->programId;
  meme.programSequenceBindingId = programSequenceBinding->id;
  meme.name = std::move(name);
  return meme;
}

ProgramSequenceChord ContentFixtures::buildChord(
    const ProgramSequence *programSequence,
    const float position,
    std::string name) {
  ProgramSequenceChord chord;
  chord.id = EntityUtils::computeUniqueId();
  chord.programSequenceId = programSequence->id;
  chord.programId = programSequence->programId;
  chord.position = position;
  chord.name = std::move(name);
  return chord;
}

ProgramSequenceChordVoicing ContentFixtures::buildVoicing(
    const ProgramSequenceChord *programSequenceChord,
    const ProgramVoice *voice,
    std::string notes) {
  ProgramSequenceChordVoicing voicing;
  voicing.id = EntityUtils::computeUniqueId();
  voicing.programId = programSequenceChord->programId;
  voicing.programSequenceChordId = programSequenceChord->id;
  voicing.programVoiceId = voice->id;
  voicing.notes = std::move(notes);
  return voicing;
}

ProgramVoice ContentFixtures::buildVoice(
    const Program *program,
    const Instrument::Type type,
    std::string name) {
  ProgramVoice voice;
  voice.id = EntityUtils::computeUniqueId();
  voice.programId = program->id;
  voice.type = type;
  voice.name = std::move(name);
  return voice;
}

ProgramVoice ContentFixtures::buildVoice(
    const Program *program,
    const Instrument::Type type) {
  return buildVoice(program, type, Instrument::toString(type));
}

ProgramVoiceTrack ContentFixtures::buildTrack(
    const ProgramVoice *programVoice,
    std::string name) {
  ProgramVoiceTrack track;
  track.id = EntityUtils::computeUniqueId();
  track.programId = programVoice->programId;
  track.programVoiceId = programVoice->id;
  track.name = std::move(name);
  return track;
}

ProgramVoiceTrack ContentFixtures::buildTrack(
    const ProgramVoice *programVoice) {
  return buildTrack(programVoice, Instrument::toString(programVoice->type));
}

ProgramSequencePattern ContentFixtures::buildPattern(
    const ProgramSequence *programSequence,
    const ProgramVoice *programVoice,
    const int total,
    std::string name) {
  ProgramSequencePattern pattern;
  pattern.id = EntityUtils::computeUniqueId();
  pattern.programId = programSequence->programId;
  pattern.programSequenceId = programSequence->id;
  pattern.programVoiceId = programVoice->id;
  pattern.total = static_cast<short>(total);
  pattern.name = std::move(name);
  return pattern;
}

ProgramSequencePattern ContentFixtures::buildPattern(
    const ProgramSequence *sequence,
    const ProgramVoice *voice,
    const int total) {
  return buildPattern(sequence, voice, total, sequence->name + " pattern");
}

ProgramSequencePatternEvent ContentFixtures::buildEvent(
    const ProgramSequencePattern *pattern,
    const ProgramVoiceTrack *track,
    const float position,
    const float duration,
    std::string note,
    const float velocity) {
  ProgramSequencePatternEvent event;
  event.id = EntityUtils::computeUniqueId();
  event.programId = pattern->programId;
  event.programSequencePatternId = pattern->id;
  event.programVoiceTrackId = track->id;
  event.position = position;
  event.duration = duration;
  event.tones = std::move(note);
  event.velocity = velocity;
  return event;
}

ProgramSequencePatternEvent ContentFixtures::buildEvent(
    const ProgramSequencePattern *pattern,
    const ProgramVoiceTrack *track,
    const float position,
    const float duration,
    std::string note) {
  return buildEvent(pattern, track, position, duration, std::move(note), 1.0f);
}

Instrument ContentFixtures::buildInstrument(
    const Instrument::Type type,
    const Instrument::Mode mode,
    const bool isTonal,
    const bool isMultiphonic
) {
  Instrument instrument;
  instrument.id = EntityUtils::computeUniqueId();
  instrument.libraryId = EntityUtils::computeUniqueId();
  instrument.type = type;
  instrument.mode = mode;
  instrument.state = Instrument::State::Published;
  instrument.config = "isTonal=" + std::string(isTonal ? "true" : "false") +
                      "\nisMultiphonic=" + std::string(isMultiphonic ? "true" : "false");
  instrument.name = "Test " + Instrument::toString(type) + "-Instrument";
  return instrument;
}

InstrumentMeme ContentFixtures::buildMeme(
    const Instrument *instrument,
    std::string name) {
  InstrumentMeme instrumentMeme;
  instrumentMeme.id = EntityUtils::computeUniqueId();
  instrumentMeme.instrumentId = instrument->id;
  instrumentMeme.name = std::move(name);
  return instrumentMeme;
}

InstrumentAudio ContentFixtures::buildInstrumentAudio(
    const Instrument *instrument,
    std::string name,
    std::string waveformKey,
    const float start,
    const float length,
    const float tempo,
    const float intensity,
    std::string event,
    std::string tones,
    const float volume) {
  InstrumentAudio instrumentAudio;
  instrumentAudio.id = EntityUtils::computeUniqueId();
  instrumentAudio.instrumentId = instrument->id;
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
    const Project *project,
    std::string name) {
  Library library;
  library.id = EntityUtils::computeUniqueId();
  library.projectId = project->id;
  library.name = std::move(name);
  return library;
}

Project ContentFixtures::buildProject(
    std::string name
) {
  Project project;
  project.id = EntityUtils::computeUniqueId();
  project.name = std::move(name);
  return project;
}

Template ContentFixtures::buildTemplate(
    const Project *project1,
    std::string name,
    std::string shipKey) {
  Template tmpl;
  tmpl.id = EntityUtils::computeUniqueId();
  tmpl.shipKey = std::move(shipKey);
  tmpl.config = TEST_TEMPLATE_CONFIG;
  tmpl.projectId = project1->id;
  tmpl.name = std::move(name);
  return tmpl;
}

Template ContentFixtures::buildTemplate(
    const Project *project1,
    std::string name,
    std::string shipKey,
    std::string config) {
  Template tmpl = buildTemplate(project1, std::move(name), std::move(shipKey));
  tmpl.config = std::move(config);
  return tmpl;
}

Template ContentFixtures::buildTemplate(
    const Project *project1,
    const std::string &name) {
  return buildTemplate(project1, name, name + "123");
}

TemplateBinding ContentFixtures::buildTemplateBinding(
    const Template *tmpl,
    const Library *library) {
  TemplateBinding templateBinding;
  templateBinding.id = EntityUtils::computeUniqueId();
  templateBinding.type = TemplateBinding::Type::Library;
  templateBinding.targetId = library->id;
  templateBinding.templateId = tmpl->id;
  return templateBinding;
}

TemplateBinding ContentFixtures::buildTemplateBinding(
    const Template *tmpl,
    const Program *program) {
  TemplateBinding templateBinding;
  templateBinding.id = EntityUtils::computeUniqueId();
  templateBinding.type = TemplateBinding::Type::Program;
  templateBinding.targetId = program->id;
  templateBinding.templateId = tmpl->id;
  return templateBinding;
}

TemplateBinding ContentFixtures::buildTemplateBinding(
    const Template *tmpl,
    const Instrument *instrument) {
  TemplateBinding templateBinding;
  templateBinding.id = EntityUtils::computeUniqueId();
  templateBinding.type = TemplateBinding::Type::Instrument;
  templateBinding.targetId = instrument->id;
  templateBinding.templateId = tmpl->id;
  return templateBinding;
}

ProgramMeme ContentFixtures::buildProgramMeme(
    const Program *program,
    std::string name) {
  ProgramMeme programMeme;
  programMeme.id = EntityUtils::computeUniqueId();
  programMeme.programId = program->id;
  programMeme.name = std::move(name);
  return programMeme;
}

ProgramSequence ContentFixtures::buildProgramSequence(
    const Program *program,
    const int total,
    std::string name,
    const float intensity,
    std::string key) {
  ProgramSequence programSequence;
  programSequence.id = EntityUtils::computeUniqueId();
  programSequence.programId = program->id;
  programSequence.total = static_cast<short>(total);
  programSequence.name = std::move(name);
  programSequence.key = std::move(key);
  programSequence.intensity = intensity;
  return programSequence;
}

ProgramSequenceBinding ContentFixtures::buildProgramSequenceBinding(
    const ProgramSequence *programSequence,
    const int offset) {
  ProgramSequenceBinding programSequenceBinding;
  programSequenceBinding.id = EntityUtils::computeUniqueId();
  programSequenceBinding.programId = programSequence->programId;
  programSequenceBinding.programSequenceId = programSequence->id;
  programSequenceBinding.offset = offset;
  return programSequenceBinding;
}

ProgramSequenceBindingMeme ContentFixtures::buildProgramSequenceBindingMeme(
    const ProgramSequenceBinding *programSequenceBinding,
    std::string name) {
  ProgramSequenceBindingMeme programSequenceBindingMeme;
  programSequenceBindingMeme.id = EntityUtils::computeUniqueId();
  programSequenceBindingMeme.programId = programSequenceBinding->programId;
  programSequenceBindingMeme.programSequenceBindingId = programSequenceBinding->id;
  programSequenceBindingMeme.name = std::move(name);
  return programSequenceBindingMeme;
}

ProgramSequenceChord ContentFixtures::buildProgramSequenceChord(
    const ProgramSequence *programSequence,
    const float position,
    std::string name) {
  ProgramSequenceChord programSequenceChord;
  programSequenceChord.id = EntityUtils::computeUniqueId();
  programSequenceChord.programSequenceId = programSequence->id;
  programSequenceChord.programId = programSequence->programId;
  programSequenceChord.position = position;
  programSequenceChord.name = std::move(name);
  return programSequenceChord;
}

ProgramSequenceChordVoicing ContentFixtures::buildProgramSequenceChordVoicing(
    const ProgramSequenceChord *programSequenceChord,
    const ProgramVoice *voice,
    std::string notes) {
  ProgramSequenceChordVoicing programSequenceChordVoicing;
  programSequenceChordVoicing.id = EntityUtils::computeUniqueId();
  programSequenceChordVoicing.programId = programSequenceChord->programId;
  programSequenceChordVoicing.programSequenceChordId = programSequenceChord->id;
  programSequenceChordVoicing.programVoiceId = voice->id;
  programSequenceChordVoicing.notes = std::move(notes);
  return programSequenceChordVoicing;
}

ProgramVoice ContentFixtures::buildProgramVoice(
    const Program *program,
    const Instrument::Type type,
    std::string name) {
  ProgramVoice programVoice;
  programVoice.id = EntityUtils::computeUniqueId();
  programVoice.programId = program->id;
  programVoice.type = type;
  programVoice.name = std::move(name);
  return programVoice;
}

ProgramVoiceTrack ContentFixtures::buildProgramVoiceTrack(
    const ProgramVoice *programVoice,
    std::string name) {
  ProgramVoiceTrack programVoiceTrack;
  programVoiceTrack.id = EntityUtils::computeUniqueId();
  programVoiceTrack.programId = programVoice->programId;
  programVoiceTrack.programVoiceId = programVoice->id;
  programVoiceTrack.name = std::move(name);
  return programVoiceTrack;
}

ProgramSequencePattern ContentFixtures::buildProgramSequencePattern(
    const ProgramSequence *programSequence,
    const ProgramVoice *programVoice,
    const int total,
    std::string name) {
  ProgramSequencePattern programSequencePattern;
  programSequencePattern.id = EntityUtils::computeUniqueId();
  programSequencePattern.programId = programSequence->programId;
  programSequencePattern.programSequenceId = programSequence->id;
  programSequencePattern.programVoiceId = programVoice->id;
  programSequencePattern.total = static_cast<short>(total);
  programSequencePattern.name = std::move(name);
  return programSequencePattern;
}

ProgramSequencePatternEvent ContentFixtures::buildProgramSequencePatternEvent(
    const ProgramSequencePattern *programSequencePattern,
    const ProgramVoiceTrack *programVoiceTrack,
    const float position,
    const float duration,
    std::string tones,
    const float velocity) {
  ProgramSequencePatternEvent programSequencePatternEvent;
  programSequencePatternEvent.id = EntityUtils::computeUniqueId();
  programSequencePatternEvent.programId = programSequencePattern->programId;
  programSequencePatternEvent.programSequencePatternId = programSequencePattern->id;
  programSequencePatternEvent.programVoiceTrackId = programVoiceTrack->id;
  programSequencePatternEvent.position = position;
  programSequencePatternEvent.duration = duration;
  programSequencePatternEvent.tones = std::move(tones);
  programSequencePatternEvent.velocity = velocity;
  return programSequencePatternEvent;
}

Instrument ContentFixtures::buildInstrument(
    const Library *library,
    const Instrument::Type type,
    const Instrument::Mode mode,
    const Instrument::State state,
    std::string name) {
  Instrument instrument;
  instrument.config = (new TemplateConfig())->toString();
  instrument.id = EntityUtils::computeUniqueId();
  instrument.libraryId = library->id;
  instrument.type = type;
  instrument.mode = mode;
  instrument.state = state;
  instrument.name = std::move(name);
  return instrument;
}

InstrumentMeme ContentFixtures::buildInstrumentMeme(
    const Instrument *instrument,
    std::string name) {
  InstrumentMeme instrumentMeme;
  instrumentMeme.id = EntityUtils::computeUniqueId();
  instrumentMeme.instrumentId = instrument->id;
  instrumentMeme.name = std::move(name);
  return instrumentMeme;
}

void ContentFixtures::setupFixtureB1(ContentEntityStore *store, bool includeBeat) {

  // Project "bananas"
  project1 = buildProject("bananas");

  // Library "house"
  library2 = buildLibrary(&project1, "house");

  // Template Binding to library 2
  template1 = buildTemplate(&project1, "Test Template 1", "test1");
  templateBinding1 = buildTemplateBinding(&template1, &library2);

  // "Tropical, Wild to Cozy" macro-program in house library
  program4 = buildProgram(&library2, Program::Type::Macro, Program::State::Published,
                                           "Tropical, Wild to Cozy", "C", 120.0f);
  program4_meme0 = buildMeme(&program4, "Tropical");
  //
  program4_sequence0 = buildSequence(&program4, 0, "Start Wild", 0.6f, "C");
  program4_sequence0_binding0 = buildBinding(&program4_sequence0, 0);
  program4_sequence0_binding0_meme0 = buildMeme(&program4_sequence0_binding0, "Wild");
  //
  program4_sequence1 = buildSequence(&program4, 0, "Intermediate", 0.4f, "Bb minor");
  program4_sequence1_binding0 = buildBinding(&program4_sequence1, 1);
  program4_sequence1_binding0_meme0 = buildMeme(&program4_sequence1_binding0, "Cozy");
  program4_sequence1_binding0_meme1 = buildMeme(&program4_sequence1_binding0, "Wild");
  //
  program4_sequence2 = buildSequence(&program4, 0, "Finish Cozy", 0.4f, "Ab minor");
  program4_sequence2_binding0 = buildBinding(&program4_sequence2, 2);
  program4_sequence2_binding0_meme0 = buildMeme(&program4_sequence2_binding0, "Cozy");

  // Main program
  program5 = buildProgram(&library2, Program::Type::Main, Program::State::Published,
                                           "Main Jam", "C minor", 140);
  program5_voiceBass = buildVoice(&program5, Instrument::Type::Bass, "Bass");
  program5_voiceSticky = buildVoice(&program5, Instrument::Type::Sticky, "Sticky");
  program5_voiceStripe = buildVoice(&program5, Instrument::Type::Stripe, "Stripe");
  program5_voicePad = buildVoice(&program5, Instrument::Type::Pad, "Pad");
  program5_meme0 = buildMeme(&program5, "Outlook");
  //
  program5_sequence0 = buildSequence(&program5, 16, "Intro", 0.5f, "G major");
  program5_sequence0_chord0 = buildChord(&program5_sequence0, 0.0f, "G major");
  program5_sequence0_chord0_voicing = buildVoicing(&program5_sequence0_chord0,
                                                                    &program5_voiceBass, "G3, B3, D4");
  program5_sequence0_chord1 = buildChord(&program5_sequence0, 8.0f, "Ab minor");
  program5_sequence0_chord1_voicing = buildVoicing(&program5_sequence0_chord1,
                                                                    &program5_voiceBass,
                                                                    "Ab3, Db3, F4");
  program5_sequence0_chord2 = buildChord(&program5_sequence0, 75.0,
                                                          "G-9");// this ChordEntity should be ignored, because it's past the end of the main-pattern total
  program5_sequence0_chord2_voicing = buildVoicing(&program5_sequence0_chord2,
                                                                    &program5_voiceBass,
                                                                    "G3, Bb3, D4, A4");
  program5_sequence0_binding0 = buildBinding(&program5_sequence0, 0);
  program5_sequence0_binding0_meme0 = buildMeme(&program5_sequence0_binding0,
                                                                 "Optimism");
  //
  program5_sequence1 = buildSequence(&program5, 32, "Drop", 0.5f, "G minor");
  program5_sequence1_chord0 = buildChord(&program5_sequence1, 0.0f, "C major");
  //
  program5_sequence1_chord0_voicing = buildVoicing(&program5_sequence1_chord0,
                                                                    &program5_voiceBass,
                                                                    "Ab3, Db3, F4");
  program5_sequence1_chord1 = buildChord(&program5_sequence1, 8.0f, "Bb minor");
  //
  program5_sequence1_chord1_voicing = buildVoicing(&program5_sequence1_chord1,
                                                                    &program5_voiceBass,
                                                                    "Ab3, Db3, F4");
  program5_sequence1_binding0 = buildBinding(&program5_sequence1, 1);
  program5_sequence1_binding0_meme0 = buildMeme(&program5_sequence1_binding0,
                                                                 "Pessimism");
  program5_sequence1_binding1 = buildBinding(&program5_sequence1, 1);
  program5_sequence1_binding1_meme0 = buildMeme(&program5_sequence1_binding0,
                                                                 "Pessimism");

  // A basic beat
  program35 = buildProgram(&library2, Program::Type::Beat, Program::State::Published,
                                            "Basic Beat", "C", 121);
  program35_meme0 = buildMeme(&program35, "Basic");
  program35_voice0 = buildVoice(&program35, Instrument::Type::Drum, "Drums");
  program35_voice0_track0 = buildTrack(&program35_voice0, "KICK");
  program35_voice0_track1 = buildTrack(&program35_voice0, "SNARE");
  program35_voice0_track2 = buildTrack(&program35_voice0, "KICK");
  program35_voice0_track3 = buildTrack(&program35_voice0, "SNARE");
  //
  program35_sequence0 = buildSequence(&program35, 16, "Base", 0.5f, "C");
  program35_sequence0_pattern0 = buildPattern(&program35_sequence0, &program35_voice0,
                                                               4, "Drop");
  program35_sequence0_pattern0_event0 = buildEvent(&program35_sequence0_pattern0,
                                                                    &program35_voice0_track0, 0.0f,
                                                                    1.0f, "C2", 1.0f);
  program35_sequence0_pattern0_event1 = buildEvent(&program35_sequence0_pattern0,
                                                                    &program35_voice0_track1, 1.0f,
                                                                    1.0f, "G5", 0.8f);
  program35_sequence0_pattern0_event2 = buildEvent(&program35_sequence0_pattern0,
                                                                    &program35_voice0_track2, 2.5f,
                                                                    1.0f, "C2", 0.6f);
  program35_sequence0_pattern0_event3 = buildEvent(&program35_sequence0_pattern0,
                                                                    &program35_voice0_track3, 3.0f,
                                                                    1.0f, "G5", 0.9f);
  //
  program35_sequence0_pattern1 = buildPattern(&program35_sequence0, &program35_voice0,
                                                               4, "Drop Alt");
  program35_sequence0_pattern1_event0 = buildEvent(&program35_sequence0_pattern1,
                                                                    &program35_voice0_track0, 0.0f,
                                                                    1.0f, "B5", 0.9f);
  program35_sequence0_pattern1_event1 = buildEvent(&program35_sequence0_pattern1,
                                                                    &program35_voice0_track1, 1.0f,
                                                                    1.0f, "D2", 1.0f);
  program35_sequence0_pattern1_event2 = buildEvent(&program35_sequence0_pattern1,
                                                                    &program35_voice0_track2, 2.5f,
                                                                    1.0f, "E4", 0.7f);
  program35_sequence0_pattern1_event3 = buildEvent(&program35_sequence0_pattern1,
                                                                    &program35_voice0_track3, 3.0f,
                                                                    1.0f, "c3", 0.5f);

  // Put all entities into the given content store
  store->put(project1);
  store->put(library2);
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
  if (includeBeat) {
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
  }
}

void ContentFixtures::setupFixtureB2(ContentEntityStore *store) {
  // "Tangy, Chunky to Smooth" macro-program in house library
  program3 = buildProgram(&library2, Program::Type::Macro, Program::State::Published,
                                           "Tangy, Chunky to Smooth", "G minor", 120.0f);
  program3_meme0 = buildMeme(&program3, "Tangy");
  //
  program3_sequence0 = buildSequence(&program3, 0, "Start Chunky", 0.4f, "G minor");
  program3_sequence0_binding0 = buildBinding(&program3_sequence0, 0);
  program3_sequence0_binding0_meme0 = buildMeme(&program3_sequence0_binding0,
                                                                 "Chunky");
  //
  program3_sequence1 = buildSequence(&program3, 0, "Finish Smooth", 0.6f, "C");
  program3_sequence1_binding0 = buildBinding(&program3_sequence1, 1);
  program3_sequence1_binding0_meme0 = buildMeme(&program3_sequence1_binding0,
                                                                 "Smooth");

  // Main program
  program15 = buildProgram(&library2, Program::Type::Main, Program::State::Published,
                                            "Next Jam", "Db minor", 140);
  program15_voiceBass = buildVoice(&program5, Instrument::Type::Bass, "Bass");
  program15_meme0 = buildMeme(&program15, "Hindsight");
  //
  program15_sequence0 = buildSequence(&program15, 16, "Intro", 0.5f, "G minor");
  program15_sequence0_chord0 = buildChord(&program15_sequence0, 0.0f, "G minor");
  program15_sequence0_chord0_voicing = buildVoicing(&program15_sequence0_chord0,
                                                                     &program15_voiceBass,
                                                                     "G3, Bb3, D4");
  program15_sequence0_chord1 = buildChord(&program15_sequence0, 8.0f, "Ab minor");
  program15_sequence0_chord1_voicing = buildVoicing(&program15_sequence0_chord1,
                                                                     &program15_voiceBass,
                                                                     "Ab3, C3, Eb4");
  program15_sequence0_binding0 = buildBinding(&program15_sequence0, 0);
  program15_sequence0_binding0_meme0 = buildMeme(&program15_sequence0_binding0,
                                                                  "Regret");
  //
  program15_sequence1 = buildSequence(&program15, 32, "Outro", 0.5f, "A major");
  program15_sequence1_chord0 = buildChord(&program15_sequence1, 0.0f, "C major");
  program15_sequence1_chord0_voicing = buildVoicing(&program15_sequence0_chord0,
                                                                     &program15_voiceBass,
                                                                     "E3, G3, C4");
  program15_sequence1_chord1 = buildChord(&program15_sequence1, 8.0f, "Bb major");
  program15_sequence1_chord1_voicing = buildVoicing(&program15_sequence0_chord1,
                                                                     &program15_voiceBass,
                                                                     "F3, Bb3, D4");
  program15_sequence1_binding0 = buildBinding(&program15_sequence1, 1);
  program15_sequence1_binding0_meme0 = buildMeme(&program15_sequence1_binding0,
                                                                  "Pride");
  program15_sequence1_binding0_meme1 = buildMeme(&program15_sequence1_binding0,
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
  program9 = buildProgram(&library2, Program::Type::Beat, Program::State::Published,
                                           "Basic Beat", "C", 121);
  program9_meme0 = buildMeme(&program9, "Basic");
  //
  program9_voice0 = buildVoice(&program9, Instrument::Type::Drum, "Drums");
  program9_voice0_track0 = buildTrack(&program9_voice0, "BLEEP");
  program9_voice0_track1 = buildTrack(&program9_voice0, "BLEEP");
  program9_voice0_track2 = buildTrack(&program9_voice0, "BLEEP");
  program9_voice0_track3 = buildTrack(&program9_voice0, "BLEEP");
  program9_voice0_track4 = buildTrack(&program9_voice0, "KICK");
  program9_voice0_track5 = buildTrack(&program9_voice0, "SNARE");
  program9_voice0_track6 = buildTrack(&program9_voice0, "KICK");
  program9_voice0_track7 = buildTrack(&program9_voice0, "SNARE");
  program9_voice0_track8 = buildTrack(&program9_voice0, "KICK");
  program9_voice0_track9 = buildTrack(&program9_voice0, "SNARE");
  program9_voice0_track10 = buildTrack(&program9_voice0, "KICK");
  program9_voice0_track11 = buildTrack(&program9_voice0, "SNARE");
  program9_voice0_track12 = buildTrack(&program9_voice0, "TOOT");
  program9_voice0_track13 = buildTrack(&program9_voice0, "TOOT");
  program9_voice0_track14 = buildTrack(&program9_voice0, "TOOT");
  program9_voice0_track15 = buildTrack(&program9_voice0, "TOOT");
  //
  program9_sequence0 = buildSequence(&program9, 16, "Base", 0.5f, "C");
  //
  program9_sequence0_pattern0 = buildPattern(&program9_sequence0, &program9_voice0, 4,
                                                              "Intro");
  program9_sequence0_pattern0_event0 = buildEvent(&program9_sequence0_pattern0,
                                                                   &program9_voice0_track0, 0, 1, "C2",
                                                                   1.0f);
  program9_sequence0_pattern0_event1 = buildEvent(&program9_sequence0_pattern0,
                                                                   &program9_voice0_track1, 1, 1, "G5",
                                                                   0.8f);
  program9_sequence0_pattern0_event2 = buildEvent(&program9_sequence0_pattern0,
                                                                   &program9_voice0_track2, 2.5f, 1,
                                                                   "C2", 0.6f);
  program9_sequence0_pattern0_event3 = buildEvent(&program9_sequence0_pattern0,
                                                                   &program9_voice0_track3, 3, 1, "G5",
                                                                   0.9f);
  //
  program9_sequence0_pattern1 = buildPattern(&program9_sequence0, &program9_voice0, 4,
                                                              "Loop A");
  program9_sequence0_pattern1_event0 = buildEvent(&program9_sequence0_pattern1,
                                                                   &program9_voice0_track4, 0, 1, "C2",
                                                                   1.0f);
  program9_sequence0_pattern1_event1 = buildEvent(&program9_sequence0_pattern1,
                                                                   &program9_voice0_track5, 1, 1, "G5",
                                                                   0.8f);
  program9_sequence0_pattern1_event2 = buildEvent(&program9_sequence0_pattern1,
                                                                   &program9_voice0_track6, 2.5f, 1,
                                                                   "C2", 0.6f);
  program9_sequence0_pattern1_event3 = buildEvent(&program9_sequence0_pattern1,
                                                                   &program9_voice0_track7, 3, 1, "G5",
                                                                   0.9f);
  //
  program9_sequence0_pattern2 = buildPattern(&program9_sequence0, &program9_voice0, 4,
                                                              "Loop B");
  program9_sequence0_pattern2_event0 = buildEvent(&program9_sequence0_pattern2,
                                                                   &program9_voice0_track8, 0, 1, "B5",
                                                                   0.9f);
  program9_sequence0_pattern2_event1 = buildEvent(&program9_sequence0_pattern2,
                                                                   &program9_voice0_track9, 1, 1, "D2",
                                                                   1.0f);
  program9_sequence0_pattern2_event2 = buildEvent(&program9_sequence0_pattern2,
                                                                   &program9_voice0_track10, 2.5f, 1,
                                                                   "E4", 0.7f);
  program9_sequence0_pattern2_event3 = buildEvent(&program9_sequence0_pattern2,
                                                                   &program9_voice0_track11, 3, 1,
                                                                   "C3",
                                                                   0.5f);
  //
  program9_sequence0_pattern3 = buildPattern(&program9_sequence0, &program9_voice0, 4,
                                                              "Outro");
  program9_sequence0_pattern3_event0 = buildEvent(&program9_sequence0_pattern3,
                                                                   &program9_voice0_track12, 0, 1,
                                                                   "C2",
                                                                   1.0f);
  program9_sequence0_pattern3_event1 = buildEvent(&program9_sequence0_pattern3,
                                                                   &program9_voice0_track13, 1, 1,
                                                                   "G5",
                                                                   0.8f);
  program9_sequence0_pattern3_event2 = buildEvent(&program9_sequence0_pattern3,
                                                                   &program9_voice0_track14, 2.5f, 1,
                                                                   "C2", 0.6f);
  program9_sequence0_pattern3_event3 = buildEvent(&program9_sequence0_pattern3,
                                                                   &program9_voice0_track15, 3, 1,
                                                                   "G5",
                                                                   0.9f);

  // Instrument "808"
  instrument8 = buildInstrument(&library2, Instrument::Type::Drum,
                                                 Instrument::Mode::Event, Instrument::State::Published,
                                                 "808 Drums");
  instrument8.volume = 0.76f;// For testing: Instrument has overall volume parameter https://github.com/xjmusic/xjmusic/issues/300
  instrument8_meme0 = buildMeme(&instrument8, "heavy");
  instrument8_audio8kick = buildAudio(&instrument8, "Kick",
                                                       "19801735098q47895897895782138975898.wav",
                                                       0.01f, 2.123f, 120.0f, 0.62f, "KICK", "Eb",
                                                       1.0f);
  instrument8_audio8snare = buildAudio(&instrument8, "Snare",
                                                        "975898198017350afghjkjhaskjdfjhk.wav", 0.01f,
                                                        1.5f, 120.0f, 0.62f, "SNARE", "Ab", 0.8f);
  instrument8_audio8bleep = buildAudio(&instrument8, "Bleep",
                                                        "17350afghjkjhaskjdfjhk9758981980.wav", 0.01f,
                                                        1.5f, 120.0f, 0.62f, "BLEEP", "Ab", 0.8f);
  instrument8_audio8toot = buildAudio(&instrument8, "Toot",
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
  program10 = buildProgram(&library2, Program::Type::Detail,
                                            Program::State::Published,
                                            "Earth Bass Detail Pattern", "C", 121);
  program10_meme0 = buildMeme(&program10, "EARTH");
  //
  program10_voice0 = buildVoice(&program10, Instrument::Type::Bass, "Dirty Bass");
  program10_voice0_track0 = buildTrack(&program10_voice0, "BUM");
  //
  program10_sequence0 = buildSequence(&program10, 16, "Simple Walk", 0.5f, "C");
  //
  program10_sequence0_pattern0 = buildPattern(&program10_sequence0, &program10_voice0,
                                                               4, "Intro");
  program10_sequence0_pattern0_event0 = buildEvent(&program10_sequence0_pattern0,
                                                                    &program10_voice0_track0, 0, 1,
                                                                    "C2", 1.0f);
  program10_sequence0_pattern0_event1 = buildEvent(&program10_sequence0_pattern0,
                                                                    &program10_voice0_track0, 1, 1,
                                                                    "G5", 0.8f);
  program10_sequence0_pattern0_event2 = buildEvent(&program10_sequence0_pattern0,
                                                                    &program10_voice0_track0, 2, 1,
                                                                    "C2", 0.6f);
  program10_sequence0_pattern0_event3 = buildEvent(&program10_sequence0_pattern0,
                                                                    &program10_voice0_track0, 3, 1,
                                                                    "G5", 0.9f);
  //
  program10_sequence0_pattern1 = buildPattern(&program10_sequence0, &program10_voice0,
                                                               4, "Loop A");
  program10_sequence0_pattern1_event0 = buildEvent(&program10_sequence0_pattern1,
                                                                    &program10_voice0_track0, 0, 1,
                                                                    "C2", 1.0f);
  program10_sequence0_pattern1_event1 = buildEvent(&program10_sequence0_pattern1,
                                                                    &program10_voice0_track0, 1, 1,
                                                                    "G5", 0.8f);
  program10_sequence0_pattern1_event2 = buildEvent(&program10_sequence0_pattern1,
                                                                    &program10_voice0_track0, 2, 1,
                                                                    "C2", 0.6f);
  program10_sequence0_pattern1_event3 = buildEvent(&program10_sequence0_pattern1,
                                                                    &program10_voice0_track0, 3, 1,
                                                                    "G5", 0.9f);
  //
  program10_sequence0_pattern2 = buildPattern(&program10_sequence0, &program10_voice0,
                                                               4, "Loop B");
  program10_sequence0_pattern2_event0 = buildEvent(&program10_sequence0_pattern2,
                                                                    &program10_voice0_track0, 0, 1,
                                                                    "B5", 0.9f);
  program10_sequence0_pattern2_event1 = buildEvent(&program10_sequence0_pattern2,
                                                                    &program10_voice0_track0, 1, 1,
                                                                    "D2", 1.0f);
  program10_sequence0_pattern2_event2 = buildEvent(&program10_sequence0_pattern2,
                                                                    &program10_voice0_track0, 2, 1,
                                                                    "E4", 0.7f);
  program10_sequence0_pattern2_event3 = buildEvent(&program10_sequence0_pattern2,
                                                                    &program10_voice0_track0, 3, 1,
                                                                    "C3", 0.5f);
  //
  program10_sequence0_pattern3 = buildPattern(&program10_sequence0, &program10_voice0,
                                                               4, "Outro");
  program10_sequence0_pattern3_event0 = buildEvent(&program10_sequence0_pattern3,
                                                                    &program10_voice0_track0, 0, 1,
                                                                    "C2", 1.0f);
  program10_sequence0_pattern3_event1 = buildEvent(&program10_sequence0_pattern3,
                                                                    &program10_voice0_track0, 1, 1,
                                                                    "G5", 0.8f);
  program10_sequence0_pattern3_event2 = buildEvent(&program10_sequence0_pattern3,
                                                                    &program10_voice0_track0, 2, 1,
                                                                    "C2", 0.6f);
  program10_sequence0_pattern3_event3 = buildEvent(&program10_sequence0_pattern3,
                                                                    &program10_voice0_track0, 3, 1,
                                                                    "G5", 0.9f);

  // Instrument "Bass"
  instrument9 = buildInstrument(&library2, Instrument::Type::Bass,
                                                 Instrument::Mode::Event, Instrument::State::Published,
                                                 "Bass");
  instrument9_meme0 = buildMeme(&instrument9, "heavy");
  instrument9_audio8 = buildAudio(&instrument9, "bass",
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

  project1 = buildProject("Generated");
  store->put(project1);
  library1 = buildLibrary(&project1, "generated");
  store->put(library1);

  template1 = buildTemplate(&project1, "Complex Library Test", "complex");
  store->put(template1);
  store->put(buildTemplateBinding(&template1, &library1));

  // Create a N-magnitude set of unique major memes
  std::vector<std::string>
      majorMemeNames = listOfUniqueRandom(N, LoremIpsum::COLORS);
  std::vector<std::string>
      minorMemeNames = listOfUniqueRandom(static_cast<long>((double) (N >> 1)), LoremIpsum::VARIANTS);
  std::vector<std::string>
      percussiveNames = listOfUniqueRandom(N, LoremIpsum::PERCUSSIVE_NAMES);

  // Generate a Drum Instrument for each meme
  for (int i = 0; i < N; i++) {
    std::string majorMemeName = majorMemeNames[i];
    std::string minorMemeName = random(minorMemeNames);
    //
    Instrument instrument = buildInstrument(&library1, Instrument::Type::Drum,
                                                             Instrument::Mode::Event,
                                                             Instrument::State::Published,
                                                             majorMemeName + " Drums");
    store->put(instrument);
    store->put(buildInstrumentMeme(&instrument, majorMemeName));
    store->put(buildInstrumentMeme(&instrument, minorMemeName));
    // audios of instrument
    for (int k = 0; k < N; k++)
      store->put(
          buildAudio(
              &instrument, StringUtils::toProper(percussiveNames[k]),
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
    Instrument instrument = buildInstrument(
        &library1,
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
    Program program = buildProgram(
        &library1,
        Program::Type::Macro,
        Program::State::Published,
        programName,
        keyFrom,
        tempoFrom);
    store->put(program);
    store->put(buildProgramMeme(&program, minorMemeName));
    // of offset 0
    ProgramSequence sequence0 = buildSequence(
        &program,
        0,
        "Start " + majorMemeFromName,
        intensityFrom,
        keyFrom);
    store->put(sequence0);
    ProgramSequenceBinding binding0 = buildProgramSequenceBinding(&sequence0, 0);
    store->put(binding0);
    store->put(
        buildProgramSequenceBindingMeme(
            &binding0,
            majorMemeFromName));
    // to offset 1
    float intensityTo = random(0.3f, 0.9f);
    ProgramSequence sequence1 = buildSequence(&program, 0,
                                                               "Finish " + majorMemeToName,
                                                               intensityTo, keyTo);
    store->put(sequence1);
    ProgramSequenceBinding binding1 = buildProgramSequenceBinding(&sequence1, 1);
    store->put(binding1);
    store->put(
        buildProgramSequenceBindingMeme(&binding1, majorMemeToName));
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
    Program program = buildProgram(&library1, Program::Type::Main,
                                                    Program::State::Published,
                                                    majorMemeName + ": " + StringUtils::join(sequenceNames, ", "),
                                                    subKeys[0], tempo);
    store->put(program);
    store->put(buildProgramMeme(&program, majorMemeName));
    // sequences of program
    for (int iP = 0; iP < N; iP++) {
      int total = random(LoremIpsum::SEQUENCE_TOTALS);
      sequences[iP] = buildSequence(&program, total,
                                                     majorMemeName + " in " + sequenceNames[iP],
                                                     subDensities[iP], subKeys[iP]);
      store->put(sequences[iP]);
      for (int iPC = 0; iPC < N << 2; iPC++) {
        // always use first chord, then use more chords with more intensity
        if (0 == iPC || random(0, 1) < subDensities[iP]) {
          store->put(
              buildChord(&sequences[iP],
                                          std::floor(static_cast<float>(iPC) * static_cast<float>(total) * 4 / static_cast<float>(N)),
                                          random(LoremIpsum::MUSICAL_CHORDS)));
        }
      }
    }
    // sequence sequence binding
    for (int offset = 0; offset < (N << 2); offset++) {
      int num = static_cast<int>(std::floor(random(0, static_cast<float>(N))));
      ProgramSequenceBinding binding = buildProgramSequenceBinding(&sequences[num], offset);
      store->put(binding);
      store->put(buildMeme(&binding, random(minorMemeNames)));
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
    Program program = buildProgram(&library1, Program::Type::Beat,
                                                    Program::State::Published,
                                                    majorMemeName + " Beat",
                                                    key,
                                                    tempo);
    store->put(program);
    trackMap.clear();
    store->put(buildProgramMeme(&program, majorMemeName));
    // voices of program
    for (int iV = 0; iV < N; iV++) {
      voices[iV] = buildVoice(&program, Instrument::Type::Drum,
                                               majorMemeName + " " + percussiveNames[iV]);
      store->put(voices[iV]);
    }
    ProgramSequence sequenceBase = buildSequence(&program,
                                                                  random(LoremIpsum::SEQUENCE_TOTALS),
                                                                  "Base", intensity, key);
    store->put(sequenceBase);
    // patterns of program
    for (int iP = 0; iP < N << 1; iP++) {
      int total = random(LoremIpsum::PATTERN_TOTALS);
      int num = static_cast<int>(std::floor(random(0, static_cast<float>(N))));

      // first pattern is always a Loop (because that's required) then the rest at random
      std::string patternName = majorMemeName;
      patternName += " ";
      patternName += majorMemeName;
      patternName += " pattern ";
      patternName += random(LoremIpsum::ELEMENTS);
      ProgramSequencePattern pattern = buildPattern(
          &sequenceBase,
          &voices[num],
          total,
          patternName);
      store->put(pattern);
      for (int iPE = 0; iPE < N << 2; iPE++) {
        // always use first chord, then use more chords with more intensity
        if (0 == iPE || random(0, 1) < intensity) {
          std::string name = percussiveNames[num];
          if (trackMap.find(name) == trackMap.end())
            trackMap[name] = buildTrack(&voices[num], name);
          store->put(trackMap[name]);
          store->put(buildEvent(
              &pattern,
              &trackMap[name],
              static_cast<float>(std::floor(static_cast<float>(iPE) * static_cast<float>(total) * 4 / static_cast<float>(N))),
              random(0.25f, 1.0f),
              "X",
              random(0.4f, 0.9f)));
        }
      }
    }
    std::cout << "Generated Beat-type Program id=" << program.id << ", majorMeme=" << majorMemeName << " with " << N
              << " patterns" << std::endl;
  }
}
