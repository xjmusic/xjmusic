//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.entity;

import io.xj.core.exception.CoreException;

import java.util.Comparator;
import java.util.Objects;

/**
 This represents common properties of all entities,
 although a Chord only actually exists as a Segment Chord, Pattern Chord, etc.
 */
public interface Chord<N> {
  /**
   Sort Chords by Position in Ascending order
   */
  Comparator<? super Chord> byPositionAscending = Comparator.comparing(Chord::getPosition);

  /*
   Sort Chords by Position in Descending order
   */
//Comparator<? super Chord> byPositionDescending = (Comparator<? super Chord>) (o1, o2) -> o2.getPosition().compareTo(o1.getPosition());

  /**
   validation of common Chord attributes

   @param chord to validate
   @throws CoreException on invalid
   */
  static void validate(Chord chord) throws CoreException {
    if (Objects.isNull(chord.getName()) || chord.getName().isEmpty())
      throw new CoreException("Name is required.");

    if (Objects.isNull(chord.getPosition()))
      throw new CoreException("Position is required.");
  }

  /**
   Whether this is a chord of any tonal kind
   [#158715321] Chord nodes able to parse No Chord notation
   */
  Boolean isChord();

  /**
   Whether this is a No Chord instance
   [#158715321] Chord nodes able to parse No Chord notation
   */
  Boolean isNoChord();

  /**
   Name

   @return name
   */
  String getName();

  /**
   Position

   @return position
   */
  Double getPosition();

  /**
   Set name

   @param name to set
   @return this (for chaining setters)
   */
  N setName(String name);

  /**
   Set position

   @param position to set
   @return this (for chaining setters)
   */
  N setPosition(Double position);

  /**
   Returns a musical chord of the current entity, for music related operations

   @return musical chord
   */
  io.xj.music.Chord toMusical();

}
