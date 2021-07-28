// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import java.util.Objects;

/**
 Key is a model of a musical key signature
 <p>
 The key of a piece is a group of pitches, or scale upon which a music composition is createdin classical, Western art, and Western pop music.
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

  /**
   delta +/- semitones from a Key (string) to another Key (string)

   @param fromKey to compute delta from
   @param toKey   to compute delta to
   @param adjust  +/- semitones adjustment
   @return delta from one key to another
   */
  public static Integer delta(String fromKey, String toKey, int adjust) {
    return of(fromKey).delta(of(toKey).transpose(adjust));
  }

  /**
   Increased score for a matching adjustment symbol,
   only if one is provided.

   @param key1 to match, or null if none matters
   @param key2 to match adjustment symbol of
   @return increased score if match, else 0
   */
  public static Boolean isSameMode(String key1, String key2) {
    return Objects.nonNull(key1) && isSameMode(Key.of(key1), Key.of(key2));
  }

  /**
   Increased score for a matching adjustment symbol,
   only if one is provided.

   @param key1 to match, or null if none matters
   @param key2 to match adjustment symbol of
   @return increased score if match, else 0
   */
  public static Boolean isSameMode(Key key1, Key key2) {
    return Objects.nonNull(key1) &&
      key2.getMode().equals(
        key1.getMode());
  }

  @Override
  protected void parseSchema(String text) {
    this.mode = KeyMode.of(text);
  }

  /**
   Copies this object to a new Note

   @return new note
   */
  public Key copy() {
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

  /**
   get mode

   @return mode
   */
  public KeyMode getMode() {
    return mode;
  }

  /**
   set mode

   @param mode of key
   @return key (for chaining setters)
   */
  public Key setMode(KeyMode mode) {
    this.mode = mode;
    return this;
  }

  /**
   set root pitch class

   @param root pitch class
   @return key (for chaining setters)
   */
  public Key setRootPitchClass(PitchClass root) {
    this.root = root;
    return this;
  }

  /**
   set adjustment symbol

   @param adjSymbol adjustment symbol
   @return key (for chaining setters)
   */
  public Key setAdjSymbol(AdjSymbol adjSymbol) {
    this.adjSymbol = adjSymbol;
    return this;
  }

  /**
   set original description

   @param description of key
   @return key (for chaining setters)
   */
  public Key setOriginalDescription(String description) {
    this.description = description;
    return this;
  }

  /**
   set name

   @param name of key
   @return key (for chaining setters)
   */
  public Key setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Whether this Key is null

   @return true if non-null
   */
  public boolean isPresent() {
    return Objects.nonNull(root);
  }
}
