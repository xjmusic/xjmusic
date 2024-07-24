// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <optional>
#include <set>
#include <vector>
#include <yaml-cpp/yaml.h>

#include "xjmusic/content/TemplateConfig.h"
#include "xjmusic/fabricator/NotePicker.h"

#include "../_helper/YamlTest.h"
#include "xjmusic/util/CsvUtils.h"

using namespace XJ;

/**
 XJ has a serviceable voicing algorithm https://github.com/xjmusic/xjmusic/issues/221
 */
class NotePickerTest : public YamlTest {
protected:
  int REPEAT_EACH_TEST_TIMES = 7;
  std::string TEST_PATH_PREFIX = "_data/picking/";
  TemplateConfig templateConfig;
  std::unique_ptr<NotePicker> subject;

  /**
   Load the specified test YAML file and Run it repeatedly.

   @param filename of test YAML file
   */
  void loadAndRunTest(const std::string& filename) {
    for (int i = 0; i < REPEAT_EACH_TEST_TIMES; i++)
      try {
        // Load YAML and parse
        const auto data = loadYaml(TEST_PATH_PREFIX, filename);

        // Read inputs from the test YAML and instantiate the subject
        auto eventNotes = loadSubject(data);

        // Execute note picking
        std::set<Note> picked;
        for (const auto note: eventNotes) {
          picked.emplace(subject->pick(note));
        }

        // assert picks
        loadAndPerformAssertions(picked, data);

      } catch (std::exception &e) {
        failures.emplace("[" + filename + "] Exception: " + e.what());
      }
  }

  /**
   Load the input section of the test YAML file

   @param data YAML file wrapper
   */
  std::vector<Note> loadSubject(YAML::Node data) {
    if (!data["input"]) throw std::runtime_error("Input is required!");

    const YAML::Node obj = data["input"];

    const auto range = getOptionalNoteRange(obj);

    const std::string instrumentType = getStr(obj, "instrumentType").value_or("");

    std::set<Note> voicingNotes;
    const std::string voicingNotesCsv = getStr(obj, "voicingNotes").value_or("");
    for (const auto& noteStr : CsvUtils::split(voicingNotesCsv)) {
      voicingNotes.insert(Note::of(noteStr));
    }

    subject = std::make_unique<NotePicker>(range, voicingNotes, templateConfig.instrumentTypesForInversionSeekingContains(Instrument::parseType(instrumentType)));

    std::vector<Note> eventNotes;
    const std::string eventNotesCsv = getStr(obj, "eventNotes").value_or("");
    for (const auto& noteStr : CsvUtils::split(eventNotesCsv)) {
      eventNotes.emplace_back(Note::of(noteStr));
    }

    return eventNotes;
  }


  static NoteRange getOptionalNoteRange(YAML::Node node) {
    if (!node["range"]) return {};

    const YAML::Node rangeNode = node["range"];
    const std::string from = getStr(rangeNode, "from").value_or("");
    const std::string to = getStr(rangeNode, "to").value_or("");

    return NoteRange::from(from, to);
  }


  void loadAndPerformAssertions(const std::set<Note>& pickedNotes, YAML::Node data) {
    if (!data["assertion"]) throw std::runtime_error("Assertion is required!");

    YAML::Node obj = data["assertion"];

    if (const auto range = getOptionalNoteRange(obj); !range.empty()) {
      if (range.low.has_value()) {
        assertSameNote("Range Low-end", range.low.value(), subject->getTargetRange().low.value());
      }

      if (range.high.has_value()) {
        assertSameNote("Range High-end", range.high.value(), subject->getTargetRange().high.value());
      }
    }

    std::set<std::string> picked;
    for (auto& note : pickedNotes) {
      picked.insert(note.toString(Sharp));
    }

    if (obj["picks"]) {
      std::set<std::string> picks;
      const std::string picksCsv = getStr(obj, "picks").value_or("");
      for (const auto& noteStr : CsvUtils::split(picksCsv)) {
        picks.insert(noteStr);
      }
      assertSameNotes("Picks", picks, picked);
    }

    if (obj["count"]) {
      const int count = getInt(obj, "count").value_or(0);
      ASSERT_EQ(count, picked.size());
    }
  }

};


TEST_F(NotePickerTest, picking_4_5_notes_one) {
  loadAndRunTest("picking_4_5_notes_one.yaml");
}


TEST_F(NotePickerTest, picking_4_5_notes_two) {
  loadAndRunTest("picking_4_5_notes_two.yaml");
}


TEST_F(NotePickerTest, picking_4_5_notes_three) {
  loadAndRunTest("picking_4_5_notes_three.yaml");
}


TEST_F(NotePickerTest, picking_6_notes) {
  loadAndRunTest("picking_6_notes.yaml");
}


