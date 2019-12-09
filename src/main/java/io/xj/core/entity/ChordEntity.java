// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.entity;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;

import java.util.Comparator;

/**
 This represents common properties of all entities,
 although a ChordEntity only actually exists as a Segment ChordEntity, Pattern ChordEntity, etc.
 */
public abstract class ChordEntity extends Entity {
  public static final ImmutableList<String> RESOURCE_ATTRIBUTE_NAMES =
    ImmutableList.<String>builder()
      .addAll(Entity.RESOURCE_ATTRIBUTE_NAMES)
      .add("name")
      .add("position")
      .build();
  private String name;
  private Double position;
  /**
   Sort Chords by Position in Ascending order
   */
  public static Comparator<? super ChordEntity> byPositionAscending = Comparator.comparing(ChordEntity::getPosition);

  /*
   Sort Chords by Position in Descending order
   */
//Comparator<? super ChordEntity> byPositionDescending = (Comparator<? super ChordEntity>) (o1, o2) -> o2.getPosition().compareTo(o1.getPosition());

  /**
   validation of common ChordEntity attributes

   @throws CoreException on invalid
   */
  public static void validate(ChordEntity chord) throws CoreException {
    require(chord.getName(), "Name");
    require(chord.getPosition(), "Position");
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
    this.position = limitDecimalPrecision(position);
    return this;
  }

  /**
   Returns a musical chord of the current entity, for music related operations

   @return musical chord
   */
  public io.xj.music.Chord toMusical() {
    return new io.xj.music.Chord(name);
  }

  @Override
  public String toString() {
    return name + "@" + position;
  }

}
