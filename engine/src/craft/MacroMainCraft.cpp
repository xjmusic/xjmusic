// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/util/ValueUtils.h"
#include "xjmusic/craft/MacroMainCraft.h"

using namespace XJ;

MacroMainCraft::MacroMainCraft(
    Fabricator *fabricator,
    const std::optional<const Program *> &overrideMacroProgram,
    const std::set<std::string> &overrideMemes) : Craft(fabricator) {
  this->overrideMacroProgram = overrideMacroProgram;
  this->overrideMemes = overrideMemes;
}

void MacroMainCraft::doWork() const {
  const auto segment = fabricator->getSegment();

  // Prepare variables to hold result of macro and main choice
  // Depending on whether we have override memes, we may perform main then macro (override), or macro then main (auto)
  const ProgramSequence *macroSequence;
  const ProgramSequence *mainSequence;

  // If we are overriding memes, start by adding them to the workbench segment, and do main before macro
  if (!overrideMemes.empty()) {
    for (const std::string &meme: overrideMemes) {
      auto segmentMeme = SegmentMeme();
      segmentMeme.id = EntityUtils::computeUniqueId();
      segmentMeme.segmentId = fabricator->getSegment()->id;
      segmentMeme.name = meme;
      fabricator->put(segmentMeme, true);
    }
    // choose main then macro (override)
    mainSequence = doMainChoiceWork(segment);
    macroSequence = doMacroChoiceWork(segment);
  } else {
    // choose macro then main (auto)
    macroSequence = doMacroChoiceWork(segment);
    mainSequence = doMainChoiceWork(segment);
  }
  const auto mainProgramOpt = fabricator->getSourceMaterial()->getProgram(mainSequence->programId);
  if (!mainProgramOpt.has_value())
    throw FabricationException("Unable to determine main program for Segment[" + std::to_string(segment->id) + "]");
  const Program *mainProgram = mainProgramOpt.value();

  // 3. Chords and voicings
  for (const auto sequenceChord: fabricator->getProgramSequenceChords(mainSequence)) {
    // don't of chord past end of Segment
    if (sequenceChord->position < static_cast<int>(mainSequence->total)) {
      // delta the chord name
      const std::string name = Chord(sequenceChord->name).getName();
      // of the chord
      auto chord = SegmentChord();
      chord.id = EntityUtils::computeUniqueId();
      chord.segmentId = segment->id;
      chord.position = sequenceChord->position;
      chord.name = name;
      fabricator->put(chord);
      for (const auto voicing: fabricator->getSourceMaterial()->getVoicingsOfChord(sequenceChord)) {
        auto segmentChordVoicing = SegmentChordVoicing();
        segmentChordVoicing.id = EntityUtils::computeUniqueId();
        segmentChordVoicing.segmentId = segment->id;
        segmentChordVoicing.segmentChordId = chord.id;
        segmentChordVoicing.type = fabricator->getProgramVoiceType(voicing);
        segmentChordVoicing.notes = voicing->notes;
        fabricator->put(segmentChordVoicing);
      }
    }
  }

  // Update the segment with fabricated content
  segment->type = fabricator->getType();
  segment->tempo = mainProgram->tempo;
  segment->key = computeSegmentKey(mainSequence);
  segment->total = mainSequence->total;
  segment->durationMicros = segmentLengthMicros(mainProgram, mainSequence);

  // If the type is not Continue, we will reset the offset main
  if (Segment::Type::Continue == fabricator->getType())
    segment->delta = segment->delta + segment->total;
  else
    segment->delta = 0;

  // Set the intensity
  segment->intensity = computeSegmentIntensity(segment->delta, macroSequence, mainSequence);

  // Finished
  fabricator->updateSegment(*segment);
}

const ProgramSequence *MacroMainCraft::doMacroChoiceWork(const Segment *segment) const {
  const auto macroProgram = chooseMacroProgram();
  const int macroSequenceBindingOffset = computeMacroSequenceBindingOffset();

  const auto macroSequenceBinding =
      fabricator->getRandomlySelectedSequenceBindingAtOffset(macroProgram, macroSequenceBindingOffset);
  if (!macroSequenceBinding.has_value())
    throw FabricationException(
        "Unable to determine macro sequence binding for Segment[" + std::to_string(segment->id) + "]");

  const auto macroSequence = fabricator->getSourceMaterial()->getSequenceOfBinding(macroSequenceBinding.value());
  if (!macroSequence.has_value())
    throw FabricationException(
        "Unable to determine macro sequence for Segment[" + std::to_string(segment->id) + "]");
  //
  auto macroChoice = SegmentChoice();
  macroChoice.id = EntityUtils::computeUniqueId();
  macroChoice.segmentId = segment->id;
  macroChoice.programSequenceId = macroSequence.value()->id;
  macroChoice.programId = macroProgram->id;
  macroChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  macroChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  macroChoice.programType = Program::Type::Macro;
  macroChoice.programSequenceBindingId = macroSequenceBinding.value()->id;
  fabricator->put(macroChoice, true); // force put, because fabrication cannot proceed without a macro choice

  return macroSequence.value();
}

const ProgramSequence *MacroMainCraft::doMainChoiceWork(const Segment *segment) const {
  const auto mainProgram = chooseMainProgram();
  const int mainSequenceBindingOffset = computeMainProgramSequenceBindingOffset();

  const auto mainSequenceBinding =
      fabricator->getRandomlySelectedSequenceBindingAtOffset(mainProgram, mainSequenceBindingOffset);
  if (!mainSequenceBinding.has_value())
    throw FabricationException(
        "Unable to determine main sequence binding for Segment[" + std::to_string(segment->id) + "]");

  const auto mainSequence =
      fabricator->getSourceMaterial()->getSequenceOfBinding(mainSequenceBinding.value());
  if (!mainSequence.has_value())
    throw FabricationException(
        "Unable to determine main sequence for Segment[" + std::to_string(segment->id) + "]");

  //
  auto mainChoice = SegmentChoice();
  mainChoice.id = EntityUtils::computeUniqueId();
  mainChoice.segmentId = segment->id;
  mainChoice.programId = mainProgram->id;
  mainChoice.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  mainChoice.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  mainChoice.programType = Program::Type::Main;
  mainChoice.programSequenceBindingId = mainSequenceBinding.value()->id;
  fabricator->put(mainChoice, true); // force put, because fabrication cannot proceed without a main choice

  return mainSequence.value();
}

std::string MacroMainCraft::computeSegmentKey(const ProgramSequence *mainSequence) const {
  std::string mainKey = mainSequence->key;
  if (mainKey.empty()) {
    const auto mainProgram = fabricator->getSourceMaterial()->getProgram(mainSequence->programId);
    if (!mainProgram.has_value())
      throw FabricationException(
          "Unable to determine key for Main-Program[" + mainSequence->programId + "] " + mainSequence->name);
    mainKey = mainProgram.value()->key;
  }
  return Chord::of(mainKey).getName();
}

double MacroMainCraft::computeSegmentIntensity(
    const int delta,
    const std::optional<const ProgramSequence *> macroSequence,
    const std::optional<const ProgramSequence *> mainSequence) const {
  return fabricator->getTemplateConfig().intensityAutoCrescendoEnabled
         ?
         ValueUtils::limitDecimalPrecision(ValueUtils::interpolate(
             fabricator->getTemplateConfig().intensityAutoCrescendoMinimum,
             fabricator->getTemplateConfig().intensityAutoCrescendoMaximum,
             static_cast<double>(delta) / fabricator->getTemplateConfig().mainProgramLengthMaxDelta,
             computeIntensity(macroSequence, mainSequence)
         ))
         :
         computeIntensity(macroSequence, mainSequence);
}

float MacroMainCraft::computeIntensity(
    const std::optional<const ProgramSequence *> macroSequence,
    const std::optional<const ProgramSequence *> mainSequence) {
  const std::optional<float> macroIntensity = macroSequence.has_value() ? std::optional(
                                                                              macroSequence.value()->intensity)
                                                                        : std::nullopt;
  const std::optional<float> mainIntensity = mainSequence.has_value() ? std::optional(mainSequence.value()->intensity)
                                                                : std::nullopt;
  if (macroIntensity.has_value() && mainIntensity.has_value())
    return (macroIntensity.value() + mainIntensity.value()) / 2;
  if (macroIntensity.has_value())
    return macroIntensity.value();
  if (mainIntensity.has_value())
    return mainIntensity.value();
  throw FabricationException("Failed to compute Intensity!");
}

int MacroMainCraft::computeMacroSequenceBindingOffset() const {
  if (fabricator->getType() == Segment::Type::Initial || fabricator->getType() == Segment::Type::NextMacro)
    return overrideMacroProgram.has_value()
               ? fabricator->getSecondMacroSequenceBindingOffset(overrideMacroProgram.value())
               : 0;

  const auto previousMacroChoice = fabricator->getMacroChoiceOfPreviousSegment();
  if (!previousMacroChoice.has_value())
    return 0;

  if (fabricator->getType() == Segment::Type::Continue)
    return fabricator->getSequenceBindingOffsetForChoice(previousMacroChoice.value());

  if (fabricator->getType() == Segment::Type::NextMain)
    return fabricator->getNextSequenceBindingOffset(previousMacroChoice.value());

  throw FabricationException(
      "Cannot get Macro-type sequence for known fabricator type=" + Segment::toString(fabricator->getType()));
}

int MacroMainCraft::computeMainProgramSequenceBindingOffset() const {
  switch (fabricator->getType()) {

    case Segment::Type::Initial:
    case Segment::Type::NextMain:
    case Segment::Type::NextMacro:
      return 0;

    case Segment::Type::Continue: {
      const auto previousMainChoice = fabricator->getPreviousMainChoice();
      if (!previousMainChoice.has_value())
        throw FabricationException("Cannot get retrieve previous main choice");
      return fabricator->getNextSequenceBindingOffset(previousMainChoice.value());
    }

    default:
      throw FabricationException(
          "Cannot get Macro-type sequence for known fabricator type=" + Segment::toString(fabricator->getType()));
  }
}

const Program *MacroMainCraft::chooseRandomProgram(const std::set<const Program *> &programs, std::set<UUID> avoid) const {
  auto bag = MarbleBag();

  // Phase 1: Directly Bound Programs, besides those we should avoid
  // Phase 2: Any Directly Bound Programs
  for (const auto program: programsDirectlyBound(programs)) {
    if (avoid.find(program->id) == avoid.end())
      bag.add(1, program->id);
    bag.add(2, program->id);
  }

  // Phase 3: All Published Programs, besides those we should avoid
  // Phase 4: Any Published Programs
  for (const auto program: programsPublished(programs)) {
    if (avoid.find(program->id) == avoid.end())
      bag.add(3, program->id);
    bag.add(4, program->id);
  }

  // Phase 5: Any Program
  for (const auto program: programs)
    bag.add(5, program->id);

  // if the bag is empty, problems
  if (bag.empty())
    throw FabricationException("Failed to choose any random program. No candidates available!");

  const auto program = fabricator->getSourceMaterial()->getProgram(bag.pick());
  if (!program.has_value()) {
    const auto message =
        "Unable to choose main program for Segment[" + std::to_string(fabricator->getSegment()->id) + "]";
    fabricator->addErrorMessage(message);
    spdlog::error(message);
    throw FabricationException(message);
  }
  return program.value();
}

const Program *MacroMainCraft::chooseMacroProgram() const {
  if (overrideMacroProgram.has_value())
    return overrideMacroProgram.value();

  auto bag = MarbleBag();
  auto candidates = fabricator->getSourceMaterial()->getProgramsOfType(Program::Type::Macro);

  // initial segment is completely random
  if (fabricator->isInitialSegment()) return chooseRandomProgram(candidates, {});

  // if continuing the macro program, use the same one
  if (fabricator->isContinuationOfMacroProgram()
      && fabricator->getMacroChoiceOfPreviousSegment().has_value()) {
    auto previousProgram = fabricator->getProgram(fabricator->getMacroChoiceOfPreviousSegment().value());
    if (!previousProgram.has_value()) {
      auto message =
          "Unable to get previous macro program for Segment[" + std::to_string(fabricator->getSegment()->id) + "]";
      fabricator->addErrorMessage(message);
      spdlog::error(message);
      throw FabricationException(message);
    }
    return previousProgram.value();
  }

  // Compute the meme isometry for use in selecting programs from the bag
  MemeIsometry iso =
      !overrideMemes.empty() ?
      MemeIsometry::of(fabricator->getMemeTaxonomy(), overrideMemes)
                             : fabricator->getMemeIsometryOfNextSequenceInPreviousMacro();

  // Compute any program id to avoid
  auto avoidOpt = fabricator->getMacroChoiceOfPreviousSegment();
  auto avoidProgramId = avoidOpt.has_value() ? std::optional(avoidOpt.value()->programId) : std::nullopt;

  // Add candidates to the bag
  // Phase 1: Directly Bound Programs besides any that should be avoided, with a meme match
  // Phase 2: Any Directly Bound Programs besides any that should be avoided, meme match is a bonus
  // Phase 3: Any Directly Bound Programs
  for (auto program: programsDirectlyBound(candidates)) {
    bag.add(1, program->id, iso.score(fabricator->getSourceMaterial()->getMemesAtBeginning(program)));
    bag.add(2, program->id, 1 + iso.score(fabricator->getSourceMaterial()->getMemesAtBeginning(program)));
    bag.add(3, program->id);
  }

  // Add candidates to the bag
  // Phase 4: All Published Programs with a meme match, besides any that should be avoided
  // Phase 5: Any Published Programs, meme match is a bonus
  // Phase 6: Any Published Programs
  for (auto program: programsPublished(candidates)) {
    if (!avoidProgramId.has_value() || avoidProgramId.value() != program->id) {
      bag.add(4, program->id, iso.score(fabricator->getSourceMaterial()->getMemesAtBeginning(program)));
      bag.add(5, program->id, 1 + iso.score(fabricator->getSourceMaterial()->getMemesAtBeginning(program)));
    }
    bag.add(6, program->id);
  }

  // Add candidates to the bag
  // Phase 7: Literally Any Programs
  for (auto program: candidates)
    bag.add(7, program->id);

  // if the bag is empty, problems
  if (bag.empty())
    throw FabricationException("Failed to choose any next macro program. No candidates available!");

  // report and pick
  fabricator->putReport("macroChoice", bag.toString());
  auto program = fabricator->getSourceMaterial()->getProgram(bag.pick());
  if (!program.has_value()) {
    auto message = "Unable to choose macro program for Segment[" + std::to_string(fabricator->getSegment()->id) + "]";
    fabricator->addErrorMessage(message);
    spdlog::error(message);
    throw FabricationException(message);
  }
  return program.value();
}

const Program *MacroMainCraft::chooseMainProgram() const {
  auto bag = MarbleBag();
  auto candidates = fabricator->getSourceMaterial()->getProgramsOfType(Program::Type::Main);

  // if continuing the macro program, use the same one
  if (Segment::Type::Continue == fabricator->getType()
      && fabricator->getPreviousMainChoice().has_value()) {
    auto previousChoice = fabricator->getPreviousMainChoice();
    auto previousProgram = previousChoice.has_value() ?
                           fabricator->getProgram(fabricator->getPreviousMainChoice().value())
                                                      : std::nullopt;
    if (!previousProgram.has_value()) {
      auto message =
          "Unable to get previous main program for Segment[" + std::to_string(fabricator->getSegment()->id) + "]";
      fabricator->addErrorMessage(message);
      spdlog::error(message);
      throw FabricationException(message);
    }
    return previousProgram.value();
  }

  // Compute the meme isometry for use in selecting programs from the bag
  MemeIsometry iso = fabricator->getMemeIsometryOfSegment();

  // Compute any program id to avoid
  auto avoidChoice = fabricator->getPreviousMainChoice();
  auto avoidProgramId = avoidChoice.has_value() ? std::optional(avoidChoice.value()->programId)
                                                : std::nullopt;

  // Add candidates to the bag
  // Phase 1: Directly Bound Programs, memes allowed, bonus for meme match, besides any that should be avoided
  for (auto program: programsDirectlyBound(candidates)) {
    if (!iso.isAllowed(fabricator->getSourceMaterial()->getMemesAtBeginning(program))) continue;
    bag.add(1, program->id, 1 + iso.score(fabricator->getSourceMaterial()->getMemesAtBeginning(program)));
  }

  // Add candidates to the bag
  // Phase 2: All Published Programs, memes allowed, bonus for meme match, besides any that should be avoided
  // Phase 3: Any Published Programs, memes allowed, bonus for meme match
  auto published = programsPublished(candidates);
  for (auto program: published) {
    if (!iso.isAllowed(fabricator->getSourceMaterial()->getMemesAtBeginning(program))) {
      continue;
    }
    if (!avoidProgramId.has_value() || avoidProgramId.value() != program->id)
      bag.add(2, program->id, 1 + iso.score(fabricator->getSourceMaterial()->getMemesAtBeginning(program)));
    else
      bag.add(3, program->id, 1 + iso.score(fabricator->getSourceMaterial()->getMemesAtBeginning(program)));
  }

  // Add candidates to the bag
  // Phase 4: Literally Any Programs
  for (auto program: candidates)
    bag.add(4, program->id);

  // if the bag is empty, problems
  if (bag.empty()) {
    throw FabricationException("Failed to choose any next main program. No candidates available!");
  }

  // report and pick
  fabricator->putReport("mainChoice", bag.toString());
  auto program = fabricator->getSourceMaterial()->getProgram(bag.pick());
  if (!program.has_value()) {
    auto message = "Unable to choose main program for Segment[" + std::to_string(fabricator->getSegment()->id) + "]";
    fabricator->addErrorMessage(message);
    spdlog::error(message);
    throw FabricationException(message);
  }
  return program.value();
}

long MacroMainCraft::segmentLengthMicros(const Program *mainProgram, const ProgramSequence *mainSequence) const {
  return fabricator->getSegmentMicrosAtPosition(mainProgram->tempo, static_cast<float>(mainSequence->total));
}
