// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

import java.util.Objects;

/**
 A Note is used to represent the relative duration and pitch of a sound.
 <p>
 https://en.wikipedia.org/wiki/Musical_note
 */
public class Note {

  private static final int MAX_DELTA_SEMITONES = 1000; // this max is only for extreme-case infinite loop prevention
  private Integer octave; // octave #
  private PitchClass pitchClass; // pitch class of note

  /**
   Construct new empty note
   */
  public Note() {
  }

  /**
   Construct of note

   @param name of note
   */
  public Note(String name) {
    this.pitchClass = PitchClass.of(name);
    this.octave = Octave.of(name);
  }

  /**
   Construct note from pitch class and octave #

   @param pitchClass of note
   @param octave     of note
   */
  public Note(PitchClass pitchClass, int octave) {
    this.octave = octave;
    this.pitchClass = pitchClass;
  }

  /**
   Instantiate a of note

   @param name of note
   @return note
   */
  public static Note of(String name) {
    return new Note(name);
  }

  /**
   Instantiate a note by pitch class and octave

   @param pitchClass of note
   @param octave     of note
   @return note
   */
  public static Note of(PitchClass pitchClass, int octave) {
    return new Note(pitchClass, octave);
  }

  /**
   Note to String
   @param adjSymbol to represent note with
   @return string representation of Note
   */
  public String toString(AdjSymbol adjSymbol) {
    return pitchClass.toString(adjSymbol) + String.valueOf(octave);
  }

  /**
   Note stepped +/- semitones to a new Note

   @param inc +/- semitones to transpose
   @return Note
   */
  public Note transpose(int inc) {
    Step step = pitchClass.step(inc);
    return this.copy()
      .setOctave(octave + step.getDeltaOctave())
      .setPitchClass(step.getPitchClass());
  }

  /**
   Copies this object to a new Note

   @return new note
   */
  private Note copy() {
    return new Note()
      .setOctave(octave)
      .setPitchClass(pitchClass);
  }

  public PitchClass getPitchClass() {
    return pitchClass;
  }

  public Note setPitchClass(PitchClass pitchClass) {
    this.pitchClass = pitchClass;
    return this;
  }

  public Integer getOctave() {
    return octave;
  }

  public Note setOctave(Integer octave) {
    this.octave = octave;
    return this;
  }

  /**
   Copy of this note, conformed to one of the pitch classes in the given Chord

   @param chord to conform note to
   @return conformed note
   */
  public Note conformedTo(Chord chord) {
    PitchClass foundPitchClass = this.getPitchClass();
    Integer foundDelta = null;

    // for all pitch classes in chord, if it's closer, or no match has yet been found, then use it
    for (PitchClass pitchClass : chord.getPitchClasses().values()) {
      int d = Math.abs(this.getPitchClass().delta(pitchClass));
      if (Objects.isNull(foundDelta) ||
        d < foundDelta) {
        foundDelta = d;
        foundPitchClass = pitchClass;
      }
    }

    return this.copy()
      .setPitchClass(foundPitchClass);
  }

  /**
   Delta +/- semitones from this Note to another Note

   @param target note to get delta to
   @return delta +/- semitones
   */
  public Integer delta(Note target) {
    if (this.sameAs(target))
      return 0;

    int delta = 0;
    Note noteUp = copy();
    Note noteDown = copy();
    while (delta < MAX_DELTA_SEMITONES) {
      delta++;

      noteUp = noteUp.transpose(1);
      if (noteUp.sameAs(target))
        return delta;

      noteDown = noteDown.transpose(-1);
      if (noteDown.sameAs(target))
        return -delta;
    }
    return 0;
  }

  /**
   Is this note the same pitch class and octave as another note?

   @param target note to compare to
   @return true if same pitch class and octave
   */
  boolean sameAs(Note target) {
    return this.octave.equals(target.octave) && this.pitchClass.equals(target.pitchClass);
  }


}
