// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chord;

import io.xj.core.exception.CoreException;

import java.util.Objects;

/**
 This represents common properties of all entities,
 although a Chord only actually exists as a Segment Chord, Pattern Chord, etc.
 */
public interface Chord<N> {

  /**
   validation of common Chord attributes

   @param chord to validate
   @throws CoreException on invalid
   */
  static void validate(Chord chord) throws CoreException {
    if (Objects.isNull(chord.getName()) || chord.getName().isEmpty()) {
      throw new CoreException("Name is required.");
    }
    if (Objects.isNull(chord.getPosition())) {
      throw new CoreException("Position is required.");
    }
  }

  /**
   Must have validation method

   @throws CoreException on invalid
   */
  void validate() throws CoreException;

  /**
   Returns a musical chord of the current entity, for music related operations

   @return musical chord
   */
  io.xj.music.Chord toMusical();

  /**
   Name

   @return name
   */
  String getName();

  /**
   Set name

   @param name to set
   @return this (for chaining setters)
   */
  N setName(String name);

  /**
   Position

   @return position
   */
  Double getPosition();

  /**
   Set position

   @param position to set
   @return this (for chaining setters)
   */
  N setPosition(Double position);

  /**
   String of Chord

   @return string
   */
  String toString();

  /**
   Whether this is a No Chord instance
   [#158715321] Chord nodes able to parse No Chord notation
   */
  Boolean isNoChord();

  /**
   Whether this is a chord of any tonal kind
   [#158715321] Chord nodes able to parse No Chord notation
   */
  Boolean isChord();

}
