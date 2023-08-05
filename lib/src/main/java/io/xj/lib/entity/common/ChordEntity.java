// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.entity.common;

import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.ValueUtils;

/**
 * This represents common properties of all entities,
 * although a ChordEntity only actually exists as a Segment ChordEntity, Pattern ChordEntity, etc.
 */
public class ChordEntity {
  String name;
  Double position;

  /**
   * validation of common ChordEntity attributes
   *
   * @throws ValueException on invalid
   */
  public static void validate(Object chord) throws ValueException {
    try {
      ValueUtils.require(Entities.get(chord, "name"), "Name");
      ValueUtils.require(Entities.get(chord, "position"), "Position");
    } catch (EntityException e) {
      throw new ValueException(e);
    }
  }

  /**
   * Whether this is a chord of any tonal kind
   * ChordEntity nodes able to parse No ChordEntity notation https://www.pivotaltracker.com/story/show/158715321
   */
  public Boolean isChord() {
    return !isNoChord();
  }

  /**
   * Whether this is a No ChordEntity instance
   * ChordEntity nodes able to parse No ChordEntity notation https://www.pivotaltracker.com/story/show/158715321
   */
  public Boolean isNoChord() {
    return toMusical().isNoChord();
  }

  /**
   * Name
   *
   * @return name
   */
  public String getName() {
    return this.name;
  }

  /**
   * Set name
   *
   * @param name to set
   * @return this (for chaining setters)
   */
  public ChordEntity setName(String name) {
    this.name = name;
    return this;
  }

  /**
   * Position
   *
   * @return position
   */
  public Double getPosition() {
    return this.position;
  }

  /**
   * Set position
   *
   * @param position to set
   * @return this (for chaining setters)
   */
  public ChordEntity setPosition(Double position) {
    this.position = position;
    return this;
  }

  /**
   * Returns a musical chord of the current entity, for music related operations
   *
   * @return musical chord
   */
  public io.xj.lib.music.Chord toMusical() {
    return new io.xj.lib.music.Chord(name);
  }

  @Override
  public String toString() {
    return name + "@" + position;
  }

}
