// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <optional>
#include <set>
#include <vector>

#include "nlohmann/json.hpp"
#include <spdlog/spdlog.h>

#include "xjmusic/content/ContentEntityStore.h"

using namespace XJ;

using json = nlohmann::json;

namespace nlohmann {
  static void setRequired(const json &json, const std::string &key, UUID &value) {
    if (!json.contains(key)) {
      throw std::invalid_argument("Missing required UUID: " + key);
    }
    try {
      value = json.at(key).get<std::string>();
    } catch (const std::exception &e) {
      throw std::invalid_argument("Invalid value for UUID: " + key + " - " + e.what());
    }
  }

  static void setIfNotNull(const json &json, const std::string &key, std::string &value) {
    if (json.contains(key) && json.at(key).is_string()) {
      try {
        value = json.at(key).get<std::string>();
      } catch (const std::exception &e) {
        throw std::invalid_argument("Invalid value for string " + key + " - " + e.what());
      }
    }
  }

  static void setIfNotNull(const json &json, const std::string &key, float &value) {
    if (json.contains(key) && json.at(key).is_number_float()) {
      try {
        value = json.at(key).get<float>();
      } catch (const std::exception &e) {
        throw std::invalid_argument("Invalid value for float " + key + " - " + e.what());
      }
    }
  }

  static void setIfNotNull(const json &json, const std::string &key, bool &value) {
    if (json.contains(key) && json.at(key).is_boolean()) {
      try {
        value = json.at(key).get<bool>();
      } catch (const std::exception &e) {
        throw std::invalid_argument("Invalid value for bool " + key + " - " + e.what());
      }
    }
  }

  static void setIfNotNull(const json &json, const std::string &key, int &value) {
    if (json.contains(key) && json.at(key).is_number_integer()) {
      try {
        value = json.at(key).get<int>();
      } catch (const std::exception &e) {
        throw std::invalid_argument("Invalid value for integer " + key + " - " + e.what());
      }
    }
  }

  static void setIfNotNull(const json &json, const std::string &key, long long &value) {
    if (json.contains(key) && json.at(key).is_number_unsigned()) {
      try {
        value = json.at(key).get<long long>();
      } catch (const std::exception &e) {
        throw std::invalid_argument("Invalid value for long " + key + " - " + e.what());
      }
    }
  }

  void from_json(const json &json, Instrument &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "libraryId", entity.libraryId);
    entity.state = Instrument::parseState(json.at("state").get<std::string>());
    entity.type = Instrument::parseType(json.at("type").get<std::string>());
    entity.mode = Instrument::parseMode(json.at("mode").get<std::string>());
    setIfNotNull(json, "name", entity.name);
    setIfNotNull(json, "config", entity.config);
    setIfNotNull(json, "volume", entity.volume);
    setIfNotNull(json, "isDeleted", entity.isDeleted);
    setIfNotNull(json, "updatedAt", entity.updatedAt);
  }

  void from_json(const json &json, InstrumentAudio &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "instrumentId", entity.instrumentId);
    setIfNotNull(json, "name", entity.name);
    setIfNotNull(json, "waveformKey", entity.waveformKey);
    setIfNotNull(json, "transientSeconds", entity.transientSeconds);
    setIfNotNull(json, "loopBeats", entity.loopBeats);
    setIfNotNull(json, "tempo", entity.tempo);
    setIfNotNull(json, "intensity", entity.intensity);
    setIfNotNull(json, "event", entity.event);
    setIfNotNull(json, "volume", entity.volume);
    setIfNotNull(json, "tones", entity.tones);
  }

  void from_json(const json &json, InstrumentMeme &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "instrumentId", entity.instrumentId);
    setIfNotNull(json, "name", entity.name);
  }

  void from_json(const json &json, Library &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "projectId", entity.projectId);
    setIfNotNull(json, "name", entity.name);
    setIfNotNull(json, "isDeleted", entity.isDeleted);
    setIfNotNull(json, "updatedAt", entity.updatedAt);
  }

  void from_json(const json &json, Program &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "libraryId", entity.libraryId);
    entity.state = Program::parseState(json.at("state").get<std::string>());
    entity.type = Program::parseType(json.at("type").get<std::string>());
    setIfNotNull(json, "key", entity.key);
    setIfNotNull(json, "tempo", entity.tempo);
    setIfNotNull(json, "name", entity.name);
    setIfNotNull(json, "config", entity.config);
    setIfNotNull(json, "isDeleted", entity.isDeleted);
    setIfNotNull(json, "updatedAt", entity.updatedAt);
  }

  void from_json(const json &json, ProgramMeme &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "programId", entity.programId);
    setIfNotNull(json, "name", entity.name);
  }

  void from_json(const json &json, ProgramSequence &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "programId", entity.programId);
    setIfNotNull(json, "name", entity.name);
    setIfNotNull(json, "key", entity.key);
    setIfNotNull(json, "intensity", entity.intensity);
    setIfNotNull(json, "total", entity.total);
  }

  void from_json(const json &json, ProgramSequenceBinding &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "programId", entity.programId);
    setRequired(json, "programSequenceId", entity.programSequenceId);
    setIfNotNull(json, "offset", entity.offset);
  }

  void from_json(const json &json, ProgramSequenceBindingMeme &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "programId", entity.programId);
    setRequired(json, "programSequenceBindingId", entity.programSequenceBindingId);
    setIfNotNull(json, "name", entity.name);
  }

  void from_json(const json &json, ProgramSequenceChord &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "programId", entity.programId);
    setRequired(json, "programSequenceId", entity.programSequenceId);
    setIfNotNull(json, "name", entity.name);
    setIfNotNull(json, "position", entity.position);
  }

  void from_json(const json &json, ProgramSequenceChordVoicing &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "programId", entity.programId);
    setRequired(json, "programSequenceChordId", entity.programSequenceChordId);
    setRequired(json, "programVoiceId", entity.programVoiceId);
    setIfNotNull(json, "notes", entity.notes);
  }

  void from_json(const json &json, ProgramSequencePattern &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "programId", entity.programId);
    setRequired(json, "programSequenceId", entity.programSequenceId);
    setRequired(json, "programVoiceId", entity.programVoiceId);
    setIfNotNull(json, "name", entity.name);
    setIfNotNull(json, "total", entity.total);
  }

  void from_json(const json &json, ProgramSequencePatternEvent &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "programId", entity.programId);
    setRequired(json, "programSequencePatternId", entity.programSequencePatternId);
    setRequired(json, "programVoiceTrackId", entity.programVoiceTrackId);
    setIfNotNull(json, "velocity", entity.velocity);
    setIfNotNull(json, "position", entity.position);
    setIfNotNull(json, "duration", entity.duration);
    setIfNotNull(json, "tones", entity.tones);
  }

  void from_json(const json &json, ProgramVoice &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "programId", entity.programId);
    entity.type = Instrument::parseType(json.at("type").get<std::string>());
    setIfNotNull(json, "duration", entity.name);
    setIfNotNull(json, "tones", entity.order);
  }

  void from_json(const json &json, ProgramVoiceTrack &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "programId", entity.programId);
    setRequired(json, "programVoiceId", entity.programVoiceId);
    setIfNotNull(json, "duration", entity.name);
    setIfNotNull(json, "tones", entity.order);
  }

  void from_json(const json &json, Project &entity) {
    setRequired(json, "id", entity.id);
    setIfNotNull(json, "name", entity.name);
    setIfNotNull(json, "platformVersion", entity.platformVersion);
    setIfNotNull(json, "isDeleted", entity.isDeleted);
    setIfNotNull(json, "updatedAt", entity.updatedAt);
  }

  void from_json(const json &json, Template &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "projectId", entity.projectId);
    setIfNotNull(json, "name", entity.name);
    setIfNotNull(json, "config", entity.config);
    setIfNotNull(json, "shipKey", entity.shipKey);
    setIfNotNull(json, "isDeleted", entity.isDeleted);
    setIfNotNull(json, "updatedAt", entity.updatedAt);
  }

  void from_json(const json &json, TemplateBinding &entity) {
    setRequired(json, "id", entity.id);
    setRequired(json, "templateId", entity.templateId);
    entity.type = TemplateBinding::parseType(json.at("type").get<std::string>());
    setRequired(json, "targetId", entity.targetId);
  }

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
}// namespace nlohmann

#pragma clang diagnostic push
#pragma ide diagnostic ignored "bugprone-macro-parentheses"

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
        STORE[entity.id] = entity;                                                         \
      }                                                                                    \
    } catch (const std::exception &e) {                                                    \
      spdlog::error("Error putting all {}: {}", #ENTITY, e.what());                        \
    }                                                                                      \
    return *this;                                                                          \
  }                                                                                        \
  ENTITY ContentEntityStore::put(const ENTITY &entity) {                                   \
    try {                                                                                  \
      STORE[entity.id] = entity;                                                           \
    } catch (const std::exception &e) {                                                    \
      spdlog::error("Error putting {}: {}", #ENTITY, e.what());                            \
    }                                                                                      \
    return entity;                                                                         \
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
  for (const auto &[fst, snd]: programSequenceBindings) {
    if (snd.programId == sequenceBinding->programId) {
      offsets.push_back(snd.offset);
    }
  }

  // Remove duplicates
  std::sort(offsets.begin(), offsets.end());
  offsets.erase(std::unique(offsets.begin(), offsets.end()), offsets.end());

  return offsets;
}

std::set<const InstrumentAudio *> ContentEntityStore::getAudiosOfInstrument(const UUID &id) const {
  std::set<const InstrumentAudio *> result;
  for (const auto &[fst, snd]: instrumentAudios) {
    if (snd.instrumentId == id) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const InstrumentAudio *> ContentEntityStore::getAudiosOfInstrument(const Instrument *instrument) const {
  return getAudiosOfInstrument(instrument->id);
}

std::vector<const ProgramSequenceBinding *> ContentEntityStore::getBindingsOfSequence(const ProgramSequence *sequence) const {
  return getBindingsOfSequence(sequence->id);
}

std::vector<const ProgramSequenceBinding *> ContentEntityStore::getBindingsOfSequence(const UUID &sequenceId) const {
  std::vector<const ProgramSequenceBinding *> result;
  for (const auto &[fst, snd]: programSequenceBindings) {
    if (snd.programSequenceId == sequenceId) {
      result.emplace_back(&snd);
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
  for (const auto &[fst, snd]: programSequenceBindingMemes) {
    if (snd.programId == programId) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::vector<const ProgramSequenceBinding *>
ContentEntityStore::getBindingsAtOffsetOfProgram(const Program *program, const int offset, const bool includeNearest) const {
  return getBindingsAtOffsetOfProgram(program->id, offset, includeNearest);
}

std::vector<const ProgramSequenceBinding *>
ContentEntityStore::getBindingsAtOffsetOfProgram(const UUID &programId, const int offset, const bool includeNearest) const {
  std::vector<const ProgramSequenceBinding *> result;
  std::vector<const ProgramSequenceBinding *> candidates;

  // Filter the programSequenceBindings map to get the candidates
  for (const auto &[fst, snd]: programSequenceBindings) {
    if (snd.programId == programId) {
      candidates.emplace_back(&snd);
    }
  }

  if (includeNearest) {
    // Find the actual offset
    int minDifference = std::numeric_limits<int>::max();
    int actualOffset = offset;
    for (const auto &candidate: candidates) {
      int difference = std::abs(candidate->offset - offset);
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

std::vector<const ProgramSequenceChord *> ContentEntityStore::getChordsOfSequence(const ProgramSequence *sequence) const {
  return getChordsOfSequence(sequence->id);
}

std::vector<const ProgramSequenceChord *> ContentEntityStore::getChordsOfSequence(const UUID &programSequenceId) const {
  std::vector<const ProgramSequenceChord *> result;
  for (const auto &[fst, snd]: programSequenceChords) {
    if (snd.programSequenceId == programSequenceId) {
      result.emplace_back(&snd);
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
  for (const auto &[fst, snd]: programSequencePatternEvents) {
    if (snd.programSequencePatternId == patternId) {
      result.emplace_back(&snd);
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
  for (const auto &[fst, snd]: programSequencePatterns) {
    if (snd.programSequenceId == programSequenceId && snd.programVoiceId == programVoiceId) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const TemplateBinding *> ContentEntityStore::getBindingsOfTemplate(const UUID &templateId) const {
  std::set<const TemplateBinding *> result;
  for (const auto &[fst, snd]: templateBindings) {
    if (snd.templateId == templateId) {
      result.emplace(&snd);
    }
  }
  return result;
}


std::set<const ProgramSequencePattern *> ContentEntityStore::getSequencePatternsOfProgram(const UUID &programId) const {
  std::set<const ProgramSequencePattern *> result;
  for (const auto &[fst, snd]: programSequencePatterns) {
    if (snd.programId == programId) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const ProgramSequencePattern *> ContentEntityStore::getSequencePatternsOfProgram(const Program *program) const {
  return getSequencePatternsOfProgram(program->id);
}

std::vector<const ProgramSequencePatternEvent *>
ContentEntityStore::getSequencePatternEventsOfProgram(const UUID &programId) const {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &[fst, snd]: programSequencePatternEvents) {
    if (snd.programId == programId) {
      result.emplace_back(&snd);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequencePatternEvent *a, const ProgramSequencePatternEvent *b) {
              return a->position < b->position;
            });
  return result;
}

std::vector<const ProgramSequencePatternEvent *> ContentEntityStore::getEventsOfTrack(const ProgramVoiceTrack *track) const {
  return getEventsOfTrack(track->id);
}

std::vector<const ProgramSequencePatternEvent *> ContentEntityStore::getEventsOfTrack(const UUID &trackId) const {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &[fst, snd]: programSequencePatternEvents) {
    if (snd.programVoiceTrackId == trackId) {
      result.emplace_back(&snd);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequencePatternEvent *a, const ProgramSequencePatternEvent *b) {
              return a->position < b->position;
            });
  return result;
}

std::vector<const ProgramSequencePatternEvent *>
ContentEntityStore::getEventsOfPatternAndTrack(const ProgramSequencePattern *pattern, const ProgramVoiceTrack *track) const {
  return getEventsOfPatternAndTrack(pattern->id, track->id);
}

std::vector<const ProgramSequencePatternEvent *>
ContentEntityStore::getEventsOfPatternAndTrack(const UUID &patternId, const UUID &trackId) const {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &[fst, snd]: programSequencePatternEvents) {
    if (snd.programSequencePatternId == patternId && snd.programVoiceTrackId == trackId) {
      result.emplace_back(&snd);
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
  for (const auto &[fst, snd]: instruments) {
    if (snd.type == type) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const Instrument *>
ContentEntityStore::getInstrumentsOfTypesAndModes(const std::set<Instrument::Type> &types,
                                                  const std::set<Instrument::Mode> &modes) const {
  std::set<const Instrument *> result;
  for (const auto &[fst, snd]: instruments) {
    if (types.empty() || types.find(snd.type) != types.end()) {
      if (modes.empty() || modes.find(snd.mode) != modes.end()) {
        result.emplace(&snd);
      }
    }
  }
  return result;
}

std::set<const Instrument *> ContentEntityStore::getInstrumentsOfTypes(const std::set<Instrument::Type> &types) const {
  std::set<const Instrument *> result;
  for (const auto &[fst, snd]: instruments) {
    if (types.empty() || types.find(snd.type) != types.end()) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const InstrumentMeme *> ContentEntityStore::getMemesOfInstrument(const UUID &instrumentId) const {
  std::set<const InstrumentMeme *> result;
  for (const auto &[fst, snd]: instrumentMemes) {
    if (snd.instrumentId == instrumentId) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const Instrument *> ContentEntityStore::getInstrumentsOfLibrary(const Library *library) const {
  return getInstrumentsOfLibrary(library->id);
}

std::set<const Instrument *> ContentEntityStore::getInstrumentsOfLibrary(const UUID &libraryId) const {
  std::set<const Instrument *> result;
  for (const auto &[fst, snd]: instruments) {
    if (snd.libraryId == libraryId) {
      result.emplace(&snd);
    }
  }
  return result;
}

Instrument::Type ContentEntityStore::getInstrumentTypeOfAudio(const UUID &instrumentAudioId) const {
  if (!instrumentAudios.count(instrumentAudioId)) throw std::runtime_error("Can't find Instrument Audio!");
  auto audio = instrumentAudios.at(instrumentAudioId);
  if (!instruments.count(audio.instrumentId)) throw std::runtime_error("Can't find Instrument!");
  return instruments.at(audio.instrumentId).type;
}

std::set<const ProgramMeme *> ContentEntityStore::getMemesOfProgram(const UUID &programId) const {
  std::set<const ProgramMeme *> result;
  for (const auto &[fst, snd]: programMemes) {
    if (snd.programId == programId) {
      result.emplace(&snd);
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
  for (const auto &[fst, snd]: programSequenceBindingMemes) {
    if (snd.programSequenceBindingId == programSequenceBindingId) {
      result.emplace(&snd);
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
  for (const auto &[fst, snd]: programSequencePatterns) {
    if (snd.programSequenceId == sequence) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const ProgramSequencePattern *> ContentEntityStore::getPatternsOfVoice(const ProgramVoice *voice) const {
  return getPatternsOfVoice(voice->id);
}

std::set<const ProgramSequencePattern *> ContentEntityStore::getPatternsOfVoice(const UUID &voiceId) const {
  std::set<const ProgramSequencePattern *> result;
  for (const auto &[fst, snd]: programSequencePatterns) {
    if (snd.programVoiceId == voiceId) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const Program *> ContentEntityStore::getProgramsOfLibrary(const Library *library) const {
  return getProgramsOfLibrary(library->id);
}

std::set<const Program *> ContentEntityStore::getProgramsOfLibrary(const UUID &libraryId) const {
  std::set<const Program *> result;
  for (const auto &[fst, snd]: programs) {
    if (snd.libraryId == libraryId) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const Program *> ContentEntityStore::getProgramsOfType(Program::Type type) const {
  std::set<const Program *> result;
  for (const auto &[fst, snd]: programs) {
    if (snd.type == type) {
      result.emplace(&snd);
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
  for (const auto &[fst, snd]: programSequences) {
    if (snd.programId == programId) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::vector<const ProgramSequenceBinding *> ContentEntityStore::getSequenceBindingsOfProgram(const UUID &programId) const {
  std::vector<const ProgramSequenceBinding *> result;
  for (const auto &[fst, snd]: programSequenceBindings) {
    if (snd.programId == programId) {
      result.emplace_back(&snd);
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
  for (const auto &[fst, snd]: programSequenceChords) {
    if (snd.programId == programId) {
      result.emplace_back(&snd);
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
  for (const auto &[fst, snd]: programSequenceChordVoicings) {
    if (snd.programId == programId && Note::containsAnyValidNotes(snd.notes)) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const ProgramVoiceTrack *> ContentEntityStore::getTracksOfProgram(const UUID &programId) const {
  std::set<const ProgramVoiceTrack *> result;
  for (const auto &[fst, snd]: programVoiceTracks) {
    if (snd.programId == programId) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const ProgramVoiceTrack *> ContentEntityStore::getTracksOfProgramType(const Program::Type type) const {
  std::set<const ProgramVoiceTrack *> result;
  for (const auto &[fst, snd]: programVoiceTracks) {
    if (programs.count(snd.programId) && programs.at(snd.programId).type == type) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const ProgramVoiceTrack *> ContentEntityStore::getTracksOfVoice(const ProgramVoice *voice) const {
  return getTracksOfVoice(voice->id);
}

std::set<const ProgramVoiceTrack *> ContentEntityStore::getTracksOfVoice(const UUID &voiceId) const {
  std::set<const ProgramVoiceTrack *> result;
  for (const auto &[fst, snd]: programVoiceTracks) {
    if (snd.programVoiceId == voiceId) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<std::string> ContentEntityStore::getTrackNamesOfVoice(const ProgramVoice *voice) const {
  std::set<std::string> result;
  for (const auto &[fst, snd]: programVoiceTracks) {
    if (snd.programVoiceId == voice->id) {
      result.emplace(snd.name);
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
  for (const auto &[fst, snd]: programSequenceChordVoicings) {
    if (snd.programSequenceChordId == chordId) {
      result.emplace(&snd);
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
  for (const auto &[fst, snd]: programSequenceChordVoicings) {
    if (snd.programSequenceChordId == chordId && snd.programVoiceId == voiceId) {
      result.emplace(&snd);
    }
  }
  return result;
}

std::set<const ProgramVoice *> ContentEntityStore::getVoicesOfProgram(const Program *program) const {
  return getVoicesOfProgram(program->id);
}

std::set<const ProgramVoice *> ContentEntityStore::getVoicesOfProgram(const UUID &programId) const {
  std::set<const ProgramVoice *> result;
  for (const auto &[fst, snd]: programVoices) {
    if (snd.programId == programId) {
      result.emplace(&snd);
    }
  }
  return result;
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
  for (const auto &[fst, snd]: content.libraries) {
    for (const auto program: getProgramsOfLibrary(&snd)) {
      if (program->state == Program::State::Published) {
        content.programs[program->id] = *program;
      }
    }
  }

  // For each library, add the Instruments that are in a published state
  for (const auto &[fst, snd]: content.libraries) {
    for (const auto instrument: getInstrumentsOfLibrary(&snd)) {
      if (instrument->state == Instrument::State::Published) {
        content.instruments[instrument->id] = *instrument;
      }
    }
  }

  // Add entities of Programs
  for (const auto &program: content.programs) {
    for (const auto meme: getMemesOfProgram(program.second.id)) {
      content.programMemes[meme->id] = *meme;
    }
    for (const auto voice: getVoicesOfProgram(program.second.id)) {
      content.programVoices[voice->id] = *voice;
    }
    for (const auto track: getTracksOfProgram(program.second.id)) {
      content.programVoiceTracks[track->id] = *track;
    }
    for (const auto sequence: getSequencesOfProgram(program.second.id)) {
      content.programSequences[sequence->id] = *sequence;
    }
    for (const ProgramSequenceBinding *binding: getSequenceBindingsOfProgram(program.second.id)) {
      content.programSequenceBindings[binding->id] = *binding;
    }
    for (const auto meme: getSequenceBindingMemesOfProgram(program.second.id)) {
      content.programSequenceBindingMemes[meme->id] = *meme;
    }
    for (const ProgramSequenceChord *chord: getSequenceChordsOfProgram(program.second.id)) {
      content.programSequenceChords[chord->id] = *chord;
    }
    for (const auto voicing: getSequenceChordVoicingsOfProgram(program.second.id)) {
      content.programSequenceChordVoicings[voicing->id] = *voicing;
    }
    for (const auto pattern: getSequencePatternsOfProgram(program.second.id)) {
      content.programSequencePatterns[pattern->id] = *pattern;
    }
    for (const ProgramSequencePatternEvent *event: getSequencePatternEventsOfProgram(program.second.id)) {
      content.programSequencePatternEvents[event->id] = *event;
    }
  }

  // Add entities of Instruments
  for (const auto &instrument: content.instruments) {
    for (const auto meme: getMemesOfInstrument(instrument.second.id)) {
      content.instrumentMemes[meme->id] = *meme;
    }
    for (const auto audio: getAudiosOfInstrument(instrument.second.id)) {
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

ContentEntityStore::ContentEntityStore() = default;

ContentEntityStore::ContentEntityStore(std::ifstream &input) {
  json j;
  input >> j;
  *this = j.get<ContentEntityStore>();
}

ContentEntityStore::ContentEntityStore(std::string &input) {
  *this = json::parse(input).get<ContentEntityStore>();
}


#pragma clang diagnostic pop