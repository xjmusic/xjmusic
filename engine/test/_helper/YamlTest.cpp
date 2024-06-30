// Copyright (c) XJ Music Inc. (https://xjmusic.com)

#include "YamlTest.h"

using namespace XJ;

void YamlTest::SetUp() {
  failures.clear();
}


void YamlTest::TearDown() {
  if (failures.empty()) {
    std::cout << "\nAll assertions OK" << std::endl;
    return;
  }
  std::cout << "\nFAILED" << std::endl;
  for (const auto &failure: failures) {
    std::cout << failure << std::endl;
  }
  // In Google Test, you would typically use an assertion here to fail the test if there are any failures
  if (!failures.empty()) {
    FAIL();
  }
}


YAML::Node YamlTest::loadYaml(const std::string &prefix, const std::string &filename) {
  const std::string fullPath = prefix + filename;
  YAML::Node data = YAML::LoadFile(fullPath);
  assert(data.IsMap());
  return data;
}


void YamlTest::assertSame(const std::string &description, const int expected, const int actual) {
  if (expected != actual) {
    const std::string failure =
        description + " — Expected: " + std::to_string(expected) + " — Actual: " + std::to_string(actual);
    failures.insert(failure);
  }
}


void YamlTest::assertSameNote(const std::string &description, const Note &expected, const Note &actual) {
  if (!(expected == actual)) {
    const std::string failure = description + " — Expected: " + expected.toString(Sharp) + " — Actual: " +
                          actual.toString(Sharp);
    failures.insert(failure);
  }
}


void YamlTest::assertSameNotes(const std::string &description, const std::set<std::string> &expected,
                               const std::set<std::string> &actual) {
  std::vector<Note> expectedNotes;
  std::transform(expected.begin(), expected.end(), std::back_inserter(expectedNotes), [](const std::string &note) {
    return Note::of(note);
  });
  std::sort(expectedNotes.begin(), expectedNotes.end());

  std::vector<Note> actualNotes;
  std::transform(actual.begin(), actual.end(), std::back_inserter(actualNotes), [](const std::string &note) {
    return Note::of(note);
  });
  std::sort(actualNotes.begin(), actualNotes.end());

  // iterate through all notes and compare
  for (size_t i = 0; i < expectedNotes.size(); i++) {
    if (!(expectedNotes[i] == actualNotes[i])) {
      const std::string failure =
          description + " — Expected: " + expectedNotes[i].toString(Sharp) + " — Actual: " +
          actualNotes[i].toString(Sharp);
      failures.insert(failure);
      return;
    }
  }
}


std::optional<std::string> YamlTest::getStr(YAML::Node node, const std::string &key) {
  if (!node[key]) return std::nullopt;
  return node[key].as<std::string>();
}


std::optional<int> YamlTest::getInt(YAML::Node node, const std::string &key) {
  if (!node[key]) return std::nullopt;
  return node[key].as<int>();
}


std::optional<float> YamlTest::getFloat(YAML::Node node, const std::string &key) {
  if (!node[key]) return std::nullopt;
  return node[key].as<float>();
}


std::optional<float> YamlTest::getFloat(YAML::Node node) {
  if (!node["position"]) return std::nullopt;
  return node["position"].as<float>();
}


std::optional<bool> YamlTest::getBool(YAML::Node node, const std::string &key) {
  if (!node[key]) return std::nullopt;
  return node[key].as<bool>();
}


