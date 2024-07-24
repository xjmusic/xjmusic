// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <memory>
#include <optional>
#include <set>
#include <vector>
#include <iostream>

#include <nlohmann/json.hpp>
#include "xjmusic/content/ContentEntityStore.h"

using namespace XJ;

using json = nlohmann::json;

namespace XJ {

  /**
   * Parse a ContentEntityStore from a JSON object
   * @param json  input
   * @param store  output
   */
  void from_json(const json &json, ContentEntityStore &store) {
    if (json.contains("instruments"))
      store.setInstruments(
          json.at("instruments").get<std::set<Instrument>>());
    if (json.contains("instrumentAudios"))
      store.setInstrumentAudios(
          json.at("instrumentAudios").get<std::set<InstrumentAudio>>());
    if (json.contains("instrumentMemes"))
      store.setInstrumentMemes(
          json.at("instrumentMemes").get<std::set<InstrumentMeme>>());
    if (json.contains("libraries"))
      store.setLibraries(
          json.at("libraries").get<std::set<Library>>());
    if (json.contains("programs"))
      store.setPrograms(
          json.at("programs").get<std::set<Program>>());
    if (json.contains("programMemes"))
      store.setProgramMemes(
          json.at("programMemes").get<std::set<ProgramMeme>>());
    if (json.contains("programSequences"))
      store.setProgramSequences(
          json.at("programSequences").get<std::set<ProgramSequence>>());
    if (json.contains("programSequenceBindings"))
      store.setProgramSequenceBindings(
          json.at("programSequenceBindings").get<std::set<ProgramSequenceBinding>>());
    if (json.contains("programSequenceBindingMemes"))
      store.setProgramSequenceBindingMemes(
          json.at("programSequenceBindingMemes").get<std::set<ProgramSequenceBindingMeme>>());
    if (json.contains("programSequenceChords"))
      store.setProgramSequenceChords(
          json.at("programSequenceChords").get<std::set<ProgramSequenceChord>>());
    if (json.contains("programSequenceChordVoicings"))
      store.setProgramSequenceChordVoicings(
          json.at("programSequenceChordVoicings").get<std::set<ProgramSequenceChordVoicing>>());
    if (json.contains("programSequencePatterns"))
      store.setProgramSequencePatterns(
          json.at("programSequencePatterns").get<std::set<ProgramSequencePattern>>());
    if (json.contains("programSequencePatternEvents"))
      store.setProgramSequencePatternEvents(
          json.at("programSequencePatternEvents").get<std::set<ProgramSequencePatternEvent>>());
    if (json.contains("programVoices"))
      store.setProgramVoices(
          json.at("programVoices").get<std::set<ProgramVoice>>());
    if (json.contains("programVoiceTracks"))
      store.setProgramVoiceTracks(
          json.at("programVoiceTracks").get<std::set<ProgramVoiceTrack>>());
    if (json.contains("templates"))
      store.setTemplates(
          json.at("templates").get<std::set<Template>>());
    if (json.contains("templateBindings"))
      store.setTemplateBindings(
          json.at("templateBindings").get<std::set<TemplateBinding>>());
    if (json.contains("project"))
      store.setProjects({json.at("project").get<Project>()});
  }
}// namespace XJ

#define CONTENT_STORE_CORE_METHODS(ENTITY, ENTITIES, STORE)                                \
  std::optional<const ENTITY *> ContentEntityStore::get##ENTITY(const UUID &id) {          \
    try {                                                                                  \
      if (STORE.count(id) == 0) {                                                          \
        return std::nullopt;                                                               \
      }                                                                                    \
      return &STORE.at(id);                                                                \
    } catch (...) {                                                                        \
      return std::nullopt;                                                                 \
    }                                                                                      \
  }                                                                                        \
  std::set<const ENTITY *> ContentEntityStore::get##ENTITIES() {                           \
    try {                                                                                  \
      std::set<const ENTITY *> result;                                                     \
      for (const auto &entry: STORE) {                                                     \
        result.emplace(&entry.second);                                                     \
      }                                                                                    \
      return result;                                                                       \
    } catch (...) {                                                                        \
      return {};                                                                           \
    }                                                                                      \
  }                                                                                        \
  ContentEntityStore ContentEntityStore::set##ENTITIES(const std::set<ENTITY> &entities) { \
    try {                                                                                  \
      STORE.clear();                                                                       \
      for (const auto &entity: entities) {                                                 \
        STORE[entity.id] = std::move(entity);                                              \
      }                                                                                    \
    } catch (const std::exception &e) {                                                    \
      std::cout << "Error putting all " << #ENTITY << ": " << e.what() << std::endl;       \
    }                                                                                      \
    return *this;                                                                          \
  }                                                                                        \
  ENTITY *ContentEntityStore::put(const ENTITY &entity) {                                  \
    try {                                                                                  \
      STORE[entity.id] = std::move(entity);                                                \
      return &STORE[entity.id];                                                            \
    } catch (const std::exception &e) {                                                    \
      std::cout << "Error putting " << #ENTITY << ": " << e.what() << std::endl;           \
      return nullptr;                                                                      \
    }                                                                                      \
  }


CONTENT_STORE_CORE_METHODS(Instrument, Instruments, instruments)

CONTENT_STORE_CORE_METHODS(InstrumentAudio, InstrumentAudios, instrumentAudios)

CONTENT_STORE_CORE_METHODS(InstrumentMeme, InstrumentMemes, instrumentMemes)

CONTENT_STORE_CORE_METHODS(Library, Libraries, libraries)

CONTENT_STORE_CORE_METHODS(Program, Programs, programs)

CONTENT_STORE_CORE_METHODS(ProgramMeme, ProgramMemes, programMemes)

CONTENT_STORE_CORE_METHODS(ProgramSequence, ProgramSequences, programSequences)

CONTENT_STORE_CORE_METHODS(ProgramSequenceBinding, ProgramSequenceBindings, programSequenceBindings)

CONTENT_STORE_CORE_METHODS(ProgramSequenceBindingMeme, ProgramSequenceBindingMemes, programSequenceBindingMemes)

CONTENT_STORE_CORE_METHODS(ProgramSequenceChord, ProgramSequenceChords, programSequenceChords)

CONTENT_STORE_CORE_METHODS(ProgramSequenceChordVoicing, ProgramSequenceChordVoicings, programSequenceChordVoicings)

CONTENT_STORE_CORE_METHODS(ProgramSequencePattern, ProgramSequencePatterns, programSequencePatterns)

CONTENT_STORE_CORE_METHODS(ProgramSequencePatternEvent, ProgramSequencePatternEvents, programSequencePatternEvents)

CONTENT_STORE_CORE_METHODS(ProgramVoice, ProgramVoices, programVoices)

CONTENT_STORE_CORE_METHODS(ProgramVoiceTrack, ProgramVoiceTracks, programVoiceTracks)

CONTENT_STORE_CORE_METHODS(Project, Projects, projects)

CONTENT_STORE_CORE_METHODS(Template, Templates, templates)

CONTENT_STORE_CORE_METHODS(TemplateBinding, TemplateBindings, templateBindings)

std::optional<Project *> ContentEntityStore::getProject() {
  if (projects.empty()) return std::nullopt;
  return &projects.begin()->second;
}

std::optional<const ProgramVoiceTrack *> ContentEntityStore::getTrackOfEvent(const ProgramSequencePatternEvent *event) {
  return getProgramVoiceTrack(event->programVoiceTrackId);
}

std::optional<const ProgramVoice *> ContentEntityStore::getVoiceOfEvent(const ProgramSequencePatternEvent *event) {
  const auto track = getTrackOfEvent(event);
  if (!track.has_value()) return std::nullopt;
  return getProgramVoice(track.value()->programVoiceId);
}

Instrument::Type ContentEntityStore::getInstrumentTypeOfEvent(const ProgramSequencePatternEvent *event) {
  const auto voiceOfEvent = getVoiceOfEvent(event);
  if (!voiceOfEvent.has_value()) {
    throw std::runtime_error("Can't get Program Voice!");
  }
  return voiceOfEvent.value()->type;
}

bool ContentEntityStore::hasInstrumentsOfType(Instrument::Type type) {
  return std::any_of(instruments.begin(), instruments.end(),
                     [type](const auto &pair) { return pair.second.type == type; });
}

bool ContentEntityStore::hasInstrumentsOfMode(Instrument::Mode mode) {
  return std::any_of(instruments.begin(), instruments.end(),
                     [mode](const auto &pair) { return pair.second.mode == mode; });
}

bool ContentEntityStore::hasInstrumentsOfTypeAndMode(Instrument::Type type, Instrument::Mode mode) {
  return std::any_of(instruments.begin(), instruments.end(),
                     [type, mode](const auto &pair) {
                       return pair.second.type == type && pair.second.mode == mode;
                     });
}

std::vector<int> ContentEntityStore::getAvailableOffsets(const ProgramSequenceBinding *sequenceBinding) const {
  std::vector<int> offsets;
  for (const auto &[_, binding]: programSequenceBindings) {
    if (binding.programId == sequenceBinding->programId) {
      offsets.push_back(binding.offset);
    }
  }

  // Remove duplicates
  std::sort(offsets.begin(), offsets.end());
  offsets.erase(std::unique(offsets.begin(), offsets.end()), offsets.end());

  return offsets;
}

std::set<const InstrumentAudio *> ContentEntityStore::getAudiosOfInstrument(const UUID &id) const {
  std::set<const InstrumentAudio *> result;
  for (const auto &[_, audio]: instrumentAudios) {
    if (audio.instrumentId == id) {
      result.emplace(&audio);
    }
  }
  return result;
}

std::set<const InstrumentAudio *> ContentEntityStore::getAudiosOfInstrument(const Instrument *instrument) const {
  return getAudiosOfInstrument(instrument->id);
}

std::vector<const ProgramSequenceBinding *>
ContentEntityStore::getBindingsOfSequence(const ProgramSequence *sequence) const {
  return getBindingsOfSequence(sequence->id);
}

std::vector<const ProgramSequenceBinding *> ContentEntityStore::getBindingsOfSequence(const UUID &sequenceId) const {
  std::vector<const ProgramSequenceBinding *> result;
  for (const auto &[_, binding]: programSequenceBindings) {
    if (binding.programSequenceId == sequenceId) {
      result.emplace_back(&binding);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequenceBinding *a, const ProgramSequenceBinding *b) {
              return a->offset < b->offset;
            });
  return result;
}

std::set<const ProgramSequenceBindingMeme *>
ContentEntityStore::getSequenceBindingMemesOfProgram(const Program *program) const {
  return getSequenceBindingMemesOfProgram(program->id);
}

std::set<const ProgramSequenceBindingMeme *>
ContentEntityStore::getSequenceBindingMemesOfProgram(const UUID &programId) const {
  std::set<const ProgramSequenceBindingMeme *> result;
  for (const auto &[_, meme]: programSequenceBindingMemes) {
    if (meme.programId == programId) {
      result.emplace(&meme);
    }
  }
  return result;
}

std::vector<const ProgramSequenceBinding *>
ContentEntityStore::getBindingsAtOffsetOfProgram(const Program *program, const int offset,
                                                 const bool includeNearest) const {
  return getBindingsAtOffsetOfProgram(program->id, offset, includeNearest);
}

std::vector<const ProgramSequenceBinding *>
ContentEntityStore::getBindingsAtOffsetOfProgram(const UUID &programId, const int offset,
                                                 const bool includeNearest) const {
  std::vector<const ProgramSequenceBinding *> result;
  std::vector<const ProgramSequenceBinding *> candidates;

  // Filter the programSequenceBindings map to get the candidates
  for (const auto &[_, binding]: programSequenceBindings) {
    if (binding.programId == programId) {
      candidates.emplace_back(&binding);
    }
  }

  if (includeNearest) {
    // Find the actual offset
    int minDifference = std::numeric_limits<int>::max();
    int actualOffset = offset;
    for (const auto &candidate: candidates) {
      const int difference = std::abs(candidate->offset - offset);
      if (difference < minDifference) {
        minDifference = difference;
        actualOffset = candidate->offset;
      }
    }

    // Filter the candidates based on the actual offset
    for (const auto &candidate: candidates) {
      if (candidate->offset == actualOffset) {
        result.push_back(candidate);
      }
    }
  } else {
    // Filter the candidates based on the given offset
    for (const auto &candidate: candidates) {
      if (candidate->offset == offset) {
        result.push_back(candidate);
      }
    }
  }

  std::sort(result.begin(), result.end(),
            [](const ProgramSequenceBinding *a, const ProgramSequenceBinding *b) {
              return a->offset < b->offset;
            });
  return result;
}

std::vector<const ProgramSequenceChord *>
ContentEntityStore::getChordsOfSequence(const ProgramSequence *sequence) const {
  return getChordsOfSequence(sequence->id);
}

std::vector<const ProgramSequenceChord *> ContentEntityStore::getChordsOfSequence(const UUID &programSequenceId) const {
  std::vector<const ProgramSequenceChord *> result;
  for (const auto &[_, chord]: programSequenceChords) {
    if (chord.programSequenceId == programSequenceId) {
      result.emplace_back(&chord);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequenceChord *a, const ProgramSequenceChord *b) {
              return a->position < b->position;
            });
  return result;
}

std::vector<const ProgramSequencePatternEvent *>
ContentEntityStore::getEventsOfPattern(const ProgramSequencePattern *pattern) const {
  return getEventsOfPattern(pattern->id);
}

std::vector<const ProgramSequencePatternEvent *> ContentEntityStore::getEventsOfPattern(const UUID &patternId) const {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &[_, event]: programSequencePatternEvents) {
    if (event.programSequencePatternId == patternId) {
      result.emplace_back(&event);
    }
  }
  // Sort the result set based on the position
  std::sort(result.begin(), result.end(),
            [](const ProgramSequencePatternEvent *a, const ProgramSequencePatternEvent *b) {
              return a->position < b->position;
            });
  return result;
}

std::set<const ProgramSequencePattern *>
ContentEntityStore::getPatternsOfSequenceAndVoice(const UUID &programSequenceId, const UUID &programVoiceId) const {
  std::set<const ProgramSequencePattern *> result;
  for (const auto &[_, pattern]: programSequencePatterns) {
    if (pattern.programSequenceId == programSequenceId && pattern.programVoiceId == programVoiceId) {
      result.emplace(&pattern);
    }
  }
  return result;
}

std::set<const TemplateBinding *> ContentEntityStore::getBindingsOfTemplate(const UUID &templateId) const {
  std::set<const TemplateBinding *> result;
  for (const auto &[_, binding]: templateBindings) {
    if (binding.templateId == templateId) {
      result.emplace(&binding);
    }
  }
  return result;
}


std::set<const ProgramSequencePattern *> ContentEntityStore::getSequencePatternsOfProgram(const UUID &programId) const {
  std::set<const ProgramSequencePattern *> result;
  for (const auto &[_, pattern]: programSequencePatterns) {
    if (pattern.programId == programId) {
      result.emplace(&pattern);
    }
  }
  return result;
}

std::set<const ProgramSequencePattern *>
ContentEntityStore::getSequencePatternsOfProgram(const Program *program) const {
  return getSequencePatternsOfProgram(program->id);
}

std::vector<const ProgramSequencePatternEvent *>
ContentEntityStore::getSequencePatternEventsOfProgram(const UUID &programId) const {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &[_, event]: programSequencePatternEvents) {
    if (event.programId == programId) {
      result.emplace_back(&event);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequencePatternEvent *a, const ProgramSequencePatternEvent *b) {
              return a->position < b->position;
            });
  return result;
}

std::vector<const ProgramSequencePatternEvent *>
ContentEntityStore::getEventsOfTrack(const ProgramVoiceTrack *track) const {
  return getEventsOfTrack(track->id);
}

std::vector<const ProgramSequencePatternEvent *> ContentEntityStore::getEventsOfTrack(const UUID &trackId) const {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &[_, event]: programSequencePatternEvents) {
    if (event.programVoiceTrackId == trackId) {
      result.emplace_back(&event);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequencePatternEvent *a, const ProgramSequencePatternEvent *b) {
              return a->position < b->position;
            });
  return result;
}

std::vector<const ProgramSequencePatternEvent *>
ContentEntityStore::getEventsOfPatternAndTrack(const ProgramSequencePattern *pattern,
                                               const ProgramVoiceTrack *track) const {
  return getEventsOfPatternAndTrack(pattern->id, track->id);
}

std::vector<const ProgramSequencePatternEvent *>
ContentEntityStore::getEventsOfPatternAndTrack(const UUID &patternId, const UUID &trackId) const {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &[_, event]: programSequencePatternEvents) {
    if (event.programSequencePatternId == patternId && event.programVoiceTrackId == trackId) {
      result.emplace_back(&event);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequencePatternEvent *a, const ProgramSequencePatternEvent *b) {
              return a->position < b->position;
            });
  return result;
}

std::set<const InstrumentAudio *>
ContentEntityStore::getAudiosOfInstrumentTypesAndModes(const std::set<Instrument::Type> &types,
                                                       const std::set<Instrument::Mode> &modes) const {
  std::set<const InstrumentAudio *> result;
  for (const auto &audio: getInstrumentsOfTypesAndModes(types, modes)) {
    auto audios = getAudiosOfInstrument(audio->id);
    result.insert(audios.begin(), audios.end());
  }
  return result;
}

std::set<const InstrumentAudio *>
ContentEntityStore::getAudiosOfInstrumentTypes(const std::set<Instrument::Type> &types) const {
  std::set<const InstrumentAudio *> result;
  for (const auto &audio: getInstrumentsOfTypes(types)) {
    auto audios = getAudiosOfInstrument(audio->id);
    result.insert(audios.begin(), audios.end());
  }
  return result;
}

std::set<const Instrument *> ContentEntityStore::getInstrumentsOfType(const Instrument::Type &type) const {
  std::set<const Instrument *> result;
  for (const auto &[_, instrument]: instruments) {
    if (instrument.type == type) {
      result.emplace(&instrument);
    }
  }
  return result;
}

std::set<const Instrument *>
ContentEntityStore::getInstrumentsOfTypesAndModes(const std::set<Instrument::Type> &types,
                                                  const std::set<Instrument::Mode> &modes) const {
  std::set<const Instrument *> result;
  for (const auto &[_, instrument]: instruments) {
    if (types.empty() || types.find(instrument.type) != types.end()) {
      if (modes.empty() || modes.find(instrument.mode) != modes.end()) {
        result.emplace(&instrument);
      }
    }
  }
  return result;
}

std::set<const Instrument *> ContentEntityStore::getInstrumentsOfTypes(const std::set<Instrument::Type> &types) const {
  std::set<const Instrument *> result;
  for (const auto &[_, instrument]: instruments) {
    if (types.empty() || types.find(instrument.type) != types.end()) {
      result.emplace(&instrument);
    }
  }
  return result;
}

std::set<const InstrumentMeme *> ContentEntityStore::getMemesOfInstrument(const UUID &instrumentId) const {
  std::set<const InstrumentMeme *> result;
  for (const auto &[_, meme]: instrumentMemes) {
    if (meme.instrumentId == instrumentId) {
      result.emplace(&meme);
    }
  }
  return result;
}

std::set<const Instrument *> ContentEntityStore::getInstrumentsOfLibrary(const Library *library) const {
  return getInstrumentsOfLibrary(library->id);
}

std::set<const Instrument *> ContentEntityStore::getInstrumentsOfLibrary(const UUID &libraryId) const {
  std::set<const Instrument *> result;
  for (const auto &[_, instrument]: instruments) {
    if (instrument.libraryId == libraryId) {
      result.emplace(&instrument);
    }
  }
  return result;
}

Instrument::Type ContentEntityStore::getInstrumentTypeOfAudio(const UUID &instrumentAudioId) const {
  if (!instrumentAudios.count(instrumentAudioId)) throw std::runtime_error("Can't find Instrument Audio!");
  const auto audio = instrumentAudios.at(instrumentAudioId);
  if (!instruments.count(audio.instrumentId)) throw std::runtime_error("Can't find Instrument!");
  return instruments.at(audio.instrumentId).type;
}

std::set<const ProgramMeme *> ContentEntityStore::getMemesOfProgram(const UUID &programId) const {
  std::set<const ProgramMeme *> result;
  for (const auto &[_, meme]: programMemes) {
    if (meme.programId == programId) {
      result.emplace(&meme);
    }
  }
  return result;
}

std::set<std::string> ContentEntityStore::getMemesAtBeginning(const Program *program) const {
  std::vector<std::string> result;

  // add program memes
  for (const auto meme: getMemesOfProgram(program->id))
    result.emplace_back(meme->name);

  // add sequence binding memes
  for (const ProgramSequenceBinding *sequenceBinding: getBindingsAtOffsetOfProgram(program, 0, false))
    for (const auto meme: getMemesOfSequenceBinding(sequenceBinding->id))
      result.emplace_back(meme->name);

  // Remove duplicates
  std::sort(result.begin(), result.end());
  result.erase(std::unique(result.begin(), result.end()), result.end());

  return {result.begin(), result.end()};
}

std::set<const ProgramSequenceBindingMeme *>
ContentEntityStore::getMemesOfSequenceBinding(const ProgramSequenceBinding *programSequenceBinding) const {
  return getMemesOfSequenceBinding(programSequenceBinding->id);
}

std::set<const ProgramSequenceBindingMeme *>
ContentEntityStore::getMemesOfSequenceBinding(const UUID &programSequenceBindingId) const {
  std::set<const ProgramSequenceBindingMeme *> result;
  for (const auto &[_, meme]: programSequenceBindingMemes) {
    if (meme.programSequenceBindingId == programSequenceBindingId) {
      result.emplace(&meme);
    }
  }
  return result;
}

UUID ContentEntityStore::getPatternIdOfEvent(const UUID &eventId) {
  if (!programSequencePatternEvents.count(eventId)) throw std::runtime_error("Can't find Event!");
  return programSequencePatternEvents.at(eventId).programSequencePatternId;
}

std::set<const ProgramSequencePattern *> ContentEntityStore::getPatternsOfSequence(const ProgramSequence *sequence) {
  return getPatternsOfSequence(sequence->id);
}

std::set<const ProgramSequencePattern *> ContentEntityStore::getPatternsOfSequence(const UUID &sequence) {
  std::set<const ProgramSequencePattern *> result;
  for (const auto &[_, pattern]: programSequencePatterns) {
    if (pattern.programSequenceId == sequence) {
      result.emplace(&pattern);
    }
  }
  return result;
}

std::set<const ProgramSequencePattern *> ContentEntityStore::getPatternsOfVoice(const ProgramVoice *voice) const {
  return getPatternsOfVoice(voice->id);
}

std::set<const ProgramSequencePattern *> ContentEntityStore::getPatternsOfVoice(const UUID &voiceId) const {
  std::set<const ProgramSequencePattern *> result;
  for (const auto &[_, pattern]: programSequencePatterns) {
    if (pattern.programVoiceId == voiceId) {
      result.emplace(&pattern);
    }
  }
  return result;
}

std::set<const Program *> ContentEntityStore::getProgramsOfLibrary(const Library *library) const {
  return getProgramsOfLibrary(library->id);
}

std::set<const Program *> ContentEntityStore::getProgramsOfLibrary(const UUID &libraryId) const {
  std::set<const Program *> result;
  for (const auto &[_, program]: programs) {
    if (program.libraryId == libraryId) {
      result.emplace(&program);
    }
  }
  return result;
}

std::set<const Program *> ContentEntityStore::getProgramsOfType(const Program::Type type) const {
  std::set<const Program *> result;
  for (const auto &[_, program]: programs) {
    if (program.type == type) {
      result.emplace(&program);
    }
  }
  return result;
}

std::optional<const ProgramSequence *>
ContentEntityStore::getSequenceOfBinding(const ProgramSequenceBinding *sequenceBinding) {
  if (!programSequences.count(sequenceBinding->programSequenceId)) {
    return std::nullopt;
  }
  return &programSequences.at(sequenceBinding->programSequenceId);
}

std::set<const ProgramSequence *> ContentEntityStore::getSequencesOfProgram(const UUID &programId) const {
  std::set<const ProgramSequence *> result;
  for (const auto &[_, sequence]: programSequences) {
    if (sequence.programId == programId) {
      result.emplace(&sequence);
    }
  }
  return result;
}

std::vector<const ProgramSequenceBinding *>
ContentEntityStore::getSequenceBindingsOfProgram(const UUID &programId) const {
  std::vector<const ProgramSequenceBinding *> result;
  for (const auto &[_, binding]: programSequenceBindings) {
    if (binding.programId == programId) {
      result.emplace_back(&binding);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequenceBinding *a, const ProgramSequenceBinding *b) {
              return a->offset < b->offset;
            });
  return result;
}

std::vector<const ProgramSequenceChord *> ContentEntityStore::getSequenceChordsOfProgram(const UUID &programId) const {
  std::vector<const ProgramSequenceChord *> result;
  for (const auto &[_, chord]: programSequenceChords) {
    if (chord.programId == programId) {
      result.emplace_back(&chord);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequenceChord *a, const ProgramSequenceChord *b) {
              return a->position < b->position;
            });
  return result;
}

std::set<const ProgramSequenceChordVoicing *>
ContentEntityStore::getSequenceChordVoicingsOfProgram(const UUID &programId) const {
  std::set<const ProgramSequenceChordVoicing *> result;
  for (const auto &[_, voicing]: programSequenceChordVoicings) {
    if (voicing.programId == programId && Note::containsAnyValidNotes(voicing.notes)) {
      result.emplace(&voicing);
    }
  }
  return result;
}

std::set<const ProgramVoiceTrack *> ContentEntityStore::getTracksOfProgram(const UUID &programId) const {
  std::set<const ProgramVoiceTrack *> result;
  for (const auto &[_, track]: programVoiceTracks) {
    if (track.programId == programId) {
      result.emplace(&track);
    }
  }
  return result;
}

std::set<const ProgramVoiceTrack *> ContentEntityStore::getTracksOfProgramType(const Program::Type type) const {
  std::set<const ProgramVoiceTrack *> result;
  for (const auto &[_, track]: programVoiceTracks) {
    if (programs.count(track.programId) && programs.at(track.programId).type == type) {
      result.emplace(&track);
    }
  }
  return result;
}

std::set<const ProgramVoiceTrack *> ContentEntityStore::getTracksOfVoice(const ProgramVoice *voice) const {
  return getTracksOfVoice(voice->id);
}

std::set<const ProgramVoiceTrack *> ContentEntityStore::getTracksOfVoice(const UUID &voiceId) const {
  std::set<const ProgramVoiceTrack *> result;
  for (const auto &[_, track]: programVoiceTracks) {
    if (track.programVoiceId == voiceId) {
      result.emplace(&track);
    }
  }
  return result;
}

std::set<std::string> ContentEntityStore::getTrackNamesOfVoice(const ProgramVoice *voice) const {
  std::set<std::string> result;
  for (const auto &[_, track]: programVoiceTracks) {
    if (track.programVoiceId == voice->id) {
      result.emplace(track.name);
    }
  }
  return result;
}

std::set<const ProgramSequenceChordVoicing *>
ContentEntityStore::getVoicingsOfChord(const ProgramSequenceChord *chord) const {
  return getVoicingsOfChord(chord->id);
}

std::set<const ProgramSequenceChordVoicing *> ContentEntityStore::getVoicingsOfChord(const UUID &chordId) const {
  std::set<const ProgramSequenceChordVoicing *> result;
  for (const auto &[_, voicing]: programSequenceChordVoicings) {
    if (voicing.programSequenceChordId == chordId) {
      result.emplace(&voicing);
    }
  }
  return result;
}

std::set<const ProgramSequenceChordVoicing *>
ContentEntityStore::getVoicingsOfChordAndVoice(const ProgramSequenceChord *chord, const ProgramVoice *voice) const {
  return getVoicingsOfChordAndVoice(chord->id, voice->id);
}

std::set<const ProgramSequenceChordVoicing *>
ContentEntityStore::getVoicingsOfChordAndVoice(const UUID &chordId, const UUID &voiceId) const {
  std::set<const ProgramSequenceChordVoicing *> result;
  for (const auto &[_, voicing]: programSequenceChordVoicings) {
    if (voicing.programSequenceChordId == chordId && voicing.programVoiceId == voiceId) {
      result.emplace(&voicing);
    }
  }
  return result;
}

std::set<const ProgramVoice *> ContentEntityStore::getVoicesOfProgram(const Program *program) const {
  return getVoicesOfProgram(program->id);
}

std::set<const ProgramVoice *> ContentEntityStore::getVoicesOfProgram(const UUID &programId) const {
  std::set<const ProgramVoice *> result;
  for (const auto &[_, voice]: programVoices) {
    if (voice.programId == programId) {
      result.emplace(&voice);
    }
  }
  return result;
}

std::optional<const Template *>
ContentEntityStore::getTemplateByIdentifier(const std::optional<std::string>::value_type &identifier) {
  // "identifier" which is first the name, then the ship key, then the id
  // is a unique identifier for a template
  for (const auto &[_, tmpl]: templates) {
    if (tmpl.name == identifier) {
      return &tmpl;
    }
    if (tmpl.shipKey == identifier) {
      return &tmpl;
    }
    if (tmpl.id == identifier) {
      return &tmpl;
    }
  }
  return std::nullopt;
}

std::optional<const Template *> ContentEntityStore::getFirstTemplate() {
  if (templates.empty()) return std::nullopt;
  return &templates.begin()->second;
}

MemeTaxonomy ContentEntityStore::getMemeTaxonomy() {
  std::optional<const Template *> FirstTemplate = getFirstTemplate();
  return FirstTemplate.has_value() ? FirstTemplate.value()->config.memeTaxonomy : MemeTaxonomy();
}

ContentEntityStore ContentEntityStore::forTemplate(const Template *tmpl) {
  ContentEntityStore content;

  // Add Template
  content.templates.insert({tmpl->id, (*tmpl)});

  // For each template binding, add the Library, Program, or Instrument
  for (const auto templateBinding: getBindingsOfTemplate(tmpl->id)) {
    content.templateBindings[templateBinding->id] = *templateBinding;
    if (templateBinding->type == TemplateBinding::Type::Library) {
      auto library = getLibrary(templateBinding->targetId);
      if (library.has_value()) {
        content.libraries[library.value()->id] = *library.value();
      }
    } else if (templateBinding->type == TemplateBinding::Type::Program) {
      auto program = getProgram(templateBinding->targetId);
      if (program.has_value()) {
        content.programs[program.value()->id] = *program.value();
      }
    } else if (templateBinding->type == TemplateBinding::Type::Instrument) {
      auto instrument = getInstrument(templateBinding->targetId);
      if (instrument.has_value()) {
        content.instruments[instrument.value()->id] = *instrument.value();
      }
    }
  }

  // For each library, add the Programs that are in a published state
  for (const auto &[_, library]: content.libraries) {
    for (const auto program: getProgramsOfLibrary(&library)) {
      if (program->state == Program::State::Published) {
        content.programs[program->id] = *program;
      }
    }
  }

  // For each library, add the Instruments that are in a published state
  for (const auto &[_, library]: content.libraries) {
    for (const auto instrument: getInstrumentsOfLibrary(&library)) {
      if (instrument->state == Instrument::State::Published) {
        content.instruments[instrument->id] = *instrument;
      }
    }
  }

  // Add entities of Programs
  for (const auto &[_, program]: content.programs) {
    for (const auto meme: getMemesOfProgram(program.id)) {
      content.programMemes[meme->id] = *meme;
    }
    for (const auto voice: getVoicesOfProgram(program.id)) {
      content.programVoices[voice->id] = *voice;
    }
    for (const auto track: getTracksOfProgram(program.id)) {
      content.programVoiceTracks[track->id] = *track;
    }
    for (const auto sequence: getSequencesOfProgram(program.id)) {
      content.programSequences[sequence->id] = *sequence;
    }
    for (const ProgramSequenceBinding *binding: getSequenceBindingsOfProgram(program.id)) {
      content.programSequenceBindings[binding->id] = *binding;
    }
    for (const auto meme: getSequenceBindingMemesOfProgram(program.id)) {
      content.programSequenceBindingMemes[meme->id] = *meme;
    }
    for (const ProgramSequenceChord *chord: getSequenceChordsOfProgram(program.id)) {
      content.programSequenceChords[chord->id] = *chord;
    }
    for (const auto voicing: getSequenceChordVoicingsOfProgram(program.id)) {
      content.programSequenceChordVoicings[voicing->id] = *voicing;
    }
    for (const auto pattern: getSequencePatternsOfProgram(program.id)) {
      content.programSequencePatterns[pattern->id] = *pattern;
    }
    for (const ProgramSequencePatternEvent *event: getSequencePatternEventsOfProgram(program.id)) {
      content.programSequencePatternEvents[event->id] = *event;
    }
  }

  // Add entities of Instruments
  for (const auto &[_, instrument]: content.instruments) {
    for (const auto meme: getMemesOfInstrument(instrument.id)) {
      content.instrumentMemes[meme->id] = *meme;
    }
    for (const auto audio: getAudiosOfInstrument(instrument.id)) {
      content.instrumentAudios[audio->id] = *audio;
    }
  }

  return content;
}

void ContentEntityStore::clear() {
  instruments.clear();
  instrumentAudios.clear();
  instrumentMemes.clear();
  libraries.clear();
  programs.clear();
  programMemes.clear();
  programSequences.clear();
  programSequenceBindings.clear();
  programSequenceBindingMemes.clear();
  programSequenceChords.clear();
  programSequenceChordVoicings.clear();
  programSequencePatterns.clear();
  programSequencePatternEvents.clear();
  programVoices.clear();
  programVoiceTracks.clear();
  templates.clear();
  templateBindings.clear();
  projects.clear();
}

void ContentEntityStore::put(const ContentEntityStore *other) {
  for (const auto &[_, instrument]: other->instruments) {
    instruments[instrument.id] = instrument;
  }
  for (const auto &[_, audio]: other->instrumentAudios) {
    instrumentAudios[audio.id] = audio;
  }
  for (const auto &[_, meme]: other->instrumentMemes) {
    instrumentMemes[meme.id] = meme;
  }
  for (const auto &[_, library]: other->libraries) {
    libraries[library.id] = library;
  }
  for (const auto &[_, program]: other->programs) {
    programs[program.id] = program;
  }
  for (const auto &[_, meme]: other->programMemes) {
    programMemes[meme.id] = meme;
  }
  for (const auto &[_, sequence]: other->programSequences) {
    programSequences[sequence.id] = sequence;
  }
  for (const auto &[_, binding]: other->programSequenceBindings) {
    programSequenceBindings[binding.id] = binding;
  }
  for (const auto &[_, meme]: other->programSequenceBindingMemes) {
    programSequenceBindingMemes[meme.id] = meme;
  }
  for (const auto &[_, chord]: other->programSequenceChords) {
    programSequenceChords[chord.id] = chord;
  }
  for (const auto &[_, voicing]: other->programSequenceChordVoicings) {
    programSequenceChordVoicings[voicing.id] = voicing;
  }
  for (const auto &[_, pattern]: other->programSequencePatterns) {
    programSequencePatterns[pattern.id] = pattern;
  }
  for (const auto &[_, event]: other->programSequencePatternEvents) {
    programSequencePatternEvents[event.id] = event;
  }
  for (const auto &[_, voice]: other->programVoices) {
    programVoices[voice.id] = voice;
  }
  for (const auto &[_, track]: other->programVoiceTracks) {
    programVoiceTracks[track.id] = track;
  }
  for (const auto &[_, tmpl]: other->templates) {
    templates[tmpl.id] = tmpl;
  }
  for (const auto &[_, binding]: other->templateBindings) {
    templateBindings[binding.id] = binding;
  }
  for (const auto &[_, project]: other->projects) {
    projects[project.id] = project;
  }
}

ContentEntityStore::~ContentEntityStore() = default;

ContentEntityStore::ContentEntityStore() = default;

ContentEntityStore::ContentEntityStore(std::ifstream &input) {
  json j;
  input >> j;
  *this = j.get<ContentEntityStore>();
}

ContentEntityStore::ContentEntityStore(std::string &input) {
  *this = json::parse(input).get<ContentEntityStore>();
}
