// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

/**
 A Note is used to represent the relative duration and pitch of a sound.
 <p>
 https://en.wikipedia.org/wiki/Musical_note
 */
public class Note {

  private Integer octave; // octave #
  private PitchClass pitchClass; // pitch class of note
  private String performer; // Can be used to sort out whose Notes are whose
  private Double position; // Can be used to represent time within the composition
  private Double duration; // Can be used to represent time of note duration
  private String code; // Can be used to store any custom values

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
   Note stepped +/- semitones to a new Note

   @param inc +/- semitones to step
   @return Note
   */
  public Note step(int inc) {
    Step step = pitchClass.step(inc);
    return this.copy()
      .setPitchClass(step.getPitchClass())
      .setOctave(octave + step.getDeltaOctave());
  }

  /**
   Copies this object to a new Note

   @return new note
   */
  private Note copy() {
    return new Note()
      .setCode(code)
      .setDuration(duration)
      .setOctave(octave)
      .setPerformer(performer)
      .setPitchClass(pitchClass)
      .setPosition(position);
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

  public String getPerformer() {
    return performer;
  }

  public Note setPerformer(String performer) {
    this.performer = performer;
    return this;
  }

  public Double getPosition() {
    return position;
  }

  public Note setPosition(Double position) {
    this.position = position;
    return this;
  }

  public Double getDuration() {
    return duration;
  }

  public Note setDuration(Double duration) {
    this.duration = duration;
    return this;
  }

  public String getCode() {
    return code;
  }

  public Note setCode(String code) {
    this.code = code;
    return this;
  }
}
