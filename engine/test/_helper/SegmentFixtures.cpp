// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "SegmentFixtures.h"

#include <utility>

using namespace XJ;

Chain SegmentFixtures::buildChain(
    const Template &tmpl
) {
  return buildChain(tmpl, Chain::State::Fabricate);
}

Chain SegmentFixtures::buildChain(
    const Template &tmpl,
    Chain::State state
) {
  Chain chain;
  chain.id = Entity::randomUUID();
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
    const Template &tmpl
) {
  return buildChain(project, name, type, state, tmpl, StringUtils::toShipKey(name));
}

Chain SegmentFixtures::buildChain(
    const Project &project,
    const Template &tmpl,
    const std::string &name,
    Chain::Type type,
    Chain::State state
) {
  return buildChain(project, name, type, state, tmpl, StringUtils::toShipKey(name));
}

Chain SegmentFixtures::buildChain(
    const Project &project,
    std::string name,
    Chain::Type type,
    Chain::State state,
    const Template &tmpl,
    const std::string &shipKey
) {
  Chain chain;
  chain.id = Entity::randomUUID();
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
    std::string storageKey
) {
  return buildSegment(chain,
                      0 < id ? Segment::Type::Continue : Segment::Type::Initial,
                      id, 0, state, std::move(key), total, intensity, tempo, std::move(storageKey),
                      state == Segment::State::Crafted);
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
    bool hasEndSet
) {
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
    float tempo
) {
  return buildSegment(chain, 0, Segment::State::Crafting, std::move(key), total, intensity, tempo, "segment123");
}

Segment SegmentFixtures::buildSegment(
    const Chain &chain,
    int offset,
    std::string key,
    int total,
    float intensity,
    float tempo
) {
  return buildSegment(chain, offset, Segment::State::Crafting, std::move(key), total, intensity, tempo, "segment123");
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment &segment,
    Program::Type programType,
    const ProgramSequenceBinding &programSequenceBinding
) {
  SegmentChoice segmentChoice;
  segmentChoice.id = Entity::randomUUID();
  segmentChoice.segmentId = segment.id;
  segmentChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.programId = programSequenceBinding.programId;
  segmentChoice.programSequenceBindingId = programSequenceBinding.id;
  segmentChoice.programType = programType;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment &segment,
    Program::Type programType,
    const ProgramSequence &programSequence
) {
  SegmentChoice segmentChoice;
  segmentChoice.id = Entity::randomUUID();
  segmentChoice.segmentId = segment.id;
  segmentChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
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
    Instrument::Mode instrumentMode
) {
  SegmentChoice segmentChoice;
  segmentChoice.id = Entity::randomUUID();
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
    const Segment &segment,
    const Program &program
) {
  SegmentChoice segmentChoice;
  segmentChoice.id = Entity::randomUUID();
  segmentChoice.segmentId = segment.id;
  segmentChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.programId = program.id;
  segmentChoice.programType = program.type;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment &segment,
    const Instrument &instrument
) {
  SegmentChoice segmentChoice;
  segmentChoice.id = Entity::randomUUID();
  segmentChoice.segmentId = segment.id;
  segmentChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.instrumentId = instrument.id;
  segmentChoice.instrumentType = instrument.type;
  return segmentChoice;
}

SegmentMeta SegmentFixtures::buildSegmentMeta(
    const Segment &segment,
    std::string key,
    std::string value
) {
  SegmentMeta segmentMeta;
  segmentMeta.id = Entity::randomUUID();
  segmentMeta.segmentId = segment.id;
  segmentMeta.key = std::move(key);
  segmentMeta.value = std::move(value);
  return segmentMeta;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment &segment,
    const Program &program,
    const ProgramSequence &programSequence,
    const ProgramVoice &voice,
    const Instrument &instrument
) {
  SegmentChoice segmentChoice;
  segmentChoice.id = Entity::randomUUID();
  segmentChoice.programVoiceId = voice.id;
  segmentChoice.instrumentId = instrument.id;
  segmentChoice.instrumentType = instrument.type;
  segmentChoice.mute = false;
  segmentChoice.instrumentMode = instrument.mode;
  segmentChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.segmentId = segment.id;
  segmentChoice.programId = program.id;
  segmentChoice.programSequenceId = programSequence.id;
  segmentChoice.programType = program.type;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment &segment,
    int deltaIn,
    int deltaOut,
    const Program &program,
    const ProgramSequenceBinding &programSequenceBinding
) {
  SegmentChoice choice = buildSegmentChoice(segment, deltaIn, deltaOut, program);
  choice.programSequenceBindingId = programSequenceBinding.id;
  return choice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment &segment,
    int deltaIn,
    int deltaOut,
    const Program &program,
    const ProgramVoice &voice,
    const Instrument &instrument
) {
  SegmentChoice choice = buildSegmentChoice(segment, deltaIn, deltaOut, program);
  choice.programVoiceId = voice.id;
  choice.instrumentId = instrument.id;
  choice.instrumentType = instrument.type;
  choice.mute = false;
  choice.instrumentMode = instrument.mode;
  return choice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment &segment,
    int deltaIn,
    int deltaOut,
    const Program &program
) {
  SegmentChoice choice;
  choice.id = Entity::randomUUID();
  choice.segmentId = segment.id;
  choice.deltaIn = deltaIn;
  choice.deltaOut = deltaOut;
  choice.programId = program.id;
  choice.programType = program.type;
  return choice;
}

SegmentMeme SegmentFixtures::buildSegmentMeme(
    const Segment &segment,
    std::string name
) {
  SegmentMeme segmentMeme;
  segmentMeme.id = Entity::randomUUID();
  segmentMeme.segmentId = segment.id;
  segmentMeme.name = std::move(name);
  return segmentMeme;
}

SegmentChord SegmentFixtures::buildSegmentChord(
    const Segment &segment,
    double atPosition,
    std::string name
) {
  SegmentChord segmentChord;
  segmentChord.id = Entity::randomUUID();
  segmentChord.segmentId = segment.id;
  segmentChord.position = atPosition;
  segmentChord.name = std::move(name);
  return segmentChord;
}

SegmentChordVoicing SegmentFixtures::buildSegmentChordVoicing(
    const SegmentChord &chord,
    Instrument::Type type,
    std::string notes
) {
  SegmentChordVoicing segmentChordVoicing;
  segmentChordVoicing.id = Entity::randomUUID();
  segmentChordVoicing.segmentId = chord.segmentId;
  segmentChordVoicing.segmentChordId = chord.id;
  segmentChordVoicing.type = Instrument::toString(type);
  segmentChordVoicing.notes = std::move(notes);
  return segmentChordVoicing;
}

SegmentChoiceArrangement SegmentFixtures::buildSegmentChoiceArrangement(
    const SegmentChoice &segmentChoice
) {
  SegmentChoiceArrangement segmentChoiceArrangement;
  segmentChoiceArrangement.id = Entity::randomUUID();
  segmentChoiceArrangement.segmentId = segmentChoice.segmentId;
  segmentChoiceArrangement.segmentChoiceId = segmentChoice.id;
  return segmentChoiceArrangement;
}

SegmentChoiceArrangementPick SegmentFixtures::buildSegmentChoiceArrangementPick(
    const Segment &segment,
    const SegmentChoiceArrangement &segmentChoiceArrangement,
    const InstrumentAudio &instrumentAudio,
    std::string pickEvent
) {
  float microsPerBeat = ValueUtils::MICROS_PER_SECOND * ValueUtils::SECONDS_PER_MINUTE / segment.tempo;
  SegmentChoiceArrangementPick pick;
  pick.id = Entity::randomUUID();
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
    const Segment &segment,
    const SegmentChoiceArrangement &segmentChoiceArrangement,
    const ProgramSequencePatternEvent &event,
    const InstrumentAudio &instrumentAudio,
    std::string pickEvent
) {
  float microsPerBeat = ValueUtils::MICROS_PER_SECOND * ValueUtils::SECONDS_PER_MINUTE / segment.tempo;
  SegmentChoiceArrangementPick pick;
  pick.id = Entity::randomUUID();
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
