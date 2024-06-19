// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_YAML_TEST_HELPER
#define XJMUSIC_YAML_TEST_HELPER

#include <optional>

#include <gtest/gtest.h>
#include <yaml-cpp/yaml.h>

#include "xjmusic/music/Note.h"

namespace XJ {

/**
 XJ has a serviceable voicing algorithm https://github.com/xjmusic/xjmusic/issues/221
 */
class YamlTest : public ::testing::Test {
protected:
  std::set<std::string> failures;

  void SetUp() override;

  /**
   The cache of failures is a Set in order to de-dupe between multiple runs of the same test
   */
  void TearDown() override;


  /**
   * Load the specified YAML file
   * @param prefix   The path prefix
   * @param filename  The name of the file to load
   * @return  The YAML node
   */
  YAML::Node loadYaml(const std::string& prefix, const std::string& filename);


  /**
   * Assert that two strings are the same, adding a failure message if they are not
   * @param description  The description of the assertion
   * @param expected   The expected value
   * @param actual   The actual value
   */
  void assertSame(const std::string& description, int expected, int actual);


  /**
   * Assert that two notes are the same, adding a failure message if they are not
   * @param description  The description of the assertion
   * @param expected  The expected note
   * @param actual  The actual note
   */
  void assertSameNote(const std::string& description, const Note& expected, const Note& actual);


  /**
   * Assert that two sets of notes are the same, adding a failure message if they are not
   * @param description  The description of the assertion
   * @param expected  The expected set of notes
   * @param actual  The actual set of notes
   */
  void assertSameNotes(const std::string& description, const std::set<std::string>& expected, const std::set<std::string>& actual);


  /**
   * Get a string from a YAML node
   * @param node  The YAML node
   * @param key  The key to get
   * @return  The string value, if it exists
   */
  static std::optional<std::string> getStr(YAML::Node node, const std::string& key);


  /**
   * Get a integer from a YAML node
   * @param node  The YAML node
   * @param key  The key to get
   * @return  The integer value, if it exists
   */
  static std::optional<int> getInt(YAML::Node node, const std::string& key) ;


  /**
   * Get a float from a YAML node
   * @param node  The YAML node
   * @param key  The key to get
   * @return  The float value, if it exists
   */
  static std::optional<float> getFloat(YAML::Node node, const std::string& key) ;


  /**
  * Get a position float from a YAML node
  * @param node  The YAML node
  * @param key  The key to get
  * @return  The position float value, if it exists
  */
  static std::optional<float> getFloat(YAML::Node node);


  /**
   * Get a boolean from a YAML node
   * @param node  The YAML node
   * @param key  The key to get
   * @return  The boolean value, if it exists
   */
  static std::optional<bool> getBool(YAML::Node node, const std::string& key);

};

} // namespace XJ

#endif // XJMUSIC_YAML_TEST_HELPER