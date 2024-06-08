// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <algorithm>

#include "yaml-cpp/yaml.h"
#include <gtest/gtest.h>

#include "xjmusic/entities/music/Chord.h"

using namespace XJ;

static std::string EXPECTED_CHORDS_YAML = "_data/music_chord_test.yaml";

TEST(Music_Chord, TestChordExpectations) {
  YAML::Node chords = YAML::LoadFile(EXPECTED_CHORDS_YAML);

  assert(chords.IsMap());

  int fails = 0;
  std::string actual;

  // Iterate over the map
  for (YAML::const_iterator it = chords.begin(); it != chords.end(); ++it) {
    auto input = it->first.as<std::string>();
    auto expect = it->second.as<std::string>();
    try {
      actual = Chord::of(input);// Assuming Chord::of() is a function that takes a string and returns a Chord object

      if (actual != expect) {
        std::cerr << "Expected \"" << input << "\" to yield \"" << expect << "\" but actually was \"" << actual << "\"\n";
        fails++;
      }
    } catch (const std::exception &e) {
      std::cerr << "Error: " << e.what() << " for " << actual << "\n";
      fails++;
    }
  }

  assert(fails == 0);
}

TEST(Music_Chord, of) {
  ASSERT_EQ("C 6/9", Chord::of("CM6add9").name);
  ASSERT_EQ("C 7b9/13", Chord::of("C dom7b9/13").name);
  ASSERT_EQ("C aug maj7", Chord::of("C+∆").name);
  ASSERT_EQ("C -7b5/11", Chord::of("C Ø11").name);
  ASSERT_EQ("C aug maj7", Chord::of("C+M7").name);
  ASSERT_EQ("C aug maj7", Chord::of("C+∆").name);
  ASSERT_EQ("C aug maj7", Chord::of("C∆#5").name);
  ASSERT_EQ("C aug maj7", Chord::of("C+♮7").name);
  ASSERT_EQ("C dim", Chord::of("C°").name);
}

TEST(Music_Chord, TestOf_Invalid) {
  Chord chord = Chord::of("P-funk");
  ASSERT_EQ(Atonal, chord.root);
}

TEST(Music_Chord, isNull) {
  ASSERT_FALSE(Chord::of("C#m7").isNoChord());
  ASSERT_TRUE(Chord::of("NC").isNoChord());
}

/**
 * XJ understands the root of a slash chord https://www.pivotaltracker.com/story/show/176728338
 * Slash Chord Fluency https://www.pivotaltracker.com/story/show/182885209
 */
TEST(Music_Chord, SlashRootPitchClass) {
  ASSERT_EQ(PitchClass::C, Chord::of("Cm7").slashRootPitchClass());
  ASSERT_EQ(PitchClass::Cs, Chord::of("C#m7").slashRootPitchClass());
  ASSERT_EQ(PitchClass::G, Chord::of("Cm7/G").slashRootPitchClass());
  ASSERT_EQ(PitchClass::Gs, Chord::of("C#m7/G#").slashRootPitchClass());
  ASSERT_EQ(PitchClass::A, Chord::of("Gsus4/A").slashRootPitchClass());
  ASSERT_EQ(PitchClass::A, Chord::of("G/A").slashRootPitchClass());
}

/**
 * XJ should correctly choose chords with tensions notated via slash and not confuse them with slash chords
 * https://www.pivotaltracker.com/story/show/183738228
 */
TEST(Music_Chord, getDescription_dontConfuseTensionWithSlash) {
  ASSERT_EQ("-", Chord::of("G#m/B").description);
  ASSERT_EQ("maj7/9", Chord::of("Gmaj7/9").description);
  ASSERT_EQ("maj7", Chord::of("Gmaj7/E").description);
}

TEST(Music_Chord, stripExtraSpacesFromName) {
  ASSERT_EQ("G", Chord::of("  G      ").name);
}

TEST(Music_Chord, isSame) {
  ASSERT_TRUE(Chord::of("  G major     ") == Chord::of(" G     major "));
  ASSERT_TRUE(Chord::of("Gm") == Chord::of("Gm"));
  ASSERT_FALSE(Chord::of("Gm") == Chord::of("Cm"));
}


/**
 * Chord mode Instruments should recognize enharmonic equivalents https://www.pivotaltracker.com/story/show/183558424
 */
TEST(Music_Chord, isSame_eharmonicEquivalent) {
  ASSERT_TRUE(Chord::of("  G# major     ") == Chord::of(" Ab     major "));
  ASSERT_TRUE(Chord::of("G#") == Chord::of("Ab"));
  ASSERT_FALSE(Chord::of("G#") == Chord::of("Bb"));
}

TEST(Music_Chord, isAcceptable) {
  ASSERT_TRUE(Chord::of("  G major     ").isAcceptable(Chord::of(" G     major ")));
  ASSERT_TRUE(Chord::of("Gm").isAcceptable(Chord::of("Gm")));
  ASSERT_FALSE(Chord::of("Gm").isAcceptable(Chord::of("Cm")));
  ASSERT_TRUE(Chord::of("Gm").isAcceptable(Chord::of("Gm/Bb")));
  ASSERT_TRUE(Chord::of("Gm/Bb").isAcceptable(Chord::of("Gm")));
  ASSERT_FALSE(Chord::of("Gm/Bb").isAcceptable(Chord::of("Cm")));
}

/**
 * XJ should correctly choose chords with tensions notated via slash and not confuse them with slash chords
 * https://www.pivotaltracker.com/story/show/183738228
 */
TEST(Music_Chord, isAcceptable_dontConfuseTensionWithSlash) {
  ASSERT_FALSE(Chord::of("Fmaj7/9").isAcceptable(Chord::of("Fmaj7/G")));
}

/**
 * Synonym of base chord should be accepted for slash chord https://www.pivotaltracker.com/story/show/183553280
 */
TEST(Music_Chord, isAcceptable_sameBaseDifferentSlash) {
  auto c1 = Chord::of("C/G");
  auto c2 = Chord::of("C/E");
  ASSERT_EQ(PitchClass::G, c1.slashRootPitchClass());
  ASSERT_EQ(PitchClass::E, c2.slashRootPitchClass());
  ASSERT_EQ(PitchClass::C, c1.root);
  ASSERT_EQ(PitchClass::C, c2.root);
  ASSERT_EQ("", c1.description);
  ASSERT_EQ("", c2.description);
  ASSERT_TRUE(Chord::of("C/G").isAcceptable(Chord::of("C/E")));
}

/**
 * Chord mode Instruments should recognize enharmonic equivalents https://www.pivotaltracker.com/story/show/183558424
 */
TEST(Music_Chord, isAcceptable_eharmonicEquivalent) {
  ASSERT_TRUE(Chord::of("  G# major     ").isAcceptable(Chord::of(" Ab     major ")));
  ASSERT_TRUE(Chord::of("G#m").isAcceptable(Chord::of("Abm")));
  ASSERT_FALSE(Chord::of("G#m").isAcceptable(Chord::of("Bbm")));
  ASSERT_TRUE(Chord::of("G#m").isAcceptable(Chord::of("Abm/Bb")));
  ASSERT_TRUE(Chord::of("G#m/C").isAcceptable(Chord::of("Abm")));
  ASSERT_FALSE(Chord::of("G#m/C").isAcceptable(Chord::of("Bbm")));
}

TEST(Music_Chord, compareTo) {
  auto source = std::vector<Chord>{
      Chord::of("Db minor"),
      Chord::of("C major"),
      Chord::of("C"),
      Chord::of("C minor"),
      Chord::of("Cs major")};

  // Sort the source vector
  std::sort(source.begin(), source.end(), [](const Chord &a, const Chord &b) {
    return a.name < b.name;
  });

  // Map the sorted chords to their names
  std::vector<std::string> sorted;
  std::transform(source.begin(), source.end(), std::back_inserter(sorted), [](const Chord &chord) {
    return chord.name;
  });

  // Join the chord names into a single string
  std::ostringstream joined;
  std::copy(sorted.begin(), sorted.end(), std::ostream_iterator<std::string>(joined, ", "));

  // Remove the trailing comma and space
  std::string result = joined.str();
  result = result.substr(0, result.length() - 2);// assuming the string is not empty

  // Now you can use `result` in your assertion
  ASSERT_EQ("C, C, C -, C s major, Db -", result);
}

TEST(Music_ChordForm, normalize) {
  ASSERT_EQ("", Chord::normalize("major"));
  ASSERT_EQ("", Chord::normalize("maj"));
  ASSERT_EQ("", Chord::normalize("M"));
  ASSERT_EQ("-", Chord::normalize("m"));
  ASSERT_EQ("-", Chord::normalize("minor"));
  ASSERT_EQ("-", Chord::normalize("min"));
  ASSERT_EQ("-", Chord::normalize("mi"));
}
