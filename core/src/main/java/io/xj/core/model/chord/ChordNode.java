// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.chord;

import com.google.common.base.Splitter;

import io.xj.music.Key;
import io.xj.music.PitchClass;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public class ChordNode {
  private static final int MAX_SEMITONES = 12;
  private static final String NOTHING = "";
  private static final Pattern NON_CHORD_DESCRIPTOR = Pattern.compile("[^a-zA-Z0-9+\\-#♭ ]");
  private final String form;
  private Long weight;
  private final Integer delta; // NOTE: delta is null when this is the first unit of a sequence.

  /**
   Construct a "null" chord descriptor unit with NO chord, and NO delta.
   This is used as a "bookend" marker, e.g. meaning "phase has ended" during chord markov computation
   */
  public ChordNode() {
    form = null;
    delta = null;
    weight = 0L;
  }

  /**
   Construct chord descriptor unit with only a chord (and no delta).
   NOTE: delta is null when this is the first unit of a sequence.

   @param chord to construct descriptor of
   */
  ChordNode(Chord chord) {
    form = formOf(chord);
    delta = null;
    weight = 1L;
  }

  /**
   Construct chord descriptor unit with a delta and a chord.
   Delta is from last chord, in semitones modulo (probably 12).

   @param prevChord previous chord
   @param chord     chord to construct descriptor of
   */
  ChordNode(Chord prevChord, Chord chord) {
    form = formOf(chord);
    delta = deltaSemitonesModulo(prevChord, chord);
    weight = 1L;
  }

  /**
   Construct chord descriptor unit from a key and a chord.
   Delta is relative to Key, in semitones modulo (probably 12).

   @param key   relative to which each chord's root will be computed in semitones modulo
   @param chord chord to construct descriptor of
   */
  public ChordNode(Key key, Chord chord) {
    form = formOf(chord);
    delta = deltaSemitonesModulo(key, chord);
    weight = 1L;
  }


  /**
   Construct a chord description unit reverse-engineered from an output descriptor string
   Expects input like "Major" or "7|Minor"
   NOTE: delta is null when this is the first unit of a sequence.

   @param descriptor to reverse engineer into a chord descriptor unit
   */
  ChordNode(String descriptor) {
    List<String> pieces = Splitter.on(Chord.SEPARATOR_DESCRIPTOR_UNIT).splitToList(descriptor);
    if (2 == pieces.size()) {
      delta = Integer.valueOf(pieces.get(0));
      form = formOf(pieces.get(1));
    } else if (1 == pieces.size()) {
      form = formOf(pieces.get(0));
      delta = null;
    } else {
      form = "";
      delta = null;
    }
    weight = 1L;
  }

  /**
   Compute Δ semitones modulo n from root chord to add chord

   @param key   relative to which each chord's root will be computed in semitones modulo
   @param chord compute Δ to
   @return Δ semitones modulo n (where n = max # of semitones, probably 12)
   */
  private Integer deltaSemitonesModulo(Key key, Chord chord) {
    return deltaSemitonesModulo(key.getRootPitchClass(), chord.toMusical().getRootPitchClass());
  }

  /**
   Compute Δ semitones modulo n from root chord to add chord

   @param addChord compute Δ to
   @return Δ semitones modulo n (where n = max # of semitones, probably 12)
   */
  private int deltaSemitonesModulo(Chord prevChord, Chord addChord) {
    return deltaSemitonesModulo(prevChord.toMusical().getRootPitchClass(), addChord.toMusical().getRootPitchClass());
  }

  /**
   Compute Δ semitones modulo n from root chord to add chord

   @param next compute Δ to
   @return Δ semitones modulo n (where n = max # of semitones, probably 12)
   */
  private int deltaSemitonesModulo(PitchClass prev, PitchClass next) {
    return Math.floorMod(prev.delta(next), MAX_SEMITONES);
  }

  /**
   Compute descriptor.

   @param chord to compute descriptor of
   @return descriptor
   */
  private String formOf(Chord chord) {
    return formOf(chord.toMusical().colloquialFormName());
  }

  /**
   Compute descriptor.

   @param form to sanitize descriptor of
   @return descriptor
   */
  private static String formOf(String form) {
    return NON_CHORD_DESCRIPTOR.matcher(form)
      .replaceAll(NOTHING);
  }

  /**
   Get the weight of this node
   @return weight
   */
  public Long getWeight() {
    return weight;
  }

  /**
   Get the weight of this node
   */
  public void addWeight(ChordNode node) {
    weight += node.getWeight();
  }

  /**
   Get chord form.

   @return chord form
   */
  @Nullable
  public String getForm() {
    return form;
  }

  /**
   Get unit delta from last chord, in semitones modulo (probably 12).
   NOTE: delta is null when this is the first unit of a sequence.

   @return unit delta from last chord, in semitones modulo (probably 12).
   */
  @Nullable
  public Integer getDelta() {
    return delta;
  }

  /**
   Render as a string
   // Ba. append primary separator + Cn Δ semitones (modulo)
   // Bb. append secondary separator + append Cn form

   @return string of chord descriptor unit
   */
  @Override
  public String toString() {
    if (Objects.nonNull(delta) && Objects.nonNull(form))
      return String.format("%d%s%s", delta, Chord.SEPARATOR_DESCRIPTOR_UNIT, form);
    else if (Objects.nonNull(form))
      return form;
    else
      return Chord.MARKER_NON_CHORD;
  }

  /**
   Whether another chord descriptor unit is equivalent to this one.
   Compares form and delta separately, where a null delta acts as a match-all wildcard.

   @param other to match
   @return true if equivalent
   */
  boolean isEquivalentTo(ChordNode other) {
    if (!Objects.equals(form, other.getForm())) return false;
    if (Objects.isNull(delta) || Objects.isNull(other.getDelta())) return true;
    return Objects.equals(delta, other.getDelta());
  }

  /**
   Is this a chord (as opposed to a null "bookend" marker)?

   @return true if this is a chord (NOT a null marker)
   */
  public boolean isChord() {
    return Objects.nonNull(form);
  }
}
