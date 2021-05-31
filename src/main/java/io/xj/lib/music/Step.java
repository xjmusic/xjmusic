// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

/**
 Represents a
 Step to another pitch class, including optional +/- octave delta
 */
public class Step {
  private final PitchClass pitchClass;
  private final Integer deltaOctave;

  /**
   Represents a step to a particular pitch class, optionally +/- octave
   Preferred usage:
   <p>
   Step step = Step.to(PitchClass.C, -1);

   @param pitchClass  to step to
   @param deltaOctave optional change of octave +/-
   @return Step
   */
  public static Step to(PitchClass pitchClass, Integer deltaOctave) {
    return new Step(pitchClass, deltaOctave);
  }

  /**
   Pitch Class to step to

   @return Pitch Class
   */
  public PitchClass getPitchClass() {
    return pitchClass;
  }

  /**
   +/- Octave to step to

   @return +/- octave
   */
  public Integer getDeltaOctave() {
    return deltaOctave;
  }

  /**
   Private constructor
   */
  private Step(PitchClass pitchClass, Integer deltaOctave) {
    this.pitchClass = pitchClass;
    this.deltaOctave = deltaOctave;
  }

}
