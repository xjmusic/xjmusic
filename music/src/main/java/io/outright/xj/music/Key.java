// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

import io.outright.xj.music.schema.IntervalPitchGroup;
import io.outright.xj.music.schema.KeyMode;

/**
 Key is a model of a musical key signature
 <p>
 The key of a piece is a group of pitches, or scale upon which a music composition is created in classical, Western art, and Western pop music.
 <p>
 https://en.wikipedia.org/wiki/Key_(music)
 */
public class Key extends IntervalPitchGroup {
  private KeyMode mode;

  /**
   Construct a new Key, by name

   @param name of key
   */
  private Key(String name) {
    super(name);
  }

  /**
   Construct a new empty Key
   */
  private Key() {
    super("");
  }

  /**
   Instantiate a new Key by name, e.g. of("C minor 7")

   @param name of key
   @return Key
   */
  public static Key of(String name) {
    return new Key(name);
  }

  @Override
  protected void parseSchema(String text) {
    this.mode = KeyMode.of(text);
  }

  /**
   Copies this object to a new Note

   @return new note
   */
  private Key copy() {
    return new Key()
      .setOriginalDescription(getOriginalDescription())
      .setRootPitchClass(root)
      .setAdjSymbol(adjSymbol);
  }

  /**
   Transpose a chord +/- semitones
   */
  public Key transpose(int deltaSemitones) {
    return copy()
      .setAdjSymbol(adjSymbol)
      .setRootPitchClass(this.root.step(deltaSemitones).getPitchClass());
  }

  /**
   Get this key's relative minor key

   @return relative minor key
   */
  public Key relativeMinor() {
    return new Key()
      .setAdjSymbol(AdjSymbol.Flat)
      .setRootPitchClass(root.step(-3).getPitchClass())
      .setMode(KeyMode.Minor);
  }

  /**
   Get this key's relative major key

   @return relative major key
   */
  public Key relativeMajor() {
    return new Key()
      .setAdjSymbol(AdjSymbol.Sharp)
      .setRootPitchClass(root.step(3).getPitchClass())
      .setMode(KeyMode.Major);
  }

  private Key setRootPitchClass(PitchClass root) {
    this.root = root;
    return this;
  }

  private Key setAdjSymbol(AdjSymbol adjSymbol) {
    this.adjSymbol = adjSymbol;
    return this;
  }

  public KeyMode getMode() {
    return mode;
  }

  private Key setMode(KeyMode mode) {
    this.mode = mode;
    return this;
  }

  private Key setOriginalDescription(String description) {
    this.description = description;
    return this;
  }

  public void setName(String name) {
    this.name = name;
  }
}
