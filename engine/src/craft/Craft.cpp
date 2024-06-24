// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/craft/Craft.h"
#include "xjmusic/util/CsvUtils.h"

#include <xjmusic/music/Bar.h>
#include <xjmusic/util/ValueUtils.h>

using namespace XJ;

Craft::Craft(Fabricator *fabricator) : FabricationWrapper(fabricator) {
  finalizeAudioLengthsForInstrumentTypes = fabricator->getTemplateConfig().instrumentTypesForAudioLengthFinalization;
}

bool Craft::isIntroSegment(const SegmentChoice *choice) const {
  return !isUnlimitedIn(*choice) && choice->deltaIn >= fabricator->getSegment()->delta &&
         choice->deltaIn < fabricator->getSegment()->delta + fabricator->getSegment()->total;
}

bool Craft::isOutroSegment(const SegmentChoice *choice) const {
  return !isUnlimitedOut(*choice) &&
         choice->deltaOut <= fabricator->getSegment()->delta + fabricator->getSegment()->total &&
         choice->deltaOut > fabricator->getSegment()->delta;
}

bool Craft::isSilentEntireSegment(const SegmentChoice *choice) const {
  return (choice->deltaOut < fabricator->getSegment()->delta) ||
         (choice->deltaIn >= fabricator->getSegment()->delta + fabricator->getSegment()->total);
}

bool Craft::isActiveEntireSegment(const SegmentChoice *choice) const {
  return (choice->deltaIn <= fabricator->getSegment()->delta) &&
         (choice->deltaOut >= fabricator->getSegment()->delta + fabricator->getSegment()->total);
}

void Craft::craftNoteEventArrangements(const float tempo, const SegmentChoice *choice, const bool defaultAtonal) {
  // this is used to invert voicings into the tightest possible range
  // passed to each iteration of note voicing arrangement in order to move as little as possible from the previous
  auto range = NoteRange();

  if (!fabricator->getProgram(choice).has_value())
    throw FabricationException("Can't get program config");

  if (const auto programConfig = XJ::Fabricator::getProgramConfig(fabricator->getProgram(choice).value()); fabricator->getSegmentChords().empty() || !programConfig.doPatternRestartOnChord) {
    craftNoteEventSection(tempo, choice, 0, static_cast<float>(fabricator->getSegment()->total), &range, defaultAtonal);
  } else {
    craftNoteEventSectionRestartingEachChord(tempo, choice, &range, defaultAtonal);
  }

  // Final pass to set the actual length of one-shot audio picks
  finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(choice);
}

void Craft::precomputeDeltas(
    const std::function<bool(const SegmentChoice *)> &choiceFilter,
    ChoiceIndexProvider *setChoiceIndexProvider,
    const std::vector<std::string> &layers,
    const std::set<std::string> &layerPrioritizationSearches,
    const int numLayersIncoming) {
  this->choiceIndexProvider = setChoiceIndexProvider;
  deltaIns.clear();
  deltaOuts.clear();

  // Ensure that we can bypass delta arcs using the template config
  if (!fabricator->getTemplateConfig().deltaArcEnabled) {
    for (const auto &layer: layers) {
      deltaIns[layer] = SegmentChoice::DELTA_UNLIMITED;
      deltaOuts[layer] = SegmentChoice::DELTA_UNLIMITED;
    }
    return;
  }

  // then we overwrite the wall-to-wall random values with more specific values depending on the situation
  switch (fabricator->type) {
    case Segment::Type::Pending: {
      // No Op
    }

    case Segment::Type::Initial:
    case Segment::Type::NextMain:
    case Segment::Type::NextMacro: {
      // randomly override N incoming (deltaIn unlimited) and N outgoing (deltaOut unlimited)
      // shuffle the layers into a random order, then step through them, assigning delta ins and then outs
      // random order in
      const auto barBeats = fabricator->getCurrentMainProgramConfig().barBeats;
      const auto deltaUnits = Bar::of(barBeats).computeSubsectionBeats(fabricator->getSegment()->total);

      // Delta arcs can prioritize the presence of a layer by name, e.g. containing "kick"
      // separate layers into primary and secondary, shuffle them separately, then concatenate
      std::vector<std::string> priLayers;
      std::vector<std::string> secLayers;
      for (const auto &layer: layers) {
        auto layerName = StringUtils::toLowerCase(layer);
        if (std::any_of(layerPrioritizationSearches.begin(), layerPrioritizationSearches.end(),
                        [&layerName](const std::string &m) { return layerName.find(m) != std::string::npos; }))
          priLayers.emplace_back(layer);
        else
          secLayers.emplace_back();
      }

      std::shuffle(priLayers.begin(), priLayers.end(), gen);
      if (!priLayers.empty())
        fabricator->addInfoMessage("Prioritized " + CsvUtils::join(priLayers));
      std::shuffle(secLayers.begin(), secLayers.end(), gen);

      std::vector<std::string> orderedLayers;
      orderedLayers.insert(orderedLayers.end(), priLayers.begin(), priLayers.end());
      orderedLayers.insert(orderedLayers.end(), secLayers.begin(), secLayers.end());

      std::shuffle(orderedLayers.begin(), orderedLayers.end(), gen);

      auto delta = ValueUtils::roundToNearest(deltaUnits, MarbleBag::quickPick(deltaUnits * 4) -
                                                              deltaUnits * 2 * numLayersIncoming);
      for (const std::string &orderedLayer: orderedLayers) {
        deltaIns[orderedLayer] = (delta > 0) ? delta : SegmentChoice::DELTA_UNLIMITED;
        deltaOuts[orderedLayer] = SegmentChoice::DELTA_UNLIMITED;// all layers get delta out unlimited
        delta += ValueUtils::roundToNearest(deltaUnits, MarbleBag::quickPick(deltaUnits * 5));
      }
    }

    case Segment::Type::Continue: {
      for (const std::string &index: layers)
        for (const auto &choice: fabricator->getRetrospective()->getChoices())
          if (choiceFilter(choice)) {
            if (this->choiceIndexProvider->get(choice) == index) {
              deltaIns[index] = choice->deltaIn;
              deltaOuts[index] = choice->deltaOut;
            }
          }
    }
  }
}

bool Craft::inBounds(const int floor, const int ceiling, const float value) {
  if (SegmentChoice::DELTA_UNLIMITED == floor && SegmentChoice::DELTA_UNLIMITED == ceiling) return true;
  if (SegmentChoice::DELTA_UNLIMITED == floor && value <= static_cast<float>(ceiling)) return true;
  if (SegmentChoice::DELTA_UNLIMITED == ceiling && value >= static_cast<float>(floor)) return true;
  return value >= static_cast<float>(floor) && value <= static_cast<float>(ceiling);
}

bool Craft::isUnlimitedIn(const SegmentChoice &choice) {
  return SegmentChoice::DELTA_UNLIMITED == choice.deltaIn;
}

bool Craft::isUnlimitedOut(const SegmentChoice &choice) {
  return SegmentChoice::DELTA_UNLIMITED == choice.deltaOut;
}

std::optional<const Program *>
Craft::chooseFreshProgram(const Program::Type programType, const std::optional<Instrument::Type> voicingType) const {
  const auto bag = new MarbleBag();

  // Retrieve programs bound to chain having a voice of the specified type
  std::map<UUID, const Program*> programMap;
  for (const auto program: fabricator->getSourceMaterial()->getProgramsOfType(programType)) {
    programMap[program->id] = program;
  }

  std::set<const Program*> candidates;
  for (const auto &programVoice: fabricator->getSourceMaterial()->getProgramVoices()) {
    if (voicingType.has_value() && voicingType.value() == programVoice->type &&
        programMap.count(programVoice->programId)) {
      candidates.insert(programMap[programVoice->programId]);
    }
  }

  // (3) score each source program based on meme isometry
  MemeIsometry iso = fabricator->getMemeIsometryOfSegment();
  std::set<std::string> memes;

  // Phase 1: Directly Bound Programs
  for (const Program *program: programsDirectlyBound(candidates)) {
    memes = ProgramMeme::getNames(fabricator->getSourceMaterial()->getMemesOfProgram(program->id));
    // FUTURE consider meme isometry, but for now, just use the meme stack
    if (iso.isAllowed(memes)) bag->add(1, program->id, 1 + iso.score(memes));
  }

  // Phase 2: All Published Programs
  for (const Program *program: programsPublished(candidates)) {
    memes = ProgramMeme::getNames(fabricator->getSourceMaterial()->getMemesOfProgram(program->id));
    // FUTURE consider meme isometry, but for now, just use the meme stack
    if (iso.isAllowed(memes)) bag->add(2, program->id, 1 + iso.score(memes));
  }

  // report
  fabricator->putReport("choiceOf" + (voicingType.has_value() ? Instrument::toString(voicingType.value()) : "") +
                            Program::toString(programType) + "Program",
                        bag->toString());

  // (4) return the top choice
  if (bag->empty()) return std::nullopt;
  return fabricator->getSourceMaterial()->getProgram(bag->pick());
}

std::optional<const Instrument *>
Craft::chooseFreshInstrument(const Instrument::Type type, const std::set<std::string> &requireEventNames) const {
  const auto bag = new MarbleBag();

  // Retrieve instruments bound to chain
  std::set<const Instrument *> candidates;
  for (const auto &instrument: fabricator->getSourceMaterial()->getInstrumentsOfType(type)) {
    if (instrumentContainsAudioEventsLike(*instrument, requireEventNames)) {
      candidates.emplace(instrument);
    }
  }

  // Retrieve meme isometry of segment
  MemeIsometry iso = fabricator->getMemeIsometryOfSegment();
  std::set<std::string> memes;

  // Phase 1: Directly Bound Instruments
  for (const Instrument *instrument: instrumentsDirectlyBound(candidates)) {
    memes = InstrumentMeme::getNames(fabricator->getSourceMaterial()->getMemesOfInstrument(instrument->id));
    if (iso.isAllowed(memes)) bag->add(1, instrument->id, 1 + iso.score(memes));
  }

  // Phase 2: All Published Instruments
  for (const Instrument *instrument: instrumentsPublished(candidates)) {
    memes = InstrumentMeme::getNames(fabricator->getSourceMaterial()->getMemesOfInstrument(instrument->id));
    if (iso.isAllowed(memes)) bag->add(2, instrument->id, 1 + iso.score(memes));
  }

  // report
  fabricator->putReport("choiceOf" + Instrument::toString(type) + "Instrument", bag->toString());

  // (4) return the top choice
  if (bag->empty()) return std::nullopt;
  return fabricator->getSourceMaterial()->getInstrument(bag->pick());
}

std::optional<const InstrumentAudio *>
Craft::chooseFreshInstrumentAudio(
    const std::set<Instrument::Type> &types,
    const std::set<Instrument::Mode> &modes,
    const std::set<UUID> &avoidIds,
    const std::set<std::string> &preferredEvents) const {
  const auto bag = new MarbleBag();

  // (2) retrieve instruments bound to chain
  std::set<const InstrumentAudio *> candidates;
  for (const auto &audio: fabricator->getSourceMaterial()->getAudiosOfInstrumentTypesAndModes(types, modes)) {
    if (avoidIds.find(audio->id) == avoidIds.end()) {
      candidates.insert(audio);
    }
  }

  // (3) score each source instrument based on meme isometry
  MemeIsometry iso = fabricator->getMemeIsometryOfSegment();
  std::set<std::string> memes;

  // Phase 1: Directly Bound Audios (Preferred)
  for (const InstrumentAudio *audio: audiosDirectlyBound(candidates)) {
    memes = InstrumentMeme::getNames(fabricator->getSourceMaterial()->getMemesOfInstrument(audio->instrumentId));
    if (iso.isAllowed(memes)) {
      bag->add(preferredEvents.find(audio->event) != preferredEvents.end() ? 1 : 3, audio->id, 1 + iso.score(memes));
    }
  }

  // Phase 2: All Published Audios (Preferred)
  for (const InstrumentAudio *audio: audiosPublished(candidates)) {
    memes = InstrumentMeme::getNames(fabricator->getSourceMaterial()->getMemesOfInstrument(audio->instrumentId));
    if (iso.isAllowed(memes)) {
      bag->add(preferredEvents.find(audio->event) != preferredEvents.end() ? 2 : 4, audio->id, 1 + iso.score(memes));
    }
  }

  // report
  std::string typeNames;
  for (const auto &type: types) {
    if (!typeNames.empty()) {
      typeNames += ",";
    }
    typeNames += Instrument::toString(type);
  }
  std::string modeNames;
  for (const auto &mode: modes) {
    if (!modeNames.empty()) {
      modeNames += ",";
    }
    modeNames += Instrument::toString(mode);
  }
  fabricator->putReport("choice" + typeNames + modeNames, bag->toString());

  // (4) return the top choice
  if (bag->empty()) return std::nullopt;
  return fabricator->getSourceMaterial()->getInstrumentAudio(bag->pick());
}

std::optional<const InstrumentAudio *>
Craft::selectNewChordPartInstrumentAudio(const Instrument *instrument, const Chord &chord) const {
  const auto bag = new MarbleBag();

  for (const auto a: fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument)) {
    Chord audioChord = Chord::of(a->tones);
    if (audioChord == chord) {
      bag->add(0, a->id);
    } else if (audioChord.isAcceptable(chord)) {
      bag->add(1, a->id);
    }
  }

  if (bag->empty()) return std::nullopt;
  auto audio = fabricator->getSourceMaterial()->getInstrumentAudio(bag->pick());
  if (!audio.has_value()) return std::nullopt;
  return {audio.value()};
}

std::set<InstrumentAudio> Craft::selectGeneralAudioIntensityLayers(const Instrument *instrument) const {
  const auto previous = fabricator->getRetrospective()->getPreviousPicksForInstrument(instrument->id);
  if (!previous.empty() && fabricator->getInstrumentConfig(instrument).isAudioSelectionPersistent) {
    std::set<InstrumentAudio> result;
    for (const auto &pick: previous) {
      auto audio = fabricator->getSourceMaterial()->getInstrumentAudio(pick->instrumentAudioId);
      if (audio.has_value()) {
        result.insert(*audio.value());
      }
    }
    return result;
  }

  return selectAudioIntensityLayers(
      fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument->id),
      fabricator->getTemplateConfig().getIntensityLayers(instrument->type));
}

std::set<const Program *> Craft::programsDirectlyBound(const std::set<const Program *> &programs) const {
  std::set<const Program *> result;
  for (const auto &program: programs) {
    if (fabricator->isDirectlyBound(program)) {
      result.insert(program);
    }
  }
  return result;
}

std::set<const Program *> Craft::programsPublished(const std::set<const Program *> &programs) {
  std::set<const Program *> result;
  for (const auto &program: programs) {
    if (program->state == Program::State::Published) {
      result.insert(program);
    }
  }
  return result;
}

std::set<const Instrument *> Craft::instrumentsDirectlyBound(const std::set<const Instrument *> &instruments) const {
  std::set<const Instrument *> result;
  for (const auto &instrument: instruments) {
    if (fabricator->isDirectlyBound(instrument)) {
      result.emplace(instrument);
    }
  }
  return result;
}

std::set<const Instrument *> Craft::instrumentsPublished(const std::set<const Instrument *> &instruments) {
  std::set<const Instrument *> result;
  for (const auto &instrument: instruments) {
    if (instrument->state == Instrument::State::Published) {
      result.emplace(instrument);
    }
  }
  return result;
}

std::set<const InstrumentAudio *> Craft::audiosDirectlyBound(const std::set<const InstrumentAudio *> &instrumentAudios) const {
  std::set<const InstrumentAudio *> result;
  for (const auto &audio: instrumentAudios) {
    if (fabricator->isDirectlyBound(audio)) {
      result.insert(audio);
    }
  }
  return result;
}

std::set<const InstrumentAudio *> Craft::audiosPublished(const std::set<const InstrumentAudio *> &instrumentAudios) const {
  std::set<const InstrumentAudio *> result;
  for (const auto &audio: instrumentAudios) {
    auto instrument = fabricator->getSourceMaterial()->getInstrument(audio->instrumentId);
    if (instrument.has_value() && instrument.value()->state == Instrument::State::Published) {
      result.insert(audio);
    }
  }
  return result;
}

bool Craft::computeMute(Instrument::Type instrumentType) const {
  return MarbleBag::quickBooleanChanceOf(fabricator->getTemplateConfig().getChoiceMuteProbability(instrumentType));
}

void Craft::pickInstrumentAudio(const SegmentChoiceArrangement &arrangement, const InstrumentAudio &audio,
                                long startAtSegmentMicros, long lengthMicros, std::string event) const {
  const auto pick = SegmentChoiceArrangementPick();
  pick->id = EntityUtils::computeUniqueId();
  pick->segmentId = fabricator->getSegment()->id;
  pick->segmentChoiceArrangementId = arrangement.id;
  pick->startAtSegmentMicros = startAtSegmentMicros;
  pick->lengthMicros = lengthMicros;
  pick->event = std::move(event);
  pick->amplitude = static_cast<float>(1.0);
  pick->instrumentAudioId = audio.id;
  fabricator->put(*pick);
}

std::set<InstrumentAudio>
Craft::selectAudioIntensityLayers(const std::set<const InstrumentAudio *> &audios, const int layers) const {
  // Sort audios by intensity
  std::vector<const InstrumentAudio *> sorted;
  std::copy(audios.begin(), audios.end(), std::back_inserter(sorted));
  std::sort(sorted.begin(), sorted.end(), [](const InstrumentAudio *a, const InstrumentAudio *b) {
    return a->intensity < b->intensity;
  });
  if (sorted.empty()) return {};

  // Create a vector of bags, one for each layer
  std::vector<MarbleBag *> bags(layers);
  for (int i = 0; i < layers; i++) {
    const auto bag = new MarbleBag();
    bags[i] = bag;
  }

  // Iterate through the available audios, and add them to the bags, divided into the number of layers
  const int marblesPerLayer = static_cast<int>(std::ceil(static_cast<float>(sorted.size()) / static_cast<float>(layers)));
  if (marblesPerLayer == 0) return {};
  for (int i = 0; i < sorted.size(); i++) {
    bags[i / marblesPerLayer]->add(1, sorted[i]->id);
  }

  std::set<InstrumentAudio> result;
  for (auto &bag: bags) {
    if (!bag->empty()) {
      auto audio = fabricator->getSourceMaterial()->getInstrumentAudio(bag->pick());
      if (audio.has_value()) {
        result.insert(*audio.value());
      }
    }
  }

  return result;
}

void Craft::craftNoteEvents(
    float tempo,
    const ProgramSequence *sequence,
    const std::set<const ProgramVoice *> &voices,
    InstrumentProvider *instrumentProvider) {
  // Craft each voice into choice
  for (const auto voice: voices) {
    auto choice = SegmentChoice();
    choice.id = EntityUtils::computeUniqueId();
    choice.segmentId = fabricator->getSegment()->id;
    choice.mute = computeMute(voice->type);
    auto p = fabricator->getSourceMaterial()->getProgram(voice->programId);
    if (!p.has_value()) {
      throw FabricationException("Can't get program for voice");
    }
    choice.programType = p.value()->type;
    choice.instrumentType = voice->type;
    choice.programId = voice->programId;
    choice.programSequenceId = sequence->id;
    choice.programVoiceId = voice->id;

    // Whether there is a prior choice for this voice
    std::optional<const SegmentChoice *> priorChoice = fabricator->getChoiceIfContinued(voice);

    if (priorChoice.has_value()) {
      choice.deltaIn = priorChoice.value()->deltaIn;
      choice.deltaOut = priorChoice.value()->deltaOut;
      choice.instrumentId = priorChoice.value()->instrumentId;
      choice.instrumentMode = priorChoice.value()->instrumentMode;
      fabricator->put(choice, false);
      this->craftNoteEventArrangements(tempo, &choice, false);
      continue;
    }

    auto instrument = instrumentProvider->get(voice);
    if (!instrument.has_value()) {
      continue;
    }

    // make new choices
    choice.deltaIn = computeDeltaIn(&choice);
    choice.deltaOut = computeDeltaOut(&choice);
    choice.instrumentId = instrument.value().id;
    choice.instrumentMode = instrument.value().mode;
    fabricator->put(choice, false);
    this->craftNoteEventArrangements(tempo, &choice, false);
  }
}

void Craft::craftChordParts(const float tempo, const Instrument *instrument) {
  // Craft each voice into choice
  auto choice = SegmentChoice();

  choice.id = EntityUtils::computeUniqueId();
  choice.segmentId = fabricator->getSegment()->id;
  choice.mute = computeMute(instrument->type);
  choice.instrumentType = instrument->type;
  choice.instrumentMode = instrument->mode;
  choice.instrumentId = instrument->id;

  // Whether there is a prior choice for this voice
  const std::optional<SegmentChoice *> priorChoice = fabricator->getChoiceIfContinued(instrument->type);

  if (priorChoice.has_value()) {
    choice.deltaIn = priorChoice.value()->deltaIn;
    choice.deltaOut = priorChoice.value()->deltaOut;
    choice.instrumentId = priorChoice.value()->instrumentId;
    fabricator->put(choice, false);
    this->craftChordParts(tempo, instrument, &choice);
    return;
  }

  // make new choices
  choice.deltaIn = computeDeltaIn(&choice);
  choice.deltaOut = computeDeltaOut(&choice);
  choice.instrumentId = instrument->id;
  fabricator->put(choice, false);
  this->craftChordParts(tempo, instrument, &choice);
}

void Craft::craftChordParts(const float tempo, const Instrument *instrument, const SegmentChoice *choice) {
  if (fabricator->getSegmentChords().empty()) return;

  // Arrangement
  auto *arrangement = SegmentChoiceArrangement();
  arrangement->id = EntityUtils::computeUniqueId();
  arrangement->segmentId = choice->segmentId;
  arrangement->segmentChoiceId = choice->id;
  fabricator->put(*arrangement);

  // Pick for each section
  for (auto &[chord, fromPos, toPos]: computeSections()) {
    auto audio = selectChordPartInstrumentAudio(instrument, Chord::of(chord->name));

    // Should gracefully skip audio in unfulfilled by instrument
    if (!audio.has_value()) continue;

    // Pick attributes are expressed "rendered" as actual seconds
    const long startAtSegmentMicros = fabricator->getSegmentMicrosAtPosition(tempo, fromPos);
    std::optional<long> lengthMicros;
    if (fabricator->isOneShot(instrument)) {
      lengthMicros = std::nullopt;
    } else {
      lengthMicros = fabricator->getSegmentMicrosAtPosition(tempo, toPos) - startAtSegmentMicros;
    }

    // Volume ratio
    const auto volRatio = computeVolumeRatioForPickedNote(*choice, fromPos);
    if (volRatio <= 0) continue;

    // Pick
    auto *pick = SegmentChoiceArrangementPick();
    pick->id = EntityUtils::computeUniqueId();
    pick->segmentId = choice->segmentId;
    pick->segmentChoiceArrangementId = arrangement->id;
    pick->instrumentAudioId = audio.value()->id;
    pick->startAtSegmentMicros = startAtSegmentMicros;
    pick->tones = chord->name;
    pick->event = StringUtils::toEvent(Instrument::toString(instrument->type));
    if (lengthMicros.has_value()) pick->lengthMicros = lengthMicros.value();
    pick->amplitude = volRatio;
    fabricator->put(*pick);
  }

  // Final pass to set the actual length of one-shot audio picks
  finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(choice);
}

void Craft::craftEventParts(const float tempo, const Instrument *instrument, const Program *program) {
  // Event detail sequence is selected at random of the current instrument
  // FUTURE: Detail Instrument with multiple Sequences https://github.com/xjmusic/xjmusic/issues/241
  const auto sequence = fabricator->getRandomlySelectedSequence(program);

  // Event voice arrangements
  if (sequence.has_value()) {
    const auto voices = fabricator->getSourceMaterial()->getVoicesOfProgram(program);
    if (voices.empty()) return;
    InstrumentProvider *instrumentProvider = new LambdaInstrumentProvider(
        [&instrument](const ProgramVoice &) -> std::optional<Instrument> {
          return {(*instrument)};
        });
    craftNoteEvents(tempo, sequence.value(), voices, instrumentProvider);
  }
}

int Craft::computeDeltaIn(const SegmentChoice *choice) {
  const auto it = deltaIns.find(choiceIndexProvider->get(choice));
  return (it != deltaIns.end()) ? it->second : SegmentChoice::DELTA_UNLIMITED;
}

int Craft::computeDeltaOut(const SegmentChoice *choice) {
  const auto it = deltaOuts.find(choiceIndexProvider->get(choice));
  return (it != deltaOuts.end()) ? it->second : SegmentChoice::DELTA_UNLIMITED;
}

void Craft::craftNoteEventSectionRestartingEachChord(
    const float tempo,
    const SegmentChoice *choice,
    NoteRange *range,
    const bool defaultAtonal) const {
  for (const auto &[chord, fromPos, toPos]: computeSections())
    craftNoteEventSection(tempo, choice, fromPos, toPos, range, defaultAtonal);
}

std::vector<Craft::Section> Craft::computeSections() const {
  // guaranteed to be in order of position ascending
  std::vector<SegmentChord *> chords(fabricator->getSegmentChords().size());
  auto i = 0;
  for (const auto &chord: fabricator->getSegmentChords()) {
    chords[i] = chord;
    i++;
  }
  std::vector<Section> sections(chords.size());
  for (i = 0; i < chords.size(); i++) {
    sections[i] = Section();
    sections[i].chord = chords[i];
    sections[i].fromPos = chords[i]->position;
    sections[i].toPos = i < chords.size() - 1 ? chords[i + 1]->position : static_cast<float>(fabricator->getSegment()->total);
  }
  return sections;
}

void Craft::craftNoteEventSection(
    const float tempo,
    const SegmentChoice *choice,
    const float fromPos,
    const float maxPos,
    NoteRange *range,
    const bool defaultAtonal) const {

  // begin at the beginning and fabricate events for the segment from beginning to end
  float curPos = fromPos;

  // choose loop patterns until arrive at the out point or end of segment
  while (curPos < maxPos) {
    std::optional<const ProgramSequencePattern *> loopPattern =
        fabricator->getRandomlySelectedPatternOfSequenceByVoiceAndType(choice);
    if (loopPattern.has_value())
      curPos += craftPatternEvents(tempo, choice, loopPattern.value(), curPos, maxPos, range, defaultAtonal);
    else
      curPos = maxPos;
  }
}

float Craft::craftPatternEvents(
    const float tempo,
    const SegmentChoice *choice,
    const ProgramSequencePattern *pattern,
    const float fromPosition,
    const float toPosition,
    NoteRange *range,
    const bool defaultAtonal) const {
  const float loopBeats = toPosition - fromPosition;
  const std::vector<const ProgramSequencePatternEvent *> events =
      fabricator->getSourceMaterial()->getEventsOfPattern(pattern);

  auto arrangement = SegmentChoiceArrangement();
  arrangement.id = EntityUtils::computeUniqueId();
  arrangement.segmentId = choice->segmentId;
  arrangement.segmentChoiceId = choice->id;
  arrangement.programSequencePatternId = pattern->id;
  fabricator->put(arrangement);

  const auto instrument = fabricator->getSourceMaterial()->getInstrument(choice->instrumentId);
  if (!instrument.has_value())
    throw FabricationException("Failed to retrieve instrument");
  for (const ProgramSequencePatternEvent *event: events)
    pickNotesAndInstrumentAudioForEvent(
        tempo,
        instrument.value(),
        choice,
        &arrangement,
        fromPosition,
        toPosition,
        event,
        range,
        defaultAtonal);
  return std::min(loopBeats, static_cast<float>(pattern->total));
}

void Craft::pickNotesAndInstrumentAudioForEvent(
    const float tempo,
    const Instrument *instrument,
    const SegmentChoice *choice,
    const SegmentChoiceArrangement *arrangement,
    const float fromPosition,
    const float toPosition,
    const ProgramSequencePatternEvent *event,
    NoteRange *range,
    const bool defaultAtonal) const {
  // Segment position is expressed in beats
  const float segmentPosition = fromPosition + event->position;

  // Should never place segment events outside of segment time range
  if (segmentPosition < 0 || segmentPosition >= static_cast<float>(fabricator->getSegment()->total)) return;

  const float duration = std::min(event->duration, toPosition - segmentPosition);
  const auto chord = fabricator->getChordAt(segmentPosition);
  std::optional<SegmentChordVoicing *> voicing = chord.has_value()
                                                     ? fabricator->chooseVoicing(chord.value(), instrument->type)
                                                     : std::nullopt;

  const auto volRatio = computeVolumeRatioForPickedNote(*choice, segmentPosition);
  if (0 >= volRatio) return;

  // The note is voiced from the chord voicing (if found) or else the default is used
  const std::set<std::string> notes = chord.value() && voicing.has_value()
                                          ? pickNotesForEvent(instrument->type, choice, event, chord.value(), voicing.value(), range)
                                          : (defaultAtonal ? std::set<std::string>{"ATONAL"}
                                                           : std::set<std::string>{});

  // Pick attributes are expressed "rendered" as actual seconds
  const long startAtSegmentMicros = fabricator->getSegmentMicrosAtPosition(tempo, segmentPosition);
  const std::optional<long> lengthMicros = fabricator->isOneShot(instrument, fabricator->getTrackName(event))
                                               ? std::nullopt
                                               : std::optional<long>(
                                                     fabricator->getSegmentMicrosAtPosition(tempo, segmentPosition + duration) -
                                                     startAtSegmentMicros);

  // pick an audio for each note
  for (auto &note: notes)
    pickInstrumentAudio(note, *instrument, *event, *arrangement, startAtSegmentMicros, lengthMicros,
                        voicing.has_value() ? std::optional(voicing.value()->id) : std::nullopt, volRatio);
}

void Craft::finalizeNoteEventCutoffsOfOneShotInstrumentAudioPicks(const SegmentChoice *choice) {
  if (!fabricator->getSourceMaterial()->getInstrument(choice->instrumentId).has_value()) {
    throw FabricationException("Failed to get instrument from source material for segment choice!");
  }
  const auto instrument = fabricator->getSourceMaterial()->getInstrument(choice->instrumentId).value();

  // skip instruments that are not one-shot
  if (!fabricator->isOneShot(instrument)) return;

  // skip instruments that are do not have one-shot cutoff enabled https://github.com/xjmusic/xjmusic/issues/225
  if (!fabricator->isOneShotCutoffEnabled(instrument)) return;

  // skip instruments that are not on the list
  if (finalizeAudioLengthsForInstrumentTypes.find(instrument->type) ==
      finalizeAudioLengthsForInstrumentTypes.end())
    return;

  // get all the picks, ordered chronologically, and skip the rest of this process if there are none
  std::vector<SegmentChoiceArrangementPick *> picks = fabricator->getPicks(choice);
  if (picks.empty()) return;

  // build an ordered unique list of the moments in time when the one-shot will be cut off
  std::vector<long> cutoffAtSegmentMicros(picks.size());
  std::transform(picks.begin(), picks.end(), cutoffAtSegmentMicros.begin(),
                 [](const SegmentChoiceArrangementPick *pick) { return pick->startAtSegmentMicros; });

  // iterate and set lengths of all picks in series
  for (const auto pick: picks) {

    // Skip picks that already have their end length set
    if (0 < pick->lengthMicros) continue;

    auto it = std::find_if(cutoffAtSegmentMicros.begin(), cutoffAtSegmentMicros.end(),
                           [&pick](const auto &c) { return c > pick->startAtSegmentMicros; });

    std::optional<long> nextCutoffAtSegmentMicros;
    if (it != cutoffAtSegmentMicros.end()) {
      nextCutoffAtSegmentMicros = *it;
    }

    if (nextCutoffAtSegmentMicros.has_value()) {
      pick->lengthMicros = nextCutoffAtSegmentMicros.value() - pick->startAtSegmentMicros;
      fabricator->put(*pick);
      continue;
    }

    if (pick->startAtSegmentMicros < fabricator->getTotalSegmentMicros()) {
      pick->lengthMicros = fabricator->getTotalSegmentMicros() - pick->startAtSegmentMicros;
      fabricator->put(*pick);
      continue;
    }

    fabricator->deletePick(pick->id);
  }
}

float Craft::computeVolumeRatioForPickedNote(const SegmentChoice &choice, float segmentPosition) const {
  if (!fabricator->getTemplateConfig().deltaArcEnabled) return 1.0f;
  return static_cast<float>(inBounds(choice.deltaIn, choice.deltaOut, static_cast<float>(fabricator->getSegment()->delta) + segmentPosition)
                                ? 1.0
                                : 0.0);
}

std::set<std::string> Craft::pickNotesForEvent(
    Instrument::Type instrumentType,
    const SegmentChoice *choice,
    const ProgramSequencePatternEvent *event,
    const SegmentChord *rawSegmentChord,
    const SegmentChordVoicing *voicing,
    NoteRange *optimalRange) const {
  // Various computations to prepare for picking
  auto segChord = Chord::of(rawSegmentChord->name);
  auto dpKey = fabricator->getKeyForChoice(choice);
  auto dpRange = fabricator->getProgramRange(choice->programId, instrumentType);
  auto voicingListRange = fabricator->getProgramVoicingNoteRange(instrumentType);

  // take semitone shift into project before computing octave shift! https://github.com/xjmusic/xjmusic/issues/245
  auto dpTransposeSemitones = fabricator->getProgramTargetShift(instrumentType, &dpKey, &segChord);
  auto sourceRange = dpRange.shifted(dpTransposeSemitones);
  auto dpTransposeOctaveSemitones = 12 * fabricator->getProgramRangeShiftOctaves(instrumentType, &sourceRange, &voicingListRange);

  // Event notes are either interpreted from specific notes in dp, or via sticky bun from X notes in dp
  std::vector<std::string> eventTones = CsvUtils::split(event->tones);
  std::vector<Note> eventNotes;
  eventNotes.reserve(eventTones.size());
  for (const auto &n: eventTones) {
    eventNotes.emplace_back(Note::of(n).shift(dpTransposeSemitones + dpTransposeOctaveSemitones));
  }
  std::sort(eventNotes.begin(), eventNotes.end());

  auto dpEventRelativeOffsetWithinRangeSemitones = dpRange.shifted(
                                                              dpTransposeSemitones + dpTransposeOctaveSemitones)
                                                       .getDeltaSemitones(NoteRange::ofNotes(eventNotes));
  auto dpEventRangeWithinWholeDP = NoteRange::ofNotes(eventNotes).shifted(dpEventRelativeOffsetWithinRangeSemitones);

  if (optimalRange->empty() && !dpEventRangeWithinWholeDP.empty())
    optimalRange->expand(&dpEventRangeWithinWholeDP);

  // Leverage segment meta to look up a sticky bun if it exists
  auto bun = fabricator->getStickyBun(event->id);

  // Prepare voicing notes and note picker
  std::vector<Note> voicingNotes;
  for (const auto &note: Fabricator::getNotes(voicing)) {
    if (Note::isValid(note)) {
      voicingNotes.emplace_back(static_cast<Note>(note));
    }
  }
  auto notePicker = NotePicker(
      optimalRange->shifted(dpEventRelativeOffsetWithinRangeSemitones),
      voicingNotes,
      fabricator->getTemplateConfig().instrumentTypesForInversionSeekingContains(instrumentType));

  // Go through the notes in the event and pick a note from the voicing, either by note picker or by sticky bun
  std::vector<Note> pickedNotes;
  for (size_t i = 0; i < eventNotes.size(); i++) {
    Note pickedNote = eventNotes[i].isAtonal() && bun.has_value()
                          ? bun.value().compute(voicingNotes, static_cast<int>(i))
                          : notePicker.pick(eventNotes[i]);
    pickedNotes.push_back(pickedNote);
  }

  std::set<std::string> pickedNoteStrings;
  for (const auto &note: pickedNotes) {
    pickedNoteStrings.insert(note.toString(segChord.accidental));
  }

  // expand the optimal range for voice leading by the notes that were just picked
  optimalRange->expand(pickedNotes);

  // outcome
  return pickedNoteStrings;
}

void Craft::pickInstrumentAudio(
    const std::string &note,
    const Instrument &instrument,
    const ProgramSequencePatternEvent &event,
    const SegmentChoiceArrangement &segmentChoiceArrangement,
    const long startAtSegmentMicros,
    const std::optional<long> lengthMicros,
    const std::optional<UUID> &segmentChordVoicingId,
    const float volRatio) const {
  const auto audio = fabricator->getInstrumentConfig(&instrument).isMultiphonic
                         ? selectMultiphonicInstrumentAudio(&instrument, &event, note)
                         : selectMonophonicInstrumentAudio(&instrument, &event);

  // Should gracefully skip audio if unfulfilled by instrument https://github.com/xjmusic/xjmusic/issues/240
  if (!audio.has_value()) return;

  // of pick
  const auto pick = SegmentChoiceArrangementPick();
  pick->id = EntityUtils::computeUniqueId();
  pick->segmentId = segmentChoiceArrangement.segmentId;
  pick->segmentChoiceArrangementId = segmentChoiceArrangement.id;
  pick->instrumentAudioId = audio.value()->id;
  pick->programSequencePatternEventId = event.id;
  pick->event = fabricator->getTrackName(&event);
  pick->startAtSegmentMicros = startAtSegmentMicros;
  if (lengthMicros.has_value()) pick->lengthMicros = lengthMicros.value();
  pick->amplitude = event.velocity * volRatio;
  pick->tones = fabricator->getInstrumentConfig(&instrument).isTonal ? note : Note::ATONAL;
  if (segmentChordVoicingId.has_value()) pick->segmentChordVoicingId = segmentChordVoicingId.value();
  fabricator->put(*pick);
}

std::optional<const InstrumentAudio *>
Craft::selectMultiphonicInstrumentAudio(
    const Instrument *instrument,
    const ProgramSequencePatternEvent *event,
    const std::string &note) const {
  if (fabricator->getInstrumentConfig(instrument).isAudioSelectionPersistent) {
    if (!fabricator->getPreferredAudio(event->programVoiceTrackId, note).has_value()) {
      const auto audio = selectNewMultiphonicInstrumentAudio(instrument, note);
      if (audio.has_value()) {
        fabricator->putPreferredAudio(event->programVoiceTrackId, note, audio.value());
      }
    }
    return fabricator->getPreferredAudio(event->programVoiceTrackId, note);

  } else {
    return selectNewMultiphonicInstrumentAudio(instrument, note);
  }
}

std::optional<const InstrumentAudio *>
Craft::selectMonophonicInstrumentAudio(const Instrument *instrument, const ProgramSequencePatternEvent *event) const {
  if (fabricator->getInstrumentConfig(instrument).isAudioSelectionPersistent) {
    if (!fabricator->getPreferredAudio(event->programVoiceTrackId, event->tones).has_value()) {
      const auto selection = selectNewNoteEventInstrumentAudio(instrument, event);
      if (!selection.has_value()) throw FabricationException("Unable to select note event instrument audio!");
      fabricator->putPreferredAudio(event->programVoiceTrackId, event->tones, selection.value());
    }
    return fabricator->getPreferredAudio(event->programVoiceTrackId, event->tones);

  } else {
    return selectNewNoteEventInstrumentAudio(instrument, event);
  }
}

std::optional<const InstrumentAudio *>
Craft::selectChordPartInstrumentAudio(const Instrument *instrument, const Chord &chord) const {
  if (fabricator->getInstrumentConfig(instrument).isAudioSelectionPersistent) {
    if (!fabricator->getPreferredAudio(instrument->id, chord.getName()).has_value()) {

      const auto audio = selectNewChordPartInstrumentAudio(instrument, chord);
      if (audio.has_value()) {
        fabricator->putPreferredAudio(instrument->id, chord.getName(), audio.value());
      }
    }
    return fabricator->getPreferredAudio(instrument->id, chord.getName());

  } else {
    return selectNewChordPartInstrumentAudio(instrument, chord);
  }
}

std::optional<const InstrumentAudio *>
Craft::selectNewNoteEventInstrumentAudio(const Instrument *instrument, const ProgramSequencePatternEvent *event) const {
  std::map<UUID, int> score;

  // add all audio to chooser
  for (auto a: fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument))
    score.emplace(a->id, 0);

  // score each audio against the current voice event, with some variability
  for (const auto audio: fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument))
    if (instrument->type == Instrument::Type::Drum)
      score.emplace(audio->id, fabricator->getTrackName(event) == audio->event ? 300 : 0);
    else if (Note::of(audio->tones) == Note::of(event->tones))
      score.emplace(audio->id, 100);

  // chosen audio event
  auto pickId = ValueUtils::getKeyOfHighestValue(score);
  if (!pickId.has_value()) return std::nullopt;
  auto audio = fabricator->getSourceMaterial()->getInstrumentAudio(pickId.value());
  if (!audio.has_value()) return std::nullopt;
  return {audio.value()};
}

std::optional<const InstrumentAudio *> Craft::selectNewMultiphonicInstrumentAudio(const Instrument *instrument, std::string note) const {
  const auto instrumentAudios = fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument);
  const auto a = Note::of(note);
  std::vector<const InstrumentAudio *> filteredAudios;
  for (const auto candidate: instrumentAudios) {
    if (!candidate->tones.empty()) {
      auto b = Note::of(candidate->tones);
      if (a.isAtonal() || b.isAtonal() || a == b) {
        filteredAudios.emplace_back(candidate);
      }
    }
  }

  if (filteredAudios.empty()) {
    std::vector<Note> availableNotes;
    for (auto audio: instrumentAudios) {
      availableNotes.push_back(Note::of(audio->tones));
    }
    std::sort(availableNotes.begin(), availableNotes.end());
    std::vector<std::string> availableNoteNames;
    for (auto N: availableNotes) {
      availableNoteNames.push_back(N.toString(Accidental::Sharp));
    }
    std::map<std::string, std::string> reportMissingData = {
        {"instrumentId", instrument->id},
        {"searchForNote", note},
        {"availableNotes", CsvUtils::join(availableNoteNames)}};
    reportMissing(reportMissingData);
    return std::nullopt;
  }

  auto audio = filteredAudios[MarbleBag::quickPick(static_cast<int>(filteredAudios.size()))];
  return {audio};
}

bool Craft::instrumentContainsAudioEventsLike(
    const Instrument &instrument,
    const std::set<std::string> &requireEvents) const {
  if (requireEvents.empty()) return true;
  for (auto event: requireEvents) {
    auto audios = fabricator->getSourceMaterial()->getAudiosOfInstrument(instrument.id);
    if (!std::any_of(audios.begin(), audios.end(), [&event](const InstrumentAudio *a) {
          return event == a->event;
        }))
      return false;
  }
  return true;
}

/**
 * This is the default implementation of the ChoiceIndexProvider interface.
 * It should be replaced by a custom Lambda implementation when used
 */
std::string Craft::ChoiceIndexProvider::get(const SegmentChoice *choice) {
  return "";
}

/**
 * This is the default implementation of the InstrumentProvider interface.
 * It should be replaced by a custom Lambda implementation when used
 */
std::optional<Instrument> Craft::InstrumentProvider::get(const ProgramVoice *voice) {
  return std::nullopt;
}
