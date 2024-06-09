// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "SegmentFixtures.h"
#include "TestHelpers.h"

#include <cmath>
#include <utility>

using namespace XJ;

std::vector<float> SegmentFixtures::listOfRandomValues(int N) {
  std::vector<float> result(N);
  for (int i = 0; i < N; i++) {
    result[i] = random(RANDOM_VALUE_FROM, RANDOM_VALUE_TO);
  }
  return result;
}

std::vector<std::string> SegmentFixtures::listOfUniqueRandom(long N, const std::vector<std::string> &sourceItems) {
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

float SegmentFixtures::random(float A, float B) {
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_real_distribution<> dis(A, B);
  return dis(gen);
}

std::string SegmentFixtures::random(std::vector<std::string> array) {
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_int_distribution<> dis(0, array.size() - 1);
  return array[dis(gen)];
}

int SegmentFixtures::random(std::vector<int> array) {
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_int_distribution<> dis(0, array.size() - 1);
  return array[dis(gen)];
}

Chain SegmentFixtures::buildChain(
    const Template &tmpl) {
  return buildChain(tmpl, Chain::State::Fabricate);
}

Chain SegmentFixtures::buildChain(
    const Template &tmpl,
    Chain::State state) {
  Chain chain;
  chain.id = TestHelpers::randomUUID();
  chain.templateId = tmpl.id;
  chain.name = "Test Chain";
  chain.type = Chain::Type::Production;
  chain.templateConfig = tmpl.config;
  chain.state = state;
  return chain;
}

Chain SegmentFixtures::buildChain(
    const Project &project,
    const std::string &name,
    Chain::Type type,
    Chain::State state,
    const Template &tmpl) {
  return buildChain(project, name, type, state, tmpl, StringUtils::toShipKey(name));
}

Chain SegmentFixtures::buildChain(
    const Project &project,
    const Template &tmpl,
    const std::string &name,
    Chain::Type type,
    Chain::State state) {
  return buildChain(project, name, type, state, tmpl, StringUtils::toShipKey(name));
}

Chain SegmentFixtures::buildChain(
    const Project &project,
    std::string name,
    Chain::Type type,
    Chain::State state,
    const Template &tmpl,
    const std::string &shipKey) {
  Chain chain;
  chain.id = TestHelpers::randomUUID();
  chain.templateId = tmpl.id;
  chain.name = std::move(name);
  chain.type = type;
  chain.state = state;
  chain.templateConfig = ContentFixtures::TEST_TEMPLATE_CONFIG;
  chain.shipKey = shipKey;
  return chain;
}

Segment SegmentFixtures::buildSegment() {
  Segment seg;
  seg.id = 123;
  return seg;
}

Segment SegmentFixtures::buildSegment(
    const Chain &chain,
    int id,
    Segment::State state,
    std::string key,
    int total,
    float intensity,
    float tempo,
    std::string storageKey) {
  return buildSegment(chain,
                      0 < id ? Segment::Type::Continue : Segment::Type::Initial,
                      id, 0, state, std::move(key), total, intensity, tempo, std::move(storageKey), state == Segment::State::Crafted);
}

Segment SegmentFixtures::buildSegment(
    const Chain &chain,
    Segment::Type type,
    int id,
    int delta,
    Segment::State state,
    std::string key,
    int total,
    float intensity,
    float tempo,
    std::string storageKey,
    bool hasEndSet) {
  Segment segment;
  segment.chainId = chain.id;
  segment.type = type;
  segment.id = id;
  segment.delta = delta;
  segment.state = state;
  segment.beginAtChainMicros =
      (long) (id * ValueUtils::MICROS_PER_SECOND * static_cast<float>(total * ValueUtils::SECONDS_PER_MINUTE / tempo));
  segment.key = std::move(key);
  segment.total = total;
  segment.intensity = intensity;
  segment.tempo = tempo;
  segment.storageKey = std::move(storageKey);
  segment.waveformPreroll = 0.0f;
  segment.waveformPostroll = 0.0;

  if (hasEndSet)
    segment.durationMicros =
        (long) (ValueUtils::MICROS_PER_SECOND * static_cast<float>(total * ValueUtils::SECONDS_PER_MINUTE / tempo));

  return segment;
}

Segment SegmentFixtures::buildSegment(
    const Chain &chain,
    std::string key,
    int total,
    float intensity,
    float tempo) {
  return buildSegment(chain, 0, Segment::State::Crafting, std::move(key), total, intensity, tempo, "segment123");
}

Segment SegmentFixtures::buildSegment(
    const Chain &chain,
    int offset,
    std::string key,
    int total,
    float intensity,
    float tempo) {
  return buildSegment(chain, offset, Segment::State::Crafting, std::move(key), total, intensity, tempo, "segment123");
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment &segment,
    Program::Type programType,
    const ProgramSequenceBinding &programSequenceBinding) {
  SegmentChoice segmentChoice;
  segmentChoice.id = TestHelpers::randomUUID();
  segmentChoice.segmentId = segment.id;
  segmentChoice.deltaIn = Segment::DELTA_UNLIMITED;
  segmentChoice.deltaOut = Segment::DELTA_UNLIMITED;
  segmentChoice.programId = programSequenceBinding.programId;
  segmentChoice.programSequenceBindingId = programSequenceBinding.id;
  segmentChoice.programType = programType;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment &segment,
    Program::Type programType,
    const ProgramSequence &programSequence) {
  SegmentChoice segmentChoice;
  segmentChoice.id = TestHelpers::randomUUID();
  segmentChoice.segmentId = segment.id;
  segmentChoice.deltaIn = Segment::DELTA_UNLIMITED;
  segmentChoice.deltaOut = Segment::DELTA_UNLIMITED;
  segmentChoice.programId = programSequence.programId;
  segmentChoice.programSequenceId = programSequence.id;
  segmentChoice.programType = programType;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment &segment,
    int deltaIn,
    int deltaOut,
    const Program &program,
    Instrument::Type instrumentType,
    Instrument::Mode instrumentMode) {
  SegmentChoice segmentChoice;
  segmentChoice.id = TestHelpers::randomUUID();
  segmentChoice.segmentId = segment.id;
  segmentChoice.deltaIn = deltaIn;
  segmentChoice.deltaOut = deltaOut;
  segmentChoice.programId = program.id;
  segmentChoice.programType = program.type;
  segmentChoice.mute = false;
  segmentChoice.instrumentType = instrumentType;
  segmentChoice.instrumentMode = instrumentMode;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment& segment,
    const Program& program) {
  SegmentChoice segmentChoice;
  segmentChoice.id = TestHelpers::randomUUID();
  segmentChoice.segmentId = segment.id;
  segmentChoice.deltaIn = Segment::DELTA_UNLIMITED;
  segmentChoice.deltaOut = Segment::DELTA_UNLIMITED;
  segmentChoice.programId = program.id;
  segmentChoice.programType = program.type;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment& segment,
    const Instrument& instrument) {
  SegmentChoice segmentChoice;
  segmentChoice.id = TestHelpers::randomUUID();
  segmentChoice.segmentId = segment.id;
  segmentChoice.deltaIn = Segment::DELTA_UNLIMITED;
  segmentChoice.deltaOut = Segment::DELTA_UNLIMITED;
  segmentChoice.instrumentId = instrument.id;
  segmentChoice.instrumentType = instrument.type;
  return segmentChoice;
}

SegmentMeta SegmentFixtures::buildSegmentMeta(
    const Segment& segment,
    std::string key,
    std::string value
    ) {
  SegmentMeta segmentMeta;
  segmentMeta.id = TestHelpers::randomUUID();
  segmentMeta.segmentId = segment.id;
  segmentMeta.key = std::move(key);
  segmentMeta.value = std::move(value);
  return segmentMeta;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment& segment,
    const Program& program,
    const ProgramSequence& programSequence,
    const ProgramVoice& voice,
    const Instrument& instrument) {
  SegmentChoice segmentChoice;
  segmentChoice.id = TestHelpers::randomUUID();
  segmentChoice.programVoiceId = voice.id;
  segmentChoice.instrumentId = instrument.id;
  segmentChoice.instrumentType = instrument.type;
  segmentChoice.mute = false;
  segmentChoice.instrumentMode = instrument.mode;
  segmentChoice.deltaIn = Segment::DELTA_UNLIMITED;
  segmentChoice.deltaOut = Segment::DELTA_UNLIMITED;
  segmentChoice.segmentId = segment.id;
  segmentChoice.programId = program.id;
  segmentChoice.programSequenceId = programSequence.id;
  segmentChoice.programType = program.type;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment& segment,
    int deltaIn,
    int deltaOut,
    const Program& program,
    const ProgramSequenceBinding& programSequenceBinding) {
  SegmentChoice choice = buildSegmentChoice(segment, deltaIn, deltaOut, program);
  choice.programSequenceBindingId = programSequenceBinding.id;
  return choice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment& segment,
    int deltaIn,
    int deltaOut,
    const Program& program,
    const ProgramVoice& voice,
    const Instrument& instrument) {
  SegmentChoice choice = buildSegmentChoice(segment, deltaIn, deltaOut, program);
  choice.programVoiceId = voice.id;
  choice.instrumentId = instrument.id;
  choice.instrumentType = instrument.type;
  choice.mute = false;
  choice.instrumentMode = instrument.mode;
  return choice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment& segment,
    int deltaIn,
    int deltaOut,
    const Program& program) {
  SegmentChoice choice;
  choice.id = TestHelpers::randomUUID();
  choice.segmentId = segment.id;
  choice.deltaIn = deltaIn;
  choice.deltaOut = deltaOut;
  choice.programId = program.id;
  choice.programType = program.type;
  return choice;
}

SegmentMeme SegmentFixtures::buildSegmentMeme(
    const Segment& segment,
    std::string name) {
  SegmentMeme segmentMeme;
  segmentMeme.id = TestHelpers::randomUUID();
  segmentMeme.segmentId = segment.id;
  segmentMeme.name = std::move(name);
  return segmentMeme;
}

SegmentChord SegmentFixtures::buildSegmentChord(
    const Segment& segment,
    double atPosition,
    std::string name) {
  SegmentChord segmentChord;
  segmentChord.id = TestHelpers::randomUUID();
  segmentChord.segmentId = segment.id;
  segmentChord.position = atPosition;
  segmentChord.name = std::move(name);
  return segmentChord;
}

SegmentChordVoicing SegmentFixtures::buildSegmentChordVoicing(
    const SegmentChord& chord,
    Instrument::Type type,
    std::string notes) {
  SegmentChordVoicing segmentChordVoicing;
  segmentChordVoicing.id = TestHelpers::randomUUID();
  segmentChordVoicing.segmentId = chord.segmentId;
  segmentChordVoicing.segmentChordId = chord.id;
  segmentChordVoicing.type = Instrument::toString(type);
  segmentChordVoicing.notes = std::move(notes);
  return segmentChordVoicing;
}

SegmentChoiceArrangement SegmentFixtures::buildSegmentChoiceArrangement(
    const SegmentChoice& segmentChoice) {
  SegmentChoiceArrangement segmentChoiceArrangement;
  segmentChoiceArrangement.id = TestHelpers::randomUUID();
  segmentChoiceArrangement.segmentId = segmentChoice.segmentId;
  segmentChoiceArrangement.segmentChoiceId = segmentChoice.id;
  return segmentChoiceArrangement;
}

SegmentChoiceArrangementPick SegmentFixtures::buildSegmentChoiceArrangementPick(
    const Segment& segment,
    const SegmentChoiceArrangement& segmentChoiceArrangement,
    const InstrumentAudio& instrumentAudio,
    std::string pickEvent) {
  float microsPerBeat = ValueUtils::MICROS_PER_SECOND * ValueUtils::SECONDS_PER_MINUTE / segment.tempo;
  SegmentChoiceArrangementPick pick;
  pick.id = TestHelpers::randomUUID();
  pick.segmentId = segmentChoiceArrangement.segmentId;
  pick.segmentChoiceArrangementId = segmentChoiceArrangement.id;
  pick.instrumentAudioId = instrumentAudio.id;
  pick.startAtSegmentMicros = (long) (0);
  pick.lengthMicros = (long) (instrumentAudio.loopBeats * microsPerBeat);
  pick.amplitude = 1;
  pick.tones = instrumentAudio.tones;
  pick.event = std::move(pickEvent);
  return pick;
}

SegmentChoiceArrangementPick SegmentFixtures::buildSegmentChoiceArrangementPick(
    const Segment& segment,
    const SegmentChoiceArrangement& segmentChoiceArrangement,
    const ProgramSequencePatternEvent& event,
    const InstrumentAudio& instrumentAudio,
    std::string pickEvent) {
  float microsPerBeat = ValueUtils::MICROS_PER_SECOND * ValueUtils::SECONDS_PER_MINUTE / segment.tempo;
  SegmentChoiceArrangementPick pick;
  pick.id = TestHelpers::randomUUID();
  pick.segmentId = segmentChoiceArrangement.segmentId;
  pick.segmentChoiceArrangementId = segmentChoiceArrangement.id;
  pick.programSequencePatternEventId = event.id;
  pick.instrumentAudioId = instrumentAudio.id;
  pick.startAtSegmentMicros = (long) (event.position * microsPerBeat);
  pick.lengthMicros = (long) (event.duration * microsPerBeat);
  pick.amplitude = event.velocity;
  pick.tones = event.tones;
  pick.event = std::move(pickEvent);
  return pick;
}

std::vector<AnyContentEntity> SegmentFixtures::setupFixtureB1() {

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

  // List of all parent entities including the library
  // ORDER IS IMPORTANT because this list will be used for real database entities, so ordered from parent -> child
  return {
      project1,
      library2,
      program35,
      program35_voice0,
      program35_voice0_track0,
      program35_voice0_track1,
      program35_voice0_track2,
      program35_voice0_track3,
      program35_meme0,
      program35_sequence0,
      program35_sequence0_pattern0,
      program35_sequence0_pattern0_event0,
      program35_sequence0_pattern0_event1,
      program35_sequence0_pattern0_event2,
      program35_sequence0_pattern0_event3,
      program35_sequence0_pattern1,
      program35_sequence0_pattern1_event0,
      program35_sequence0_pattern1_event1,
      program35_sequence0_pattern1_event2,
      program35_sequence0_pattern1_event3,
      program4,
      program4_meme0,
      program4_sequence0,
      program4_sequence0_binding0,
      program4_sequence0_binding0_meme0,
      program4_sequence1,
      program4_sequence1_binding0,
      program4_sequence1_binding0_meme0,
      program4_sequence1_binding0_meme1,
      program4_sequence2,
      program4_sequence2_binding0,
      program4_sequence2_binding0_meme0,
      program5,
      program5_voiceBass,
      program5_voiceSticky,
      program5_voiceStripe,
      program5_voicePad,
      program5_meme0,
      program5_sequence0,
      program5_sequence0_binding0,
      program5_sequence0_binding0_meme0,
      program5_sequence0_chord0,
      program5_sequence0_chord0_voicing,
      program5_sequence0_chord1,
      program5_sequence0_chord1_voicing,
      program5_sequence0_chord2,
      program5_sequence0_chord2_voicing,
      program5_sequence1,
      program5_sequence1_binding0,
      program5_sequence1_binding0_meme0,
      program5_sequence1_chord0,
      program5_sequence1_chord0_voicing,
      program5_sequence1_chord1,
      program5_sequence1_chord1_voicing,
      template1,
      templateBinding1};
}

std::vector<AnyContentEntity> SegmentFixtures::setupFixtureB2() {
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

  // return them all
  return {
      program3,
      program3_meme0,
      program3_sequence0,
      program3_sequence0_binding0,
      program3_sequence0_binding0_meme0,
      program3_sequence1,
      program3_sequence1_binding0,
      program3_sequence1_binding0_meme0,
      program15,
      program15_voiceBass,
      program15_meme0,
      program15_sequence0,
      program15_sequence0_chord0,
      program15_sequence0_chord0_voicing,
      program15_sequence0_chord1,
      program15_sequence0_chord1_voicing,
      program15_sequence0_binding0,
      program15_sequence0_binding0_meme0,
      program15_sequence1,
      program15_sequence1_chord0,
      program15_sequence1_chord0_voicing,
      program15_sequence1_chord1,
      program15_sequence1_chord1_voicing,
      program15_sequence1_binding0,
      program15_sequence1_binding0_meme0,
      program15_sequence1_binding0_meme1};
}

std::vector<AnyContentEntity> SegmentFixtures::setupFixtureB3() {
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
  instrument8.volume = 0.76f;// For testing: Instrument has overall volume parameter https://github.com/xjmusic/workstation/issues/300
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

  // return them all
  return {
      program9,
      program9_meme0,
      program9_voice0,
      program9_voice0_track0,
      program9_voice0_track1,
      program9_voice0_track2,
      program9_voice0_track3,
      program9_voice0_track4,
      program9_voice0_track5,
      program9_voice0_track6,
      program9_voice0_track7,
      program9_voice0_track8,
      program9_voice0_track9,
      program9_voice0_track10,
      program9_voice0_track11,
      program9_voice0_track12,
      program9_voice0_track13,
      program9_voice0_track14,
      program9_voice0_track15,
      program9_sequence0,
      program9_sequence0_pattern0,
      program9_sequence0_pattern0_event0,
      program9_sequence0_pattern0_event1,
      program9_sequence0_pattern0_event2,
      program9_sequence0_pattern0_event3,
      program9_sequence0_pattern1,
      program9_sequence0_pattern1_event0,
      program9_sequence0_pattern1_event1,
      program9_sequence0_pattern1_event2,
      program9_sequence0_pattern1_event3,
      program9_sequence0_pattern2,
      program9_sequence0_pattern2_event0,
      program9_sequence0_pattern2_event1,
      program9_sequence0_pattern2_event2,
      program9_sequence0_pattern2_event3,
      program9_sequence0_pattern3,
      program9_sequence0_pattern3_event0,
      program9_sequence0_pattern3_event1,
      program9_sequence0_pattern3_event2,
      program9_sequence0_pattern3_event3,
      instrument8,
      instrument8_meme0,
      instrument8_audio8kick,
      instrument8_audio8snare,
      instrument8_audio8bleep,
      instrument8_audio8toot};
}

std::vector<AnyContentEntity> SegmentFixtures::setupFixtureB4_DetailBass() {
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

  // return them all
  return {
      program10,
      program10_meme0,
      program10_voice0,
      program10_voice0_track0,
      program10_sequence0,
      program10_sequence0_pattern0,
      program10_sequence0_pattern0_event0,
      program10_sequence0_pattern0_event1,
      program10_sequence0_pattern0_event2,
      program10_sequence0_pattern0_event3,
      program10_sequence0_pattern1,
      program10_sequence0_pattern1_event0,
      program10_sequence0_pattern1_event1,
      program10_sequence0_pattern1_event2,
      program10_sequence0_pattern1_event3,
      program10_sequence0_pattern2,
      program10_sequence0_pattern2_event0,
      program10_sequence0_pattern2_event1,
      program10_sequence0_pattern2_event2,
      program10_sequence0_pattern2_event3,
      program10_sequence0_pattern3,
      program10_sequence0_pattern3_event0,
      program10_sequence0_pattern3_event1,
      program10_sequence0_pattern3_event2,
      program10_sequence0_pattern3_event3,
      instrument9,
      instrument9_meme0,
      instrument9_audio8};
}
std::vector<AnyContentEntity> SegmentFixtures::generatedFixture(int N) {
  std::vector<AnyContentEntity> entities;

  project1 = ContentFixtures::buildProject("Generated");
  entities.emplace_back(project1);
  library1 = ContentFixtures::buildLibrary(project1, "generated");
  entities.emplace_back(library1);

  template1 = ContentFixtures::buildTemplate(project1, "Complex Library Test", "complex");
  entities.emplace_back(template1);
  entities.emplace_back(ContentFixtures::buildTemplateBinding(template1, library1));

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
    entities.emplace_back(instrument);
    entities.emplace_back(ContentFixtures::buildInstrumentMeme(instrument, majorMemeName));
    entities.emplace_back(ContentFixtures::buildInstrumentMeme(instrument, minorMemeName));
    // audios of instrument
    for (int k = 0; k < N; k++)
      entities.emplace_back(
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
    entities.emplace_back(instrument);
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
    Program program = ContentFixtures::buildProgram(
        library1,
        Program::Type::Macro,
        Program::State::Published,
        minorMemeName + ", create " + majorMemeFromName + " to " + majorMemeToName,
        keyFrom,
        tempoFrom);
    entities.emplace_back(program);
    entities.emplace_back(ContentFixtures::buildProgramMeme(program, minorMemeName));
    // of offset 0
    ProgramSequence sequence0 = ContentFixtures::buildSequence(
        program,
        0,
        "Start " + majorMemeFromName,
        intensityFrom,
        keyFrom);
    entities.emplace_back(sequence0);
    ProgramSequenceBinding binding0 = ContentFixtures::buildProgramSequenceBinding(sequence0, 0);
    entities.emplace_back(binding0);
    entities.emplace_back(
        ContentFixtures::buildProgramSequenceBindingMeme(
            binding0,
            majorMemeFromName));
    // to offset 1
    float intensityTo = random(0.3f, 0.9f);
    ProgramSequence sequence1 = ContentFixtures::buildSequence(program, 0,
                                                                             "Finish " + majorMemeToName,
                                                                             intensityTo, keyTo);
    entities.emplace_back(sequence1);
    ProgramSequenceBinding binding1 = ContentFixtures::buildProgramSequenceBinding(sequence1, 1);
    entities.emplace_back(binding1);
    entities.emplace_back(
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
    entities.emplace_back(program);
    entities.emplace_back(ContentFixtures::buildProgramMeme(program, majorMemeName));
    // sequences of program
    for (int iP = 0; iP < N; iP++) {
      int total = random(LoremIpsum::SEQUENCE_TOTALS);
      sequences[iP] = ContentFixtures::buildSequence(program, total,
                                                                   majorMemeName + " in " + sequenceNames[iP],
                                                                   subDensities[iP], subKeys[iP]);
      entities.emplace_back(sequences[iP]);
      for (int iPC = 0; iPC < N << 2; iPC++) {
        // always use first chord, then use more chords with more intensity
        if (0 == iPC || ((float) std::rand() / RAND_MAX) < subDensities[iP]) {
          entities.emplace_back(
              ContentFixtures::buildChord(sequences[iP],
                                                        std::floor((float) iPC * total * 4 / N),
                                                        random(LoremIpsum::MUSICAL_CHORDS)));
        }
      }
    }
    // sequence sequence binding
    for (int offset = 0; offset < (N << 2); offset++) {
      int num = static_cast<int>(std::floor(std::rand() * N / static_cast<float>(RAND_MAX)));
      ProgramSequenceBinding binding = ContentFixtures::buildProgramSequenceBinding(sequences[num], offset);
      entities.push_back(binding);
      entities.push_back(ContentFixtures::buildMeme(binding, random(minorMemeNames)));
    }
    std::cout << "Generated Main-type Program id=" << program.id << ", majorMeme=" << majorMemeName << " with " << N << " sequences bound " << (N << 2) << " times" << std::endl;
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
    entities.emplace_back(program);
    trackMap.clear();
    entities.emplace_back(ContentFixtures::buildProgramMeme(program, majorMemeName));
    // voices of program
    for (int iV = 0; iV < N; iV++) {
      voices[iV] = ContentFixtures::buildVoice(program, Instrument::Type::Drum,
                                                             majorMemeName + " " + percussiveNames[iV]);
      entities.emplace_back(voices[iV]);
    }
    ProgramSequence sequenceBase = ContentFixtures::buildSequence(program,
                                                                                random(LoremIpsum::SEQUENCE_TOTALS),
                                                                                "Base", intensity, key);
    entities.emplace_back(sequenceBase);
    // patterns of program
    for (int iP = 0; iP < N << 1; iP++) {
      int total = random(LoremIpsum::PATTERN_TOTALS);
      int num = static_cast<int>(std::floor(std::rand() * N / static_cast<float>(RAND_MAX)));

      // first pattern is always a Loop (because that's required) then the rest at random
      ProgramSequencePattern pattern = ContentFixtures::buildPattern(sequenceBase, voices[num], total,

                                                                                   majorMemeName + " " +
                                                                                       majorMemeName +
                                                                                       " pattern" + " " +
                                                                                       random(
                                                                                           LoremIpsum::ELEMENTS));
      entities.emplace_back(pattern);
      for (int iPE = 0; iPE < N << 2; iPE++) {
        // always use first chord, then use more chords with more intensity
        if (0 == iPE || std::rand() < intensity) {
          std::string name = percussiveNames[num];
          if (trackMap.find(name) == trackMap.end())
            trackMap[name] = ContentFixtures::buildTrack(voices[num], name);
          entities.emplace_back(trackMap[name]);
          entities.emplace_back(ContentFixtures::buildEvent(pattern, trackMap[name],
                                                                          static_cast<float>(std::floor(static_cast<float>(iPE) * total * 4 / N)),
                                                                          random(0.25f, 1.0f), "X", random(0.4f, 0.9f)));
        }
      }
    }
    std::cout << "Generated Beat-type Program id=" << program.id << ", majorMeme=" << majorMemeName << " with " << N << " patterns" << std::endl;
  }

  return entities;
}
