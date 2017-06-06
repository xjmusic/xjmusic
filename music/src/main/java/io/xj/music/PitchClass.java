// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.music;

import com.google.common.collect.ImmutableMap;

import java.util.Objects;

/**
 PitchClass of pitch for a note (across all octaves)
 In music, a pitch class is a set of all pitches that are a whole number of octaves apart, e.g., the pitch class C consists of the Cs in all octaves.
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
   Map of +1 semitone for each note
   */
  private static final ImmutableMap<PitchClass, Step> stepUp = ImmutableMap.<PitchClass, Step>builder()
    .put(None, Step.to(None, 0))
    .put(C, Step.to(Cs, 0))
    .put(Cs, Step.to(D, 0))
    .put(D, Step.to(Ds, 0))
    .put(Ds, Step.to(E, 0))
    .put(E, Step.to(F, 0))
    .put(F, Step.to(Fs, 0))
    .put(Fs, Step.to(G, 0))
    .put(G, Step.to(Gs, 0))
    .put(Gs, Step.to(A, 0))
    .put(A, Step.to(As, 0))
    .put(As, Step.to(B, 0))
    .put(B, Step.to(C, 1))
    .build();
  /**
   Map of -1 semitone for each note
   */
  private static final ImmutableMap<PitchClass, Step> stepDown = ImmutableMap.<PitchClass, Step>builder()
    .put(None, Step.to(None, 0))
    .put(C, Step.to(B, -1))
    .put(Cs, Step.to(C, 0))
    .put(D, Step.to(Cs, 0))
    .put(Ds, Step.to(D, 0))
    .put(E, Step.to(Ds, 0))
    .put(F, Step.to(E, 0))
    .put(Fs, Step.to(F, 0))
    .put(G, Step.to(Fs, 0))
    .put(Gs, Step.to(G, 0))
    .put(A, Step.to(Gs, 0))
    .put(As, Step.to(A, 0))
    .put(B, Step.to(As, 0))
    .build();

  /**
   pitch class by name

   @param name of pitch class
   @return pitch class
   */
  public static PitchClass of(String name) {
    return baseNameOf(name)
      .step(baseDeltaOf(name))
      .getPitchClass();
  }

  /**
   Step up from a particular pitch class

   @param from transpose to transpose up from
   @return transpose up
   */
  static Step getStepUpOneFrom(PitchClass from) {
    return stepUp.get(from);
  }

  /**
   Step down from a particular pitch class

   @param from transpose to transpose down from
   @return transpose down
   */
  static Step getStepDownOneFrom(PitchClass from) {
    return stepDown.get(from);
  }

  /**
   Pitch Class based on the first character of the text

   @param text to get pitch class from
   @return pitch class
   */
  static PitchClass baseNameOf(String text) {
    if (Objects.isNull(text) || text.length() == 0)
      return None;

    switch (text.substring(0, 1)) {
      case "C":
        return C;
      case "D":
        return D;
      case "E":
        return E;
      case "F":
        return F;
      case "G":
        return G;
      case "A":
        return A;
      case "B":
        return B;
      default:
        return None;
    }
  }

  /**
   Base delta +/- semitones of the of pitch class (e.g. sharp or flat) from a basic pitch class

   @param text of of pitch class
   @return +/- semitones, e.g. sharp or flat
   */
  static int baseDeltaOf(String text) {
    if (Objects.isNull(text) || text.length() <= 1)
      return 0;

    switch (AdjSymbol.firstOf(text.substring(1))) {
      case Sharp:
        return 1;
      case Flat:
        return -1;
      default:
        return 0;
    }
  }

  /**
   Note stepped +/- semitones to a new Note

   @param inc +/- semitones to transpose
   @return Note
   */
  public Step step(int inc) {
    if (inc > 0)
      return stepUp(inc);
    else if (inc < 0)
      return stepDown(-inc);
    else
      return Step.to(this, 0);
  }

  /**
   Note stepped + semitones to a new Note

   @param inc + semitones to transpose
   @return Note
   */
  private Step stepUp(int inc) {
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
   Note stepped - semitones to a new Note

   @param inc - semitones to transpose
   @return Note
   */
  private Step stepDown(int inc) {
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
   String of the pitch class, expressed with Sharps or Flats

   @param with adjustment symbol
   @return pitch class with adjustment symbol
   */
  public String toString(AdjSymbol with) {
    return stringOf(this, with);
  }

  /**
   Nearest difference, +/- semitones, to another target pitchClass

   @param target pitch class
   @return difference +/- semitones
   */
  public int delta(PitchClass target) {
    int deltaUp = deltaDirectional(this, target, 1);
    int deltaDown = deltaDirectional(this, target, -1);
    if (Math.abs(deltaUp) < Math.abs(deltaDown))
      return deltaUp;
    else
      return deltaDown;
  }

  /**
   Nearest difference, +/- semitones, between two pitch classes

   @param from pitch class
   @param to   pitch class
   @param inc  increment, +1 or -1 (else risk infinite loop)
   @return difference +/- semitones
   */
  private static int deltaDirectional(PitchClass from, PitchClass to, int inc) {
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
   String of Pitch class, with adjustment symbol

   @param from pitch class
   @param with adjustment symbol
   @return string of pitch class
   */
  private String stringOf(PitchClass from, AdjSymbol with) {
    switch (this) {
      case C:
        return "C";
      case D:
        return "D";
      case E:
        return "E";
      case F:
        return "F";
      case G:
        return "G";
      case A:
        return "A";
      case B:
        return "B";
    }

    if (with == AdjSymbol.Sharp)
      return stringSharpOf(from);
    else if (with == AdjSymbol.Flat)
      return stringFlatOf(from);
    else
      return "-";
  }

  /**
   String of Pitch class, expressed with Sharp adjustment symbol

   @param from pitch class
   @return string adjusted with sharp
   */
  private String stringSharpOf(PitchClass from) {
    switch (from) {
      case Cs:
        return "C#";
      case Ds:
        return "D#";
      case Fs:
        return "F#";
      case Gs:
        return "G#";
      case As:
        return "A#";
      default:
        return "-";
    }
  }

  /**
   String of Pitch class, expressed with Flat adjustment symbol

   @param from pitch class
   @return string adjusted with flat
   */
  private String stringFlatOf(PitchClass from) {
    switch (from) {
      case Cs:
        return "Db";
      case Ds:
        return "Eb";
      case Fs:
        return "Gb";
      case Gs:
        return "Ab";
      case As:
        return "Bb";
      default:
        return "-";
    }
  }
}
