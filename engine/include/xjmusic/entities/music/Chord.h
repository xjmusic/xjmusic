// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_MUSIC_CHORD_H
#define XJMUSIC_MUSIC_CHORD_H

#include <iostream>
#include <vector>
#include <string>

#include "PitchClass.h"
#include "SlashRoot.h"
#include "Accidental.h"
#include "Root.h"

namespace XJ {

  /**
   * Each potential synonym
   */
  class ChordSynonym {
  public:
    std::string match;
    bool caseSensitive;

    explicit ChordSynonym(const std::string &match, bool caseSensitive = false);

    bool operator==(const ChordSynonym &other) const;

    [[nodiscard]] bool matches(const std::string &input) const;
  };

  /**
   * One Chord form has a basic description with many potential synonyms
   */
  class ChordForm {
  public:
    std::string description;
    std::vector<ChordSynonym> synonyms{};

    ChordForm(const std::string &description, const std::vector<ChordSynonym> &synonyms);

    [[nodiscard]] bool matches(const std::string &input) const;

    bool operator==(const ChordForm &other) const;

    std::size_t hashCode();

  };

  /**
   * Chord in a particular key
   */
  class Chord {
  private:
    static std::vector<ChordForm> forms;

  public:
    std::string description;

    PitchClass root; // Root Pitch Class

    SlashRoot slashRoot; // Slash Root Pitch Class

    Accidental accidental; // the (flat/sharp) adjustment symbol, which will be used to express this chord

    bool operator<(const Chord &other) const;

    bool operator==(const Chord &other) const;

    explicit Chord(const std::string &input);

    /**
     * Parse a chord description and return its most basic representation from the form dictionary
     *
     * @param input to parse
     * @return most basic synonym, or original if already most basic
     */
    static std::string normalize(const std::string &input);

    /**
     * std::string expression of interval pitch group, original name
     *
     * @return scale as string
     */
    std::string toString() const;

    /**
     * Delta to another Key calculated in +/- semitones
     *
     * @param target key to calculate delta to
     * @return delta +/- semitones to another key
     */
    [[nodiscard]] int delta(const Chord &target) const;

    /**
     * Compute the name from the root pitch class and description
     *
     * @return chord name
     */
    [[nodiscard]] std::string name const;

    /**
     * XJ understands the root of a slash chord https://www.pivotaltracker.com/story/show/176728338
     */
    PitchClass slashRootPitchClass();

    /**
     * Chord of a particular key, e.g. of("C minor 7")
     *
     * @param name of Chord
     * @return new Chord
     */
    static Chord of(const std::string &name);

    /**
     * Whether this is a No Chord instance
     *
     * @return true if No Chord
     */
    [[nodiscard]] bool isNoChord() const;

    /**
     * Whether one chord is acceptable as a substitute another
     *
     * @param other chord to test
     * @return true if acceptable
     */
    bool isAcceptable(const Chord &other) const;

    /**
     * Whether this Chord is null
     *
     * @return true if non-null
     */
    [[nodiscard]] bool has_value() const;
  };

}// namespace XJ

#endif// XJMUSIC_MUSIC_CHORD_H