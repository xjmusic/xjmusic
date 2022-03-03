// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import com.google.api.client.util.Strings;
import io.xj.lib.util.CSV;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 A Note is used to represent the relative duration and pitch of a sound.
 <p>
 https://en.wikipedia.org/wiki/Musical_note
 */
public class Note {
  public static final String ATONAL = "X";
  private static final Pattern rgxValidNote = Pattern.compile("^([ABCDEFGX][♯#♭b]*[0-9]*)$");
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
    return Strings.isNullOrEmpty(name)
      ? Note.atonal()
      : new Note(name.trim());
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
   Instantiate an atonal note

   @return atonal note
   */
  public static Note atonal() {
    return of(Note.ATONAL);
  }

  /**
   Only stream a valid note, else empty
   </>
   NC sections should not cache notes from the previous section #179409784

   @param name of note to test for validity
   @return valid note stream, or empty stream (if invalid)
   */
  public static Stream<Note> ofValid(String name) {
    return isValid(name) ? Stream.of(Note.of(name)) : Stream.empty();
  }

  /**
   Whether the current note is valid

   @param name of note to test
   @return true if valid
   */
  public static boolean isValid(String name) {
    return rgxValidNote.matcher(name).find();
  }

  /**
   Whether the CSV contains any valid notes

   @param notes to test
   @return true if contains any valid notes
   */
  public static boolean containsAnyValidNotes(String notes) {
    return CSV.split(notes).stream().anyMatch(Note::isValid);
  }

  /**
   Return the median note between the two given notes, or just one if the other is null

   @param n1 note 1
   @param n2 note 2
   @return median note between the given two
   */
  public static @Nullable
  Note median(@Nullable Note n1, @Nullable Note n2) {
    if (Objects.isNull(n1) && Objects.isNull(n2)) return null;
    if (Objects.isNull(n1)) return n2;
    if (Objects.isNull(n2)) return n1;
    return n1.shift(n1.delta(n2) / 2);
  }

  /**
   Note to String

   @param adjSymbol to represent note with
   @return string representation of Note
   */
  public String toString(AdjSymbol adjSymbol) {
    return String.format("%s%s", pitchClass.toString(adjSymbol), octave);
  }

  /**
   Note stepped +/- semitones to a new Note

   @param inc +/- semitones to transpose
   @return Note
   */
  public Note shift(int inc) {
    Step step = pitchClass.step(inc);
    return this.copy()
      .setOctave(octave + step.getDeltaOctave())
      .setPitchClass(step.getPitchClass());
  }

  /**
   Note stepped +/- octaves to a new Note

   @param inc +/- octaves to transpose
   @return Note
   */
  public Note shiftOctave(int inc) {
    return shift(12 * inc);
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
   Set the octave of this note to the one that would result in the target note
   being at most -6 or +5 semitones from the original note.
   <p>
   Here we guarantee that the target note is no more than -6 or +5 semitones away from the original audio note. Note that we are arbitrarily favoring down-pitching versus up-pitching, and that is an aesthetic decision, because it just sounds good.
   <p>
   [#303] Craft calculates drum audio pitch to conform to the allowable note closest to the original note, slightly favoring down-pitching versus up-pitching.

   @param fromNote to set octave nearest to
   @return this note for chaining
   */
  public Note setOctaveNearest(Note fromNote) {
    if (fromNote.getPitchClass().equals(PitchClass.None))
      return this;

    Note toNote = fromNote.shift(-6);
    while (!toNote.pitchClass.equals(pitchClass))
      toNote = toNote.shift(1);

    this.setOctave(toNote.octave);
    return this;
  }

  /**
   Copy of this note, conformed to one of the pitch classes in the given Chord
   <p>
   [#308] When conforming a Note to a Chord, find the absolute closest Note that conforms to the Chord's pitch classes

   @param chord to conform note to
   @return conformed note
   */
  public Note conformedTo(Chord chord) {

    if (chord.getPitchClasses().containsValue(pitchClass))
      return copy();

    int delta = 0;
    Note noteUp = copy();
    Note noteDown = copy();
    while (delta < MAX_DELTA_SEMITONES) {
      delta++;

      noteDown = noteDown.shift(-1);
      if (chord.getPitchClasses().containsValue(noteDown.getPitchClass()))
        return noteDown;

      noteUp = noteUp.shift(1);
      if (chord.getPitchClasses().containsValue(noteUp.getPitchClass()))
        return noteUp;
    }

    return copy();
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

      noteUp = noteUp.shift(1);
      if (noteUp.sameAs(target))
        return delta;

      noteDown = noteDown.shift(-1);
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
  public boolean sameAs(Note target) {
    return this.octave.equals(target.octave) && this.pitchClass.equals(target.pitchClass);
  }

  /**
   Compare two notes

   @param target to compare to
   @return comparison result
   */
  public int compareTo(Note target) {
    if (octave > target.octave) return 1;
    if (octave < target.octave) return -1;
    if (pitchClass.equals(target.pitchClass)) return 0;
    return pitchClass.compareTo(target.pitchClass) > 0 ? 1 : -1;
  }

  /**
   Is this note lower than the target note?

   @param target to compare to
   @return true if this is lower than the target
   */
  public boolean isLower(Note target) {
    return -1 == compareTo(target);
  }

  /**
   Is this note higher than the target note?

   @param target to compare to
   @return true if this is higher than the target
   */
  public boolean isHigher(Note target) {
    return 1 == compareTo(target);
  }

  /**
   Whether this note is atonal

   @return true if the pitch class is none
   */
  public boolean isAtonal() {
    return PitchClass.None.equals(pitchClass);
  }

  /**
   Get the first occurrence of the given pitch class up from the current note

   @param target pitch class to seek
   @return first note with given pitch class up from this
   */
  public Note nextUp(PitchClass target) {
    return next(target, 1);
  }

  /**
   Get the first occurrence of the given pitch class down from the current note

   @param target pitch class to seek
   @return first note with given pitch class down from this
   */
  public Note nextDown(PitchClass target) {
    return next(target, -1);
  }

  /**
   Get the first occurrence of the given pitch class in the given direction from the current note

   @param target pitch class to seek
   @param delta  direction (1 or -1) in which to seek
   @return first note with given pitch class from this
   */
  private Note next(PitchClass target, int delta) {
    if (isAtonal() || Objects.equals(pitchClass, target))
      return this;

    Note n;
    for (var i = 1; Math.abs(i) < MAX_DELTA_SEMITONES; i += delta) {
      n = this.shift(i);
      if (Objects.equals(n.getPitchClass(), target))
        return n;
    }

    throw new RuntimeException(String.format("Unable to determine first occurrence of %s from here!", target));
  }
}
