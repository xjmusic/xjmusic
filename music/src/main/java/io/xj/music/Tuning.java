// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.music;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;

/**
 Provides exact pitch for any Note, in Hz.
 A `Tuning` instance is fixed to a given tuning of note A4, in Hz.
 Computations for notes at that tuning are cached in memory.
 <p>
 [#255] Note pitch is calculated at 32-bit floating point precision, based on root note configured in environment parameters.
 <p>
 Reference: http://www.phy.mtu.edu/~suits/notefreqs.html
 */
public class Tuning {
  private static final double TWELFTH_ROOT_OF_TWO = Math.pow(2d, 1d / 12d);
  private static final double NATURAL_LOGARITHM_OF_TWELFTH_ROOT_OF_TWO = Math.log(TWELFTH_ROOT_OF_TWO);
  private static final double ROOT_PITCH_MINIMUM = 1d;
  private static final double ROOT_PITCH_MAXIMUM = 100000d;
  private static final int ROOT_OCTAVE_MINIMUM = 0;
  private static final int ROOT_OCTAVE_MAXIMUM = 15;
  private static final PitchClass ROOT_OCTAVE_PITCH_CLASS = PitchClass.C;
  private final Note rootNote;
  private final Double rootPitch;
  //  private Map<Integer, Double> _pitches = Maps.newHashMap();
  private Map<Integer, Map<PitchClass, Double>> _notePitches = Maps.newHashMap();
  private Map<Double, Integer> _deltaFromRootPitch = Maps.newHashMap();
  private Map<Double, Note> _pitchNotes = Maps.newHashMap();

  /**
   A `Tuning` instance, fixed to a given tuning of note A4, in Hz.

   @param a4 tuning of note A4, in Hz
   @return a Tuning instance ready to provide exact pitch for any note, in Hz
   */
  public static Tuning atA4(double a4) throws MusicalException {
    return new Tuning(Note.of("A4"), a4);
  }

  /**
   A `Tuning` instance, fixed to a given tuning of note A4, in Hz.

   @param note  to use as the root of the tuning
   @param pitch of the root note
   @return a Tuning instance ready to provide exact pitch for any note, in Hz
   */
  public static Tuning at(Note note, Double pitch) throws MusicalException {
    return new Tuning(note, pitch);
  }

  /**
   Pitch for any Note, in Hz
   (caches results by octave and pitch class)

   @param note to get pitch for
   @return pitch of note, in Hz
   */
  public double pitch(Note note) {
    Integer octave = note.getOctave();
    PitchClass pitchClass = note.getPitchClass();

    if (!_notePitches.containsKey(octave))
      _notePitches.put(octave, Maps.newHashMap());

    if (!_notePitches.get(octave).containsKey(pitchClass))
      _notePitches.get(octave).put(pitchClass,
        pitchAtDelta(rootNote.delta(note)));

    return _notePitches.get(octave).get(pitchClass);
  }

  /**
   Closest Note, for any pitch in Hz

   @param pitch to get octave of
   */
  public Note note(Double pitch) {
    if (!_pitchNotes.containsKey(pitch))
      _pitchNotes.put(pitch,
        rootNote.transpose(deltaFromRootPitch(pitch)));

    return _pitchNotes.get(pitch);
  }

  /**
   delta, +/- semitones, from the root pitch to the target pitch

   @param pitch to get delta of
   @return delta +/- semitones
   */
  public Integer deltaFromRootPitch(Double pitch) {
    if (!_deltaFromRootPitch.containsKey(pitch))
      _deltaFromRootPitch.put(pitch,
        (int) (Math.log(pitch / rootPitch) / NATURAL_LOGARITHM_OF_TWELFTH_ROOT_OF_TWO));

    return _deltaFromRootPitch.get(pitch);
  }

  /**
   Pitch in Hz, for +/- semitones from known root pitch

   @param delta +/- semitones from root pitch
   @return pitch
   */
  private Double pitchAtDelta(Integer delta) {
    return rootPitch * Math.pow(TWELFTH_ROOT_OF_TWO, delta);
  }

  /**
   private constructor
   */
  private Tuning(Note rootNote, Double rootPitch) throws MusicalException {
    this.rootNote = rootNote;
    this.rootPitch = rootPitch;
    validate();
  }

  /**
   validate after construction

   @throws MusicalException if any properties are invalid
   */
  private void validate() throws MusicalException {
    if (Objects.isNull(rootNote))
      throw new MusicalException("Must specify a root note for tuning");

    if (Objects.isNull(rootPitch))
      throw new MusicalException("Must specify a root pitch (in Hz) for tuning");

    if (!(rootPitch >= ROOT_PITCH_MINIMUM && rootPitch <= ROOT_PITCH_MAXIMUM))
      throw new MusicalException(
        String.format("Root pitch must be between %f and %f (Hz)",
          ROOT_PITCH_MINIMUM, ROOT_PITCH_MAXIMUM));

    if (Objects.isNull(rootNote.getPitchClass()) ||
      rootNote.getPitchClass().equals(PitchClass.None))
      throw new MusicalException("Root note must have a pitch class (e.g. 'C')");

    if (!(rootNote.getOctave() >= ROOT_OCTAVE_MINIMUM && rootNote.getOctave() <= ROOT_OCTAVE_MAXIMUM))
      throw new MusicalException(
        String.format("Root note octave must be between %d and %d",
          ROOT_OCTAVE_MINIMUM, ROOT_OCTAVE_MAXIMUM));
  }
}
