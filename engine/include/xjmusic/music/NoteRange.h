// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_MUSIC_NOTE_RANGE_H
#define XJMUSIC_MUSIC_NOTE_RANGE_H

#include "Note.h"

namespace Music {

  /**
   * Represent a note range
   */
  class NoteRange {
  private:
    std::string UNKNOWN = "Unknown";
    int MAX_SEEK_OCTAVES = 3;

  public:

    std::optional<Note> low;

    std::optional<Note> high;

    NoteRange();

    NoteRange(std::optional<Note> low, std::optional<Note> high);

    NoteRange(const std::string &low, const std::string &high);

    static NoteRange from(Note low, Note high);

    static NoteRange from(const std::string &low, const std::string &high);

    static NoteRange copyOf(const NoteRange &range);

    static NoteRange ofNotes(std::vector<Note> notes);

    static NoteRange ofStrings(const std::vector<std::string> &notes);

    static NoteRange median(const NoteRange &r1, const NoteRange &r2);

    static NoteRange empty();

    /**
     * Compute the median optimal range shift octaves
     *
     * @param sourceRange from
     * @param targetRange to
     * @return median optimal range shift octaves
     */
    static int computeMedianOptimalRangeShiftOctaves(NoteRange sourceRange, NoteRange targetRange);

    std::string toString(Accidental accidental);

    void expand(const std::vector<Note> &notes);

    /**
     * Expand the note range to include the given note
     * @param note  to include
     */
    void expand(Note note);

    /**
     * Expand the note range to include the given range
     * @param range  to include
     */
    void expand(const NoteRange &range);

    /**
     * Get the delta semitones between this range and the target
     * @param target  to compare
     * @return        delta semitones
     */
    int getDeltaSemitones(NoteRange target);

    /**
     * Get the median note of this range
     * @return  median note
     */
    std::optional<Note> getMedianNote();

    /**
     * Get this range shifted by the given number of semitones
     * @param inc  semitones to shift
     * @return     shifted range
     */
    NoteRange shifted(int inc);

    /**
     * Whether this range is empty
     * @return  true if empty
     */
    bool isEmpty();

    /**
     * Get the note nearest the median of the given root
     * @param root  to find nearest note
     * @return      note nearest the median
     */
    std::optional<Note> getNoteNearestMedian(PitchClass root);

    /**
     * Change the octave of a note such that it is within this range
     *
     * @param note source
     * @return note moved to available octave
     */
    Note toAvailableOctave(Note note);

    /**
     * Whether the given note is within this range
     *
     * @param note to test
     * @return true if note is within this range
     */
    bool includes(Note note);
  };

}// namespace Music

#endif// XJMUSIC_MUSIC_NOTE_RANGE_H