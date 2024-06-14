// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <utility>
#include <vector>
#include <algorithm>

#include "spdlog/spdlog.h"

#include "xjmusic/fabricator/FabricationFatalException.h"
#include "xjmusic/fabricator/FabricationException.h"
#include "xjmusic/fabricator/Fabricator.h"
#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/util/CsvUtils.h"
#include "xjmusic/fabricator/SegmentUtils.h"

using namespace XJ;

Fabricator::Fabricator(
#pragma clang diagnostic push
#pragma ide diagnostic ignored "UnusedParameter"
    FabricatorFactory &fabricatorFactory,
#pragma clang diagnostic pop
    SegmentEntityStore &store,
    ContentEntityStore &sourceMaterial,
    int segmentId,
    double outputFrameRate,
    int outputChannels,
    std::optional<Segment::Type> overrideSegmentType
) :
    retrospective(fabricatorFactory.loadRetrospective(segmentId)),
    sourceMaterial(sourceMaterial),
    store(store) {
  this->outputFrameRate = outputFrameRate;
  this->outputChannels = outputChannels;

  // keep elapsed time based on system nano time
  startAtSystemNanoTime = std::chrono::high_resolution_clock::now();

  // read the chain, configs, and bindings
  auto chainOpt = store.readChain();
  if (!chainOpt.has_value())
    throw FabricationFatalException("No chain found");

  chain = chainOpt.value();
  templateConfig = TemplateConfig(chain.templateConfig);
  templateBindings = sourceMaterial.getTemplateBindings();
  boundProgramIds = ChainUtils::targetIdsOfType(templateBindings, TemplateBinding::Type::Program);
  boundInstrumentIds = ChainUtils::targetIdsOfType(templateBindings, TemplateBinding::Type::Instrument);
  spdlog::debug("[segId={}] Chain {} configured with {} and bound to {} ",
                segmentId, chain.id, templateConfig.toString(), TemplateBinding::toPrettyCsv(templateBindings));

  // digest previous instrument audio
  preferredAudios = computePreferredInstrumentAudio();

  // the current segment on the workbench
  this->segmentId = segmentId;

  // Override the segment type by passing the fabricator a segment type on creation
  // live performance modulation https://github.com/xjmusic/xjmusic/issues/197
  if (overrideSegmentType.has_value()) {
    type = overrideSegmentType.value();
  }

  // final pre-flight check
  ensureShipKey();
}


void Fabricator::addMessage(SegmentMessage::Type messageType, std::string body) {
  try {
    SegmentMessage msg;
    msg.id = Entity::randomUUID();
    msg.segmentId = getSegment().id;
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
  store.deleteSegmentChoiceArrangementPick(segmentId, id);
}


std::set<SegmentChoiceArrangement> Fabricator::getArrangements() {
  return store.readAllSegmentChoiceArrangements(segmentId);
}


std::set<SegmentChoiceArrangement> Fabricator::getArrangements(const std::set<SegmentChoice> &choices) {
  std::vector<UUID> choiceIds;
  for (const auto &choice: choices) {
    choiceIds.push_back(choice.id);
  }

  std::set<SegmentChoiceArrangement> allArrangements = getArrangements();
  std::set<SegmentChoiceArrangement> filteredArrangements;

  for (const auto &arrangement: allArrangements) {
    if (std::find(choiceIds.begin(), choiceIds.end(), arrangement.segmentChoiceId) != choiceIds.end()) {
      filteredArrangements.emplace(arrangement);
    }
  }

  return filteredArrangements;
}


Chain Fabricator::getChain() {
  return chain;
}


TemplateConfig Fabricator::getTemplateConfig() {
  return templateConfig;
}


std::set<SegmentChoice> Fabricator::getChoices() {
  return store.readAllSegmentChoices(segmentId);
}


std::optional<SegmentChord> Fabricator::getChordAt(float position) {
  std::optional<SegmentChord> foundChord;
  float foundPosition = -1.0f; // Initialize with a negative value

  // We assume that these entities are in order of position ascending
  std::vector<SegmentChord> segmentChords = getSegmentChords();
  for (const auto &segmentChord: segmentChords) {
    // If it's a better match (or no match has yet been found) then use it
    if (foundPosition < 0 || (segmentChord.position > foundPosition && segmentChord.position <= position)) {
      foundPosition = segmentChord.position;
      foundChord = segmentChord;
    }
  }

  return foundChord;
}

std::optional<SegmentChoice> Fabricator::getCurrentMainChoice() {
  return getChoiceOfType(Program::Type::Main);
}


std::vector<SegmentChoice> Fabricator::getCurrentDetailChoices() {
  return getBeatChoices();
}


std::optional<SegmentChoice> Fabricator::getCurrentBeatChoice() {
  return getChoiceOfType(Program::Type::Beat);
}


std::set<Instrument::Type> Fabricator::getDistinctChordVoicingTypes() {
  if (distinctChordVoicingTypes == nullptr) {
    auto mainChoice = getCurrentMainChoice();
    if (!mainChoice.has_value()) return std::set<Instrument::Type>{};
    auto voicings = sourceMaterial.getSequenceChordVoicingsOfProgram(mainChoice.value().programId);
    distinctChordVoicingTypes = new std::set<Instrument::Type>();
    for (const ProgramSequenceChordVoicing *voicing: voicings) {
      try {
        distinctChordVoicingTypes->insert(getProgramVoiceType(voicing));
      } catch (FabricationException &e) {
        spdlog::warn(formatLog("Failed to get distinct chord voicing type! {}"), e.what());
      }
    }
  }

  return *distinctChordVoicingTypes;
}


long Fabricator::getElapsedMicros() {
  auto now = std::chrono::high_resolution_clock::now();
  auto elapsed = std::chrono::duration_cast<std::chrono::nanoseconds>(now - startAtSystemNanoTime);
  return elapsed.count() / ValueUtils::NANOS_PER_MICRO; // NOLINT(*-narrowing-conversions)
}


InstrumentConfig Fabricator::getInstrumentConfig(Instrument instrument) {
  auto [it, inserted] = instrumentConfigs.emplace(instrument.id, InstrumentConfig(instrument));
  return it->second;
}


std::optional<SegmentChoice> Fabricator::getChoiceIfContinued(ProgramVoice voice) {
  if (getSegment().type != Segment::Type::Continue) return std::nullopt;

  auto choices = retrospective.getChoices();
  auto it = std::find_if(choices.begin(), choices.end(), [&](const SegmentChoice &choice) {
    auto candidateVoice = sourceMaterial.getProgramVoice(choice.programVoiceId);
    return candidateVoice.has_value()
           && candidateVoice.value()->name == voice.name &&
           candidateVoice.value()->type == voice.type;
  });

  if (it != choices.end()) {
    return *it;
  } else {
    spdlog::warn(formatLog("Could not get previous voice instrumentId for voiceName={}"), voice.name);
    return std::nullopt;
  }
}


#include <optional>
#include <algorithm>

std::optional<SegmentChoice> Fabricator::getChoiceIfContinued(Instrument::Type instrumentType) {
  if (getSegment().type != Segment::Type::Continue) return std::nullopt;

  auto choices = retrospective.getChoices();
  auto it = std::find_if(choices.begin(), choices.end(), [&](const SegmentChoice &choice) {
    return choice.instrumentType == instrumentType;
  });

  if (it != choices.end()) {
    return *it;
  } else {
    spdlog::warn(formatLog("Could not get previous choice for instrumentType={}"), instrumentType);
    return std::nullopt;
  }
}


std::optional<SegmentChoice>
Fabricator::getChoiceIfContinued(Instrument::Type instrumentType, Instrument::Mode instrumentMode) {
  if (getSegment().type != Segment::Type::Continue) return std::nullopt;

  auto choices = retrospective.getChoices();
  auto it = std::find_if(choices.begin(), choices.end(), [&](const SegmentChoice &choice) {
    return choice.instrumentType == instrumentType && choice.instrumentMode == instrumentMode;
  });

  if (it != choices.end()) {
    return *it;
  } else {
    spdlog::warn(formatLog("Could not get previous choice for instrumentType={}"), instrumentType);
    return std::nullopt;
  }
}


std::vector<SegmentChoice> Fabricator::getChoicesIfContinued(Program::Type programType) {
  if (getSegment().type != Segment::Type::Continue) return {};

  auto choices = retrospective.getChoices();
  std::vector<SegmentChoice> filteredChoices;

  std::copy_if(choices.begin(), choices.end(), std::back_inserter(filteredChoices), [&](const SegmentChoice &choice) {
    return choice.programType == programType;
  });

  if (filteredChoices.empty()) {
    spdlog::warn(formatLog("Could not get previous choice for programType={}"), programType);
  }

  return filteredChoices;
}


std::string Fabricator::computeCacheKeyForVoiceTrack(const SegmentChoiceArrangementPick &pick) {
  std::string cacheKey;
  auto eventOpt = sourceMaterial.getProgramSequencePatternEvent(pick.programSequencePatternEventId);

  if (eventOpt.has_value()) {
    auto trackOpt = sourceMaterial.getTrackOfEvent(*eventOpt.value());
    if (trackOpt.has_value()) {
      auto voiceIdOpt = trackOpt.value()->programVoiceId;
      cacheKey = trackOpt.value()->programVoiceId;
    } else {
      cacheKey = UNKNOWN_KEY;
    }
  } else {
    cacheKey = UNKNOWN_KEY;
  }

  return "voice-" + cacheKey + "_track-" + pick.event;
}


Chord Fabricator::getKeyForChoice(const SegmentChoice &choice) {
  std::optional<Program> program = getProgram(choice);
  if (!choice.programSequenceBindingId.empty()) {
    auto sequence = getSequence(choice);
    if (sequence.has_value() && !sequence.value().key.empty())
      return Chord::of(sequence.value().key);
  }

  if (!program.has_value()) {
    throw FabricationException("Cannot get key for nonexistent choice!");
  }

  return Chord::of(program->key);
}

std::optional<const ProgramSequence *> Fabricator::getProgramSequence(const SegmentChoice &choice) {
  if (!choice.programSequenceId.empty())
    return sourceMaterial.getProgramSequence(choice.programSequenceId);
  if (choice.programSequenceBindingId.empty()) return std::nullopt;
  auto psb = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
  if (!psb.has_value()) return std::nullopt;
  return sourceMaterial.getProgramSequence(psb.value()->programSequenceId);
}


std::optional<SegmentChoice> Fabricator::getMacroChoiceOfPreviousSegment() {
  if (!macroChoiceOfPreviousSegment.has_value())
    macroChoiceOfPreviousSegment = retrospective.getPreviousChoiceOfType(Program::Type::Macro);
  return macroChoiceOfPreviousSegment;
}

std::optional<SegmentChoice> Fabricator::getPreviousMainChoice() {
  if (!mainChoiceOfPreviousSegment.has_value())
    mainChoiceOfPreviousSegment = retrospective.getPreviousChoiceOfType(Program::Type::Main);
  return mainChoiceOfPreviousSegment;
}


ProgramConfig Fabricator::getCurrentMainProgramConfig() {
  auto currentMainChoice = getCurrentMainChoice();
  if (!currentMainChoice.has_value()) {
    throw FabricationException("No current main choice!");
  }

  auto program = sourceMaterial.getProgram(currentMainChoice.value().programId);
  if (!program.has_value()) {
    throw FabricationException("Failed to retrieve current main program config!");
  }

  return {*program};
}


std::optional<const ProgramSequence *> Fabricator::getCurrentMainSequence() {
  auto mc = getCurrentMainChoice();
  if (!mc.has_value()) return std::nullopt;
  return getProgramSequence(mc.value());
}


std::optional<const ProgramSequence *> Fabricator::getPreviousMainSequence() {
  auto mc = getPreviousMainChoice();
  if (!mc.has_value()) return std::nullopt;
  return getProgramSequence(mc.value());
}


MemeIsometry Fabricator::getMemeIsometryOfNextSequenceInPreviousMacro() {
  auto previousMacroChoice = getMacroChoiceOfPreviousSegment();
  if (!previousMacroChoice.has_value())
    return MemeIsometry::none();

  auto previousSequenceBinding = sourceMaterial.getProgramSequenceBinding(
      previousMacroChoice.value().programSequenceBindingId);
  if (!previousSequenceBinding.has_value())
    return MemeIsometry::none();

  auto nextSequenceBinding = sourceMaterial.getBindingsAtOffsetOfProgram(previousMacroChoice.value().programId,
                                                                         previousSequenceBinding.value()->offset + 1,
                                                                         true);

  std::set<std::string> memes;
  auto programMemes = sourceMaterial.getMemesOfProgram(previousMacroChoice.value().programId);
  for (const auto& meme : programMemes) {
    memes.emplace(meme->name);
  }

  for (const auto& binding : nextSequenceBinding) {
    auto sequenceBindingMemes = sourceMaterial.getMemesOfSequenceBinding(binding->id);
    for (const auto& meme : sequenceBindingMemes) {
      memes.emplace(meme->name);
    }
  }

  return MemeIsometry::of(templateConfig.memeTaxonomy, memes);
}


MemeIsometry Fabricator::getMemeIsometryOfSegment() {
  return MemeIsometry::of(templateConfig.memeTaxonomy, SegmentMeme::getNames(getSegmentMemes()));
}


int Fabricator::getNextSequenceBindingOffset(SegmentChoice choice) {
  if (ValueUtils.isEmpty(choice.programSequenceBindingId)) return 0;

  var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
  int sequenceBindingOffset = getSequenceBindingOffsetForChoice(choice);
  int offset = null;
  if (sequenceBinding.isEmpty()) return 0;
  for (int availableOffset: sourceMaterial.getAvailableOffsets(sequenceBinding.get()))
    if (0 < availableOffset.compareTo(sequenceBindingOffset))
      if (Objects.isNull(offset) || 0 > availableOffset.compareTo(offset)) offset = availableOffset;

  // if none found, loop back around to zero
  return Objects.nonNull(offset) ? offset : 0;

}


std::vector<std::string> Fabricator::getNotes(SegmentChordVoicing voicing) {
  return new ArrayList<>(CsvUtils.split(voicing.notes));
}


std::vector<SegmentChoiceArrangementPick> Fabricator::getPicks() {
  return store.readAll(segmentId, SegmentChoiceArrangementPick.
  class);
}


std::vector<SegmentChoiceArrangementPick> Fabricator::getPicks(SegmentChoice choice) {
  if (!picksForChoice.containsKey(choice.id)) {
    var arrangementIds = getArrangements().stream().filter(a->a.getSegmentChoiceId().equals(choice.id)).map(
        SegmentChoiceArrangement::getId).toList();
    picksForChoice.put(choice.id, getPicks().stream()
        .filter(p->arrangementIds.contains(p.getSegmentChoiceArrangementId()))
        .sorted(Comparator.comparing(SegmentChoiceArrangementPick::getStartAtSegmentMicros)).toList());
  }
  return picksForChoice.get(choice.id);
}


std::optional<InstrumentAudio> Fabricator::getPreferredAudio(const std::string &parentIdent, const std::string &ident) {
  std::string cacheKey = computeCacheKeyForPreferredAudio(parentIdent, ident);

  if (preferredAudios.find(cacheKey) != preferredAudios.end()) {
    return {preferredAudios.at(cacheKey)};
  }

  return std::nullopt;
}


std::optional<Program> Fabricator::getProgram(SegmentChoice choice) {
  return sourceMaterial.getProgram(choice.programId);
}


ProgramConfig Fabricator::getProgramConfig(Program program) {
  try {
    return new ProgramConfig(program);
  } catch (ValueException e) {
    throw new FabricationException(e);
  }
}


std::vector<ProgramSequenceChord> Fabricator::getProgramSequenceChords(ProgramSequence programSequence) {
  if (!completeChordsForProgramSequence.containsKey(programSequence.id)) {
    Map<float, ProgramSequenceChord> chordForPosition = new HashMap<>();
    Map<float, int> validVoicingsForPosition = new HashMap<>();
    for (ProgramSequenceChord chord: sourceMaterial.getChordsOfSequence(programSequence)) {
      int validVoicings = sourceMaterial.getVoicingsOfChord(chord).stream().map(
          V->CsvUtils.split(V.notes).size()).reduce(0, int
      ::sum);
      if (!validVoicingsForPosition.containsKey(chord.position) ||
          validVoicingsForPosition.get(chord.position) < validVoicings) {
        validVoicingsForPosition.put(chord.position, validVoicings);
        chordForPosition.put(chord.position, chord);
      }
    }
    completeChordsForProgramSequence.put(programSequence.id, chordForPosition.values());
  }

  return completeChordsForProgramSequence.get(programSequence.id);
}


NoteRange Fabricator::getProgramRange(UUID programId, Instrument::Type instrumentType) {
  var cacheKey = std::string.format("%s__%s", programId, instrumentType);

  if (!rangeForChoice.containsKey(cacheKey)) {
    rangeForChoice.put(cacheKey, computeProgramRange(programId, instrumentType));
  }

  return rangeForChoice.get(cacheKey);
}

NoteRange Fabricator::computeProgramRange(UUID programId, Instrument::Type instrumentType) {
  return NoteRange.ofStrings(
      sourceMaterial.getSequencePatternEventsOfProgram(programId).stream()
          .filter(event->sourceMaterial.getVoiceOfEvent(event).map(
              voice->Objects.equals(voice.type, instrumentType)).orElse(false)
                  && !Objects.equals(Note.of(event.getTones()).getPitchClass(), PitchClass.None))
          .flatMap(programSequencePatternEvent->CsvUtils.split(programSequencePatternEvent.getTones()).stream())
          .collect(Collectors.toList()));
}


int Fabricator::getProgramRangeShiftOctaves(Instrument::Type type, NoteRange sourceRange, NoteRange targetRange) {
  var cacheKey = std::string.format("%s__%s__%s", type, sourceRange.toString(Accidental.None),
                                    targetRange.toString(Accidental.None));

  if (!rangeShiftOctave.containsKey(cacheKey))
    switch (type) {
      case Bass->rangeShiftOctave.put(cacheKey, computeLowestOptimalRangeShiftOctaves(sourceRange, targetRange));
      case Drum->{
        return 0;
      }
      case Pad, Stab, Sticky, Stripe->
        rangeShiftOctave.put(cacheKey, NoteRange.computeMedianOptimalRangeShiftOctaves(sourceRange, targetRange));
    }

  return rangeShiftOctave.get(cacheKey);
}


int Fabricator::getProgramTargetShift(Instrument::Type instrumentType, Chord fromChord, Chord toChord) {
  if (!fromChord.has_value()) return 0;
  var cacheKey = std::string.format("%s__%s__%s", instrumentType, fromChord, toChord);
  if (!targetShift.containsKey(cacheKey)) {
    if (instrumentType.equals(Instrument::Type::Bass)) {
      targetShift.put(cacheKey, fromChord.getRoot().delta(toChord.getSlashRoot()));
    } else {
      targetShift.put(cacheKey, fromChord.getRoot().delta(toChord.getRoot()));
    }
  }

  return targetShift.get(cacheKey);
}


Program::Type Fabricator::getProgramType(ProgramVoice voice) {
  return sourceMaterial.getProgram(voice.programId).orElseThrow(()->
  new FabricationException("Could not get program!")).type;
}


Instrument::Type Fabricator::getProgramVoiceType(const ProgramSequenceChordVoicing *voicing) {
  auto voice = sourceMaterial.getProgramVoice(voicing->programVoiceId);
  if (!voice.has_value()) throw FabricationException("Could not get voice!");
  return voice.value()->type;
}


NoteRange Fabricator::getProgramVoicingNoteRange(Instrument::Type type) {
  if (!voicingNoteRange.containsKey(type)) {
    voicingNoteRange.put(type, NoteRange.ofStrings(
        getChordVoicings().stream().filter(SegmentUtils::containsAnyValidNotes).filter(
            segmentChordVoicing->Objects.equals(segmentChordVoicing.type, type)).flatMap(
            segmentChordVoicing->getNotes(segmentChordVoicing).stream()).collect(Collectors.toList())));
  }

  return voicingNoteRange.get(type);
}


std::optional<ProgramSequence> Fabricator::getRandomlySelectedSequence(Program program) {
  var bag = MarbleBag.empty();
  sourceMaterial.getProgramSequences().stream().filter(s->Objects.equals(s.programId, program.id)).forEach(
      sequence->bag.add(1, sequence.id));
  if (bag.isEmpty()) return std::nullopt;
  return sourceMaterial.getProgramSequence(bag.pick());
}


std::optional<ProgramSequenceBinding>
Fabricator::getRandomlySelectedSequenceBindingAtOffset(Program program, int offset) {
  var bag = MarbleBag.empty();
  for (ProgramSequenceBinding sequenceBinding: sourceMaterial.getBindingsAtOffsetOfProgram(program, offset, true))
    bag.add(1, sequenceBinding.id);
  if (bag.isEmpty()) return std::nullopt;
  return sourceMaterial.getProgramSequenceBinding(bag.pick());
}


std::optional<ProgramSequencePattern>
Fabricator::getRandomlySelectedPatternOfSequenceByVoiceAndType(SegmentChoice choice) {
  var bag = MarbleBag.empty();
  sourceMaterial.getProgramSequencePatterns().stream().filter(
      pattern->Objects.equals(pattern.programSequenceId, choice.programSequenceId)).filter(
      pattern->Objects.equals(pattern.programVoiceId, choice.programVoiceId)).forEach(pattern->bag.add(1, pattern.id));
  if (bag.isEmpty()) return std::nullopt;
  return sourceMaterial.getProgramSequencePattern(bag.pick());
}


std::optional<Note> Fabricator::getRootNoteMidRange(std::string voicingNotes, Chord chord) {
  return rootNotesByVoicingAndChord.computeIfAbsent(std::string.format("%s_%s", voicingNotes, chord.name),
                                                    (std::string
  key) -> NoteRange.ofStrings(CsvUtils.split(voicingNotes)).getNoteNearestMedian(chord.getSlashRoot()));
}


void Fabricator::putStickyBun(StickyBun bun) {
  store.put(new SegmentMeta()
      .id(randomUUID())
      .segmentId(getSegment().id)
      .key(bun.computeMetaKey())
      .value(jsonProvider.getMapper().writeValueAsString(bun)));
}


std::optional<StickyBun> Fabricator::getStickyBun(UUID eventId) {
  if (!templateConfig.isStickyBunEnabled()) return std::nullopt;
  //
  var currentMeta = getSegmentMeta(StickyBun.computeMetaKey(eventId));
  if (currentMeta.has_value()) {
    try {
      return std::optional.of(jsonProvider.getMapper().readValue(currentMeta.get().getValue(), StickyBun.
      class));
    } catch (JsonProcessingException e) {
      addErrorMessage(
          std::string.format("Failed to deserialize current segment meta value StickyBun JSON for Event[%s]", eventId));
    }
  }
  //
  var previousMeta = retrospective.getPreviousMeta(StickyBun.computeMetaKey(eventId));
  if (previousMeta.has_value()) {
    try {
      return std::optional.of(jsonProvider.getMapper().readValue(previousMeta.get().getValue(), StickyBun.
      class));
    } catch (JsonProcessingException e) {
      addErrorMessage(
          std::string.format("Failed to deserialize previous segment meta value StickyBun JSON for Event[%s]",
                             eventId));
    }
  }
  var event = sourceMaterial.getProgramSequencePatternEvent(eventId);
  if (event.isEmpty()) {
    addErrorMessage(std::string.format("Failed to get StickyBun for Event[%s] because it does not exist", eventId));
    return std::nullopt;
  }
  var bun = new StickyBun(eventId, CsvUtils.split(event.get().getTones()).size());
  try {
    putStickyBun(bun);
  } catch (FabricationException e) {
    addErrorMessage(std::string.format("Failed to put StickyBun for Event[%s] because %s", eventId, e.getMessage()));
  } catch (JsonProcessingException e) {
    addErrorMessage(std::string.format("Failed to serialize segment meta value StickyBun JSON for Event[%s]", eventId));
  }
  return std::optional.of(bun);
}


long Fabricator::getSegmentMicrosAtPosition(double tempo, double position) {
  return (long) (getMicrosPerBeat(tempo) * position);
}


long Fabricator::getTotalSegmentMicros() {
  return Objects.requireNonNull(getSegment().durationMicros);
}


Segment Fabricator::getSegment() {
  auto seg = store.readSegment(segmentId);
  if (!seg.has_value())
    throw FabricationFatalException("No segment found");
  return seg.value();
}


std::vector<SegmentChord> Fabricator::getSegmentChords() {
  auto chords = store.readAllSegmentChords(segmentId);
  std::vector<SegmentChord> sortedChords = std::vector<SegmentChord>(chords.begin(), chords.end());
  std::sort(sortedChords.begin(), sortedChords.end(), [](const SegmentChord &a, const SegmentChord &b) {
    return a.position < b.position;
  });
  return sortedChords;
}


std::set<SegmentChordVoicing> Fabricator::getChordVoicings() {
  return store.readAllSegmentChordVoicings(segmentId);
}


std::set<SegmentMeme> Fabricator::getSegmentMemes() {
  return store.readAllSegmentMemes(segmentId);
}


std::optional<ProgramSequence> Fabricator::getSequence(SegmentChoice choice) {
  std::optional<Program> program = getProgram(choice);
  if (program.isEmpty()) return std::nullopt;
  if (ValueUtils.isSet(choice.programSequenceBindingId)) {
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
    if (sequenceBinding.has_value())
      return sourceMaterial.getProgramSequence(sequenceBinding.get().programSequenceId);
  }

  if (!sequenceForChoice.containsKey(choice))
    getRandomlySelectedSequence(program.get()).ifPresent(
        programSequence->sequenceForChoice.put(choice, programSequence));

  return std::optional.of(sequenceForChoice.get(choice));
}


int Fabricator::getSequenceBindingOffsetForChoice(SegmentChoice choice) {
  if (ValueUtils.isEmpty(choice.programSequenceBindingId)) return 0;
  var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
  return sequenceBinding.map(ProgramSequenceBinding::getOffset).orElse(0);
}


std::string Fabricator::getTrackName(ProgramSequencePatternEvent event) {
  return sourceMaterial.getTrackOfEvent(event).map(ProgramVoiceTrack::getName).orElse(UNKNOWN_KEY);
}


Segment::Type Fabricator::getType() {
  if (!type.has_value()) type = computeType();
  return type.value();
}


std::optional<SegmentChordVoicing> Fabricator::chooseVoicing(SegmentChord chord, Instrument::Type type) {
  std::vector<SegmentChordVoicing>
  voicings = store.readAll(segmentId, SegmentChordVoicing.
  class);
  return MarbleBag.quickPick(voicings.stream()
                                 .filter(SegmentUtils::containsAnyValidNotes)
                                 .filter(voicing->Objects.equals(type, voicing.type))
                                 .filter(voicing->Objects.equals(chord.id, voicing.getSegmentChordId()))
                                 .collect(Collectors.toList()));
}


bool Fabricator::hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) {
  if (ValueUtils.isEmpty(choice.programSequenceBindingId)) return false;
  var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);

  if (sequenceBinding.isEmpty()) return false;
  std::vector<int> avlOfs = std::vector.copyOf(sourceMaterial.getAvailableOffsets(sequenceBinding.get()));

  // if we locate the target and still have two offsets remaining, result is true
  for (int i = 0; i < avlOfs.size(); i++)
    if (Objects.equals(avlOfs.get(i), sequenceBinding.get().offset) && i < avlOfs.size() - N) return true;

  return false;
}


bool Fabricator::hasOneMoreSequenceBindingOffset(SegmentChoice choice) {
  return hasMoreSequenceBindingOffsets(choice, 1);
}


bool Fabricator::hasTwoMoreSequenceBindingOffsets(SegmentChoice choice) {
  return hasMoreSequenceBindingOffsets(choice, 2);
}


bool Fabricator::isContinuationOfMacroProgram() {
  return Segment::Type::Continue == type || Segment::Type::NextMain == type;
}


bool Fabricator::isDirectlyBound(Program program) {
  return boundProgramIds.find(program.id) != boundProgramIds.end();
}


bool Fabricator::isOneShot(Instrument instrument, std::string trackName) {
  return isOneShot(instrument) &&
         !getInstrumentConfig(instrument).oneShotObserveLengthOfEventsContains(trackName);
}


bool Fabricator::isOneShot(Instrument instrument) {
  return getInstrumentConfig(instrument).isOneShot();
}


bool Fabricator::isOneShotCutoffEnabled(Instrument instrument) {
  return getInstrumentConfig(instrument).isOneShotCutoffEnabled();
}


bool Fabricator::isDirectlyBound(Instrument instrument) {
  return boundInstrumentIds.find(instrument.id) != boundInstrumentIds.end();
}


bool Fabricator::isDirectlyBound(InstrumentAudio instrumentAudio) {
  return boundInstrumentIds.find(instrumentAudio.instrumentId) != boundInstrumentIds.end();
}


bool Fabricator::isInitialSegment() {
  return 0L == getSegment().id;
}


SegmentChoice Fabricator::put(SegmentChoice entity, bool force) {
  auto memeStack = MemeStack::from(templateConfig.memeTaxonomy, SegmentMeme::getNames(getSegmentMemes()));

  // For a SegmentChoice, add memes from program, program sequence binding, and instrument if present
  if (!isValidChoiceAndMemesHaveBeenAdded((SegmentChoice) entity, memeStack, force))
    return entity;

  store.put(entity);

  return entity;
}


SegmentChoiceArrangement Fabricator::put(SegmentChoiceArrangement entity) {
  store.put(entity);
  return entity;
}


SegmentChoiceArrangementPick Fabricator::put(SegmentChoiceArrangementPick entity) {
  store.put(entity);
  return entity;
}


SegmentChord Fabricator::put(SegmentChord entity) {
  store.put(entity);
  return entity;
}


SegmentChordVoicing Fabricator::put(SegmentChordVoicing entity) {
  store.put(entity);
  return entity;
}


SegmentMeme Fabricator::put(SegmentMeme entity, bool force) {
  auto memeStack = MemeStack::from(templateConfig.memeTaxonomy, SegmentMeme::getNames(getSegmentMemes()));

  // Unless forced, don't put a duplicate of an existing meme
  if (!isValidMemeAddition((SegmentMeme) entity, memeStack, force))
    return entity;

  store.put(entity);

  return entity;
}


SegmentMessage Fabricator::put(SegmentMessage entity) {
  store.put(entity);
  return entity;
}


SegmentMeta Fabricator::put(SegmentMeta entity) {
  store.put(entity);
  return entity;
}


void Fabricator::putPreferredAudio(const std::string &parentIdent, const std::string &ident,
                                   const InstrumentAudio *instrumentAudio) {
  std::string cacheKey = computeCacheKeyForPreferredAudio(parentIdent, ident);

  preferredAudios.emplace(cacheKey, instrumentAudio);
}


void Fabricator::putReport(const std::string &key, const std::map<std::string, std::string> &value) {
  addMessage(SegmentMessage::Type::Debug, "Report[" + key + "]:" + CsvUtils::from(value));
}


void Fabricator::updateSegment(Segment segment) {
  try {
    store.updateSegment(segment);

  } catch (const FabricationException &e) {
    spdlog::error("Failed to update Segment", e.what());
  }
}


SegmentRetrospective Fabricator::getRetrospective() {
  return retrospective;
}


ContentEntityStore Fabricator::getSourceMaterial() {
  return sourceMaterial;
}


float Fabricator::getMicrosPerBeat(float tempo) {
  if (0 == microsPerBeat)
    microsPerBeat = (float) ValueUtils::MICROS_PER_MINUTE / tempo;
  return microsPerBeat;
}


int Fabricator::getSecondMacroSequenceBindingOffset(const Program &macroProgram) {
  std::vector<int> offsets;
  auto bindings = sourceMaterial.getSequenceBindingsOfProgram(macroProgram.id);

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
  return getSegment().tempo;
}


std::optional<SegmentMeta> Fabricator::getSegmentMeta(const std::string &key) {
  return store.readSegmentMeta(segmentId, key);
}


std::optional<SegmentChoice> Fabricator::getChoiceOfType(Program::Type programType) {
  auto allChoices = getChoices();
  auto it = std::find_if(allChoices.begin(), allChoices.end(), [programType](const SegmentChoice &choice) {
    return choice.programType == programType;
  });

  if (it != allChoices.end()) {
    return *it;
  } else {
    return std::nullopt;
  }
}


std::vector<SegmentChoice> Fabricator::getBeatChoices() {
  std::set<SegmentChoice> allChoices = getChoices();
  std::vector<SegmentChoice> beatChoices;

  std::copy_if(allChoices.begin(), allChoices.end(), std::back_inserter(beatChoices), [](const SegmentChoice &choice) {
    return choice.programType == Program::Type::Beat;
  });

  return beatChoices;
}


int Fabricator::computeLowestOptimalRangeShiftOctaves(const NoteRange &sourceRange, NoteRange targetRange) {
  int shiftOctave = 0; // search for optimal value
  int baselineDelta = 100; // optimal is the lowest possible integer zero or above
  for (int o = 10; o >= -10; o--) {
    if (!targetRange.low.has_value()) {
      throw FabricationException("can't get low end of target range");
    }
    int d = targetRange.low.value().delta(
        sourceRange.low.value_or(Note::atonal()).shiftOctave(o));
    if (0 <= d && d < baselineDelta) {
      baselineDelta = d;
      shiftOctave = o;
    }
  }
  return shiftOctave;
}


std::string Fabricator::computeShipKey(const Chain &chain, const Segment &segment) {
  std::string chainName = chain.shipKey.empty() ? "chain" + NAME_SEPARATOR + chain.id
                                                : chain.shipKey;
  std::string segmentName = std::to_string(segment.beginAtChainMicros);
  return chainName + NAME_SEPARATOR + segmentName;
}


std::string Fabricator::formatLog(const std::string &message) {
  return "[segId=" + std::to_string(getSegment().id) + "] " + message;
}


void Fabricator::ensureShipKey() {
  if (getSegment().storageKey.empty() || getSegment().storageKey.empty()) {
    auto seg = getSegment();
    auto chainOpt = store.readChain();
    if (!chainOpt.has_value()) {
      throw FabricationException("No chain");
    }
    seg.storageKey = computeShipKey(chainOpt.value(), getSegment());
    updateSegment(seg);

    spdlog::debug(formatLog("[segId={}] Generated ship key {}"), getSegment().id, getSegment().storageKey);
  }
}


Segment::Type Fabricator::computeType() {
  if (isInitialSegment())
    return Segment::Type::Initial;

  // previous main choice having at least one more pattern?
  std::optional<SegmentChoice> previousMainChoice = getPreviousMainChoice();

  if (previousMainChoice.has_value() && hasOneMoreSequenceBindingOffset(previousMainChoice.value())
      && getTemplateConfig().mainProgramLengthMaxDelta > getPreviousSegmentDelta())
    return Segment::Type::Continue;

  // previous macro choice having at least two more patterns?
  std::optional<SegmentChoice> previousMacroChoice = getMacroChoiceOfPreviousSegment();

  if (previousMacroChoice.has_value() && hasTwoMoreSequenceBindingOffsets(previousMacroChoice.value()))
    return Segment::Type::NextMain;

  return Segment::Type::NextMacro;
}


int Fabricator::getPreviousSegmentDelta() {
  auto previousSegment = retrospective.getPreviousSegment();
  return previousSegment.has_value() ? previousSegment.value().delta : 0;
}


std::map<std::string, const InstrumentAudio *> Fabricator::computePreferredInstrumentAudio() {
  std::map<std::string, const InstrumentAudio *> audios;

  auto picks = retrospective.getPicks();
  for (const auto &pick: picks) {
    auto audioOpt = sourceMaterial.getInstrumentAudio(pick.instrumentAudioId);
    if (audioOpt.has_value()) {
      audios[computeCacheKeyForVoiceTrack(pick)] = audioOpt.value();
    }
  }

  return audios;
}


bool Fabricator::isValidChoiceAndMemesHaveBeenAdded(SegmentChoice choice, MemeStack memeStack, bool force) {
  std::set<std::string> names;

  if (!choice.programId.empty())
    for (auto meme: sourceMaterial.getMemesOfProgram(choice.programId))
      names.emplace(StringUtils::toMeme(meme->name));

  if (choice.programSequenceBindingId.empty())
    for (auto meme: sourceMaterial.getMemesOfSequenceBinding(choice.programSequenceBindingId))
      names.emplace(StringUtils::toMeme(meme->name));

  if (choice.instrumentId.empty())
    for (auto meme: sourceMaterial.getMemesOfInstrument(choice.instrumentId))
      names.emplace(StringUtils::toMeme(meme->name));

  if (!force && !memeStack.isAllowed(names)) {
    addMessage(SegmentMessage::Type::Error,
               "Refused to add Choice[" + SegmentUtils::describe(choice) + "] because adding Memes[" +
               CsvUtils::join(std::vector<std::string>(names.begin(), names.end())) + "] to MemeStack[" +
               memeStack.getConstellation() + "] would result in an invalid meme stack theorem!");
    return false;
  }

  for (const std::string &name: names) {
    SegmentMeme segmentMeme;
    segmentMeme.id = Entity::randomUUID();
    segmentMeme.segmentId = getSegment().id;
    segmentMeme.name = name;
    put(segmentMeme, false);
  }

  return true;
}


bool Fabricator::isValidMemeAddition(SegmentMeme meme, MemeStack memeStack, bool force) {
  if (!force && !memeStack.isAllowed({meme.name})) return false;
  if (!force && std::any_of(getSegmentMemes().begin(), getSegmentMemes().end(),
                            [&meme](const SegmentMeme &m) { return m.name == meme.name; }))
    return false;
  return true;
}

std::string Fabricator::computeCacheKeyForPreferredAudio(const std::string &parentIdent, const std::string &ident) {
  return "voice-" + parentIdent + "_note-" + ident;
}