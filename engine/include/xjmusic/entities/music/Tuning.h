// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_MUSIC_TUNING_H
#define XJMUSIC_MUSIC_TUNING_H

#include <map>
#include <string>
#include <cmath>

#include "Note.h"

namespace XJ {

/**
 * Provides exact pitch for any Note, in Hz.
 * A `Tuning` instance is fixed to a given tuning of note A4, in Hz.
 * Computations for notes at that tuning are cached in memory.
 * <p>
 * [#255] Note pitch is calculated at 32-bit floating point precision, based on root note configured in environment parameters.
 * <p>
 * Reference: http://www.phy.mtu.edu/~suits/notefreqs.html
 */
  class Tuning {
  private:
    double TWELFTH_ROOT_OF_TWO = std::pow(2.0, 1.0 / 12.0);
    double NATURAL_LOGARITHM_OF_TWELFTH_ROOT_OF_TWO = std::log(TWELFTH_ROOT_OF_TWO);
    float ROOT_PITCH_MINIMUM = 1.0;
    float ROOT_PITCH_MAXIMUM = 100000.0;
    int ROOT_OCTAVE_MINIMUM = 0;
    int ROOT_OCTAVE_MAXIMUM = 15;
    std::map <int, std::map<PitchClass, float>> _notePitches;
    std::map <float, int> _deltaFromRootPitch;
    std::map <float, Note> _pitchNotes;

  public:
    Note rootNote;
    float rootPitch;

    /**
     * Default constructor
     */
    Tuning();

    /**
     * constructor
     */
    Tuning(const Note &rootNote, const float &rootPitch);

    /**
     * A `Tuning` instance, fixed to a given tuning of note A4, in Hz.
     *
     * @param a4 tuning of note A4, in Hz
     * @return a Tuning instance ready to provide exact pitch for any note, in Hz
     */
    static Tuning atA4(float a4);

    /**
     * A `Tuning` instance, fixed to a given tuning of note A4, in Hz.
     *
     * @param note  to use as the root of the tuning
     * @param pitch of the root note
     * @return a Tuning instance ready to provide exact pitch for any note, in Hz
     */
    static Tuning at(Note note, float pitch);

    /**
     * Pitch for any Note, in Hz
     * (caches results by octave and pitch class)
     *
     * @param note to get pitch for
     * @return pitch of note, in Hz
     */
    float pitch(Note note);

    /**
     * Closest Note, for any pitch in Hz
     *
     * @param pitch to get octave of
     */
    Note getTones(float pitch);

    /**
     * delta, +/- semitones, from the root pitch to the target pitch
     *
     * @param pitch to get delta of
     * @return delta +/- semitones
     */
    int deltaFromRootPitch(float pitch);

    /**
     * Pitch in Hz, for +/- semitones from known root pitch
     *
     * @param delta +/- semitones from root pitch
     * @return pitch
     */
    [[nodiscard]] float pitchAtDelta(int delta) const;

    /**
     * validate after construction
     *
     * @ if any properties are invalid
     */
    void validate() const;
  };

}// namespace XJ

#endif //XJMUSIC_MUSIC_TUNING_H