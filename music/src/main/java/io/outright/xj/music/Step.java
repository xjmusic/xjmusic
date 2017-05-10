// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

/**
 Step to another pitch class, including optional +/- octave delta
 */
public class Step {
  private PitchClass pitchClass;
  private Integer deltaOctave;

  private Step(PitchClass pitchClass, Integer deltaOctave) {
    this.pitchClass = pitchClass;
    this.deltaOctave = deltaOctave;
  }

  /**
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

  public PitchClass getPitchClass() {
    return pitchClass;
  }

  public Integer getDeltaOctave() {
    return deltaOctave;
  }
}
