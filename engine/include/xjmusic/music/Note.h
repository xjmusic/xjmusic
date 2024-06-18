// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_MUSIC_NOTE_H
#define XJMUSIC_MUSIC_NOTE_H

#include <optional>
#include <regex>
#include <string>

#include "PitchClass.h"
#include "Step.h"

namespace XJ {

  /**
   * A Note is used to represent the relative duration and pitch of a sound.
   * <p>
   * https://en.wikipedia.org/wiki/Musical_note
   */
  class Note {
  private:
    static std::regex rgxValidNote;

    // this max is only for extreme-case infinite loop prevention
    static int MAX_DELTA_SEMITONES;

  public:
    /**
     * Octave #
     */
    int octave;

    /**
     * Pitch class of note
     */
    PitchClass pitchClass;

    /**
     * Equality of two notes
     * @param other  to compare
     * @return       true if this is equal to other
     */
    bool operator==(const Note &other) const;

    /**
     * Comparison of two notes: this < other
     * @param other  to compare
     * @return       true if this is less than other
     */
    bool operator<(const Note &other) const;

    /**
     * Comparison of two notes: this > other
     * @param other  to compare
     * @return       true if this is less than other
     */
    bool operator>(const Note &other) const;

    /**
     * Comparison of two notes: this <= other
     * @param other  to compare
     * @return       true if this is less than other
     */
    bool operator<=(const Note &other) const;

    /**
     * Comparison of two notes: this >= other
     * @param other  to compare
     * @return       true if this is less than other
     */
    bool operator>=(const Note &other) const;

    /**
     * Atonal note string representation
     */
    static std::string ATONAL;

    /**
     * Default constructor
     */
    Note();

    /**
     * Construct of note
     * @param name of note
     */
    explicit Note(const std::string &name);

    /**
     * Construct note from pitch class and octave #
     * @param pitchClass of note
     * @param octave     of note
     */
    Note(PitchClass pitchClass, int octave);

    /**
     * Instantiate a note
     * @param name of note
     * @return note
     */
    static Note of(const std::string &name);

    /**
     * Instantiate a note by pitch class and octave
     * @param pitchClass of note
     * @param octave     of note
     * @return note
     */
    static Note of(PitchClass pitchClass, int octave);

    /**
     * Instantiate an atonal note
     * @return atonal note
     */

    static Note atonal();

    /**
     * Only stream a valid note, else empty
     * NC sections should not cache notes from the previous section https://www.pivotaltracker.com/story/show/179409784
     * @param name of note to test for validity
     * @return valid note stream, or empty stream (if invalid)
     */
    static std::optional<Note> ifValid(const std::string &name);

    /**
     * Only stream a valid and tonal note, else empty
     * NC sections should not cache notes from the previous section https://www.pivotaltracker.com/story/show/179409784
     * @param name of note to test for validity
     * @return valid note stream, or empty stream (if invalid)
     */
    static std::optional<Note> ifTonal(const std::string &name);

    /**
     * Whether the current note is valid
     * @param name of note to test
     * @return true if valid
     */
    static bool isValid(const std::string &name);

    /**
     * Whether the CSV contains any valid noteCsv
     * @param noteCsv to test
     * @return true if contains any valid noteCsv
     */
    static bool containsAnyValidNotes(const std::string &noteCsv);

    /**
     * Return the median note between the two given notes, or just one if the other is null
     * @param n1 note 1
     * @param n2 note 2
     * @return median note between the given two
     */
    static std::optional<Note> median(std::optional<Note> n1, std::optional<Note> n2);

    /**
     * Note to std::string
     * @param accidental to represent note with
     * @return string representation of Note
     */
    [[nodiscard]] std::string toString(Accidental accidental) const;

    /**
     * Note stepped +/- semitones to a new Note
     * @param inc +/- semitones to transpose
     * @return Note
     */
    [[nodiscard]] Note shift(int inc) const;

    /**
     * Note stepped +/- octaves to a new Note
     * @param inc +/- octaves to transpose
     * @return Note
     */
    [[nodiscard]] Note shiftOctave(int inc) const;

    /**
     * Copies this object to a new Note
     *
     * @return new note
     */
    [[nodiscard]] Note copy() const;

    /**
     * Set the octave of this note to the one that would result in the target note
     * being at most -6 or +5 semitones from the original note.
     * <p>
     * Here we guarantee that the target note is no more than -6 or +5 semitones away from the original audio note. Note that we are arbitrarily favoring down-pitching versus up-pitching, and that is an aesthetic decision, because it just sounds good.
     * <p>
     * [#303] Craft calculates drum audio pitch to conform to the allowable note closest to the original note, slightly favoring down-pitching versus up-pitching.
     * @param fromNote to set octave nearest to
     * @return this note for chaining
     */
    Note setOctaveNearest(Note fromNote);

    /**
     * Delta +/- semitones from this Note to another Note
     * @param target note to get delta to
     * @return delta +/- semitones
     */
    [[nodiscard]] int delta(const Note &target) const;

    /**
     * Whether this note is atonal
     * @return true if the pitch class is none
     */

    [[nodiscard]] bool isAtonal() const;

    /**
     * Get the first occurrence of the given pitch class up from the current note
     * @param target pitch class to seek
     * @return first note with given pitch class up from this
     */
    Note nextUp(PitchClass target);

    /**
     * Get the first occurrence of the given pitch class down from the current note
     *
     * @param target pitch class to seek
     * @return first note with given pitch class down from this
     */
    Note nextDown(PitchClass target);

    /**
     * Get the first occurrence of the given pitch class in the given direction from the current note
     *
     * @param target pitch class to seek
     * @param delta  direction (1 or -1) in which to seek
     * @return first note with given pitch class from this
     */
    Note next(PitchClass target, int delta);
  };

}// namespace XJ

#endif// XJMUSIC_MUSIC_NOTE_H