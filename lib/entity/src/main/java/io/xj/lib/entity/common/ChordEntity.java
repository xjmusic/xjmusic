// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity.common;

import io.xj.lib.entity.Entity;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

import java.util.Comparator;

/**
 This represents common properties of all entities,
 although a ChordEntity only actually exists as a Segment ChordEntity, Pattern ChordEntity, etc.
 */
public abstract class ChordEntity extends Entity {
  /**
   Sort Chords by Position in Ascending order
   */
  public static Comparator<? super ChordEntity> byPositionAscending = Comparator.comparing(ChordEntity::getPosition);
  private String name;
  private Double position;

  /*
   Sort Chords by Position in Descending order
   */
//Comparator<? super ChordEntity> byPositionDescending = (Comparator<? super ChordEntity>) (o1, o2) -> o2.getPosition().compareTo(o1.getPosition());

  /**
   validation of common ChordEntity attributes

   @throws ValueException on invalid
   */
  public static void validate(ChordEntity chord) throws ValueException {
    Value.require(chord.getName(), "Name");
    Value.require(chord.getPosition(), "Position");
  }

  /**
   Whether this is a chord of any tonal kind
   [#158715321] ChordEntity nodes able to parse No ChordEntity notation
   */
  public Boolean isChord() {
    return !isNoChord();
  }

  /**
   Whether this is a No ChordEntity instance
   [#158715321] ChordEntity nodes able to parse No ChordEntity notation
   */
  public Boolean isNoChord() {
    return toMusical().isNoChord();
  }

  /**
   Name

   @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   Position

   @return position
   */
  public Double getPosition() {
    return this.position;
  }

  /**
   Set name

   @param name to set
   @return this (for chaining setters)
   */
  public ChordEntity setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Set position

   @param position to set
   @return this (for chaining setters)
   */
  public ChordEntity setPosition(Double position) {
    this.position = position;
    return this;
  }

  /**
   Returns a musical chord of the current entity, for music related operations

   @return musical chord
   */
  public io.xj.lib.music.Chord toMusical() {
    return new io.xj.lib.music.Chord(name);
  }

  @Override
  public String toString() {
    return name + "@" + position;
  }

}
