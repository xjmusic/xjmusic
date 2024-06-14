// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <optional>
#include <vector>
#include <set>

#include "nlohmann/json.hpp"
#include "spdlog/spdlog.h"

#include "xjmusic/entities/content/ContentStore.h"

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
      throw std::invalid_argument("Invalid value for UUID: " + key);
    }
  }

  static void setIfNotNull(const json &json, const std::string &key, std::string &value) {
    if (json.contains(key) && json.at(key).is_string()) {
      try {
        value = json.at(key).get<std::string>();
      } catch (const std::exception &e) {
        throw std::invalid_argument("Invalid value for string " + key);
      }
    }
  }

  static void setIfNotNull(const json &json, const std::string &key, float &value) {
    if (json.contains(key) && json.at(key).is_number_float()) {
      try {
        value = json.at(key).get<float>();
      } catch (const std::exception &e) {
        throw std::invalid_argument("Invalid value for float " + key);
      }
    }
  }

  static void setIfNotNull(const json &json, const std::string &key, bool &value) {
    if (json.contains(key) && json.at(key).is_boolean()) {
      try {
        value = json.at(key).get<bool>();
      } catch (const std::exception &e) {
        throw std::invalid_argument("Invalid value for bool " + key);
      }
    }
  }

  static void setIfNotNull(const json &json, const std::string &key, int &value) {
    if (json.contains(key) && json.at(key).is_number_integer()) {
      try {
        value = json.at(key).get<int>();
      } catch (const std::exception &e) {
        throw std::invalid_argument("Invalid value for integer " + key);
      }
    }
  }

  static void setIfNotNull(const json &json, const std::string &key, long long &value) {
    if (json.contains(key) && json.at(key).is_number_unsigned()) {
      try {
        value = json.at(key).get<long long>();
      } catch (const std::exception &e) {
        throw std::invalid_argument("Invalid value for long " + key);
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

  void from_json(const json &json, ContentStore &store) {
    if (json.contains("instruments"))
      store.setInstruments(
          json.at("instruments").get < std::set < Instrument >> ());
    if (json.contains("instrumentAudios"))
      store.setInstrumentAudios(
          json.at("instrumentAudios").get < std::set < InstrumentAudio >> ());
    if (json.contains("instrumentMemes"))
      store.setInstrumentMemes(
          json.at("instrumentMemes").get < std::set < InstrumentMeme >> ());
    if (json.contains("libraries"))
      store.setLibraries(
          json.at("libraries").get < std::set < Library >> ());
    if (json.contains("programs"))
      store.setPrograms(
          json.at("programs").get < std::set < Program >> ());
    if (json.contains("programMemes"))
      store.setProgramMemes(
          json.at("programMemes").get < std::set < ProgramMeme >> ());
    if (json.contains("programSequences"))
      store.setProgramSequences(
          json.at("programSequences").get < std::set < ProgramSequence >> ());
    if (json.contains("programSequenceBindings"))
      store.setProgramSequenceBindings(
          json.at("programSequenceBindings").get < std::set < ProgramSequenceBinding >> ());
    if (json.contains("programSequenceBindingMemes"))
      store.setProgramSequenceBindingMemes(
          json.at("programSequenceBindingMemes").get < std::set < ProgramSequenceBindingMeme >> ());
    if (json.contains("programSequenceChords"))
      store.setProgramSequenceChords(
          json.at("programSequenceChords").get < std::set < ProgramSequenceChord >> ());
    if (json.contains("programSequenceChordVoicings"))
      store.setProgramSequenceChordVoicings(
          json.at("programSequenceChordVoicings").get < std::set < ProgramSequenceChordVoicing >> ());
    if (json.contains("programSequencePatterns"))
      store.setProgramSequencePatterns(
          json.at("programSequencePatterns").get < std::set < ProgramSequencePattern >> ());
    if (json.contains("programSequencePatternEvents"))
      store.setProgramSequencePatternEvents(
          json.at("programSequencePatternEvents").get < std::set < ProgramSequencePatternEvent >> ());
    if (json.contains("programVoices"))
      store.setProgramVoices(
          json.at("programVoices").get < std::set < ProgramVoice >> ());
    if (json.contains("programVoiceTracks"))
      store.setProgramVoiceTracks(
          json.at("programVoiceTracks").get < std::set < ProgramVoiceTrack >> ());
    if (json.contains("templates"))
      store.setTemplates(
          json.at("templates").get < std::set < Template >> ());
    if (json.contains("templateBindings"))
      store.setTemplateBindings(
          json.at("templateBindings").get < std::set < TemplateBinding >> ());
    if (json.contains("project"))
      store.setProjects({json.at("project").get<Project>()});
  }
}// namespace nlohmann

#pragma clang diagnostic push
#pragma ide diagnostic ignored "bugprone-macro-parentheses"

#define CONTENT_STORE_CORE_METHODS(ENTITY, ENTITIES, STORE)                       \
  std::optional<const ENTITY *> ContentStore::get##ENTITY(const UUID &id) {       \
    try {                                                                         \
      if (STORE.count(id) == 0) {                                                 \
        return std::nullopt;                                                      \
      }                                                                           \
      return &STORE.at(id);                                                       \
    } catch (...) {                                                               \
      return std::nullopt;                                                        \
    }                                                                             \
  }                                                                               \
  std::set<const ENTITY *> ContentStore::get##ENTITIES() {                        \
    try {                                                                         \
      std::set<const ENTITY *> result;                                            \
      result.reserve(STORE.size());                                               \
      for (const auto &entry: STORE) {                                            \
        result.push_back(&entry.second);                                          \
      }                                                                           \
      return result;                                                              \
    } catch (...) {                                                               \
      return {};                                                                  \
    }                                                                             \
  }                                                                               \
  ContentStore ContentStore::set##ENTITIES(const std::set<ENTITY> &entities) {    \
    try {                                                                         \
      STORE.clear();                                                              \
      for (const auto &entity: entities) {                                        \
        STORE[entity.id] = std::move(entity);                                     \
      }                                                                           \
    } catch (const std::exception &e) {                                           \
      spdlog::error("Error putting all {}: {}", #ENTITY, e.what());               \
    }                                                                             \
    return *this;                                                                 \
  }                                                                               \
  ENTITY put(const ENTITY &entity)                                           {    \
    try {                                                                         \
      STORE[entity.id] = std::move(entity);                                       \
    } catch (const std::exception &e) {                                           \
      spdlog::error("Error putting {}: {}", #ENTITY, e.what());                   \
    }                                                                             \
    return entity;                                                                \
  }                                                                               \


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

ContentStore ContentStore::fromJson(std::ifstream &input) {
  json j;
  input >> j;
  return j.get<ContentStore>();
}

ContentStore ContentStore::fromJson(std::string &input) {
  return json::parse(input).get<ContentStore>();
}

std::optional<Project *> ContentStore::getProject() {
  if (projects.empty()) return std::nullopt;
  return &projects.begin()->second;
}

std::optional<const ProgramVoiceTrack *> ContentStore::getTrackOfEvent(const ProgramSequencePatternEvent &event) {
  return getProgramVoiceTrack(event.programVoiceTrackId);
}

std::optional<const ProgramVoice *> ContentStore::getVoiceOfEvent(const ProgramSequencePatternEvent &event) {
  auto track = getTrackOfEvent(event);
  if (!track.has_value()) return std::nullopt;
  return getProgramVoice(track.value()->programVoiceId);
}

Instrument::Type ContentStore::getInstrumentTypeOfEvent(const ProgramSequencePatternEvent &event) {
  auto voiceOfEvent = getVoiceOfEvent(event);
  if (!voiceOfEvent.has_value()) {
    throw std::runtime_error("Can't get Program Voice!");
  }
  return voiceOfEvent.value()->type;
}

bool ContentStore::hasInstrumentsOfType(Instrument::Type type) {
  return std::any_of(instruments.begin(), instruments.end(),
                     [type](const auto &pair) { return pair.second.type == type; });
}

bool ContentStore::hasInstrumentsOfMode(Instrument::Mode mode) {
  return std::any_of(instruments.begin(), instruments.end(),
                     [mode](const auto &pair) { return pair.second.mode == mode; });
}

bool ContentStore::hasInstrumentsOfTypeAndMode(Instrument::Type type, Instrument::Mode mode) {
  return std::any_of(instruments.begin(), instruments.end(),
                     [type, mode](const auto &pair) {
                       return pair.second.type == type && pair.second.mode == mode;
                     });
}

std::vector<int> ContentStore::getAvailableOffsets(const ProgramSequenceBinding &sequenceBinding) {
  std::vector<int> offsets;
  for (const auto &pair: programSequenceBindings) {
    if (pair.second.programId == sequenceBinding.programId) {
      offsets.push_back(pair.second.offset);
    }
  }

  // Remove duplicates
  std::sort(offsets.begin(), offsets.end());
  offsets.erase(std::unique(offsets.begin(), offsets.end()), offsets.end());

  return offsets;
}

std::set<const InstrumentAudio *> ContentStore::getAudiosOfInstrument(const UUID &id) {
  std::set<const InstrumentAudio *> result;
  for (const auto &pair: instrumentAudios) {
    if (pair.second.instrumentId == id) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const InstrumentAudio *> ContentStore::getAudiosOfInstrument(const Instrument &instrument) {
  return getAudiosOfInstrument(instrument.id);
}

std::vector<const ProgramSequenceBinding *> ContentStore::getBindingsOfSequence(const ProgramSequence &sequence) {
  return getBindingsOfSequence(sequence.id);
}

std::vector<const ProgramSequenceBinding *> ContentStore::getBindingsOfSequence(const UUID &sequenceId) {
  std::vector<const ProgramSequenceBinding *> result;
  for (const auto &pair: programSequenceBindings) {
    if (pair.second.programSequenceId == sequenceId) {
      result.push_back(&pair.second);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequenceBinding *a, const ProgramSequenceBinding *b) {
              return a->offset < b->offset;
            });
  return result;
}

std::set<const ProgramSequenceBindingMeme *>
ContentStore::getSequenceBindingMemesOfProgram(const Program &program) {
  return getSequenceBindingMemesOfProgram(program.id);
}

std::set<const ProgramSequenceBindingMeme *>
ContentStore::getSequenceBindingMemesOfProgram(const UUID &programId) {
  std::set<const ProgramSequenceBindingMeme *> result;
  for (const auto &pair: programSequenceBindingMemes) {
    if (pair.second.programId == programId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::vector<const ProgramSequenceBinding *>
ContentStore::getBindingsAtOffsetOfProgram(const Program &program, int offset, bool includeNearest) {
  return getBindingsAtOffsetOfProgram(program.id, offset, includeNearest);
}

std::vector<const ProgramSequenceBinding *>
ContentStore::getBindingsAtOffsetOfProgram(const UUID &programId, int offset, bool includeNearest) {
  std::vector<const ProgramSequenceBinding *> result;
  std::vector<const ProgramSequenceBinding *> candidates;

  // Filter the programSequenceBindings map to get the candidates
  for (const auto &pair: programSequenceBindings) {
    if (pair.second.programId == programId) {
      candidates.push_back(&pair.second);
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

std::vector<const ProgramSequenceChord *> ContentStore::getChordsOfSequence(const ProgramSequence &sequence) {
  return getChordsOfSequence(sequence.id);
}

std::vector<const ProgramSequenceChord *> ContentStore::getChordsOfSequence(const UUID &programSequenceId) {
  std::vector<const ProgramSequenceChord *> result;
  for (const auto &pair: programSequenceChords) {
    if (pair.second.programSequenceId == programSequenceId) {
      result.push_back(&pair.second);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequenceChord *a, const ProgramSequenceChord *b) {
              return a->position < b->position;
            });
  return result;
}

std::vector<const ProgramSequencePatternEvent *>
ContentStore::getEventsOfPattern(const ProgramSequencePattern &pattern) {
  return getEventsOfPattern(pattern.id);
}

std::vector<const ProgramSequencePatternEvent *> ContentStore::getEventsOfPattern(const UUID &patternId) {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &pair: programSequencePatternEvents) {
    if (pair.second.programSequencePatternId == patternId) {
      result.push_back(&pair.second);
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
ContentStore::getPatternsOfSequenceAndVoice(const UUID &programSequenceId, const UUID &programVoiceId) {
  std::set<const ProgramSequencePattern *> result;
  for (const auto &pair: programSequencePatterns) {
    if (pair.second.programSequenceId == programSequenceId && pair.second.programVoiceId == programVoiceId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const TemplateBinding *> ContentStore::getBindingsOfTemplate(const UUID &templateId) {
  std::set<const TemplateBinding *> result;
  for (const auto &pair: templateBindings) {
    if (pair.second.templateId == templateId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}


std::set<const ProgramSequencePattern *> ContentStore::getSequencePatternsOfProgram(const UUID &programId) {
  std::set<const ProgramSequencePattern *> result;
  for (const auto &pair: programSequencePatterns) {
    if (pair.second.programId == programId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const ProgramSequencePattern *> ContentStore::getSequencePatternsOfProgram(const Program &program) {
  return getSequencePatternsOfProgram(program.id);
}

std::vector<const ProgramSequencePatternEvent *>
ContentStore::getSequencePatternEventsOfProgram(const UUID &programId) {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &pair: programSequencePatternEvents) {
    if (pair.second.programId == programId) {
      result.push_back(&pair.second);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequencePatternEvent *a, const ProgramSequencePatternEvent *b) {
              return a->position < b->position;
            });
  return result;
}

std::vector<const ProgramSequencePatternEvent *> ContentStore::getEventsOfTrack(const ProgramVoiceTrack &track) {
  return getEventsOfTrack(track.id);
}

std::vector<const ProgramSequencePatternEvent *> ContentStore::getEventsOfTrack(const UUID &trackId) {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &pair: programSequencePatternEvents) {
    if (pair.second.programVoiceTrackId == trackId) {
      result.push_back(&pair.second);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequencePatternEvent *a, const ProgramSequencePatternEvent *b) {
              return a->position < b->position;
            });
  return result;
}

std::vector<const ProgramSequencePatternEvent *>
ContentStore::getEventsOfPatternAndTrack(const ProgramSequencePattern &pattern, const ProgramVoiceTrack &track) {
  return getEventsOfPatternAndTrack(pattern.id, track.id);
}

std::vector<const ProgramSequencePatternEvent *>
ContentStore::getEventsOfPatternAndTrack(const UUID &patternId, const UUID &trackId) {
  std::vector<const ProgramSequencePatternEvent *> result;
  for (const auto &pair: programSequencePatternEvents) {
    if (pair.second.programSequencePatternId == patternId && pair.second.programVoiceTrackId == trackId) {
      result.push_back(&pair.second);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequencePatternEvent *a, const ProgramSequencePatternEvent *b) {
              return a->position < b->position;
            });
  return result;
}

std::set<const InstrumentAudio *>
ContentStore::getAudiosOfInstrumentTypesAndModes(const std::set<Instrument::Type> &types,
                                                 const std::set<Instrument::Mode> &modes) {
  std::set<const InstrumentAudio *> result;
  for (const auto &audio: getInstrumentsOfTypesAndModes(types, modes)) {
    auto audios = getAudiosOfInstrument(audio->id);
    result.insert(audios.begin(), audios.end());
  }
  return result;
}

std::set<const InstrumentAudio *>
ContentStore::getAudiosOfInstrumentTypes(const std::set<Instrument::Type> &types) {
  std::set<const InstrumentAudio *> result;
  for (const auto &audio: getInstrumentsOfTypes(types)) {
    auto audios = getAudiosOfInstrument(audio->id);
    result.insert(audios.begin(), audios.end());
  }
  return result;
}

std::set<const Instrument *> ContentStore::getInstrumentsOfType(const Instrument::Type &type) {
  std::set<const Instrument *> result;
  for (const auto &pair: instruments) {
    if (pair.second.type == type) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const Instrument *>
ContentStore::getInstrumentsOfTypesAndModes(const std::set<Instrument::Type> &types,
                                            const std::set<Instrument::Mode> &modes) {
  std::set<const Instrument *> result;
  for (const auto &pair: instruments) {
    if (types.empty() || std::find(types.begin(), types.end(), pair.second.type) != types.end()) {
      if (modes.empty() || std::find(modes.begin(), modes.end(), pair.second.mode) != modes.end()) {
        result.emplace(&pair.second);
      }
    }
  }
  return result;
}

std::set<const Instrument *> ContentStore::getInstrumentsOfTypes(const std::set<Instrument::Type> &types) {
  std::set<const Instrument *> result;
  for (const auto &pair: instruments) {
    if (types.empty() || std::find(types.begin(), types.end(), pair.second.type) != types.end()) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const InstrumentMeme *> ContentStore::getMemesOfInstrument(const UUID &instrumentId) {
  std::set<const InstrumentMeme *> result;
  for (const auto &pair: instrumentMemes) {
    if (pair.second.instrumentId == instrumentId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const Instrument *> ContentStore::getInstrumentsOfLibrary(const Library &library) {
  return getInstrumentsOfLibrary(library.id);
}

std::set<const Instrument *> ContentStore::getInstrumentsOfLibrary(const UUID &libraryId) {
  std::set<const Instrument *> result;
  for (const auto &pair: instruments) {
    if (pair.second.libraryId == libraryId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

Instrument::Type ContentStore::getInstrumentTypeOfAudio(const UUID &instrumentAudioId) {
  if (!instrumentAudios.count(instrumentAudioId)) throw std::runtime_error("Can't find Instrument Audio!");
  auto audio = instrumentAudios.at(instrumentAudioId);
  if (!instruments.count(audio.instrumentId)) throw std::runtime_error("Can't find Instrument!");
  return instruments.at(audio.instrumentId).type;
}

std::set<const ProgramMeme *> ContentStore::getMemesOfProgram(const UUID &programId) {
  std::set<const ProgramMeme *> result;
  for (const auto &pair: programMemes) {
    if (pair.second.programId == programId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<std::string> ContentStore::getMemesAtBeginning(const Program &program) {
  std::vector<std::string> result;

  // add program memes
  for (const ProgramMeme *meme: getMemesOfProgram(program.id))
    result.emplace_back(meme->name);

  // add sequence binding memes
  for (const ProgramSequenceBinding *sequenceBinding: getBindingsAtOffsetOfProgram(program, 0, false))
    for (const ProgramSequenceBindingMeme *meme: getMemesOfSequenceBinding(sequenceBinding->id))
      result.emplace_back(meme->name);

  // Remove duplicates
  std::sort(result.begin(), result.end());
  result.erase(std::unique(result.begin(), result.end()), result.end());

  return std::set < std::string > (result.begin(), result.end());
}

std::set<const ProgramSequenceBindingMeme *>
ContentStore::getMemesOfSequenceBinding(const ProgramSequenceBinding &programSequenceBinding) {
  return getMemesOfSequenceBinding(programSequenceBinding.id);
}

std::set<const ProgramSequenceBindingMeme *>
ContentStore::getMemesOfSequenceBinding(const UUID &programSequenceBindingId) {
  std::set<const ProgramSequenceBindingMeme *> result;
  for (const auto &pair: programSequenceBindingMemes) {
    if (pair.second.programSequenceBindingId == programSequenceBindingId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

UUID ContentStore::getPatternIdOfEvent(const UUID &eventId) {
  if (!programSequencePatternEvents.count(eventId)) throw std::runtime_error("Can't find Event!");
  return programSequencePatternEvents.at(eventId).programSequencePatternId;
}

std::set<const ProgramSequencePattern *> ContentStore::getPatternsOfSequence(const ProgramSequence &sequence) {
  return getPatternsOfSequence(sequence.id);
}

std::set<const ProgramSequencePattern *> ContentStore::getPatternsOfSequence(const UUID &sequence) {
  std::set<const ProgramSequencePattern *> result;
  for (const auto &pair: programSequencePatterns) {
    if (pair.second.programSequenceId == sequence) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const ProgramSequencePattern *> ContentStore::getPatternsOfVoice(const ProgramVoice &voice) {
  return getPatternsOfVoice(voice.id);
}

std::set<const ProgramSequencePattern *> ContentStore::getPatternsOfVoice(const UUID &voice) {
  std::set<const ProgramSequencePattern *> result;
  for (const auto &pair: programSequencePatterns) {
    if (pair.second.programVoiceId == voice) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const Program *> ContentStore::getProgramsOfLibrary(const Library &library) {
  return getProgramsOfLibrary(library.id);
}

std::set<const Program *> ContentStore::getProgramsOfLibrary(const UUID &libraryId) {
  std::set<const Program *> result;
  for (const auto &pair: programs) {
    if (pair.second.libraryId == libraryId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const Program *> ContentStore::getProgramsOfType(Program::Type type) {
  std::set<const Program *> result;
  for (const auto &pair: programs) {
    if (pair.second.type == type) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::optional<const ProgramSequence *>
ContentStore::getSequenceOfBinding(const ProgramSequenceBinding &sequenceBinding) {
  if (!programSequences.count(sequenceBinding.programSequenceId)) {
    return std::nullopt;
  }
  return &programSequences.at(sequenceBinding.programSequenceId);
}

std::set<const ProgramSequence *> ContentStore::getSequencesOfProgram(const UUID &programId) {
  std::set<const ProgramSequence *> result;
  for (const auto &pair: programSequences) {
    if (pair.second.programId == programId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::vector<const ProgramSequenceBinding *> ContentStore::getSequenceBindingsOfProgram(const UUID &programId) {
  std::vector<const ProgramSequenceBinding *> result;
  for (const auto &pair: programSequenceBindings) {
    if (pair.second.programId == programId) {
      result.push_back(&pair.second);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequenceBinding *a, const ProgramSequenceBinding *b) {
              return a->offset < b->offset;
            });
  return result;
}

std::vector<const ProgramSequenceChord *> ContentStore::getSequenceChordsOfProgram(const UUID &programId) {
  std::vector<const ProgramSequenceChord *> result;
  for (const auto &pair: programSequenceChords) {
    if (pair.second.programId == programId) {
      result.push_back(&pair.second);
    }
  }
  std::sort(result.begin(), result.end(),
            [](const ProgramSequenceChord *a, const ProgramSequenceChord *b) {
              return a->position < b->position;
            });
  return result;
}

std::set<const ProgramSequenceChordVoicing *>
ContentStore::getSequenceChordVoicingsOfProgram(const UUID &programId) {
  std::set<const ProgramSequenceChordVoicing *> result;
  for (const auto &pair: programSequenceChordVoicings) {
    if (pair.second.programId == programId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const ProgramVoiceTrack *> ContentStore::getTracksOfProgram(const UUID &programId) {
  std::set<const ProgramVoiceTrack *> result;
  for (const auto &pair: programVoiceTracks) {
    if (pair.second.programId == programId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const ProgramVoiceTrack *> ContentStore::getTracksOfProgramType(Program::Type type) {
  std::set<const ProgramVoiceTrack *> result;
  for (const auto &pair: programVoiceTracks) {
    if (programs.count(pair.second.programId) && programs.at(pair.second.programId).type == type) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const ProgramVoiceTrack *> ContentStore::getTracksOfVoice(const ProgramVoice &voice) {
  return getTracksOfVoice(voice.id);
}

std::set<const ProgramVoiceTrack *> ContentStore::getTracksOfVoice(const UUID &voiceId) {
  std::set<const ProgramVoiceTrack *> result;
  for (const auto &pair: programVoiceTracks) {
    if (pair.second.programVoiceId == voiceId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<std::string> ContentStore::getTrackNamesOfVoice(const ProgramVoice &voice) {
  std::set < std::string > result;
  for (const auto &pair: programVoiceTracks) {
    if (pair.second.programVoiceId == voice.id) {
      result.emplace(pair.second.name);
    }
  }
  return result;
}

std::set<const ProgramSequenceChordVoicing *> ContentStore::getVoicingsOfChord(const ProgramSequenceChord &chord) {
  return getVoicingsOfChord(chord.id);
}

std::set<const ProgramSequenceChordVoicing *> ContentStore::getVoicingsOfChord(const UUID &chordId) {
  std::set<const ProgramSequenceChordVoicing *> result;
  for (const auto &pair: programSequenceChordVoicings) {
    if (pair.second.programSequenceChordId == chordId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const ProgramSequenceChordVoicing *>
ContentStore::getVoicingsOfChordAndVoice(const ProgramSequenceChord &chord, const ProgramVoice &voice) {
  return getVoicingsOfChordAndVoice(chord.id, voice.id);
}

std::set<const ProgramSequenceChordVoicing *>
ContentStore::getVoicingsOfChordAndVoice(const UUID &chordId, const UUID &voiceId) {
  std::set<const ProgramSequenceChordVoicing *> result;
  for (const auto &pair: programSequenceChordVoicings) {
    if (pair.second.programSequenceChordId == chordId && pair.second.programVoiceId == voiceId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

std::set<const ProgramVoice *> ContentStore::getVoicesOfProgram(const Program &program) {
  return getVoicesOfProgram(program.id);
}

std::set<const ProgramVoice *> ContentStore::getVoicesOfProgram(const UUID &programId) {
  std::set<const ProgramVoice *> result;
  for (const auto &pair: programVoices) {
    if (pair.second.programId == programId) {
      result.emplace(&pair.second);
    }
  }
  return result;
}

ContentStore ContentStore::forTemplate(const Template &tmpl) {
  ContentStore content;

  // Add Template
  content.templates.insert({tmpl.id, tmpl});

  // For each template binding, add the Library, Program, or Instrument
  for (const TemplateBinding *templateBinding: getBindingsOfTemplate(tmpl.id)) {
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
  for (const auto &library: content.libraries) {
    for (const Program *program: getProgramsOfLibrary(library.second)) {
      if (program->state == Program::State::Published) {
        content.programs[program->id] = *program;
      }
    }
  }

  // For each library, add the Instruments that are in a published state
  for (const auto &library: content.libraries) {
    for (const Instrument *instrument: getInstrumentsOfLibrary(library.second)) {
      if (instrument->state == Instrument::State::Published) {
        content.instruments[instrument->id] = *instrument;
      }
    }
  }

  // Add entities of Programs
  for (const auto &program: content.programs) {
    for (const ProgramMeme *meme: getMemesOfProgram(program.second.id)) {
      content.programMemes[meme->id] = *meme;
    }
    for (const ProgramVoice *voice: getVoicesOfProgram(program.second.id)) {
      content.programVoices[voice->id] = *voice;
    }
    for (const ProgramVoiceTrack *track: getTracksOfProgram(program.second.id)) {
      content.programVoiceTracks[track->id] = *track;
    }
    for (const ProgramSequence *sequence: getSequencesOfProgram(program.second.id)) {
      content.programSequences[sequence->id] = *sequence;
    }
    for (const ProgramSequenceBinding *binding: getSequenceBindingsOfProgram(program.second.id)) {
      content.programSequenceBindings[binding->id] = *binding;
    }
    for (const ProgramSequenceBindingMeme *meme: getSequenceBindingMemesOfProgram(program.second.id)) {
      content.programSequenceBindingMemes[meme->id] = *meme;
    }
    for (const ProgramSequenceChord *chord: getSequenceChordsOfProgram(program.second.id)) {
      content.programSequenceChords[chord->id] = *chord;
    }
    for (const ProgramSequenceChordVoicing *voicing: getSequenceChordVoicingsOfProgram(program.second.id)) {
      content.programSequenceChordVoicings[voicing->id] = *voicing;
    }
    for (const ProgramSequencePattern *pattern: getSequencePatternsOfProgram(program.second.id)) {
      content.programSequencePatterns[pattern->id] = *pattern;
    }
    for (const ProgramSequencePatternEvent *event: getSequencePatternEventsOfProgram(program.second.id)) {
      content.programSequencePatternEvents[event->id] = *event;
    }
  }

  // Add entities of Instruments
  for (const auto &instrument: content.instruments) {
    for (const InstrumentMeme *meme: getMemesOfInstrument(instrument.second.id)) {
      content.instrumentMemes[meme->id] = *meme;
    }
    for (const InstrumentAudio *audio: getAudiosOfInstrument(instrument.second.id)) {
      content.instrumentAudios[audio->id] = *audio;
    }
  }

  return content;
}

void ContentStore::clear() {
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

#pragma clang diagnostic pop