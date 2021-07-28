// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity.common;

import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;

/**
 This represents common properties of all entities,
 although a ChordEntity only actually exists as a Segment ChordEntity, Pattern ChordEntity, etc.
 */
public class ChordEntity {
  private String name;
  private Double position;

  /**
   validation of common ChordEntity attributes

   @throws ValueException on invalid
   */
  public static void validate(Object chord) throws ValueException {
    try {
      Value.require(Entities.get(chord, "name"), "Name");
      Value.require(Entities.get(chord, "position"), "Position");
    } catch (EntityException e) {
      throw new ValueException(e);
    }
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
   Set name

   @param name to set
   @return this (for chaining setters)
   */
  public ChordEntity setName(String name) {
    this.name = name;
    return this;
  }

  /**
   Position

   @return position
   */
  public Double getPosition() {
    return this.position;
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
