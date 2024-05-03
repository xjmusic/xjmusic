// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJNEXUS_MUSIC_STEP_H
#define XJNEXUS_MUSIC_STEP_H

#include <map>

#include "PitchClass.h"

namespace Music {

  /**
   * Represents a
   * Step to another note, including optional +/- octave delta
   */
  class Step {
  private:
    /**
     * Map of +1 semitone for each note
     */
    static std::map<PitchClass, Step> stepUpMap;

    /**
     * Map of -1 semitone for each note
     */
    static std::map<PitchClass, Step> stepDownMap;

    /**
     * Map of delta semitones for each note
     */
    static std::map<PitchClass, std::map<PitchClass, int>> deltaMap;

  public:
    PitchClass pitchClass;
    int deltaOctave;

    /**
     * Construct a step to a particular pitch class and optional +/- octave delta
     */
    Step(PitchClass pitchClass, int deltaOctave);

    /**
     * Represents a step to a particular pitch class, optionally +/- octave
     * Preferred usage:
     * <p>
     * Step step = Step.to(PitchClass.C, -1);
     *
     * @param pitchClass  to step to
     * @param deltaOctave optional change of octave +/-
     * @return Step
     */
    static Step to(PitchClass pitchClass, int deltaOctave);

    /**
     * Nearest difference, +/- semitones, to another target pitchClass
     *
     * @param from pitch class
     * @param to pitch class
     * @return difference +/- semitones
     */
    static int delta(PitchClass from, PitchClass to);

    /**
     * Note stepped +/- semitones to a new Note
     *
     * @param inc +/- semitones to transpose
     * @return Note
     */
    static Step step(PitchClass from, int inc);

    /**
     * Note stepped + semitones to a new Note
     *
     * @param inc + semitones to transpose
     * @return Note
     */
    static Step stepUp(PitchClass from, int inc);

    /**
     * Note stepped - semitones to a new Note
     *
     * @param inc - semitones to transpose
     * @return Note
     */
    static Step stepDown(PitchClass from, int inc);

  };

}//namespace Music

#endif //XJNEXUS_MUSIC_STEP_H