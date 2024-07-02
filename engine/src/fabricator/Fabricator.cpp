// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <spdlog/spdlog.h>

#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/FabricationException.h"
#include "xjmusic/fabricator/FabricationFatalException.h"
#include "xjmusic/fabricator/Fabricator.h"
#include "xjmusic/fabricator/MarbleBag.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/music/Step.h"
#include "xjmusic/util/CsvUtils.h"
#include "xjmusic/util/ValueUtils.h"

using namespace XJ;


const std::string Fabricator::NAME_SEPARATOR = "-";


const std::string Fabricator::UNKNOWN_KEY = "unknown";


Fabricator::Fabricator(
    ContentEntityStore *contentEntityStore,
    SegmentEntityStore *segmentEntityStore,
    SegmentRetrospective *segmentRetrospective,
    int segmentId,
    std::optional<Segment::Type> overrideSegmentType) : sourceMaterial(contentEntityStore),
                                                        store(segmentEntityStore),
                                                        retrospective(segmentRetrospective),
                                                        segmentId(segmentId) {

  // keep elapsed time based on system nano time
  startAtSystemNanoTime = std::chrono::high_resolution_clock::now();

  // read the chain, configs, and bindings
  const auto chainOpt = store->readChain();
  if (!chainOpt.has_value())
    throw FabricationFatalException("No chain found");

  chain = chainOpt.value();
  templateConfig = TemplateConfig(chain->templateConfig);
  templateBindings = sourceMaterial->getTemplateBindings();
  boundProgramIds = ChainUtils::targetIdsOfType(templateBindings, TemplateBinding::Type::Program);
  boundInstrumentIds = ChainUtils::targetIdsOfType(templateBindings, TemplateBinding::Type::Instrument);
  spdlog::debug("[segId={}] Chain {} configured with {} and bound to {} ",
                segmentId, chain->id, templateConfig.toString(), TemplateBinding::toPrettyCsv(templateBindings));

  // digest previous instrument audio
  preferredAudios = computePreferredInstrumentAudio();

  // the current segment on the workbench
  this->segmentId = segmentId;

  // Override the segment type by passing the fabricator a segment type on creation
  // live performance modulation https://github.com/xjmusic/xjmusic/issues/197
  if (overrideSegmentType.has_value()) {
    type = overrideSegmentType.value();
  }

  // pre-flight check
  ensureShipKey();
}


void Fabricator::addMessage(const SegmentMessage::Type messageType, std::string body) {
  try {
    SegmentMessage msg;
    msg.id = EntityUtils::computeUniqueId();
    msg.segmentId = getSegment()->id;
    msg.type = messageType;
    msg.body = std::move(body);
    put(msg);
  } catch (const FabricationFatalException &e) {
    spdlog::warn("Failed to add message!", e.what());
  }
}


void Fabricator::addErrorMessage(std::string body) {
  addMessage(SegmentMessage::Type::Error, std::move(body));
}


void Fabricator::addWarningMessage(std::string body) {
  addMessage(SegmentMessage::Type::Warning, std::move(body));
}


void Fabricator::addInfoMessage(std::string body) {
  addMessage(SegmentMessage::Type::Info, std::move(body));
}


void Fabricator::deletePick(const UUID &id) {
  store->deleteSegmentChoiceArrangementPick(segmentId, id);
}


std::set<const SegmentChoiceArrangement *> Fabricator::getArrangements() {
  return store->readAllSegmentChoiceArrangements(segmentId);
}


std::set<const SegmentChoiceArrangement *> Fabricator::getArrangements(std::set<const SegmentChoice *> &choices) {
  std::vector<UUID> choiceIds;
  for (const auto &choice: choices) {
    choiceIds.push_back(choice->id);
  }

  const std::set<const SegmentChoiceArrangement *> allArrangements = getArrangements();
  std::set<const SegmentChoiceArrangement *> filteredArrangements;

  for (const auto &arrangement: allArrangements) {
    if (std::find(choiceIds.begin(), choiceIds.end(), arrangement->segmentChoiceId) != choiceIds.end()) {
      filteredArrangements.emplace(arrangement);
    }
  }

  return filteredArrangements;
}


Chain *Fabricator::getChain() {
  return chain;
}


TemplateConfig Fabricator::getTemplateConfig() {
  return templateConfig;
}


std::set<const SegmentChoice *> Fabricator::getChoices() const {
  return store->readAllSegmentChoices(segmentId);
}


std::optional<const SegmentChord *> Fabricator::getChordAt(const float position) {
  std::optional<const SegmentChord *> foundChord;
  float foundPosition = -1.0f;// Initialize with a negative value

  // We assume that these entities are in order of position ascending
  std::vector<const SegmentChord *> segmentChords = getSegmentChords();
  for (const auto &segmentChord: segmentChords) {
    // If it's a better match (or no match has yet been found) then use it
    if (foundPosition < 0 || (segmentChord->position > foundPosition && segmentChord->position <= position)) {
      foundPosition = segmentChord->position;
      foundChord = segmentChord;
    }
  }

  return foundChord;
}

std::optional<const SegmentChoice *> Fabricator::getCurrentMainChoice() {
  return getChoiceOfType(Program::Type::Main);
}


std::set<const SegmentChoice *> Fabricator::getCurrentDetailChoices() {
  std::set<const SegmentChoice *> allChoices = getChoices();
  std::set<const SegmentChoice *> detailChoices;

  for (const auto &choice: allChoices) {
    if (choice->programType == Program::Type::Detail) {
      detailChoices.insert(choice);
    }
  }

  return detailChoices;
}


std::optional<const SegmentChoice *> Fabricator::getCurrentBeatChoice() {
  return getChoiceOfType(Program::Type::Beat);
}


std::set<Instrument::Type> Fabricator::getDistinctChordVoicingTypes() {
  if (distinctChordVoicingTypes == nullptr) {
    const auto mainChoice = getCurrentMainChoice();
    if (!mainChoice.has_value()) return std::set<Instrument::Type>{};
    const auto voicings = sourceMaterial->getSequenceChordVoicingsOfProgram(mainChoice.value()->programId);
    distinctChordVoicingTypes = new std::set<Instrument::Type>();
    for (const auto voicing: voicings) {
      try {
        distinctChordVoicingTypes->insert(getProgramVoiceType(voicing));
      } catch (FabricationException &e) {
        spdlog::warn("[seg-{}] Failed to get distinct chord voicing type! {}", segmentId, e.what());
      }
    }
  }

  return *distinctChordVoicingTypes;
}


long Fabricator::getElapsedMicros() {
  const auto now = std::chrono::high_resolution_clock::now();
  const auto elapsed = std::chrono::duration_cast<std::chrono::nanoseconds>(now - startAtSystemNanoTime);
  return elapsed.count() / ValueUtils::NANOS_PER_MICRO;// NOLINT(*-narrowing-conversions)
}


InstrumentConfig Fabricator::getInstrumentConfig(const Instrument *instrument) {
  auto [it, inserted] = instrumentConfigs.emplace(instrument->id, InstrumentConfig(*instrument));
  return it->second;
}


std::optional<const SegmentChoice *> Fabricator::getChoiceIfContinued(const ProgramVoice *voice) {
  if (getSegment()->type != Segment::Type::Continue) return std::nullopt;

  auto choices = retrospective->getChoices();
  auto it = std::find_if(choices.begin(), choices.end(), [&](const SegmentChoice *choice) {
    const auto candidateVoice = sourceMaterial->getProgramVoice(choice->programVoiceId);
    return candidateVoice.has_value() && candidateVoice.value()->name == voice->name &&
           candidateVoice.value()->type == voice->type;
  });

  if (it == choices.end()) {
    spdlog::warn("[seg-{}] Could not get previous voice instrumentId for voiceName={}", segmentId, voice->name);
    return std::nullopt;
  }

  return {*it};
}


std::optional<const SegmentChoice *> Fabricator::getChoiceIfContinued(const Instrument::Type instrumentType) {
  if (getSegment()->type != Segment::Type::Continue) return std::nullopt;

  auto choices = retrospective->getChoices();
  const auto it = std::find_if(choices.begin(), choices.end(), [&](const SegmentChoice *choice) {
    return choice->instrumentType == instrumentType;
  });

  if (it != choices.end()) {
    return *it;
  }

  spdlog::debug("[seg-{}] Could not get previous choice for instrumentType={}", segmentId,
               Instrument::toString(instrumentType));
  return std::nullopt;
}


std::optional<const SegmentChoice *>
Fabricator::getChoiceIfContinued(const Instrument::Type instrumentType, const Instrument::Mode instrumentMode) {
  if (getSegment()->type != Segment::Type::Continue) return std::nullopt;

  auto choices = retrospective->getChoices();
  const auto it = std::find_if(choices.begin(), choices.end(), [&](const SegmentChoice *choice) {
    return choice->instrumentType == instrumentType && choice->instrumentMode == instrumentMode;
  });

  if (it != choices.end()) {
    return *it;
  }

  spdlog::debug("[seg-{}] Could not get previous choice for instrumentType={}", segmentId,
               Instrument::toString(instrumentType));
  return std::nullopt;
}


std::set<const SegmentChoice *> Fabricator::getChoicesIfContinued(const Program::Type programType) {
  if (getSegment()->type != Segment::Type::Continue) return {};

  std::set<const SegmentChoice *> filteredChoices;

  for (auto choice : retrospective->getChoices())
    if (choice->programType == programType)
      filteredChoices.emplace(choice);

  if (filteredChoices.empty()) {
    spdlog::debug("[seg-{}] Could not get previous choice for programType={}", segmentId,
                 Program::toString(programType));
  }

  return filteredChoices;
}


Chord Fabricator::getKeyForChoice(const SegmentChoice *choice) {
  const std::optional<const Program *> program = getProgram(choice);
  if (!choice->programSequenceBindingId.empty()) {
    const auto sequence = getSequence(choice);
    if (sequence.has_value() && !sequence.value()->key.empty())
      return Chord::of(sequence.value()->key);
  }

  if (!program.has_value()) {
    throw FabricationException("Cannot get key for nonexistent choice!");
  }

  return Chord::of(program.value()->key);
}

std::optional<const ProgramSequence *> Fabricator::getProgramSequence(const SegmentChoice *choice) {
  if (!choice->programSequenceId.empty())
    return sourceMaterial->getProgramSequence(choice->programSequenceId);
  if (choice->programSequenceBindingId.empty()) return std::nullopt;
  const auto psb = sourceMaterial->getProgramSequenceBinding(choice->programSequenceBindingId);
  if (!psb.has_value()) return std::nullopt;
  return sourceMaterial->getProgramSequence(psb.value()->programSequenceId);
}


std::optional<const SegmentChoice *> Fabricator::getMacroChoiceOfPreviousSegment() {
  if (!macroChoiceOfPreviousSegment.has_value())
    macroChoiceOfPreviousSegment = retrospective->getPreviousChoiceOfType(Program::Type::Macro);
  return macroChoiceOfPreviousSegment;
}

std::optional<const SegmentChoice *> Fabricator::getPreviousMainChoice() {
  if (!mainChoiceOfPreviousSegment.has_value())
    mainChoiceOfPreviousSegment = retrospective->getPreviousChoiceOfType(Program::Type::Main);
  return mainChoiceOfPreviousSegment;
}


ProgramConfig Fabricator::getCurrentMainProgramConfig() {
  const auto currentMainChoice = getCurrentMainChoice();
  if (!currentMainChoice.has_value()) {
    throw FabricationException("No current main choice!");
  }

  const auto program = sourceMaterial->getProgram(currentMainChoice.value()->programId);
  if (!program.has_value()) {
    throw FabricationException("Failed to retrieve current main program config!");
  }

  return ProgramConfig(program.value());
}


std::optional<const ProgramSequence *> Fabricator::getCurrentMainSequence() {
  const auto mc = getCurrentMainChoice();
  if (!mc.has_value()) return std::nullopt;
  return getProgramSequence(mc.value());
}


std::optional<const ProgramSequence *> Fabricator::getPreviousMainSequence() {
  const auto mc = getPreviousMainChoice();
  if (!mc.has_value()) return std::nullopt;
  return getProgramSequence(mc.value());
}


MemeIsometry Fabricator::getMemeIsometryOfNextSequenceInPreviousMacro() {
  const auto previousMacroChoice = getMacroChoiceOfPreviousSegment();
  if (!previousMacroChoice.has_value())
    return MemeIsometry::none();

  const auto previousSequenceBinding = sourceMaterial->getProgramSequenceBinding(
      previousMacroChoice.value()->programSequenceBindingId);
  if (!previousSequenceBinding.has_value())
    return MemeIsometry::none();

  const auto nextSequenceBinding = sourceMaterial->getBindingsAtOffsetOfProgram(previousMacroChoice.value()->programId,
                                                                          previousSequenceBinding.value()->offset + 1,
                                                                          true);

  std::set<std::string> memes;
  const auto programMemes = sourceMaterial->getMemesOfProgram(previousMacroChoice.value()->programId);
  for (const auto &meme: programMemes) {
    memes.emplace(meme->name);
  }

  for (const auto &binding: nextSequenceBinding) {
    auto sequenceBindingMemes = sourceMaterial->getMemesOfSequenceBinding(binding->id);
    for (const auto &meme: sequenceBindingMemes) {
      memes.emplace(meme->name);
    }
  }

  return MemeIsometry::of(templateConfig.memeTaxonomy, memes);
}


MemeIsometry Fabricator::getMemeIsometryOfSegment() {
  return MemeIsometry::of(templateConfig.memeTaxonomy, SegmentMeme::getNames(getSegmentMemes()));
}


int Fabricator::getNextSequenceBindingOffset(const SegmentChoice *choice) {
  if (choice->programSequenceBindingId.empty()) return 0;

  const auto sequenceBinding = sourceMaterial->getProgramSequenceBinding(choice->programSequenceBindingId);
  const int sequenceBindingOffset = getSequenceBindingOffsetForChoice(choice);
  int offset = -1;
  if (!sequenceBinding.has_value()) return 0;
  auto availableOffsets = sourceMaterial->getAvailableOffsets(sequenceBinding.value());
  for (const int availableOffset: availableOffsets)
    if (0 < availableOffset - sequenceBindingOffset)
      if (offset == -1 || 0 > availableOffset - offset) offset = availableOffset;

  // if none found, loop back around to zero
  return offset != -1 ? offset : 0;
}


std::vector<std::string> Fabricator::getNotes(const SegmentChordVoicing *voicing) {
  return CsvUtils::split(voicing->notes);
}


std::set<const SegmentChoiceArrangementPick *> Fabricator::getPicks() {
  return store->readAllSegmentChoiceArrangementPicks(segmentId);
}


std::vector<const SegmentChoiceArrangementPick *> Fabricator::getPicks(const SegmentChoice *choice) {
  if (picksForChoice.find(choice->id) == picksForChoice.end()) {
    std::vector<UUID> arrangementIds;
    const auto arrangements = getArrangements();
    for (const auto &arrangement: arrangements) {
      if (arrangement->segmentChoiceId == choice->id) {
        arrangementIds.push_back(arrangement->id);
      }
    }
    std::vector<const SegmentChoiceArrangementPick *> picks;
    const auto allPicks = getPicks();
    for (const auto &pick: allPicks) {
      if (std::find(arrangementIds.begin(), arrangementIds.end(), pick->segmentChoiceArrangementId) !=
          arrangementIds.end()) {
        picks.push_back(pick);
      }
    }
    // Sort the picks by startAtSegmentMicros
    std::sort(picks.begin(), picks.end(),
              [](const SegmentChoiceArrangementPick *a, const SegmentChoiceArrangementPick *b) {
                return a->startAtSegmentMicros < b->startAtSegmentMicros;
              });
    picksForChoice.emplace(choice->id, picks);
  }
  return picksForChoice[choice->id];
}


std::optional<const InstrumentAudio *> Fabricator::getPreferredAudio(const std::string &parentIdent, const std::string &ident) {
  const std::string cacheKey = computeCacheKeyForPreferredAudio(parentIdent, ident);

  if (preferredAudios.find(cacheKey) != preferredAudios.end()) {
    return {preferredAudios.at(cacheKey)};
  }

  return std::nullopt;
}


std::optional<const Program *> Fabricator::getProgram(const SegmentChoice *choice) {
  return sourceMaterial->getProgram(choice->programId);
}


ProgramConfig Fabricator::getProgramConfig(const Program *program) {
  return {program};
}


std::vector<const ProgramSequenceChord *> Fabricator::getProgramSequenceChords(const ProgramSequence *programSequence) {
  if (completeChordsForProgramSequence.find(programSequence->id) == completeChordsForProgramSequence.end()) {
    std::map<float, const ProgramSequenceChord *> chordForPosition;
    std::map<float, int> validVoicingsForPosition;
    const auto chords = sourceMaterial->getChordsOfSequence(programSequence);
    for (const ProgramSequenceChord *chord: chords) {
      int validVoicings = 0;
      auto voicings = sourceMaterial->getVoicingsOfChord(chord);
      for (const auto &voicing: voicings) {
        validVoicings += static_cast<int>(CsvUtils::split(voicing->notes).size());
      }
      if (validVoicingsForPosition.find(chord->position) == validVoicingsForPosition.end() ||
          validVoicingsForPosition[chord->position] < validVoicings) {
        validVoicingsForPosition[chord->position] = validVoicings;
        chordForPosition.emplace(chord->position, chord);
      }
    }
    std::vector<const ProgramSequenceChord *> sortedChords;
    for (const auto &[position, chord]: chordForPosition) {
      sortedChords.emplace_back(chord);
    }
    std::sort(sortedChords.begin(), sortedChords.end(),
              [](const ProgramSequenceChord *a, const ProgramSequenceChord *b) {
                return a->position < b->position;
              });
    completeChordsForProgramSequence[programSequence->id] = sortedChords;
  }

  return completeChordsForProgramSequence[programSequence->id];
}


NoteRange Fabricator::getProgramRange(const UUID &programId, const Instrument::Type instrumentType) {
  const std::string cacheKey = programId + "__" + std::to_string(instrumentType);

  if (rangeForChoice.find(cacheKey) == rangeForChoice.end()) {
    rangeForChoice[cacheKey] = computeProgramRange(programId, instrumentType);
  }

  return rangeForChoice[cacheKey];
}


int Fabricator::getProgramRangeShiftOctaves(const Instrument::Type instrumentType, NoteRange *sourceRange, NoteRange *targetRange) {
  const std::string cacheKey =
      std::to_string(instrumentType) + "__" + sourceRange->toString(Natural) +
      "__" + targetRange->toString(Natural);

  if (rangeShiftOctave.find(cacheKey) == rangeShiftOctave.end()) {
    switch (instrumentType) {
      case Instrument::Type::Bass:
        rangeShiftOctave[cacheKey] = computeLowestOptimalRangeShiftOctaves(*sourceRange, *targetRange);
        break;
      default:
      case Instrument::Type::Drum:
        return 0;
      case Instrument::Type::Pad:
      case Instrument::Type::Stab:
      case Instrument::Type::Sticky:
      case Instrument::Type::Stripe:
        rangeShiftOctave[cacheKey] = NoteRange::computeMedianOptimalRangeShiftOctaves(sourceRange, targetRange);
        break;
    }
  }

  return rangeShiftOctave[cacheKey];
}


int Fabricator::getProgramTargetShift(const Instrument::Type instrumentType, const Chord *fromChord, const Chord *toChord) {
  if (!fromChord->has_value()) return 0;
  const std::string cacheKey =
      std::to_string(static_cast<int>(instrumentType)) + "__" + fromChord->toString() + "__" + toChord->toString();
  if (targetShift.find(cacheKey) == targetShift.end()) {
    if (instrumentType == Instrument::Type::Bass) {
      targetShift[cacheKey] = Step::delta(fromChord->root, toChord->slashRoot.pitchClass.value_or(toChord->root));
    } else {
      targetShift[cacheKey] = Step::delta(fromChord->root, toChord->root);
    }
  }

  return targetShift[cacheKey];
}


Program::Type Fabricator::getProgramType(const ProgramVoice *voice) {
  const auto programOpt = sourceMaterial->getProgram(voice->programId);
  if (!programOpt.has_value()) {
    throw FabricationException("Could not get program!");
  }
  return programOpt.value()->type;
}


Instrument::Type Fabricator::getProgramVoiceType(const ProgramSequenceChordVoicing *voicing) {
  const auto voice = sourceMaterial->getProgramVoice(voicing->programVoiceId);
  if (!voice.has_value()) throw FabricationException("Could not get voice!");
  return voice.value()->type;
}


NoteRange Fabricator::getProgramVoicingNoteRange(const Instrument::Type instrumentType) {
  if (voicingNoteRange.find(instrumentType) == voicingNoteRange.end()) {
    std::vector<std::string> notes;
    const auto voicings = getChordVoicings();
    for (const auto voicing: voicings) {
      if (SegmentUtils::containsAnyValidNotes(voicing) && voicing->type == instrumentType) {
        auto voicingNotes = getNotes(voicing);
        notes.insert(notes.end(), voicingNotes.begin(), voicingNotes.end());
      }
    }
    voicingNoteRange[instrumentType] = NoteRange::ofStrings(notes);
  }

  return voicingNoteRange[instrumentType];
}


std::optional<const ProgramSequence *> Fabricator::getRandomlySelectedSequence(const Program *program) {
  std::vector<const ProgramSequence*> sequences;
  for (const auto sequence: sourceMaterial->getProgramSequences()) {
    if (sequence->programId == program->id) {
      sequences.emplace_back(sequence);
    }
  }
  if (sequences.empty())
    return std::nullopt;
  return {sequences[MarbleBag::quickPick(static_cast<int>(sequences.size()))]};
}


std::optional<const ProgramSequenceBinding *>
Fabricator::getRandomlySelectedSequenceBindingAtOffset(const Program *program, const int offset) {
  std::vector<const ProgramSequenceBinding *> sequenceBindings;
  for (const auto &sequenceBinding: sourceMaterial->getBindingsAtOffsetOfProgram(program, offset, true)) {
    sequenceBindings.emplace_back(sequenceBinding);
  }
  if (sequenceBindings.empty())
    return std::nullopt;
  return {sequenceBindings[MarbleBag::quickPick(static_cast<int>(sequenceBindings.size()))]};
}


std::optional<const ProgramSequencePattern *>
Fabricator::getRandomlySelectedPatternOfSequenceByVoiceAndType(const SegmentChoice *choice) {
  MarbleBag bag;
  const std::set<const ProgramSequencePattern *> patterns = sourceMaterial->getProgramSequencePatterns();

  for (const auto &pattern: patterns) {
    if (pattern->programSequenceId == choice->programSequenceId && pattern->programVoiceId == choice->programVoiceId) {
      bag.add(1, pattern->id);
    }
  }

  if (bag.empty()) {
    return std::nullopt;
  }

  return sourceMaterial->getProgramSequencePattern(bag.pick());
}


std::optional<Note> Fabricator::getRootNoteMidRange(const std::string &voicingNotes, const Chord *chord) {
  const std::string key = voicingNotes + "_" + chord->toString();
  const auto it = rootNotesByVoicingAndChord.find(key);
  if (it == rootNotesByVoicingAndChord.end()) {
    NoteRange noteRange = NoteRange::ofStrings(CsvUtils::split(voicingNotes));
    const std::optional<Note> note = noteRange.getNoteNearestMedian(chord->slashRoot.pitchClass.value_or(chord->root));
    if (!note.has_value())
      return std::nullopt;
    rootNotesByVoicingAndChord[key] = note;
    return note;
  }

  return it->second;
}


void Fabricator::putStickyBun(StickyBun bun) {
  SegmentMeta meta;
  meta.id = EntityUtils::computeUniqueId();
  meta.segmentId = getSegment()->id;
  meta.key = bun.computeMetaKey();
  meta.value = bun.serialize();

  store->put(meta);
}


std::optional<const StickyBun> Fabricator::getStickyBun(const UUID &eventId) {
  if (!templateConfig.stickyBunEnabled) return std::nullopt;

  const auto currentMeta = getSegmentMeta(StickyBun::computeMetaKey(eventId));
  if (currentMeta.has_value()) {
    try {
      const StickyBun bun = StickyBun::deserializeFrom(currentMeta.value()->value);
      return {bun};
    } catch (const std::exception &e) {
      addErrorMessage("Failed to deserialize current segment meta value StickyBun JSON for Event[" + eventId + "]: " +
                      e.what());
    }
  }

  const auto previousMeta = retrospective->getPreviousMeta(StickyBun::computeMetaKey(eventId));
  if (previousMeta.has_value()) {
    try {
      const StickyBun bun = StickyBun::deserializeFrom(previousMeta.value()->value);
      return {bun};
    } catch (const std::exception &e) {
      addErrorMessage("Failed to deserialize previous segment meta value StickyBun JSON for Event[" + eventId + "]: " +
                      e.what());
    }
  }

  const auto eventOpt = sourceMaterial->getProgramSequencePatternEvent(eventId);
  if (!eventOpt.has_value()) {
    addErrorMessage("Failed to get StickyBun for Event[" + eventId + "] because it does not exist");
    return std::nullopt;
  }

  const auto event = eventOpt.value();
  StickyBun bun(eventId, static_cast<int>(CsvUtils::split(event->tones).size()));
  try {
    putStickyBun(bun);
  } catch (const FabricationException &e) {
    addErrorMessage("Failed to put StickyBun for Event[" + eventId + "] because " + e.what());
  } catch (const std::exception &e) {
    addErrorMessage("Failed to serialize segment meta value StickyBun JSON for Event[" + eventId + "]: " + e.what());
  }
  return {bun};
}


long Fabricator::getSegmentMicrosAtPosition(const float tempo, const float position) {
  return static_cast<long>(getMicrosPerBeat(tempo) * position);
}


long Fabricator::getTotalSegmentMicros() {
  const auto segment = getSegment();
  if (!segment->durationMicros.has_value()) {
    throw FabricationFatalException("Segment has no duration");
  }
  return *segment->durationMicros;
}


const Segment *Fabricator::getSegment() {
  const auto seg = store->readSegment(segmentId);
  if (!seg.has_value())
    throw FabricationFatalException("No segment found");
  return seg.value();
}


std::vector<const SegmentChord *> Fabricator::getSegmentChords() {
  auto chords = store->readAllSegmentChords(segmentId);
  auto sortedChords = std::vector(chords.begin(), chords.end());
  std::sort(sortedChords.begin(), sortedChords.end(), [](const SegmentChord *a, const SegmentChord *b) {
    return a->position < b->position;
  });
  return sortedChords;
}


std::set<const SegmentChordVoicing *> Fabricator::getChordVoicings() {
  return store->readAllSegmentChordVoicings(segmentId);
}


std::set<const SegmentMeme *> Fabricator::getSegmentMemes() {
  return store->readAllSegmentMemes(segmentId);
}


std::optional<const ProgramSequence *> Fabricator::getSequence(const SegmentChoice *choice) {
  const std::optional<const Program *> program = getProgram(choice);
  if (!program.has_value()) return std::nullopt;
  if (!choice->programSequenceBindingId.empty()) {
    const auto sequenceBinding = sourceMaterial->getProgramSequenceBinding(choice->programSequenceBindingId);
    if (sequenceBinding.has_value())
      return {sourceMaterial->getProgramSequence(sequenceBinding.value()->programSequenceId).value()};
  }

  auto it = sequenceForChoice.find(choice);
  if (it == sequenceForChoice.end()) {
    const auto randomSequence = getRandomlySelectedSequence(program.value());
    if (randomSequence.has_value()) {
      sequenceForChoice[choice] = randomSequence.value();
    }
  }

  it = sequenceForChoice.find(choice);
  if (it == sequenceForChoice.end()) {
    return std::nullopt;
  }
  return {it->second};
}


int Fabricator::getSequenceBindingOffsetForChoice(const SegmentChoice *choice) {
  if (choice->programSequenceBindingId.empty()) return 0;
  const auto sequenceBindingOpt = sourceMaterial->getProgramSequenceBinding(choice->programSequenceBindingId);
  if (!sequenceBindingOpt.has_value()) {
    return 0;
  }
  return sequenceBindingOpt.value()->offset;
}


std::string Fabricator::getTrackName(const ProgramSequencePatternEvent *event) {
  const auto trackOpt = sourceMaterial->getTrackOfEvent(event);
  if (!trackOpt.has_value()) {
    return UNKNOWN_KEY;
  }
  return trackOpt.value()->name;
}


Segment::Type Fabricator::getType() {
  if (!type.has_value()) type = computeType();
  return type.value();
}


std::optional<const SegmentChordVoicing *>
Fabricator::chooseVoicing(const SegmentChord *chord, const Instrument::Type instrumentType) {
  const std::set<const SegmentChordVoicing *> voicings = store->readAllSegmentChordVoicings(segmentId);

  std::vector<const SegmentChordVoicing *> validVoicings;
  for (auto voicing: voicings) {
    if (SegmentUtils::containsAnyValidNotes(voicing) && voicing->type == instrumentType &&
        voicing->segmentChordId == chord->id) {
      validVoicings.push_back(voicing);
    }
  }

  if (validVoicings.empty()) {
    return std::nullopt;
  }

  // Use a random generator to select a voicing
  std::random_device rd;
  std::mt19937 gen(rd());
  std::uniform_int_distribution<std::vector<SegmentChordVoicing>::size_type> distrib(0, validVoicings.size() - 1);

  return validVoicings[distrib(gen)];
}


bool Fabricator::hasMoreSequenceBindingOffsets(const SegmentChoice *choice, const int N) {
  if (choice->programSequenceBindingId.empty()) return false;

  const auto sequenceBindingOpt = sourceMaterial->getProgramSequenceBinding(choice->programSequenceBindingId);

  if (!sequenceBindingOpt.has_value()) return false;
  const auto sequenceBinding = sequenceBindingOpt.value();

  const std::vector<int> avlOfs = sourceMaterial->getAvailableOffsets(sequenceBinding);

  // if we locate the target and still have two offsets remaining, result is true
  for (int i = 0; i < avlOfs.size(); i++)
    if (avlOfs[i] == sequenceBinding->offset && i < avlOfs.size() - N) return true;

  return false;
}


bool Fabricator::hasOneMoreSequenceBindingOffset(const SegmentChoice *choice) {
  return hasMoreSequenceBindingOffsets(choice, 1);
}


bool Fabricator::hasTwoMoreSequenceBindingOffsets(const SegmentChoice *choice) {
  return hasMoreSequenceBindingOffsets(choice, 2);
}


bool Fabricator::isContinuationOfMacroProgram() {
  return getType() == Segment::Type::Continue || getType() == Segment::Type::NextMain;
}


bool Fabricator::isDirectlyBound(const Program *program) {
  return boundProgramIds.find(program->id) != boundProgramIds.end();
}


bool Fabricator::isOneShot(const Instrument *instrument, const std::string &trackName) {
  return isOneShot(instrument) &&
         !getInstrumentConfig(instrument).oneShotObserveLengthOfEventsContains(trackName);
}


bool Fabricator::isOneShot(const Instrument *instrument) {
  return getInstrumentConfig(instrument).isOneShot;
}


bool Fabricator::isOneShotCutoffEnabled(const Instrument *instrument) {
  return getInstrumentConfig(instrument).isOneShotCutoffEnabled;
}


bool Fabricator::isDirectlyBound(const Instrument *instrument) {
  return boundInstrumentIds.find(instrument->id) != boundInstrumentIds.end();
}


bool Fabricator::isDirectlyBound(const InstrumentAudio *instrumentAudio) {
  return boundInstrumentIds.find(instrumentAudio->instrumentId) != boundInstrumentIds.end();
}


bool Fabricator::isInitialSegment() {
  return 0L == getSegment()->id;
}


std::optional<const SegmentChoice *> Fabricator::put(const SegmentChoice entity, const bool force) {
  const auto memeStack = MemeStack::from(templateConfig.memeTaxonomy, SegmentMeme::getNames(getSegmentMemes()));

  // For a SegmentChoice, add memes from program, program sequence binding, and instrument if present
  if (!isValidChoiceAndMemesHaveBeenAdded(entity, memeStack, force))
    return std::nullopt;

   return {store->put(entity)};
}


const SegmentChoiceArrangement* Fabricator::put(const SegmentChoiceArrangement entity) {
  return store->put(entity);
}


const SegmentChoiceArrangementPick* Fabricator::put(const SegmentChoiceArrangementPick entity) {
  return store->put(entity);
}


const SegmentChord* Fabricator::put(const SegmentChord entity) {
  return store->put(entity);
}


const SegmentChordVoicing* Fabricator::put(const SegmentChordVoicing entity) {
  return store->put(entity);
}


std::optional<const SegmentMeme  *> Fabricator::put(const SegmentMeme entity, const bool force) {
  const auto memeStack = MemeStack::from(templateConfig.memeTaxonomy, SegmentMeme::getNames(getSegmentMemes()));

  // Unless forced, don't put a duplicate of an existing meme
  if (!isValidMemeAddition(entity, memeStack, force))
    return std::nullopt;

 return store->put(entity);
}


const SegmentMessage * Fabricator::put(const SegmentMessage entity) {
  return store->put(entity);
}


const SegmentMeta * Fabricator::put(const SegmentMeta entity) {
  return store->put(entity);
}


void Fabricator::putPreferredAudio(
    const std::string &parentIdent,
    const std::string &ident,
    const InstrumentAudio *instrumentAudio) {
  std::string cacheKey = computeCacheKeyForPreferredAudio(parentIdent, ident);
  preferredAudios.emplace(cacheKey, instrumentAudio);
}


void Fabricator::putReport(const std::string &key, const std::map<std::string, std::string> &value) {
  addMessage(SegmentMessage::Type::Debug, "Report[" + key + "]:" + CsvUtils::from(value));
}


void Fabricator::putReport(const std::string &key, const std::string &value) {
  addMessage(SegmentMessage::Type::Debug, "Report[" + key + "]:" + value);
}


const Segment *Fabricator::updateSegment(Segment segment) {
  try {
    return store->updateSegment(segment);

  } catch (const FabricationException &e) {
    spdlog::error("Failed to update Segment", e.what());
    return nullptr;
  }
}


SegmentRetrospective *Fabricator::getRetrospective() {
  return retrospective;
}


ContentEntityStore *Fabricator::getSourceMaterial() {
  return sourceMaterial;
}


double Fabricator::getMicrosPerBeat(const float tempo) {
  if (0 == microsPerBeat)
    microsPerBeat = static_cast<double>(ValueUtils::MICROS_PER_MINUTE) / tempo;
  return microsPerBeat;
}


int Fabricator::getSecondMacroSequenceBindingOffset(const Program *macroProgram) {
  std::vector<int> offsets;
  const auto bindings = sourceMaterial->getSequenceBindingsOfProgram(macroProgram->id);

  for (const auto &binding: bindings) {
    offsets.push_back(binding->offset);
  }

  std::sort(offsets.begin(), offsets.end());

  return offsets.size() > 1 ? offsets[1] : offsets[0];
}


MemeTaxonomy Fabricator::getMemeTaxonomy() const {
  return templateConfig.memeTaxonomy;
}


double Fabricator::getTempo() {
  return getSegment()->tempo;
}


std::optional<const SegmentMeta *> Fabricator::getSegmentMeta(const std::string &key) const {
  const std::set<const SegmentMeta *> allMetas = store->readAllSegmentMetas(segmentId);
  for (const auto &meta: allMetas) {
    if (meta->key == key) {
      return meta;
    }
  }
  return std::nullopt;
}


std::optional<const SegmentChoice *> Fabricator::getChoiceOfType(Program::Type programType) const {
  auto allChoices = getChoices();
  const auto it = std::find_if(allChoices.begin(), allChoices.end(), [programType](const SegmentChoice *choice) {
    return choice->programType == programType;
  });

  if (it != allChoices.end()) {
    return *it;
  }

  return std::nullopt;
}


std::set<const SegmentChoice *> Fabricator::getBeatChoices() const {
  const std::set<const SegmentChoice *> allChoices = getChoices();
  std::set<const SegmentChoice *> beatChoices;

  for (const auto &choice: allChoices) {
    if (choice->programType == Program::Type::Beat) {
      beatChoices.insert(choice);
    }
  }

  return beatChoices;
}


int Fabricator::computeLowestOptimalRangeShiftOctaves(const NoteRange &sourceRange, const NoteRange &targetRange) {
  int shiftOctave = 0;    // search for optimal value
  int baselineDelta = 100;// optimal is the lowest possible integer zero or above
  for (int o = 10; o >= -10; o--) {
    if (!targetRange.low.has_value()) {
      throw FabricationException("can't get low end of target range");
    }
    const int d = targetRange.low.value().delta(
        sourceRange.low.value_or(Note::atonal()).shiftOctave(o));
    if (0 <= d && d < baselineDelta) {
      baselineDelta = d;
      shiftOctave = o;
    }
  }
  return shiftOctave;
}


std::string Fabricator::computeShipKey(const Chain *chain, const Segment *segment) {
  const std::string chainName = chain->shipKey.empty() ? "chain" + NAME_SEPARATOR + chain->id
                                                       : chain->shipKey;
  const std::string segmentName = std::to_string(segment->beginAtChainMicros);
  return chainName + NAME_SEPARATOR + segmentName;
}


void Fabricator::ensureShipKey() {
  if (getSegment()->storageKey.empty()) {
    const auto originalSegment = getSegment();
    const auto chainOpt = store->readChain();
    if (!chainOpt.has_value()) {
      throw FabricationException("No chain");
    }
    Segment updatedSegment = *originalSegment;
    updatedSegment.storageKey = computeShipKey(chainOpt.value(), getSegment());
    updateSegment(updatedSegment);

    spdlog::debug("[seg-{}] Generated ship key {}", segmentId, getSegment()->storageKey);
  }
}


Segment::Type Fabricator::computeType() {
  if (isInitialSegment())
    return Segment::Type::Initial;

  // previous main choice having at least one more pattern?
  const std::optional<const SegmentChoice *> previousMainChoice = getPreviousMainChoice();

  if (previousMainChoice.has_value() && hasOneMoreSequenceBindingOffset(previousMainChoice.value()) && getTemplateConfig().mainProgramLengthMaxDelta > getPreviousSegmentDelta())
    return Segment::Type::Continue;

  // previous macro choice having at least two more patterns?
  const std::optional<const SegmentChoice *> previousMacroChoice = getMacroChoiceOfPreviousSegment();

  if (previousMacroChoice.has_value() && hasTwoMoreSequenceBindingOffsets(previousMacroChoice.value()))
    return Segment::Type::NextMain;

  return Segment::Type::NextMacro;
}


int Fabricator::getPreviousSegmentDelta() const {
  const auto previousSegment = retrospective->getPreviousSegment();
  return previousSegment.has_value() ? previousSegment.value()->delta : 0;
}


std::map<std::string, const InstrumentAudio *> Fabricator::computePreferredInstrumentAudio() {
  std::map<std::string, const InstrumentAudio *> audios = {};

  const auto picks = retrospective->getPicks();
  for (const auto &pick: picks) {
    auto audioOpt = sourceMaterial->getInstrumentAudio(pick->instrumentAudioId);
    if (audioOpt.has_value()) {
      audios.emplace(computeCacheKeyForVoiceTrack(pick), audioOpt.value());
    }
  }

  return audios;
}


bool Fabricator::isValidChoiceAndMemesHaveBeenAdded(const SegmentChoice &choice, const MemeStack &memeStack, const bool force) {
  std::set<std::string> names;

  if (!choice.programId.empty())
    for (const auto meme: sourceMaterial->getMemesOfProgram(choice.programId))
      names.emplace(StringUtils::toMeme(meme->name));

  if (!choice.programSequenceBindingId.empty())
    for (const auto meme: sourceMaterial->getMemesOfSequenceBinding(choice.programSequenceBindingId))
      names.emplace(StringUtils::toMeme(meme->name));

  if (!choice.instrumentId.empty())
    for (const auto meme: sourceMaterial->getMemesOfInstrument(choice.instrumentId))
      names.emplace(StringUtils::toMeme(meme->name));

  if (!force && !memeStack.isAllowed(names)) {
    addMessage(SegmentMessage::Type::Error,
               "Refused to add Choice[" + SegmentUtils::describe(choice) + "] because adding Memes[" +
                   CsvUtils::join(std::vector(names.begin(), names.end())) + "] to MemeStack[" +
                   memeStack.getConstellation() + "] would result in an invalid meme stack theorem!");
    return false;
  }

  spdlog::debug("Adding Choice[{}] with Memes[{}]", SegmentUtils::describe(choice),
                CsvUtils::join(std::vector(names.begin(), names.end())));

  for (auto &name: names) {
    SegmentMeme segmentMeme;
    segmentMeme.id = EntityUtils::computeUniqueId();
    segmentMeme.segmentId = getSegment()->id;
    segmentMeme.name = name;
    put(segmentMeme, true);
  }

  return true;
}


bool Fabricator::isValidMemeAddition(const SegmentMeme &meme, const MemeStack &memeStack, const bool force) {
  if (force) return true;
  if (!memeStack.isAllowed({meme.name})) return false;
  return std::all_of(getSegmentMemes().begin(), getSegmentMemes().end(), [&meme](const SegmentMeme *m) {
    return m->name != meme.name;
  });
}


std::string Fabricator::computeCacheKeyForPreferredAudio(const std::string &parentIdent, const std::string &ident) {
  return "voice-" + parentIdent + "_note-" + ident;
}

std::string Fabricator::computeCacheKeyForVoiceTrack(const SegmentChoiceArrangementPick *pick) {
  std::string cacheKey = UNKNOWN_KEY;
  const auto event = getSourceMaterial()->getProgramSequencePatternEvent(pick->programSequencePatternEventId);
  if (event.has_value()) {
    const auto trackOpt = getSourceMaterial()->getTrackOfEvent(event.value());
    if (trackOpt.has_value()) {
      cacheKey = trackOpt.value()->programVoiceId;
    }
  }

  return "voice-" + cacheKey + "_track-" + pick->event;
}

NoteRange Fabricator::computeProgramRange(const UUID &programId, const Instrument::Type instrumentType) const {
  std::vector<std::string> notes;
  const auto events = sourceMaterial->getSequencePatternEventsOfProgram(programId);
  for (const auto &event: events) {
    auto voiceOpt = sourceMaterial->getVoiceOfEvent(event);
    if (voiceOpt.has_value() && voiceOpt.value()->type == instrumentType &&
        Note::of(event->tones).pitchClass != Atonal) {
      auto tones = CsvUtils::split(event->tones);
      notes.insert(notes.end(), tones.begin(), tones.end());
    }
  }
  return NoteRange::ofStrings(notes);
}

int Fabricator::getSegmentId(const SegmentChoice *segmentChoice) {
  return segmentChoice->segmentId;
}

int Fabricator::getSegmentId(const SegmentChoiceArrangement *segmentChoiceArrangement) {
  return segmentChoiceArrangement->segmentId;
}

int Fabricator::getSegmentId(const SegmentChoiceArrangementPick *segmentChoiceArrangementPick) {
  return segmentChoiceArrangementPick->segmentId;
}

int Fabricator::getSegmentId(const SegmentChord *segmentChord) {
  return segmentChord->segmentId;
}

int Fabricator::getSegmentId(const SegmentChordVoicing *segmentChordVoicing) {
  return segmentChordVoicing->segmentId;
}

int Fabricator::getSegmentId(const SegmentMeme *segmentMeme) {
  return segmentMeme->segmentId;
}

int Fabricator::getSegmentId(const SegmentMessage *segmentMessage) {
  return segmentMessage->segmentId;
}

int Fabricator::getSegmentId(const SegmentMeta *segmentMeta) {
  return segmentMeta->segmentId;
}

std::string Fabricator::toString(const ControlMode controlMode) {
  switch (controlMode) {
    default:
    case ControlMode::Auto:
      return "Auto";
    case ControlMode::Macro:
      return "Macro";
    case ControlMode::Taxonomy:
      return "Taxonomy";
  }
}
