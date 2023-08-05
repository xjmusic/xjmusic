// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.music;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * PitchClass of pitch for a note (across all octaves)
 * In music, a pitch class is a set of all pitches that are a whole number of octaves apart, e.g., the pitch class C consists of the Cs in all octaves.
 */
public enum PitchClass {
  None,
  C,
  Cs,
  D,
  Ds,
  E,
  F,
  Fs,
  G,
  Gs,
  A,
  As,
  B;

  /**
   * Map of +1 semitone for each note
   */
  static final Map<PitchClass, Step> stepUp = buildStepUpMap();

  /**
   * Map of -1 semitone for each note
   */
  static final Map<PitchClass, Step> stepDown = buildStepDownMap();


  @SuppressWarnings("DuplicatedCode")
  static Map<PitchClass, Step> buildStepUpMap() {
    Map<PitchClass, Step> map = new HashMap<>();
    map.put(None, Step.to(None, 0));
    map.put(C, Step.to(Cs, 0));
    map.put(Cs, Step.to(D, 0));
    map.put(D, Step.to(Ds, 0));
    map.put(Ds, Step.to(E, 0));
    map.put(E, Step.to(F, 0));
    map.put(F, Step.to(Fs, 0));
    map.put(Fs, Step.to(G, 0));
    map.put(G, Step.to(Gs, 0));
    map.put(Gs, Step.to(A, 0));
    map.put(A, Step.to(As, 0));
    map.put(As, Step.to(B, 0));
    map.put(B, Step.to(C, 1));
    return map;
  }

  @SuppressWarnings("DuplicatedCode")
  static Map<PitchClass, Step> buildStepDownMap() {
    Map<PitchClass, Step> map = new HashMap<>();
    map.put(None, Step.to(None, 0));
    map.put(C, Step.to(B, -1));
    map.put(Cs, Step.to(C, 0));
    map.put(D, Step.to(Cs, 0));
    map.put(Ds, Step.to(D, 0));
    map.put(E, Step.to(Ds, 0));
    map.put(F, Step.to(E, 0));
    map.put(Fs, Step.to(F, 0));
    map.put(G, Step.to(Fs, 0));
    map.put(Gs, Step.to(G, 0));
    map.put(A, Step.to(Gs, 0));
    map.put(As, Step.to(A, 0));
    map.put(B, Step.to(As, 0));
    return map;
  }

  /**
   * pitch class by name
   *
   * @param name of pitch class
   * @return pitch class
   */
  public static PitchClass of(String name) {
    return baseNameOf(name)
      .step(baseDeltaOf(name))
      .getPitchClass();
  }

  /**
   * Step up from a particular pitch class
   *
   * @param from transpose to transpose up from
   * @return transpose up
   */
  static Step getStepUpOneFrom(PitchClass from) {
    return stepUp.get(from);
  }

  /**
   * Step down from a particular pitch class
   *
   * @param from transpose to transpose down from
   * @return transpose down
   */
  static Step getStepDownOneFrom(PitchClass from) {
    return stepDown.get(from);
  }

  /**
   * Pitch Class based on the first character of the text
   *
   * @param text to get pitch class from
   * @return pitch class
   */
  static PitchClass baseNameOf(String text) {
    if (Objects.isNull(text) || text.length() == 0)
      return None;

    return switch (text.substring(0, 1)) {
      case "C" -> C;
      case "D" -> D;
      case "E" -> E;
      case "F" -> F;
      case "G" -> G;
      case "A" -> A;
      case "B" -> B;
      default -> None;
    };
  }

  /**
   * Base delta +/- semitones of the of pitch class (e.g. sharp or flat) from a basic pitch class
   *
   * @param text of pitch class
   * @return +/- semitones, e.g. sharp or flat
   */
  static int baseDeltaOf(String text) {
    if (Objects.isNull(text) || text.length() <= 1)
      return 0;

    return switch (Accidental.firstOf(text.substring(1))) {
      case Sharp -> 1;
      case Flat -> -1;
      default -> 0;
    };
  }

  /**
   * Nearest difference, +/- semitones, between two pitch classes
   *
   * @param from pitch class
   * @param to   pitch class
   * @param inc  increment, +1 or -1 (else risk infinite loop)
   * @return difference +/- semitones
   */
  static int deltaDirectional(PitchClass from, PitchClass to, int inc) {
    if (from == None)
      return 0;

    int delta = 0;
    while (true) {
      if (from == to) {
        return delta;
      }
      delta += inc;
      from = from.step(inc).getPitchClass();
    }
  }

  /**
   * Note stepped +/- semitones to a new Note
   *
   * @param inc +/- semitones to transpose
   * @return Note
   */
  public Step step(int inc) {
    if (0 < inc)
      return stepUp(inc);
    else if (0 > inc)
      return stepDown(-inc);
    else
      return Step.to(this, 0);
  }

  /**
   * Note stepped + semitones to a new Note
   *
   * @param inc + semitones to transpose
   * @return Note
   */
  Step stepUp(int inc) {
    PitchClass newPitchClass = this;
    int newOctave = 0;
    for (int i = 0; i < inc; i++) {
      Step step = PitchClass.getStepUpOneFrom(newPitchClass);
      newPitchClass = step.getPitchClass();
      newOctave += step.getDeltaOctave();
    }
    return Step.to(newPitchClass, newOctave);
  }

  /**
   * Note stepped - semitones to a new Note
   *
   * @param inc - semitones to transpose
   * @return Note
   */
  Step stepDown(int inc) {
    PitchClass newPitchClass = this;
    int newOctave = 0;
    for (int i = 0; i < inc; i++) {
      Step step = PitchClass.getStepDownOneFrom(newPitchClass);
      newPitchClass = step.getPitchClass();
      newOctave += step.getDeltaOctave();
    }
    return Step.to(newPitchClass, newOctave);
  }

  /**
   * String of the pitch class, expressed with Sharps or Flats
   *
   * @param with adjustment symbol
   * @return pitch class with adjustment symbol
   */
  public String toString(Accidental with) {
    return stringOf(this, with);
  }

  /**
   * Nearest difference, +/- semitones, to another target pitchClass
   *
   * @param target pitch class
   * @return difference +/- semitones
   */
  public int delta(PitchClass target) {
    if (Objects.equals(None, this)) return 0;
    if (Objects.equals(None, target)) return 0;
    int deltaUp = deltaDirectional(this, target, 1);
    int deltaDown = deltaDirectional(this, target, -1);
    if (Math.abs(deltaUp) < Math.abs(deltaDown))
      return deltaUp;
    else
      return deltaDown;
  }

  /**
   * String of Pitch class, with adjustment symbol
   *
   * @param from pitch class
   * @param with adjustment symbol
   * @return string of pitch class
   */
  String stringOf(PitchClass from, Accidental with) {
    return switch (this) {
      case C -> "C";
      case D -> "D";
      case E -> "E";
      case F -> "F";
      case G -> "G";
      case A -> "A";
      case B -> "B";
      default -> {
        if (with == Accidental.Sharp)
          yield stringSharpOf(from);
        else if (with == Accidental.Flat)
          yield stringFlatOf(from);
        else
          yield "X";
      }
    };
  }

  /**
   * String of Pitch class, expressed with Sharp adjustment symbol
   *
   * @param from pitch class
   * @return string adjusted with sharp
   */
  String stringSharpOf(PitchClass from) {
    return switch (from) {
      case Cs -> "C#";
      case Ds -> "D#";
      case Fs -> "F#";
      case Gs -> "G#";
      case As -> "A#";
      default -> "X";
    };
  }

  /**
   * String of Pitch class, expressed with Flat adjustment symbol
   *
   * @param from pitch class
   * @return string adjusted with flat
   */
  String stringFlatOf(PitchClass from) {
    return switch (from) {
      case Cs -> "Db";
      case Ds -> "Eb";
      case Fs -> "Gb";
      case Gs -> "Ab";
      case As -> "Bb";
      default -> "X";
    };
  }
}
