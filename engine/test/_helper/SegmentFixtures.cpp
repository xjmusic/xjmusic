// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <utility>

#include "xjmusic/util/ValueUtils.h"

#include "SegmentFixtures.h"
#include "ContentFixtures.h"

using namespace XJ;

Chain SegmentFixtures::buildChain(
    const Template *tmpl) {
  return buildChain(tmpl, Chain::State::Fabricate);
}

Chain SegmentFixtures::buildChain(
    const Template *tmpl,
    const Chain::State state) {
  Chain chain;
  chain.id = EntityUtils::computeUniqueId();
  chain.templateId = tmpl->id;
  chain.name = "Test Chain";
  chain.type = Chain::Type::Production;
  chain.templateConfig = tmpl->config;
  chain.state = state;
  return chain;
}

Chain SegmentFixtures::buildChain(
    const std::string &name,
    const Chain::Type type,
    const Chain::State state,
    const Template *tmpl) {
  return buildChain(name, type, state, tmpl, StringUtils::toShipKey(name));
}

Chain SegmentFixtures::buildChain(
    const Template *tmpl,
    const std::string &name,
    const Chain::Type type,
    const Chain::State state) {
  return buildChain(name, type, state, tmpl, StringUtils::toShipKey(name));
}

Chain SegmentFixtures::buildChain(
    std::string name,
    const Chain::Type type,
    const Chain::State state,
    const Template *tmpl,
    const std::string &shipKey) {
  Chain chain;
  chain.id = EntityUtils::computeUniqueId();
  chain.templateId = tmpl->id;
  chain.name = std::move(name);
  chain.type = type;
  chain.state = state;
  chain.templateConfig = TemplateConfig("outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n");
  chain.shipKey = shipKey;
  return chain;
}

Segment SegmentFixtures::buildSegment() {
  Segment seg;
  seg.id = 123;
  return seg;
}

Segment SegmentFixtures::buildSegment(
    const Chain *chain,
    const int id,
    const Segment::State state,
    std::string key,
    const int total,
    const float intensity,
    const float tempo,
    std::string storageKey) {
  return buildSegment(chain,
                      0 < id ? Segment::Type::Continue : Segment::Type::Initial,
                      id, 0, state, std::move(key), total, intensity, tempo, std::move(storageKey),
                      state == Segment::State::Crafted);
}

Segment SegmentFixtures::buildSegment(
    const Chain *chain,
    const Segment::Type type,
    const int id,
    const int delta,
    const Segment::State state,
    std::string key,
    const int total,
    const float intensity,
    const float tempo,
    std::string storageKey,
    const bool hasEndSet) {
  Segment segment;
  segment.chainId = chain->id;
  segment.type = type;
  segment.id = id;
  segment.delta = delta;
  segment.state = state;
  segment.beginAtChainMicros =
      static_cast<long>(id * ValueUtils::MICROS_PER_SECOND * (total * ValueUtils::SECONDS_PER_MINUTE / tempo));
  segment.key = std::move(key);
  segment.total = total;
  segment.intensity = intensity;
  segment.tempo = tempo;
  segment.storageKey = std::move(storageKey);
  segment.waveformPreroll = 0.0f;
  segment.waveformPostroll = 0.0;

  if (hasEndSet)
    segment.durationMicros =
        static_cast<long>(ValueUtils::MICROS_PER_SECOND * (total * ValueUtils::SECONDS_PER_MINUTE / tempo));

  return segment;
}

Segment SegmentFixtures::buildSegment(
    const Chain *chain,
    std::string key,
    const int total,
    const float intensity,
    const float tempo) {
  return buildSegment(chain, 0, Segment::State::Crafting, std::move(key), total, intensity, tempo, "segment123");
}

Segment SegmentFixtures::buildSegment(
    const Chain *chain,
    const int offset,
    std::string key,
    const int total,
    const float intensity,
    const float tempo) {
  return buildSegment(chain, offset, Segment::State::Crafting, std::move(key), total, intensity, tempo, "segment123");
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment *segment,
    const Program::Type programType,
    const ProgramSequenceBinding *programSequenceBinding) {
  SegmentChoice segmentChoice;
  segmentChoice.id = EntityUtils::computeUniqueId();
  segmentChoice.segmentId = segment->id;
  segmentChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.programId = programSequenceBinding->programId;
  segmentChoice.programSequenceBindingId = programSequenceBinding->id;
  segmentChoice.programType = programType;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment *segment,
    const Program::Type programType,
    const ProgramSequence *programSequence) {
  SegmentChoice segmentChoice;
  segmentChoice.id = EntityUtils::computeUniqueId();
  segmentChoice.segmentId = segment->id;
  segmentChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.programId = programSequence->programId;
  segmentChoice.programSequenceId = programSequence->id;
  segmentChoice.programType = programType;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment *segment,
    const int deltaIn,
    const int deltaOut,
    const Program *program,
    const Instrument::Type instrumentType,
    const Instrument::Mode instrumentMode) {
  SegmentChoice segmentChoice;
  segmentChoice.id = EntityUtils::computeUniqueId();
  segmentChoice.segmentId = segment->id;
  segmentChoice.deltaIn = deltaIn;
  segmentChoice.deltaOut = deltaOut;
  segmentChoice.programId = program->id;
  segmentChoice.programType = program->type;
  segmentChoice.mute = false;
  segmentChoice.instrumentType = instrumentType;
  segmentChoice.instrumentMode = instrumentMode;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment *segment,
    const Program *program) {
  SegmentChoice segmentChoice;
  segmentChoice.id = EntityUtils::computeUniqueId();
  segmentChoice.segmentId = segment->id;
  segmentChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.programId = program->id;
  segmentChoice.programType = program->type;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment *segment,
    const Instrument *instrument) {
  SegmentChoice segmentChoice;
  segmentChoice.id = EntityUtils::computeUniqueId();
  segmentChoice.segmentId = segment->id;
  segmentChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.instrumentId = instrument->id;
  segmentChoice.instrumentType = instrument->type;
  return segmentChoice;
}

SegmentMeta SegmentFixtures::buildSegmentMeta(
    const Segment *segment,
    std::string key,
    std::string value) {
  SegmentMeta segmentMeta;
  segmentMeta.id = EntityUtils::computeUniqueId();
  segmentMeta.segmentId = segment->id;
  segmentMeta.key = std::move(key);
  segmentMeta.value = std::move(value);
  return segmentMeta;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment *segment,
    const Program *program,
    const ProgramSequence *programSequence,
    const ProgramVoice *voice,
    const Instrument *instrument) {
  SegmentChoice segmentChoice;
  segmentChoice.id = EntityUtils::computeUniqueId();
  segmentChoice.programVoiceId = voice->id;
  segmentChoice.instrumentId = instrument->id;
  segmentChoice.instrumentType = instrument->type;
  segmentChoice.mute = false;
  segmentChoice.instrumentMode = instrument->mode;
  segmentChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  segmentChoice.segmentId = segment->id;
  segmentChoice.programId = program->id;
  segmentChoice.programSequenceId = programSequence->id;
  segmentChoice.programType = program->type;
  return segmentChoice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment *segment,
    const int deltaIn,
    const int deltaOut,
    const Program *program,
    const ProgramSequenceBinding *programSequenceBinding) {
  SegmentChoice choice = buildSegmentChoice(segment, deltaIn, deltaOut, program);
  choice.programSequenceBindingId = programSequenceBinding->id;
  return choice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment *segment,
    const int deltaIn,
    const int deltaOut,
    const Program *program,
    const ProgramVoice *voice,
    const Instrument *instrument) {
  SegmentChoice choice = buildSegmentChoice(segment, deltaIn, deltaOut, program);
  choice.programVoiceId = voice->id;
  choice.instrumentId = instrument->id;
  choice.instrumentType = instrument->type;
  choice.mute = false;
  choice.instrumentMode = instrument->mode;
  return choice;
}

SegmentChoice SegmentFixtures::buildSegmentChoice(
    const Segment *segment,
    const int deltaIn,
    const int deltaOut,
    const Program *program) {
  SegmentChoice choice;
  choice.id = EntityUtils::computeUniqueId();
  choice.segmentId = segment->id;
  choice.deltaIn = deltaIn;
  choice.deltaOut = deltaOut;
  choice.programId = program->id;
  choice.programType = program->type;
  return choice;
}

SegmentMeme SegmentFixtures::buildSegmentMeme(
    const Segment *segment,
    std::string name) {
  SegmentMeme segmentMeme;
  segmentMeme.id = EntityUtils::computeUniqueId();
  segmentMeme.segmentId = segment->id;
  segmentMeme.name = std::move(name);
  return segmentMeme;
}

SegmentChord SegmentFixtures::buildSegmentChord(
    const Segment *segment,
    const float atPosition,
    std::string name) {
  SegmentChord segmentChord;
  segmentChord.id = EntityUtils::computeUniqueId();
  segmentChord.segmentId = segment->id;
  segmentChord.position = atPosition;
  segmentChord.name = std::move(name);
  return segmentChord;
}

SegmentChordVoicing SegmentFixtures::buildSegmentChordVoicing(
    const SegmentChord *chord,
    const Instrument::Type type,
    std::string notes) {
  SegmentChordVoicing segmentChordVoicing;
  segmentChordVoicing.id = EntityUtils::computeUniqueId();
  segmentChordVoicing.segmentId = chord->segmentId;
  segmentChordVoicing.segmentChordId = chord->id;
  segmentChordVoicing.type = type;
  segmentChordVoicing.notes = std::move(notes);
  return segmentChordVoicing;
}

SegmentChoiceArrangement SegmentFixtures::buildSegmentChoiceArrangement(
    const SegmentChoice *segmentChoice) {
  SegmentChoiceArrangement segmentChoiceArrangement;
  segmentChoiceArrangement.id = EntityUtils::computeUniqueId();
  segmentChoiceArrangement.segmentId = segmentChoice->segmentId;
  segmentChoiceArrangement.segmentChoiceId = segmentChoice->id;
  return segmentChoiceArrangement;
}

SegmentChoiceArrangementPick SegmentFixtures::buildSegmentChoiceArrangementPick(
    const Segment *segment,
    const SegmentChoiceArrangement *segmentChoiceArrangement,
    const InstrumentAudio *instrumentAudio,
    std::string pickEvent) {
  const float microsPerBeat = ValueUtils::MICROS_PER_SECOND * ValueUtils::SECONDS_PER_MINUTE / segment->tempo;
  SegmentChoiceArrangementPick pick;
  pick.id = EntityUtils::computeUniqueId();
  pick.segmentId = segmentChoiceArrangement->segmentId;
  pick.segmentChoiceArrangementId = segmentChoiceArrangement->id;
  pick.instrumentAudioId = instrumentAudio->id;
  pick.startAtSegmentMicros = static_cast<long>(0);
  pick.lengthMicros = static_cast<long>(instrumentAudio->loopBeats * microsPerBeat);
  pick.amplitude = 1;
  pick.tones = instrumentAudio->tones;
  pick.event = std::move(pickEvent);
  return pick;
}

SegmentChoiceArrangementPick SegmentFixtures::buildSegmentChoiceArrangementPick(
    const Segment *segment,
    const SegmentChoiceArrangement *segmentChoiceArrangement,
    const ProgramSequencePatternEvent *event,
    const InstrumentAudio *instrumentAudio,
    std::string pickEvent) {
  const float microsPerBeat = ValueUtils::MICROS_PER_SECOND * ValueUtils::SECONDS_PER_MINUTE / segment->tempo;
  SegmentChoiceArrangementPick pick;
  pick.id = EntityUtils::computeUniqueId();
  pick.segmentId = segmentChoiceArrangement->segmentId;
  pick.segmentChoiceArrangementId = segmentChoiceArrangement->id;
  pick.programSequencePatternEventId = event->id;
  pick.instrumentAudioId = instrumentAudio->id;
  pick.startAtSegmentMicros = static_cast<long>(event->position * microsPerBeat);
  pick.lengthMicros = static_cast<long>(event->duration * microsPerBeat);
  pick.amplitude = event->velocity;
  pick.tones = event->tones;
  pick.event = std::move(pickEvent);
  return pick;
}
