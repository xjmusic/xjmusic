// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/util/ConfigParser.h"

using namespace XJ;


TEST(ConfigParserTest, ParseConfig) {
  std::string input = R"(
            arrayOfMapsWithArrayOfStringValues = [
                {
                  memes = ["RED","GREEN","BLUE"]
                  name = "COLOR"
                },
                {
                  memes = ["WINTER","SPRING","SUMMER","FALL"]
                  name = "SEASON"
                }
              ]
            booleanValue = false
            floatValue = 2.64872
            intValue = 12
            mapOfFloatValues = {
                Apples = 0.0
                Bananas = 1.7
                Oranges = 3.4
              }
            mapOfIntValues = {
                Apples = 3
                Bananas = 12
                Oranges = 43
              }
            quotedStringValue = "Hello, World!"
            stringListValue = ["Apples","Bananas","Oranges"]
            stringValue = Apples
    )";

  auto subject = ConfigParser(input);

  // single values
  ASSERT_EQ("Apples", subject.getSingleValue("stringValue").getString());
  ASSERT_EQ("Hello, World!", subject.getSingleValue("quotedStringValue").getString());
  ASSERT_NEAR(2.64872, subject.getSingleValue("floatValue").getFloat(), 0.0001);
  ASSERT_EQ(12, subject.getSingleValue("intValue").getInt());
  ASSERT_EQ(false, subject.getSingleValue("booleanValue").getBool());
  //
  // list of string values
  ConfigListValue stringList = subject.getListValue("stringListValue");
  ASSERT_EQ(3, stringList.size());
  ASSERT_EQ("Apples", stringList.atSingle(0).getString());
  ASSERT_EQ("Bananas", stringList.atSingle(1).getString());
  ASSERT_EQ("Oranges", stringList.atSingle(2).getString());
  //
  // map of float values
  ConfigObjectValue mapOfFloatValues = subject.getObjectValue("mapOfFloatValues");
  ASSERT_EQ(3, mapOfFloatValues.size());
  ASSERT_NEAR(0.0, mapOfFloatValues.atSingle("Apples").getFloat(), 0.0001);
  ASSERT_NEAR(1.7, mapOfFloatValues.atSingle("Bananas").getFloat(), 0.0001);
  ASSERT_NEAR(3.4, mapOfFloatValues.atSingle("Oranges").getFloat(), 0.0001);
  //
  // map of int values
  ConfigObjectValue mapOfIntValues = subject.getObjectValue("mapOfIntValues");
  ASSERT_EQ(3, mapOfIntValues.size());
  ASSERT_EQ(3, mapOfIntValues.atSingle("Apples").getInt());
  ASSERT_EQ(12, mapOfIntValues.atSingle("Bananas").getInt());
  ASSERT_EQ(43, mapOfIntValues.atSingle("Oranges").getInt());
  //
  // array of maps with mixed single values and array of string values
  ConfigListValue arrayOfMapsWithArrayOfStringValues = subject.getListValue("arrayOfMapsWithArrayOfStringValues");
  ASSERT_EQ(2, arrayOfMapsWithArrayOfStringValues.size());
  ConfigObjectValue firstMap = arrayOfMapsWithArrayOfStringValues.atObject(0);
  ASSERT_EQ(2, firstMap.size());
  ASSERT_EQ("COLOR", firstMap.atSingle("name").getString());
  std::vector<ConfigSingleValue> firstMapMemes = firstMap.atList("memes");
  ASSERT_EQ(3, firstMapMemes.size());
  ASSERT_EQ("RED", firstMapMemes.at(0).getString());
  ASSERT_EQ("GREEN", firstMapMemes.at(1).getString());
  ASSERT_EQ("BLUE", firstMapMemes.at(2).getString());
  ConfigObjectValue secondMap = arrayOfMapsWithArrayOfStringValues.atObject(1);
  ASSERT_EQ(2, secondMap.size());
  ASSERT_EQ("SEASON", secondMap.atSingle("name").getString());
  std::vector<ConfigSingleValue> secondMapMemes = secondMap.atList("memes");
  ASSERT_EQ(4, secondMapMemes.size());
  ASSERT_EQ("WINTER", secondMapMemes.at(0).getString());
  ASSERT_EQ("SPRING", secondMapMemes.at(1).getString());
  ASSERT_EQ("SUMMER", secondMapMemes.at(2).getString());
  ASSERT_EQ("FALL", secondMapMemes.at(3).getString());
}

TEST(ConfigParserTest, ParseConfigWithDefaults) {
  const std::string defaults = R"(
            quotedStringValue = "Hello, World!"
            stringValue = Apples
    )";
  const std::string input = R"(
            booleanValue = false
            floatValue = 2.64872
            intValue = 12
    )";

  auto subject = ConfigParser(input, ConfigParser(defaults));

  // single values
  ASSERT_EQ("Apples", subject.getSingleValue("stringValue").getString());
  ASSERT_EQ("Hello, World!", subject.getSingleValue("quotedStringValue").getString());
  ASSERT_NEAR(2.64872, subject.getSingleValue("floatValue").getFloat(), 0.0001);
  ASSERT_EQ(12, subject.getSingleValue("intValue").getInt());
  ASSERT_EQ(false, subject.getSingleValue("booleanValue").getBool());
}

TEST(ConfigParserTest, FormatBoolValue) {
  ASSERT_EQ("true", ConfigParser::format(true));
  ASSERT_EQ("false", ConfigParser::format(false));
}

TEST(ConfigParserTest, FormatQuotedListValue) {
  const std::vector<std::string> values = {"Apples", "Bananas", "Oranges"};

  ASSERT_EQ("[\"Apples\",\"Bananas\",\"Oranges\"]", ConfigParser::format(values));
}

TEST(ConfigParserTest, FormatStringValue) {
  ASSERT_EQ("\"Hello, World!\"", ConfigParser::format("Hello, World!"));
}

TEST(ConfigParserTest, FormatFloatValue) {
  ASSERT_EQ("2.64872", ConfigParser::format(2.64872f));
}

TEST(ConfigParserTest, FormatIntValue) {
  ASSERT_EQ("12", ConfigParser::format(12));
}

TEST(ConfigParserTest, EqualityOperator) {
  const auto subject = ConfigParser(R"(
            booleanValue = false
            floatValue = 2.64872
            mapOfIntValues = {
                Apples = 3
                Bananas = 12
                Oranges = 43
              }
            quotedStringValue = "Hello, World!"
            stringListValue = ["Apples","Bananas","Oranges"]
    )");

  ASSERT_TRUE(subject == ConfigParser(R"(
            booleanValue = false
            floatValue = 2.64872
            mapOfIntValues = {
                Apples = 3
                Bananas = 12
                Oranges = 43
              }
            quotedStringValue = "Hello, World!"
            stringListValue = ["Apples","Bananas","Oranges"]
    )"));
  ASSERT_FALSE(subject == ConfigParser(R"(
            booleanValue = true
            floatValue = 2.64872
            mapOfIntValues = {
                Apples = 3
                Bananas = 12
                Oranges = 43
              }
            quotedStringValue = "Hello, World!"
            stringListValue = ["Apples","Bananas","Oranges"]
    )"));
  ASSERT_FALSE(subject == ConfigParser(R"(
            booleanValue = false
            floatValue = 2.65872
            mapOfIntValues = {
                Apples = 3
                Bananas = 12
                Oranges = 43
              }
            quotedStringValue = "Hello, World!"
            stringListValue = ["Apples","Bananas","Oranges"]
    )"));
  ASSERT_FALSE(subject == ConfigParser(R"(
            booleanValue = false
            floatValue = 2.64872
            mapOfIntValues = {
                Apples = 6
                Bananas = 12
                Oranges = 43
              }
            quotedStringValue = "Hello, World!"
            stringListValue = ["Apples","Bananas","Oranges"]
    )"));
  ASSERT_FALSE(subject == ConfigParser(R"(
            booleanValue = false
            floatValue = 2.64872
            mapOfIntValues = {
                Apples = 3
                Oranges = 43
              }
            quotedStringValue = "Hello, World!"
            stringListValue = ["Apples","Bananas","Oranges"]
    )"));
  ASSERT_FALSE(subject == ConfigParser(R"(
            booleanValue = false
            floatValue = 2.64872
            mapOfIntValues = {
                Apples = 3
                Bananas = 12
                Oranges = 43
              }
            quotedStringValue = "Hello, World!!!"
            stringListValue = ["Apples","Bananas","Oranges"]
    )"));
  ASSERT_FALSE(subject == ConfigParser(R"(
            booleanValue = false
            floatValue = 2.64872
            mapOfIntValues = {
                Apples = 3
                Bananas = 12
                Oranges = 43
              }
            quotedStringValue = "Hello, World!"
            stringListValue = ["Apples","Oranges"]
    )"));
  ASSERT_FALSE(subject == ConfigParser(R"(
            booleanValue = false
            floatValue = 2.64872
            mapOfIntValues = {
                Apples = 3
                Bananas = 12
                Oranges = 43
              }
            quotedStringValue = "Hello, World!"
            stringListValue = ["Apples","Bananas","Mangos"]
    )"));
}

TEST(ConfigParserTest, SetToOperator) {
  const auto subject = ConfigParser(R"(
            booleanValue = false
            floatValue = 2.64872
            mapOfIntValues = {
                Apples = 3
                Bananas = 12
                Oranges = 43
              }
            quotedStringValue = "Hello, World!"
            stringListValue = ["Apples","Bananas","Oranges"]
    )");

  const auto other = subject;

  ASSERT_EQ(subject, other);
}